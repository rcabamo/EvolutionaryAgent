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
 * This class should contain statistics about an individual's match. That information
 * is to be used to estimate the fitness. Each derived class must implement a new
 * fitness function.
 *
 * @author Francisco Aisa Garcia
 */

/*
 * Although this class stores some other additional info, it's main purpose is to
 * facilitate trials with different fitness and invididuals (different chromosomes).
 * That is the main reason why the derived classes should have names of fitness techniques
 * (to allow clients to know rapidly which kind of fitness they are using).
 */
public abstract class IndividualStats {

    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************


    /** Number of times the individual has killed */
    protected int kills;
    /** Number of times the individual has died */
    protected int deaths;
    /** Total amount of damage the individual has given to the enemy */
    protected int totalDamageGiven;
    /** Total amount of damage the individual has received from the enemy */
    protected int totalDamageTaken;


    // *************************************************************************
    //                                METHODS
    // *************************************************************************

    /**
     * Initialize all stats to 0 (although this is done by java, we've done it
     * explicitly for the sake of simplicity.
     */
    IndividualStats () {
        kills = deaths = totalDamageGiven = totalDamageTaken = 0;
    }

    //__________________________________________________________________________

    /** Estimate fitness */
    public abstract double fitness ();

    //__________________________________________________________________________

    /**
     * Increment the total amount of damage given.
     * @param amount Amount to be incremented.
     */
    public void incrementDamageGiven (int amount) {
        totalDamageGiven = totalDamageGiven + amount;
    }

    //__________________________________________________________________________

    /**
     * Increment the total amount of damage taken.
     * @param amount Amount to be incremented.
     */
    public void incrementDamageTaken (int amount) {
        totalDamageTaken = totalDamageTaken + amount;
    }

    //__________________________________________________________________________

    /** Increment the number of kills */
    public void incrementKills () {
        kills = kills + 1;
    }

    //__________________________________________________________________________

    /** Increment the number of deaths */
    public void incrementDeaths () {
        deaths = deaths + 1;
    }

    //__________________________________________________________________________

    /** Get the total amount of damage taken */
    public int getTotalDamageTaken () {
        return totalDamageTaken;
    }

    //__________________________________________________________________________

    /** Get the total amount of damage given */
    public int getTotalDamageGiven () {
        return totalDamageGiven;
    }

    //__________________________________________________________________________

    /** Get the number of the deaths */
    public int getDeaths () {
        return deaths;
    }

    //__________________________________________________________________________

    /** Get the number of kills */
    public int getKills () {
        return kills;
    }

    //__________________________________________________________________________

    /**
     * Resets the individual's temporary information about the match
     */
    public void reset () {
        kills = deaths = totalDamageGiven = totalDamageTaken = 0;
    }

    //__________________________________________________________________________

    /**
     * Set the total amount of damage taken, it ONLY should be used by the DB
     * @param damage Total amount of damage taken
     */
    void setTotalDamageTaken (int damage) {
        totalDamageTaken = damage;
    }

    //__________________________________________________________________________

    /**
     * Set the total amount of damage given, it ONLY should be used by the DB
     * @param damage Total amount of damage given
     */
    void setTotalDamageGiven (int damage) {
        totalDamageGiven = damage;
    }

    //__________________________________________________________________________

    /**
     * Set the number of kills, it ONLY should be used by the DB
     * @param kills Number of kills to set
     */
    void setKills (int kills) {
        this.kills = kills;
    }

    //__________________________________________________________________________

    /** Set the number of the deaths, it ONLY should be used by the DB */
    void setDeaths (int deaths) {
        this.deaths = deaths;
    }
}
