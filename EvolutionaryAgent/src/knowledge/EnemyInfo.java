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

import utilities.Arithmetic;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Game;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.bot.command.CompleteBotCommandsWrapper;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.HearPickup;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;


/**
 * This class stores guessed information about the enemy. It's mainly useful to
 * decide when to fight or retreat.
 *
 * @author Francisco Aisa García
 * @author Ricardo Caballero Moral
 */


public class EnemyInfo {

    // *************************************************************************
    //                             INSTANCE FIELDS
    // *************************************************************************


    /** Pointer to the field body from T800 (used for debugging) */
    protected CompleteBotCommandsWrapper body;
    /** Enemy's name */
    private String name;
    /** Health that we suppose the enemy has */
    private int health;
    /** Armor that we suppose the enemy has */
    private int armor;
    /** List of weapons that we suppose the enemy has */
    private boolean weaponry [];
    /** Location where we know/guess the enemy is */
    private Location currentPosition;
    /** NavPoint where we know/guess the enemy is */
    private NavPoint navPointPosition;
    /** Game time where we saw for the last time the enemy */
    private double lastEncounter;


    // *************************************************************************
    //                               CONSTANTS
    // *************************************************************************


    /** Constant that refers to health vials */
    public final static int HEALTH_VIAL = 0;
    /** Constant that refers to health packs */
    public final static int HEALTH_PACK = 1;

    /** Constant that identifies the position of the shield gun in the arsenal vector */
    public final static int SHIELD_GUN = 0;
    /** Constant that identifies the position of the assault rifle in the arsenal vector */
    public final static int ASSAULT_RIFLE = 1;
    /** Constant that identifies the position of the bio rifle in the arsenal vector */
    public final static int BIO_RIFLE = 2;
    /** Constant that identifies the position of the link gun in the arsenal vector */
    public final static int LINK_GUN = 3;
    /** Constant that identifies the position of the minigun in the arsenal vector */
    public final static int MINIGUN = 4;
    /** Constant that identifies the position of the flak cannon in the arsenal vector */
    public final static int FLAK_CANNON = 5;
    /** Constant that identifies the position of the rocket launcher in the arsenal vector */
    public final static int ROCKET_LAUNCHER = 6;
    /** Constant that identifies the position of the shock rifle in the arsenal vector */
    public final static int SHOCK_RIFLE = 7;
    /** Constant that identifies the position of the lightning gun in the arsenal vector */
    public final static int LIGHTNING_GUN = 8;
    /** Constant that identifies the position of the sniper rifle in the arsenal vector */
    public final static int SNIPER_RIFLE = 8;


    // *************************************************************************
    //                               METHODS
    // *************************************************************************


    /**
     * Argument based constructor.
     * @param body Pointer to the field body in T800.
     */
    public EnemyInfo (final CompleteBotCommandsWrapper body) {
        this.body = body;
        health = 100;
        armor = 0;
        navPointPosition = null;
        currentPosition = null;
        lastEncounter = -1;
        name = "";

        weaponry = new boolean [9];
        weaponry [0] = weaponry [1] = true;
        for (int i = 2; i < weaponry.length; ++i) {
            weaponry [i] = false;
        }
    }

    //__________________________________________________________________________

    /**
     * Argument based constructor.
     * @param health Initial health of the enemy.
     * @param armor Initial armor of the enemy.
     * @param weaponry Initial weaponry of the enemy.
     */
    public EnemyInfo (final int health, final int armor, final boolean weaponry []) {
        this.health = health;
        this.armor = armor;
        this.weaponry = weaponry;
    }

    //__________________________________________________________________________

    /**
     * It resets all the information that we are guessing about the bot, to its
     * default values (except for the player's name).
     * Use it when the enemy dies.
     */
    public void reset () {
        health = 100;
        armor = 0;
        navPointPosition = null;
        currentPosition = null;
        lastEncounter = -1;

        weaponry [0] = weaponry [1] = true;
        for (int i = 2; i < weaponry.length; ++i) {
            weaponry [i] = false;
        }
    }

    //__________________________________________________________________________

    /**
     * Updates the location where we think/know the enemy is.
     * @param enemyLocation Location where we think the enemy is.
     * @param clockTime Game time when we guessed/saw where the enemy was.
     */
    public void updateEnemyLocation (final Location enemyLocation, final double clockTime) {
        lastEncounter = clockTime;
        currentPosition = enemyLocation;
    }

