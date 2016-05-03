package garr9903;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import spacesettlers.actions.AbstractAction;
import spacesettlers.actions.MoveAction;
import spacesettlers.objects.AbstractObject;
import spacesettlers.objects.Asteroid;
import spacesettlers.objects.Base;
import spacesettlers.objects.Beacon;
import spacesettlers.objects.Ship;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Movement;
import spacesettlers.utilities.Position;
import spacesettlers.utilities.Vector2D;

public class Planner {
	public enum resources{
		METAL,
		WATER,
		FUEL,
		NONE
	}

	private Set<Asteroid>currAsts; 
	private Set<Beacon> currBeacons;  
	private LinkedHashMap<UUID,ArrayList<UUID>> targets;
	
	private int sight;
	String teamName;
	
	public Planner(int range, String team){
		currAsts = new HashSet<Asteroid>();
		currBeacons = new HashSet<Beacon>();
		targets = new LinkedHashMap<UUID,ArrayList<UUID>>();
		
		sight = range;
		teamName = team;
	}
	
	public void plan(Toroidal2DPhysics space, UUID id){
		//System.out.println("creating plan objects");
		
		//System.out.println("getting asteroids");
		Set<Asteroid> freeAsteroids = space.getAsteroids();
		
		//System.out.println("getting beacons");
		Set<Beacon> freeBeacons = space.getBeacons();
		
		System.out.println("getting bases");
		Set<Base> currBases = space.getBases();
		System.out.println("removing bases");
		for(Base b : currBases){
			System.out.println("is the base mine?");
			if(!(b.getTeamName().equalsIgnoreCase(teamName))){
				currBases.remove(b);
			}
		}
		
		System.out.println("creating target object");
		AbstractObject target = null;
		int currTarget = 0;
		Position loc = space.getObjectById(id).getPosition();
		
		System.out.println("adding targets to plan");
		while(targets.get(id).size() < 5){
			System.out.println("removing asteroids");
			freeAsteroids.removeAll(currAsts);
			
			System.out.println("removing beacons");
			freeBeacons.removeAll(currBeacons);
			
			System.out.println("storing ship");
			Ship ship = (Ship) space.getObjectById(id);
			System.out.println("storing energy");
			double energy = ship.getEnergy();
			System.out.println("storing resources");
			double resources = ship.getResources().getTotal();
			
			if(resources >= 750){
				System.out.println("adding base");
				if(currTarget == 0){
					target = getBestBase(space, ship, currBases);
				} else {
					target = getBestBase(space, space.getObjectById(targets.get(id).get(currTarget)), currBases);
				}
				
				resources = 0;
			} else if(energy < 2000 && resources < 750){
				System.out.println("adding beacon");
				if(currTarget == 0){
					target = getBestBeacon(space, ship, freeBeacons);
				} else {
					target = getBestBeacon(space, space.getObjectById(targets.get(id).get(currTarget)), freeBeacons);
				}
				currBeacons.add((Beacon) target);
			} else {
				System.out.println("adding asteroid");
				if(currTarget == 0){
					target = getBestAsteroid(space, ship, freeAsteroids);
				} else {
					target = getBestAsteroid(space, space.getObjectById(targets.get(id).get(currTarget)), freeAsteroids);
				}
				currAsts.add((Asteroid) target);
				resources = resources + target.getResources().getTotal();
			}
			
			System.out.println("adding target");
			targets.get(id).add(target.getId());
			currTarget++;
			System.out.println("On target number: " + currTarget);
			System.out.println("Has: " + targets.get(id).size() + " targets now");
			
			loc = space.getObjectById(targets.get(id).get(currTarget)).getPosition();
			AbstractAction temp = calcMove(space, loc, target.getPosition());
			Movement actionMovement = temp.getMovement(space, ship);
			double angularAccel = Math.abs(actionMovement.getAngularAccleration());
			double angularInertia = (3.0 * ship.getMass() * ship.getRadius() * angularAccel) / 2.0; 
			double linearAccel = actionMovement.getTranslationalAcceleration().getMagnitude();
			double linearInertia = ship.getMass() * linearAccel;
			
			int energyPenalty = (int) Math.floor(space.ENERGY_PENALTY * (angularInertia + linearInertia));
			energy = energy - energyPenalty;
		}
	}
	
