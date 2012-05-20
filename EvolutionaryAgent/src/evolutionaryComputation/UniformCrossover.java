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


/**
 * This class implements a uniform crossover where each gene of two individuals
 * has 50% chance of reaching the offspring.
 *
 * @author Francisco Aisa Garcia
 */


public class UniformCrossover extends CrossoverType {

    // *************************************************************************
    //                                METHODS
    // *************************************************************************


    /**
     * Crossover phase. Mate two different Individuals.
     * @param male One individual.
     * @param female Another individual.
     * @param offspring Resulting individual. IT CAN'T BE a null pointer, it must
     * be a valid object.
     */
    public void crossover (Individual male, Individual female, Individual offspring) {
        int maleChromosomeSize = male.chromosomeSize ();
        int femaleChromosomeSize = female.chromosomeSize ();

        if (maleChromosomeSize != femaleChromosomeSize) {
            //Individual offspring = new IndividualV1 (false);
            for (int locus = 0; locus < maleChromosomeSize; ++locus) {
                double probability = Math.random ();

                if (probability > 0.5) {
                    offspring.setGene (locus, male.getGene (locus));
                }
                else {
                    offspring.setGene (locus, female.getGene (locus));
                }
            }
        }
    }
}
