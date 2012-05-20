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

package knowledge;

import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import evolutionaryComputation.*;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import utilities.Pair;


/**
 *
 * @author Ricardo Caballero Moral
 *
 */


public class Memoria {

    /** DB's name. */
    private static String BDNAME = "Memoria.db";
    /** Debug mode. */
    private static Boolean DEBUG = false;

    /** Contains all the weapons related to a certain level. */
    public static Map <String, Pair<String, Boolean> > weapon = new HashMap<String, Pair <String, Boolean> > ();
    /** Contains all the shield items related to a certain level. */
    public static Map <String, Pair<String, Boolean>> armor = new HashMap<String, Pair<String, Boolean> > ();
    /** Contains all the health items related to a certain level. */
    public static Map <String, Set <String> > health = new HashMap<String, Set <String> > ();
    /** Contains all the ammo items related to a certain level. */
    public static Map <String, Set <String> > ammo = new HashMap<String, Set <String> > ();
    /** Contains all the adrenaline items related to a certain level. */
    public static Set <String> adrenaline = new HashSet<String>();

     /** Default Constructor. */
    public Memoria () {

        loadObject(false, false, 0, false);

    }

    /** Argument based constructor.
      * @param tablaItems Create table Items?.
      * @param tablaGenetico Create table Genetico?.
      * @param nGenes Number of genes.
      * @param tablaAuxiliar Create table Auxiliar?
     */
    public Memoria (boolean tablaItems, boolean tablaGenetico, int nGenes, boolean tablaAuxiliar) {

        loadObject(tablaItems, tablaGenetico, nGenes, tablaAuxiliar);

    }

    /** Private function to load an object of this class.
     * @param createTables Create table Items?
     * @param tablaGenetico Create table Genetico?
     * @param nGenes Number of genes.
     * @param tablaAuxiliar Create table Auxiliar?
     */
    private void loadObject(boolean tablaItems, boolean tablaGenetico, int nGenes, boolean tablaAuxiliar){

        Connection conn = null;

        if(DEBUG == true){
            System.out.println("Start function loadObject()");
            System.out.println("CREATE TABLE Item (id char(50) not null, tipo char(15) not null, nombre char(15) not null, mapa char(20) not null" + ", x double not null, y double not null, z double not null" + ",  primary key(mapa, x, y, z) )");
        }

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);
            Statement stat = conn.createStatement();

            if (tablaItems == true) {
                stat.execute("DROP TABLE IF EXISTS Item;");
            }
            if (tablaGenetico == true) {
                stat.execute("DROP TABLE IF EXISTS Genetico;");
            }
            if (tablaAuxiliar == true) {
                stat.execute("DROP TABLE IF EXISTS Auxiliar;");
            }

            stat.execute("CREATE TABLE Item (id char(50) not null, tipo char(15) not null, nombre char(15) not null, mapa char(20) not null,  primary key(id, mapa) )");
            String genetico = "CREATE TABLE Genetico (posicion int not null, generacion int not null, deaths int not null, kills int not null, totalDamageGiven int not null, totalDamageTaken int not null, current int not null";
            for (int i=0; i<nGenes; ++i){
                genetico = genetico.concat(",chromosome" +i+ " int not null");
            }
            genetico = genetico.concat(");");
            stat.execute(genetico);

