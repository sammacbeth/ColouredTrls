package uk.ac.imperial.colrdtrls.facts;

import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.simulator.SimTime;

public class Infringement {

	Player p;
	Action action;
	int time;

	public Infringement(Player p, Action action) {
		super();
		this.p = p;
		this.action = action;
		try {
			this.time = SimTime.get().intValue();
		} catch (NullPointerException e) {
			this.time = 0;
		}
	}

	public Action getAction() {
		return action;
	}

	public int getTime() {
		return time;
	}

}
