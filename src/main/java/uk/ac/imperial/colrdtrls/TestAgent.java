package uk.ac.imperial.colrdtrls;

import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.colrdtrls.facts.Move;
import uk.ac.imperial.colrdtrls.facts.Surrender;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

public class TestAgent extends AbstractParticipant {

	Cell loc;
	TileColourService tileService;
	KnowledgeBaseService knowledge;
	int nextTurn;

	public TestAgent(UUID id, String name, Cell start) {
		super(id, name);
		this.loc = start;
	}

	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(ParticipantLocationService.createSharedState(getID(), loc));
		return ss;
	}

	@Override
	public void initialise() {
		super.initialise();
		try {
			this.tileService = getEnvironmentService(TileColourService.class);
			this.knowledge = getEnvironmentService(KnowledgeBaseService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}
		nextTurn = this.knowledge.nextTurn();
	}

	@Override
	protected void processInput(Input in) {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute() {
		super.execute();
		int time = SimTime.get().intValue();
		this.loc = this.knowledge.getPlayer(getID()).getLocation();
		logger.info("My Location is: " + this.loc);

		if (nextTurn == time) {
			try {
				int x = (int) (Random.randomInt(3) - 1 + loc.getX());
				int y = (int) (Random.randomInt(3) - 1 + loc.getY());
				Cell target = new Cell(x, y);
				Colour c = this.tileService.getTileColour(x, y);
				logger.info("Move: " + target);
				this.environment.act(new Move(target), getID(), authkey);
				this.environment.act(new Surrender(c), getID(), authkey);
			} catch (IndexOutOfBoundsException e) {
				logger.warn("", e);
			} catch (ActionHandlingException e) {
				logger.warn("", e);
			}

			nextTurn = this.knowledge.nextTurn();
		}
	}

}
