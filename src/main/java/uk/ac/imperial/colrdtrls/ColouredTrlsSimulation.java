package uk.ac.imperial.colrdtrls;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.colrdtrls.facts.CellTranslator;
import uk.ac.imperial.presage2.core.plugin.PluginModule;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.rules.facts.SimParticipantsTranslator;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.network.NetworkModule;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class ColouredTrlsSimulation extends InjectedSimulation {

	private final Logger logger = Logger
			.getLogger(ColouredTrlsSimulation.class);

	@Parameter(name = "x")
	public int x;
	@Parameter(name = "y")
	public int y;
	@Parameter(name = "turnlength")
	public int turnLength;
	@Parameter(name = "seed")
	public int randomSeed;
	@Parameter(name = "agents")
	public int agents;

	private StatefulKnowledgeSession session;

	public ColouredTrlsSimulation(Set<AbstractModule> modules) {
		super(modules);
	}

	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		modules.add(Area.Bind.area2D(x, y));
		modules.add(new AbstractEnvironmentModule()
				.addParticipantEnvironmentService(
						ParticipantLocationService.class)
				.addActionHandler(MoveHandler.class)
				.addActionHandler(SurrenderHandler.class)
				.addParticipantGlobalEnvironmentService(
						KnowledgeBaseService.class)
				.addParticipantGlobalEnvironmentService(TileColourService.class)
				.setStorage(RuleStorage.class));
		modules.add(new RuleModule().addClasspathDrlFile("ColrdTrls.drl")
				.addClasspathDrlFile("MoveHandler.drl")
				.addClasspathDrlFile("Goals.drl")
				.addStateTranslator(SimParticipantsTranslator.class)
				.addAgentStateTranslator(CellTranslator.class));
		modules.add(NetworkModule.fullyConnectedNetworkModule());
		modules.add(new PluginModule().addPlugin(TokenStoragePlugin.class)
				.addPlugin(GameDisplayPlugin.class));
		modules.add(new AbstractModule() {
			@Override
			protected void configure() {
				RandomTokenDistribution tokenDist = new RandomTokenDistribution(
						randomSeed);
				bind(TileColourGenerator.class).toInstance(tokenDist);
				bind(TokenAllocator.class).toInstance(tokenDist);
			}
		});
		return modules;
	}

	@Override
	protected void addToScenario(Scenario s) {
		session.setGlobal("colrdtrlsLogger", logger);

		for (int i = 0; i < agents; i++) {
			int initialX = Random.randomInt(x);
			int initialY = Random.randomInt(y);
			Cell startLoc = new Cell(initialX, initialY);
			s.addParticipant(new TestAgent(Random.randomUUID(), "agent" + i,
					startLoc));
		}
	}

	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

}
