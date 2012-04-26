package uk.ac.imperial.colrdtrls.facts;

import uk.ac.imperial.presage2.core.Action;

public class Infringement {

	Player p;
	Action action;

	public Infringement(Player p, Action action) {
		super();
		this.p = p;
		this.action = action;
	}

	public Action getAction() {
		return action;
	}

}
