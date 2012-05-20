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

import cz.cuni.amis.pogamut.base.communication.worldview.object.WorldObjectId;
import cz.cuni.amis.pogamut.base3d.worldview.IVisionWorldView;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The purpose of this class is to initialize useful information about the environment
 * before the match starts.
 *
 * @author Francisco Aisa García
 */


public class Initialization {

    /**
     * It retrieves all the path nodes from the current level.
     * @param world IVisionWorldView.
     * @return All the path nodes from the current level.
     */
    public static NavPoint [] initializePathNodes (final IVisionWorldView world) {
        Map <WorldObjectId, NavPoint> stageNavPoints = world.getAll(NavPoint.class);

        // Temporary list holding all the path nodes
        List <NavPoint> navPointsList = new ArrayList <NavPoint> ();

        for (NavPoint spot : stageNavPoints.values()) {
            // If it is a path node, we add it to the list of path nodes
            if (!spot.isAIMarker() && !spot.isDoor() && !spot.isDoorOpened() && !spot.isInvSpot()
                && !spot.isItemSpawned() && !spot.isJumpDest() && !spot.isJumpSpot() && !spot.isLiftCenter()
                && !spot.isLiftExit() && !spot.isLiftJumpExit() && !spot.isNoDoubleJump() && !spot.isPlayerStart()
                && !spot.isReachable() && !spot.isRoamingSpot() && !spot.isSnipingSpot() && !spot.isTeleporter()) {

                navPointsList.add(spot);
            }
        }

        // This is the vector that is going to contain all the path nodes and is going to be returned
        NavPoint pathNodes [] = new NavPoint [navPointsList.size()];

        // We copy all the path nodes in the vector
        int i = 0;
        for (NavPoint spot : navPointsList) {
            pathNodes [i] = spot;
            ++i;
        }

        return pathNodes;
    }

    //__________________________________________________________________________

    /**
     * It retrieves all the important areas from the current map (shields, weapons and UDamage).
     * @param items The items from the current map.
     * @return All the important areas from the current map.
     */
    public static NavPoint [] initializeAreas (final Items items) {
        Map <UnrealId, Item> weaponSpots = items.getAllItems(ItemType.Category.WEAPON);
        Map <UnrealId, Item> shieldSpots = items.getAllItems(ItemType.Category.SHIELD);
        Map <UnrealId, Item> UDamageSpots = items.getAllItems(ItemType.Category.OTHER);

        // The areas vector must have size for each important area, therefore:
        NavPoint areas [] = new NavPoint [weaponSpots.size() + shieldSpots.size() + UDamageSpots.size()];

        // In each position of the vector we will put the NavPoint corresponding
        // to an area.
        int i = 0;
        for (Item item : weaponSpots.values()) {
            areas [i] = item.getNavPoint();
            ++i;
        }

        for (Item item : shieldSpots.values()) {
            areas [i] = item.getNavPoint();
            ++i;
        }

        for (Item item : UDamageSpots.values()) {
            areas [i] = item.getNavPoint();
            ++i;
        }

        return areas;
    }
}
