package garr9903;

import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Ship;
import spacesettlers.objects.Base;
import spacesettlers.objects.resources.ResourceTypes;
import spacesettlers.utilities.Position;
import spacesettlers.simulator.Toroidal2DPhysics;

public class SingleShipState {
	Asteroid target;
	UUID ship;
	String teamName;
	Set<Base> bases;
	
	public SingleShipState(){
		
	}
	
	public SingleShipState(Ship s){
		ship = s.getId();
		teamName = s.getTeamName();
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
		double nearest = Double.MAX_VALUE;
		Base best = null;
		for (Base base : bases){
			double dist = space.findShortestDistance(self.getPosition(), base.getPosition());
			if(dist < nearest){
				best = base;
				nearest = dist; 
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
}
