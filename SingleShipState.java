package garr9903;

import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
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
	Asteroid target;
	//use to get info on the ship
	//Storing just the UUID so that queries always return the correct values
	UUID ship;
	//used to ensure client is only messing with its own ships
	String teamName;
	
	public SingleShipState(){
		//default constructor, never use.
		System.out.println("I should have never been called you fool.");
	}
	
	public SingleShipState(Ship s){
		//Actual constructor
		ship = s.getId();
		teamName = s.getTeamName();
	}
	
	//Gets the team name from the game space
	String getTeamName(Toroidal2DPhysics space){
		return teamName;
	}
	
	//Gets the ship's current energy from the game space
	double getCurrentEnergy(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getEnergy();
	}
	
	//Gets the total number of resources being carried by the ship from the game space 
	double getResources(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getTotal();
	}
	
	//Gets the amount of water the ship has
	double getWater(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getResourceQuantity(ResourceTypes.WATER);
	}
	
	//Gets the amount of fuel the ship has
	double getFuel(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getResourceQuantity(ResourceTypes.FUEL);
	}
	
	//Gets the amount of metal the ship has
	double getMetal(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getResourceQuantity(ResourceTypes.METALS);
	}
	
	//finds the ship's current action
	//used to check for if the ship is stuck or endlessly chasing an asteroid
	AbstractAction getAction(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getCurrentAction();
	}
	
	//gets the ships current position in the game
	Position getPosition(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getPosition();
	}
	
	//finds the base nearest to the ship
	Base getNearestBase(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		Set<Base> bases = space.getBases();
		double nearest = Double.MAX_VALUE;
		Base best = null;
		for (Base base : bases){
			if(base.getTeamName() == self.getTeamName()){
				double dist = space.findShortestDistance(self.getPosition(), base.getPosition());
				if(dist < nearest){
					best = base;
					nearest = dist; 
				}
			}
		}
		return best;
	}
	
	//returns the best asteroid in terms of value/distance
	Asteroid getBestAsteroid(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		Set<Asteroid> asteroids = space.getAsteroids();
		double test = Double.MIN_VALUE;
		Asteroid best = null;
		for(Asteroid ast : asteroids){
			if(ast.getResources().getTotal() / space.findShortestDistance(self.getPosition(), ast.getPosition()) > test){
				best = ast;
				test = ast.getResources().getTotal() / space.findShortestDistance(self.getPosition(), ast.getPosition());
			}
		}
		return best;
	}
	
	//sets the ship's target asteroid
	void setTarget(Asteroid ast){
		target = ast;
	}
	
	//returns the ship's target asteroid
	Asteroid getTarget(){
		return target;
	}
	
	//checks to see if the ship has gotten into an endless chase state
	boolean getChase(Toroidal2DPhysics space){
		KnowledgeRepresentation kr = new KnowledgeRepresentation();
		double time = space.getCurrentTimestep();
		while((space.getCurrentTimestep()-time)>15){
			kr.add(space.getObjectById(ship).getPosition());
		}
		return kr.checkChase();
	}
	
	//checks to see if the ship has gotten stuck on an asteroid
	boolean getStuck(Toroidal2DPhysics space){
		KnowledgeRepresentation kr = new KnowledgeRepresentation();
		double time = space.getCurrentTimestep();
		while((space.getCurrentTimestep()-time)>15){
			kr.add(space.getObjectById(ship).getPosition());
		}
		return kr.checkStuck();
	}
	
	//returns the UUID of the ship
	UUID getUUID(){
		return ship;
	}
	
	//finds the beacon closest to the ship
	Beacon getNearestBeacon(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		Set<Beacon> beacons = space.getBeacons();
		double closest = Double.MAX_VALUE;
		Beacon best = null;
		for(Beacon beacon : beacons){
			double dist = space.findShortestDistance(beacon.getPosition(), self.getPosition());
			if(dist < closest){
				best = beacon;
				closest = dist;
			}
		}
		return best;
	}

	AbstractAction getCurrentAction(Toroidal2DPhysics space) {
		Ship self = (Ship) space.getObjectById(ship);
		return self.getCurrentAction();
	}
}