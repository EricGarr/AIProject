package garr9903;

import java.util.Random;

public class PopulationInstance {
	int moveRate, sightRadius, newBaseDist, equalShips;

	public PopulationInstance() {
		Random rand = new Random();
		moveRate = rand.nextInt(5);
		sightRadius = rand.nextInt(16);
		newBaseDist = rand.nextInt(10);
		equalShips = rand.nextInt(2);
	}

	public PopulationInstance(int move, int sight, int base, int ships) {
		moveRate = move;
		sightRadius = sight;
		newBaseDist = base;
		equalShips = ships;
	}
	
	public int getMoveRate() {
		return moveRate;
	}

	public void setMoveRate(int moveRate) {
		this.moveRate = moveRate;
	}

	public int getSightRadius() {
		return sightRadius;
	}

	public void setSightRadius(int sightRadius) {
		this.sightRadius = sightRadius;
	}

	public int getNewBaseDist() {
		return newBaseDist;
	}

	public void setNewBaseDist(int newBaseDist) {
		this.newBaseDist = newBaseDist;
	}

	public int getEqualShips() {
		return equalShips;
	}

	public void setEqualShips(int equalShips) {
		this.equalShips = equalShips;
	}
	
}
