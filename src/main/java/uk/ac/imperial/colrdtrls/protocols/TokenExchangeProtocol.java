package uk.ac.imperial.colrdtrls.protocols;

import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.colrdtrls.facts.Surrender;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.environment.ActionHandlingException;
import uk.ac.imperial.presage2.core.environment.EnvironmentConnector;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAdaptor;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.network.UnicastMessage;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.util.fsm.AndCondition;
import uk.ac.imperial.presage2.util.fsm.EventTypeCondition;
import uk.ac.imperial.presage2.util.fsm.FSM;
import uk.ac.imperial.presage2.util.fsm.FSMException;
import uk.ac.imperial.presage2.util.fsm.StateType;
import uk.ac.imperial.presage2.util.fsm.Transition;
import uk.ac.imperial.presage2.util.protocols.ConversationCondition;
import uk.ac.imperial.presage2.util.protocols.ConversationSpawnEvent;
import uk.ac.imperial.presage2.util.protocols.FSMConversation;
import uk.ac.imperial.presage2.util.protocols.FSMProtocol;
import uk.ac.imperial.presage2.util.protocols.InitialiseConversationAction;
import uk.ac.imperial.presage2.util.protocols.MessageAction;
import uk.ac.imperial.presage2.util.protocols.MessageTypeCondition;
import uk.ac.imperial.presage2.util.protocols.SpawnAction;
import uk.ac.imperial.presage2.util.protocols.TimeoutCondition;

public abstract class TokenExchangeProtocol extends FSMProtocol {

	private final Logger logger;
	protected final EnvironmentConnector environment;
	private final UUID myId;
	private final UUID authkey;

	enum State {
		START, OFFER_SENT, OFFER_ACCEPTED, OFFER_REJECTED, OFFER_RECEIVED, ERROR
	};

	enum MessageType {
		OFFER, ACCEPT, REJECT, ACCEPTED, REJECTED, TIMEOUT, ERROR
	};

