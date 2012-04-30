package uk.ac.imperial.colrdtrls;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.colrdtrls.facts.Owns;
import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.colrdtrls.facts.Surrendered;
import uk.ac.imperial.colrdtrls.facts.Tile;
import uk.ac.imperial.presage2.core.db.StorageService;
import uk.ac.imperial.presage2.core.environment.EnvironmentRegistrationRequest;
import uk.ac.imperial.presage2.core.environment.EnvironmentService;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.EnvironmentSharedStateAccess;
import uk.ac.imperial.presage2.core.environment.ServiceDependencies;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.event.EventListener;
import uk.ac.imperial.presage2.core.simulator.Events;
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
	private KnowledgeBaseService knowledge;

	private Colour[][] tileOwners;

	private StatefulKnowledgeSession session;

	private final TileColourGenerator generator;
	private final TokenAllocator allocator;

	private final Map<UUID, Map<Colour, FactHandle>> ownsHandles = new HashMap<UUID, Map<Colour, FactHandle>>();
	private final Map<UUID, Map<Colour, FactHandle>> surrenderedHandles = new HashMap<UUID, Map<Colour, FactHandle>>();

	private StorageService persist;

	@Inject
	public TileColourService(EnvironmentSharedStateAccess sharedState,
			EnvironmentServiceProvider serviceProvider, EventBus eb,
			TileColourGenerator generator, TokenAllocator allocator)
			throws UnavailableServiceException {
		super(sharedState);
		this.serviceProvider = serviceProvider;
		this.generator = generator;
		this.allocator = allocator;
		eb.subscribe(this);
	}

	@Inject(optional = true)
	void setPersistence(StorageService s) {
		persist = s;
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

	protected KnowledgeBaseService getKnowledgeBase() {
		if (knowledge == null) {
			try {
				knowledge = serviceProvider
						.getEnvironmentService(KnowledgeBaseService.class);
			} catch (UnavailableServiceException e) {
				throw new RuntimeException(e);
			}
		}
		return knowledge;
	}

	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

	@Override
	public void registerParticipant(EnvironmentRegistrationRequest req) {
		UUID aid = req.getParticipantID();
		Player p = getKnowledgeBase().getPlayer(aid);
		Map<Colour, FactHandle> ownsTokenMap = new HashMap<Colour, FactHandle>();
		Map<Colour, FactHandle> surrenderedTokenMap = new HashMap<Colour, FactHandle>();
		ownsHandles.put(aid, ownsTokenMap);
		surrenderedHandles.put(aid, surrenderedTokenMap);
		for (Colour c : Colour.values()) {
			int tokens = allocator.allocateTokens(aid, c);
			Owns o = new Owns(p, c, tokens);
			Surrendered s = new Surrendered(p, c, 0);
			ownsTokenMap.put(c, session.insert(o));
			surrenderedTokenMap.put(c, session.insert(s));
		}
		logger.info("Player: " + p.getAgent().getAid()
				+ " got token allocation: ");
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
				Cell c = new Cell(x, y);
				tileOwners[x][y] = generator.getTileColour(c);
				session.insert(new Tile(c, tileOwners[x][y]));
				if (persist != null) {
					persist.getSimulation()
							.getEnvironment()
							.setProperty("tile-" + x + "-" + y,
									tileOwners[x][y].toString());
				}
			}
		}

	}

	public Colour getTileColour(int x, int y) throws IndexOutOfBoundsException {
		QueryResults results = session.getQueryResults("tile", new Cell(x, y));
		if (results.size() == 1) {
			for (QueryResultsRow r : results) {
				return ((Tile) r.get("tile")).getCol();
			}
		}
		throw new IndexOutOfBoundsException(
				"Could not get tile from knowledgebase: at (" + x + "," + y
						+ ")");
	}

	public Map<Colour, Integer> getPlayerTokens(final UUID aid) {
		if (ownsHandles.containsKey(aid)) {
			Map<Colour, Integer> tokens = new HashMap<Colour, Integer>();
			for (Map.Entry<Colour, FactHandle> entry : ownsHandles.get(aid)
					.entrySet()) {
				Owns o = (Owns) session.getObject(entry.getValue());
				tokens.put(o.getColour(), o.getCount());
			}
			return tokens;
		} else
			return null;
	}

}
