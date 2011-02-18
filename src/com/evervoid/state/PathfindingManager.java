package com.evervoid.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.evervoid.state.geometry.Dimension;
import com.evervoid.state.geometry.GridLocation;
import com.evervoid.state.geometry.Point;
import com.evervoid.state.prop.Ship;

public class PathfindingManager
{
	/**
	 * Data structure used for the A* pathfinding algorithm.
	 */
	class PathNode implements Comparable<PathNode>{
		public int costSoFar; //Equivalent to g
		public int goalHeuristic; //Equivalent to h
		public int totalCost; //Equivalent to f (f = g + h)
		private final Point fCoord;
		public PathNode parent;
		
		/**
		 * Constructor used to initialise nodes without path knowledge
		 * has a null parent and 0 costSoFar.
		 * @param pCoord Point associated with this node's coordinates.
		 */
		public PathNode(Point pCoord){
			this(null, 0, pCoord);
		}
		
		/**
		 * Default PathNode constructor.
		 * @param pParent Parent of this PathNode.
		 * @param pCostSoFar Cost to reach this node from the origin.
		 * @param pCoord Point associated with this node's coordinates.
		 */
		public PathNode(PathNode pParent, int pCostSoFar, Point pCoord){
			costSoFar = pCostSoFar;
			goalHeuristic = 0;
			parent = pParent;
			fCoord = pCoord;
		}
		
		/**
		 * Returns the coordinates associated with this PathNode.
		 * @return The Point representing the coordinates of this PathNode.
		 */
		public Point getCoord(){
			return fCoord;
		}

		@Override
		public int compareTo(PathNode o)
		{
			if ((o.costSoFar + o.goalHeuristic) < (costSoFar + goalHeuristic))
				return -1;
			else if ((o.costSoFar + o.goalHeuristic) == (costSoFar + goalHeuristic))
				return 0;
			else
				return 1;
		}
		
		@Override
		public boolean equals(final Object other)
		{
			if (super.equals(other)) {
				return true;
			}
			if (other == null) {
				return false;
			}
			if (!other.getClass().equals(getClass())) {
				return false;
			}
			final PathNode l = (PathNode) other;
			return fCoord.equals(l.getCoord());
		}
	}
	
	
	private ArrayList<PathNode> closed = new ArrayList<PathNode>();
	private PriorityQueue<PathNode> open = new PriorityQueue<PathNode>();
	
	public PathfindingManager()
	{
	}

	private Set<Point> getNeighbours(final SolarSystem pSolarSystem, final Point p)
	{
		final Set<Point> directNeighbours = new HashSet<Point>();
		
		final Point east = new Point(p.x + 1, p.y);
		final Point west = new Point(p.x - 1, p.y);
		final Point north = new Point(p.x, p.y + 1);
		final Point south = new Point(p.x, p.y - 1);
		
		if (p.y - 1 > 0)
			directNeighbours.add(south);
		if (p.y + 1 < pSolarSystem.getHeight())
			directNeighbours.add(north);
		if (p.x - 1 > 0)
			directNeighbours.add(west);
		if (p.x + 1 < pSolarSystem.getWidth())
			directNeighbours.add(east);
		return directNeighbours;
	}

	/**
	 * Returns a list of points where a ship can move to.
	 * 
	 * @param pShip
	 *            A ship located in a solarSystem.
	 * @param pSolarSystem
	 *            The solar system containing the ship.
	 * @return A list of points where the specified ship can move within the solar system.
	 */
	public Set<Point> getValidDestinations(final SolarSystem pSolarSystem, final Ship pShip)
	{
		final Point shipOrigin = pShip.getLocation().origin;
		final Dimension shipDimension = pShip.getLocation().dimension;
		final Set<Point> graphFrontier = new HashSet<Point>();
		final Set<Point> newFrontier = new HashSet<Point>();
		final Set<Point> validDestinations = new HashSet<Point>();
		graphFrontier.addAll(getNeighbours(pSolarSystem,shipOrigin));
		// Implementation of a limited-depth breadth-first search.
		for (int i = 0; i < pShip.getSpeed(); i++) {
			// Traverse all the points contained in the frontier.
			for (final Point p : graphFrontier) {
				if (!pSolarSystem.isOccupied(new GridLocation(p, shipDimension))) {
					// Point is not occupied nor already known as valid.
					validDestinations.add(p);
					// Add the neighbours to the new frontier.
					newFrontier.addAll(getNeighbours(pSolarSystem,p));
				}
			}
			/*
			 * Remove all already known points from the new frontier, clear the old frontier and replace with new frontier.
			 */
			newFrontier.removeAll(validDestinations);
			graphFrontier.clear();
			graphFrontier.addAll(newFrontier);
			newFrontier.clear();
		}
		validDestinations.remove(shipOrigin);
		return validDestinations;
	}
	
