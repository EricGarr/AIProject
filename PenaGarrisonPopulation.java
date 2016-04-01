package garr9903;

import java.util.Random;

public class PenaGarrisonPopulation {
	//stores chromosomes for instance of population
	private PopulationInstance[] population;
	//stores fitness scores for population member
	private double[] fitnessScores;
	//stores the total size of the population 
	private int populationSize;
	//stores the current population to be checked
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
		currentPopMember = 0;
	}

	public PenaGarrisonPopulation() {
		populationSize = 30;
		// make an empty population
		population = new PopulationInstance[populationSize];
		for (int i = 0; i < populationSize; i++) {
			population[i] = new PopulationInstance();
		}
		
		// make space for the fitness scores
		fitnessScores = new double[populationSize];
		currentPopMember = 0;
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
	
	public int getPopSize(){
		return populationSize;
	}
	
	public double[] getFitness(){
		return fitnessScores;
	}
	
	public PopulationInstance[] getPop(){
		return population;
	}
}
