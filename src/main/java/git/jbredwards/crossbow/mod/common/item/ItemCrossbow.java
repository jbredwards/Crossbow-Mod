/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.item;

import git.jbredwards.crossbow.api.ICrossbow;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowSoundData;
import git.jbredwards.crossbow.mod.common.init.CrossbowEnchantments;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFirework;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 *
 * @author jbred
 *
 */
public class ItemCrossbow extends Item implements ICrossbow
{
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
                shootAll(worldIn, playerIn, held, cap, 3.15f, 1);
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
        final int ammoToLoad = ((ICrossbow)crossbow.getItem()).getAmmoToLoad(user, crossbow);
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
            final double spread = ((ICrossbow)crossbow.getItem()).getArrowSpread(user, crossbow) / 2;

            for(int i = 0; i < cap.size(); i++) {
                final ItemStack projectile = cap.get(i);
                if(!projectile.isEmpty()) {
                    final double offset = (i & 1) == 0 ? (cap.size() & 1) == 0 ? i + 1 : i : 1 - i - ((cap.size() & 1) == 0 ? 1 : 2);
                    ((ICrossbow)crossbow.getItem()).shoot(world, user, crossbow, projectile, soundPitches[i > 0 ? ((i & 2) >> 1) + 1 : 0], isCreative, speed, divergence, offset * spread);
                }
            }
        }

        cap.clear();
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

    @Nonnull
    @Override
    public EnumAction getItemUseAction(@Nonnull ItemStack stack) { return CROSSBOW_ACTION; }
}
