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
 * This class is meant to facilitate trials with different crossover techniques.
 *
 * @author Francisco Aisa Garcia
 */


public abstract class CrossoverType {

    /**
     * Given two individuals, this function produces a new offspring. The derived
     * classes are responsible for the behavior of this method.
     * @param male One individual to be mixed.
     * @param female Other individual to be mixed.
     * @param offspring Resulting individual. IT CAN'T BE a null pointer. "offspring"
     * must be a valid object.
     */
    public abstract void crossover (Individual male, Individual female, Individual offspring);
}
