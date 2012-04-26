package uk.ac.imperial.colrdtrls;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.drools.runtime.ObjectFilter;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.colrdtrls.facts.Owns;
import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.colrdtrls.facts.Surrendered;
import uk.ac.imperial.colrdtrls.facts.Tile;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.Events;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.area.AreaService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
@ServiceDependencies({ AreaService.class })
public class TileColourService extends EnvironmentService {

	final private Logger logger = Logger.getLogger(TileColourService.class);

	final private EnvironmentServiceProvider serviceProvider;
	private AreaService areaService;
	private Colour[][] tileOwners;

	private StatefulKnowledgeSession session;

	@Inject
	public TileColourService(EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider, EventBus eb)
			throws UnavailableServiceException {
		super(sharedState);
		this.serviceProvider = serviceProvider;
		eb.subscribe(this);
	}

	protected AreaService getAreaService() {
		if (areaService == null) {
			try {
				areaService = serviceProvider
						.getEnvironmentService(AreaService.class);
			} catch (UnavailableServiceException e) {
				throw new RuntimeException(e);
			}
		}
		return areaService;
	}

	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

	@EventListener
	public void onInitialiseComplete(Events.Initialised e) {
		logger.info("Generating tile colours.");
		getAreaService();
		tileOwners = new Colour[areaService.getSizeX()][areaService.getSizeY()];

		for (Colour c : Colour.values()) {
			session.insert(c);
		}

		// generate tile colours
		for (int x = 0; x < areaService.getSizeX(); x++) {
			for (int y = 0; y < areaService.getSizeY(); y++) {
				Colour col;
				switch (Random.randomInt(Colour.values().length)) {
				case 0:
					col = Colour.BLUE;
					break;
				case 1:
					col = Colour.GREEN;
					break;
				case 2:
					col = Colour.PURPLE;
					break;
				case 3:
					col = Colour.RED;
					break;
				default:
					col = Colour.YELLOW;
					break;
				}
				tileOwners[x][y] = col;
				Cell c = new Cell(x, y);
				session.insert(new Tile(c, tileOwners[x][y]));
			}
		}
		// initialise players
		Collection<FactHandle> handles = session
				.getFactHandles(new ObjectFilter() {
					@Override
					public boolean accept(Object object) {
						return object instanceof Player;
					}
				});
		for (FactHandle factHandle : handles) {
			Player p = (Player) session.getObject(factHandle);
			for (Colour c : Colour.values()) {
				int tokens = Random.randomInt(10);
				Owns o = new Owns(p, c, tokens);
				Surrendered s = new Surrendered(p, c, 0);
				session.insert(o);
				session.insert(s);
			}
			session.update(factHandle, p);
			logger.info("Player: " + p.getAgent().getAid()
					+ " got token allocation: " + p.owns);
		}
	}

	public Colour getTileColour(int x, int y) {
		QueryResults results = session.getQueryResults("tile", new Cell(x, y));
		if (results.size() == 1) {
			for (QueryResultsRow r : results) {
				return ((Tile) r.get("tile")).getCol();
			}
		}
		throw new RuntimeException("Could not get tile from knowledgebase.");
	}

}
