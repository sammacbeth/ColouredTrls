package uk.ac.imperial.colrdtrls;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.Events;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import uk.ac.imperial.presage2.util.location.area.AreaService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@ServiceDependencies({ EnvironmentMembersService.class, AreaService.class })
public class TileColourService extends EnvironmentService {

	final private EnvironmentMembersService membersService;
	final private AreaService areaService;
	private UUID[][] tileOwners;

	@Inject
	public TileColourService(EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider, EventBus eb)
			throws UnavailableServiceException {
		super(sharedState);
		eb.subscribe(this);
		membersService = serviceProvider
				.getEnvironmentService(EnvironmentMembersService.class);
		areaService = serviceProvider.getEnvironmentService(AreaService.class);
	}

	@EventListener
	public void onInitialiseComplete(Events.Initialised e) {
		// get game players
		List<UUID> players = new ArrayList<UUID>(
				membersService.getParticipants());
		tileOwners = new UUID[areaService.getSizeX()][areaService.getSizeY()];
		// generate tile colours
		for (int x = 0; x < areaService.getSizeX(); x++) {
			for (int y = 0; y < areaService.getSizeY(); y++) {
				tileOwners[x][y] = players
						.get(Random.randomInt(players.size()));
			}
		}
	}

	public UUID getTileOwner(int x, int y) {
		return tileOwners[x][y];
	}

}