	//ships hull is full enough, find the nearest base to turn resources in
	Base getBestBase(Toroidal2DPhysics space, AbstractObject obj, Set<Base> bases){
		//get the list of bases
		double nearest = Double.MAX_VALUE;
		//initialize the current best base found
		Base best = null;
		for (Base base : bases){
			//check if base is ours, and if the path is clear
			if(base.getTeamName() == teamName &&
					space.isPathClearOfObstructions(
							obj.getPosition(),
							base.getPosition(),
							space.getAllObjects(),
							obj.getRadius()
							)){
				//find the distance to the base
				double dist = space.findShortestDistance(obj.getPosition(), base.getPosition());
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
				if(base.getTeamName() == teamName){
					double dist = space.findShortestDistance(obj.getPosition(), base.getPosition());
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
	
	//ship is low on energy, find the best source
	AbstractObject getBestBeacon(Toroidal2DPhysics space, AbstractObject obj, Set<Beacon> beacons){
		double closestBeacon = Double.MAX_VALUE;
		//best beacon found so far
		Beacon bestBeacon = null;
		if(beacons != null){
			for(Beacon beacon : beacons){
				//find the distance to the beacon
				double dist = space.findShortestDistance(beacon.getPosition(), obj.getPosition());
				if(dist < closestBeacon){
					//if the beacon is closer, store it and its distance
					bestBeacon = beacon;
					closestBeacon = dist;
				}
			}
		} else {
			for(Beacon beacon : space.getBeacons()){
				//find the distance to the beacon
				double dist = space.findShortestDistance(beacon.getPosition(), obj.getPosition());
				if(dist < closestBeacon){
					//if the beacon is closer, store it and its distance
					bestBeacon = beacon;
					closestBeacon = dist;
				}
			}
		}
		return bestBeacon;
	}
	
	//finds the asteroid with the best ratio of resources/distance
	Asteroid getBestAsteroid(Toroidal2DPhysics space, AbstractObject obj, Set<Asteroid> asts){
		double test = Double.MIN_VALUE;
		//best asteroid found so far
		Asteroid best = null;
		System.out.println("selecting asteroid");
		if(asts != null){
			for(Asteroid ast : asts){
				//calculate the ratio of resources to distance for each asteriod
				if(ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition()) > test){
					//if a better asteroid is found, store it, and it's ratio
					best = ast;
					test = ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition());
				}
			}
		} else {
			for(Asteroid ast : space.getAsteroids()){
				//calculate the ratio of resources to distance for each asteriod
				if(ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition()) > test){
					//if a better asteroid is found, store it, and it's ratio
					best = ast;
					test = ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition());
				}
			}
		}
		//return the best asteroid found
		System.out.println("returning asteroid");
		currAsts.add(best);
		return best;
	}
	
	
	AbstractAction calcMove(Toroidal2DPhysics space, Position current, Position target){
		//get the unit vector to the target
		Vector2D vect = space.findShortestDistanceVector(current, target).getUnitVector();
		//set the unit vector to the fastest speed
		vect.setX(vect.getXValue()*Movement.MAX_TRANSLATIONAL_ACCELERATION);
		vect.setY(vect.getYValue()*Movement.MAX_TRANSLATIONAL_ACCELERATION);
		//move to the target
		MoveAction temp = new MoveAction(space, current, target, vect);
		temp.setKpRotational(0);
		temp.setKvRotational(0);
		return temp;
	}
	
	public AbstractAction getAction(Toroidal2DPhysics space, UUID id){
		if(!(targets.containsKey(id))){
			targets.put(id, new ArrayList<UUID>());
		}
		
		if(targets.get(id).isEmpty()){
			//System.out.println("Ship needs targets.");
			plan(space, id);
		}
		
		AbstractAction getCurrTarget = calcMove(
				space,
				space.getObjectById(id).getPosition(),
				space.getObjectById(targets.get(id).get(0)).getPosition()
				);
		return getCurrTarget;
	}
	
	public void replan(Toroidal2DPhysics space){
		System.out.println("Replanning");
		for(Ship ship : space.getShips()){
			System.out.println(ship.getTeamName());
			if(ship.getTeamName().equalsIgnoreCase(teamName)){
				System.out.println("Found one of my ships.");
				
				if(targets.containsKey(ship.getId())){
					System.out.println("Removing targets to replan."); 
					targets.get(ship.getId()).removeAll(targets.get(ship.getId()));
				}
				
				if(!(targets.containsKey(ship.getId()))){
					System.out.println("Adding it to targets.");
					targets.put(ship.getId(), new ArrayList<UUID>());
				}
				System.out.println("Planning");
				plan(space, ship.getId());
			}
		}
		System.out.println("planning done.");
	}
	
	public void removeTarget(UUID id){
		System.out.println("removing asteroid");
		for(ArrayList<UUID> target : targets.values()){
			if(target.contains(id)){
				target.remove(target.indexOf(id));
				break;
			}
		}
	}
	
	public LinkedHashMap<UUID, ArrayList<UUID>> getAllTargets(){
		return targets;
	}
}
