package garr9903;

import spacesettlers.simulator.Toroidal2DPhysics;

public class Population {
	private PopulationInstance[] population;
	private int currentPopulationCounter;
	private double[] fitnessScores;

	public Population(int populationSize) {
		// start at member zero
		currentPopulationCounter = 0;
		
		// make an empty population
		population = new PopulationInstance[populationSize];
		///generate new members of population.
		for (int i = 0; i < populationSize; i++) {
			population[i] = new PopulationInstance();
		}
		
		// make space for the fitness scores
		fitnessScores = new double[populationSize];
	}

	public PopulationInstance getPopulationInstance(int index) {
		return population[index];
	}
	
	/**
	 * Currently scores all members as zero (the student must implement this!)
	 * 
	 * @param space
	 */
	public void evaluateFitnessForCurrentMember(Toroidal2DPhysics space) {
		fitnessScores[currentPopulationCounter] = 0;
	}
	
}
