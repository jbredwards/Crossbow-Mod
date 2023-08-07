/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.capability;

import git.jbredwards.crossbow.api.entity.ICrossbowUser;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.util.CapabilityProvider;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
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

    @Nonnull
    default ItemStack findAmmo(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        if(user instanceof ICrossbowUser) return ((ICrossbowUser)user).findAmmo(crossbow);
        else if(isHeldProjectile(user, user.getHeldItem(EnumHand.OFF_HAND))) return user.getHeldItem(EnumHand.OFF_HAND);
        else if(isHeldProjectile(user, user.getHeldItem(EnumHand.MAIN_HAND))) return user.getHeldItem(EnumHand.MAIN_HAND);

        if(user instanceof EntityPlayer) {
            final IInventory inventory = ((EntityPlayer)user).inventory;
            for(int i = 0; i < inventory.getSizeInventory(); ++i) {
                final ItemStack stack = inventory.getStackInSlot(i);
                if(isInventoryProjectile(user, stack)) return stack;
            }

            if(((EntityPlayer)user).isCreative()) return new ItemStack(Items.ARROW);
        }

        return ItemStack.EMPTY;
    }

    default boolean isHeldProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack stack) {
        return isInventoryProjectile(user, stack) || stack.getItem() instanceof ItemFirework;
    }

    default boolean isInventoryProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemArrow;
    }

    @Nullable
    static ICrossbowProjectiles get(@Nullable ICapabilityProvider provider) {
        return provider != null && provider.hasCapability(CAPABILITY, null) ? provider.getCapability(CAPABILITY, null) : null;
    }

    @SubscribeEvent
    static void attach(@Nonnull AttachCapabilitiesEvent<ItemStack> event) {
        if(event.getObject().getItem() instanceof ItemCrossbow) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY));
    }

    class Impl extends ArrayList<ItemStack> implements ICrossbowProjectiles {}
    enum Storage implements Capability.IStorage<ICrossbowProjectiles>
    {
        INSTANCE;

        @Nullable
        @Override
        public NBTBase writeNBT(@Nonnull Capability<ICrossbowProjectiles> capability, @Nonnull ICrossbowProjectiles instance, @Nullable EnumFacing side) {
            final NBTTagList nbtList = new NBTTagList();
            for(final ItemStack projectile : instance) nbtList.appendTag(projectile.serializeNBT());
            return nbtList;
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

            else instance.clear();
        }
    }
}
