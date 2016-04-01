package garr9903;

import java.util.Random;

public class PenaGarrisonPopulation {
	private PopulationInstance[] population;
	private double[] fitnessScores;
	private int populationSize;
	private int currentPopMember;

	public PenaGarrisonPopulation(int popSize) {
		populationSize = popSize;
		// make an empty population
		population = new PopulationInstance[populationSize];
		///generate new members of population.
		for (int i = 0; i < populationSize; i++) {
			population[i] = new PopulationInstance();
		}
		
		// make space for the fitness scores
		fitnessScores = new double[populationSize];
	}

	public PenaGarrisonPopulation() {
		populationSize = 100;
		// make an empty population
		population = new PopulationInstance[populationSize];
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
	public void storeFitness(int currentPopulationCounter, double score) {
		fitnessScores[currentPopulationCounter] = score;
	}
	
	public void add(int index, PopulationInstance p){
		population[index] = p;
	}
	
	public int getCurrentPopMember(){
		return currentPopMember;
	}
	
	public void incrementCurrentPopMember(){
		currentPopMember++;
	}
}
