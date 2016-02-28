package garr9903;

import spacesettlers.objects.AbstractObject;
import spacesettlers.simulator.Toroidal2DPhysics;
import spacesettlers.utilities.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class AStarSearch {
	Set<AbstractObject> allObjects;
	
	boolean isOccupied(int minx, int maxx, int miny, int maxy, Toroidal2DPhysics space){
		boolean occupied = false;
		return false;
	}
	
	public void search(Toroidal2DPhysics space){
		//search the game
		allObjects = space.getAllObjects();
	}
	
	/*
	 * Artifacts from attempting to do roadmap A*
	 */
	
	/*AStarSearch(Toroidal2DPhysics space){
		locations = new Position[16][18];
		//int nodeNum = 0;
		for(int width = 0; width < 1600; width++){
			for(int height = 0; height < 1080; height++){
				int x = width*100;
				int y = height*60;
				locations[width][height] = new Position(x, y);
			}
			//nodeNum++;
		}
	}*/
	
	/*
	//create the graph
	public void makeGraph(Toroidal2DPhysics space){
		//make sure there isn't an existing graph
		clearGraph();
		space.findShortestDistance(locations[0][0], locations[1][0]);
	}
	
	private void clearGraph(){
		//destroy the graph for next time
	}*/
}