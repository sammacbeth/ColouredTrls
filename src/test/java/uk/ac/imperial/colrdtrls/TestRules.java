package uk.ac.imperial.colrdtrls;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResults;
import org.drools.runtime.rule.QueryResultsRow;
import org.drools.runtime.rule.Variable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.colrdtrls.facts.Move;
import uk.ac.imperial.colrdtrls.facts.Owns;
import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.colrdtrls.facts.Surrender;
import uk.ac.imperial.colrdtrls.facts.Surrendered;
import uk.ac.imperial.colrdtrls.facts.Tile;
import uk.ac.imperial.colrdtrls.facts.Turn;
import uk.ac.imperial.colrdtrls.protocols.TokenExchangeProtocol.Exchange;
import uk.ac.imperial.presage2.core.IntegerTime;
import uk.ac.imperial.presage2.core.Time;
import uk.ac.imperial.presage2.core.messaging.Performative;
import uk.ac.imperial.presage2.core.network.Message;
import uk.ac.imperial.presage2.core.network.NetworkAddress;
import uk.ac.imperial.presage2.core.network.UnicastMessage;
import uk.ac.imperial.presage2.core.simulator.SimTime;
import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.rules.RuleModule;
import uk.ac.imperial.presage2.rules.RuleStorage;
import uk.ac.imperial.presage2.rules.facts.Agent;
import uk.ac.imperial.presage2.util.location.Cell;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestRules {

	final private Logger logger = Logger.getLogger(TestRules.class);

	Injector injector;
	RuleStorage rules;
	StatefulKnowledgeSession session;

	@Before
	public void setUp() throws Exception {
		injector = Guice.createInjector(new RuleModule()
				.addClasspathDrlFile("ColrdTrls.drl")
				.addClasspathDrlFile("MoveHandler.drl")
				.addClasspathDrlFile("Goals.drl")
				.addClasspathDrlFile("TokenExchange.drl"));
		rules = injector.getInstance(RuleStorage.class);
		session = injector.getInstance(StatefulKnowledgeSession.class);
		session.setGlobal("colrdtrlsLogger", logger);
		session.setGlobal("moveHandlerLogger", logger);
	}

	@After
	public void tearDown() throws Exception {
		session.dispose();
	}

	@Test
	public void testOwnsPredicate() {
		Agent a1 = new Agent(Random.randomUUID());
		Player p1 = new Player(a1, new Cell(0, 0));
		Colour c1 = Colour.BLUE;
		Colour c2 = Colour.GREEN;
		Owns o1 = new Owns(p1, c1, 1);
		Owns o2 = new Owns(p1, c2, 3);

		session.insert(c1);
		session.insert(c2);
		session.insert(a1);
		session.insert(p1);
		session.insert(o1);
		session.insert(o2);

		rules.incrementTime();

		QueryResults results = session.getQueryResults("owns", new Object[] {
				p1, Variable.v, Variable.v });
		assertEquals(2, results.size());
		for (QueryResultsRow r : results) {
			Colour c = (Colour) r.get("c");
			int i = (Integer) r.get("i");
			if (c == Colour.BLUE)
				assertEquals(i, 1);
			else if (c == Colour.GREEN)
				assertEquals(i, 3);
			else
				assertEquals(i, 0);
		}
	}

	@Test
	public void testMoveHandler() {
		// create tiles
		session.insert(new Tile(new Cell(0, 0), Colour.BLUE));
		session.insert(new Tile(new Cell(0, 1), Colour.GREEN));
		session.insert(new Tile(new Cell(1, 0), Colour.BLUE));
		session.insert(new Tile(new Cell(1, 1), Colour.PURPLE));

		Agent a1 = new Agent(Random.randomUUID());
		Player p1 = new Player(a1, new Cell(0, 0));

		session.insert(a1);
		session.insert(p1);

		session.insert(new Move(p1, new Cell(0, 0), new Cell(1, 0), 0));
		session.insert(new Move(p1, new Cell(0, 0), new Cell(2, 0), 0));

		rules.incrementTime();

		FactHandle turn = session.insert(new Turn(0));

		assertEquals(p1.getLocation(), new Cell(0, 0));

		rules.incrementTime();

		session.retract(turn);
		session.insert(new Move(p1, new Cell(1, 0), new Cell(0, 1), 1));

		assertEquals(p1.getLocation(), new Cell(1, 0));

		rules.incrementTime();

		session.insert(new Move(p1, new Cell(0, 0), new Cell(1, 0), 1));
		session.insert(new Move(p1, new Cell(1, 0), new Cell(1, 1), 1));
		session.insert(new Turn(1));

		rules.incrementTime();

		assertEquals(p1.getLocation(), new Cell(1, 1));
	}

	@Test
	public void testSurrender() {
		Agent a1 = new Agent(Random.randomUUID());
		Player p1 = new Player(a1, new Cell(0, 0));
		Colour c1 = Colour.BLUE;
		Colour c2 = Colour.GREEN;
		Owns o1 = new Owns(p1, c1, 1);
		Owns o2 = new Owns(p1, c2, 3);
		Surrendered s1 = new Surrendered(p1, c1, 0);
		Surrendered s2 = new Surrendered(p1, c2, 0);

		session.insert(a1);
		session.insert(p1);
		session.insert(c1);
		session.insert(c2);
		session.insert(o1);
		session.insert(o2);
		session.insert(s1);
		session.insert(s2);

		rules.incrementTime();

		assertEquals(1, o1.getCount());
		assertEquals(0, s1.getCount());
		assertEquals(3, o2.getCount());
		assertEquals(0, s2.getCount());

		session.insert(new Surrender(p1, c2));

		rules.incrementTime();

		assertEquals(1, o1.getCount());
		assertEquals(0, s1.getCount());
		assertEquals(2, o2.getCount());
		assertEquals(1, s2.getCount());

		session.insert(new Surrender(p1, c1));

		rules.incrementTime();

		assertEquals(0, o1.getCount());
		assertEquals(1, s1.getCount());
		assertEquals(2, o2.getCount());
		assertEquals(1, s2.getCount());

		session.insert(new Surrender(p1, c1));

		rules.incrementTime();

		assertEquals(0, o1.getCount());
		assertEquals(1, s1.getCount());
		assertEquals(2, o2.getCount());
		assertEquals(1, s2.getCount());
	}

	@Test
	public void testRetreiveSurrendered() {
		Agent a1 = new Agent(Random.randomUUID());
		Player p1 = new Player(a1, new Cell(0, 0));
		Colour c1 = Colour.BLUE;
		Colour c2 = Colour.GREEN;
		Owns o1 = new Owns(p1, c1, 1);
		Owns o2 = new Owns(p1, c2, 3);
		Surrendered s1 = new Surrendered(p1, c1, 0);
		Surrendered s2 = new Surrendered(p1, c2, 0);

		// create tiles
		session.insert(new Tile(new Cell(0, 0), Colour.BLUE));
		session.insert(new Tile(new Cell(0, 1), Colour.GREEN));
		session.insert(new Tile(new Cell(1, 0), Colour.BLUE));
		session.insert(new Tile(new Cell(1, 1), Colour.BLUE));

		session.insert(a1);
		session.insert(p1);
		session.insert(c1);
		session.insert(c2);
		session.insert(o1);
		session.insert(o2);
		session.insert(s1);
		session.insert(s2);

		rules.incrementTime();

		// initial owned & surrendered
		assertEquals(1, o1.getCount());
		assertEquals(0, s1.getCount());
		assertEquals(3, o2.getCount());
		assertEquals(0, s2.getCount());

		// move to blue tile + surrender
		session.insert(new Move(p1, new Cell(0, 0), new Cell(1, 0), 0));
		session.insert(new Surrender(p1, c1));
		FactHandle turn = session.insert(new Turn(0));

		rules.incrementTime();

		assertEquals(0, o1.getCount());
		assertEquals(0, s1.getCount());
		assertEquals(3, o2.getCount());
		assertEquals(0, s2.getCount());

		session.retract(turn);

		// move to green tile + surrender 2
		session.insert(new Move(p1, new Cell(1, 0), new Cell(0, 1), 1));
		session.insert(new Surrender(p1, c2));
		session.insert(new Surrender(p1, c2));
		turn = session.insert(new Turn(1));

		rules.incrementTime();

		assertEquals(0, o1.getCount());
		assertEquals(0, s1.getCount());
		assertEquals(1, o2.getCount());
		assertEquals(1, s2.getCount());

		session.retract(turn);

		// move to blue without surrender
		session.insert(new Move(p1, new Cell(0, 1), new Cell(1, 1), 2));
		turn = session.insert(new Turn(2));

		rules.incrementTime();

		assertEquals(0, o1.getCount());
		assertEquals(0, s1.getCount());
		assertEquals(1, o2.getCount());
		assertEquals(1, s2.getCount());
	}

	@Test
	public void testTokenExchange() {
		Agent a1 = new Agent(Random.randomUUID());
		Player p1 = new Player(a1, new Cell(0, 0));
		NetworkAddress n1 = new NetworkAddress(a1.getAid());
		assertEquals(a1.getAid(), n1.getId());
		Agent a2 = new Agent(Random.randomUUID());
		Player p2 = new Player(a2, new Cell(0, 1));
		NetworkAddress n2 = new NetworkAddress(a2.getAid());
		assertEquals(a2.getAid(), n2.getId());

		Colour c1 = Colour.BLUE;
		Colour c2 = Colour.GREEN;

		Owns p1o1 = new Owns(p1, c1, 0);
		Owns p1o2 = new Owns(p1, c2, 3);
		Surrendered p1s1 = new Surrendered(p1, c1, 0);
		Surrendered p1s2 = new Surrendered(p1, c2, 0);

		Owns p2o1 = new Owns(p2, c1, 3);
		Owns p2o2 = new Owns(p2, c2, 0);
		Surrendered p2s1 = new Surrendered(p2, c1, 0);
		Surrendered p2s2 = new Surrendered(p2, c2, 0);

		session.insert(a1);
		session.insert(p1);
		session.insert(a2);
		session.insert(p2);
		session.insert(c1);
		session.insert(c2);
		session.insert(p1o1);
		session.insert(p1o2);
		session.insert(p1s1);
		session.insert(p1s2);
		session.insert(p2o1);
		session.insert(p2o2);
		session.insert(p2s1);
		session.insert(p2s2);

		rules.incrementTime();

		Time t = new IntegerTime(0);
		new SimTime(t);
		UUID convKey = Random.randomUUID();
		Exchange ex = new Exchange(c2, c1);
		Message<Exchange> m1 = new UnicastMessage<Exchange>(
				Performative.PROPOSE, "OFFER", SimTime.get(), n1, n2, ex);
		m1.setConversationKey(convKey);
		session.insert(m1);

		rules.incrementTime();
		t.increment();

		assertEquals(0, p1o1.getCount());
		assertEquals(3, p1o2.getCount());
		assertEquals(0, p1s1.getCount());
		assertEquals(0, p1s2.getCount());
		assertEquals(3, p2o1.getCount());
		assertEquals(0, p2o2.getCount());
		assertEquals(0, p2s1.getCount());
		assertEquals(0, p2s2.getCount());

		Message<Exchange> m2 = new UnicastMessage<Exchange>(
				Performative.ACCEPT_PROPOSAL, "ACCEPT", t, n2, n1, ex.reverse());
		m2.setConversationKey(convKey);
		session.insert(m2);
		session.insert(new Surrender(p2, c1));

		rules.incrementTime();
		t.increment();

		session.insert(new Surrender(p1, c2));
		rules.incrementTime();
		t.increment();

		assertEquals(1, p1o1.getCount());
		assertEquals(2, p1o2.getCount());
		assertEquals(0, p1s1.getCount());
		assertEquals(0, p1s2.getCount());
		assertEquals(2, p2o1.getCount());
		assertEquals(1, p2o2.getCount());
		assertEquals(0, p2s1.getCount());
		assertEquals(0, p2s2.getCount());

	}

}
