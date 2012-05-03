package uk.ac.imperial.colrdtrls.planning;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import uk.ac.imperial.colrdtrls.TileColourService;
import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;
import uk.ac.imperial.presage2.util.location.Cell;

public class NoInfringmentConstraint implements HardConstraint {

	final UUID pid;
	final private TileColourService tileService;
	Map<Colour, Integer> tokens;

	public NoInfringmentConstraint(UUID pid, TileColourService tileService,
			EventBus eb) {
		super();
		this.pid = pid;
		this.tileService = tileService;
		tokens = tileService.getPlayerTokens(pid);
		eb.subscribe(this);
	}

	@Override
	public boolean isViolated(LinkedList<Cell> path) {
		Map<Colour, Integer> tokenCopy = new HashMap<Colour, Integer>(
				this.tokens);
		Iterator<Cell> pathIterator = path.iterator();
		// ignore first cell (already on it)
		pathIterator.next();
		while (pathIterator.hasNext()) {
			Cell tile = pathIterator.next();
			Colour c = getTileColour((int) tile.getX(), (int) tile.getY());
			int count = tokenCopy.get(c) - 1;
			if (count < 0)
				return true;
			tokenCopy.put(c, count);
		}
		return false;
	}

	Colour getTileColour(int x, int y) {
		return tileService.getTileColour(x, y);
	}

	@EventListener
	public void incrementTime(EndOfTimeCycle e) {
		this.tokens = tileService.getPlayerTokens(pid);
	}

}
