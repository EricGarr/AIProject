package garr9903;

public class Generation {
	double avgScore;  //stores the average scores for a generation
	int genNum;  //stores how many generations the program has gone through
	
	//constructor
	public Generation(double sum, int generationNum) {
		avgScore = sum;
		genNum = generationNum;
	}

}
