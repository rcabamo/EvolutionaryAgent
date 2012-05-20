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

package utilities;

import evolutionaryComputation.Individual;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is used to create text documents with the results of the execution
 * of each generation. It depends on the class Individual.
 *
 * @author Francisco Aisa García
 */


public class Copycat {

    /**
     * It copies all the relevant information of a given population and it prints
     * it on an file.
     * @param generation Current generation to be printed.
     * @param population Each individual contained in the current generation.
     */
    public static void copyPopulation (final int generation, final Individual population []) {
        String fileName;
        File file;

        int k = 0;
        do {
            fileName = "Test" + k + "_Generation" + generation;
            file = new File (fileName);
            ++k;
        } while (file.exists ());

        FileWriter fileToWrite = null;
        PrintWriter pw = null;

        try {
            fileToWrite = new FileWriter (fileName);
            pw = new PrintWriter (fileToWrite);
        }
        catch (Exception e) {
            e.printStackTrace ();
        }

        for (int i = 0; i < population.length; ++i) {
            pw.print ("Individual = " + i);
            pw.print ("; Kills = " + population [i].getKills ());
            pw.print ("; Deaths = " + population [i].getDeaths ());
            pw.print ("; TotalDamageGiven = " + population [i].getTotalDamageGiven ());
            pw.print ("; TotalDamageTaken = " + population [i].getTotalDamageTaken ());
            pw.println ("; Fitness = " + population [i].fitness ());
        }

        try {
            if (fileToWrite != null) {
                fileToWrite.close ();
            }
        }
        catch (IOException ex) {
            Logger.getLogger (Copycat.class.getName ()).log (Level.SEVERE, null, ex);
        }
    }
}
