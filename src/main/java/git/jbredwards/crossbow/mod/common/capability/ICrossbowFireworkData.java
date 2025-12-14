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
import git.jbredwards.crossbow.mod.common.network.MessageSyncFireworkData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

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

    @Nullable
    UUID getOwnerUUID();
    void setOwnerUUID(@Nullable UUID ownerUUIDIn);

    @Nullable
    Entity getOwner();
    void setOwner(@Nullable Entity owner);

    boolean wasShotByCrossbow();
    void setShotByCrossbow(boolean flag);

    boolean isShotAtAngle();
    void setShotAtAngle(boolean flag);

    @Nullable
    static ICrossbowFireworkData get(@Nullable ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attach(@Nonnull AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityFireworkRocket) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, new Impl(event.getObject().world)));
    }

    @SubscribeEvent
    static void sync(@Nonnull PlayerEvent.StartTracking event) {
        if(event.getEntityPlayer() instanceof EntityPlayerMP) {
            final ICrossbowFireworkData cap = get(event.getTarget());
            if(cap != null) Crossbow.WRAPPER.sendTo(new MessageSyncFireworkData(event.getTarget().getEntityId(), cap), (EntityPlayerMP)event.getEntityPlayer());
        }
    }

    class Impl implements ICrossbowFireworkData
    {
        @Nullable protected UUID ownerUUID;
        @Nullable protected Entity cachedOwner;
        @Nullable protected World world;
        protected boolean shotByCrossbow, shotAtAngle;

        public Impl() {}
        public Impl(@Nullable World worldIn) { world = worldIn; }

        @Nullable
        @Override
        public UUID getOwnerUUID() { return ownerUUID; }

        @Override
        public void setOwnerUUID(@Nullable UUID ownerUUIDIn) {
            ownerUUID = ownerUUIDIn;
            cachedOwner = null;
        }

        @Nullable
        @Override
        public Entity getOwner() {
            if(cachedOwner != null && cachedOwner.isAddedToWorld()) return cachedOwner;
            return ownerUUID != null && world instanceof WorldServer ? (cachedOwner = ((WorldServer)world).getEntityFromUuid(ownerUUID)) : null;
        }

        @Override
        public void setOwner(@Nullable Entity owner) {
            if(owner != null) {
                ownerUUID = owner.getUniqueID();
                cachedOwner = owner;
            }
        }

        @Override
        public boolean wasShotByCrossbow() { return shotByCrossbow; }

        @Override
        public void setShotByCrossbow(boolean flag) { shotByCrossbow = flag; }

        @Override
        public boolean isShotAtAngle() { return shotAtAngle; }

        @Override
        public void setShotAtAngle(boolean flag) { shotAtAngle = flag; }
    }

    enum Storage implements Capability.IStorage<ICrossbowFireworkData>
    {
        INSTANCE;

        @Nullable
        @Override
        public NBTBase writeNBT(@Nonnull Capability<ICrossbowFireworkData> capability, @Nonnull ICrossbowFireworkData instance, @Nullable EnumFacing side) {
            final NBTTagCompound nbt = new NBTTagCompound();
            if(instance.getOwnerUUID() != null) nbt.setUniqueId("Owner", instance.getOwnerUUID());

            nbt.setBoolean("WasShotByCrossbow", instance.wasShotByCrossbow());
            nbt.setBoolean("ShotAtAngle", instance.isShotAtAngle());
            return nbt;
        }

        @Override
        public void readNBT(@Nonnull Capability<ICrossbowFireworkData> capability, @Nonnull ICrossbowFireworkData instance, @Nullable EnumFacing side, @Nullable NBTBase nbtIn) {
            if(nbtIn instanceof NBTTagCompound) {
                final NBTTagCompound nbt = (NBTTagCompound)nbtIn;
                if(nbt.hasUniqueId("Owner")) instance.setOwnerUUID(nbt.getUniqueId("Owner"));

                instance.setShotByCrossbow(nbt.getBoolean("WasShotByCrossbow"));
                instance.setShotAtAngle(instance.wasShotByCrossbow() || nbt.getBoolean("ShotAtAngle"));
            }
        }
    }
}
