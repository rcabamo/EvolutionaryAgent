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

package behavior.primaryStates;

import bot.T800;
import knowledge.EnemyInfo;
import utilities.Arithmetic;
import evolutionaryComputation.Individual;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathPlanner;
import cz.cuni.amis.pogamut.base.communication.command.IAct;
import cz.cuni.amis.pogamut.base3d.worldview.IVisionWorldView;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.unreal.agent.navigation.IUnrealPathExecutor;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Raycasting;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.SetCrouch;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

import java.util.Random;


/**
 * This state is used to run from the enemy. Its useful to get to a safe spot when
 * we are going to be killed. When activated, the bot uses its shield to protect
 * himself from the enemy's shots. If its used when the enemy is not on sight, the
 * bot will try to get to the furthest spot from the enemy (guessing the enemy's
 * position).
 *
 * @author Francisco Aisa García
 */


public class Retreat extends PrimaryState {

    // *************************************************************************
    //                           INSTANCE FIELDS
    // *************************************************************************


    /** True if the bot is in the reloading phase (for the shield gun) */
    private boolean reloading;


    // *************************************************************************
    //                                METHODS
    // *************************************************************************


    /**
     * Argument based constructor.
     * @param body body field from T800.
     * @param act act field from T800.
     * @param world world field from T800.
     * @param game game field from T800.
     * @param items items field from T800.
     * @param info info field from T800.
     * @param weaponry weaponry field from T800.
     * @param pathPlanner pathPlanner field from T800.
     * @param pathExecutor pathExecutor field from T800.
     * @param move move field from T800.
     * @param raycasting raycasting field from T800.
     * @param cardinalRayArray cardinalRayArray field from T800.
     * @param shoot shoot field from T800.
     */
    public Retreat (final CompleteBotCommandsWrapper body, final IAct act, final IVisionWorldView world,
                 final Game game, final Items items, final AgentInfo info, final Weaponry weaponry,
                 final IPathPlanner <ILocated> pathPlanner, final IUnrealPathExecutor <ILocated> pathExecutor,
                 final AdvancedLocomotion move, final Raycasting raycasting, final AutoTraceRay cardinalRayArray [],
                 final ImprovedShooting shoot, final Individual testIndividual) {

        super (body, act, world, game, items, info, weaponry, pathPlanner, pathExecutor, move, raycasting, cardinalRayArray, shoot, testIndividual);
    }

    //__________________________________________________________________________

    /**
     * Retreat to the safest area.
     * @param enemy Enemy.
     * @param facingSpot Location that the bot has to face.
     * @param enemyInfo Guess/Known information about the enemy.
     */
    public void stateDrivenMovement (final Player enemy, final Location facingSpot, final EnemyInfo enemyInfo) {

        if (crouched) {
            act.act(new SetCrouch ().setCrouch (false));
            crouched = false;
        }

        Location enemyLocation = null;
        if (enemy != null) {
            enemyLocation = enemy.getLocation ();
        }
        else {
            enemyLocation = enemyInfo.getLastKnownLocation();
        }

        // If we know the enemy's location because we are seeing him or because
        // we know/guess where he is, retreat to the closest area to me that is
        // the furthest to the enemy
        if (enemyLocation != null) {
            if (Math.abs (enemyInfo.getLastTimeMet() - game.getTime ()) < 10) {
                Location newDestination = Arithmetic.getBestRunZone (enemyLocation, info);

                if (newDestination != null && newDestination != stateDrivenDestination) {
                    if (visitedSpots.size() >= 1) {
                        visitedSpots.remove (0);
                    }

                    stateDrivenDestination = newDestination;
                    visitedSpots.add (stateDrivenDestination);

                    IPathFuture <ILocated> pathHandle = pathPlanner.computePath (info.getLocation(), stateDrivenDestination);

                    if (pathExecutor.isExecuting()) {
                        pathExecutor.stop ();
                    }

                    pathExecutor.followPath(pathHandle);
                }
            }
        }

        if (!pathExecutor.isExecuting()) {
            // Move to a random area
            Random rand = new Random();
            Location newDestination = null;
            int pos = 0;

            boolean success = false;
            while (!success) {
                pos = rand.nextInt(T800.areas.length);
                if (!visitedSpots.contains (T800.areas [pos].getLocation ())) {
                    success = true;
                    newDestination = T800.areas [pos].getLocation ();
                }
            }

            if (visitedSpots.size() >= 2) {
                visitedSpots.remove (0);
            }

            stateDrivenDestination = newDestination;
            visitedSpots.add (stateDrivenDestination);

            IPathFuture <ILocated> pathHandle = pathPlanner.computePath(info.getLocation(), stateDrivenDestination);
            if (move.isRunning()) {
                move.stopMovement();
            }
            pathExecutor.followPath(pathHandle);
        }
    }

    //__________________________________________________________________________

    /**
     * If the enemy is on sight and we have ammo for the shield gun, use it for protection,
     * otherwise, shoot as usual.
     * @param enemy Enemy.
     * @param enemyInfo Guessed/Known information about the enemy.
     */
    @Override
    public void switchToBestWeapon (final Player enemy, final EnemyInfo enemyInfo) {
        // If the shield gun's ammo is over 50, we can skip the reloading phase
        if (weaponry.getAmmo(ItemType.SHIELD_GUN) > 50) {
            reloading = false;
        }

        if (weaponry.getAmmo(ItemType.SHIELD_GUN) > 0 && !reloading) {
            // If we are not holding the shield gun, we switch to it
            if (!weaponry.getCurrentWeapon().getType().equals(ItemType.SHIELD_GUN)) {
                weaponry.changeWeapon(ItemType.SHIELD_GUN);
            }
        }
        // If we run out of shield gun's ammo or we are in the reloading phase, switch
        // to the best combat weapon
        else {
            // Activate the reloading flag
            reloading = true;

            // Switch to the best combat weapon
            super.switchToBestWeapon (enemy, enemyInfo);
        }
    }

    //__________________________________________________________________________

    /**
     * If we are holding the shield gun, use it for protection, otherwise, shoot
     * as usual.
     * @param enemy Enemy.
     * @param bullseye Target (combo or spam).
     */
    @Override
    public void engage (final Player enemy, final Location bullseye) {
        if (enemy != null || bullseye != null) {
            if (weaponry.getCurrentWeapon().getType().equals(ItemType.SHIELD_GUN)){
                if (enemy != null) {
                    shoot.shootSecondary (enemy);
                }
                else {
                    shoot.shootSecondary (bullseye);
                }
            }
            else {
                super.engage(enemy, bullseye);
            }
        }
        else {
            shoot.stopShooting();
        }
    }

    //__________________________________________________________________________

    /**
     * Converts to string the state's name.
     * @return The name of the state.
     */
    public String toString () {
        return "Retreat";
    }
}
