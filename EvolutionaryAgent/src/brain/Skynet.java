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

package brain;

import bot.T800;
import behavior.primaryStates.PrimaryState;
import behavior.secondaryStates.SecondaryState;
import knowledge.EnemyInfo;
import evolutionaryComputation.*;
import utilities.Arithmetic;
import knowledge.Memoria;
import utilities.Pair;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;


/**
 * The purpose of this class is to deliberate. The primary state will make the bot
 * react inconciously as a reflex, but after that Skynet will think about what to
 * do next. This class is responsible for changing the primary and secondary state
 * of the bot. We could say it is the brains of the bot.
 *
 * @author Francisco Aisa García
 * @author Ricardo Caballero Moral
 */


public class Skynet {

    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************


    /** Current Individual to be evaluated */
    Individual testIndividual;
    /** Pointer to body from T800 */
    protected CompleteBotCommandsWrapper body;
    /** Location of a combo (null if it doesn't exist) */
    private Location comboLocation;
    /** Location of a spam (null if it doesn't exists) */
    private Location spamLocation;


    // *************************************************************************
    //                                 METHODS
    // *************************************************************************


    /**
     * Argument based constructor.
     * @param body Pointer to body from T800.
     * @param testIndividual Individual that is being tested during this execution
     */
    public Skynet (final CompleteBotCommandsWrapper body, final Individual testIndividual) {
        this.body = body;
        this.testIndividual = testIndividual;

        comboLocation = null;
        spamLocation = null;
    }

    //__________________________________________________________________________

    /**
     * Resets all the temporary information hosted in skynet.
     */
    public void resetTempInfo () {
        comboLocation = null;
        spamLocation = null;
    }

    //__________________________________________________________________________

    /**
     * It estimates how bad we need an item based on the items that we already have.
     * @param item Item that we are evaluating.
     * @param weaponry Weaponry that we are carrying.
     * @return The priority of the given item.
     */
    protected int estimateItemPriority (final Item item, final Weaponry weaponry) {
        int priority = -1;
        ItemType type = item.getType ();

        if (type.equals (ItemType.SUPER_SHIELD_PACK)) {
            priority = testIndividual.getGene (20);
        }
        else if (type.equals (ItemType.SHIELD_PACK)) {
            priority = testIndividual.getGene (21);
        }
        else if (type.equals (ItemType.SNIPER_RIFLE) || type.equals (ItemType.LIGHTNING_GUN)) {
            if (!weaponry.hasLoadedWeapon (type)) {
                priority = testIndividual.getGene (22);
            }
        }
        else if (type.equals (ItemType.SHOCK_RIFLE)) {
            if (!weaponry.hasLoadedWeapon (type)) {
                priority = testIndividual.getGene (23);
            }
        }
        else if (type.equals (ItemType.FLAK_CANNON) || type.equals (ItemType.ROCKET_LAUNCHER)) {
            if (!weaponry.hasWeapon(ItemType.FLAK_CANNON) && !weaponry.hasWeapon(ItemType.ROCKET_LAUNCHER)) {
                priority = testIndividual.getGene (24);
            }
        }
        else if (type.equals (ItemType.MINIGUN)) {
            if (!weaponry.hasWeapon (type)) {
                priority = testIndividual.getGene (25);
            }
        }

        return priority;
    }

    //__________________________________________________________________________

