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
 * This class is meant to hide all the details of a chromosome. Because it contains
 * an IndividualStats object, it can be combined with different fitness functions.
 *
 * @author Francisco Aisa Garcia
 */


public abstract class Individual {

    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************


    /** Individual chromosome */
    protected int chromosome [];
    /** Individual stats */
    protected IndividualStats stats; // Must be initialized in the derived classes


    // *************************************************************************
    //                                METHODS
    // *************************************************************************


    /**
     * Argument based constructor.
     * @param nGenes Size of the chromosome.
     * @param stats IndividualStats object, it is meant to facilitate the association
     * of an Individual with different stats and fitness techniques. Stats can't be
     * a NULL object.
     */
    public Individual (int nGenes, IndividualStats stats) {
        chromosome = new int [nGenes];
        this.stats = stats;
    }

    //__________________________________________________________________________

    /**
     * Set the value of a gene.
     * @param locus Position in the chromosome.
     * @param value Value to which we want to set the gene.
     */
    public void setGene (int locus, int value) {
        chromosome [locus] = value;
    }

    //__________________________________________________________________________

    /**
     * Get the value of a chromosome's gene.
     * @param locus Position of the gene in the chromosome.
     * @return The gene's value.
     */
    public int getGene (int locus) {
        return chromosome [locus];
    }

    //__________________________________________________________________________

    /** Chromosome's size */
    public int chromosomeSize () {
        return chromosome.length;
    }

    //__________________________________________________________________________

    /** Create an individual with random genes */
    public abstract void createRandomIndividual ();

    //__________________________________________________________________________

    /** Estimate fitness */
    public double fitness () {
        return stats.fitness ();
    }

    //__________________________________________________________________________

    /** Increment the number of kills */
    public void incrementKills () {
        stats.incrementKills ();
    }

    //__________________________________________________________________________

    /** Increment the number of deaths */
    public void incrementDeaths () {
        stats.incrementDeaths ();
    }

    //__________________________________________________________________________

    /** Get the number of kills */
    public int getKills () {
        return stats.getKills ();
    }

    //__________________________________________________________________________

    /**
     * Set the number of kills, it ONLY should be used by the DB
     * @param kills Number of kills to set
     */
    public void setKills (int kills) {
        stats.setKills (kills);
    }

    //__________________________________________________________________________

    /** Get the number of the deaths */
    public int getDeaths () {
        return stats.getDeaths ();
    }

    //__________________________________________________________________________

    /** Set the number of the deaths, it ONLY should be used by the DB */
    public void setDeaths (int deaths) {
        stats.setDeaths (deaths);
    }

    //__________________________________________________________________________

    /** Get the total amount of damage given */
    public int getTotalDamageGiven () {
        return stats.getTotalDamageGiven ();
    }

    //__________________________________________________________________________

    /**
     * Set the total amount of damage given, it ONLY should be used by the DB
     * @param damage Total amount of damage given
     */
    public void setTotalDamageGiven (int damage) {
        stats.setTotalDamageGiven (damage);
    }

    //__________________________________________________________________________

    /** Get the total amount of damage taken */
    public int getTotalDamageTaken () {
        return stats.getTotalDamageTaken ();
    }

    //__________________________________________________________________________

    /**
     * Set the total amount of damage taken, it ONLY should be used by the DB
     * @param damage Total amount of damage taken
     */
    public void setTotalDamageTaken (int damage) {
        stats.setTotalDamageTaken (damage);
    }

    //__________________________________________________________________________

    /**
     * Increment the total amount of damage given.
     * @param amount Amount to be incremented.
     */
    public void incrementDamageGiven (int amount) {
        stats.incrementDamageGiven (amount);
    }

    //__________________________________________________________________________

    /**
     * Increment the total amount of damage taken.
     * @param amount Amount to be incremented.
     */
    public void incrementDamageTaken (int amount) {
        stats.incrementDamageTaken (amount);
    }

    //__________________________________________________________________________

    /**
     * Resets the individual's temporary information about the match
     */
    public void resetStats () {
        stats.reset ();
    }
}

