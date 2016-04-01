package garr9903;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

public class Learner{
	public static void main(String[] args){
		//create list to store each population
		ArrayList<PenaGarrisonPopulation> population = new ArrayList<PenaGarrisonPopulation>();
		//create list to store generation data
		ArrayList<Generation> generations = new ArrayList<Generation>();
		
		//initial generation
		int generationNum = 0;
		
		for(int i = 0; i < 15; i++){
			//read current generation
			XStream xstream = new XStream();
			xstream.alias("PenaGarrisonPopulation", PenaGarrisonPopulation.class);
			try { 
				population.add((PenaGarrisonPopulation) xstream.fromXML(new File("PenaGarrisonPopulation.xml")));
			} catch (XStreamException e) {
				// if you get an error, handle it other than a null pointer because
				// the error will happen the first time you run
				population.add(new PenaGarrisonPopulation());
			}
			
			double sum = 0;
			for(double score : population.get(generationNum).getFitness()){
				sum+=score;
			}
			sum = sum/population.get(generationNum).getPopSize();
			
			Generation generation = new Generation(sum, generationNum);
			generations.add(generation);
			
			generateNewPop(population.get(generationNum));
			
			generationNum++;
		}
	}

	private static void generateNewPop(PenaGarrisonPopulation pop){
		//sort fitness array by quicksort
		//modify order of population instance array simultaneously
		double[] fitness = pop.getFitness();
		
		PenaGarrisonPopulation newPop = new PenaGarrisonPopulation(pop.getPopSize()); 
		
		//tournament size 5
		int i = 0;
		while(i < newPop.getPopSize()){
			PopulationInstance parent1 = tournament(5, pop);
			PopulationInstance parent2 = tournament(5, pop);
			PopulationInstance[] children = crossover(parent1, parent2);
			newPop.add(i, children[0]);
			newPop.storeFitness(i++, 0);
			newPop.add(i, children[1]);
			newPop.storeFitness(i++, 0);
		}
	}

	private static PopulationInstance[] crossover(PopulationInstance parent1, PopulationInstance parent2) {
		PopulationInstance[] children = new PopulationInstance[2];
		PopulationInstance child1 = null;
		PopulationInstance child2 = null;
		Random rand = new Random();
		int crossPoint = rand.nextInt(3)+1;
		switch(crossPoint){
			case 1:
				child1 = new PopulationInstance(parent1.getMoveRate(), parent2.getSightRadius(), parent2.getNewBaseDist(), parent2.getEqualShips());
				child2 = new PopulationInstance(parent2.getMoveRate(), parent1.getSightRadius(), parent1.getNewBaseDist(), parent1.getEqualShips());
				break;
			case 2:
				child1 = new PopulationInstance(parent1.getMoveRate(), parent1.getSightRadius(), parent2.getNewBaseDist(), parent2.getEqualShips());
				child2 = new PopulationInstance(parent2.getMoveRate(), parent2.getSightRadius(), parent1.getNewBaseDist(), parent1.getEqualShips());
				break;
			case 3:
				child1 = new PopulationInstance(parent1.getMoveRate(), parent1.getSightRadius(), parent1.getNewBaseDist(), parent2.getEqualShips());
				child2 = new PopulationInstance(parent2.getMoveRate(), parent2.getSightRadius(), parent2.getNewBaseDist(), parent1.getEqualShips());
				break;
			default:
				child1 = new PopulationInstance();
				child2 = new PopulationInstance();
				break;
		}
		
		//mutate
		if(rand.nextDouble() < .001){
			int mutate = rand.nextInt(4);
			switch(mutate){
				case 1:
					child1.setMoveRate(rand.nextInt(5));
					break;
				case 2:
					child1.setSightRadius(rand.nextInt(16));
					break;
				case 3:
					child1.setNewBaseDist(rand.nextInt(10));
					break;
				default:
					if(child1.getEqualShips() == 0){
						child1.setEqualShips(1);
					} else {
						child1.setEqualShips(0);
					}
					break;
			}
		}
		if(rand.nextDouble() < .001){
			int mutate = rand.nextInt(4);
			switch(mutate){
				case 1:
					child2.setMoveRate(rand.nextInt(5));
					break;
				case 2:
					child2.setSightRadius(rand.nextInt(16));
					break;
				case 3:
					child2.setNewBaseDist(rand.nextInt(10));
					break;
				default:
					if(child2.getEqualShips() == 0){
						child2.setEqualShips(1);
					} else {
						child2.setEqualShips(0);
					}
					break;
			}
		}
		children[0] = child1;
		children[1] = child2;
		return children;
	}

	private static PopulationInstance tournament(int size, PenaGarrisonPopulation pop){
		Random rand = new Random();
		ArrayList<PopulationInstance> contestants = new ArrayList<PopulationInstance>();
		ArrayList<Double> scores = new ArrayList<Double>();
		ArrayList<Integer> selections = new ArrayList<Integer>();
		int i = 0;
		while(i < size){
			int j = rand.nextInt(pop.getPopSize());
			if(!(selections.contains(j))){
				selections.add(j);
				contestants.add(pop.getPopulationInstance(j));
				scores.add(pop.getFitness()[j]);
				i++;
			}
		}
		
		PopulationInstance bestMem = null;
		double bestScore = 0;
		for(Double d : scores){
			if(d > bestScore){
				bestMem = contestants.get(scores.indexOf(d));
				bestScore = d;
			}
		}
		return bestMem;
	}

}
