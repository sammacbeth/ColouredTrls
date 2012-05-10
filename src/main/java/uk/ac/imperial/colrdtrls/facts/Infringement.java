package uk.ac.imperial.colrdtrls.facts;

import uk.ac.imperial.presage2.core.simulator.SimTime;

public class Infringement {

	Player p;
	Object action;
	int time;

	public Infringement(Player p, Object action) {
		super();
		this.p = p;
		this.action = action;
		try {
			this.time = SimTime.get().intValue();
		} catch (NullPointerException e) {
			this.time = 0;
		}
	}

	public Object getAction() {
		return action;
	}

	public int getTime() {
		return time;
	}

	@Override
	public String toString() {
		return "Infringement [p=" + p.getAgent().getAid() + ", action=" + action + ", time=" + time
				+ "]";
	}

}
