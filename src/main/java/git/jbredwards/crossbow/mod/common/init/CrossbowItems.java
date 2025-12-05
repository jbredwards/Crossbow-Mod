/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.init;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@GameRegistry.ObjectHolder(Crossbow.MODID)
public final class CrossbowItems
{
    @GameRegistry.ObjectHolder("crossbow")
    public static ItemCrossbow CROSSBOW = null;

    @Nonnull
    public static CreativeTabs TAB = new CreativeTabs(Crossbow.MODID + ".tab")
    {
        @Nonnull
        @SideOnly(Side.CLIENT)
        @Override
        public ItemStack createIcon() { return new ItemStack(CROSSBOW); }
    };
}
