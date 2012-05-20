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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.SetCrouch;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

import java.util.Random;


/**
 * This state is used to hunt the enemy. If the enemy is not on sight and this state
 * is activated, the bot will use its senses (ears and eyes) to try to hunt him.
 * Use it, when the enemy is not on sight.
 *
 * @author Francisco Aisa García
 */


public class Hunt extends PrimaryState {


    // *************************************************************************
    //                                  METHODS
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
    public Hunt (final CompleteBotCommandsWrapper body, final IAct act, final IVisionWorldView world,
                  final Game game, final Items items, final AgentInfo info, final Weaponry weaponry,
                  final IPathPlanner <ILocated> pathPlanner, final IUnrealPathExecutor <ILocated> pathExecutor,
                  final AdvancedLocomotion move, final Raycasting raycasting, final AutoTraceRay cardinalRayArray [],
                  final ImprovedShooting shoot, final Individual testIndividual) {

        super (body, act, world, game, items, info, weaponry, pathPlanner, pathExecutor, move, raycasting, cardinalRayArray, shoot, testIndividual);
    }

    //__________________________________________________________________________

    /**
     * Hunt the enemy. The bot will go to the position he thinks the enemy is. This
     * position is being updated all the time through EnemyInfo (@see EnemyInfo.class).
     * For example, if the bot hears the enemy picking up something, an event will get
     * triggered where enemyInfo will update the last known position of the enemy; Because
     * this position has changed the hunt state will recalculate the path to this new
     * location.
     * @param enemy Enemy.
     * @param facingSpot Location that the bot has to face.
     * @param enemyInfo Guess/Known information about the enemy.
     */
    public void stateDrivenMovement (final Player enemy, final Location facingSpot, final EnemyInfo enemyInfo) {

        if (crouched) {
            act.act(new SetCrouch ().setCrouch (false));
            crouched = false;
        }

        Location enemyLocation = enemyInfo.getLastKnownLocation ();

        // If we have intel about the enemy's location and this location is not
        // where we are headed
        if (enemyLocation != null && !enemyLocation.equals (stateDrivenDestination)) {
            double lastEncounter = enemyInfo.getLastTimeMet();
            double elapsedTime = Math.abs (lastEncounter - game.getTime ());

            // If the intel is fresh
            if (elapsedTime < 10) {
                // Set the destination to where we think the enemy is
                stateDrivenDestination = enemyLocation;
            }
            // If the intel is not fresh
            else if (elapsedTime < 20) {
                // Set the destination to the closest area to where we think the enemy is
                stateDrivenDestination = Arithmetic.getClosestZone (enemyLocation);
            }

            // If the intel is old and it's not a location that we have already visited, recalculate path
            if (stateDrivenDestination != null && !visitedSpots.contains (stateDrivenDestination)) {
                if (visitedSpots.size () >= 1){
                    visitedSpots.remove (0);
                }

                visitedSpots.add (stateDrivenDestination);

                IPathFuture <ILocated> pathHandle = pathPlanner.computePath(info.getLocation(), stateDrivenDestination);

                if (pathExecutor.isExecuting()) {
                    pathExecutor.stop ();
                }

                pathExecutor.followPath(pathHandle);
            }

            // If the information is not recent, it get's out of this "if" and gets
            // in the next because pathExecutor should be stopped.
        }
        // If we have reached a destination and we don't know where to go, go to
        // a random area
        if (!pathExecutor.isExecuting()) {
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

            if (visitedSpots.size() >= 6) {
                visitedSpots.remove (0);
            }

            stateDrivenDestination = newDestination;
            visitedSpots.add (stateDrivenDestination);

            IPathFuture <ILocated> pathHandle = pathPlanner.computePath(info.getLocation(), stateDrivenDestination);
            pathExecutor.followPath(pathHandle);
        }
    }

    //__________________________________________________________________________

    /**
     * Converts to string the state's name.
     * @return The name of the state.
     */
    public String toString () {
        return "Hunt";
    }
}
