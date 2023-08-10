/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.api;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import git.jbredwards.crossbow.mod.common.init.CrossbowEnchantments;
import git.jbredwards.crossbow.mod.common.init.CrossbowSounds;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Any crossbow item should either implement this interface or extend {@link git.jbredwards.crossbow.mod.common.item.ItemCrossbow ItemCrossbow}.
 *
 * @since 1.0.0
 * @author jbred
 *
 */
public interface ICrossbow
{
    @Nonnull
    EnumAction CROSSBOW_ACTION = Objects.requireNonNull(EnumHelper.addAction(Crossbow.MODID + "_crossbow"));

    /**
     * @return the angle separating each arrow (in degrees).
     */
    default double getArrowSpread(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) { return 10; }

    /**
     * @return the number of projectiles to load into the crossbow.
     */
    default int getAmmoToLoad(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        return EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.MULTISHOT, crossbow) > 0 ? 3 : 1;
    }

    @Nonnull
    default SoundEvent getArrowHitSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull EntityArrow arrow, @Nonnull ItemStack arrowStack) {
        return CrossbowSounds.ITEM_CROSSBOW_HIT;
    }

    @Nonnull
    default SoundEvent getLoadingEndSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        return CrossbowSounds.ITEM_CROSSBOW_LOADING_END;
    }

    @Nonnull
    default SoundEvent getLoadingMiddleSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, int quickChargeEnchLvl) {
        return CrossbowSounds.ITEM_CROSSBOW_LOADING_MIDDLE;
    }

    @Nonnull
    default SoundEvent getLoadingStartSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, int quickChargeEnchLvl) {
        switch(quickChargeEnchLvl) {
            case 1: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_1;
            case 2: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_2;
            case 3: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_3;
            default: return CrossbowSounds.ITEM_CROSSBOW_LOADING_START;
        }
    }

    @Nonnull
    default SoundEvent getShootSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull IProjectile projectile, double multishotOffset) {
        return CrossbowSounds.ITEM_CROSSBOW_SHOOT;
    }

    default void shoot(@Nonnull World world, @Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile, float soundPitch, boolean isCreative, float speed, float divergence, double multishotOffset) {
        final IProjectile projectileEntity = createProjectileFromStack(world, user, crossbow, projectile, isCreative, multishotOffset);
        if(projectileEntity == null) return;

        if(user instanceof ICrossbowUser) ((ICrossbowUser)user).shootAtTarget(crossbow, projectileEntity, multishotOffset);
        else {
            final Vec3d vec = Quat4dUtils.getMultishotVector(user, multishotOffset);
            projectileEntity.shoot(vec.x, vec.y, vec.z, speed, divergence);
            world.playSound(null, user.posX, user.posY, user.posZ, getShootSound(user, crossbow, projectileEntity, multishotOffset), SoundCategory.PLAYERS, 1, soundPitch);
        }

        world.spawnEntity((Entity)projectileEntity);
        if(!isCreative) crossbow.damageItem(projectile.getItem() instanceof ItemFirework ? 3 : 1, user);
    }

    @Nullable
    default IProjectile createProjectileFromStack(@Nonnull World world, @Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile, boolean isCreative, double multishotOffset) {
        if(projectile.getItem() instanceof ItemFirework) {
            final EntityFireworkRocket firework = new EntityFireworkRocket(world, user.posX, user.posY + user.getEyeHeight() - 0.15, user.posZ, projectile);
            final ICrossbowFireworkData fireworkCap = ICrossbowFireworkData.get(firework);
            assert fireworkCap != null; // should always pass
            fireworkCap.setOwner(user);
            fireworkCap.setShotByCrossbow(true);

            return (IProjectile)firework;
        }

        // forge event hook
        if(user instanceof EntityPlayer && ForgeEventFactory.onArrowLoose(crossbow, world, (EntityPlayer)user, 1, true) < 0) return null;

        final ItemArrow arrowItem = (ItemArrow)(projectile.getItem() instanceof ItemArrow ? projectile.getItem() : Items.ARROW);
        final EntityArrow arrow = arrowItem.createArrow(world, projectile, user);
        final ICrossbowArrowData arrowData = ICrossbowArrowData.get(arrow);

        assert arrowData != null; // should always pass
        arrowData.setHitSound(getArrowHitSound(user, crossbow, arrow, projectile));
        arrowData.setPierceLevel(EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.PIERCING, crossbow));

        if(user instanceof EntityPlayer) arrow.setIsCritical(true);
        if(isCreative || multishotOffset != 0) arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;

        return arrow;
    }

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
}
