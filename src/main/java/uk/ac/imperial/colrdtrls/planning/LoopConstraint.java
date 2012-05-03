package uk.ac.imperial.colrdtrls.planning;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import uk.ac.imperial.presage2.util.location.Cell;

public class LoopConstraint implements HardConstraint {

	@Override
	public boolean isViolated(LinkedList<Cell> path) {
		Set<Cell> set = new HashSet<Cell>(path);
		return path.size() > set.size();
	}

}
