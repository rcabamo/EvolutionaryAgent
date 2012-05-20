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
 * Proportional fitness estimation.
 *
 * @author Francisco Aisa García
 */


public class ProportionalFitness extends IndividualStats {

    // *************************************************************************
    //                                METHODS
    // *************************************************************************

    /** Estimate fitness */
    public double fitness () {
        double balance = kills - deaths;

        double fitnessValue = 0;
        // If we were killed more than we killed, then fitness is the killing ratio
        if (balance < 0) {
            fitnessValue = (kills * 1.0) / (deaths * 1.0);
        }
        else {
            fitnessValue = balance + 1;
        }

        return fitnessValue;
    }
}
