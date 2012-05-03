package uk.ac.imperial.colrdtrls.planning;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import uk.ac.imperial.presage2.util.location.Cell;

public class Path implements Comparable<Path> {

	final static Path EMPTY_PATH = new Path(
			Collections.unmodifiableList(new LinkedList<Cell>()), 0);

	LinkedList<Cell> path;
	double fCost;
	boolean expanded = false;

	public Path(List<Cell> path, double fCost) {
		super();
		this.path = new LinkedList<Cell>(path);
		this.fCost = fCost;
	}
	
	public Path(Cell start, double fCost) {
		super();
		this.path = new LinkedList<Cell>();
		this.path.add(start);
		this.fCost = fCost;
	}

	public boolean finishesAt(Cell c) {
		if (path.size() > 0)
			return path.getLast().equals(c);
		else
			return false;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	@Override
	public int compareTo(Path p) {
		if(this.fCost < p.fCost)
			return -1;
		else if(this.fCost == p.fCost) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public String toString() {
		return "Path [path=" + path + ", fCost=" + fCost + ", expanded="
				+ expanded + "]";
	}

}
