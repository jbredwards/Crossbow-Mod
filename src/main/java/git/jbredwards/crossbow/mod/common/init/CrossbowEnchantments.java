/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
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
