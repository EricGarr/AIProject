package garr9903;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.PurchaseCosts;
import spacesettlers.actions.PurchaseTypes;
import spacesettlers.clients.ExampleKnowledge;
import spacesettlers.clients.ImmutableTeamInfo;
import spacesettlers.clients.TeamClient;
import spacesettlers.graphics.SpacewarGraphics;
import spacesettlers.objects.AbstractActionableObject;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourcePile;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Movement;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;

/**
 * Collects nearby asteroids and brings them to the base, picks up beacons as needed for energy.
 * 
 * If there is more than one ship, this version happily collects asteroids with as many ships as it
 * has.  it never shoots (it is a pacifist)
 * 
 * @author amy
 * modified by Eric Garrison and Francisco Pena
 */
public class PenaGarrisonAIClient extends TeamClient {
	//ship state tracker
	SingleShipState shipState;
	//current targets
	HashMap <UUID, Ship> targets;
	HashMap <UUID, Boolean> aimingForBase;	
	HashMap <UUID, Stack<Node>> movements;

	//nodes of the graph
	Set<Node> nodes;
	//farthest distance a node and "see" another node
	int maxNodeView = 150;
	//stack of moves returned by A*
	Stack<Node> moves;
	//how often can A* be re-run
	@SuppressWarnings("unused")
	private static int REMAP = 20;
	private static int REPLAN = 250;
	//when was the last A* run?
	@SuppressWarnings("unused")
	private int lastRun = 0;
	
	boolean buyShip = false;			//determines if a ship can be bought
	boolean buyBase = false;			//determines if a base can be bought 

	//genes
	int moveRate = 0;					//Ship speed.  0-4
	int sightRadius = 0;				//Maximum distance from the ship that an asteroid can be for collection.  0-15
	int newBaseDist = 0;				//Minimum distance for a new base from the old ones.  0-9
	int shipsToBase = 0;				//Do we keep the number of bases equal to the number of ships? 0-1
	int popMemberNum = -1;				//Which member of the population am I?
	
	PenaGarrisonPopulation population;
	
	Planner plan = null;

	/**
	 * Assigns ships to asteroids and beacons, as described above
	 */
	public Map<UUID, AbstractAction> getMovementStart(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, AbstractAction> actions = new HashMap<UUID, AbstractAction>();
		// loop through each ship
		for (AbstractObject actionable :  actionableObjects) {
			if (actionable instanceof Ship) {
				Ship ship = (Ship) actionable;
				shipState = new SingleShipState(ship);
				AbstractAction action;
				action = getAsteroidCollectorAction(space, ship);
				actions.put(ship.getId(), action);
			}
		}
		return actions;
	}
	
	/**
	 * Gets the action for the asteroid collecting ship
	 * @param space
	 * @param ship
	 * @param shipStates 
	 * @param currAsts 
	 * @param currBeacons 
	 * @param currBases 
	 * @return
	 */
	private AbstractAction getAsteroidCollectorAction(Toroidal2DPhysics space, Ship ship) {
		if((space.getCurrentTimestep() - lastRun) > REPLAN){
			lastRun = space.getCurrentTimestep();
			plan.replan(space);
		}
		
		try{
			AbstractAction current = ship.getCurrentAction();
			AbstractAction newAction = current;
			
			newAction = plan.getAction(space, ship.getId());
			
			if(newAction != null){
				return newAction;
			} else {
				return new DoNothingAction();
			}
		}catch (Exception e){
			return new DoNothingAction();
		}
	}
	