    //__________________________________________________________________________

    /**
     * Updates the NavPoint where we think/know the enemy is.
     * @param enemyNavPoint NavPoint where we think the enemy is.
     * @param clockTime Game time when we guessed/saw where the enemy was.
     */
    public void updateEnemyNavPoint (final NavPoint enemyNavPoint, final double clockTime) {
        lastEncounter = clockTime;
        navPointPosition = enemyNavPoint;
        currentPosition = enemyNavPoint.getLocation();
    }

    //__________________________________________________________________________

    /**
     * It returns the time when we saw for the last time the enemy.
     * @return Game time when we saw for the last time the enemy. -1 if we haven't
     * still seen the enemy.
     */
    public double getLastTimeMet () {
        return lastEncounter;
    }

    //__________________________________________________________________________

    /**
     * It returns the location where we saw for the last time the enemy.
     * @return Location where we saw for the last time the enemy. Null if we haven't
     * still seen the enemy.
     */
    public Location getLastKnownLocation () {
        return currentPosition;
    }

    //__________________________________________________________________________

    /**
     * It returns the NavPoint where we saw for the last time the enemy.
     * @return NavPoint where we saw for the last time the enemy. Null if we haven't
     * still seen the enemy.
     */
    public NavPoint getLastKnownNavPoint () {
        if (navPointPosition == null && currentPosition != null) {
            return Arithmetic.getClosestPathNodeToLocation (currentPosition);
        }

        // Si navPoint Posicion no era null, se devuelve ese valor, si es null
        // y tambien la localizacion, quiere decir que no sabemos donde esta el
        // enemigo, de modo que se devolvera null, y si la posicion es distinta de
        // null, calculamos el navPoint mas cercano
        return navPointPosition;
    }

    //__________________________________________________________________________

    /**
     * Set the enemy name.
     * @param name Enemy's name.
     */
    public void setName (final String name) {
        this.name = name;
    }

    //__________________________________________________________________________

    /**
     * Get the name of the enemy.
     * @return The enemy's name.
     */
    public String getName () {
        return name;
    }

    //__________________________________________________________________________

    /**
     * Cleans out the enemy's name.
     */
    public void eraseName () {
        name = "";
    }

    //__________________________________________________________________________

    /**
     * Sets the enemy's health
     * @param health Health we want to set.
     */
    public void setHealth (final int health) {
        this.health = health;
    }

    //__________________________________________________________________________

    /**
     * Set the enemy's armor.
     * @param armor Armor we want to sent.
     */
    public void setArmor (final int armor) {
        this.armor = armor;
    }

    //__________________________________________________________________________

    /**
     * It changes the value of a weapon from the enemy. If we think/know he has
     * a weapon (or not) we can update it through this function.
     * @param weapon Weapon we want to update.
     * @param isHoldingWeapon If he has this weapon.
     */
    public void setWeapon (final int weapon, final boolean isHoldingWeapon) {
        weaponry [weapon] = isHoldingWeapon;
    }

    //__________________________________________________________________________

    /**
     * It returns the complete vector containing the state of the weapons.
     * @return Weapons state vector.
     */
    public boolean [] getArsenal () {
        return weaponry;
    }

    //__________________________________________________________________________

    /** Get the enemy's health */
    public int getHealth () {
        return health;
    }

    //__________________________________________________________________________

     /** Get the enemy's armor */
    public int getArmor () {
        return armor;
    }

    //__________________________________________________________________________

    /** Get the enemy's health + armor points */
    public int getHealthArmor () {
        return health + armor;
    }

    //__________________________________________________________________________

    /**
     * Returns the state of an enemy's weapon.
     * @param weapon Weapon of which we want to know it's state.
     * @return The state of the weapon given.
     */
    public boolean hasWeapon (final int weapon) {
        return weaponry [weapon];
    }

    //__________________________________________________________________________

    /**
     * Increase enemy's health
     * @param type Type of health we want to update. 0 for health vial and 1 for
     * health pack (use the constants above).
     */
    public void increaseHealth (final int type) {
        if (type == HEALTH_VIAL) {
            health = health + 5;

            if (health > 199) {
                health = 199;
            }
        }
        else if (type == HEALTH_PACK) {
            if (health < 100) {
                health = health + 25;

                if (health > 100) {
                    health = 100;
                }
            }
        }
    }

    //__________________________________________________________________________

