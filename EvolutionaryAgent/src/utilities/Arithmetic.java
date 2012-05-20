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

import bot.T800;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.base3d.worldview.object.Rotation;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;

import java.util.Map;

/**
 * This class contains all those functions used to estimate distances and geometrical
 * calculations.
 *
 * @author Francisco Aisa García
 */

public class Arithmetic {

    // *************************************************************************
    //                            INSTANCE FIELDS
    // *************************************************************************


    /** Constant that represents infinity */
    public static final double INFINITY = 999999999;


    // *************************************************************************
    //                                METHODS
    // *************************************************************************


    /**
     * Transforms a Rotation type object into a Location type object. The object
     * returned can be used to make the bot turn to a fixed position.
     * @param info Agent information.
     * @param rotation The rotation we want to transform.
     * @return The spot ("Location") where the bot should look to rotate as much
     * as rotation indicates.
     */
    public static Location rotationToLocation (final AgentInfo info, final Rotation rotation) {
        // First we'll get the direction vector
        Location directionVector = rotation.toLocation().getNormalized();

        // We will need also bot location beacause we need to compute absolute focus point location
        Location botLocation = info.getLocation();

        // Now we have everything needed for the computation. We normalized
        // our direction vector which is a good idea, because generally you
        // don't know how "big" it is, so we will scale it, so the distance
        // between botlocation and focus point location is big enough
        // (in example below it will be 500 ut units = ~500 cm)
        Location newAbsoluteFocusLocation = botLocation.add (directionVector.scale(500));

        return newAbsoluteFocusLocation;
    }

    //__________________________________________________________________________

    /**
     * It calculates the furthest area to the enemy that is closest to the bot.
     * @param enemyLocation Position of the enemy.
     * @param info Agent information.
     * @return Best run spot to stay away from the enemy.
     */
    public static Location getBestRunZone (final Location enemyLocation, final AgentInfo info) {
        Location targetSpot = null;

        double maxDistToEnemy = 0;
        for (int i = 0; i < T800.areas.length; ++i) {
            Location currentSpot = T800.areas [i].getLocation();
            double distToEnemy = enemyLocation.getDistance (currentSpot);

            if (distToEnemy > maxDistToEnemy && info.getDistance (currentSpot) < distToEnemy) {
                maxDistToEnemy = distToEnemy;
                targetSpot = T800.areas [i].getLocation ();
            }
        }

        return targetSpot;
    }

    //__________________________________________________________________________


    /**
     * It calculates the closest area (meaning weapon spot, shield spot ...) to
     * a given Location.
     * @param spot "Location" from which we want to know where the closest area is.
     * @return The closest area to the spot given.
     */
    public static Location getClosestZone (final Location spot) {
        double minimumDistance, currentDistance;
        Location areaSpot;
        Location newDestination = null;

        if (T800.areas.length > 0) {
            areaSpot = T800.areas [0].getLocation ();
            minimumDistance = spot.getDistance (areaSpot);
            newDestination = areaSpot;

                for (int i = 1; i < T800.areas.length; ++i) {
                areaSpot = T800.areas [i].getLocation ();
                currentDistance = spot.getDistance (areaSpot);

                if (currentDistance < minimumDistance) {
                    minimumDistance = currentDistance;
                    newDestination = areaSpot;
                }
            }
        }

        return newDestination;
    }

    //__________________________________________________________________________


    /**
     * It retrieves the closest item to the bot.
     * @param info Agent information.
     * @param itemsMap List of a certain type of items (it depends on the UnrealId
     * given).
     * @return The closest location from an item to the bot.
     */
    public static Location getClosestItemLocation (final AgentInfo info, final Map <UnrealId, Item> itemsMap) {
        Location newDestination = null;
        double minimumDistance, currentDistance;

        Map.Entry<UnrealId, Item> entry = itemsMap.entrySet().iterator().next();
        if (entry != null) {
            Location firstItemLocation = entry.getValue ().getLocation ();
            minimumDistance = info.getDistance (firstItemLocation);
            newDestination = firstItemLocation;

            for (Item item : itemsMap.values()) {
                currentDistance = info.getDistance (item.getLocation ());

                if (currentDistance < minimumDistance) {
                    minimumDistance = currentDistance;
                    newDestination = item.getLocation ();
                }
            }
        }

        return newDestination;
    }

    //__________________________________________________________________________

    /**
     * It retrieves the closest NavPoint to a given spot. The spot has to be an item's location.
     * @param spot Location of an item.
     * @return The closest NavPoint to the Location given.
     */
    public static NavPoint getClosestPathNodeToLocation (final Location spot) {
        double minimumDistance, currentDistance;
        NavPoint newDestination = null;

        if (T800.pathNodes.length > 0) {
            minimumDistance = spot.getDistance (T800.pathNodes [0].getLocation ());
            newDestination = T800.pathNodes [0];

            for (int i = 1; i < T800.pathNodes.length; ++i) {
                currentDistance = spot.getDistance(T800.pathNodes [i].getLocation());

                if (currentDistance < minimumDistance) {
                    minimumDistance = currentDistance;
                    newDestination = T800.pathNodes [i];
                }
            }
        }

        return newDestination;
    }

    //__________________________________________________________________________

    /**
     * It estimates the height relative to two given spots. We will consider three
     * different results: That spot1 is higher than spot2, that spot1 is more or
     * less at the same height as spot2 or that spot1 is lower than spot2.
     * @param spot1 One location to compare.
     * @param spot2 Other location to compare.
     * @return Depending on the height it returns:
     * -1 if spot2 is lower than spot1.
     * 1 if spot2 is higher than spot1.
     * 0 if spot1 and spot2 are more or less at the same height.
     */
    public static int estimateHeight (final Location spot1, final Location spot2) {
        int height = 0;
        double verticalDistance;

        verticalDistance = spot1.z - spot2.z;

        if (Math.abs (verticalDistance) > 200) {
            if (verticalDistance < 0) {
                height = 1;
            }
            else
                height = -1;
        }

        return height;
    }

    //__________________________________________________________________________

    /**
     * Calculate a random number between lowerBound and upperBound (both included).
     * @param lowerBound Lower bound.
     * @param upperBound Upper bound.
     * @return A random integer between lowerBound and upperBound.
     */
    public static int doRandomNumber (int lowerBound, int upperBound) {
        return (int) (Math.random () * (upperBound - lowerBound + 1) ) + lowerBound;
    }
}