    /**
     * It dictates where the bot should go (if any place). If we are seeing the enemy
     * it will return a destination ONLY if the super shield is spawned. If we aren't
     * seeing the enemy, it will return the best destination where we could be headed.
     * @param info Agent information.
     * @param enemy Enemy.
     * @param weaponry Weaponry we are carrying.
     * @param items Items from the current map.
     * @return Location where the bot must go. If it musn't go to a certain location,
     * it returns null.
     */
    public Location estimateDestination (final AgentInfo info, final Player enemy, final Weaponry weaponry, final Items items) {
        Location newDestination = null;
        int currentPriority;
        int maximumPriority = 0;

        // If we have information in the DB about shields, see if the 50 or the 100
        // are spawned. If they are spawned, select the one with the biggest priority.
        // In the case of the 100, even if we are seeing the enemy we will go to it.
        if (!Memoria.armor.isEmpty ()) {
            for (Pair <String, Boolean> couple : Memoria.armor.values()) {
                Item item = items.getItem (couple.getFirst ());

                if (item != null) {
                    if (items.isPickupSpawned (item)) {
                        currentPriority = estimateItemPriority (item, weaponry);

                        if (currentPriority > maximumPriority) {
                            maximumPriority = currentPriority;

                            if (enemy != null) {
                                if (maximumPriority == 100) {
                                    newDestination = item.getLocation ();
                                }
                            }
                            else {
                                newDestination = item.getLocation ();
                            }
                        }
                    }
                }
            }
        }

        // If we are not seeing the enemy and we are not going to pick up a shield,
        // look for the locations of the weapons in the DB (if any), and go for the
        // weapon the best suit us.
        if (enemy == null && newDestination == null) {
            if (!Memoria.weapon.isEmpty ()) {
                double currentDistance;
                double targetDistance = Arithmetic.INFINITY;

                for (Pair <String, Boolean> couple : Memoria.weapon.values ()) {
                    Item item = items.getItem (couple.getFirst ());

                    if (item != null) {
                        if (items.isPickupSpawned (item)) {
                            currentPriority = estimateItemPriority (item, weaponry);

                            if (currentPriority > maximumPriority) {
                                maximumPriority = currentPriority;
                                targetDistance = info.getDistance (item.getLocation ());
                                newDestination = item.getLocation ();
                            }
                            // If we have two items with the same priority, we'll
                            // stick with the closest one
                            else if (currentPriority == maximumPriority) {
                                currentDistance = info.getDistance (item.getLocation ());
                                if (currentDistance < targetDistance) {
                                    targetDistance = currentDistance;
                                    newDestination = item.getLocation ();
                                }
                            }
                        }
                    }
                }
            }
        }

        return newDestination;
    }

    //__________________________________________________________________________

