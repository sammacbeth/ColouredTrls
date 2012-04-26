package uk.ac.imperial.colrdtrls.facts;

import uk.ac.imperial.presage2.core.Action;

public class Surrender implements Action {

	Player actor;
	Colour colour;

	public Surrender(Colour colour) {
		super();
		this.colour = colour;
	}

	public Surrender(Player actor, Colour colour) {
		super();
		this.actor = actor;
		this.colour = colour;
	}

	public Player getActor() {
		return actor;
	}

	public void setActor(Player actor) {
		this.actor = actor;
	}

	public Colour getColour() {
		return colour;
	}

	@Override
	public String toString() {
		return "Surrender [actor=" + actor + ", colour=" + colour + "]";
	}

}
