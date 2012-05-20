/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright © 2011-2012 Francisco Aisa Garcia and Ricardo Caballero Moral
 */

package evolutionaryComputation;

import knowledge.Memoria;
import utilities.Arithmetic;
import utilities.Copycat;
import utilities.Quicksort;


/**
 * This class implements a genetic algorithm with a deterministic selection phase.
 *
 * @author Francisco Aisa Garcia
 */


public class DeterministicGenetic extends GeneticAlg {


    // *************************************************************************
    //                                METHODS
    // *************************************************************************


    /**
     * It initializes the deterministic genetic algorithm using the information stored in
     * the data base. If there isn't any information available, it generates the first
     * generation.
     * @param GENERATIONS Number of generations to be executed.
     * @param populationSize Size of the population for each generation.
     * @param crossoverType Kind of crossover to be applied.
     * @param memory Data Base controller.
     */
    @Deprecated
    public DeterministicGenetic (int GENERATIONS, int populationSize, CrossoverType crossoverType, Memoria memory) {
        super (GENERATIONS, populationSize, crossoverType);

        // Initialize each individual
        for (int i = 0; i < population.length; ++i) {
            // To try different chromosomes change the individual being initialized
            // To try different fitness techniques, change the IndividualStats object passed
            population [i] = new IndividualV1 (true, new NonLinearFitness ());
        }

        this.chromosomeSize = population [0].chromosomeSize ();

        // Load stored information in the data base. If no information is available
        // then initialize everything randomly
        boolean success = load (memory);

        if (success) {
            // If the next individual to evaluate is the first one of the next population
            // check out if the next generation is the last one.
            // If it is the last one, store the best individual, otherwise, create
            // the next generation based on selection, crossover and mutation

            if (currentIndividual >= population.length) {
                // Write down the results
                Copycat.copyPopulation (currentGeneration, population);

                currentGeneration = currentGeneration + 1;

                if (currentGeneration == GENERATIONS) {
                    // The execution has ended, select the best individual (the one
                    // with the biggest fitness) and store it as a result in the
                    // data base

                    // Look for the best inidividual
                    double max = population [0].fitness ();
                    int pos = 0;
                    for (int i = 1; i < population.length; ++i) {
                        if (population [i].fitness () >= max){
                            max = population [i].fitness ();
                            pos = i;
                        }
                    }

                    // Store the best individual
                    String tableName = population [pos].getClass ().getName ();
                    tableName = tableName.replace("evolutionaryComputation.", "");
                    memory.storeBestIndividuo (tableName, population [pos], false);

                    // Restart execution
                    currentGeneration = 0;
                    currentIndividual = 0;
                    currentMatch = 0;

                    for (int i = 0; i < population.length; ++i) {
                        population [i].createRandomIndividual ();
                    }
                }
                else {
                    // Reset the next individual to be evaluated
                    currentIndividual = 0;
                    currentMatch = 0;

                    // Selection, Crossover and Mutation
                    selection ();
                }
            }
        }
        else {
            currentGeneration = 0;
            currentIndividual = 0;
            currentMatch = 0;

            for (int i = 0; i < population.length; ++i) {
                population [i].createRandomIndividual ();
            }
        }
    }

    //__________________________________________________________________________

    /** Select the best Individuals for the next population, mate them and mutate them */
    @Deprecated
    public void selection () {
        // Sort the individuals based on their fitness
        Quicksort.sort (population);

        // Create the new population
        Individual [] newPopulation = new Individual [population.length];

        // Since we are using Elitism, we will keep the best four individuals (the
        // last four in the population array)

        newPopulation [0] = population [population.length - 1];
        newPopulation [0].resetStats ();
        newPopulation [1] = population [population.length - 2];
        newPopulation [1].resetStats ();
        newPopulation [2] = population [population.length - 3];
        newPopulation [2].resetStats ();
        newPopulation [3] = population [population.length - 4];
        newPopulation [3].resetStats ();

        // Mate the best candidates

        double chanceOfMutation = 1/chromosomeSize;

        // First children
        int randomIndividual = 0;
        randomIndividual = Arithmetic.doRandomNumber (1, 3);

        // Crossover between the best individual and one of the other three best individuals
        newPopulation [4] = new IndividualV1 (false, new NonLinearFitness ());
        crossoverType.crossover (newPopulation [0], newPopulation [randomIndividual], newPopulation [4]);
        // Mutate the offspring
        mutation (newPopulation [4], chanceOfMutation, 0.1);

        // Second children
        double probability = Math.random ();
        if (probability < 0.33) {
            randomIndividual = 0;
        }
        else {
            randomIndividual = Arithmetic.doRandomNumber (2, 3);
        }

        // Crossover between the second best individual and one of the other three best individuals
        newPopulation [5] = new IndividualV1 (false, new NonLinearFitness ());
        crossoverType.crossover (newPopulation [1], newPopulation [randomIndividual], newPopulation [5]);
        // Mutate the offspring
        mutation (newPopulation [5], chanceOfMutation, 0.1);

        // Mate randomly the following 25 best individuals

        for (int individual = population.length - 7, nextOffspring = 6; individual > 3; --individual, ++nextOffspring) {
            do {
                randomIndividual = Arithmetic.doRandomNumber (0, 29);
            } while (randomIndividual != individual);

            newPopulation [nextOffspring] = new IndividualV1 (false, new NonLinearFitness ());
            crossoverType.crossover (population [individual], population [randomIndividual], newPopulation [nextOffspring]);
            mutation (newPopulation [nextOffspring], chanceOfMutation, 0.1);
        }

        // We will forget about the worst four individuals, creating four new
        // random individuals in their place

        newPopulation [newPopulation.length - 1] = new IndividualV1 (true, new NonLinearFitness ());
        newPopulation [newPopulation.length - 2] = new IndividualV1 (true, new NonLinearFitness ());
        newPopulation [newPopulation.length - 3] = new IndividualV1 (true, new NonLinearFitness ());
        newPopulation [newPopulation.length - 4] = new IndividualV1 (true, new NonLinearFitness ());

        population = newPopulation;
    }



    //__________________________________________________________________________

    /**
     * Load all the genetic algorithm information stored in the data base.
     * @param memory Data Base controller.
     */
    public boolean load (Memoria memory) {
        boolean success = memory.loadPoblacion (population, population [0].chromosomeSize ());

        currentGeneration = memory.loadGeneration ();
        currentIndividual = memory.loadCurrent ();
        currentMatch = memory.loadIteration ();

        return success;
    }

    //__________________________________________________________________________

    /**
     * Store all the genetic algorithm information in the data base.
     * @param memory Data Base controller.
     */
    public void store (Memoria memory) {
        memory.storeGenes (currentIndividual, currentGeneration, currentMatch, population);
    }
}
