/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.capability;

import git.jbredwards.crossbow.api.ICrossbow;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.util.CapabilityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.List;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("ConstantConditions")
public interface ICrossbowProjectiles extends List<ItemStack>
{
    @CapabilityInject(ICrossbowProjectiles.class)
    @Nonnull Capability<ICrossbowProjectiles> CAPABILITY = null;
    @Nonnull ResourceLocation CAPABILITY_ID = new ResourceLocation(Crossbow.MODID, "crossbow_projectiles");

    @Nonnull String AMMO_NBT = "ChargedProjectiles";
    @Nonnull String PICKUP_NBT = Crossbow.MODID + ":pickup_status";

    @Nullable
    EntityArrow.PickupStatus getPickupStatus();
    void setPickupStatus(@Nullable final EntityArrow.PickupStatus pickupStatus);
    static boolean applyPickupStatus(@Nullable final ICapabilityProvider provider, @Nonnull final EntityArrow arrow) {
        @Nullable final ICrossbowProjectiles cap = get(provider);
        if(cap == null) return false;

        @Nullable final EntityArrow.PickupStatus status = cap.getPickupStatus();
        if(status == null) return false;

        arrow.pickupStatus = status;
        return true;
    }

    @Nonnull
    default ItemStack findAmmo(@Nonnull final EntityLivingBase user, @Nonnull final ItemStack crossbow) {
        return ((ICrossbow)crossbow.getItem()).findAmmo(user, crossbow);
    }

    @Nullable
    static ICrossbowProjectiles get(@Nullable final ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attach(@Nonnull final AttachCapabilitiesEvent<ItemStack> event) {
        if(event.getObject().getItem() instanceof ICrossbow) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, new Impl(event.getObject())));
    }

    class Impl extends AbstractList<ItemStack> implements ICrossbowProjectiles
    {
        @Nonnull
        protected final ItemStack crossbow;
        public Impl(@Nonnull final ItemStack crossbowIn) { crossbow = crossbowIn; }

        @Nullable
        @Override
        public EntityArrow.PickupStatus getPickupStatus() {
            return crossbow.hasTagCompound() && crossbow.getTagCompound().hasKey(PICKUP_NBT, Constants.NBT.TAG_ANY_NUMERIC)
                    ? EntityArrow.PickupStatus.getByOrdinal(crossbow.getTagCompound().getInteger(PICKUP_NBT)) : null;
        }

        @Override
        public void setPickupStatus(@Nullable final EntityArrow.PickupStatus pickupStatus) {
            if(pickupStatus != null) crossbow.setTagInfo(PICKUP_NBT, new NBTTagInt(pickupStatus.ordinal()));
            else if(crossbow.hasTagCompound()) {
                crossbow.getTagCompound().removeTag(PICKUP_NBT);
                if(crossbow.getTagCompound().isEmpty()) crossbow.setTagCompound(null);
            }
        }

        @Nonnull
        @Override
        public ItemStack get(final int index) {
            if(!crossbow.hasTagCompound() || !crossbow.getTagCompound().hasKey(AMMO_NBT, Constants.NBT.TAG_LIST)) throw new IndexOutOfBoundsException("Index: "+index+", Size: 0");
            @Nonnull final NBTTagList projectiles = crossbow.getTagCompound().getTagList(AMMO_NBT, Constants.NBT.TAG_COMPOUND);

            if(index >= projectiles.tagCount() || index < 0) throw new IndexOutOfBoundsException("Index: "+index+", Size: "+projectiles.tagCount());
            return new ItemStack(projectiles.getCompoundTagAt(index));
        }

        @Nullable
        @Override
        public ItemStack set(final int index, @Nonnull final ItemStack element) {
            @Nullable final ItemStack prev = get(index);
            @Nonnull final NBTTagList projectiles = crossbow.getTagCompound().getTagList(AMMO_NBT, Constants.NBT.TAG_COMPOUND);

            projectiles.set(index, element.serializeNBT());
            return prev;
        }

        @Nullable
        @Override
        public ItemStack remove(final int index) {
            @Nullable final ItemStack prev = get(index);
            @Nonnull final NBTTagList projectiles = crossbow.getTagCompound().getTagList(AMMO_NBT, Constants.NBT.TAG_COMPOUND);

            projectiles.removeTag(index);
            if(projectiles.isEmpty()) {
                crossbow.getTagCompound().removeTag(AMMO_NBT);
                if(crossbow.getTagCompound().isEmpty()) crossbow.setTagCompound(null);
            }

            return prev;
        }

        @Override
        public void add(final int index, @Nonnull final ItemStack element) {
            @Nonnull final NBTTagList projectiles;
            if(!crossbow.hasTagCompound() || !crossbow.getTagCompound().hasKey(AMMO_NBT, Constants.NBT.TAG_LIST)) crossbow.setTagInfo(AMMO_NBT, projectiles = new NBTTagList());
            else projectiles = crossbow.getTagCompound().getTagList(AMMO_NBT, Constants.NBT.TAG_COMPOUND);

            if(index > projectiles.tagCount() || index < 0) throw new IndexOutOfBoundsException("Index: "+index+", Size: "+projectiles.tagCount());
            else if(index == projectiles.tagCount()) projectiles.appendTag(element.serializeNBT());
            else {
                projectiles.appendTag(projectiles.get(projectiles.tagCount() - 1));
                for(int i = projectiles.tagCount() - 2; i > index; i--) projectiles.set(i, projectiles.get(i - 1));
                projectiles.set(index, element.serializeNBT());
            }
        }

        @Override
        public int size() {
            if(!crossbow.hasTagCompound() || !crossbow.getTagCompound().hasKey(AMMO_NBT, Constants.NBT.TAG_LIST)) return 0;
            else return crossbow.getTagCompound().getTagList(AMMO_NBT, Constants.NBT.TAG_COMPOUND).tagCount();
        }

        @Override
        public void clear() {
            super.clear();
            setPickupStatus(null);
        }
    }

    enum Storage implements Capability.IStorage<ICrossbowProjectiles>
    {
        INSTANCE;

        @Nullable
        @Override
        public NBTBase writeNBT(@Nonnull Capability<ICrossbowProjectiles> capability, @Nonnull ICrossbowProjectiles instance, @Nullable EnumFacing side) {
            return new NBTTagByte((byte)0); // Handle data through ItemStack nbt.
        }

        @Override
        public void readNBT(@Nonnull Capability<ICrossbowProjectiles> capability, @Nonnull ICrossbowProjectiles instance, @Nullable EnumFacing side, @Nullable NBTBase nbt) {
            if(nbt instanceof NBTTagList && !nbt.isEmpty()) {
                final NBTTagList nbtList = (NBTTagList)nbt;
                for(int i = 0; i < nbtList.tagCount(); i++) {
                    final ItemStack projectile = new ItemStack(nbtList.getCompoundTagAt(i));
                    instance.add(projectile.isEmpty() ? ItemStack.EMPTY : projectile);
                }
            }
        }
    }
}
