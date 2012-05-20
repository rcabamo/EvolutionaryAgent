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

import enumTypes.rayCardinals;
import knowledge.EnemyInfo;
import evolutionaryComputation.Individual;

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
 * This state is useful when we want to shoot the enemy. If the enemy is on sight,
 * the bot will move in an unpredictable way (making it harder for the enemy
 * to make a hit) and will shoot with the best weapon that is available. If on the
 * other hand the enemy is not on sight, the bot will swing from left to right
 * waiting for the enemy to be on sight again.
 *
 * @author Francisco Aisa García
 */


public class Attack extends PrimaryState {

    // *************************************************************************
    //                            INSTANCE FIELDS
    // *************************************************************************


    /** It contains the side to which we should be moving */
    private boolean pendulum;


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
    public Attack (final CompleteBotCommandsWrapper body, final IAct act, final IVisionWorldView world,
                   final Game game, final Items items, final AgentInfo info, final Weaponry weaponry,
                   final IPathPlanner <ILocated> pathPlanner, final IUnrealPathExecutor <ILocated> pathExecutor,
                   final AdvancedLocomotion move, final Raycasting raycasting, final AutoTraceRay cardinalRayArray [],
                   final ImprovedShooting shoot, final Individual testIndividual) {

        super (body, act, world, game, items, info, weaponry, pathPlanner, pathExecutor, move, raycasting, cardinalRayArray, shoot, testIndividual);
    }

    //__________________________________________________________________________

    /**
     * Makes the bot move randomly. If we are seeing an enemy, the bot will dodge
     * and strafe randomly. If we are not seeing an enemy the bot will strafe from
     * left to right like a pendulum.
     * @param enemy Enemy.
     * @param facingSpot Location that the bot has to face.
     * @param enemyInfo Guess/Known information about the enemy.
     */
    public void stateDrivenMovement (final Player enemy, final Location facingSpot, final EnemyInfo enemyInfo) {

        if (crouched) {
            act.act(new SetCrouch ().setCrouch (false));
            crouched = false;
        }

        if (enemy != null) {
            Location focus = facingSpot;
            if (focus == null) {
                focus = enemy.getLocation ();
            }

            if (pathExecutor.isExecuting()) {
                pathExecutor.stop ();
            }

            if (!raycasting.getAllRaysInitialized().getFlag()) return;

            double eastDistance = info.getDistance(cardinalRayArray [rayCardinals.EAST.ordinal ()].getHitLocation());
            double westDistance = info.getDistance(cardinalRayArray [rayCardinals.WEST.ordinal ()].getHitLocation());

            Random rand = new Random();
            int chance = rand.nextInt(100);
            if (chance < 30) {
                if (westDistance > 200) {
                    move.strafeLeft(200, focus);
                }
                else {
                    move.strafeRight(200, focus);
                }
            }
            else if (chance < 60) {
                if (eastDistance > 200) {
                    move.strafeRight(200, focus);
                }
                else {
                    move.strafeLeft(200, focus);
                }
            }
            else if (chance < 80) {
                if (westDistance > 400) {
                    move.dodge (new Location (0, -1, 0), false);
                }
                else {
                    move.dodge (new Location (0, 1, 0), false);
                }
            }
            else if (chance < 100) {
                if (eastDistance > 400) {
                    move.dodge (new Location (0, 1, 0), false);
                }
                else {
                    move.dodge (new Location (0, -1, 0), false);
                }
            }
        }
        else {
            if (pathExecutor.isExecuting()) {
                pathExecutor.stop ();
            }

            if (!raycasting.getAllRaysInitialized().getFlag()) return;

            double eastDistance = info.getDistance(cardinalRayArray [rayCardinals.EAST.ordinal ()].getHitLocation());
            double westDistance = info.getDistance(cardinalRayArray [rayCardinals.WEST.ordinal ()].getHitLocation());

            // Pendulum tells us where to strafe
            if (!pendulum) {
                if (westDistance > 200) {
                    move.strafeLeft(200);
                }
                else {
                    move.strafeRight(200);
                }

                pendulum = true;
            }
            else {
                if (eastDistance > 200) {
                    move.strafeRight(200);
                }
                else {
                    move.strafeLeft(200);
                }

                pendulum = false;
            }
        }
    }

    //__________________________________________________________________________

    /**
     * Converts to string the state's name.
     * @return The name of the state.
     */
    public String toString () {
        return "Attack";
    }
}