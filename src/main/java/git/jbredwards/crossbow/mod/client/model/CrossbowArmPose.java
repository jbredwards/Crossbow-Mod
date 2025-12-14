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

package git.jbredwards.crossbow.mod.client.model;

import git.jbredwards.crossbow.mod.common.Crossbow;
import net.minecraft.client.model.ModelBiped;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class CrossbowArmPose
{
    @Nonnull
    public static final ModelBiped.ArmPose
            CHARGE = Objects.requireNonNull(EnumHelper.addEnum(ModelBiped.ArmPose.class, Crossbow.MODID + "_crossbow_charge", new Class[0])),
            HOLD = Objects.requireNonNull(EnumHelper.addEnum(ModelBiped.ArmPose.class, Crossbow.MODID + "_crossbow_hold", new Class[0]));

    // initialize new enums during mod construction to prevent errors down the line
    public static void init() {}
}
