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
import git.jbredwards.crossbow.mod.common.enchantment.EnchantmentCrossbow;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 *
 * @author jbred
 *
 */
@GameRegistry.ObjectHolder(Crossbow.MODID)
public final class CrossbowEnchantments
{
    @GameRegistry.ObjectHolder("multishot")
    public static EnchantmentCrossbow MULTISHOT = null;

    @GameRegistry.ObjectHolder("piercing")
    public static EnchantmentCrossbow PIERCING = null;

    @GameRegistry.ObjectHolder("quick_charge")
    public static EnchantmentCrossbow QUICK_CHARGE = null;
}
