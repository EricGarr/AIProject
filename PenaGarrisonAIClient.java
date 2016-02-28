package garr9903;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
/*
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
*/
import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveAction;
//import spacesettlers.actions.MoveToObjectAction;
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
	SingleShipState myShip;
	HashMap <UUID, Ship> targets;
	HashMap <UUID, Boolean> aimingForBase;
	boolean bought_ship = true;
	
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
	private AbstractAction getAsteroidCollectorAction(Toroidal2DPhysics space,
			Ship ship) {
		AbstractAction current = ship.getCurrentAction();
		Position currentPosition = ship.getPosition();
		AbstractAction newAction = null;
		
		// aim for a beacon if there isn't enough energy
		if (ship.getEnergy() < 2000) {
			AbstractObject target = getNearestBeacon(space, ship);
			newAction = calcMove(space, currentPosition, target.getPosition());
			aimingForBase.put(ship.getId(), false);
			return newAction;
		}

		// if the ship has enough resourcesAvailable, take it back to base
		if (ship.getResources().getTotal() > 500 || space.getCurrentTimestep() >= 19900) {
			Base base = getNearestBase(space, ship);
			newAction = calcMove(space, currentPosition, base.getPosition());
			aimingForBase.put(ship.getId(), true);
			return newAction;
		}

		// did we bounce off the base?
		if (ship.getResources().getTotal() == 0 && ship.getEnergy() > 2000 && aimingForBase.containsKey(ship.getId()) && aimingForBase.get(ship.getId())) {
			current = null;
			aimingForBase.put(ship.getId(), false);
		}
		
		//if nothing else triggered, collect an asteroid
		aimingForBase.put(ship.getId(), false);
		Asteroid asteroid = getBestAsteroid(space, ship);
		
		if (asteroid == null) {
			asteroid = getBestAsteroid(space, ship);
		}
		if (asteroid != null) {
			targets.put(asteroid.getId(), ship);
			newAction = calcMove(space, currentPosition, asteroid.getPosition());
		}
		
		if(newAction != null){
			return newAction;
		} else {
			return new DoNothingAction();
		}
	}
	
	Asteroid getBestAsteroid(Toroidal2DPhysics space, Ship ship){
		Set<Asteroid> asteroids = space.getAsteroids();
		double test = Double.MIN_VALUE;
		Asteroid best = null;
		for(Asteroid ast : asteroids){
			if(ast.getResources().getTotal() / space.findShortestDistance(ship.getPosition(), ast.getPosition()) > test){
				best = ast;
				test = ast.getResources().getTotal() / space.findShortestDistance(ship.getPosition(), ast.getPosition());
			}
		}
		return best;
	}
	
	AbstractObject getNearestBeacon(Toroidal2DPhysics space, Ship ship){
		Set<Beacon> beacons = space.getBeacons();
		double closestBeacon = Double.MAX_VALUE;
		Beacon bestBeacon = null;
		for(Beacon beacon : beacons){
			double dist = space.findShortestDistance(beacon.getPosition(), ship.getPosition());
			if(dist < closestBeacon){
				bestBeacon = beacon;
				closestBeacon = dist;
			}
		}
		Set<Base> bases = space.getBases();
		double closestBase = Double.MAX_VALUE;
		Base bestBase = null;
		for(Base base : bases){
			if(base.getTeamName() == this.getTeamName() && base.getEnergy() > 1000){
				double dist = space.findShortestDistance(base.getPosition(), ship.getPosition());
				if(dist < closestBase){
					bestBase = base;
					closestBase = dist;
				}
			}
		}
		if(closestBase > closestBeacon){
			return bestBeacon;
		} else {
			return bestBase;
		}
		//return bestBeacon;
	}
	
	Base getNearestBase(Toroidal2DPhysics space, Ship ship){
		Set<Base> bases = space.getBases();
		double nearest = Double.MAX_VALUE;
		Base best = null;
		for (Base base : bases){
			if(base.getTeamName() == ship.getTeamName() && space.isPathClearOfObstructions(ship.getPosition(), base.getPosition(), space.getAllObjects(), ship.getRadius())){
				double dist = space.findShortestDistance(ship.getPosition(), base.getPosition());
				if(dist < nearest){
					best = base;
					nearest = dist; 
				}
			}
		}
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
		return best;
	}
	
	AbstractAction calcMove(Toroidal2DPhysics space, Position current, Position target){
		Vector2D vect = space.findShortestDistanceVector(current, target).getUnitVector();
		vect.setX(vect.getXValue()*Movement.MAX_TRANSLATIONAL_ACCELERATION);
		vect.setY(vect.getYValue()*Movement.MAX_TRANSLATIONAL_ACCELERATION);
		
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
		double BASE_BUYING_DISTANCE = 200;
		boolean buyBase = true;
		
		if(purchaseCosts.canAfford(PurchaseTypes.POWERUP_DOUBLE_MAX_ENERGY, resourcesAvailable)){
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;
					purchases.put(ship.getId(), PurchaseTypes.POWERUP_DOUBLE_MAX_ENERGY);
					ship.addPowerup(SpaceSettlersPowerupEnum.DOUBLE_MAX_ENERGY);
				}
			}
		}
		
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
		
		Ship ship = (Ship) space.getObjectById(myShip.getUUID());
		
		if(ship.getCurrentPowerups().contains(SpaceSettlersPowerupEnum.DOUBLE_MAX_ENERGY)){
			powerUps.put(ship.getId(), SpaceSettlersPowerupEnum.DOUBLE_MAX_ENERGY);
		}
		
		return powerUps;
	}
}