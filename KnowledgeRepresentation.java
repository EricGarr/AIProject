package garr9903;

import java.util.ArrayList;

import spacesettlers.utilities.Position;

public class KnowledgeRepresentation {
	ArrayList<Position> stuckArrList;
	boolean stuck = false;
	boolean chasing = true;
	
	public ArrayList<Position> getStuckArrList() {
		return stuckArrList;
	}

	public void add(Position pos) {
		if(stuckArrList.size() == 15) {
			stuckArrList.remove(0);
			stuckArrList.add(pos);
		}
		else {
			stuckArrList.add(pos);
		}
	}
	
//	private void amStuck(int xSum, int ySum) {
//		if (xSum ) {
//			System.out.println("I'm Stuck!");
//		}
//	}
	
	public boolean checkStuck() {
		int xSum = 0;
		int ySum = 0;
		int tallyX = 0;
		int tallyY = 0;
		getAverage(xSum, ySum);
		
		for(int i = 0; i < stuckArrList.size(); i++) {
			if(Math.abs((int)stuckArrList.get(i).getX() - xSum) < 30) {
				tallyX++;
			}
			if(Math.abs((int)stuckArrList.get(i).getY() - ySum) < 30) {
				tallyY++;
			}
		}
		if(tallyX == stuckArrList.size() && tallyY == stuckArrList.size()) {
			stuck = true;
		}
		return stuck;
	}
	
	public void getAverage(int xSum, int ySum) {
		
		for(Position pos : stuckArrList) {
			xSum += (int) pos.getX();
			ySum += (int) pos.getY();
		}
		xSum = xSum/stuckArrList.size();
		ySum = ySum/stuckArrList.size();
	}
	
	public boolean checkChase() {
		int vel = 0;
		int tallyVel = 0;
		getAvgVel(vel);
		
		for(int i = 0; i < stuckArrList.size(); i++) {
			if(Math.abs((int)stuckArrList.get(i).getxVelocity() - vel) < 2) {
				//System.out.println((int)stuckArrList.get(i).getxVelocity());
				tallyVel++;
			}
		}
		if(tallyVel == stuckArrList.size()) {
			chasing = true;
		}
		return chasing;
	}
	
	private void getAvgVel(int vel) {
		for(Position pos : stuckArrList) {
			vel += (int)pos.getxVelocity();
		}
		vel = vel/stuckArrList.size();
	}
	
	public void printList() {
		for(Position pos : stuckArrList) {
			System.out.println(pos.toString());
		}
	}
	
	
	
	public KnowledgeRepresentation() {
		stuckArrList = new ArrayList<Position>(15);
	}
}