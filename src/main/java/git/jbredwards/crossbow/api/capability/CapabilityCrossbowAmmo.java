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

package git.jbredwards.crossbow.api.capability;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.util.CapabilityProvider;
import git.jbredwards.crossbow.mod.common.compat.SpartanWeaponryHandler;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Capability handler for {@link ICrossbowAmmo}.
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
    @Nonnull public static final ResourceLocation CAPABILITY_ID = new ResourceLocation(Crossbow.MODID, "crossbow_sound_data"); // Bad ID... leaving it for now to maintain backward compatibility.

    @Nullable
    public static ICrossbowAmmo get(@Nonnull ItemStack stack) {
        return stack.hasCapability(CAPABILITY, null) ? stack.getCapability(CAPABILITY, null) : stack.getItem() instanceof ICrossbowAmmo ? (ICrossbowAmmo)stack.getItem() : null;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void attach(@Nonnull AttachCapabilitiesEvent<ItemStack> event) {
        final Item item = event.getObject().getItem();
        if(item instanceof ICrossbowAmmo) return;

        // spectral arrows
        if(item instanceof ItemSpectralArrow) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, new ICrossbowAmmo.Arrow() {
                @Nonnull
                @SideOnly(Side.CLIENT)
                @Override
                public ModelResourceLocation getAmmoModelLocation(@Nullable EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                    return new ModelResourceLocation(new ResourceLocation(Crossbow.MODID, "crossbow"), "spectral_arrow");
                }
            }));
        }

        // tipped arrows
        if(item instanceof ItemTippedArrow) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, new ICrossbowAmmo.Arrow() {
                @Nonnull
                @SideOnly(Side.CLIENT)
                @Override
                public ModelResourceLocation getAmmoModelLocation(@Nullable EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                    return new ModelResourceLocation(new ResourceLocation(Crossbow.MODID, "crossbow"), "tipped_arrow");
                }

                @Nonnull
                @SideOnly(Side.CLIENT)
                @Override
                public Int2IntMap getAmmoModelColor(@Nullable EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                    return Int2IntMaps.singleton(0, PotionUtils.getColor(projectile));
                }
            }));
        }

        // fireworks
        if(item instanceof ItemFirework) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, new ICrossbowAmmo() {
                @Nonnull
                @SideOnly(Side.CLIENT)
                @Override
                public ModelResourceLocation getAmmoModelLocation(@Nullable EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                    return new ModelResourceLocation(new ResourceLocation(Crossbow.MODID, "crossbow"), "firework");
                }

                @Nullable
                @Override
                public IProjectile createCrossbowProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) {
                    return (IProjectile)new EntityFireworkRocket(user.world, user.posX, user.posY + user.getEyeHeight() - 0.15, user.posZ, projectile);
                }

                @Override
                public boolean isInventoryCrossbowAmmo(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { return false; }

                @Override
                public float velocityMultiplier(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { return 0.5f; }

                @Override
                public void damageCrossbow(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile) { crossbow.damageItem(3, user); }
            }));
        }

        // arrows
        if(item instanceof ItemArrow) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, new ICrossbowAmmo.Arrow() {}));
        }

        // bolts (Spartan Weaponry)
        if(Crossbow.hasSpartanWeaponry && SpartanWeaponryHandler.isBolt(item)) {
            if(!event.getCapabilities().containsKey(CAPABILITY_ID)) event.addCapability(CAPABILITY_ID, new CapabilityProvider<>(CAPABILITY, SpartanWeaponryHandler.createAmmoHandler()));
        }
    }
}
