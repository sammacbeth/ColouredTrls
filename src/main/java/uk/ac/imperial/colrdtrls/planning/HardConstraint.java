package uk.ac.imperial.colrdtrls.planning;

import java.util.LinkedList;

import uk.ac.imperial.presage2.util.location.Cell;

public interface HardConstraint {

	boolean isViolated(LinkedList<Cell> path);
	
}
