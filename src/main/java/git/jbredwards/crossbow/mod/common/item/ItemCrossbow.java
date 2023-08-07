/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.item;

import git.jbredwards.crossbow.api.entity.ICrossbowUser;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import git.jbredwards.crossbow.mod.common.init.CrossbowEnchantments;
import git.jbredwards.crossbow.mod.common.init.CrossbowSounds;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
public class ItemCrossbow extends Item
{
    @Nonnull
    public static final EnumAction ACTION = Objects.requireNonNull(EnumHelper.addAction(Crossbow.MODID + "_crossbow"));
    boolean charged, loaded;

    public ItemCrossbow() {
        addPropertyOverride(new ResourceLocation(Crossbow.MODID, "pull"), (stack, world, entity) -> {
            if(entity == null) return 0;
            final ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            return cap == null || cap.isEmpty() ? 0 : (float)(stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / getPullTime(stack);
        });

        addPropertyOverride(new ResourceLocation(Crossbow.MODID, "pulling"), (stack, world, entity) ->
            entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1 : 0);

        addPropertyOverride(new ResourceLocation(Crossbow.MODID, "charged"), (stack, world, entity) -> {
            final ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            return cap != null && !cap.isEmpty() ? 1 : 0;
        });

        addPropertyOverride(new ResourceLocation(Crossbow.MODID, "firework"), (stack, world, entity) -> {
            final ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            return cap != null && cap.stream().anyMatch(projectile -> projectile.getItem() instanceof ItemFirework) ? 1 : 0;
        });
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World worldIn, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        final ItemStack held = playerIn.getHeldItem(handIn);
        final ICrossbowProjectiles cap = ICrossbowProjectiles.get(held);
        if(cap != null) {
            if(!cap.isEmpty()) {
                shootAll(worldIn, playerIn, held, cap, cap.stream().anyMatch(projectile -> projectile.getItem() instanceof ItemFirework) ? 1.6f : 3.15f, 1);
                return ActionResult.newResult(EnumActionResult.SUCCESS, held);
            }

            else if(!cap.findAmmo(playerIn, held).isEmpty()) {
                charged = false;
                loaded = false;
                playerIn.setActiveHand(handIn);
                return ActionResult.newResult(EnumActionResult.SUCCESS, held);
            }
        }

        return ActionResult.newResult(EnumActionResult.FAIL, held);
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World worldIn, @Nonnull EntityLivingBase entityLiving, int timeLeft) {
        if((float)(stack.getMaxItemUseDuration() - timeLeft) / getPullTime(stack) >= 1) {
            final ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            if(cap != null && cap.isEmpty() && loadProjectiles(entityLiving, stack, cap))
                entityLiving.playSound(CrossbowSounds.ITEM_CROSSBOW_LOADING_END, 1, 1 / (itemRand.nextFloat() * 0.5f + 1) + 0.2f);
        }
    }

    protected static boolean loadProjectiles(@Nonnull EntityLivingBase user, @Nonnull ItemStack stack, @Nonnull ICrossbowProjectiles cap) {
        final int multishot = EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.MULTISHOT, stack);
        final int ammoToLoad = multishot == 0 ? 1 : 3;

        final boolean isCreative = user instanceof EntityPlayer && ((EntityPlayer)user).isCreative();
        ItemStack ammo = cap.findAmmo(user, stack);
        ItemStack ammoCopy = ammo.copy();

        for(int ammoLoaded = 0; ammoLoaded < ammoToLoad; ammoLoaded++) {
            if(ammoLoaded > 0) ammo = ammoCopy.copy();
            if(ammo.isEmpty() && isCreative) {
                ammo = new ItemStack(Items.ARROW);
                ammoCopy = new ItemStack(Items.ARROW);
            }

            if(!loadProjectile(user, cap, ammo, isCreative)) return false;
        }

        return true;
    }

    protected static boolean loadProjectile(@Nonnull EntityLivingBase user, @Nonnull ICrossbowProjectiles cap, @Nonnull ItemStack projectile, boolean isCreative) {
        if(projectile.isEmpty()) return false;
        if(!isCreative) {
            cap.add(projectile.splitStack(1));
            if(projectile.isEmpty() && user instanceof EntityPlayer) ((EntityPlayer)user).inventory.deleteStack(projectile);
        }

        else cap.add(projectile.copy());
        return true;
    }

    public static void shootAll(@Nonnull World world, @Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ICrossbowProjectiles cap, float speed, float divergence) {
        if(!world.isRemote) {
            final float[] soundPitches = getSoundPitches(user.getRNG());
            final boolean isCreative = user instanceof EntityPlayer && ((EntityPlayer)user).isCreative();

            for(int i = 0; i < cap.size(); i++) {
                final ItemStack projectile = cap.get(i);
                if(!projectile.isEmpty()) {
                    switch(i) {
                        case 0:
                            shoot(world, user, crossbow, projectile, soundPitches[i], isCreative, speed, divergence, 0);
                            break;
                        case 1:
                            shoot(world, user, crossbow, projectile, soundPitches[i], isCreative, speed, divergence, -10);
                            break;
                        case 2:
                            shoot(world, user, crossbow, projectile, soundPitches[i], isCreative, speed, divergence, 10);
                    }
                }
            }
        }

        cap.clear();
    }

