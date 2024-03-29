package MonteCarlo;

import java.util.ArrayList;
import Actor.Agent;
import Actor.GreedyPredator;
import Actor.Prey;
import Enum.Direction;
import Main.RandomSeededDouble;
import Main.State;

public class MonteCarloNodeG extends MonteCarloNode {

	private final int modelPossibility = 8;
	private RandomSeededDouble rand;
	private Direction parentDirection;

	public MonteCarloNodeG(MonteCarloNodeS monteCarloNodeS, Direction parentDirection, RandomSeededDouble rand) {
		super(monteCarloNodeS);
		childsNode = new MonteCarloNode[modelPossibility];
		this.parentNode = monteCarloNodeS;
		this.rand = rand;
		this.parentDirection = parentDirection;
	}

	public MonteCarloNodeS getChild(int indexChild) {
		return (MonteCarloNodeS) childsNode[indexChild];
	}

	public MonteCarloNodeS computeChild(Agent[] generatedAgents, int indexChild) {
		MonteCarloNodeS currentChild = getChild(indexChild);
		// already computed
		if (currentChild != null) {
			return currentChild;
		} else {
			State nextState = ((MonteCarloNodeS) parentNode).getNodeState().clone();
			ArrayList<Direction> directionOfAgents = new ArrayList<Direction>();
			ArrayList<Agent> agents = new ArrayList<Agent>();
			// compute a random object which the seed is the previous state
			// so the generated prey will do the same action for the 4 child of
			// the same
			// parent
			directionOfAgents.add(Direction.values()[(int) (rand.generateDouble() * 4)]);
			agents.add(new Prey(nextState, 1, 0));
			// value for the MTC agent
			directionOfAgents.add(parentDirection);
			// we add a useless agent
			agents.add(new GreedyPredator(nextState, 2, 0));
			for (Agent a : generatedAgents) {
				directionOfAgents.add(a.iterate(nextState));
				agents.add(a);
			}
			nextState.modifyState(directionOfAgents, agents);
			MonteCarloNodeS equivalentChild = findEquivalentChild(nextState.toLong());
			MonteCarloNodeS newChild;
			if (equivalentChild != null) {
				newChild = equivalentChild;
			} else {
				newChild = new MonteCarloNodeS(nextState, this, rand);
			}
			childsNode[indexChild] = newChild;
			return newChild;
		}
	}

	private MonteCarloNodeS findEquivalentChild(long stateHash) {
		for (MonteCarloNode node : childsNode) {
			if (node != null) {
				if (((MonteCarloNodeS) node).getNodeState().toLong() == stateHash) {
					return (MonteCarloNodeS) node;
				}
			}
		}
		return null;

	}

	public MonteCarloNode[] getChilds() {
		return (MonteCarloNode[]) childsNode;
	}
}
