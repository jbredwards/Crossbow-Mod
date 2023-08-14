/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.capability.util;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
public class EmptyStorage<T> implements Capability.IStorage<T>
{
    @Nullable
    @Override
    public NBTBase writeNBT(@Nonnull Capability<T> capability, @Nonnull T instance, @Nullable EnumFacing side) {
        // NO-OP
        return new NBTTagByte((byte)0);
    }

    @Override
    public void readNBT(@Nonnull Capability<T> capability, @Nonnull T instance, @Nullable EnumFacing side, @Nullable NBTBase nbt) {
        // NO-OP
    }
}
