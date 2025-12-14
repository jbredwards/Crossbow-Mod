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

package git.jbredwards.crossbow.mod.common.capability.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A general purpose capability provider class
 * @author jbred
 *
 */
public final class CapabilityProvider<T> implements ICapabilitySerializable<NBTBase>
{
    @Nonnull final Capability<T> capability;
    @Nullable final T instance;

    public CapabilityProvider(@Nonnull Capability<T> capabilityIn) {
        this(capabilityIn, capabilityIn.getDefaultInstance());
    }

    public CapabilityProvider(@Nonnull Capability<T> capabilityIn, @Nullable T instanceIn) {
        capability = capabilityIn;
        instance = instanceIn;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capabilityIn, @Nullable EnumFacing facing) {
        return capabilityIn == capability;
    }

    @Nullable
    @Override
    public <t> t getCapability(@Nonnull Capability<t> capabilityIn, @Nullable EnumFacing facing) {
        return capabilityIn == capability ? capability.cast(instance) : null;
    }

    @Nullable
    @Override
    public NBTBase serializeNBT() { return capability.writeNBT(instance, null); }

    @Override
    public void deserializeNBT(@Nonnull NBTBase nbt) { capability.readNBT(instance, null, nbt); }
}
