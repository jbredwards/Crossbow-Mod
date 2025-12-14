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

package git.jbredwards.crossbow.api;

import git.jbredwards.crossbow.api.capability.CapabilityCrossbowAmmo;
import git.jbredwards.crossbow.api.capability.ICrossbowAmmo;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import git.jbredwards.crossbow.mod.common.init.CrossbowEnchantments;
import git.jbredwards.crossbow.mod.common.init.CrossbowSounds;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

/**
 * Any crossbow item should either implement this interface or extend {@link git.jbredwards.crossbow.mod.common.item.ItemCrossbow ItemCrossbow}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ICrossbow
{
    /**
     * Item use action for crossbows.
     * @since 1.0.0
     */
    @Nonnull
    EnumAction CROSSBOW_ACTION = Objects.requireNonNull(EnumHelper.addAction(Crossbow.MODID + "_crossbow"));

    /**
     * @return The horizontal angle separating each projectile (in degrees) when firing multiple projectiles.
     * The first projectile fired will not have this applied, unless the number of projectiles returned by {@link ICrossbow#getAmmoToLoad} is an even number.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    default double getArrowSpread(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        return 10;
    }

    /**
     * @return The number of projectiles to load into the crossbow. This does not change the amount of ammo consumed, which is always 1.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    default int getAmmoToLoad(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        return EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.MULTISHOT, crossbow) > 0 ? 3 : 1;
    }

    /**
     * Note: This method itself is called when the arrow instance is created by this crossbow, not when the arrow hits a block.
     * @return The sound that the arrow will play when it hits a block.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Nonnull
    default SoundEvent getArrowHitSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull EntityArrow arrow, @Nonnull ItemStack arrowStack) {
        return CrossbowSounds.ITEM_CROSSBOW_HIT;
    }

    /**
     * @return The sound played when the user finishes loading this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Nonnull
    default SoundEvent getLoadingEndSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        return CrossbowSounds.ITEM_CROSSBOW_LOADING_END;
    }

    /**
     * @return The sound played when the user is halfway done with loading this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Nonnull
    default SoundEvent getLoadingMiddleSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, int quickChargeEnchLvl) {
        return CrossbowSounds.ITEM_CROSSBOW_LOADING_MIDDLE;
    }

    /**
     * @return The sound played when the user starts loading this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Nonnull
    default SoundEvent getLoadingStartSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, int quickChargeEnchLvl) {
        switch(quickChargeEnchLvl) {
            case 1: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_1;
            case 2: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_2;
            case 3: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_3;
            default: return CrossbowSounds.ITEM_CROSSBOW_LOADING_START;
        }
    }

    /**
     * @return The sound played when the user shoots this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Nonnull
    default SoundEvent getShootSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull IProjectile projectile, double multishotOffset) {
        return CrossbowSounds.ITEM_CROSSBOW_SHOOT;
    }

    /**
     * Utility method that clears any ammo from the crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.2.0
     */
    static void clearProjectiles(@Nonnull ItemStack crossbow) {
        final ICrossbowProjectiles projectiles = ICrossbowProjectiles.get(crossbow);
        if(projectiles != null) projectiles.clear();
    }

    /**
     * Utility method that finds an ammo ItemStack from the user and loads it into the crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.2.0
     */
    static boolean loadProjectiles(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        final ICrossbowProjectiles projectiles = ICrossbowProjectiles.get(crossbow);
        return projectiles != null && projectiles.isEmpty() && ItemCrossbow.loadProjectiles(user, crossbow, projectiles);
    }

    /**
     * Utility method that fires all projectiles loaded in the crossbow, then clears any ammo.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.2.0
     */
    static boolean shootAll(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, float speed, float divergence) {
        final ICrossbowProjectiles projectiles = ICrossbowProjectiles.get(crossbow);
        if(projectiles != null && !projectiles.isEmpty()) {
            ItemCrossbow.shootAll(user.world, user, crossbow, projectiles, speed, divergence);
            return true;
        }

        return false;
    }

    /**
     * Utility method that fires the provided projectile item, then damages this crossbow. This method may be overriden,
     * but it should never be called by another mod. Use {@link ICrossbow#shootAll}.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    default void shoot(@Nonnull World world, @Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile, float soundPitch, boolean isCreative, float speed, float divergence, double multishotOffset) {
        final ICrossbowAmmo ammoHandler = CapabilityCrossbowAmmo.get(projectile);
        if(ammoHandler != null) {
            final IProjectile projectileEntity = createProjectileFromStack(world, user, crossbow, projectile, ammoHandler, isCreative, multishotOffset);
            if(projectileEntity == null) return;

            if(user instanceof ICrossbowUser) ((ICrossbowUser)user).shootAtTarget(crossbow, projectileEntity, speed * ammoHandler.velocityMultiplier(user, crossbow, projectile), divergence, multishotOffset);
            else {
                final Vec3d vec = Quat4dUtils.getMultishotVector(user, multishotOffset);
                projectileEntity.shoot(vec.x, vec.y, vec.z, speed * ammoHandler.velocityMultiplier(user, crossbow, projectile), divergence);
                world.playSound(null, user.posX, user.posY, user.posZ, getShootSound(user, crossbow, projectileEntity, multishotOffset), SoundCategory.PLAYERS, 1, soundPitch);
            }

            MinecraftForge.EVENT_BUS.post(new CreateCrossbowProjectileEvent.Post(user, crossbow, projectile, projectileEntity));
            if(!isCreative && (!(user instanceof ICrossbowUser) || ((ICrossbowUser)user).damagesCrossbow())) ammoHandler.damageCrossbow(user, crossbow, projectile);
            world.spawnEntity((Entity)projectileEntity);
        }
    }

    /**
     * @return The created projectile upon firing this crossbow, before it's summoned into the world.
     * The crossbow firing sound and the projectile's velocity are both handled by {@link ICrossbow#shoot}.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.1.0
     */
    @Nullable
    default IProjectile createProjectileFromStack(@Nonnull World world, @Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile, @Nonnull ICrossbowAmmo ammoHandler, boolean isCreative, double multishotOffset) {
        final IProjectile projectileAsEntity = ammoHandler.createCrossbowProjectile(user, crossbow, projectile);
        if(projectileAsEntity == null) return null;

        // Let modders change the projectile entity if desired.
        final CreateCrossbowProjectileEvent.Pre event = new CreateCrossbowProjectileEvent.Pre(user, crossbow, projectile, projectileAsEntity);
        final IProjectile projectileEntity = MinecraftForge.EVENT_BUS.post(event) ? event.newProjectileEntity : event.projectileAsEntity;

        // Apply data to arrow if applicable.
        final ICrossbowArrowData arrowData = ICrossbowArrowData.get((Entity)projectileEntity);
        if(arrowData != null) {
            if(user instanceof EntityPlayer && ForgeEventFactory.onArrowLoose(crossbow, world, (EntityPlayer)user, 1, true) < 0) return null;

            arrowData.setHitSound(getArrowHitSound(user, crossbow, (EntityArrow)projectileEntity, projectile));
            arrowData.setPierceLevel(EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.PIERCING, crossbow));
            arrowData.setShotByCrossbow(true);

            if(user instanceof EntityPlayer) ((EntityArrow)projectileEntity).setIsCritical(true);
            if(multishotOffset != 0 || !ICrossbowProjectiles.applyPickupStatus(crossbow, (EntityArrow)projectileEntity) && isCreative) ((EntityArrow)projectileEntity).pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;

            if(Crossbow.Cfg.allowBowEnchantments) {
                if(EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, crossbow) > 0) ((EntityArrow)projectileEntity).setFire(100);

                final int power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, crossbow);
                if(power > 0) ((EntityArrow)projectileEntity).setDamage(((EntityArrow)projectileEntity).getDamage() + power * 0.5 + 0.5);

                final int punch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, crossbow);
                if(punch > 0) ((EntityArrow)projectileEntity).setKnockbackStrength(punch);
            }
        }

        // Apply data to other projectiles if applicable.
        else if(projectileEntity instanceof ICrossbowProjectile) {
            ((ICrossbowProjectile)projectileEntity).setShooter(user);
            ((ICrossbowProjectile)projectileEntity).setShotByCrossbow(true);
        }

        return projectileEntity;
    }

    /**
     * @return The ammo ItemStack from the user's inventory to be loaded into this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    @Nonnull
    default ItemStack findAmmo(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        if(user instanceof ICrossbowUser) return ((ICrossbowUser)user).findAmmo(crossbow);
        else if(isHeldProjectile(user, crossbow, user.getHeldItemOffhand())) return user.getHeldItemOffhand();
        else if(isHeldProjectile(user, crossbow, user.getHeldItemMainhand())) return user.getHeldItemMainhand();

        if(user instanceof EntityPlayer) {
            final IInventory inventory = ((EntityPlayer)user).inventory;
            for(int i = 0; i < inventory.getSizeInventory(); ++i) {
                final ItemStack stack = inventory.getStackInSlot(i);
                if(isInventoryProjectile(user, crossbow, stack)) return stack;
            }

            if(((EntityPlayer)user).isCreative()) return new ItemStack(Items.ARROW);
        }

        return ItemStack.EMPTY;
    }

    /**
     * Component of {@link ICrossbow#findAmmo}.
     * @return True if the provided ItemStack, while being held, is recognised as "ammo" by this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    default boolean isHeldProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack stack) {
        final ICrossbowAmmo ammoHandler = CapabilityCrossbowAmmo.get(stack);
        return ammoHandler != null && ammoHandler.isHeldCrossbowAmmo(user, crossbow, stack);
    }

    /**
     * Component of {@link ICrossbow#findAmmo}.
     * @return True if the provided ItemStack, while in the inventory, is recognised as "ammo" by this crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.0.0
     */
    default boolean isInventoryProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack stack) {
        final ICrossbowAmmo ammoHandler = CapabilityCrossbowAmmo.get(stack);
        return ammoHandler != null && ammoHandler.isInventoryCrossbowAmmo(user, crossbow, stack);
    }

    /**
     * @return The sound pitches for all projectiles in the crossbow when firing them. This should not be called for each
     * individual projectile, so even vs odd projectiles have matching pitches.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.2.0
     */
    @Nonnull
    static float[] getFiringSoundPitches(@Nonnull Random random) {
        final float pitchLow = 1 / (random.nextFloat() * 0.5f + 1.8f) + 0.43f;
        final float pitchHigh = 1 / (random.nextFloat() * 0.5f + 1.8f) + 0.63f;

        final boolean flip = random.nextBoolean();
        return new float[] {1, flip ? pitchHigh : pitchLow, flip ? pitchLow : pitchHigh};
    }

    /**
     * @return The index for {@link ICrossbow#getFiringSoundPitches} based on the projectile's loading order in its crossbow.
     *
     * @throws NullPointerException If any parameters are null.
     * @since 1.2.0
     */
    static int getFiringSoundPitchIndex(int projectileIndex) {
        return projectileIndex > 0 ? (projectileIndex & 1) == 0 ? 2 : 1 : 0;
    }
}
