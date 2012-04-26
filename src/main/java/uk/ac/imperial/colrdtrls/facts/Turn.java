package uk.ac.imperial.colrdtrls.facts;

import org.drools.definition.type.Position;

public class Turn {

	@Position(0)
	int turn;

	public Turn(int turn) {
		super();
		this.turn = turn;
	}

	public int getTurn() {
		return turn;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + turn;
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
		Turn other = (Turn) obj;
		if (turn != other.turn) {
			return false;
		}
		return true;
	}

}
