package garr9903;

public class PopulationInstance {
	int moveRate, sightRadius, newBaseDist, equalShips;

	public PopulationInstance() {
		moveRate = 0;
		sightRadius = 0;
		newBaseDist = 2;
		equalShips = 1;
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
