package uk.ac.imperial.colrdtrls.planning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;

import uk.ac.imperial.colrdtrls.KnowledgeBaseService;
import uk.ac.imperial.colrdtrls.facts.Move;
import uk.ac.imperial.colrdtrls.facts.Player;
import uk.ac.imperial.presage2.util.location.Cell;

public class PathPlanner {

	private final Logger logger; 
	final private UUID player;
	final private KnowledgeBaseService knowledge;
	Set<HardConstraint> hardConstraints = new HashSet<HardConstraint>();
	Set<SoftConstraint> softConstraints = new HashSet<SoftConstraint>();

	public PathPlanner(UUID player, KnowledgeBaseService knowledge,
			Set<HardConstraint> hardConstraints,
			Set<SoftConstraint> softConstraints) {
		super();
		this.player = player;
		this.knowledge = knowledge;
		this.hardConstraints = hardConstraints;
		this.softConstraints = softConstraints;
		logger = Logger.getLogger("PathPlanner, "+ player);
	}

	public Move getNextMove() {
		Path bestPath = search();
		if (bestPath != null) {
			return new Move(bestPath.path.getFirst(), bestPath.path.get(1));
		}
		return null;
	}

	Path search() {
		Player p = knowledge.getPlayer(player);
		Cell start = p.getLocation();
		Cell goal = knowledge.getGoal(player).getGoal();

		if (start.equals(goal)) {
			// nothing to do
		} else {
			logger.debug("Starting search from "+ start +" to "+ goal);
			ArrayList<Path> paths = new ArrayList<Path>();
			paths.add(new Path(start, 0));

			for(int i=0; i < 100; i++) {
				boolean updated = false;
				for (Path path : paths) {
					if (path.finishesAt(goal)) {
						return path;
					} else if (!path.isExpanded()) {
						paths.addAll(expandNode(path, goal));
						updated = true;
						break;
					}
				}
				if (!updated) {
					return null;
				}
				Collections.sort(paths);
			}
			// max expansions exhausted.
			logger.warn("Maximum expansions exhausted, "+ start +" -> "+ goal +", "+ paths.size() +" paths.");
		}
		return null;
	}

	private List<Path> expandNode(final Path currentPath, final Cell goal) {
		List<Path> newPaths = new LinkedList<Path>();
		final Cell node;
		if (currentPath.path.size() == 0)
			node = knowledge.getPlayer(player).getLocation();
		else
			node = currentPath.path.getLast();
		int currentX = (int) node.getX();
		int currentY = (int) node.getY();

		for (int x = currentX - 1; x <= currentX + 1; x++) {
			for (int y = currentY - 1; y <= currentY + 1; y++) {
				try {
					// generate a new path
					LinkedList<Cell> newPath = new LinkedList<Cell>(
							currentPath.path);
					newPath.add(new Cell(x, y));
					// check hard constraints
					boolean hardViolation = false;
					for (HardConstraint constraint : hardConstraints) {
						if (constraint.isViolated(newPath)) {
							hardViolation = true;
							break;
						}
					}
					if (hardViolation)
						continue;
					// path is allowed, calculate fCost from soft
					// constraints
					double fCost = 0;
					for (SoftConstraint constraint : softConstraints) {
						fCost += constraint.pathCost(newPath);
						fCost += constraint.estimatedCostTo(newPath, goal);
					}
					newPaths.add(new Path(newPath, fCost));
				} catch (IndexOutOfBoundsException e) {
					continue;
				}
			}
		}
		currentPath.setExpanded(true);
		return newPaths;
	}

}
