package uk.ac.imperial.colrdtrls.facts;

import java.util.List;

import uk.ac.imperial.presage2.core.util.random.Random;
import uk.ac.imperial.presage2.util.location.Cell;

public class Goal {

	Player player;
	Cell goal;
	int payoff;

	public Goal(Player player, Cell goal, int payoff) {
		super();
		this.player = player;
		this.goal = goal;
		this.payoff = payoff;
	}

	public static Goal randomGoal(Player p, int payoff, List<Tile> candidates) {
		return new Goal(p, candidates.get(Random.randomInt(candidates.size()))
				.getLocation(), payoff);
	}

	public Player getPlayer() {
		return player;
	}

	public Cell getGoal() {
		return goal;
	}

	public int getPayoff() {
		return payoff;
	}

	@Override
	public String toString() {
		return "Goal [player=" + player + ", goal=" + goal + ", payoff="
				+ payoff + "]";
	}

}
