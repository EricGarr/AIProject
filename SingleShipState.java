package garr9903;

import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.objects.Base;
import spacesettlers.objects.resources.ResourceTypes;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;
import spacesettlers.simulator.Toroidal2DPhysics;

public class SingleShipState {
	//asteroid the ship is to pickup
	AbstractObject target;
	//use to get info on the ship
	//Storing just the UUID so that queries always return the correct values
	Ship ship;
	
	public SingleShipState(){
		//default constructor, never use.
		System.out.println("I should have never been called you fool.");
	}
	
	public SingleShipState(Ship s){
		//Actual constructor
		ship = s;
	}
	
	//Gets the team name from the game space
	public String getTeamName(){
		return ship.getTeamName();
	}
	
	//Gets the ship's current energy from the game space
	public double getCurrentEnergy(){
		return ship.getEnergy();
	}
	
	//Gets the total number of resources being carried by the ship from the game space 
	public double getResources(){
		return ship.getResources().getTotal();
	}
	
	//Gets the amount of water the ship has
	public double getWater(){
		return ship.getResources().getResourceQuantity(ResourceTypes.WATER);
	}
	
	//Gets the amount of fuel the ship has
	public double getFuel(){
		return ship.getResources().getResourceQuantity(ResourceTypes.FUEL);
	}
	
	//Gets the amount of metal the ship has
	public double getMetal(){
		return ship.getResources().getResourceQuantity(ResourceTypes.METALS);
	}
	
	//finds the ship's current action
	//used to check for if the ship is stuck or endlessly chasing an asteroid
	public AbstractAction getAction(){
		return ship.getCurrentAction();
	}
	
	//gets the ships current position in the game
	public Position getPosition(){
		return ship.getPosition();
	}
	
	//finds the base nearest to the ship
	public Base getNearestBase(Toroidal2DPhysics space){
		Set<Base> bases = space.getBases();
		double nearest = Double.MAX_VALUE;
		Base best = null;
		for (Base base : bases){
			if(base.getTeamName() == ship.getTeamName()){
				double dist = space.findShortestDistance(ship.getPosition(), base.getPosition());
				if(dist < nearest){
					best = base;
					nearest = dist; 
				}
			}
		}
		return best;
	}
	
	//returns the best asteroid in terms of value/distance
	public Asteroid getBestAsteroid(Toroidal2DPhysics space){
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
	
	//sets the ship's target asteroid
	public void setTarget(AbstractObject a){
		target = a;
	}
	
	//returns the ship's target asteroid
	public AbstractObject getTarget(){
		return target;
	}
	
	//returns the UUID of the ship
	public UUID getUUID(){
		return ship.getId();
	}
	
	//finds the beacon closest to the ship
	public Beacon getNearestBeacon(Toroidal2DPhysics space){
		Set<Beacon> beacons = space.getBeacons();
		double closest = Double.MAX_VALUE;
		Beacon best = null;
		for(Beacon beacon : beacons){
			double dist = space.findShortestDistance(beacon.getPosition(), ship.getPosition());
			if(dist < closest){
				best = beacon;
				closest = dist;
			}
		}
		return best;
	}

	public AbstractAction getCurrentAction() {
		return ship.getCurrentAction();
	}
}