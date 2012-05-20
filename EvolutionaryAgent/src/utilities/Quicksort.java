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
 * Copyright © 2009-2010 Lars Vogel
 */

package utilities;

import evolutionaryComputation.Individual;


/**
 * @author Lars Vogel
 * @version 0.6
 */


public class Quicksort  {
    private static Individual [] numbers;
    private static int number;

    public static void sort(final Individual [] values) {
        // Check for empty or null array
        if (values == null || values.length == 0){
                return;
        }

        Quicksort.numbers = values;
        number = values.length;
        quicksort (0, number - 1);
    }

    private static void quicksort (int low, int high) {
        int i = low, j = high;
        // Get the pivot element from the middle of the list
        double pivot = numbers [low + (high-low)/2].fitness ();

        // Divide into two lists
        while (i <= j) {
            // If the current value from the left list is smaller then the pivot
            // element then get the next element from the left list
            while (numbers [i].fitness () < pivot) {
                    i++;
            }
            // If the current value from the right list is larger then the pivot
            // element then get the next element from the right list
            while (numbers [j].fitness () > pivot) {
                    j--;
            }

            // If we have found a values in the left list which is larger then
            // the pivot element and if we have found a value in the right list
            // which is smaller then the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                    exchange (i, j);
                    i++;
                    j--;
            }
        }
        // Recursion
        if (low < j)
            quicksort (low, j);
        if (i < high)
            quicksort (i, high);
    }

    private static void exchange (int i, int j) {
        Individual temp = numbers [i];
        numbers [i] = numbers [j];
        numbers [j] = temp;
    }
}