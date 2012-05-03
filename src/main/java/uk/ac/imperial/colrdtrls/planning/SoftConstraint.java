package uk.ac.imperial.colrdtrls.planning;

import java.util.LinkedList;

import uk.ac.imperial.presage2.util.location.Cell;

public interface SoftConstraint {

	double pathCost(LinkedList<Cell> path);

	double estimatedCostTo(LinkedList<Cell> path, Cell goal);

}
