package uk.ac.imperial.colrdtrls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.colrdtrls.facts.Move;
import uk.ac.imperial.colrdtrls.facts.Surrender;
import uk.ac.imperial.colrdtrls.planning.AreaConstraint;
import uk.ac.imperial.colrdtrls.planning.HardConstraint;
import uk.ac.imperial.colrdtrls.planning.LoopConstraint;
import uk.ac.imperial.colrdtrls.planning.NoInfringmentConstraint;
import uk.ac.imperial.colrdtrls.planning.PathPlanner;
import uk.ac.imperial.colrdtrls.planning.SoftConstraint;
import uk.ac.imperial.colrdtrls.planning.StraightLineDistance;
import uk.ac.imperial.colrdtrls.protocols.TokenExchangeProtocol;
import uk.ac.imperial.colrdtrls.protocols.TokenExchangeProtocol.Exchange;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.ParticipantSharedState;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.event.EventBus;
import uk.ac.imperial.presage2.core.messaging.Input;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.ParticipantLocationService;
import uk.ac.imperial.presage2.util.location.area.AreaService;
import uk.ac.imperial.presage2.util.participant.AbstractParticipant;

import com.google.inject.Inject;

public class TestAgent extends AbstractParticipant {

	Cell loc;
	TileColourService tileService;
	KnowledgeBaseService knowledge;
	int nextTurn;
	PathPlanner planner;
	EventBus eb;
	TokenExchangeProtocol tokenExchange;

	public TestAgent(UUID id, String name, Cell start) {
		super(id, name);
		this.loc = start;
	}

	@Inject
	public void setEb(EventBus eb) {
		this.eb = eb;
	}

	@Override
	protected Set<ParticipantSharedState> getSharedState() {
		Set<ParticipantSharedState> ss = super.getSharedState();
		ss.add(ParticipantLocationService.createSharedState(getID(), loc));
		return ss;
	}

	@Override
	public void initialise() {
		super.initialise();
		try {
			this.tileService = getEnvironmentService(TileColourService.class);
			this.knowledge = getEnvironmentService(KnowledgeBaseService.class);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}
		nextTurn = this.knowledge.nextTurn();
		NoInfringmentConstraint infcon = new NoInfringmentConstraint(getID(),
				tileService, eb);
		Set<HardConstraint> hardConstraints = new HashSet<HardConstraint>();
		hardConstraints.add(new LoopConstraint());
		try {
			hardConstraints.add(new AreaConstraint(
					getEnvironmentService(AreaService.class)));
			hardConstraints.add(infcon);
		} catch (UnavailableServiceException e) {
			logger.warn(e);
		}
		Set<SoftConstraint> softConstraints = new HashSet<SoftConstraint>();
		softConstraints.add(new StraightLineDistance());
		softConstraints.add(infcon);

		planner = new PathPlanner(getID(), knowledge, hardConstraints,
				softConstraints);

		try {
			this.tokenExchange = new TokenExchangeProtocol(getID(), authkey,
					environment, network) {

				@Override
				protected boolean acceptExchange(NetworkAddress from,
						Exchange exchange) {
					// accept if I have a token to exchange.
					if (tileService.getPlayerTokens(getID()).get(
							exchange.getMine()) > 0) {
						return true;
					}
					return false;
				}
			};
		} catch (FSMException e) {
			logger.warn("Error creating token exchange protocol", e);
		}
	}

	@Override
	protected void processInput(Input in) {
		if (this.tokenExchange != null && this.tokenExchange.canHandle(in)) {
			this.tokenExchange.handle(in);
		}
	}

	@Override
	public void execute() {
		super.execute();
		int time = SimTime.get().intValue();
		this.loc = this.knowledge.getPlayer(getID()).getLocation();
		logger.info("My Location is: " + this.loc);

		if (nextTurn == time) {
			Move m = planner.getNextMove();
			if (m != null) {
				Colour c = this.tileService.getTileColour((int) m.getTo()
						.getX(), (int) m.getTo().getY());
				try {
					this.environment.act(m, getID(), authkey);
					this.environment.act(new Surrender(c), getID(), authkey);
				} catch (ActionHandlingException e) {
					logger.warn("", e);
				}

			}

			nextTurn = this.knowledge.nextTurn();
		}
		if (this.tokenExchange != null) {
			if (this.tokenExchange.getActiveConversations().size() == 0) {
				for (NetworkAddress a : this.network.getConnectedNodes()) {
					List<Colour> colours = new ArrayList<Colour>(
							Arrays.asList(Colour.values()));
					Colour c1 = colours.get(Random.randomInt(colours.size()));
					colours.remove(c1);
					Colour c2 = colours.get(Random.randomInt(colours.size()));
					try {
						this.tokenExchange.offer(a, c1, c2);
					} catch (FSMException e) {
						logger.warn("Error creating token offer", e);
					}
				}
			}
			this.tokenExchange.incrementTime();
		}
	}

}
