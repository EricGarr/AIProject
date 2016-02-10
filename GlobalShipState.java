package garr9903;

import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.actions.MoveToObjectAction;
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
	String teamName;
	SingleShipState myShip;
	Toroidal2DPhysics space;
	
	GlobalShipState(){
		
	}
	
	GlobalShipState(Toroidal2DPhysics space){
		ships = space.getShips();
		asteroids = space.getAsteroids();
		bases = space.getBases();
		beacons = space.getBeacons();
		teamName = "PenaGarrison";
		for(Ship ship : ships){
			if (ship.getTeamName() == teamName){
				myShip = new SingleShipState(ship);
			}
		}
		this.space = space;
	}
	
	void setTarget(){
		myShip.setTarget(myShip.getBestAsteroid(space));
	}
	
	AbstractAction goToAsteroid(){
		MoveToObjectAction mvo = new MoveToObjectAction(space, myShip.getPosition(space), myShip.getTarget());
		return mvo;
	}
	
	AbstractAction handleChase(Ship ship){
		AbstractAction mv = ship.getCurrentAction();
		if(myShip.getChase(space)){
			Vector2D currVector = new Vector2D(ship.getCurrentAction().getMovement(space, (Ship) space.getObjectById(myShip.getUUID())).getTranslationalAcceleration());
			Vector2D newVector = new Vector2D(currVector.getAngle(), currVector.getMagnitude()*2);
			mv = new MoveAction(space, myShip.getPosition(space), myShip.getTarget().getPosition(), newVector);
		}
		return mv;
	}
	
	AbstractAction goToBeacon(){
		MoveToObjectAction mvo = new MoveToObjectAction(space, myShip.getPosition(space), myShip.getNearestBeacon(space));
		return mvo;
	}
	
	AbstractAction goToBase(){
		MoveToObjectAction mvo = new MoveToObjectAction(space, myShip.getPosition(space), myShip.getNearestBase(space));
		return mvo;
	}
	
	boolean buyBase(){
		boolean buy = true;
		bases = space.getBases();
		double minDist = 300;
		for(Base base : bases){
			if(base.getTeamName() == teamName && space.findShortestDistance(base.getPosition(), myShip.getPosition(space)) < minDist){
				buy = false;
			}
		}
		
		return buy;
	}
	
	Ship getShip(Toroidal2DPhysics space){
		return (Ship) space.getObjectById(myShip.getUUID());
	}
}
