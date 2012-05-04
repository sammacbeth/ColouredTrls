package uk.ac.imperial.colrdtrls;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

import com.google.inject.Singleton;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.presage2.util.location.Cell;

@Singleton
public class UniformTokenDistribution implements TokenAllocator,
		TileColourGenerator {

	final Random rand;
	final int tokens;
	int[] allocation = new int[Colour.values().length];
	Colour[] colours = Colour.values();

	public UniformTokenDistribution(int randSeed, int tokens) {
		super();
		this.rand = new Random(randSeed);
		this.tokens = tokens;
		Arrays.fill(allocation, 0);
	}

	@Override
	public Colour getTileColour(Cell c) {
		int minAl = allocation[0];
		int minCol = 0;
		int maxAl = 0;
		for (int i = 0; i < allocation.length; i++) {
			if (allocation[i] > maxAl)
				maxAl = allocation[i];
			else if (allocation[i] < minAl) {
				minAl = allocation[i];
				minCol = i;
			}
		}
		if (maxAl - minAl > 2) {
			allocation[minCol]++;
			return colours[minCol];
		} else {
			int n = rand.nextInt(colours.length);
			allocation[n]++;
			return colours[n];
		}
	}

	@Override
	public int allocateTokens(UUID agentId, Colour c) {
		return tokens;
	}

}
