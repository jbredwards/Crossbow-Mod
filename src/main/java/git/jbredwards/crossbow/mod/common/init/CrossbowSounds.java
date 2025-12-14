/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.crossbow.mod.common.init;

import git.jbredwards.crossbow.mod.common.Crossbow;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 *
 * @author jbred
 *
 */
@GameRegistry.ObjectHolder(Crossbow.MODID)
public final class CrossbowSounds
{
    @GameRegistry.ObjectHolder("items.crossbow.hit")
    public static SoundEvent ITEM_CROSSBOW_HIT = null;

    @GameRegistry.ObjectHolder("items.crossbow.loading.end")
    public static SoundEvent ITEM_CROSSBOW_LOADING_END = null;

    @GameRegistry.ObjectHolder("items.crossbow.loading.middle")
    public static SoundEvent ITEM_CROSSBOW_LOADING_MIDDLE = null;

    @GameRegistry.ObjectHolder("items.crossbow.loading.start")
    public static SoundEvent ITEM_CROSSBOW_LOADING_START = null;

    @GameRegistry.ObjectHolder("items.crossbow.quick_charge_1")
    public static SoundEvent ITEM_CROSSBOW_QUICK_CHARGE_1 = null;

    @GameRegistry.ObjectHolder("items.crossbow.quick_charge_2")
    public static SoundEvent ITEM_CROSSBOW_QUICK_CHARGE_2 = null;

    @GameRegistry.ObjectHolder("items.crossbow.quick_charge_3")
    public static SoundEvent ITEM_CROSSBOW_QUICK_CHARGE_3 = null;

    @GameRegistry.ObjectHolder("items.crossbow.shoot")
    public static SoundEvent ITEM_CROSSBOW_SHOOT = null;
}
