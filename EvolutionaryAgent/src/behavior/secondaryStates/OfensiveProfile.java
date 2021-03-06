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

package behavior.secondaryStates;

import behavior.primaryStates.PrimaryState;
import exceptions.SubStatusException;

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
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.SetCrouch;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;


/**
 * This secondary state makes the bot go closer to the enemy. It is only useful
 * if the enemy is on sight.
 *
 * @author Francisco Aisa García
 */


public class OfensiveProfile extends SecondaryState {


    // *************************************************************************
    //                               METHODS
    // *************************************************************************


    /**
     * Argument based constructor.
     * @param body body field from T800.
     * @param act act field from T800.
     * @param world world field from T800.
     * @param items items field from T800.
     * @param info info field from T800.
     * @param weaponry weaponry field from T800.
     * @param pathPlanner pathPlanner field from T800.
     * @param pathExecutor pathExecutor field from T800.
     * @param move move field from T800.
     * @param raycasting raycasting field from T800.
     * @param cardinalRayArray cardinalRayArray field from T800.
     */
    public OfensiveProfile (final CompleteBotCommandsWrapper body, final IAct act, final IVisionWorldView world,
                           final Items items, final AgentInfo info, final Weaponry weaponry,
                           final IPathPlanner <ILocated> pathPlanner, final IUnrealPathExecutor <ILocated> pathExecutor,
                           final AdvancedLocomotion move, final Raycasting raycasting, final AutoTraceRay cardinalRayArray []) {

        super (body, act, world, items, info, weaponry, pathPlanner, pathExecutor, move, raycasting, cardinalRayArray);
    }

    //__________________________________________________________________________

    /**
     * Advance to the enemy's location if he is visible, throw an exception otherwise.
     * @param enemy Enemy.
     * @param facingSpot Location where want to make the bot face.
     * @throws SubStatusException Thrown when the enemy is not visible.
     */
    public void executeMovement (final Player enemy, final Location facingSpot) throws SubStatusException {

        if (PrimaryState.crouched) {
            act.act(new SetCrouch ().setCrouch (false));
            PrimaryState.crouched = false;
        }

        if (enemy == null) {
            move.stopMovement ();
            throw new SubStatusException ("No se puede activar perfil ofensivo, enemy no visible");
        }
        else {
            //move.moveTo (enemy.getLocation ());

            Location enemyLocation = enemy.getLocation ();

            if (info.getDistance (enemyLocation) < 600) {
                if (pathExecutor.isExecuting ()) {
                    pathExecutor.stop ();
                }

                move.moveTo (enemyLocation);
            }
            else if (destination == null || (destination != null && enemyLocation.getDistance (destination) > 600)) {
                destination = enemyLocation;
                IPathFuture <ILocated> pathHandle = pathPlanner.computePath (info.getLocation (), destination);

                if (pathExecutor.isExecuting ()) {
                    pathExecutor.stop ();
                }

                pathExecutor.followPath (pathHandle);
            }
        }
    }

    //__________________________________________________________________________

    /**
     * Converts to string the state's name.
     * @return The name of the state.
     */
    public String toString () {
        return "OfensiveProfile";
    }
}
