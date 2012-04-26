package uk.ac.imperial.colrdtrls;

import java.util.Collection;
import java.util.UUID;

import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.simulator.SimTime;

@Singleton
public class KnowledgeBaseService extends EnvironmentService {

	final StatefulKnowledgeSession session;
	int currentTurn = 0;
	final int turnLength;

	@Inject
	protected KnowledgeBaseService(StatefulKnowledgeSession session,
			EnvironmentSharedStateAccess sharedState,
			@Named("params.turnlength") int turnLength) {
		super(sharedState);
		this.session = session;
		this.turnLength = turnLength;
	}

	public Player getPlayer(final UUID aid) {
		Collection<Object> matches = session.getObjects(new ObjectFilter() {
			@Override
			public boolean accept(Object object) {
				if (object instanceof Player) {
					Player p = (Player) object;
					return p.getAgent().getAid().equals(aid);
				}
				return false;
			}
		});
		for (Object player : matches) {
			return (Player) player;
		}
		return null;
	}

	public int nextTurn() {
		int time = SimTime.get().intValue();
		return ((int) Math.floor(((double) time / (double) turnLength)))
				* turnLength + turnLength;
	}

}