    /**
     * Estimate how good our arsenal is when compared to the enemy's.
     * @param weaponry Weaponry that we are carrying.
     * @param enemyArsenal List of weapons we suppose the enemy has.
     * @return A vector divided in 3 slots. Each one indicates close, average and
     * far distance (in that order). Each slot has a value that varies from 1 to 5, meaning:
     * 1 our arsenal is far worst, 2 our arsenal is worst, 3 our arsenals are more or less
     * the same, 4 our arsenal is better, 5 our arsenal is far better.
     */
    public int [] compareArsenals (final Weaponry weaponry, final boolean enemyArsenal []) {
        int arsenalProfit [] = new int [3];
        int enemyTotalProfit [] = new int [3];
        int ownTotalProfit [] = new int [3];
        int ownProfit = 0;
        int enemyProfit = 0;

        // *********************************************************************
        //                       PROFIT FROM A FAR DISTANCE
        // *********************************************************************


        // CHECK OUR ARSENAL


        if ((weaponry.hasLoadedWeapon(ItemType.LIGHTNING_GUN) || weaponry.hasLoadedWeapon(ItemType.SNIPER_RIFLE)) && weaponry.hasLoadedWeapon(ItemType.SHOCK_RIFLE)) {
            ownProfit = 100;
        }
        else if (weaponry.hasLoadedWeapon(ItemType.LIGHTNING_GUN) || weaponry.hasLoadedWeapon(ItemType.SNIPER_RIFLE)) {
            ownProfit = 90;
        }
        else if (weaponry.hasLoadedWeapon(ItemType.SHOCK_RIFLE)) {
            ownProfit = 80;
        }
        else if (weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
            ownProfit = 40;
        }


        // CHECK ENEMY'S ARSENAL


        if (enemyArsenal [EnemyInfo.LIGHTNING_GUN] && enemyArsenal [EnemyInfo.SHOCK_RIFLE]) {
            enemyProfit = 100;
        }
        else if (enemyArsenal [EnemyInfo.LIGHTNING_GUN]) {
            enemyProfit = 90;
        }
        else if (enemyArsenal [EnemyInfo.SHOCK_RIFLE]) {
            enemyProfit = 80;
        }
        else if (weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
            enemyProfit = 40;
        }


        // ESTIMATE PROFIT

        ownTotalProfit [2] = ownProfit;
        enemyTotalProfit [2] = enemyProfit;

        ownProfit = enemyProfit = 0;



        // *********************************************************************
        //                   PROFIT FROM AN AVERAGE DISTANCE
        // *********************************************************************


        // CHECK OUR ARSENAL


        if ((weaponry.hasLoadedWeapon(ItemType.FLAK_CANNON) || weaponry.hasLoadedWeapon(ItemType.ROCKET_LAUNCHER))) {
            if (weaponry.hasLoadedWeapon(ItemType.SHOCK_RIFLE) && weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
                ownProfit = 100;
            }
            else if (weaponry.hasLoadedWeapon(ItemType.SHOCK_RIFLE)) {
                ownProfit = 95;
            }
            else if (weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
                ownProfit = 90;
            }
            else if (weaponry.hasLoadedWeapon(ItemType.SNIPER_RIFLE) || weaponry.hasLoadedWeapon(ItemType.LIGHTNING_GUN)) {
                ownProfit = 85;
            }
            else {
                ownProfit = 80;
            }
        }
        else {
            if (weaponry.hasLoadedWeapon(ItemType.SHOCK_RIFLE) && (weaponry.hasLoadedWeapon(ItemType.SNIPER_RIFLE) || weaponry.hasLoadedWeapon(ItemType.LIGHTNING_GUN))) {
                if (weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
                    ownProfit = 80;
                }
                else {
                    ownProfit = 75;
                }
            }
            else if (weaponry.hasLoadedWeapon (ItemType.SHOCK_RIFLE)) {
                if (weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
                    ownProfit = 60;
                }
                else {
                    ownProfit = 50;
                }
            }
            else if (weaponry.hasLoadedWeapon(ItemType.SNIPER_RIFLE) || weaponry.hasLoadedWeapon(ItemType.LIGHTNING_GUN)) {
                if (weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
                    ownProfit = 40; // Lower than if he is holding shock and mini
                }
                else {
                    ownProfit = 10;
                }
            }
            else if (weaponry.hasLoadedWeapon (ItemType.MINIGUN)) {
                ownProfit = 30; // Lower than if he is holding shock/sniper and mini
            }
        }


        // CHECK ENEMY'S ARSENAL


        if ((enemyArsenal [EnemyInfo.FLAK_CANNON] || enemyArsenal [EnemyInfo.ROCKET_LAUNCHER])) {
            if (enemyArsenal [EnemyInfo.SHOCK_RIFLE] && enemyArsenal [EnemyInfo.MINIGUN]) {
                enemyProfit = 100;
            }
            else if (enemyArsenal [EnemyInfo.SHOCK_RIFLE]) {
                enemyProfit = 90;
            }
            else if (enemyArsenal [EnemyInfo.MINIGUN]) {
                enemyProfit = 80;
            }
            else if (enemyArsenal [EnemyInfo.SNIPER_RIFLE]) {
                enemyProfit = 70;
            }
            else {
                enemyProfit = 60;
            }
        }
        else {
            if (enemyArsenal [EnemyInfo.SHOCK_RIFLE] && (enemyArsenal [EnemyInfo.SNIPER_RIFLE])) {
                if (enemyArsenal [EnemyInfo.MINIGUN]) {
                    enemyProfit = 90;
                }
                else {
                    enemyProfit = 60;
                }
            }
            else if (enemyArsenal [EnemyInfo.SHOCK_RIFLE]) {
                if (enemyArsenal [EnemyInfo.MINIGUN]) {
                    enemyProfit = 55;
                }
                else {
                    enemyProfit = 50;
                }
            }
            else if (enemyArsenal [EnemyInfo.SNIPER_RIFLE]) {
                if (enemyArsenal [EnemyInfo.MINIGUN]) {
                    enemyProfit = 40; // Lower than if he has shock and mini
                }
                else {
                    enemyProfit = 10;
                }
            }
            else if (enemyArsenal [EnemyInfo.MINIGUN]) {
                enemyProfit = 30; // Lower than if he has shock/sniper and mini
            }
        }


        // ESTIMATE PROFIT


        ownTotalProfit [1] = ownProfit;
        enemyTotalProfit [1] = enemyProfit;

        ownProfit = enemyProfit = 0;


        // *********************************************************************
        //                    PROFIT FROM A CLOSE DISTANCE
        // *********************************************************************


        // CHECK OUR ARSENAL


        if (weaponry.hasLoadedWeapon(ItemType.FLAK_CANNON) || weaponry.hasLoadedWeapon(ItemType.ROCKET_LAUNCHER)) {
            if (weaponry.hasLoadedWeapon(ItemType.LINK_GUN) || weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
               ownProfit = 100;
            }
            else {
                ownProfit = 90;
            }
        }
        else if (weaponry.hasLoadedWeapon(ItemType.LINK_GUN) || weaponry.hasLoadedWeapon(ItemType.MINIGUN)) {
            ownProfit = 50;
        }


        // CHECK ENEMY'S ARSENAL


        if (enemyArsenal [EnemyInfo.FLAK_CANNON] || enemyArsenal[EnemyInfo.ROCKET_LAUNCHER]) {
            if (enemyArsenal[EnemyInfo.LINK_GUN] || enemyArsenal[EnemyInfo.MINIGUN]) {
               enemyProfit = 100;
            }
            else {
                enemyProfit = 90;
            }
        }
        else if (enemyArsenal[EnemyInfo.LINK_GUN] || enemyArsenal[EnemyInfo.MINIGUN]) {
            enemyProfit = 50;
        }


        // ESTIMATE PROFIT


        ownTotalProfit [0] = ownProfit;
        enemyTotalProfit [0] = enemyProfit;


        // *********************************************************************
        //    ESTIMATE HOW GOOD OUR ARSENAL IS WHEN COMPARED TO THE ENEMY'S
        // *********************************************************************

        // 1 -> Our arsenal is far worst
        // 2 -> Our arsenal is worst
        // 3 -> Our arsenals are mor or less the same
        // 4 -> Our arsenal is better
        // 5 -> Our arsenal is far better


        // --------------------------> RESULTS  <-------------------------------

        for (int i = 0; i < 3; ++i) {
            int dif = ownTotalProfit [i] - enemyTotalProfit [i];
            if (dif >= -10 && dif <= 10 ) {
                arsenalProfit [i] = 3;
            }
            else if(dif >= 0) {
                if (dif > 10 && dif <= 30) {
                    arsenalProfit [i] = 4;
                }
                else
                    arsenalProfit [i] = 5;
            }
            else {
                if (dif < -10 && dif >= -30) {
                    arsenalProfit [i] = 2;
                }
                else
                    arsenalProfit [i] = 1;
            }
        }

        return arsenalProfit;
    }

