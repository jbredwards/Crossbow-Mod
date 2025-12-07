/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.init;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.enchantment.EnchantmentCrossbow;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Crossbow.MODID)
final class RegistryHandler
{
    @SubscribeEvent
    static void registerEnchantments(@Nonnull RegistryEvent.Register<Enchantment> event) {
        event.getRegistry().register(new EnchantmentCrossbow(Enchantment.Rarity.RARE).setMinEnchantability(lvl -> 20).setMaxEnchantability(lvl -> 50).setApplicableCondition(ench -> ench != CrossbowEnchantments.PIERCING).setRegistryName(Crossbow.MODID, "multishot").setName(Crossbow.MODID + ".multishot"));
        event.getRegistry().register(new EnchantmentCrossbow(Enchantment.Rarity.COMMON).setMinEnchantability(lvl -> 1 + (lvl - 1) * 10).setMaxEnchantability(lvl -> 50).setMaxLevel(4).setApplicableCondition(ench -> ench != CrossbowEnchantments.MULTISHOT).setRegistryName(Crossbow.MODID, "piercing").setName(Crossbow.MODID + ".piercing"));
        event.getRegistry().register(new EnchantmentCrossbow(Enchantment.Rarity.UNCOMMON).setMinEnchantability(lvl -> 12 + (lvl - 1) * 20).setMaxEnchantability(lvl -> 50).setMaxLevel(3).setRegistryName(Crossbow.MODID, "quick_charge").setName(Crossbow.MODID + ".quick_charge"));

        CreativeTabs.COMBAT.setRelevantEnchantmentTypes(ArrayUtils.add(CreativeTabs.COMBAT.getRelevantEnchantmentTypes(), EnchantmentCrossbow.CROSSBOW));
        CrossbowItems.TAB.setRelevantEnchantmentTypes(EnchantmentCrossbow.CROSSBOW);
    }

    @SubscribeEvent
    static void registerItem(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemCrossbow().setMaxStackSize(1).setMaxDamage(464).setCreativeTab(CreativeTabs.COMBAT).setRegistryName(Crossbow.MODID, "crossbow").setTranslationKey(Crossbow.MODID + ".crossbow"));

        @Nonnull final IBehaviorDispenseItem oldBehavior = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(Items.FIREWORKS);
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIREWORKS, (source, stack) -> {
            if(!Crossbow.Cfg.replaceFireworkDispenseBehavior) return oldBehavior.dispense(source, stack);
            @Nonnull final EnumFacing direction = source.getBlockState().getValue(BlockDispenser.FACING);

            @Nonnull final EntityFireworkRocket rocket = new EntityFireworkRocket(source.getWorld(), source.getX(), source.getY(), source.getZ(), stack);
            rocket.setPosition(rocket.posX + direction.getXOffset() * (0.5 - rocket.width / 2), rocket.posY + direction.getYOffset() * (0.5 - rocket.height / 2), rocket.posZ + direction.getZOffset() * (0.5 - rocket.width / 2));
            ((IProjectile)rocket).shoot(direction.getXOffset(), direction.getYOffset(), direction.getZOffset(), 0.5f, 1);

            source.getWorld().playEvent(Constants.WorldEvents.FIREWORK_SHOOT_SOUND, source.getBlockPos(), 0);
            source.getWorld().playEvent(Constants.WorldEvents.DISPENSER_SMOKE, source.getBlockPos(), direction.getXOffset() + 1 + (direction.getZOffset() + 1) * 3);
            source.getWorld().spawnEntity(rocket);

            stack.shrink(1);
            return stack;
        });
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    static void registerModel(@Nonnull ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(CrossbowItems.CROSSBOW, 0, new ModelResourceLocation(new ResourceLocation(Crossbow.MODID, "crossbow"), "inventory"));
    }

    @SubscribeEvent
    static void registerSounds(@Nonnull RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.hit")).setRegistryName(Crossbow.MODID, "items.crossbow.hit"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.loading.end")).setRegistryName(Crossbow.MODID, "items.crossbow.loading.end"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.loading.middle")).setRegistryName(Crossbow.MODID, "items.crossbow.loading.middle"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.loading.start")).setRegistryName(Crossbow.MODID, "items.crossbow.loading.start"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.quick_charge_1")).setRegistryName(Crossbow.MODID, "items.crossbow.quick_charge_1"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.quick_charge_2")).setRegistryName(Crossbow.MODID, "items.crossbow.quick_charge_2"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.quick_charge_3")).setRegistryName(Crossbow.MODID, "items.crossbow.quick_charge_3"));
        event.getRegistry().register(new SoundEvent(new ResourceLocation(Crossbow.MODID, "items.crossbow.shoot")).setRegistryName(Crossbow.MODID, "items.crossbow.shoot"));
    }

    @SubscribeEvent
    static void registerTrades(@Nonnull RegistryEvent.Register<VillagerRegistry.VillagerProfession> event) {
        Objects.requireNonNull(event.getRegistry().getValue(new ResourceLocation("farmer"))).getCareer(3).addTrade(3, new EntityVillager.ListItemForEmeralds(CrossbowItems.CROSSBOW, new EntityVillager.PriceInfo(3, 5)));
    }
}
