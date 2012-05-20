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

import behavior.secondaryStates.SecondaryState;
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
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Raycasting;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensomotoric.Weaponry;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.bot.command.AdvancedLocomotion;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType.Category;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.SetCrouch;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.AutoTraceRay;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

import java.util.Map;


/**
 * This state is used to pickup items. When used, the bot will move through the
 * level, picking up those items that are most useful.
 * Use it, when the enemy is not on sight.
 *
 * @author Francisco Aisa García
 */


public class Greedy extends PrimaryState {

    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************


    /** Priority of execution (usefull to know when to interrupt the execution) */
    private int executionPriority = 0;


    // *************************************************************************
    //                               METHODS
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
    public Greedy (final CompleteBotCommandsWrapper body, final IAct act, final IVisionWorldView world,
                     final Game game, final Items items, final AgentInfo info, final Weaponry weaponry,
                     final IPathPlanner <ILocated> pathPlanner, final IUnrealPathExecutor <ILocated> pathExecutor,
                     final AdvancedLocomotion move, final Raycasting raycasting, final AutoTraceRay cardinalRayArray [],
                     final ImprovedShooting shoot, final Individual testIndividual) {

        super (body, act, world, game, items, info, weaponry, pathPlanner, pathExecutor, move, raycasting, cardinalRayArray, shoot, testIndividual);
    }

    //__________________________________________________________________________

    /**
     * Keeps the bot walking to the closests path nodes until he finds items. He
     * will start picking up items based on how bad he needs them.
     * @param enemy Enemy.
     * @param facingSpot Location that we want to make the bot face.
     * @param enemyInfo Guess/Known information about the enemy.
     */
    public void stateDrivenMovement (final Player enemy, final Location facingSpot, final EnemyInfo enemyInfo) {

        if (crouched) {
            act.act(new SetCrouch ().setCrouch (false));
            crouched = false;
        }

        Map <UnrealId, Item> visibleItems = items.getVisibleItems ();

        // If we can see items, lets see which one is the one that suit us the best
        if (!visibleItems.isEmpty ()) {
            Location newDestination = null;

            int maximumPriority = -1;
            int itemPriority;

            double maximumPriorityDistance = Arithmetic.INFINITY;
            double itemPriorityDistance = 0;

            for (Item item : visibleItems.values()) {
                itemPriority = estimateItemPriority (item, info, weaponry);
                Location itemLocation = item.getLocation();

                if (itemPriority > maximumPriority) {
                    maximumPriorityDistance = info.getDistance (itemLocation);
                    maximumPriority = itemPriority;
                    newDestination = itemLocation;
                }
                else if (itemPriority == maximumPriority) {
                    itemPriorityDistance = info.getDistance (itemLocation);
                    if (itemPriorityDistance < maximumPriorityDistance) {
                        maximumPriorityDistance = itemPriorityDistance;
                        maximumPriority = itemPriority;
                        newDestination = itemLocation;
                    }
                }
            }

            // If we can see an item that is more important than the one we are
            // going for, recalculate path
            if (maximumPriority > executionPriority && newDestination != null) {
                executionPriority = maximumPriority;

                IPathFuture <ILocated> pathHandle = pathPlanner.computePath (info.getLocation(), newDestination);

                if (pathExecutor.isExecuting ()) {
                    pathExecutor.stop ();
                }

                pathExecutor.followPath (pathHandle);
            }
        }
        // If we can't see any items and the pathExecutor is not executing, then,
        // move to the closest spot we haven't recently visited
        if (!pathExecutor.isExecuting ()) {
            Location newDestination = null;
            double minimumDistance = Arithmetic.INFINITY;
            double currentDistance;

            for (int i = 0; i < T800.pathNodes.length; ++i) {
                currentDistance = info.getDistance (T800.pathNodes [i].getLocation ());

                if (currentDistance < minimumDistance && !visitedSpots.contains (T800.pathNodes [i].getLocation ())) {
                    newDestination = T800.pathNodes [i].getLocation ();
                }
            }

            if (visitedSpots.size () == 5) {
                visitedSpots.remove (0);
            }

            visitedSpots.add (newDestination);
            executionPriority = 1;

            IPathFuture <ILocated> pathHandle = pathPlanner.computePath (info.getLocation (), newDestination);
            pathExecutor.followPath (pathHandle);
        }
    }


    //__________________________________________________________________________

    /**
     * When we reach the destination, we change the execution priority.
     * @param subState Sub state that is being executed.
     */
    @Override
    public void destinationReached (final SecondaryState subState) {
        executionPriority = 0;
        super.destinationReached (subState);
    }

    //__________________________________________________________________________

    /**
     * Change the value of the execution priority and then call the super function.
     * @param subState Sub state that is being executed.
     */
    @Override
    public void stopExecution (final SecondaryState subState) {
        executionPriority = 0;
        super.stopExecution (subState);
    }

