/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.init;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.enchantment.EnchantmentCrossbow;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

        final EnumEnchantmentType[] oldTypes = CreativeTabs.COMBAT.getRelevantEnchantmentTypes();
        final EnumEnchantmentType[] types = new EnumEnchantmentType[oldTypes.length + 1];
        types[oldTypes.length] = EnchantmentCrossbow.CROSSBOW;
        System.arraycopy(oldTypes, 0, types, 0, oldTypes.length);

        CreativeTabs.COMBAT.setRelevantEnchantmentTypes(types);
    }

    @SubscribeEvent
    static void registerItem(@Nonnull RegistryEvent.Register<Item> event) {
        event.getRegistry().register(new ItemCrossbow().setMaxStackSize(1).setMaxDamage(326).setCreativeTab(CreativeTabs.COMBAT).setRegistryName(Crossbow.MODID, "crossbow").setTranslationKey(Crossbow.MODID + ".crossbow"));
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
}