    //__________________________________________________________________________

    /**
     * Estimate the best distance to stay put from an enemy (if he is on sight)
     * based on both the enemy's arsenal and our arsenal.
     * @param enemyDistance Distance to enemy (from us).
     * @param maximumProfit Maximum profit we have gotten comparing our arsenal
     * to the enemy's.
     * @param risk Level of risk (between 1 and 5) that we are willing to take.
     * @param sweetSpot Indicates the range in which we want to stay (close, average, far).
     * @return The profile we need (Defensive or Ofensive) to stay in the best range.
     * If the risk is too high or we are already in the range, it returns "disabled".
     */
    public int estimateProfile (double enemyDistance, int maximumProfit, int risk, int sweetSpot) {
        // If our arsenal is more or less the same or better than the enemy's
        if (maximumProfit >= risk) {
            // Check which is the best range

            // SHORT DISTANCE
            if (sweetSpot == 0) {
               if (enemyDistance > PrimaryState.getShortRange () / 2) {
                   return SecondaryState.States.OFENSIVE_PROFILE.ordinal ();
               }
            }
            // AVERAGE DISTANCE
            else if (sweetSpot == 1) {
                if (enemyDistance > PrimaryState.getFarRange ()) {
                   return SecondaryState.States.OFENSIVE_PROFILE.ordinal ();
                }
                else if (enemyDistance < PrimaryState.getMediumRange () / 3) {
                    return SecondaryState.States.DEFENSIVE_PROFILE.ordinal ();
                }
            }
            // FAR DISTANCE
            else if (sweetSpot == 2) {
                if (enemyDistance < PrimaryState.getFarRange () / 2) {
                   return SecondaryState.States.DEFENSIVE_PROFILE.ordinal ();
                }
            }
        }

        return SecondaryState.States.DISABLED.ordinal ();
    }

