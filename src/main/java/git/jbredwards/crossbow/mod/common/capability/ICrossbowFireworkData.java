/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.capability;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.util.CapabilityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTPrimitive;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.util.EnumFacing;
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
public interface ICrossbowFireworkData
{
    @CapabilityInject(ICrossbowFireworkData.class)
    @Nonnull Capability<ICrossbowFireworkData> CAPABILITY = null;
    @Nonnull ResourceLocation CAPABILITY_ID = new ResourceLocation(Crossbow.MODID, "crossbow_firework_data");

    boolean wasShotByCrossbow();
    void setShotByCrossbow(boolean flag);

    @Nullable
    static ICrossbowFireworkData get(@Nullable ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attach(@Nonnull AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityFireworkRocket) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY));
    }

    class Impl implements ICrossbowFireworkData
    {
        protected boolean shotByCrossbow;

        @Override
        public boolean wasShotByCrossbow() { return shotByCrossbow; }

        @Override
        public void setShotByCrossbow(boolean flag) { shotByCrossbow = flag; }
    }

    enum Storage implements Capability.IStorage<ICrossbowFireworkData>
    {
        INSTANCE;

        @Nullable
        @Override
        public NBTBase writeNBT(@Nonnull Capability<ICrossbowFireworkData> capability, @Nonnull ICrossbowFireworkData instance, @Nullable EnumFacing side) {
            return new NBTTagByte(instance.wasShotByCrossbow() ? (byte)1 : (byte)0);
        }

        @Override
        public void readNBT(@Nonnull Capability<ICrossbowFireworkData> capability, @Nonnull ICrossbowFireworkData instance, @Nullable EnumFacing side, @Nullable NBTBase nbt) {
            if(nbt instanceof NBTPrimitive) instance.setShotByCrossbow(((NBTPrimitive)nbt).getByte() != 0);
        }
    }
}
