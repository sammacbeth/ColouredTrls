package uk.ac.imperial.colrdtrls.facts;

import java.util.HashMap;
import java.util.Map;

import uk.ac.imperial.presage2.rules.facts.Agent;
import uk.ac.imperial.presage2.util.location.Cell;

public class Player {

	Agent agent;
	Cell location;
	public Map<Colour, Owns> owns = new HashMap<Colour, Owns>();
	public Map<Colour, Surrendered> surrendered = new HashMap<Colour, Surrendered>();

	public Player(Agent agent, Cell location) {
		super();
		this.agent = agent;
		this.location = location;
		for (Colour c : Colour.values()) {
			owns.put(c, new Owns(this, c, 0));
			surrendered.put(c, new Surrendered(this, c, 0));
		}
	}

	public Agent getAgent() {
		return agent;
	}

	public Cell getLocation() {
		return location;
	}

	public void setLocation(Cell location) {
		this.location = location;
	}

	public void addToken(Colour c) {
		owns.get(c).count++;
	}

	public void surrenderToken(Colour c) {
		owns.get(c).count--;
		surrendered.get(c).count++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((agent == null) ? 0 : agent.hashCode());
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
		Player other = (Player) obj;
		if (agent == null) {
			if (other.agent != null) {
				return false;
			}
		} else if (!agent.equals(other.agent)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Player [agent=" + agent + ", location=" + location + "]";
	}

}
