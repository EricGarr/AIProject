package garr9903;

public class PenaGarrisonPopulation {
	private PopulationInstance[] population;
	private int[] fitnessScores;
	private int populationSize;

	public PenaGarrisonPopulation(int popSize) {
		populationSize = popSize;
		// make an empty population
		population = new PopulationInstance[populationSize];
		///generate new members of population.
		for (int i = 0; i < populationSize; i++) {
			population[i] = new PopulationInstance();
		}
		
		// make space for the fitness scores
		fitnessScores = new int[populationSize];
	}

	public PenaGarrisonPopulation() {
		populationSize = 18;
		// make an empty population
		population = new PopulationInstance[populationSize];
		///generate new members of population.
		for (int i = 0; i < populationSize; i++) {
			population[i] = new PopulationInstance();
		}
		
		// make space for the fitness scores
		fitnessScores = new int[populationSize];
	}

	public PopulationInstance getPopulationInstance(int index) {
		return population[index];
	}
	
	/**
	 * Currently scores all members as zero (the student must implement this!)
	 * 
	 * @param space
	 */
	public void storeFitness(int currentPopulationCounter, int score) {
		fitnessScores[currentPopulationCounter] = score;
	}
	
}