            String auxiliar = "CREATE TABLE Auxiliar (posicion int not null, deaths int not null, kills int not null, totalDamageGiven int not null, totalDamageTaken int not null";
            for (int i=0; i<nGenes; ++i){
                auxiliar = auxiliar.concat(",chromosome" +i+ " int not null");
            }
            auxiliar = auxiliar.concat(");");
            stat.execute(auxiliar);

            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR EN LA CREACIÓN DE LAS TABLAS");
            e.printStackTrace(System.out);
        }
    }

    /** Create a special table to store the best individuals of the genetic algorithm.
     * @param nombreTabla Name of the new table.
     * @param best The best individual of the genetic algorithm.
     * @param delete Delete the table?
     *
     */
    public void storeBestIndividuo(String nombreTabla, Individual best, boolean delete){

        Connection conn = null;
        String insert;
        String tabla = "CREATE TABLE "+ nombreTabla +" (deaths int not null, kills int not null, totalDamageGiven int not null, totalDamageTaken int not null";

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+nombreTabla+"info.db");
            Statement stat = conn.createStatement();

            if (delete == true) {
                stat.execute("DROP TABLE IF EXISTS"+ nombreTabla +";");
            }

            if (best != null){
                for (int i=0; i<best.chromosomeSize(); ++i){
                    tabla = tabla.concat(",chromosome" +i+ " int not null");
                }
                tabla = tabla.concat(");");
                stat.execute(tabla);

                insert = "INSERT INTO "+ nombreTabla +" VALUES";
                insert = insert.concat("(' +"+ best.getDeaths() +"', '"+
                        best.getKills() +"', '"+ best.getTotalDamageGiven() +"', '"+
                        best.getTotalDamageTaken() +"'");

                for (int j=0; j<best.chromosomeSize(); ++j){
                    insert = insert.concat(",' "+ best.getGene(j) +"'");
                }

                insert = insert.concat(");");

                stat.execute(insert);
            }

            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR EN LA CREACIÓN DE LA TABLA PARA EL MEJOR INDIVIDUO");
            e.printStackTrace(System.out);
        }
    }

    /** It loads the population that has being stored in the DB.
     * @param population Array containing all the individuals that belong to the current generation.
     * @param nGenes Size of the individual's chromosome.
     *
     * @return List of the individuals stored in the DB.
     */
    public boolean loadPoblacion(Individual [] population, int nGenes){

        ResultSet resultados;
        String sql = "SELECT * FROM Genetico";
        Connection conn = null;
        boolean success = false;
        boolean salir = false;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);
            Statement stat = conn.createStatement();
            resultados = stat.executeQuery(sql);

            if (resultados.next()!= false){
                success = true;
                System.out.println("SUCCESS");
            }
            else{
                salir = true;
                System.out.println("NO SUCCESS");

            }

            int i = 0;
            while(!salir){
                population[i].setDeaths(resultados.getInt("deaths"));
                population[i].setKills(resultados.getInt("kills"));
                population[i].setTotalDamageGiven(resultados.getInt("totalDamageGiven"));
                population[i].setTotalDamageTaken(resultados.getInt("totalDamageTaken"));

                for (int j=0; j<nGenes; ++j){
                    population[i].setGene(j, resultados.getInt("chromosome"+j));
                }
                ++i;
                if (resultados.next() == false){
                    salir = true;
                }
            }
            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR RECUPERANDO LOS INDIVIDUOS");
            e.printStackTrace(System.out);
        }

        return success;
    }

    /** Each individual is executed more than once, this table makes sure that all
     * the information about those individuals is stored, in order to use it later.
     * @param population Results of the same individual stored as different individuals
     * in the same array (when really is the same individual with different results).
     * @param nGenes Size of the individual's chromosome.
     *
     * @return List of the individuals in the DB.
     */
    public boolean loadPoblacionAuxiliar(Individual [] population, int nGenes){

        ResultSet resultados;
        String sql = "SELECT * FROM Auxiliar";
        Connection conn = null;
        boolean success = false;
        boolean salir = false;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);
            Statement stat = conn.createStatement();
            resultados = stat.executeQuery(sql);

            if (resultados.next()!= false){
                success = true;
                System.out.println("SUCCESS");
            }
            else{
                salir = true;
                System.out.println("NO SUCCESS");

            }

            int i = 0;
            while(!salir){
                population[i].setDeaths(resultados.getInt("deaths"));
                population[i].setKills(resultados.getInt("kills"));
                population[i].setTotalDamageGiven(resultados.getInt("totalDamageGiven"));
                population[i].setTotalDamageTaken(resultados.getInt("totalDamageTaken"));

                for (int j=0; j<nGenes; ++j){
                    population[i].setGene(j, resultados.getInt("chromosome"+j));
                }
                ++i;
                if (resultados.next() == false){
                    salir = true;
                }
            }
            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR RECUPERANDO LOS INDIVIDUOS");
            e.printStackTrace(System.out);
        }

        return success;
    }

    /** Load the current individual.
     *
     * @return Current individual's position.
     */
    public int loadCurrent (){

        ResultSet resultados;
        String sql = "SELECT posicion FROM Genetico WHERE current != '-1'";
        Connection conn = null;
        int salida = 0;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);
            Statement stat = conn.createStatement();
            resultados = stat.executeQuery(sql);

            if (resultados.next()!= false){
               salida = resultados.getInt("posicion") + 1;
            }
            else{
                salida = 0;

            }

            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR RECUPERANDO EL CURRENT");
            e.printStackTrace(System.out);
        }

        return salida;
    }

    /** Load the current individual.
     *
     * @return Current individual's position.
     */
    public int loadIteration (){

        ResultSet resultados;
        String sql = "SELECT current FROM Genetico WHERE current != '-1'";
        Connection conn = null;
        int salida = 0;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);
            Statement stat = conn.createStatement();
            resultados = stat.executeQuery(sql);

            salida = resultados.getInt("current");

            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR RECUPERANDO EL ITERATION");
            e.printStackTrace(System.out);
        }

        return salida;
    }

    /** Load the current generation.
     * @return Number of current generation.
     */
    public int loadGeneration (){

        ResultSet resultados;
        String sql = "SELECT generacion FROM Genetico LIMIT 1";
        Connection conn = null;
        int salida = 0;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);
            Statement stat = conn.createStatement();
            resultados = stat.executeQuery(sql);

            salida = resultados.getInt("generacion");

            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR RECUPERANDO EL GENERATION");
            e.printStackTrace(System.out);
        }

        return salida;
    }

    /** Store the population in DB.
     * @param currentIndividual Current individual's position..
     * @param generation Current generation's number.
     * @param population Array containing all the individuals that belong to the current generation.
     * @param iteration Number of matches the bot has played
     */
    public void storeGenes (int currentIndividual, int generation,int iteration, Individual [] population){

        Connection conn = null;

        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);
            Statement stat = conn.createStatement();


            stat.execute("DELETE FROM Genetico WHERE posicion != '-1';");
            if (iteration == 0) {
                stat.execute("DELETE FROM Auxiliar WHERE posicion != '-1';");
            }

            String insert;

            for (int i=0; i<population.length; ++i) {
                insert = "INSERT INTO Genetico VALUES";
                insert = insert.concat("('"+ i +"', '"+ generation +"', '"+ population[i].getDeaths() +"', '"+
                        population[i].getKills() +"', '"+ population[i].getTotalDamageGiven() +"', '"+
                        population[i].getTotalDamageTaken() +"'");

                if(i == currentIndividual) {
                    insert = insert.concat(", '1'");
                }
                else {
                    insert = insert.concat(", '-1'");
                }

                for (int j=0; j<population[i].chromosomeSize(); ++j){
                    insert = insert.concat(",' "+ population[i].getGene(j) +"'");
                }

                insert = insert.concat(");");

                stat.execute(insert);
                insert = "";

                /*
                if (iteration < 2 && currentIndividual == i) {
                    insert = "INSERT INTO Auxiliar VALUES";
                    insert = insert.concat("('"+ iteration +"', '"+ population[i].getDeaths() +"', '"+
                        population[i].getKills() +"', '"+ population[i].getTotalDamageGiven() +"', '"+
                        population[i].getTotalDamageTaken() +"'");

                    for (int j=0; j<population[i].size(); ++j){
                        insert = insert.concat(",' "+ population[i].getChromosome()[j] +"'");
                    }
                    insert = insert.concat(");");

                    stat.execute(insert);
                    insert = "";
                }
                 */

            }

            conn.close();
        }
        catch(Exception e) {

            System.out.println("OCURRIO UN ERROR EN LA INSERCION EN GENETICO");
            e.printStackTrace(System.out);
        }
    }

    /**  Enable debug mode.
     * @param d Enable debug mode?
     */
    public void debug(Boolean d){
        DEBUG = d;
    }

    /** Update the class Memoria structures.
     * @param obj Object to update.
     */
    public static void update(Item obj){
        if(obj.getType().getCategory().equals(ItemType.Category.WEAPON)){
            Pair<String, Boolean> pp = new Pair(obj.getId().getStringId(), true);
            weapon.put(obj.getType().getGroup().toString(), pp);
        }
        else if(obj.getType().getCategory().equals(ItemType.Category.ADRENALINE)){
            adrenaline.add(obj.getId().getStringId());
        }
        else if(obj.getType().getCategory().equals(ItemType.Category.ARMOR)){
            Pair<String, Boolean> pp = new Pair(obj.getId().getStringId(), true);
            armor.put(obj.getType().getGroup().toString(), pp);
        }
        else if(obj.getType().getCategory().equals(ItemType.Category.AMMO)){
            Set<String> s = ammo.get(obj.getType().getGroup().toString());
            if (s == null){
                Set<String> set = new HashSet<String>();
                set.add(obj.getId().getStringId());
                ammo.put(obj.getType().getGroup().toString(), set);
            }
            else{
                s.add(obj.getId().getStringId());
                ammo.put(obj.getType().getGroup().toString(), s);
                System.out.println(obj.getType().getGroup().toString()+": "+s.size());
            }
        }
        else if(obj.getType().getCategory().equals(ItemType.Category.HEALTH)){
            Set<String> s = health.get(obj.getType().getGroup().toString());
            if (s == null){
                Set<String> set = new HashSet<String>();
                set.add(obj.getId().getStringId());
                health.put(obj.getType().getGroup().toString(), set);
                System.out.println(obj.getType().getGroup().toString()+": "+set.size());
            }
            else{
                s.add(obj.getId().getStringId());
                health.put(obj.getType().getGroup().toString(), s);
                System.out.println(obj.getType().getGroup().toString()+": "+s.size());
            }
        }
    }

     /**  Store de class Memoria structures in DB.
     * @param map Map of the game.
     */
    public void store (String map){

        Iterator it = null;

        if(!weapon.isEmpty()){
            it = weapon.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry)it.next();
                Pair <String, Boolean> p = (Pair<String, Boolean>) e.getValue();
                storeObject(p.getFirst(), "WEAPON", (String) e.getKey(), map);
            }
        }

       if(!armor.isEmpty()){
            it = armor.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry)it.next();
                Pair <String, Boolean> p = (Pair<String, Boolean>) e.getValue();
                storeObject(p.getFirst(), "ARMOR", (String) e.getKey(), map);
            }
        }

        if(!health.isEmpty()){
            it = health.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry)it.next();
                Set <String> p = (Set<String>) e.getValue();
                for(String t : p)
                    storeObject(t, "HEALTH", (String) e.getKey(), map);
            }
        }

        if(!ammo.isEmpty()){
            it = ammo.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry)it.next();
                Set <String> p = (Set<String>) e.getValue();
                for(String t : p)
                    storeObject(t, "AMMO", (String) e.getKey(), map);
            }
        }

        if(!adrenaline.isEmpty()){
            for(String t : adrenaline)
                storeObject(t, "ADRENALINE", "ADRENALINE" , map);
        }
    }

    /** Private function to store a object in DB.
     * @param id Identificator of this object.
     * @param tipo Type of this object.
     * @param nombre Name of this object.
     * @param mapa Map of the game.
     */
    private void storeObject (String id, String tipo, String nombre, String mapa){

        String sql = null;

        if(DEBUG == true){
            System.out.println("Start function store()");
        }

        try {

            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);

            sql = "insert into Item values('"+ id +"','"+ tipo +"',"
                    + "'"+ nombre +"',"
                    + "'"+ mapa +"');";

             if(DEBUG == true){
                System.out.println("SQL: " + sql);
            }

            Statement sta = conn.createStatement();
            sta.execute(sql);
            sta.close();
            conn.commit();
            conn.close();

        }
        catch (Exception e) {
            System.out.println("OCURRIO UN ERROR EN LA FUNCION STORE");
            e.printStackTrace(System.out);
        }

    }

     /** Load the class Memoria structures.
      * @param map Map of the game.
      */
    public void load(String map){

        ResultSet resultados;
        String aux = new String();
        String sql = null;
        Set <String> miniH = new HashSet <String>();
        Set <String> H = new HashSet <String>();
        Set <String> AMMO_ASSAULT_RIFLE = new HashSet <String> ();
        Set <String> AMMO_BIO_RIFLE = new HashSet <String> ();
        Set <String> AMMO_FLAK_CANNON = new HashSet <String> ();
        Set <String> AMMO_LIGHTNING_GUN = new HashSet <String> ();
        Set <String> AMMO_LINK_GUN = new HashSet <String> ();
        Set <String> AMMO_MINIGUN = new HashSet <String> ();
        Set <String> AMMO_ROCKET_LAUNCHER = new HashSet <String> ();
        Set <String> AMMO_SHIELD_GUN = new HashSet <String> ();
        Set <String> AMMO_SHOCK_RIFLE = new HashSet <String> ();
        Set <String> AMMO_SNIPER_RIFLE = new HashSet <String> ();

        sql = "SELECT id, tipo, nombre FROM Item WHERE mapa = '"+ map +"';";

        if(DEBUG == true){
            System.out.println("Start function loadLocation()");
            System.out.println("SQL: " + sql);
        }

        try {

            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:"+BDNAME);

            Statement sta = conn.createStatement();
            resultados = sta.executeQuery(sql);

            if(DEBUG == true){
                System.out.println("LLamada a la BD realizada con éxito");
            }

            while(resultados.next()){
                aux = resultados.getString("id");

                if(resultados.getString("tipo").equals("ADRENALINE")){
                    adrenaline.add(aux);
                }

                else if(resultados.getString("tipo").equals("HEALTH")){
                    if(resultados.getString("nombre").equals("MINI_HEALTH")){
                        miniH.add(aux);
                    }
                    else{
                        H.add(aux);
                    }
                }

                 else if(resultados.getString("tipo").equals("AMMO")){
                     if(resultados.getString("nombre").equals("ASSAULT_RIFLE")){
                         AMMO_ASSAULT_RIFLE.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("BIO_RIFLE")){
                         AMMO_BIO_RIFLE.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("FLAK_CANNON")){
                         AMMO_FLAK_CANNON.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("LIGHTNING_GUN")){
                         AMMO_LIGHTNING_GUN.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("LINK_GUN")){
                         AMMO_LINK_GUN.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("MINIGUN")){
                         AMMO_MINIGUN.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("ROCKET_LAUNCHER")){
                         AMMO_ROCKET_LAUNCHER.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("SHIELD_GUN")){
                         AMMO_SHIELD_GUN.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("SHOCK_RIFLE")){
                         AMMO_SHOCK_RIFLE.add(aux);
                     }
                     else if(resultados.getString("nombre").equals("SNIPER_RIFLE")){
                         AMMO_SNIPER_RIFLE.add(aux);
                     }
                }
                else if(resultados.getString("tipo").equals("WEAPON")){
                    weapon.put(resultados.getString("nombre"), new Pair<String, Boolean> (aux, false));
                }
                else if(resultados.getString("tipo").equals("ARMOR")){
                    armor.put(resultados.getString("nombre"), new Pair <String, Boolean> (aux, false));
                }
           }

            /*Guardamos los datos recopilados en cada una de su estructuras*/
            health.put("MINI_HEALTH", miniH);
            health.put("HEALTH", H);
            ammo.put("ASSAULT_RIFLE", AMMO_ASSAULT_RIFLE);
            ammo.put("BIO_RIFLE", AMMO_BIO_RIFLE);
            ammo.put("FLAIK_CANNON", AMMO_FLAK_CANNON);
            ammo.put("LIGHTNING_GUN", AMMO_LIGHTNING_GUN);
            ammo.put("LINK_GUN", AMMO_LINK_GUN);
            ammo.put("MINIGUN", AMMO_MINIGUN);
            ammo.put("ROCKET_LAUNCHER", AMMO_ROCKET_LAUNCHER);
            ammo.put("SHIELD_GUN", AMMO_SHIELD_GUN);
            ammo.put("SHOCK_RIFLE", AMMO_SHOCK_RIFLE);
            ammo.put("SNIPER_RIFLE", AMMO_SNIPER_RIFLE);


            for(Pair<String,Boolean> i : armor.values()){
                System.out.println(i.getFirst());
            }

            sta.close();
            conn.close();
        }
        catch (Exception e) {
            System.out.println("OCURRIO UN ERROR EN LA FUNCION LOADLOCATION");
            e.printStackTrace(System.out);

        }
    }

}