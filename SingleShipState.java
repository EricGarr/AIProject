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
	Asteroid target;
	UUID ship;
	String teamName;
	double[] distances;
	
	public SingleShipState(){
		
	}
	
	public SingleShipState(Ship s){
		ship = s.getId();
		teamName = s.getTeamName();
		distances = new double[3];
	}
	
	String getTeamName(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getTeamName();
	}
	
	double getCurrentEnergy(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getEnergy();
	}
	
	double getResources(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getTotal();
	}
	
	double getWater(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getResourceQuantity(ResourceTypes.WATER);
	}
	
	double getFuel(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getResourceQuantity(ResourceTypes.FUEL);
	}
	
	double getMetal(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getResources().getResourceQuantity(ResourceTypes.METALS);
	}
	
	AbstractAction getAction(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getCurrentAction();
	}
	
	Position getPosition(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getPosition();
	}
	
	UUID getId(Toroidal2DPhysics space){
		Ship self = (Ship) space.getObjectById(ship);
		return self.getId();
	}
	
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
	
	void setTarget(Asteroid ast){
		target = ast;
	}
	
	Asteroid getTarget(){
		return target;
	}
	
	boolean getChase(Toroidal2DPhysics space){
		KnowledgeRepresentation kr = new KnowledgeRepresentation();
		double time = space.getCurrentTimestep();
		while((space.getCurrentTimestep()-time)>15){
			kr.add(space.getObjectById(ship).getPosition());
		}
		return kr.checkChase();
	}
	
	boolean getStuck(Toroidal2DPhysics space){
		KnowledgeRepresentation kr = new KnowledgeRepresentation();
		double time = space.getCurrentTimestep();
		while((space.getCurrentTimestep()-time)>15){
			kr.add(space.getObjectById(ship).getPosition());
		}
		return kr.checkStuck();
	}
	
	UUID getUUID(){
		return ship;
	}
	
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
}