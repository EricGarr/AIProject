package garr9903;

import java.util.Random;

import javax.swing.text.Position;

import spacesettlers.simulator.Toroidal2DPhysics;

public class GridSquare {
	private int lowerX;
	private int upperX;
	private int lowerY;
	private int upperY;
	Toroidal2DPhysics space;
	
	public GridSquare(int lx, int ux, int ly, int uy, Toroidal2DPhysics s){
		lowerX = lx;
		upperX = ux;
		lowerY = ly;
		upperY = uy;
		space = s;
	}
	
	public Position getPoint(){
		Random rand = new Random();
		return (Position) space.getRandomFreeLocationInRegion(rand, 40, (lowerX + (upperX-lowerX)/2), (lowerY + (upperY-lowerY)/2), 40);
	}
	
}
