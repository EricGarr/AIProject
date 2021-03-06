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
	
	int speed;
	private int sight;
	String teamName;
	
	public Planner(int range, String team, int speed){
		currAsts = new HashSet<Asteroid>();
		currBeacons = new HashSet<Beacon>();
		targets = new LinkedHashMap<UUID,ArrayList<UUID>>();
		
		sight = range;
		teamName = team;
		this.speed = speed;
	}
	
	public void plan(Toroidal2DPhysics space, UUID id){
		Set<Base> currBases = new HashSet<Base>();
		for(Base b : space.getBases()){
			if(b.getTeamName().equalsIgnoreCase(teamName)){
				currBases.add(b);
			}
		}
		
		AbstractObject target = null;
		int currTarget = 0;
		Position loc = space.getObjectById(id).getPosition();
		Ship ship = (Ship) space.getObjectById(id);
		double energy = ship.getEnergy();
		double resources = ship.getResources().getTotal();
		
		while(targets.get(id).size() < 5){
			Set<Asteroid> freeAsteroids = new HashSet<Asteroid>();
			for(Asteroid ast : space.getAsteroids()){
				if(!(currAsts.contains(ast))){
					freeAsteroids.add(ast);
				}
			}
			
			Set<Beacon> freeBeacons = new HashSet<Beacon>();
			for(Beacon b : space.getBeacons()){
				if(!(currBeacons.contains(b))){
					freeBeacons.add(b);
				}
			}
			
			if(resources >= 750){
				//ship full, turn in
				if(currTarget == 0){
					//use the ship's location
					target = getBestBase(space, ship, currBases);
				} else {
					//use the last object's location
					target = getBestBase(space, space.getObjectById(targets.get(id).get(currTarget-1)), currBases);
				}
				//empty resources for planning
				resources = 0;
			} else if(energy < 2000 && resources < 750){
				//ship needs energy and isn't on it's way back to base
				if(currTarget == 0){
					//plan from the ship's location
					target = getBestBeacon(space, ship, freeBeacons);
				} else {
					//plan from the last target's location
					target = getBestBeacon(space, space.getObjectById(targets.get(id).get(currTarget-1)), freeBeacons);
				}
				//remove the beacon from consideration
				currBeacons.add((Beacon) target);
				//increase ship's energy for future planning
				energy += Beacon.BEACON_ENERGY_BOOST;
			} else {
				//get an asteroid
				if(currTarget == 0){
					//search from the ships's location
					target = getBestAsteroid(space, ship, freeAsteroids);
				} else {
					//search from the previous target's location
					target = getBestAsteroid(space, space.getObjectById(targets.get(id).get(currTarget-1)), freeAsteroids);
				}
				//remove the asteroid from further consideration
				currAsts.add((Asteroid) target);
//				System.out.println(freeAsteroids.size());
//				System.out.println(target);
				//add the resources to the ship's hold for future planning
				resources = resources + target.getResources().getTotal();
			}
			
			targets.get(id).add(target.getId());
			currTarget++;
			
			AbstractAction temp = calcMove(space, loc, target.getPosition());
			Movement actionMovement = temp.getMovement(space, ship);
			double angularAccel = Math.abs(actionMovement.getAngularAccleration());
			double angularInertia = (3.0 * ship.getMass() * ship.getRadius() * angularAccel) / 2.0;
			double linearAccel = actionMovement.getTranslationalAcceleration().getMagnitude();
			double linearInertia = ship.getMass() * linearAccel;
			
			int energyPenalty = (int) Math.floor(space.ENERGY_PENALTY * (angularInertia + linearInertia));
			energy = energy - (energyPenalty + 100);
			
			loc = target.getPosition();
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
		if(asts != null){
			for(Asteroid ast : asts){
				if(space.findShortestDistance(obj.getPosition(), ast.getPosition()) <= sight){
					//calculate the ratio of resources to distance for each asteriod
					if(ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition()) > test){
						//if a better asteroid is found, store it, and it's ratio
						best = ast;
						test = ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition());
					}
				}
			}
		}
		//if no asteroid in range was found, select from universe
		if(best == null){
			for(Asteroid ast : asts){
				//calculate the ratio of resources to distance for each asteriod
				if(ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition()) > test){
					//if a better asteroid is found, store it, and it's ratio
					best = ast;
					test = ast.getResources().getTotal() / space.findShortestDistance(obj.getPosition(), ast.getPosition());
				}
			}
		}
		//return the best asteroid found
		currAsts.add(best);
		return best;
	}
	
	
	AbstractAction calcMove(Toroidal2DPhysics space, Position current, Position target){
		//get the unit vector to the target
		Vector2D vect = space.findShortestDistanceVector(current, target).getUnitVector();
		//set the unit vector to the fastest speed
		vect.setX(vect.getXValue()*speed);
		vect.setY(vect.getYValue()*speed);
		//move to the target
		MoveAction temp = new MoveAction(space, current, target, vect);
		temp.setKpRotational(0);
		temp.setKvRotational(0);
		return temp;
	}
	
	public AbstractAction getAction(Toroidal2DPhysics space, UUID id){
		//checking ship existence
		if(!(targets.containsKey(id))){
			targets.put(id, new ArrayList<UUID>());
		}
		
		//removing dead targets
		removeTarget(space);
		
		//planning if needed
		if(targets.get(id).isEmpty()){
			plan(space, id);
		}
		
		//creating action
		AbstractAction getCurrTarget = calcMove(
				space,
				space.getObjectById(id).getPosition(),
				space.getObjectById(targets.get(id).get(0)).getPosition()
				);
		//returning action
		return getCurrTarget;
	}
	
	public void replan(Toroidal2DPhysics space){
		currAsts.clear();
		currBeacons.clear();
		for(Ship ship : space.getShips()){
			if(ship.getTeamName().equalsIgnoreCase(teamName)){
				if(!(targets.containsKey(ship.getId()))){
					targets.put(ship.getId(), new ArrayList<UUID>());
				}
				
				if(targets.containsKey(ship.getId())){
					targets.get(ship.getId()).clear();;
				}
				
				plan(space, ship.getId());
			}
		}
	}
	
	public void removeTarget(Toroidal2DPhysics space){
		for(Ship ship : space.getShips()){
			if(targets.containsKey(ship.getId())){
				for(UUID id : targets.get(ship.getId())){
					if((space.getObjectById(id) == null) || !(space.getObjectById(id).isAlive())){
						targets.get(ship.getId()).remove(targets.get(ship.getId()).indexOf(id));
					}
					if(space.getObjectById(id).getClass() == Base.class && ship.getResources().getTotal() < 750){
						if(targets.get(ship.getId()).indexOf(id) == 0){
							targets.get(ship.getId()).remove(0);
						}
					}
				}
			}
		}
	}
	
	public LinkedHashMap<UUID, ArrayList<UUID>> getAllTargets(){
		return targets;
	}
}
