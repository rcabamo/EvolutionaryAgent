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
 * This class implements a non-linear fitness that focuses on benefiting those
 * individuals that make more kills (and rarely killed).
 * Note that because this class implements a fitness that is not linear, it shouldn't
 * be used in combination with a selection scheme like roulette-wheel selection.
 *
 * @author Francisco Aisa Garcia
 */


public class NonLinearFitness extends IndividualStats {

    // *************************************************************************
    //                                METHODS
    // *************************************************************************


    /** Estimate fitness */
    public double fitness () {
        double balance = kills - deaths;

        double fitnessValue = 0;
        if (balance < 0) {
            if (kills == 0) {
                fitnessValue = 1.0 / (deaths * 1.0);
            }
            else {
                fitnessValue = (kills * 1.0) / (deaths * 1.0);
            }
        }
        else if (balance == 0) {
            if (totalDamageTaken == 0) {
                fitnessValue = 1.5;
            }
            else {
                fitnessValue = 1.5 + ((totalDamageGiven * 1.0) / (totalDamageTaken * 1.0));
            }
        }
        else if (balance == 1) {
            if (totalDamageTaken == 0) {
                fitnessValue = 3.0;
            }
            else {
                fitnessValue = 3.0 + ((totalDamageGiven * 1.0) / (totalDamageTaken * 1.0));
            }
        }
        else {
            if (totalDamageTaken == 0) {
                fitnessValue = (2.0 * kills) - (deaths * 1.0);
            }
            else {
                fitnessValue = (2.0 * kills) - (deaths * 1.0) + ((totalDamageGiven * 1.0) / (totalDamageTaken * 1.0));
            }
        }

        return fitnessValue;
    }
}
