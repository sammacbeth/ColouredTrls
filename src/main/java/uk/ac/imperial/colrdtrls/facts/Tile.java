package uk.ac.imperial.colrdtrls.facts;

import uk.ac.imperial.presage2.util.location.Cell;

public class Tile {

	public Cell location;
	public Colour col;

	public Tile(Cell location, Colour col) {
		super();
		this.location = location;
		this.col = col;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Tile other = (Tile) obj;
		if (location == null) {
			if (other.location != null) {
				return false;
			}
		} else if (!location.equals(other.location)) {
			return false;
		}
		return true;
	}

	public Cell getLocation() {
		return location;
	}

	public Colour getCol() {
		return col;
	}

	@Override
	public String toString() {
		return "Tile [location=" + location + ", col=" + col + "]";
	}

}
