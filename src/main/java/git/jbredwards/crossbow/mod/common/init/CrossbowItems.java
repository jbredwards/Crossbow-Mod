/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.init;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraftforge.fml.common.registry.GameRegistry;

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
}