    protected static void shoot(@Nonnull World world, @Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile, float soundPitch, boolean isCreative, float speed, float divergence, float multishotOffset) {
        final boolean isFirework = projectile.getItem() instanceof ItemFirework;
        final IProjectile projectileEntity;

        if(isFirework) {
            projectileEntity = (IProjectile)new EntityFireworkRocket(world, user.posX, user.posY + user.getEyeHeight() - 0.15, user.posZ, projectile);
            final ICrossbowFireworkData fireworkCap = ICrossbowFireworkData.get((Entity)projectileEntity);

            assert fireworkCap != null; //should always pass
            fireworkCap.setShotByCrossbow(true);
        }

        else {
            final ItemArrow arrowItem = (ItemArrow)(projectile.getItem() instanceof ItemArrow ? projectile.getItem() : Items.ARROW);
            final EntityArrow arrow = arrowItem.createArrow(world, projectile, user);
            final ICrossbowArrowData arrowData = ICrossbowArrowData.get(arrow);

            assert arrowData != null; //should always pass
            arrowData.setHitSound(CrossbowSounds.ITEM_CROSSBOW_HIT);
            arrowData.setPierceLevel(EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.PIERCING, crossbow));

            if(user instanceof EntityPlayer) arrow.setIsCritical(true);
            if(isCreative || multishotOffset != 0) arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;

            projectileEntity = arrow;
        }

        if(user instanceof ICrossbowUser) ((ICrossbowUser)user).shootAtTarget(crossbow, projectileEntity, multishotOffset);
        else {
            final Vec3d vec = user.getLookVec().rotateYaw((float)Math.toRadians(multishotOffset));
            projectileEntity.shoot(vec.x, vec.y, vec.z, speed, divergence);
        }

        world.spawnEntity((Entity)projectileEntity);
        world.playSound(null, user.posX, user.posY, user.posZ, CrossbowSounds.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1, soundPitch);

        if(!isCreative) crossbow.damageItem(isFirework ? 3 : 1, user);
    }

    protected static float[] getSoundPitches(@Nonnull Random random) {
        final boolean flag = random.nextBoolean();
        return new float[] {1, getSoundPitch(flag), getSoundPitch(!flag)};
    }

    protected static float getSoundPitch(boolean flag) {
        final float constant = flag ? 0.63f : 0.43f;
        return 1 / (itemRand.nextFloat() * 0.5f + 1.8f) + constant;
    }

    @Override
    public void onUsingTick(@Nonnull ItemStack stack, @Nonnull EntityLivingBase player, int count) {
        if(!player.world.isRemote) {
            final float charge = (float)(stack.getMaxItemUseDuration() - count) / getPullTime(stack);
            if(charge < 0.2) {
                charged = false;
                loaded = false;
                return;
            }

            final int lvl = EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.QUICK_CHARGE, stack);
            if(charge >= 0.2 && !charged) {
                charged = true;
                player.playSound(getChargingSound(lvl), 0.5f, 1);
            }

            if(charge >= 0.5 && lvl == 0 && !loaded) {
                loaded = true;
                player.playSound(CrossbowSounds.ITEM_CROSSBOW_LOADING_MIDDLE, 0.5f, 1);
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack) { return getPullTime(stack) + 3; }
    public static int getPullTime(@Nonnull ItemStack stack) {
        final int lvl = EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.QUICK_CHARGE, stack);
        return lvl == 0 ? 25 : 25 - 5 * lvl;
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(@Nonnull ItemStack stack) { return ACTION; }

    @Nonnull
    protected SoundEvent getChargingSound(int quickChargeEnchLvl) {
        switch(quickChargeEnchLvl) {
            case 1: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_1;
            case 2: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_2;
            case 3: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_3;
            default: return CrossbowSounds.ITEM_CROSSBOW_LOADING_START;
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        final ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
        if(cap != null && !cap.isEmpty()) {
            final ItemStack projectile = cap.get(0);
            tooltip.add(I18n.format("tooltip.crossbow.crossbow.projectile", projectile.getTextComponent().getFormattedText()));
            if(flagIn.isAdvanced()) {
                final List<String> subTooltip = new LinkedList<>();
                projectile.getItem().addInformation(projectile, worldIn, subTooltip, flagIn);
                if(!subTooltip.isEmpty()) tooltip.addAll(subTooltip.stream().map(str -> "  " + TextFormatting.GRAY + str).collect(Collectors.toCollection(LinkedList::new)));
            }
        }
    }
}
