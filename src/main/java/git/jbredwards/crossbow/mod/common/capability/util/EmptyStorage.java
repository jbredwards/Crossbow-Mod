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
