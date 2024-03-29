package uk.ac.imperial.colrdtrls;

import java.util.Random;
import java.util.UUID;

import com.google.inject.Singleton;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.presage2.util.location.Cell;

@Singleton
public class RandomTokenDistribution implements TileColourGenerator,
		TokenAllocator {

	final Random rand;
	final int maxTokens;

	public RandomTokenDistribution(int randSeed, int maxTokens) {
		super();
		this.rand = new Random(randSeed);
		this.maxTokens = maxTokens;
	}

	@Override
	public int allocateTokens(UUID agentId, Colour c) {
		return rand.nextInt(maxTokens);
	}

	@Override
	public Colour getTileColour(Cell c) {
		Colour col;
		switch (rand.nextInt(Colour.values().length)) {
		case 4:
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
		return col;
	}

}
