package uk.ac.imperial.colrdtrls.planning;

import java.util.LinkedList;

import uk.ac.imperial.presage2.util.location.Cell;

public class StraightLineDistance implements SoftConstraint {

	@Override
	public double pathCost(LinkedList<Cell> path) {
		return path.size() -1;
	}

	@Override
	public double estimatedCostTo(LinkedList<Cell> path, Cell goal) {
		return path.getLast().distanceTo(goal);
	}

}
