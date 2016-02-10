package garr9903;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.DoNothingAction;
import spacesettlers.actions.MoveToObjectAction;
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
import spacesettlers.utilities.Position;

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
	GlobalShipState shipHandler;
	SingleShipState ship;
	HashMap <UUID, Ship> targets;
	HashMap <UUID, Boolean> aimingForBase;
	boolean bought_ship = false;
	
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
				
				AbstractAction action;
				action = getAsteroidCollectorAction(space);
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
	private AbstractAction getAsteroidCollectorAction(Toroidal2DPhysics space) {
		AbstractAction current = ship.getCurrentAction(space);
		Position currentPosition = ship.getPosition(space);
		
		// aim for a beacon if there isn't enough energy
		if (ship.getCurrentEnergy(space) < 2000) {
			AbstractAction newAction = shipHandler.goToBeacon();
			aimingForBase.put(ship.getUUID(), false);
			return newAction;
		}

		// if the ship has enough resourcesAvailable, take it back to base
		if (ship.getResources(space) > 500) {
			AbstractAction newAction = shipHandler.goToBase();
			aimingForBase.put(ship.getUUID(), true);
			return newAction;
		}

		// did we bounce off the base?
		if (ship.getResources(space) == 0 && ship.getCurrentEnergy(space) > 2000 && aimingForBase.containsKey(ship.getUUID()) && aimingForBase.get(ship.getUUID())) {
			current = null;
			aimingForBase.put(ship.getUUID(), false);
		}

		// otherwise aim for the asteroid
		if (current == null || current.isMovementFinished(space)) {
			aimingForBase.put(ship.getUUID(), false);
			AbstractAction newAction = null;

			if (ship.getTarget() == null) {
				newAction = shipHandler.goToBeacon();
			} else {
				newAction = shipHandler.goToAsteroid();
			}
			return newAction;
		} else {
			return ship.getCurrentAction(space);
		}
	}

	@Override
	public void getMovementEnd(Toroidal2DPhysics space, Set<AbstractActionableObject> actionableObjects) {
		ArrayList<Asteroid> finishedAsteroids = new ArrayList<Asteroid>();

		for (UUID asteroidId : targets.keySet()) {
			Asteroid asteroid = (Asteroid) space.getObjectById(asteroidId);
			try{
				if (asteroid == null || !asteroid.isAlive()) {
					finishedAsteroids.add(asteroid);
				}
			} catch(Exception e){
				
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
		shipHandler = new GlobalShipState(space);
		Set<Ship> ships = space.getShips();
		for(Ship s : ships){
			if(s.getTeamName() == "PenaGarrison"){
				ship = new SingleShipState(s);
			}
		}
		
		/*
		XStream xstream = new XStream();
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
		XStream xstream = new XStream();
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
		}
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
		
		if (purchaseCosts.canAfford(PurchaseTypes.BASE, resourcesAvailable) && bought_ship) {
			for (AbstractActionableObject actionableObject : actionableObjects) {
				if (actionableObject instanceof Ship) {
					Ship ship = (Ship) actionableObject;
					Set<Base> bases = space.getBases();

					// how far away is this ship to a base of my team?
					double maxDistance = Double.MIN_VALUE;
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
	 * The pacifist asteroid collector doesn't use power ups 
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
	
	public void suicide(Ship ship){
		HashMap<UUID, SpaceSettlersPowerupEnum> powerUps = new HashMap<UUID, SpaceSettlersPowerupEnum>();
		SpaceSettlersPowerupEnum powerup = SpaceSettlersPowerupEnum.FIRE_MISSILE;
		while(ship.getEnergy() > 0){
			powerUps.put(ship.getId() , powerup);
		}
	}
}