    /**
     * Increase enemy's armor
     * @param armorPoints Amount of points we want to increase.
     */
    public void increaseArmor (final int armorPoints) {
        armor = armor + armorPoints;

        if (armor > 150) {
            armor = 150;
        }
    }

    //__________________________________________________________________________

    /**
     * Update the enemy's health/armor based on how much damage it has taken.
     * @param damage health/armor points we want to substract from the enemy.
     */
    public void hit (int damage) {
        if (armor > 0) {
            armor = armor - damage;

            if (armor < 0) {
                damage = Math.abs (armor);
                armor = 0;
            }

            health = health - damage;
            if (health < 0) {
                health = 0;
            }
        }
        else {
            health = health - damage;
            if (health < 0) {
                health = 0;
            }
        }
    }

    //__________________________________________________________________________

    /**
     * Sets to true a weapon in the weaponry vector, meaning we assume the enemy
     * has that weapon.
     * @param weaponName Name of the weapon we are assuming the enemy has.
     */
    public void updateWeapon (String weaponName) {
        if (weaponName == null) {
            return;
        }
        if(weaponName.equals("XWeapons.BioRifle")) {
            weaponry [BIO_RIFLE] = true;
        }
        else if (weaponName.equals("XWeapons.LinkGun")) {
            weaponry [LINK_GUN] = true;
        }
        else if (weaponName.equals("XWeapons.Minigun")) {
            weaponry [MINIGUN] = true;
        }
        else if (weaponName.equals("XWeapons.FlakCannon")) {
            weaponry [FLAK_CANNON] = true;
        }
        else if (weaponName.equals("XWeapons.RocketLauncher")) {
            weaponry [ROCKET_LAUNCHER] = true;
        }
        else if (weaponName.equals("XWeapons.ShockRifle")) {
            weaponry [SHOCK_RIFLE] = true;
        }
        else if (weaponName.equals("XWeapons.SniperRifle")) {
            weaponry [LIGHTNING_GUN] = true;
        }
    }


    //__________________________________________________________________________

    /**
     * Update relevant information from the enemy based on a pickup noise. For
     * example, if the enemy picks up health or armor, we will update the amount
     * of health/armor we suppose he has. It also updates the location where we
     * supposed the enemy is.
     * @param noise HearPickup.
     * @param info Agent information.
     * @param game Game information.
     * @param items Items from the current map.
     */
    public void hearPickup (final HearPickup noise, final AgentInfo info, final Game game, final Items items) {

        Item item = items.getItem (noise.getSource ());

        if (item != null && info.getDistance(item.getLocation ()) > 200) {

            // SOUNDS RELATED TO HEALTH ITEMS OR ARMOR ITEMS

            // If we heared the small shield
            if (noise.getType().equals("XPickups.ShieldPack")) {
                increaseArmor(50);
            }
            // If we heared the super shield
            else if(noise.getType().equals("XPickups.SuperShieldPack")) {
                increaseArmor(100);
            }
            // If we heared a health pack
            else if(noise.getType().equals("XPickups.HealthPack")) {
                increaseHealth(HEALTH_PACK);
            }
            // If we heared a health vial
            else if(noise.getType().equals("XPickups.MiniHealthPack")) {
                increaseHealth(HEALTH_VIAL);
            }

            // SOUNDS RELATED TO WEAPONS

            else if(noise.getType().equals("XWeapons.BioRiflePickup")) {
                weaponry [BIO_RIFLE] = true;
            }
            else if (noise.getType().equals("XWeapons.LinkGunPickup")) {
                weaponry [LINK_GUN] = true;
            }
            else if (noise.getType().equals("XWeapons.MinigunPickup")) {
                weaponry [MINIGUN] = true;
            }
            else if (noise.getType().equals("XWeapons.FlakCannonPickup")) {
                weaponry [FLAK_CANNON] = true;
            }
            else if (noise.getType().equals("XWeapons.RocketLauncherPickup")) {
                weaponry [ROCKET_LAUNCHER] = true;
            }
            else if (noise.getType().equals("XWeapons.ShockRiflePickup")) {
                weaponry [SHOCK_RIFLE] = true;
            }
            else if (noise.getType().equals("XWeapons.SniperRiflePickup")) {
                weaponry [LIGHTNING_GUN] = true;
            }

            // ENEMY LOCATION

            if (item.getNavPoint() != null) {
                updateEnemyNavPoint (item.getNavPoint (), game.getTime ());
            }
        }
    }
}