    //__________________________________________________________________________

    /**
     * Estimate the priority of an item based on our status.
     * @param item Item we want to evaluate.
     * @param info Agent information.
     * @param weaponry Weaponry that we are carrying.
     * @return -1 if the item isn't in the list of items considered or else, a value between
     * 0 and 100 indicating how good the item is.
     */
    protected int estimateItemPriority (final Item item, final AgentInfo info, final Weaponry weaponry) {
        ItemType type = item.getType();
        int health = info.getHealth();
        int priority;

        if (type.equals(ItemType.MINI_HEALTH_PACK)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                priority = 90;
            }
            else if (health < 100) {
                priority = 65;
            }
            else if (health < 150) {
                priority = 75;
            }
            else {
                priority = 65;
            }
        }
        else if (type.equals(ItemType.HEALTH_PACK)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                priority = 95;
            }
            else if (health < 100) {
                priority = 75;
            }
            else {
                priority = 0;
            }
        }
        else if (type.equals(ItemType.SUPER_SHIELD_PACK)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                priority = 99;
            }
            else if (health < 100) {
                priority = 99;
            }
            else if (health < 150) {
                priority = 99;
            }
            else {
                priority = 99;
            }
        }
        else if (type.equals(ItemType.SHIELD_PACK)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                priority = 85;
            }
            else if (health < 100) {
                priority = 98;
            }
            else if (health < 150) {
                priority = 98;
            }
            else {
                priority = 98;
            }
        }
        else if (type.equals (ItemType.U_DAMAGE_PACK)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                priority = 100;
            }
            else if (health < 100) {
                priority = 100;
            }
            else if (health < 150) {
                priority = 100;
            }
            else {
                priority = 100;
            }
        }
        else if (type.equals (ItemType.ADRENALINE_PACK)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                priority = 40;
            }
            else if (health < 100) {
                priority = 40;
            }
            else if (health < 150) {
                priority = 40;
            }
            else {
                priority = 40;
            }
        }
        else if (type.getCategory().equals(Category.AMMO)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                priority = 20;
            }
            else if (health < 100) {
                priority = 20;
            }
            else if (health < 150) {
                priority = 20;
            }
            else {
                priority = 20;
            }
        }
        else if (type.equals (ItemType.BIO_RIFLE)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 50;
                }
                else {
                    priority = 25;
                }
            }
            else if (health < 100) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 55;
                }
                else {
                    priority = 25;
                }
            }
            else if (health < 150) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
        }
        else if (type.equals (ItemType.LINK_GUN)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 55;
                }
                else {
                    priority = 30;
                }
            }
            else if (health < 100) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
            else if (health < 150) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 65;
                }
                else {
                    priority = 40;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
        }
        else if (type.equals (ItemType.MINIGUN)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 65;
                }
                else {
                    priority = 40;
                }
            }
            else if (health < 100) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
            else if (health < 150) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
        }
        else if (type.equals (ItemType.FLAK_CANNON)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
            else if (health < 100) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 85;
                }
                else {
                    priority = 60;
                }
            }
            else if (health < 150) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 85;
                }
                else {
                    priority = 60;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 85;
                }
                else {
                    priority = 60;
                }
            }
        }
        else if (type.equals (ItemType.ROCKET_LAUNCHER)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 60;
                }
                else {
                    priority = 35;
                }
            }
            else if (health < 100) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
            else if (health < 150) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 70;
                }
                else {
                    priority = 45;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 75;
                }
                else {
                    priority = 50;
                }
            }
        }
        else if (type.equals (ItemType.SHOCK_RIFLE)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 80;
                }
                else {
                    priority = 55;
                }
            }
            else if (health < 100) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 95;
                }
                else {
                    priority = 80;
                }
            }
            else if (health < 150) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 95;
                }
                else {
                    priority = 80;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 95;
                }
                else {
                    priority = 80;
                }
            }
        }
        else if (type.equals (ItemType.SNIPER_RIFLE) || type.equals(ItemType.LIGHTNING_GUN)) {
            // Critical health, just a single shot of sniper can kill us
            if (health < 70) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 75;
                }
                else {
                    priority = 50;
                }
            }
            else if (health < 100) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 90;
                }
                else {
                    priority = 65;
                }
            }
            else if (health < 150) {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 90;
                }
                else {
                    priority = 65;
                }
            }
            else {
                if (!weaponry.hasWeapon(type) || !weaponry.isLoaded(type)) {
                    priority = 90;
                }
                else {
                    priority = 65;
                }
            }
        }
        else {
            priority = -1;
        }


        return priority;
    }

    //__________________________________________________________________________

    /**
     * Converts to string the state's name.
     * @return The name of the state.
     */
    public String toString () {
        return "Greedy";
    }
}
