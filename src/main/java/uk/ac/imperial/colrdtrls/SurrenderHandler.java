package uk.ac.imperial.colrdtrls;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import com.google.inject.Inject;

import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.colrdtrls.facts.Surrender;
import uk.ac.imperial.presage2.core.Action;
import uk.ac.imperial.presage2.core.environment.ActionHandler;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.messaging.Input;

public class SurrenderHandler implements ActionHandler {

	final private Logger logger = Logger.getLogger(Surrender.class);
	final StatefulKnowledgeSession session;
	final KnowledgeBaseService knowledgeService;

	@Inject
	public SurrenderHandler(StatefulKnowledgeSession session,
			EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		super();
		this.session = session;
		this.knowledgeService = serviceProvider
				.getEnvironmentService(KnowledgeBaseService.class);
	}

	@Override
	public boolean canHandle(Action action) {
		return action instanceof Surrender;
	}

	@Override
	public Input handle(Action action, UUID actor)
			throws ActionHandlingException {
		Surrender s = (Surrender) action;
		Player p = knowledgeService.getPlayer(actor);
		s.setActor(p);
		session.insert(s);
		logger.info("Handling: " + s);
		return null;
	}

}
