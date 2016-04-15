package garr9903;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
		
		/*File file = new File("src/garr9903/PenaGarrisonPopulation.xml");
		System.out.println(file.getAbsolutePath());
		/**/
		
		for(int i = 0; i < 20; i++){
			//read current generation
			XStream xstream = new XStream();
			xstream.alias("PenaGarrisonPopulation", PenaGarrisonPopulation.class);
			PenaGarrisonPopulation oldPop = null;
			try {
				File file = new File("src/garr9903/PenaGarrisonPopulation.xml");
				oldPop = (PenaGarrisonPopulation) xstream.fromXML(file);
			} catch (XStreamException e) {
				// if you get an error, handle it somehow as it means your knowledge didn't save
				// the error will happen the first time you run
				System.out.println("Error in learning while importing old population (xstream)");
			}
			//store the current generation
			population.add(oldPop);
			
			//get the average score for the generation
			double sum = 0;
			double[] fitness = population.get(generationNum).getFitness();
			for(int j = 0; j < population.get(generationNum).getPopSize(); j++){
				//sum the scores
				sum += fitness[i];
			}
			//calculate the average score
			sum = sum/population.get(generationNum).getPopSize();
			System.out.println("The current generation is: " + generationNum + "\n" +
					"and its score is : " + sum);
			
			//store the score for the generation
			Generation generation = new Generation(sum, generationNum);
			generations.add(generation);
			
			//sort the old generation
			//quickSort(0, population.get(generationNum).getPopSize()-1, population.get(generationNum));
			
			//increment generation
			generationNum++;
			
			//create new population
			System.out.println("Making generation: " + generationNum);
			PenaGarrisonPopulation newPop = generateNewPop(population.get(generationNum-1), generationNum);
			
			//store the new population
			try { 
				// if you want to compress the file, change FileOuputStream to a GZIPOutputStream
				xstream.toXML(newPop, new FileOutputStream(new File("src/garr9903/PenaGarrisonPopulation.xml")));
			} catch (XStreamException e) {
				// if you get an error, handle it somehow as it means your knowledge didn't save
				// the error will happen the first time you run
				System.out.println("Error in learning while exporting new population (xstream)");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("Error in learning while exporting new population (IO)");
			}
			System.out.println("Testing new generation");
			//Run New Population
			try{
				Runtime rt = Runtime.getRuntime();
				String[] command = {"ant", "coopLadder"};
				Process runGeneration = rt.exec(command);
				runGeneration.waitFor();
			}catch(Exception e){
				System.out.println(e);
			}
			System.out.println("Generation done");
		}
		//get last generation
		PenaGarrisonPopulation finalPop = population.get(population.size()-1);
		//sort last generation (DECENDING order)
		//quickSort(0,finalPop.getPopSize()-1,finalPop);
		//best population member should be the first one
		finalPop.setCurrentPopMember(0);
		//store the final population;
		XStream xstream = new XStream();
		xstream.alias("Population", PenaGarrisonPopulation.class);
		xstream.alias("Generation", Generation.class);
		try{
			xstream.toXML(population.get(generationNum-1), new FileOutputStream(new File("src/garr9903/PenaGarrisonPopulation.xml")));
		} catch (XStreamException e){
			System.out.println(e);
		} catch (FileNotFoundException e){
			System.out.println(e);
		}

		//store the learning results
		try { 
			// if you want to compress the file, change FileOuputStream to a GZIPOutputStream
			xstream.toXML(generations, new FileOutputStream(new File("src/garr9903/GenerationKnowledge.xml"),true));
			xstream.toXML(population, new FileOutputStream(new File("src/garr9903/GenerationKnowledge.xml"),true));
		} catch (XStreamException e){
			System.out.println(e);
		} catch (FileNotFoundException e){
			System.out.println(e);
		}
	}

	private static void quickSort(int lowerBound, int upperBound, PenaGarrisonPopulation finalPop) {
		//sorts into DECENDING order
		int i = lowerBound;
		System.out.println("i:" + i + " value:" + finalPop.getFitness()[i]);
        int j = upperBound;
        System.out.println("j:" + j + " value:" + finalPop.getFitness()[j]);
        // calculate pivot number
        double pivot = finalPop.getFitness()[(i+j)/2];
        System.out.println("pivot:" + pivot);
        // Divide into two arrays
        while(i <= j) {
        	//find the first member less than pivot on "left"
            while(finalPop.getFitness()[i] > pivot) {
                i++;
            }
            //find the first member greater than pivot on the "right"
            while(finalPop.getFitness()[j] < pivot) {
                j--;
            }
            if (i <= j) {
            	//swap the population members and their scores
            	double temp = finalPop.getFitness()[j];
            	PopulationInstance tempPop = finalPop.getPop()[j];
            	finalPop.getFitness()[j] = finalPop.getFitness()[i];
            	finalPop.getPop()[j] = finalPop.getPop()[i];
            	finalPop.getFitness()[i] = temp;
            	finalPop.getPop()[i] = tempPop;
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        //call quickSort() method recursively
        //not sure why, but this line never worked...
        //somehow it would start trying to sort with lowerBound = 0, j = -1
        if (lowerBound < j)
        	System.out.println("lower sorting");
            quickSort(lowerBound, j, finalPop);
        if (i < upperBound)
        	System.out.println("upper sorting");
            quickSort(i, upperBound, finalPop);
		
	}

	private static PenaGarrisonPopulation generateNewPop(PenaGarrisonPopulation pop, int gNum){
		PenaGarrisonPopulation newPop = new PenaGarrisonPopulation(pop.getPopSize()); 
		
		//tournament size 5
		int i = 0;
		while(i < newPop.getPopSize()){
			//select parent 1
			PopulationInstance parent1 = tournament(10, pop);
			//select parent 2
			PopulationInstance parent2 = tournament(10, pop);
			//create 2 children from parents
			PopulationInstance[] children = crossover(parent1, parent2, gNum);
			//store the children
			newPop.add(i, children[0]);
			newPop.storeFitness(i++, 0);
			newPop.add(i, children[1]);
			newPop.storeFitness(i++, 0);
		}
		
		return newPop;
	}

	private static PopulationInstance[] crossover(PopulationInstance parent1, PopulationInstance parent2, int gNum) {
		PopulationInstance[] children = new PopulationInstance[2];
		PopulationInstance child1 = null;
		PopulationInstance child2 = null;
		Random rand = new Random();
		//get crossover point between 1 and 4
		int crossPoint = rand.nextInt(3)+1;
		switch(crossPoint){
			case 1:
				child1 = new PopulationInstance(parent1.getMoveRate(), parent2.getSightRadius(), parent2.getNewBaseDist(), parent2.getshipsToBase());
				child2 = new PopulationInstance(parent2.getMoveRate(), parent1.getSightRadius(), parent1.getNewBaseDist(), parent1.getshipsToBase());
				break;
			case 2:
				child1 = new PopulationInstance(parent1.getMoveRate(), parent1.getSightRadius(), parent2.getNewBaseDist(), parent2.getshipsToBase());
				child2 = new PopulationInstance(parent2.getMoveRate(), parent2.getSightRadius(), parent1.getNewBaseDist(), parent1.getshipsToBase());
				break;
			case 3:
				child1 = new PopulationInstance(parent1.getMoveRate(), parent1.getSightRadius(), parent1.getNewBaseDist(), parent2.getshipsToBase());
				child2 = new PopulationInstance(parent2.getMoveRate(), parent2.getSightRadius(), parent2.getNewBaseDist(), parent1.getshipsToBase());
				break;
			default:
				child1 = new PopulationInstance();
				child2 = new PopulationInstance();
				break;
		}
		
		//mutate
		if(rand.nextDouble() < (.1/gNum)){
			//get mutation bit between 0 and 3
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
					child1.setshipsToBase(rand.nextInt(10));
					break;
			}
		}
		if(rand.nextDouble() < .01){
			//get mutation bit between 0 and 3
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
					child1.setshipsToBase(rand.nextInt(10));
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
			//Select 5 unique population members
			int j = rand.nextInt(pop.getPopSize());
			if(!(selections.contains(j))){
				//member is unique, store it
				selections.add(j);
				contestants.add(pop.getPopulationInstance(j));
				scores.add(pop.getFitness()[j]);
				i++;
			}
		}
		
		//select best of the 5
		PopulationInstance bestMem = null;
		double bestScore = 0;
		for(Double d : scores){
			if(d > bestScore){
				bestMem = contestants.get(scores.indexOf(d));
				bestScore = d;
			}
		}
		//return the best
		return bestMem;
	}

}
