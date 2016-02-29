package garr9903;

import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Ship;
import spacesettlers.objects.powerups.SpaceSettlersPowerupEnum;
import spacesettlers.objects.resources.ResourceTypes;
import spacesettlers.utilities.Position;

public class SingleShipState {
	//use to get info on the ship
	//Storing just the UUID so that queries always return the correct values
	Ship ship;
	
	public enum Target{
		ENERGY,
		BASE,
		ASTEROID
	}
	
	public SingleShipState(){
		//default constructor, never use.
		System.out.println("I should have never been called you fool.");
	}
	
	public SingleShipState(Ship s){
		//Actual constructor
		ship = s;
	}
	
	public Target getState(){
		if(getCurrentEnergy() < 2000){
			return Target.ENERGY; 
		} else if(getResources() > (ship.getMaxEnergy()/10)){
			return Target.BASE;
		} else {
			return Target.ASTEROID;
		}
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
	
	//returns the UUID of the ship
	public UUID getUUID(){
		return ship.getId();
	}
	
	public AbstractAction getCurrentAction() {
		return ship.getCurrentAction();
	}
	
	public Set<SpaceSettlersPowerupEnum> getPowerUps(){
		return ship.getCurrentPowerups();
	}
}