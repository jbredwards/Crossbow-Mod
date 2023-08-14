/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api.capability;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.util.CapabilityProvider;
import git.jbredwards.crossbow.mod.common.compat.SpartanWeaponryHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @since 1.1.0
 * @author jbred
 *
 */
@SuppressWarnings("ConstantConditions")
public final class CapabilityCrossbowAmmo
{
    @CapabilityInject(ICrossbowAmmo.class)
    @Nonnull public static final Capability<ICrossbowAmmo> CAPABILITY = null;
    @Nonnull public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Crossbow.MODID, "crossbow_sound_data");

    @Nullable
    public static ICrossbowAmmo get(@Nonnull ItemStack stack) {
        return stack.hasCapability(CAPABILITY, null) ? stack.getCapability(CAPABILITY, null) : stack.getItem() instanceof ICrossbowAmmo ? (ICrossbowAmmo)stack.getItem() : null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void attach(@Nonnull AttachCapabilitiesEvent<ItemStack> event) {
        final Item item = event.getObject().getItem();

        // arrows
        if(item instanceof ItemArrow) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY,
                    (user, crossbow, projectile) -> ((ItemArrow)projectile.getItem()).createArrow(user.world, projectile, user)));
        }

        // fireworks
        if(item instanceof ItemFirework) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, new ICrossbowAmmo() {
                @Nullable
                @Override
                public IProjectile createCrossbowProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                    return (IProjectile)new EntityFireworkRocket(user.world, user.posX, user.posY + user.getEyeHeight() - 0.15, user.posZ, projectile);
                }

                @Override
                public boolean isInventoryCrossbowAmmo(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { return false; }

                @Override
                public float velocityMultiplier(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { return 0.5f; }
            }));
        }

        // bolts (Spartan Weaponry)
        if(Crossbow.hasSpartanWeaponry && SpartanWeaponryHandler.isBolt(item)) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, SpartanWeaponryHandler.createAmmoHandler()));
        }
    }
}