	public TokenExchangeProtocol(final UUID myId, final UUID authkey,
			EnvironmentConnector environment, NetworkAdaptor network)
			throws FSMException {
		super("TokenExchange", FSM.description(), network);
		this.environment = environment;
		this.myId = myId;
		this.authkey = authkey;

		this.logger = Logger.getLogger("TokenExchange for " + myId);

		// sender side of protocol
		this.description.addState(State.START, StateType.START)
				.addState(State.OFFER_SENT)
				.addState(State.OFFER_ACCEPTED, StateType.END)
				.addState(State.OFFER_REJECTED, StateType.END)
				.addState(State.ERROR, StateType.END);

		/*
		 * Transition: Start -> OFFER_SENT. Send an OFFER message to recipient
		 * with proposed exchange.
		 */
		this.description.addTransition(MessageType.OFFER,
				new EventTypeCondition(ExchangeSpawnEvent.class), State.START,
				State.OFFER_SENT, new SpawnAction() {

					@Override
					public void processSpawn(ConversationSpawnEvent event,
							FSMConversation conv, Transition transition) {
						// send message offering the Exchange of tokens
						// described in the ExchangeSpawnEvent.
						ExchangeSpawnEvent e = (ExchangeSpawnEvent) event;
						NetworkAddress from = conv.getNetwork().getAddress();
						NetworkAddress to = conv.recipients.get(0);
						logger.debug("Initiating: " + e.exchange);
						conv.entity = e.exchange;
						conv.getNetwork().sendMessage(
								new UnicastMessage<Exchange>(
										Performative.PROPOSE, MessageType.OFFER
												.name(), SimTime.get(), from,
										to, e.exchange));
					}
				});
		/*
		 * Transition: OFFER_SENT -> OFFER_ACCEPTED. Offer was accepted,
		 * optionally surrender token.
		 */
		this.description.addTransition(MessageType.ACCEPTED, new AndCondition(
				new ConversationCondition(), new MessageTypeCondition(
						MessageType.ACCEPT.name())), State.OFFER_SENT,
				State.OFFER_ACCEPTED, new MessageAction() {

					@Override
					public void processMessage(Message<?> message,
							FSMConversation conv, Transition transition) {
						if (message.getData() instanceof Exchange) {
							NetworkAddress to = conv.recipients.get(0);
							Exchange exchange = (Exchange) conv.getEntity();
							if (surrenderToken(to, exchange)) {
								EnvironmentConnector env = TokenExchangeProtocol.this.environment;
								logger.debug("Exchange proposal was accepted, surrendering my token.");
								try {
									env.act(new Surrender(exchange.mine), myId,
											authkey);
								} catch (ActionHandlingException e) {
									logger.warn("Error surrendering token", e);
								}
							}
						}
					}
				});
		this.description.addTransition(MessageType.REJECTED, new AndCondition(
				new ConversationCondition(), new MessageTypeCondition(
						MessageType.REJECT.name())), State.OFFER_SENT,
				State.OFFER_REJECTED, null);
		this.description.addTransition(MessageType.TIMEOUT,
				new TimeoutCondition(10), State.OFFER_SENT, State.ERROR, null);

		// receiver side of protocol
		this.description.addState(State.OFFER_RECEIVED, StateType.END);
		/*
		 * Transition: START -> OFFER_RECEIVED. Take OFFER message and decide
		 * whether to accept or reject the offer and if we accept, whether to
		 * surrender a token.
		 */
		this.description.addTransition(MessageType.ACCEPT,
				new MessageTypeCondition(MessageType.OFFER.name()),
				State.START, State.OFFER_RECEIVED,
				new InitialiseConversationAction() {

					@Override
					public void processInitialMessage(Message<?> message,
							FSMConversation conv, Transition transition) {
						if (message.getData() instanceof Exchange) {
							EnvironmentConnector env = TokenExchangeProtocol.this.environment;
							Exchange exchange = ((Exchange) message.getData())
									.reverse();
							conv.setEntity(exchange);
							NetworkAddress from = conv.getNetwork()
									.getAddress();
							NetworkAddress to = message.getFrom();
							Time t = SimTime.get();
							if (acceptExchange(to, exchange)) {
								// send accept message
								logger.debug("Accepting exchange proposal: "
										+ exchange);
								conv.getNetwork().sendMessage(
										new UnicastMessage<Exchange>(
												Performative.ACCEPT_PROPOSAL,
												MessageType.ACCEPT.name(), t,
												from, to, exchange));
								// TODO optionally surrender appropriate token
								if (surrenderToken(to, exchange)) {
									try {
										env.act(new Surrender(exchange.mine),
												myId, authkey);
									} catch (ActionHandlingException e) {
										logger.warn("Error surrendering token",
												e);
									}
								}
							} else {
								// send reject message
								logger.debug("Rejecting exchange proposal: "
										+ exchange);
								conv.getNetwork().sendMessage(
										new UnicastMessage<Exchange>(
												Performative.REJECT_PROPOSAL,
												MessageType.REJECT.name(), t,
												from, to, exchange));
							}
						} else {
							// TODO error transition
						}
					}
				});
	}

	class ExchangeSpawnEvent extends ConversationSpawnEvent {

		final Exchange exchange;

		ExchangeSpawnEvent(NetworkAddress with, Colour c1, Colour c2) {
			super(with);
			this.exchange = new Exchange(c1, c2);
		}

	}

	public static class Exchange {
		final Colour mine;
		final Colour theirs;

		public Exchange(Colour mine, Colour theirs) {
			super();
			this.mine = mine;
			this.theirs = theirs;
		}

		public Exchange reverse() {
			return new Exchange(theirs, mine);
		}

		@Override
		public String toString() {
			return "Exchange [mine=" + mine + ", theirs=" + theirs + "]";
		}

		public Colour getMine() {
			return mine;
		}

		public Colour getTheirs() {
			return theirs;
		}

	}

	public void offer(NetworkAddress to, Colour c1, Colour c2)
			throws FSMException {
		this.spawnAsInititor(new ExchangeSpawnEvent(to, c1, c2));
	}

	protected abstract boolean acceptExchange(NetworkAddress from,
			Exchange exchange);

	protected boolean surrenderToken(NetworkAddress to, Exchange exchange) {
		return true;
	}
}
