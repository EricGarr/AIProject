package garr9903;

public class PenaGarrisonPopulation {
	//stores chromosomes for instance of population
	private PopulationInstance[] population;
	//stores fitness scores for population member
	private double[] fitnessScores;
	//stores the total size of the population 
	private int populationSize;
	//stores the current population to be checked
	private int currentPopMember;
	//stores the number of games a member has been involved in
	private int games;
	
	public PenaGarrisonPopulation(int popSize) {
		populationSize = popSize;
		// make an empty population
		population = new PopulationInstance[populationSize];
		// make space for the fitness scores
		fitnessScores = new double[populationSize];
		currentPopMember = 0;
		games = 0;
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
		games = 0;
	}

	//get specific member of population
	public PopulationInstance getPopulationInstance(int index) {
		return population[index];
	}
	
	//store score for fitness 
	public void storeFitness(int currentPopulationCounter, double score) {
		fitnessScores[currentPopulationCounter] = score;
	}
	
	//add population member
	public void add(int index, PopulationInstance p){
		population[index] = p;
	}
	
	//get the current population member
	//used to determine who is being run
	public int getCurrentPopMember(){
		return currentPopMember;
	}
	
	//increment the current population member
	//used at shutdown for next run
	public void incrementCurrentPopMember(){
		currentPopMember++;
	}
	
	//set the current population member
	//used to ensure ladder runs best options
	public void setCurrentPopMember(int currPop){
		currentPopMember = currPop;
	}
	
	//get the current population member
	//used to determine who is being run
	public int getGames(){
		return games;
	}
	
	//increment the current population member
	//used at shutdown for next run
	public void incrementGames(){
		games++;
	}
	
	//set the current population member
	//used to ensure ladder runs best options
	public void setGame(int game){
		games = game;
	}
	
	//get the current size of the population
	//we're using n=30 at time of writing this
	public int getPopSize(){
		return populationSize;
	}
	
	//used by learning class to determine best options
	//also used to sort final population
	public double[] getFitness(){
		return fitnessScores;
	}
	
	public double getScore(int popMem){
		return fitnessScores[popMem];
	}
	
	//used by learning class to seed new generations
	//also used by learning class to give ladder the best chance
	public PopulationInstance[] getPop(){
		return population;
	}
}
