package uk.ac.imperial.colrdtrls.facts;

import uk.ac.imperial.presage2.core.Action;

public class MoveInfringement extends Infringement {

	Colour colour;

	public MoveInfringement(Player p, Action action, Colour colour) {
		super(p, action);
		this.colour = colour;
	}

	public Colour getColour() {
		return colour;
	}

}
