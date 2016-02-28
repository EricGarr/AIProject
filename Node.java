package test;

import java.awt.Point;

public class Node {
	
	private int x, y;
	
	private Point[] directions = new Point[4];
	private Point North = new Point(0,1);
	private Point East = new Point(1,0);
	private Point South = new Point(0,-1);
	private Point West = new Point(-1,0);
	
	private Point[] edges = new Point[4];
	
	public Node() {
		
	}
	
	public Node(int x, int y) {
		this.x = x;
		this.y = y;
		
		directions[0] = North;
		directions[1] = East;
		directions[2] = South;
		directions[3] = West;
	}
	
	public Point[] getEdges(Node[][] nodes) {
		for(int i = 0; i < 4; i++) {
			edges[i] = new Point((directions[i].x*100) + this.x, (directions[i].y*60) + this.y);
			System.out.println(edges[i].x + ", " + edges[i].y);
		}
		
		return edges;
	}
}