    //__________________________________________________________________________

    /**
     * It sets the behavior of the bot. Based on his status and the enemy status,
     * it decide which is the best primary state and which is the best secondary state
     * (if any).
     * @param info Agent information.
     * @param weaponry Weaponry that we are carrying.
     * @param enemy Enemy.
     * @param enemyInfo Information about the enemy.
     * @param game Game information.
     * @return A vector containing 2 integers. The first one indicates the primary state
     * and the second one the secondary state. Numbers correspond to the constants defined
     * in T800 (@see T800.class).
     */
    public int [] behave (final AgentInfo info, final Weaponry weaponry, final Player enemy, final EnemyInfo enemyInfo, final Game game) {
        int maximumProfit, bestDefensiveRange, bestOfensiveRange;
        int arsenalStatus [] = compareArsenals (weaponry, enemyInfo.getArsenal ());

        // ESTIMATE HOW GOOD OUR ARSENAL IS WHEN COMPARED TO THE ENEMY'S

        // maximumProfit is going to hold the best score our arsenal gets in any range
        // bestDefensiveRange is going to contain the furthest range where our arsenal is the best
        // bestOfensiveRange is going to contain the closest range where our arsenal is the best
        // Note that bestDefensiveRange != bestOfensiveRange only when the best score
        // is repeated.

        maximumProfit = arsenalStatus [0];
        bestDefensiveRange = bestOfensiveRange = 0;
        for (int i = 1; i < arsenalStatus.length; ++i) {
            if (arsenalStatus [i] >= maximumProfit) {
                maximumProfit = arsenalStatus [i];

                if (arsenalStatus [i] == maximumProfit) {
                    bestDefensiveRange = i;
                }
                else {
                    bestOfensiveRange = i;
                }
            }
        }

        int nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
        int nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();

        int health = info.getHealth ();
        int armor = info.getArmor ();
        int total = health + armor;
        int enemyHealth = enemyInfo.getHealth ();
        int enemyTotal = enemyInfo.getHealthArmor ();

        double enemyDistance = 0;
        if (enemy != null) {
            enemyDistance = info.getDistance (enemy.getLocation ());
        }

        double elapsedTime = Math.abs (enemyInfo.getLastTimeMet () - game.getTime ());

        if (health < testIndividual.getGene (12)) {
            if (enemyHealth < health + testIndividual.getGene (14)) {
                // If our arsenals are more or less the same
                if (maximumProfit == 3) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 3, bestDefensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.CAMP.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.PICKUP_HEALTH.ordinal ();
                        }
                    }
                }
                // If my arsenal is better
                else if (maximumProfit > 3) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 3, bestOfensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.PICKUP_HEALTH.ordinal ();
                        }
                    }
                }
                // If my arsenal is worst
                else {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                    }
                }
            }
            else {
                // If we are seeing the enemy
                if (enemy != null) {
                    nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                    nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                }
                // If we can't see him
                else {
                    if (elapsedTime < testIndividual.getGene (19)) {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.CRITICAL_HEALTH.ordinal ();
                    }
                    else {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.CRITICAL_HEALTH.ordinal ();
                    }
                }
            }
        }
        else if (health < testIndividual.getGene (13)) {
            // If our status is better, we risk it a bit
            if (enemyTotal + testIndividual.getGene (15) < total) {
                // If our arsenals are more or less the same or my arsenal is better
                if (maximumProfit >= 3) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 3, bestOfensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.PICKUP_HEALTH.ordinal ();
                        }
                    }
                }
                // If my arsenal is a bit worst, we risk it a bit
                else if (maximumProfit == 2) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 2, bestOfensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                    }
                }
                // If our arsenal is far worst
                else {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                    }
                }
            }

            // If we are more or less in the same status
            else if (enemyTotal < total + testIndividual.getGene (16)) {
                // If our arsenals are more or less the same or my arsenal is better
                if (maximumProfit >= 3) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 3, bestDefensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.PICKUP_HEALTH.ordinal ();
                        }
                    }
                }
                // If my arsenal is a bit worst, we risk it a bit
                else if (maximumProfit == 2) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 2, bestOfensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                    }
                }
                // If my arsenal is far worst, retreat!!
                else {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                    }
                }
            }

            // If our status is worst
            else {
                if (enemy != null) {
                    nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                    nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                }
                else {
                    if (elapsedTime < testIndividual.getGene (19)) {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.CRITICAL_HEALTH.ordinal ();
                    }
                    else {
                        nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                        nextSecondaryState = SecondaryState.States.CRITICAL_HEALTH.ordinal ();
                    }
                }
            }
        }
        // If our health is very good
        else {
            // If our status is much better than the enemy's
            if (enemyTotal + testIndividual.getGene (17) < total) {
                // If our arsenal is not much worst than the enemy's
                if (maximumProfit >= 2) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 2, bestOfensiveRange);
                    }
                    // If we can't see him
                    else {
                        nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                        nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                    }
                }
                // If our arsenal is far worst
                else {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                    }
                }
            }

            // If we are more or less in the same status
            else if (enemyTotal < total + testIndividual.getGene (18)) {
                // If our arsenal is more or less the same or my arsenal is better
                if (maximumProfit >= 3) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 3, bestDefensiveRange);
                    }
                    // If we can't see him
                    else {
                        nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                        nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                    }
                }
                // If my arseanl is a bit worst, we risk it a bit
                else if (maximumProfit == 2) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 2, bestDefensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                    }
                }
                else {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                        nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.GREEDY.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_WEAPONRY.ordinal ();
                        }
                    }
                }
            }

            // If we are worst
            else {
                // If my arsenal is at least better
                if (maximumProfit >= 4) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 4, bestDefensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.CAMP.ordinal ();
                            nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.HUNT.ordinal ();
                            nextSecondaryState = SecondaryState.States.PICKUP_HEALTH.ordinal ();
                        }
                    }
                }
                // If my arsenal is more or less like the enemy's
                else if (maximumProfit == 3) {
                    // If we are seeing the enemy
                    if (enemy != null) {
                        nextPrimaryState = PrimaryState.States.ATTACK.ordinal ();
                        nextSecondaryState = estimateProfile (enemyDistance, maximumProfit, 3, bestDefensiveRange);
                    }
                    // If we can't see him
                    else {
                        if (elapsedTime < testIndividual.getGene (19)) {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.CRITICAL_HEALTH.ordinal ();
                        }
                        else {
                            nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                            nextSecondaryState = SecondaryState.States.PICKUP_HEALTH.ordinal ();
                        }
                    }
                }
                else {
                    nextPrimaryState = PrimaryState.States.RETREAT.ordinal ();
                    nextSecondaryState = SecondaryState.States.DISABLED.ordinal ();
                }
            }
        }

        int behaviorArray [] = {nextPrimaryState, nextSecondaryState};
        return behaviorArray;
    }

    //__________________________________________________________________________

    /**
     * Depending on if there is a feasible spam or combo, it returns a target. If
     * there's not a feasible combo or spam it returns null.
     * @return A Location if there is a feasible combo or spam, or null in any other
     * case.
     */
    public Location estimateTarget () {
        if (comboLocation != null) {
            PrimaryState.feasibleCombo ();

            return comboLocation;
        }

        // Check if we should spam. If we should spam, then we should return the spam
        // Location.

        return null;
    }

    //__________________________________________________________________________

    /**
     * Depending on the kind of projectile, it checks whether it is a plausible combo
     * or not (to blow it with the shock).
     * @param projectile Projectile that we are seeing.
     * @param enemy Enemy
     */
    public void incomingProjectile (final IncomingProjectile projectile, final Player enemy) {
        // If the projectile is a feasible combo, we update the combo Location.
        // spamLocation indicates if the spot where we want to blow the combo is a spam.
        if (projectile.getType().equals ("XWeapons.ShockProjectile")) {
            if (enemy != null || spamLocation != null) {
                Location targetPosition = null;
                if (enemy != null) {
                    targetPosition = enemy.getLocation();
                }
                else if (spamLocation != null) {
                    targetPosition = spamLocation;
                }

                Location projectilePosition = projectile.getLocation();
                if (Location.getDistance (targetPosition, projectilePosition) < 600) {
                    comboLocation = projectile.getLocation();
                }
            }
        }
    }
}
