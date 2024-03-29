package Actor;

import Enum.Direction;
import Enum.AgentType;
import Main.State;

//extension of an agent, the greedy predator just want to get closer to the prey, not considering the others agents
public class GreedyPredator extends Agent {

	public GreedyPredator(int x, int y, int agentIndex, long randSeed) {
		super(x, y, agentIndex, randSeed);
		type = AgentType.Greedy;
	}

	// second constructor used for MCT
	// create an agent that match the current state parameter
	public GreedyPredator(State s, int agentIndex, long randSeed) {
		super(s.getAgentsCoordinateList()[agentIndex - 1][0], s.getAgentsCoordinateList()[agentIndex - 1][1],
				agentIndex, randSeed);
		type = AgentType.Greedy;
	}

	@Override
	public Direction iterate(State state) {
		int width = state.getMapWidth();
		int height = state.getMapHeight();
		int preyX = state.getPreyPosX();
		int preyY = state.getPreyPosY();
		// compute the relative distance between the prey and this agent
		int dx = (preyX - posX + width) % width;
		int dy = (preyY - posY + height) % height;
		// If already neighboring the prey, try to move onto the prey so that if
		// it moves, the predator will follow.
		if (dx == 0 && dy == 1) {
			return Direction.BOTTOM;
		}
		if (dx == 1 && dy == 0) {
			return Direction.RIGHT;
		}
		if (dx == 0 && dy == height - 1) {
			return Direction.TOP;
		}
		if (dx == width - 1 && dy == 0) {
			return Direction.LEFT;
		}
		// Choose the nearest unoccupied cell neighboring the prey as the
		// destination.
		if (!state.isPreyCaptured()) {
			int destinationX = preyX;
			int destinationY = preyY;
			Direction nearestEmptyCellDirection = findNearestEmptyCell(state, dx, dy, width, height);
			// computes the destination position
			switch (nearestEmptyCellDirection) {
			case LEFT:
				destinationX = (preyX - 1 + width) % width;
				break;
			case TOP:
				destinationY = (preyY - 1 + height) % height;
				break;
			case RIGHT:
				destinationX = (preyX + 1) % width;
				break;
			case BOTTOM:
				destinationY = (preyY + 1) % height;
				break;
			default:
				break;
			}
			// format the dx and dy to be a relative position around the prey:
			// [0 : width-1] -> [-width/2 : width/2]
			dx = (destinationX - posX + width) % width;
			dy = (destinationY - posY + height) % height;
			if (dx > width / 2) {
				dx = dx - width;
			}
			if (dy > height / 2) {
				dy = dy - height;
			}
			// choose the best path in function of dy and dx, if the direction
			// is not blocked
			if (Math.abs(dy) > Math.abs(dx)) {
				if (dy < 0) {
					if (!state.isDirectionBlocked(posX, posY, Direction.TOP)) {
						return Direction.TOP;
					}
				} else {
					if (!state.isDirectionBlocked(posX, posY, Direction.BOTTOM)) {
						return Direction.BOTTOM;
					}
				}
			} else {
				if (dx < 0) {
					if (!state.isDirectionBlocked(posX, posY, Direction.LEFT)) {
						return Direction.LEFT;
					}
				} else {
					if (!state.isDirectionBlocked(posX, posY, Direction.RIGHT)) {
						return Direction.RIGHT;
					}
				}
			}
			if (Math.abs(dy) < Math.abs(dx)) {
				if (dy < 0) {
					if (!state.isDirectionBlocked(posX, posY, Direction.TOP)) {
						return Direction.TOP;
					}
				} else {
					if (!state.isDirectionBlocked(posX, posY, Direction.BOTTOM)) {
						return Direction.BOTTOM;
					}
				}
			} else {
				if (dx < 0) {
					if (!state.isDirectionBlocked(posX, posY, Direction.LEFT)) {
						return Direction.LEFT;
					}
				} else {
					if (!state.isDirectionBlocked(posX, posY, Direction.RIGHT)) {
						return Direction.RIGHT;
					}
				}
			}
		}
		// Otherwise, move randomly.
		double randomDouble = rand.generateDouble();
		return Direction.values()[(int) (randomDouble * 4)];
	}

	private Direction findNearestEmptyCell(State state, int dx, int dy, int width, int height) {
		int x = state.getPreyPosX();
		int y = state.getPreyPosY();
		// format the dx and dy to be a relative position around the prey: [0 :
		// width-1] -> [-width/2 : width/2]
		if (dx > width / 2) {
			dx = dx - width;
		}
		if (dy > height / 2) {
			dy = dy - height;
		}

		// compute the order of the closest neighbor of the prey in function of
		// the agent position around the prey.
		Direction[] orderedDirection;
		if (dx < 0) {
			// is bottom right of the prey
			if (dy < 0) {
				// more right
				if (Math.abs(dx) > Math.abs(dy)) {
					orderedDirection = new Direction[] { Direction.RIGHT, Direction.BOTTOM, Direction.TOP,
							Direction.LEFT };
				}
				// more bottom
				else {
					orderedDirection = new Direction[] { Direction.BOTTOM, Direction.RIGHT, Direction.LEFT,
							Direction.TOP };
				}
			}
			// is top right of the prey
			else {
				// more right
				if (Math.abs(dx) > Math.abs(dy)) {
					orderedDirection = new Direction[] { Direction.RIGHT, Direction.TOP, Direction.BOTTOM,
							Direction.LEFT };
				}
				// more top
				else {
					orderedDirection = new Direction[] { Direction.TOP, Direction.RIGHT, Direction.LEFT,
							Direction.BOTTOM };
				}
			}
		} else {
			// is bottom left of the prey
			if (dy < 0) {
				// more left
				if (Math.abs(dx) > Math.abs(dy)) {
					orderedDirection = new Direction[] { Direction.LEFT, Direction.BOTTOM, Direction.TOP,
							Direction.RIGHT };
				}
				// more bottom
				else {
					orderedDirection = new Direction[] { Direction.BOTTOM, Direction.LEFT, Direction.RIGHT,
							Direction.TOP };
				}
			}
			// is top left of the prey
			else {
				// more left
				if (Math.abs(dx) > Math.abs(dy)) {
					orderedDirection = new Direction[] { Direction.LEFT, Direction.TOP, Direction.BOTTOM,
							Direction.RIGHT };
				}
				// more top
				else {
					orderedDirection = new Direction[] { Direction.TOP, Direction.LEFT, Direction.RIGHT,
							Direction.BOTTOM };
				}
			}
		}
		return firstEmptyCell(state, x, y, orderedDirection);
	}

	// return the first cell of the list which is Empty
	private Direction firstEmptyCell(State state, int x, int y, Direction[] listDir) {
		Direction testedDirection;
		for (int i = 0; i < 4; i++) {
			testedDirection = listDir[i];
			if (!state.isDirectionBlocked(x, y, testedDirection)) {
				return testedDirection;
			}
		}
		state.printMapHelper();
		throw new AssertionError("We checked that the prey has at least a free neighbor");
	}

	@Override
	public void initiate(State initialState) {
		// nothing to initialize for a greedyPredator
	}
}