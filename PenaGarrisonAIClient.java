package garr9903;

/*
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import spacesettlers.actions.MoveToObjectAction;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
*/
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
	SingleShipState myShip;
	//current targets
	HashMap <UUID, Ship> targets;
	HashMap <UUID, Boolean> aimingForBase;
	
	//nodes of the graph
	Set<Node> nodes;
	//furthest distance a node and "see" another node
	int maxNodeView = 130;
	//stack of moves returned by A*
	Stack<Node> moves;
	//how often can A* be re-run
	private static int REMAP = 20;
	//when was the last A* run?
	private int lastRun = 0;
	//buy ships before bases
	boolean bought_ship = true;
	//left over artifact
	//Set<GridSquare> grid;
	
	/**
	 * Example knowledge used to show how to load in/save out to files for learning
	 */
	ExampleKnowledge myKnowledge;

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
				myShip = new SingleShipState(ship);
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
	 * @return
	 */
	private AbstractAction getAsteroidCollectorAction(Toroidal2DPhysics space, Ship ship) {
		try{
			AbstractAction current = ship.getCurrentAction();
			Position currentPosition = ship.getPosition();
			AbstractAction newAction = current;
			myShip = new SingleShipState(ship);
			AbstractObject target = null;
			//makeGraph(Position current, Position target, Toroidal2DPhysics space)
			if(space.getCurrentTimestep() - lastRun >= REMAP){
				lastRun = space.getCurrentTimestep();
				System.out.println(myShip.getState());
				// aim for a beacon if there isn't enough energy
				if (myShip.getState() == SingleShipState.Target.ENERGY) {
					target = getNearestBeacon(space, ship);
					aimingForBase.put(ship.getId(), false);
				}
				
				// if the ship has enough resourcesAvailable or time is about up: take them back to base
				//
				if (myShip.getState() == SingleShipState.Target.BASE || space.getCurrentTimestep() >= 19900) {
					//choose a base
					target = getNearestBase(space, ship);
					aimingForBase.put(ship.getId(), true);
				}
				
				//did I turn in my resources?
				if (ship.getResources().getTotal() == 0 && aimingForBase.containsKey(ship.getId()) && aimingForBase.get(ship.getId())){
					current = null;
					aimingForBase.put(ship.getId(), false);
				}
				
				//if nothing else is needed, get an asteroid
				if (target == null || current == null) {
					target = getBestAsteroid(space, ship);
					targets.put(target.getId(), ship);
					aimingForBase.put(ship.getId(), false);
				}
				System.out.println("beginning A*");
				moves = makeGraph(ship.getPosition(), target.getPosition(), space);
			}
			if(!moves.isEmpty()){
				newAction = calcMove(space, currentPosition, moves.pop().getLoc());
			}
			
			if(newAction != null){
				return newAction;
			} else {
				return new DoNothingAction();
			}
		}catch (Exception e){
			return new DoNothingAction();
		}
	}
	
	//finds the asteroid with the best ratio of resources/distance
	Asteroid getBestAsteroid(Toroidal2DPhysics space, Ship ship){
		//get the list of asteroids
		Set<Asteroid> asteroids = space.getAsteroids();
		double test = Double.MIN_VALUE;
		//best asteroid found so far
		Asteroid best = null;
		for(Asteroid ast : asteroids){
			//calculate the ratio of resources to distance for each asteriod
			if(ast.getResources().getTotal() / space.findShortestDistance(ship.getPosition(), ast.getPosition()) > test){
				//if a better asteroid is found, store it, and it's ratio
				best = ast;
				test = ast.getResources().getTotal() / space.findShortestDistance(ship.getPosition(), ast.getPosition());
			}
		}
		//return the best asteroid found
		return best;
	}
	
	//ship is low on energy, find the best source
	AbstractObject getNearestBeacon(Toroidal2DPhysics space, Ship ship){
		//get the list of current beacons
		Set<Beacon> beacons = space.getBeacons();
		double closestBeacon = Double.MAX_VALUE;
		//best beacon found so far
		Beacon bestBeacon = null;
		for(Beacon beacon : beacons){
			//find the distance to the beacon
			double dist = space.findShortestDistance(beacon.getPosition(), ship.getPosition());
			if(dist < closestBeacon){
				//if the beacon is closer, store it and its distance
				bestBeacon = beacon;
				closestBeacon = dist;
			}
		}
		//get the list of bases
		Set<Base> bases = space.getBases();
		double closestBase = Double.MAX_VALUE;
		//initialize the best base
		Base bestBase = null;
		for(Base base : bases){
			//check if base has enough energy and is ours
			if(base.getTeamName() == this.getTeamName() && base.getEnergy() > 1000){
				//check base distance
				double dist = space.findShortestDistance(base.getPosition(), ship.getPosition());
				if(dist < closestBase){
					//if better, store
					bestBase = base;
					closestBase = dist;
				}
			}
		}
		//return the closest energy option
		if(closestBase > closestBeacon){
			return bestBeacon;
		} else {
			return bestBase;
		}
		//return bestBeacon;
	}
	
	//ships hull is full enough, find the nearest base to turn resources in
	Base getNearestBase(Toroidal2DPhysics space, Ship ship){
		//get the list of bases
		Set<Base> bases = space.getBases();
		double nearest = Double.MAX_VALUE;
		//initialize the current best base found
		Base best = null;
		for (Base base : bases){
			//check if base is ours, and if the path is clear
			if(base.getTeamName() == ship.getTeamName() && space.isPathClearOfObstructions(ship.getPosition(), base.getPosition(), space.getAllObjects(), ship.getRadius())){
				//find the distance to the base
				double dist = space.findShortestDistance(ship.getPosition(), base.getPosition());
				if(dist < nearest){
					//if distance is better, store the base and the new distance
					best = base;
					nearest = dist; 
				}
			}
		}
		//same as above, but doesn't care if the path is clear
		if(best == null){
			for (Base base : bases){
				if(base.getTeamName() == ship.getTeamName()){
					double dist = space.findShortestDistance(ship.getPosition(), base.getPosition());
					if(dist < nearest){
						best = base;
						nearest = dist; 
					}
				}
			}
		}
		//return the target base
		return best;
	}
	
	AbstractAction calcMove(Toroidal2DPhysics space, Position current, Position target){
		//get the unit vector to the target
		Vector2D vect = space.findShortestDistanceVector(current, target).getUnitVector();
		//set the unit vector to the fastest speed
		vect.setX(vect.getXValue()*Movement.MAX_TRANSLATIONAL_ACCELERATION);
		vect.setY(vect.getYValue()*Movement.MAX_TRANSLATIONAL_ACCELERATION);
		//move to the target
		return new MoveAction(space, current, target, vect);
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

		for (Asteroid asteroid : finishedAsteroids) {
			targets.remove(asteroid);
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
		
		/*XStream xstream = new XStream();
		xstream.alias("ExampleKnowledge", ExampleKnowledge.class);
		try { 
			myKnowledge = (ExampleKnowledge) xstream.fromXML(new File(getKnowledgeFile()));
		} catch (XStreamException e) {
			// if you get an error, handle it other than a null pointer because
			// the error will happen the first time you run
			myKnowledge = new ExampleKnowledge();
		}*/
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
		/*XStream xstream = new XStream();
		xstream.alias("ExampleKnowledge", ExampleKnowledge.class);

		try { 
			// if you want to compress the file, change FileOuputStream to a GZIPOutputStream
			xstream.toXML(myKnowledge, new FileOutputStream(new File(getKnowledgeFile())));
		} catch (XStreamException e) {
			// if you get an error, handle it somehow as it means your knowledge didn't save
			// the error will happen the first time you run
			myKnowledge = new ExampleKnowledge();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			myKnowledge = new ExampleKnowledge();
		}*/
	}

	@Override
	public Set<SpacewarGraphics> getGraphics() {
		// TODO Auto-generated method stub
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
		double BASE_BUYING_DISTANCE = 200;  //minimum distance from other bases
		boolean buyBase = true;
		
		//purchase an energy doubler if possible
		if(purchaseCosts.canAfford(PurchaseTypes.POWERUP_DOUBLE_MAX_ENERGY, resourcesAvailable)){
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;
					purchases.put(ship.getId(), PurchaseTypes.POWERUP_DOUBLE_MAX_ENERGY);
					//give energy doubler to ship
					ship.addPowerup(SpaceSettlersPowerupEnum.DOUBLE_MAX_ENERGY);
				}
			}
		}
		
		//purchase a new base
		if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable) && bought_ship) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;
					Set<Base> bases = space.getBases();

					// how far away is this ship to a base of my team?
					double maxDistance = BASE_BUYING_DISTANCE;
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
					} else {
						buyBase = true;
					}
				}
			}		
		} 
		
		// Buy a ship if possible
		if (purchaseCosts.canAfford(PurchaseTypes.SHIP, resourcesAvailable)) {
			bought_ship = true;
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
		
		//use double energy powerup if possible.
		Ship ship = (Ship) space.getObjectById(myShip.getUUID());
		
		if(ship.getCurrentPowerups().contains(SpaceSettlersPowerupEnum.DOUBLE_MAX_ENERGY)){
			powerUps.put(ship.getId(), SpaceSettlersPowerupEnum.DOUBLE_MAX_ENERGY);
		}
		
		return powerUps;
	}
	
	/*
	 * create nodes
	 * Set<Node> nodes
	 */
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
		
		//create a list of all non-collectable objects
		Set<AbstractObject> obstructions = new HashSet<AbstractObject>();
		for(Ship s : space.getShips()){
			if(!(s.getId().equals(myShip.getUUID()))){
				obstructions.add(s);
			}
		}
		for(Base b : space.getBases()){
			obstructions.add(b);
		}
		for(Asteroid a : space.getAsteroids()){
			if(!a.isMineable()){
				obstructions.add(a);
			}
		}
		
		//create an adjacency list to store the graph
		HashMap<Node, HashSet<Node>> graph = new HashMap<Node, HashSet<Node>>();
		
		//store the start and goal nodes
		graph.put(start, new HashSet<Node>());
		graph.put(goal, new HashSet<Node>());
		
		//check if the start and goal nodes are close and free of obstructions
		if((space.findShortestDistance(start.getLoc(), target) <= maxNodeView) &&
				(space.isPathClearOfObstructions(start.getLoc(), target, obstructions, Ship.SHIP_RADIUS))){
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
					graph.get(curNode).add(nextNode);
					graph.get(nextNode).add(curNode);
				}
			}
		}
		
		System.out.println("creating route");
		
		boolean routeCreated = false;
		//create the frontier
		PriorityQueue<Node> frontier = new PriorityQueue<Node>();
		//add the start node to the frontier
		frontier.add(start);
		//intitialize a variable to store the current node being checked
		Node currentNode = null;
		while(!routeCreated){
			//move the best node from the frontier to the currently being checked node
			currentNode = frontier.poll();
			//mark the current node as visited
			currentNode.setVisited();
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
			
			//add the child nodes to the frontier
			for(Node nextNode : graph.get(currentNode)){
				System.out.println("adding child");
				if(nextNode.getVisited()){
					System.out.println("child has been visited before me");
					//skip the node if it's already been visited
					continue;
				} else if(nextNode == currentNode.getParent()){
					//no need to check the parent node.
					//parent should have already been checked, but just in case
					System.out.println("I found my parent!");
					continue;
				} else {
					System.out.println("storing child");
					//Node hasn't been seen before.
					//calc the node's g(n)
					double currentPathCost = currentNode.getG() +
							space.findShortestDistance(nextNode.getLoc(), goal.getLoc());
					if(currentPathCost >= nextNode.getG() && nextNode.getG() != 0){
						//We found an equal or better path elsewhere, move on
						//If it's equal elsewhere, then including it would duplicate paths
						continue;
					}
					//set the path cost to the node
					//this also sets the total estimated cost
					nextNode.setCost(currentPathCost);
					//make sure the current Node is marked as a parent node
					nextNode.setParent(currentNode);
					//add the node to the frontier
					frontier.add(nextNode);
				}
			}
		}
		
		//create a stack to list the movements (LIFO)
		System.out.println("storing route");
		Stack<Node> route = new Stack<Node>();
		while(currentNode.getParent() != null){
			//push the current (last) node onto the stack
			route.push(currentNode);
			//set the current node to the parent of the node that just entered the stack
			currentNode = currentNode.getParent();
		}
		System.out.println("returning route");
		//current node should equal start at this point, but start isn't needed on the stack.
		return route;
	}
}