	public List<GridLocation> findPath(final SolarSystem pSolarSystem, final Ship pShip, Point pDestination){
		/* Create an internal representation of the grid.
		 *  TODO: Make it so we only consider the square reachable by the ship's speed. (offset) 
		 */
		PathNode[][] nodes = new PathNode[pSolarSystem.getWidth()][pSolarSystem.getHeight()];
		for (int i = 0; i < pSolarSystem.getWidth(); i++){
			for (int j = 0; j < pSolarSystem.getHeight(); j++){
				nodes[i][j] = new PathNode(new Point(i,j));
			}
		}
		
		final Point shipOrigin = pShip.getLocation().origin;
		final Dimension shipDimension = pShip.getLocation().dimension;
		PathNode originNode = nodes[shipOrigin.x][shipOrigin.y];
		originNode.costSoFar = 0;
		originNode.goalHeuristic = 0;
		int tentativeCostSoFar = 0;
		boolean tentativeIsBetter = false;
		
		open.clear();
		closed.clear();
		
		open.add(originNode);
		
		int maxDepth = 0;
		PathNode current = null, neighbour = null;
		while((maxDepth < pShip.getSpeed()) && (open.size() != 0)){
			//Get the first element from the list.
			current = open.peek();
			
			if (current.getCoord().equals(pDestination)){
				ArrayList<PathNode> tempResults = reconstructPath(current);
				ArrayList<GridLocation> finalResults = new ArrayList<GridLocation>();
				for (PathNode r : tempResults){
					finalResults.add(new GridLocation(r.getCoord().x, r.getCoord().y, shipDimension));
				}
				return finalResults;
			}
			open.poll();
			closed.add(current);
			
			for (Point p : getNeighbours(pSolarSystem,current.getCoord())){
				neighbour = nodes[p.x][p.y];
				
				if (closed.contains(neighbour)){
					continue;
				}
				tentativeCostSoFar = current.costSoFar + 1;
				
				if (!open.contains(neighbour)){
					open.add(neighbour);
					tentativeIsBetter = true;
				}
				else if (tentativeCostSoFar < neighbour.costSoFar){
					tentativeIsBetter = true;
				}
				else{
					tentativeIsBetter = false;
				}
				
				if (tentativeIsBetter){
					neighbour.parent = current;
					
					neighbour.costSoFar = tentativeCostSoFar;
					neighbour.goalHeuristic =  computeHeuristic(neighbour.getCoord(), pDestination);
					neighbour.costSoFar = neighbour.costSoFar + neighbour.goalHeuristic;
				}			
			}
			
		}
		
		return null;
		
	}
	
	private int computeHeuristic(Point pOrig, Point pDest){
		//Straight line distance
		return (int) Math.abs(Math.floor((pDest.y - pOrig.y)/(pDest.x - pOrig.x)));
	}
	
	private ArrayList<PathNode> reconstructPath(PathNode pCurrentNode){
		if (pCurrentNode.parent != null){
			ArrayList<PathNode> p = reconstructPath(pCurrentNode.parent);
			p.add(pCurrentNode);
			return p;
		}
		else{
			ArrayList<PathNode> path = new ArrayList<PathNode>();
			path.add(pCurrentNode);
			return path;
		}
	}
}
