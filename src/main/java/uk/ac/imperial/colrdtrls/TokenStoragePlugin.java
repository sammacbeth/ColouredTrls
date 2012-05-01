package uk.ac.imperial.colrdtrls;

import java.util.Map;
import java.util.UUID;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.colrdtrls.facts.Goal;
import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.db.persistent.TransientAgentState;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.plugin.Plugin;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;

import com.google.inject.Inject;

public class TokenStoragePlugin implements Plugin {

	StorageService storage;
	private final EnvironmentMembersService membersService;
	final TileColourService tileService;
	final KnowledgeBaseService knowledge;

	@Inject
	public TokenStoragePlugin(EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		this.storage = null;
		this.membersService = serviceProvider
				.getEnvironmentService(EnvironmentMembersService.class);
		this.tileService = serviceProvider
				.getEnvironmentService(TileColourService.class);
		this.knowledge = serviceProvider
				.getEnvironmentService(KnowledgeBaseService.class);
	}

	@Inject(optional = true)
	public void setStorage(StorageService storage) {
		this.storage = storage;
	}

	@Override
	public void incrementTime() {
		if (this.storage != null) {
			for (UUID pid : this.membersService.getParticipants()) {
				Map<Colour, Integer> tokens = this.tileService
						.getPlayerTokens(pid);
				TransientAgentState s = this.storage.getAgentState(pid, SimTime
						.get().intValue());
				if (tokens != null) {
					for (Map.Entry<Colour, Integer> entry : tokens.entrySet()) {
						s.setProperty(entry.getKey().toString(), entry
								.getValue().toString());
					}
				}
				Player p = this.knowledge.getPlayer(pid);
				s.setProperty("payoff", Integer.toString(p.getUtilityEarnt()));
				s.setProperty("x",
						Integer.toString((int) p.getLocation().getX()));
				s.setProperty("y",
						Integer.toString((int) p.getLocation().getY()));

				Goal g = this.knowledge.getGoal(pid);
				if (g != null) {
					s.setProperty("goal-x",
							Integer.toString((int) g.getGoal().getX()));
					s.setProperty("goal-y",
							Integer.toString((int) g.getGoal().getY()));
				}
			}
		}
	}

	@Override
	public void initialise() {

	}

	@Override
	public void execute() {

	}

	@Override
	public void onSimulationComplete() {

	}

}
