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

import utilities.Arithmetic;


/**
 * This class implements a small chromosome with lesser control over the weapon
 * selection. It is meant to accelerate the convergence of any given genetic algorithm
 * (less matches to be played).
 *
 * @author Francisco Aisa Garcia
 */


public class IndividualV1 extends Individual {

    // *************************************************************************
    //                                METHODS
    // *************************************************************************


    /** Create a chromosome with random gene values */
    public void createRandomIndividual () {

        // CLOSE, AVERAGE AND FAR DISTANCE

        chromosome [0] = Arithmetic.doRandomNumber (0, 1200);
        chromosome [1] = Arithmetic.doRandomNumber (chromosome [0], 2000);
        chromosome [2] = Arithmetic.doRandomNumber (chromosome [1], 2800);

        // ALLELES FOR WEAPON CHOICE. THERE ARE 12 GENES FOR EACH WEAPON (3 GENES PER
        // HEIGHT AND 4 HEIGHTS PER DISTANCE = 12 GENES).
        // THERE ARE 9 WEAPONS AND WE NEED ANOTHER 18 ADDITIONAL GENES FOR SPAM
        // AND NON ENEMY ON SIGHT BEHAVIORS WHICH MAKES UP TO:
        // 9*12 + 2*9 GENES MAKING A TOTAL OF = 126 GENES
        // EXAMPLE: SHIELD GUN
        // CLOSE - LOW, MIDDLE, HIGH ; AVERAGE - LOW, MIDDLE, HIGH ; FAR - LOW, MIDDLE, HIGH
        // WEAPONS ORDER: SHIELD GUN, ASSAULT RIFLE, BIO RIFLE, LINK GUN, MINIGUN
        //                FLAK CANNON, ROCKET LAUNCHER, SHOCK RIFLE, SNIPER RIFLE

        for (int locus = 3; locus < 12; ++locus) {
            chromosome [locus] = Arithmetic.doRandomNumber (0, 100);
        }

        // ALLELES FOR SKYNET DECISIONS

        // HEALTH ALLELES

        chromosome [12] = Arithmetic.doRandomNumber (0, 100);
        chromosome [13] = Arithmetic.doRandomNumber (chromosome [12], 160);

        // HEALTH RISK ALLELES

        chromosome [14] = Arithmetic.doRandomNumber (5, 30);
        chromosome [15] = Arithmetic.doRandomNumber (15, 80);
        chromosome [16] = Arithmetic.doRandomNumber (15, 60);
        chromosome [17] = Arithmetic.doRandomNumber (10, 120);
        chromosome [18] = Arithmetic.doRandomNumber (20, 100);

        // ELAPSED TIME

        chromosome [19] = Arithmetic.doRandomNumber (3, 9);

        // DESTINATION BASED ON ITEMS PRIORITY

        for (int locus = 20; locus < 26; ++locus) {
            chromosome [locus] = Arithmetic.doRandomNumber (0, 100);
        }
    }

    //__________________________________________________________________________

    /**
     * Argument based constructor.
     * @param initialize True if we want to generate random gene values.
     */
    public IndividualV1 (boolean initialize, IndividualStats stats) {
        super (26, stats);

        if (initialize) {
            createRandomIndividual ();
        }
    }
}
