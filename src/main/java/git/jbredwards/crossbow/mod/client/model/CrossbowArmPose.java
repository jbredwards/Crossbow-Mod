/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
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
