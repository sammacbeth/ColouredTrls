package uk.ac.imperial.colrdtrls;

import java.util.Map;
import java.util.UUID;

import uk.ac.imperial.colrdtrls.facts.Colour;
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

	@Inject
	public TokenStoragePlugin(EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		this.storage = null;
		this.membersService = serviceProvider
				.getEnvironmentService(EnvironmentMembersService.class);
		this.tileService = serviceProvider
				.getEnvironmentService(TileColourService.class);
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
				if (tokens != null) {
					TransientAgentState s = this.storage.getAgentState(pid,
							SimTime.get().intValue());
					for (Map.Entry<Colour, Integer> entry : tokens.entrySet()) {
						s.setProperty(entry.getKey().toString(), entry
								.getValue().toString());
					}
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
