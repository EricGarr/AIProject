package garr9903;

import java.util.Random;

public class PopulationInstance {
	//chromosomes
	int moveRate, sightRadius, newBaseDist, shipsToBase;

	//default constructor
	public PopulationInstance() {
		Random rand = new Random();
		moveRate = rand.nextInt(5);
		sightRadius = rand.nextInt(16);
		newBaseDist = rand.nextInt(10);
		shipsToBase = rand.nextInt(10);
	}

	//constructor
	public PopulationInstance(int move, int sight, int base, int ships) {
		moveRate = move;
		sightRadius = sight;
		newBaseDist = base;
		shipsToBase = ships;
	}
	
	//get the move rate
	public int getMoveRate() {
		return moveRate;
	}

	//set the move rate
	public void setMoveRate(int moveRate) {
		this.moveRate = moveRate;
	}

	//get the ship's viewable distance
	public int getSightRadius() {
		return sightRadius;
	}

	//set the move rate
	public void setSightRadius(int sightRadius) {
		this.sightRadius = sightRadius;
	}

	//get the minimum distance from the other bases
	public int getNewBaseDist() {
		return newBaseDist;
	}

	//set the minimum distance from the other bases
	public void setNewBaseDist(int newBaseDist) {
		this.newBaseDist = newBaseDist;
	}

	//do we keep ships and bases about equal?
	public int getshipsToBase() {
		return shipsToBase;
	}

	//set if we keep ships and bases equal
	public void setshipsToBase(int shipsToBase) {
		this.shipsToBase = shipsToBase;
	}
	
}
