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

package git.jbredwards.crossbow.mod.common.capability;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.util.CapabilityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("ConstantConditions")
public interface ICrossbowSoundData
{
    @CapabilityInject(ICrossbowSoundData.class)
    @Nonnull Capability<ICrossbowSoundData> CAPABILITY = null;
    @Nonnull ResourceLocation CAPABILITY_ID = new ResourceLocation(Crossbow.MODID, "crossbow_sound_data");

    boolean getPlayedLoadingStartSound();
    void setPlayedLoadingStartSound(boolean playChargeSoundIn);

    boolean getPlayedLoadingMiddleSound();
    void setPlayedLoadingMiddleSound(boolean playLoadSoundIn);

    @Nullable
    static ICrossbowSoundData get(@Nullable ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attach(@Nonnull AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityLivingBase) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY));
    }

    class Impl implements ICrossbowSoundData
    {
        protected boolean playedChargeSound, playedLoadSound;

        @Override
        public boolean getPlayedLoadingStartSound() { return playedChargeSound; }

        @Override
        public void setPlayedLoadingStartSound(boolean playedChargeSoundIn) { playedChargeSound = playedChargeSoundIn; }

        @Override
        public boolean getPlayedLoadingMiddleSound() { return playedLoadSound; }

        @Override
        public void setPlayedLoadingMiddleSound(boolean playedLoadSoundIn) { playedLoadSound = playedLoadSoundIn; }
    }
}
