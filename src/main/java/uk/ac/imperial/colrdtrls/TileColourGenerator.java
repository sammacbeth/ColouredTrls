package uk.ac.imperial.colrdtrls;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.presage2.util.location.Cell;

public interface TileColourGenerator {

	Colour getTileColour(Cell c);

}
