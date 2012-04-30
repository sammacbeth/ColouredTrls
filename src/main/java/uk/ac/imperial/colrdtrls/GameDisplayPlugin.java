package uk.ac.imperial.colrdtrls;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.colrdtrls.facts.Colour;
import uk.ac.imperial.presage2.core.environment.EnvironmentServiceProvider;
import uk.ac.imperial.presage2.core.environment.UnavailableServiceException;
import uk.ac.imperial.presage2.core.plugin.Plugin;
import uk.ac.imperial.presage2.util.environment.EnvironmentMembersService;
import uk.ac.imperial.presage2.util.location.Cell;
import uk.ac.imperial.presage2.util.location.area.AreaService;

import com.google.inject.Inject;

public class GameDisplayPlugin implements Plugin {

	private Logger logger = Logger.getLogger(GameDisplayPlugin.class);
	final TileColourService tileService;
	final AreaService areaService;
	final KnowledgeBaseService knowledge;
	final EnvironmentMembersService membersService;
	Map<UUID, Character> assignedNames = new HashMap<UUID, Character>();
	char nameCtr = 'a';

	@Inject
	public GameDisplayPlugin(EnvironmentServiceProvider serviceProvider)
			throws UnavailableServiceException {
		this.tileService = serviceProvider
				.getEnvironmentService(TileColourService.class);
		this.areaService = serviceProvider
				.getEnvironmentService(AreaService.class);
		this.knowledge = serviceProvider
				.getEnvironmentService(KnowledgeBaseService.class);
		this.membersService = serviceProvider
				.getEnvironmentService(EnvironmentMembersService.class);
	}

	@Override
	public void incrementTime() {
		final int xSize = areaService.getSizeX();
		final int ySize = areaService.getSizeY();
		int x = 0, y = 0;

		char[][][] playerLocs = new char[xSize][ySize][4];
		for (x = 0; x < xSize; x++) {
			for (y = 0; y < ySize; y++) {
				for (int n = 0; n < 4; n++) {
					playerLocs[x][y][n] = ' ';
				}
			}
		}
		for (UUID pid : this.membersService.getParticipants()) {
			if (!assignedNames.containsKey(pid)) {
				assignedNames.put(pid, nameCtr++);
			}
			Cell loc = this.knowledge.getPlayer(pid).getLocation();
			int n = 0;
			do {
				if (playerLocs[(int) loc.getX()][(int) loc.getY()][n] == ' ') {
					playerLocs[(int) loc.getX()][(int) loc.getY()][n] = assignedNames
							.get(pid);
					break;
				}
				n++;
			} while (n < 4);
		}
		List<String> lines = new LinkedList<String>();

		final int gridXSize = (xSize * 3) + 1;
		final int gridYSize = (ySize * 5) + 1;
		char[][] charGrid = new char[gridXSize][gridYSize];
		x = 0;
		y = 0;

		for (y = 0; y < gridYSize; y++) {
			if (y % 5 == 0)
				charGrid[x][y] = ' ';
			else
				charGrid[x][y] = '_';
		}
		for (x = 0; x < xSize; x++) {
			for (y = 0; y < ySize; y++) {
				int xBase = 1 + (x * 3);
				int yBase = y * 5;
				charGrid[xBase][yBase] = '|';
				charGrid[xBase][yBase + 1] = getTileColour(x, y);
				charGrid[xBase][yBase + 2] = ' ';
				charGrid[xBase][yBase + 3] = ' ';
				charGrid[xBase][yBase + 4] = ' ';

				charGrid[xBase + 1][yBase] = '|';
				char[] players = playerLocs[x][y];
				for (int i = 0; i < 4; i++) {
					if (i < players.length)
						charGrid[xBase + 1][yBase + 1 + i] = players[i];
					else
						charGrid[xBase + 1][yBase + 1 + i] = ' ';
				}

				charGrid[xBase + 2][yBase] = '|';
				charGrid[xBase + 2][yBase + 1] = '_';
				charGrid[xBase + 2][yBase + 2] = '_';
				charGrid[xBase + 2][yBase + 3] = '_';
				charGrid[xBase + 2][yBase + 4] = '_';

				if (y == ySize - 1) {
					charGrid[xBase][yBase + 5] = '|';
					charGrid[xBase + 1][yBase + 5] = '|';
					charGrid[xBase + 2][yBase + 5] = '|';
				}
			}
		}
		for (x = 0; x < gridXSize; x++) {
			StringBuilder b = new StringBuilder();
			for (y = 0; y < gridYSize; y++) {
				b.append(charGrid[x][y]);
			}
			lines.add(b.toString());
		}

		for (String string : lines) {
			logger.info(string);
		}
	}

	private char getTileColour(int x, int y) {
		Colour c = tileService.getTileColour(x, y);
		switch (c) {
		case BLUE:
			return 'B';
		case GREEN:
			return 'G';
		case PURPLE:
			return 'P';
		case RED:
			return 'R';
		case YELLOW:
			return 'Y';
		}
		return ' ';
	}

	@Override
	public void initialise() {
	}

	@Override
	public void execute() {
	}

	@Override
	public void onSimulationComplete() {
	}

}
