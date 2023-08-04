/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.init;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.enchantment.EnchantmentCrossbow;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

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
    }

    @SubscribeEvent
    static void registerItems(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemCrossbow().setMaxStackSize(1).setMaxDamage(326).setCreativeTab(CreativeTabs.COMBAT).setRegistryName(Crossbow.MODID, "crossbow").setTranslationKey(Crossbow.MODID + ".crossbow"));
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
}