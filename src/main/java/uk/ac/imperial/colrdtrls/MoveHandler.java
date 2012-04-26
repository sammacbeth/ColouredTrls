package uk.ac.imperial.colrdtrls;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import uk.ac.imperial.colrdtrls.facts.Move;
import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.colrdtrls.facts.Turn;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.simulator.EndOfTimeCycle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class MoveHandler implements ActionHandler {

	final private Logger logger = Logger.getLogger(MoveHandler.class);

	final int turnLength;
	FactHandle lastTurn = null;
	int currentTurn = 0;
	final KnowledgeBaseService knowledgeService;

	final StatefulKnowledgeSession session;

	@Inject
	public MoveHandler(@Named("params.turnlength") int turnLength,
			StatefulKnowledgeSession session,
			EnvironmentServiceProvider serviceProvider, EventBus eb)
			throws UnavailableServiceException {
		super();
		this.turnLength = turnLength;
		this.session = session;
		this.knowledgeService = serviceProvider
				.getEnvironmentService(KnowledgeBaseService.class);
		eb.subscribe(this);
		session.setGlobal("moveHandlerLogger", logger);
	}

	@Override
	public boolean canHandle(Action action) {
		return action instanceof Move;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		// insert values
		Move m = (Move) action;
		Player p = knowledgeService.getPlayer(actor);
		m.setActor(p);
		m.setFrom(p.getLocation());
		m.setTurn(currentTurn);
		// submit to session
		session.insert(m);
		return null;
	}

	@EventListener
	public void incrementTime(EndOfTimeCycle e) {
		int time = e.getTime().intValue();
		// retract a turn after one timestep
		if (lastTurn != null) {
			session.retract(lastTurn);
			lastTurn = null;
			currentTurn++;
		}
		// insert new turn and update currentTurn to the next value.
		if (knowledgeService.nextTurn() == time + 1) {
			logger.info("Turn " + currentTurn);
			lastTurn = session.insert(new Turn(currentTurn));
		}
	}

}
