package uk.ac.imperial.colrdtrls.planning;

import java.util.LinkedList;

import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.area.AreaService;

public class AreaConstraint implements HardConstraint {

	int sizeX;
	int sizeY;

	public AreaConstraint(AreaService areaService) {
		super();
		sizeX = areaService.getSizeX();
		sizeY = areaService.getSizeY();
	}

	public AreaConstraint(int sizeX, int sizeY) {
		super();
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}

	@Override
	public boolean isViolated(LinkedList<Cell> path) {
		if (path.size() > 0) {
			Cell c = path.getLast();
			return c.getX() < 0 || c.getX() >= sizeX || c.getY() < 0
					|| c.getY() >= sizeY;
		}
		return false;
	}

}
