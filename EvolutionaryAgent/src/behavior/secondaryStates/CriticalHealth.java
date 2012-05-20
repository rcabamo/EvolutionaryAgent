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
import knowledge.Memoria;
import exceptions.SubStatusException;
import utilities.Arithmetic;

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
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

import java.util.Set;


/**
 * This secondary state should be used when the bot is very low on health. The bot
 * will move constantly to zones where health items can be taken (it uses the
 * information stored in the data base).
 *
 * @author Francisco Aisa García
 */


public class CriticalHealth extends SecondaryState {

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
     */
    public CriticalHealth (final CompleteBotCommandsWrapper body, final IAct act, final IVisionWorldView world,
                           final Items items, final AgentInfo info, final Weaponry weaponry,
                           final IPathPlanner <ILocated> pathPlanner, final IUnrealPathExecutor <ILocated> pathExecutor,
                           final AdvancedLocomotion move, final Raycasting raycasting, final AutoTraceRay orientacionRayArray []) {

        super (body, act, world, items, info, weaponry, pathPlanner, pathExecutor, move, raycasting, orientacionRayArray);
    }

    //__________________________________________________________________________

    /**
     * Uses the data base to look for the closest health pack or health vial. If
     * there are no health vials or health packs available it throws an exception.
     * @param enemy Enemy.
     * @param facingSpot Location where want to make the bot face.
     * @throws SubStatusException Thrown when there are no health packs or health
     * vials available.
     */
    public void executeMovement (final Player enemy, final Location facingSpot) throws SubStatusException {

        if (PrimaryState.crouched) {
            act.act(new SetCrouch ().setCrouch (false));
            PrimaryState.crouched = false;
        }

        // If we haven't looked for health vials or health packs yet
        if (destination == null) {
            Set <String> healthVialBD = Memoria.health.get ("MINI_HEALTH");
            Set <String> healthPackBD = Memoria.health.get ("HEALTH");

            if (info.getHealth () < 100) {
                double minimumDistance = Arithmetic.INFINITY;
                double currentDistance = 0;

                // Look for health packs
                if (!healthPackBD.isEmpty()) {
                    for (String healthPackId : healthPackBD) {
                        Item healthPack = items.getItem (healthPackId);
                        Location healthPackLocation = healthPack.getLocation ();

                        currentDistance = info.getDistance (healthPackLocation);
                        if (currentDistance < minimumDistance && items.isPickupSpawned(healthPack)) {
                            minimumDistance = currentDistance;
                            destination = healthPackLocation;
                        }
                    }
                }
                // If there are no health packs, look for health vials
                else if (!healthVialBD.isEmpty ()) {
                    for (String healthVialId : healthVialBD) {
                        Item healthVial = items.getItem (healthVialId);
                        Location healthVialLocation = healthVial.getLocation ();

                        currentDistance = info.getDistance (healthVialLocation);
                        if (currentDistance < minimumDistance && items.isPickupSpawned(healthVial)) {
                            minimumDistance = currentDistance;
                            destination = healthVialLocation;
                        }
                    }
                }
            }
            else {
                double minimumDistance = Arithmetic.INFINITY;
                double currentDistance = 0;

                // Look for health vials
                if (!healthVialBD.isEmpty ()) {
                    for (String healthVialId : healthVialBD) {
                        Item healthVial = items.getItem (healthVialId);
                        Location healthVialLocation = healthVial.getLocation ();

                        currentDistance = info.getDistance (healthVialLocation);
                        if (currentDistance < minimumDistance && items.isPickupSpawned(healthVial)) {
                            minimumDistance = currentDistance;
                            destination = healthVialLocation;
                        }
                    }
                }
            }

            if (destination == null) {
                throw new SubStatusException ("There are no available health items");
            }

            IPathFuture <ILocated> pathHandle = pathPlanner.computePath (info.getLocation(), destination);

            if (pathExecutor.isExecuting ()) {
                pathExecutor.stop ();
            }

            pathExecutor.followPath (pathHandle);
        }
    }

    //__________________________________________________________________________

    /**
     * Converts to string the state's name.
     * @return The name of the state.
     */
    public String toString () {
        return "CriticalHealth";
    }
}