	/*
	 * max node view = 130
	 * value chosen because we are setting the nodes in a grid pattern where
	 * each grid is 100x80
	 * sqrt(100^2+80^2) = 128.0624
	 */
	@SuppressWarnings("unused")
	private Stack<Node> makeGraph(Position current, Position target, Toroidal2DPhysics space){
		//create the start node where ship is
		Node start = new Node(current);
		start.setH(space.findShortestDistance(current, target));
		//create the goal node where target is
		Node goal = new Node(target);
		goal.setH(0);
		
		//System.out.println("Creating obstructions");
		//create a list of all non-collectable objects
		Set<AbstractObject> obstructions = new HashSet<AbstractObject>();
		
		for(Base b : space.getBases()){
			if(!(b.getTeamName() == shipState.getTeamName()))
			obstructions.add(b);
		}
		for(Asteroid a : space.getAsteroids()){
			if(!a.isMineable()){
				obstructions.add(a);
			}
		}
		
		//System.out.println("creating graph");
		
		//create an adjacency list to store the graph
		HashMap<Node, HashSet<Node>> graph = new HashMap<Node, HashSet<Node>>();
		
		//store the start node
		graph.put(start, new HashSet<Node>());
		graph.put(goal, new HashSet<Node>());
		
		//check if the start and goal nodes are close and free of obstructions
		if((space.findShortestDistance(start.getLoc(), target) <= maxNodeView) &&
				(space.isPathClearOfObstructions(start.getLoc(), target, obstructions, Ship.SHIP_RADIUS))){
			//if so, add goal as a child of the start!
			graph.get(start).add(goal);
			graph.get(goal).add(start);
		}
		
		//fill the graph with the rest of the nodes
		for(Node curNode : nodes){
			//make sure that there are no hold-over values in the node
			curNode.wipe();
			//calculate the h(n) for each node in the node set
			curNode.setH(space.findShortestDistance(curNode.getLoc(), target));
			//add the node to the graph
			graph.put(curNode, new HashSet<Node>());
			
			//check the graph for connections with the new node
			for(Node nextNode : graph.keySet()){
				//to limit branching factor, make sure nodes are close
				//if the path is clear, add the path to the graph
				if(space.findShortestDistance(curNode.getLoc(), nextNode.getLoc()) <= maxNodeView && 
						space.isPathClearOfObstructions(curNode.getLoc(), nextNode.getLoc(),
								obstructions, Ship.SHIP_RADIUS)){
					//store the child nodes
					graph.get(curNode).add(nextNode);
					graph.get(nextNode).add(curNode);
				}
			}
		}
		
		//System.out.println("creating route");
		boolean routeCreated = false;
		//create the frontier
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		//add the start node to the frontier
		frontier.add(start);
		//intitialize a variable to store the current node being checked
		Node currentNode = null;
		/**/
		int nodesChecked = 0;
		/**/
		while(!routeCreated){
			//System.out.println(++nodesChecked);
			
			//move the best node from the frontier to the currently being checked node
			currentNode = frontier.poll();
			//mark the current node as visited
			currentNode.setVisited(true);
			//check if the goal is the node
			if(currentNode == goal){
				routeCreated = true;
				break;
			}
			//if the current node is null, break, no path is possible
			if(currentNode == null){
				routeCreated = true;
				break;
			}
			
			//System.out.println(graph.get(currentNode).size());
			//add the child nodes to the frontier
			for(Node nextNode : graph.get(currentNode)){
				//System.out.println("checking child");
				if(nextNode.getVisited() == true){
					//System.out.println("child already seen");
					//skip the node if it's already been visited
					continue;
				} else {
					//System.out.println("comparing child");
					//Node hasn't been expanded
					//calc this path's g(n) for the node
					double currentPathCost = currentNode.getG() +
							space.findShortestDistance(nextNode.getLoc(), goal.getLoc());
					/*System.out.println("current path: " + currentPathCost +
							"current child path cost" + nextNode.getG());*/
					if(currentPathCost >= nextNode.getG() && nextNode.getG() != 0){
						/*
						 * "nothing to see here, move along"
						 * We found an equal or better path elsewhere
						 * If it's equal elsewhere, then including it would duplicate paths
						*/
						continue;
					}
					//System.out.println("storing child");
					/*
					 * set the path cost to the node
					 * this also sets the total estimated cost
					 */
					nextNode.setCost(currentPathCost);
					//make sure the current Node is marked as a parent node
					nextNode.setParent(currentNode);
					//add the node to the frontier
					frontier.add(nextNode);
				}
			}
		}
		
		//create a stack to list the movements (LIFO)
		//System.out.println("storing route");
		Stack<Node> route = new Stack<Node>();
		while(currentNode.getParent() != null){
			//push the current (last) node onto the stack
			route.push(currentNode);
			//set the current node to the parent of the node that just entered the stack
			currentNode = currentNode.getParent();
		}
		//System.out.println("returning route");
		//current node should equal start at this point, but start isn't needed on the stack.
		return route;
	}
	
	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		ArrayList<Asteroid> finishedAsteroids = new ArrayList<Asteroid>();

