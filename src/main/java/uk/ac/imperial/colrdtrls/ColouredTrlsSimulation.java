package uk.ac.imperial.colrdtrls;

import java.util.HashSet;
import java.util.Set;

import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.simulator.InjectedSimulation;
import uk.ac.imperial.presage2.core.simulator.Parameter;
import uk.ac.imperial.presage2.core.simulator.Scenario;
import uk.ac.imperial.presage2.util.environment.AbstractEnvironmentModule;
import uk.ac.imperial.presage2.util.location.MoveHandler;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.Area;
import uk.ac.imperial.presage2.util.network.NetworkModule;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

public class ColouredTrlsSimulation extends InjectedSimulation {

	@Parameter(name = "x")
	public int x;
	@Parameter(name = "y")
	public int y;
	@Parameter(name = "turnlength")
	public int turnLength;

	public ColouredTrlsSimulation(Set<AbstractModule> modules) {
		super(modules);
	}

	@Override
	protected Set<AbstractModule> getModules() {
		Set<AbstractModule> modules = new HashSet<AbstractModule>();
		modules.add(Area.Bind.area2D(x, y));
		modules.add(new AbstractEnvironmentModule()
			.addActionHandler(MoveHandler.class)
			.addParticipantEnvironmentService(ParticipantLocationService.class)
			.addParticipantGlobalEnvironmentService(TileColourService.class));
		modules.add(NetworkModule.fullyConnectedNetworkModule());
		return modules;
	}

	@Override
	protected void addToScenario(Scenario s) {
		// TODO Auto-generated method stub

	}

	@Inject
	public void registerEventBus(EventBus eb) {
		eb.subscribe(this);
	}

}
