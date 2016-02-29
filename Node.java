package garr9903;

import spacesettlers.utilities.Position;

public class Node implements Comparable<Node>{
	//variables needed by the node
	private double hOfN;
	private double gOfN;
	private double fOfN;
	private Node parent;
	private Position loc;
	private boolean visited;
	
	//node constructor
	public Node(Position l){
		//tell the node where it is
		loc = l;
		//initialize the other parameters
		parent = null;
		hOfN = 0;
		gOfN = 0;
		fOfN = 0;
	}
	
	//return the heuristic value
	public double getH(){
		return hOfN;
	}
	
	//return the path cost
	public double getG(){
		return gOfN;
	}
	
	//return the total cost
	public double getf(){
		return fOfN;
	}
	
	//return the parent node (used to get path cost)
	public Node getParent(){
		return parent;
	}
	
	//return the node's location
	public Position getLoc(){
		return loc;
	}
	
	public boolean getVisited(){
		return visited;
	}
	
	//set the heuristic cost
	public void setH(double val){
		hOfN = val;
		fOfN = hOfN + gOfN;
	}

	//set the path cost
	public void setCost(double val){
		gOfN = val;
		fOfN = hOfN + gOfN;
	}
	
	//set the current parent node
	public void setParent(Node par){
		parent = par;
	}
	
	public void setVisited(){
		visited = true;
	}
	
	//reset the current node
	public void wipe(){
		parent = null;
		hOfN = 0;
		gOfN = 0;
		fOfN = 0;
		visited = false;
	}
	
	@Override
	public int compareTo(Node n){
		return (int) (this.fOfN - n.getf());
	}
}
