package uk.ac.imperial.colrdtrls;

import java.util.UUID;

import uk.ac.imperial.colrdtrls.facts.Colour;

public interface TokenAllocator {

	int allocateTokens(UUID agentId, Colour c);

}
