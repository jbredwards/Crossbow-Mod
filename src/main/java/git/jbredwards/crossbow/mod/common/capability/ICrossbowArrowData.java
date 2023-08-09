/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.capability;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.util.CapabilityProvider;
import git.jbredwards.crossbow.mod.common.network.MessageSyncArrowData;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("ConstantConditions")
public interface ICrossbowArrowData
{
    @CapabilityInject(ICrossbowArrowData.class)
    @Nonnull Capability<ICrossbowArrowData> CAPABILITY = null;
    @Nonnull ResourceLocation CAPABILITY_ID = new ResourceLocation(Crossbow.MODID, "crossbow_arrow_data");

    @Nonnull
    SoundEvent getHitSound();
    void setHitSound(@Nonnull SoundEvent sound);

    @Nullable
    IntSet getPiercedEntities();
    void setPiercedEntities(@Nullable IntSet entities);

    int getPierceLevel();
    void setPierceLevel(int level);

    @Nullable
    static ICrossbowArrowData get(@Nullable ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attach(@Nonnull AttachCapabilitiesEvent<Entity> event) {
        if(event.getObject() instanceof EntityArrow) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY));
    }

    @SubscribeEvent
    static void sync(@Nonnull PlayerEvent.StartTracking event) {
        if(event.getEntityPlayer() instanceof EntityPlayerMP) {
            final ICrossbowArrowData cap = get(event.getTarget());
            if(cap != null) Crossbow.WRAPPER.sendTo(new MessageSyncArrowData(event.getTarget().getEntityId(), cap.getPierceLevel()), (EntityPlayerMP)event.getEntityPlayer());
        }
    }

    class Impl implements ICrossbowArrowData
    {
        @Nonnull
        protected SoundEvent hitSound = SoundEvents.ENTITY_ARROW_HIT;

        @Nullable
        protected IntSet piercedEntities;
        protected int pierceLevel;

        @Nonnull
        @Override
        public SoundEvent getHitSound() { return hitSound; }

        @Override
        public void setHitSound(@Nonnull SoundEvent sound) { hitSound = sound; }

        @Nullable
        @Override
        public IntSet getPiercedEntities() { return piercedEntities; }

        @Override
        public void setPiercedEntities(@Nullable IntSet entities) { piercedEntities = entities; }

        @Override
        public int getPierceLevel() { return pierceLevel; }

        @Override
        public void setPierceLevel(int level) { pierceLevel = level; }
    }

    enum Storage implements Capability.IStorage<ICrossbowArrowData>
    {
        INSTANCE;

        @Nullable
        @Override
        public NBTBase writeNBT(@Nonnull Capability<ICrossbowArrowData> capability, @Nonnull ICrossbowArrowData instance, @Nullable EnumFacing side) {
            final NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("HitSound", instance.getHitSound().delegate.name().toString());
            nbt.setInteger("PierceLevel", instance.getPierceLevel());

            final IntSet piercedEntities = instance.getPiercedEntities();
            if(piercedEntities != null) nbt.setIntArray("PiercedEntities", piercedEntities.toIntArray());
            return nbt;
        }

        @Override
        public void readNBT(@Nonnull Capability<ICrossbowArrowData> capability, @Nonnull ICrossbowArrowData instance, @Nonnull EnumFacing side, @Nullable NBTBase nbtIn) {
            if(nbtIn instanceof NBTTagCompound) {
                final NBTTagCompound nbt = (NBTTagCompound)nbtIn;
                if(nbt.hasKey("HitSound", Constants.NBT.TAG_STRING)) {
                    Optional.ofNullable(SoundEvent.REGISTRY.getObject(new ResourceLocation(nbt.getString("HitSound")))).ifPresent(instance::setHitSound);
                }

                if(nbt.hasKey("PierceLevel", Constants.NBT.TAG_ANY_NUMERIC)) {
                    instance.setPierceLevel(nbt.getInteger("PierceLevel"));
                }

                if(nbt.hasKey("PiercedEntities", Constants.NBT.TAG_INT_ARRAY)) {
                    instance.setPiercedEntities(new IntOpenHashSet(nbt.getIntArray("PiercedEntities")));
                }
            }
        }
    }
}
