package uk.ac.imperial.colrdtrls;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;

import uk.ac.imperial.colrdtrls.facts.CellTranslator;
import uk.ac.imperial.presage2.core.network.NetworkConstraint;
import uk.ac.imperial.presage2.core.plugin.PluginModule;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.MessagesToRuleEngine;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.rules.facts.SimParticipantsTranslator;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.location.area.AreaService;
import uk.ac.imperial.presage2.util.network.NetworkModule;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

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
	@Parameter(name = "tokens")
	public int tokens;

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
				.addParticipantGlobalEnvironmentService(AreaService.class)
				.setStorage(RuleStorage.class));
		modules.add(new RuleModule().addClasspathDrlFile("ColrdTrls.drl")
				.addClasspathDrlFile("MoveHandler.drl")
				.addClasspathDrlFile("Goals.drl")
				.addClasspathDrlFile("TokenExchange.drl")
				.addStateTranslator(SimParticipantsTranslator.class)
				.addAgentStateTranslator(CellTranslator.class));
		Set<Class<? extends NetworkConstraint>> constraints = new HashSet<Class<? extends NetworkConstraint>>();
		constraints.add(MessagesToRuleEngine.class);
		modules.add(NetworkModule.constrainedNetworkModule(constraints)
				.withNodeDiscovery());
		modules.add(new PluginModule().addPlugin(TokenStoragePlugin.class));
		modules.add(new AbstractModule() {
			@Override
			protected void configure() {
				UniformTokenDistribution tokenDist = new UniformTokenDistribution(
						randomSeed, tokens);
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
			AbstractParticipant p = new TestAgent(Random.randomUUID(), "agent"
					+ i, startLoc);
			s.addParticipant(p);
			// s.addParticipant(new RuleAgent(Random.randomUUID(), "agent" + i,
			// startLoc, "BasicAgent.drl"));
		}
	}

	@Inject
	public void setSession(StatefulKnowledgeSession session) {
		this.session = session;
	}

}
