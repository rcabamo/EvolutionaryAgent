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

import java.util.Random;
import knowledge.Memoria;
import utilities.Arithmetic;
import utilities.Copycat;


/**
 * A genetic algorithm with a roulette wheel selection phase.
 *
 * @author Francisco Aisa García
 */


public class RouletteWheel extends GeneticAlg {

    // *************************************************************************
    //                                METHODS
    // *************************************************************************

    /**
     * It initializes the genetic algorithm using the information stored in
     * the data base. If there isn't any information available, it generates the first
     * generation.
     * @param GENERATIONS Number of generations to be executed.
     * @param populationSize Size of the population for each generation.
     * @param crossoverType Kind of crossover to be applied.
     * @param memory Data Base controller.
     */
    @Deprecated
    public RouletteWheel (int GENERATIONS, int populationSize, CrossoverType crossoverType, Memoria memory) {
        super (GENERATIONS, populationSize, crossoverType);

        // Initialize each individual
        for (int i = 0; i < population.length; ++i) {
            // To try different chromosomes change the individual being initialized
            // To try different fitness techniques, change the IndividualStats object passed
            population [i] = new IndividualV1 (true, new ProportionalFitness ());
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
    public void selection() {
        int chances [] = new int [population.length];

        chances [0] = (int) (Math.round(population [0].fitness() * 10));
        for (int i = 1; i < population.length; ++i) {
            chances [i] = chances [i - 1] + (int) (Math.round(population [i].fitness() * 10));
        }

        // Create the new population
        Individual [] newPopulation = new Individual [population.length];

        // Create the new population
        for (int i = 0; i <population.length; ++i) {
            // Select by statistics an individual to survive
            int dice = Arithmetic.doRandomNumber (0, chances [population.length - 1]);

            boolean found = false;
            int survivorA = 0;
            for (int k = 0; k < population.length && !found; ++k) {
                if (dice <= chances [k]) {
                    survivorA = k;
                    found = true;
                }
            }

            // With 90% chance the selected individual will achieve reproduction
            Random generator = new Random();
            if (generator.nextDouble() < 0.9) {
                double chanceOfMutation = 1.0 / chromosomeSize;

                // Get the other chromosome involved in reproduction by statistics
                int survivorB;
                boolean differentIndividual = true;
                do {
                    dice = Arithmetic.doRandomNumber (0, chances [population.length - 1]);

                    survivorB = 0;
                    found = false;
                    for (int k = 0; k < population.length && !found; ++k) {
                        if (dice <= chances [k]) {
                            if (k != survivorA) {
                                survivorB = k;
                                found = true;
                                differentIndividual = true;
                            }
                            else {
                                differentIndividual = false;
                                found = true;
                            }
                        }
                    }
                } while (!differentIndividual);

                // Crossover and mutation
                newPopulation [i] = new IndividualV1 (false, new ProportionalFitness ());
                crossoverType.crossover (population [survivorA], population [survivorB], newPopulation [i]);
                mutation (newPopulation [i], chanceOfMutation, 0.1);
            }
            else {
                // Note that we actually create a new individual, because several
                // suvivors can be in the next generations (the same survivors).
                // Copying references can create trouble with individual stats.
                newPopulation [i] = new IndividualV1 (false, new ProportionalFitness ());

                // Copy the chromosome values
                for (int k = 0; k < population [survivorA].chromosomeSize (); ++k) {
                    newPopulation [i].setGene(k, population [survivorA].getGene(k));
                }
            }
        }

        population = newPopulation;
    }

    //__________________________________________________________________________

    /**
     * Load all the genetic algorithm information stored in the data base.
     * @param memory Data Base controller.
     */
    public boolean load(Memoria memory) {
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
    public void store(Memoria memory) {
        memory.storeGenes (currentIndividual, currentGeneration, currentMatch, population);
    }
}
