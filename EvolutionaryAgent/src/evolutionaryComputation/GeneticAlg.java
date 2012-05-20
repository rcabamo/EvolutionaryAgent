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


/**
 * This class defines the basic interface for any genetic algorithm to be defined.
 * The population is formed by Individuals (the reason why it's been done like this
 * is so we can experiment with different individuals and different fitness functions
 * easily). There is a CrossoverType object contained within this class to make it
 * easy to use different crossover techniques through the use of polymorphism.
 *
 * Currently the derived classes are responsible for choosing the type of individual
 * that they are going to used, but this is going to be changed in future versions
 * (so we can create a genetic algorithm selecting size of population, generations,
 * number of matches per bot, crossover technique, individual/size of chromosome,
 * fitness technique) to be more flexible.
 *
 * @author Francisco Aisa Garcia
 */


public abstract class GeneticAlg {

    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************


    /** Number of generations to be generated before the end of the algorithm */
    protected final int GENERATIONS;
    /** Current generation */
    protected int currentGeneration;
    /** Current population */
    protected Individual population [];
    /** Current individual to be tested */
    protected int currentIndividual;
    /** Each individual can play more than one match, this field indicates which
     * match is being played. IT MUST BE INITIALIZED IN THE DERIVED CLASSES */
    protected int currentMatch;
    /** It stores the chromosome size of the individual being used.
     IT MUST BE INITIALIZED IN THE DERIVED CLASSES */
    protected int chromosomeSize;
    /** It references a CrossoverType object. Any crossover method is meant to work with
     any individual, hence, using polymorphism we can try different crossover techniques
     without changing our original algorithm */
    protected CrossoverType crossoverType;


    // *************************************************************************
    //                                METHODS
    // *************************************************************************

    /**
     * Initializes the number of generations to execute and the size of the population
     * for each generation.
     * @param GENERATIONS Number of generations to execute.
     * @param populationSize Number of individuals per generation.
     * @param crossoverType Crossover technique to be applied.
     */
    @Deprecated
    protected GeneticAlg (int GENERATIONS, int populationSize, CrossoverType crossoverType) {
        this.GENERATIONS = GENERATIONS;
        currentGeneration = currentIndividual = 0;
        population = new Individual [populationSize];
        this.crossoverType = crossoverType;
    }

    //__________________________________________________________________________

    /**
     * Get the next individual that should be put to the test.
     * @return Next individual to be tested.
     */
    public Individual testIndividual () {
        return population [currentIndividual];
    }

    //__________________________________________________________________________

    /**
     * Retrieves the current individual.
     * @return Integer indicating which is the individual that we are currently
     * testing.
     */
    public int currentIndividual () {
        return currentIndividual;
    }

    //__________________________________________________________________________

    /**
     * Retrieves the current generation.
     * @return Integer indicating which is the current generation.
     */
    public int currentGeneration () {
        return currentGeneration;
    }

    //__________________________________________________________________________

    /**
     * Selection is meant to implement the selection phase of a given genetic algorithm.
     * NOTE that it has to contain the rest of the phases (crossover, mutation etc) because
     * this is the method that is going to be executed after the end of each generation to
     * generate a new one.
     */
    public abstract void selection ();

    //__________________________________________________________________________

    /**
     * Mutation phase.
     * Given a probability of mutation and the rate of mutation, this function
     * mutates the chromosome of a given individual accordingly.
     * @param offspring Individual that is going to suffer mutations.
     * @param chance Probability of mutation.
     * @param rate Rate of mutation. The number MUST BE between 0 and 1. For example
     * if "rate == 0.1", each gene that mutates, will mutate its value in + - 10%
     * of its original value.
     */
    public void mutation (Individual offspring, double chance, double rate) {
        for (int locus = 0; locus < chromosomeSize; ++locus) {
            double probability = Math.random ();

            if (probability < chance) {
                if (Math.random () < 0.5) {
                    offspring.setGene (locus, offspring.getGene (locus) + (int) (rate * offspring.getGene (locus)));
                }
                else {
                    int res = offspring.getGene (locus) - (int) (rate * offspring.getGene (locus));

                    // Avoid negative gene values
                    if (res < 0) {
                        offspring.setGene (locus, 0);
                    }
                    else {
                        offspring.setGene (locus, res);
                    }
                }
            }
        }
    }

    //__________________________________________________________________________

    /**
     * Load the genetic algorithm parameters from DB. The ideal situation to load
     * this data would be in the constructor of the derived class.
     * @param memory Data Base controller.
     * @return True if there was information stored. If false is returned, it means
     * that we have to build the first generation.
     */
    public abstract boolean load (Memoria memory);

    //__________________________________________________________________________

    /**
     * Store the information of the current match in DB. It should be executed
     * at the end of the match each time, so we can store information about the
     * results of the current individual.
     * @param memory Data Base controller.
     */
    public abstract void store (Memoria memory);
}
