package uk.ac.imperial.colrdtrls.facts;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.util.location.Cell;

public class Move implements Action {

	Player actor;
	Cell from;
	Cell to;
	int turn;

	public Move(Cell to) {
		super();
		this.to = to;
	}

	public Move(Cell from, Cell to) {
		super();
		this.from = from;
		this.to = to;
	}

	public Move(Player actor, Cell from, Cell to, int timestamp) {
		super();
		this.actor = actor;
		this.from = from;
		this.to = to;
		this.turn = timestamp;
	}

	public Player getActor() {
		return actor;
	}

	public void setTurn(int turn) {
		this.turn = turn;
	}

	public Cell getFrom() {
		return from;
	}

	public void setFrom(Cell from) {
		this.from = from;
	}

	public Cell getTo() {
		return to;
	}

	public int getTurn() {
		return turn;
	}

	public void setActor(Player actor) {
		this.actor = actor;
	}

	@Override
	public String toString() {
		return "Move [actor=" + actor + ", from=" + from + ", to=" + to
				+ ", turn=" + turn + "]";
	}

}
