package uk.ac.imperial.colrdtrls.facts;

import java.io.Serializable;
import java.util.UUID;

import uk.ac.imperial.presage2.rules.facts.Agent;
import uk.ac.imperial.presage2.rules.facts.location.LocationTranslator;
import uk.ac.imperial.presage2.util.location.Cell;

public class CellTranslator extends LocationTranslator {

	@Override
	public Object getFactObject(String name, UUID aid, Serializable value) {
		return new Player(new Agent(aid), (Cell) value);
	}

	@Override
	public Serializable getStateFromFact(Object fact) {
		return ((Player) fact).getLocation();
	}

}
