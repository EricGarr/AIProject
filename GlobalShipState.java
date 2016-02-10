package garr9903;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.MoveToObjectAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Ship;
import spacesettlers.objects.Beacon;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Vector2D;

public class GlobalShipState {
	Set<Ship> ships;
	Set<Asteroid> asteroids;
	Set<Base> bases;
	Set<Beacon> beacons;
	//HashMap<UUID, SingleShipState> obsticles;
	SingleShipState myShip;
	Toroidal2DPhysics space;
	String teamName;
	
	GlobalShipState(){
		
	}
	
	GlobalShipState(Toroidal2DPhysics space, String teamName){
		ships = space.getShips();
		asteroids = space.getAsteroids();
		bases = space.getBases();
		beacons = space.getBeacons();
		this.teamName = teamName; 
		for(Ship ship : ships){
			if(ship.getTeamName() == teamName){
				myShip = new SingleShipState(ship);
			}
		}
		//implement obsticle tracking here
		this.space = space;
	}
		
	AbstractAction goToAsteroid(Asteroid asteroid){
		MoveToObjectAction mvo = new MoveToObjectAction(space, myShip.getPosition(), asteroid);
		myShip.setTarget(asteroid);
		return mvo;
	}
	
	AbstractAction goToBeacon(){
		Beacon beacon = myShip.getNearestBeacon(space);
		MoveToObjectAction mvo = new MoveToObjectAction(space, myShip.getPosition(), beacon);
		myShip.setTarget(beacon);
		return mvo;
	}
	
	AbstractAction goToBase(){
		Base base = myShip.getNearestBase(space);
		MoveToObjectAction mvo = new MoveToObjectAction(space, myShip.getPosition(), base);
		myShip.setTarget(base);
		return mvo;
	}
	
	boolean buyBase(){
		boolean buy = true;
		bases = space.getBases();
		double minDist = 300;
		for(Base base : bases){
			if(base.getTeamName() == teamName && space.findShortestDistance(base.getPosition(), myShip.getPosition()) < minDist){
				buy = false;
			}
		}
		
		return buy;
	}
	
	Ship getShip(Toroidal2DPhysics space){
		return (Ship) space.getObjectById(myShip.getUUID());
	}
}
