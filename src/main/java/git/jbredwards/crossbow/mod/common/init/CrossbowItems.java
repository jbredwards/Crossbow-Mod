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

        @Override
        public boolean hasSearchBar() { return true; }
    }.setBackgroundImageName("item_search.png");
}
