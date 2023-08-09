/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.item;

import git.jbredwards.crossbow.api.entity.ICrossbowUser;
import git.jbredwards.crossbow.api.util.Quat4dUtils;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowSoundData;
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
import net.minecraftforge.event.ForgeEventFactory;
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

    public ItemCrossbow() {
        addPropertyOverride(new ResourceLocation(Crossbow.MODID, "pull"), (stack, world, entity) -> {
            if(entity == null) return 0;
            final ICrossbowProjectiles cap = ICrossbowProjectiles.get(stack);
            return cap == null || !cap.isEmpty() ? 0 : (float)(stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / getPullTime(stack);
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

            final boolean hasAmmo = !cap.findAmmo(playerIn, held).isEmpty();
            final ActionResult<ItemStack> eventResult = ForgeEventFactory.onArrowNock(held, worldIn, playerIn, handIn, hasAmmo);
            if(eventResult != null) return eventResult;
            else if(hasAmmo) {
                final ICrossbowSoundData soundData = ICrossbowSoundData.get(playerIn);
                if(soundData != null) {
                    soundData.setPlayedLoadingStartSound(false);
                    soundData.setPlayedLoadingMiddleSound(false);
                }

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
                entityLiving.playSound(getLoadingEndSound(entityLiving, stack), 1, 1 / (itemRand.nextFloat() * 0.5f + 1) + 0.2f);
        }
    }

    protected static boolean loadProjectiles(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ICrossbowProjectiles cap) {
        final int ammoToLoad = ((ItemCrossbow)crossbow.getItem()).getAmmoToLoad(user, crossbow);
        final boolean isCreative = user instanceof EntityPlayer && ((EntityPlayer)user).isCreative();

        ItemStack ammo = cap.findAmmo(user, crossbow);
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
            final double spread = ((ItemCrossbow)crossbow.getItem()).getArrowSpread(user, crossbow) / 2;

            for(int i = 0; i < cap.size(); i++) {
                final ItemStack projectile = cap.get(i);
                if(!projectile.isEmpty()) {
                    final double offset = (i & 1) == 0 ? (cap.size() & 1) == 0 ? i + 1 : i : 1 - i - ((cap.size() & 1) == 0 ? 1 : 2);
                    shoot(world, user, crossbow, projectile, soundPitches[i > 0 ? ((i & 2) >> 1) + 1 : 0], isCreative, speed, divergence, offset * spread);
                }
            }
        }

        cap.clear();
    }

    protected static void shoot(@Nonnull World world, @Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull ItemStack projectile, float soundPitch, boolean isCreative, float speed, float divergence, double multishotOffset) {
        final boolean isFirework = projectile.getItem() instanceof ItemFirework;
        final IProjectile projectileEntity;

        if(isFirework) {
            projectileEntity = (IProjectile)new EntityFireworkRocket(world, user.posX, user.posY + user.getEyeHeight() - 0.15, user.posZ, projectile);
            final ICrossbowFireworkData fireworkCap = ICrossbowFireworkData.get((Entity)projectileEntity);
            assert fireworkCap != null; // should always pass
            fireworkCap.setOwner(user);
            fireworkCap.setShotByCrossbow(true);
        }

        else {
            // forge event hook
            if(user instanceof EntityPlayer && ForgeEventFactory.onArrowLoose(crossbow, world, (EntityPlayer)user, 1, true) < 0) return;

            final ItemArrow arrowItem = (ItemArrow)(projectile.getItem() instanceof ItemArrow ? projectile.getItem() : Items.ARROW);
            final EntityArrow arrow = arrowItem.createArrow(world, projectile, user);
            final ICrossbowArrowData arrowData = ICrossbowArrowData.get(arrow);

            assert arrowData != null; // should always pass
            arrowData.setPierceLevel(EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.PIERCING, crossbow));
            arrowData.setHitSound(((ItemCrossbow)crossbow.getItem()).getArrowHitSound(user, crossbow, arrow, projectile));

            if(user instanceof EntityPlayer) arrow.setIsCritical(true);
            if(isCreative || multishotOffset != 0) arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;

            projectileEntity = arrow;
        }

        if(user instanceof ICrossbowUser) ((ICrossbowUser)user).shootAtTarget(crossbow, projectileEntity, multishotOffset);
        else {
            final Vec3d vec = Quat4dUtils.getMultishotVector(user, multishotOffset);
            projectileEntity.shoot(vec.x, vec.y, vec.z, speed, divergence);
            world.playSound(null, user.posX, user.posY, user.posZ, ((ItemCrossbow)crossbow.getItem()).getShootSound(user, crossbow, projectileEntity, multishotOffset), SoundCategory.PLAYERS, 1, soundPitch);
        }

        world.spawnEntity((Entity)((ItemCrossbow)crossbow.getItem()).customizeProjectile(user, crossbow, projectileEntity, projectile, soundPitch, isCreative, speed, divergence, multishotOffset));
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
            final ICrossbowSoundData soundData = ICrossbowSoundData.get(player);
            if(soundData != null) {
                final float charge = (float)(stack.getMaxItemUseDuration() - count) / getPullTime(stack);
                if(charge < 0.2) {
                    soundData.setPlayedLoadingStartSound(false);
                    soundData.setPlayedLoadingMiddleSound(false);
                    return;
                }

                final int lvl = EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.QUICK_CHARGE, stack);
                if(charge >= 0.2 && !soundData.getPlayedLoadingStartSound()) {
                    soundData.setPlayedLoadingStartSound(true);
                    player.world.playSound(null, player.posX, player.posY, player.posZ, getLoadingStartSound(player, stack, lvl), player.getSoundCategory(), 0.5f, 1);
                }

                if(charge >= 0.5 && lvl == 0 && !soundData.getPlayedLoadingMiddleSound()) {
                    soundData.setPlayedLoadingMiddleSound(true);
                    player.world.playSound(null, player.posX, player.posY, player.posZ, getLoadingMiddleSound(player, stack, lvl), player.getSoundCategory(), 0.5f, 1);
                }
            }
        }
    }

    @Override
    public final int getMaxItemUseDuration(@Nonnull ItemStack stack) { return getPullTime(stack) + 3; }
    public int getPullTime(@Nonnull ItemStack stack) {
        final int lvl = EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.QUICK_CHARGE, stack);
        return lvl == 0 ? 25 : 25 - 5 * lvl;
    }

    @Nonnull
    @Override
    public EnumAction getItemUseAction(@Nonnull ItemStack stack) { return ACTION; }

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

    @Override
    public int getItemEnchantability() { return 1; }

    // ==============================
    // FUNCTIONS FOR CUSTOM CROSSBOWS
    // ==============================

    /**
     * @return the angle separating each arrow (in degrees).
     */
    public double getArrowSpread(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) { return 10; }

    /**
     * Exists in case someone wants to make an addon mod that adds crossbows that fire a special amount of projectiles.
     */
    public int getAmmoToLoad(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        return EnchantmentHelper.getEnchantmentLevel(CrossbowEnchantments.MULTISHOT, crossbow) > 0 ? 3 : 1;
    }

    @Nonnull
    public SoundEvent getArrowHitSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull EntityArrow arrow, @Nonnull ItemStack arrowStack) {
        return CrossbowSounds.ITEM_CROSSBOW_HIT;
    }

    @Nonnull
    public SoundEvent getLoadingEndSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow) {
        return CrossbowSounds.ITEM_CROSSBOW_LOADING_END;
    }

    @Nonnull
    public SoundEvent getLoadingMiddleSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, int quickChargeEnchLvl) {
        return CrossbowSounds.ITEM_CROSSBOW_LOADING_MIDDLE;
    }

    @Nonnull
    public SoundEvent getLoadingStartSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, int quickChargeEnchLvl) {
        switch(quickChargeEnchLvl) {
            case 1: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_1;
            case 2: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_2;
            case 3: return CrossbowSounds.ITEM_CROSSBOW_QUICK_CHARGE_3;
            default: return CrossbowSounds.ITEM_CROSSBOW_LOADING_START;
        }
    }

    @Nonnull
    public SoundEvent getShootSound(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull IProjectile projectile, double multishotOffset) {
        return CrossbowSounds.ITEM_CROSSBOW_SHOOT;
    }

    /**
     * Exists in case someone wants to make an addon mod that adds crossbows that add special properties to projectiles.
     */
    @Nonnull
    public IProjectile customizeProjectile(@Nonnull EntityLivingBase user, @Nonnull ItemStack crossbow, @Nonnull IProjectile projectile, @Nonnull ItemStack projectileStack, float soundPitch, boolean isCreative, float speed, float divergence, double multishotOffset) {
        return projectile;
    }
}