		for (UUID asteroidId : targets.keySet()) {
			Asteroid asteroid = (Asteroid) space.getObjectById(asteroidId);
			if (asteroid == null || !asteroid.isAlive()) {
				finishedAsteroids.add(asteroid);
			}
		}
	}
	
	/**
	 * Demonstrates one way to read in knowledge from a file
	 */
	@Override
	public void initialize(Toroidal2DPhysics space) {
		targets = new HashMap<UUID, Ship>();
		makeNodes();
		moves = new Stack<Node>();
		aimingForBase = new HashMap<UUID, Boolean>();
		movements = new HashMap <UUID, Stack<Node>>();
		
		/* used to seed the initial population file
		 * after seeding, comment out and run ladder
		 * X times to get results for initial population
		 */
		//population = new PenaGarrisonPopulation();
		
		///* The following block should be commented for the first run through the ladder
		XStream xstream = new XStream();
		xstream.alias("PenaGarrisonPopulation", PenaGarrisonPopulation.class);
		try { 
			population = (PenaGarrisonPopulation) xstream.fromXML(new File(getKnowledgeFile()));
		} catch (XStreamException e) {
			// if you get an error, handle it other than a null pointer because
			// the error will happen the first time you run
			population = new PenaGarrisonPopulation();
		}
		/* end section*/
		
		//uncomment for ladder
		/*population.setCurrentPopMember(0);/**/
		//population.setGame(0);
		
		//get the move rate from the current population member
		moveRate = population.getPopulationInstance(population.getCurrentPopMember()).getMoveRate();
		//get the max sight around the ships from the current population member 
		sightRadius = population.getPopulationInstance(population.getCurrentPopMember()).getSightRadius();
		//get the minimum distance for a new base from the current population member
		newBaseDist = population.getPopulationInstance(population.getCurrentPopMember()).getNewBaseDist();
		//with the ships and bases be approx. equal for this population member
		shipsToBase = population.getPopulationInstance(population.getCurrentPopMember()).getshipsToBase();
		
		plan = new Planner(sightRadius, this.getTeamName(), moveRate * 10);
	}
	
	public static void touch(File file){
//		try {
//			if (!file.exists()){
//				new FileOutputStream(file).close();
//			}
//		} catch (Exception e) {
//			System.out.println(e);
//		}
	}

	/*
	 * create nodes
	 * Set<Node> nodes
	 */
	@SuppressWarnings("unused")
	private void makeNodes(){
		//make a set of the nodes
		nodes = new HashSet<Node>();
		for(int x = 0; x < 1600; x += 100){
			for(int y = 0; y < 1080; y+= 80){
				//create nodes in a 100x80 grid pattern
				Node node = new Node(new Position(x, y));
				nodes.add(node);
			}
		}
	}

	/**
	 * Demonstrates saving out to the xstream file
	 * You can save out other ways too.  This is a human-readable way to examine
	 * the knowledge you have learned.
	 * 
	 * Not being used yet.
	 */
	@Override
	public void shutDown(Toroidal2DPhysics space) {
//		double score = 0;
//		for(ImmutableTeamInfo t : space.getTeamInfo()){
//			if(t.getTeamName().equalsIgnoreCase(getTeamName())){
//				score = population.getScore(population.getCurrentPopMember()) + t.getScore();
//			}
//		}
//				
//		XStream xstream = new XStream();
//		xstream.alias("PenaGarrisonPopulation", PenaGarrisonPopulation.class);
//		
//		if(population.getGames()<2){
//			population.incrementGames();
//			population.storeFitness(population.getCurrentPopMember(), score);
//		}else {
//			population.storeFitness(population.getCurrentPopMember(), (score/3));
//			///*following section needed for learning.  Should not be enabled in ladder
//			population.incrementCurrentPopMember();
//			population.setGame(0);
//		}
//		
//		try { 
//			// if you want to compress the file, change FileOuputStream to a GZIPOutputStream
//			xstream.toXML(population, new FileOutputStream(new File(getKnowledgeFile())));
//		} catch (XStreamException e) {
//			// if you get an error, handle it somehow as it means your knowledge didn't save
//			// the error will happen the first time you run
//			population = new PenaGarrisonPopulation();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			population = new PenaGarrisonPopulation();
//		}
//		/*end section*/
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {/*
		HashSet<SpacewarGraphics> graphics = new HashSet<SpacewarGraphics>();
		graphics.addAll(graphicsToAdd);
		graphicsToAdd.clear();*/
		return null;
	}

	@Override
	/**
	 * If there is enough resourcesAvailable, buy a base.  Place it by finding a ship that is sufficiently
	 * far away from the existing bases
	 */
	public Map<UUID, PurchaseTypes> getTeamPurchases(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects, 
			ResourcePile resourcesAvailable, 
			PurchaseCosts purchaseCosts) {

		HashMap<UUID, PurchaseTypes> purchases = new HashMap<UUID, PurchaseTypes>();
		
		if(shipsToBase != 0){
			int numShips = 0;
			for(Ship ship : space.getShips()){
				if(ship.getTeamName().equalsIgnoreCase(getTeamName())){
					numShips++;
				}
			}
			int numBases = 0;
			for(Base base : space.getBases()){
				if(base.getTeamName().equalsIgnoreCase(getTeamName())){
					numBases++;
				}
			}
			if(numShips < (shipsToBase * numBases)){
				buyShip = true;
				buyBase = false;
			} else {
				buyBase = true;
				buyShip = false;
			}
		}
		
		//purchase a new base
		if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable) && buyBase) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;
					Set<Base> bases = space.getBases();

					// how far away is this ship to a base of my team?
					double maxDistance = newBaseDist * 100;
					for (Base base : bases) {
						if (base.getTeamName().equalsIgnoreCase(getTeamName())) {
							double distance = space.findShortestDistance(ship.getPosition(), base.getPosition());
							//check that base is far enough from ALL other bases
							if (distance < maxDistance) {
								buyBase = false;
							}
						}
					}

					if (buyBase) {
						purchases.put(ship.getId(), PurchaseTypes.BASE);
						break;
					}
				}
			}		
		} 
		
		// Buy a ship if possible
		if (purchaseCosts.canAfford(PurchaseTypes.SHIP, resourcesAvailable) && buyShip) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Base) {
					Base base = (Base) actionableObject;
					purchases.put(base.getId(), PurchaseTypes.SHIP);
					break;
				}

			}

		}

		return purchases;
	}

	/**
	 * @param space
	 * @param actionableObjects
	 * @return
	 */
	@Override
	public Map<UUID, SpaceSettlersPowerupEnum> getPowerups(Toroidal2DPhysics space,
			Set<AbstractActionableObject> actionableObjects) {
		HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();
		
		return powerUps;
	}
}