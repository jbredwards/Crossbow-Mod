/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.compat;

import com.google.common.collect.ImmutableSet;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.init.CrossbowEnchantments;
import git.jbredwards.crossbow.mod.common.init.CrossbowItems;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import thedarkcolour.futuremc.config.FConfig;
import thedarkcolour.futuremc.item.CrossbowItem;

import javax.annotation.Nonnull;

/**
 * Also supports remapping Crossbow backport mods that were made after this one.
 * @author jbred
 *
 */
@Mod.EventBusSubscriber(modid = Crossbow.MODID)
public final class RemapperHandler
{
    @Nonnull
    static final ImmutableSet<String> REMAPPED_IDS = ImmutableSet.of("futuremc", "cross", "crossbows");

    @SubscribeEvent
    static void remapEnchantments(@Nonnull final RegistryEvent.MissingMappings<Enchantment> event) {
        event.getAllMappings().forEach(mapping -> {
            if(REMAPPED_IDS.contains(mapping.key.getNamespace())) switch(mapping.key.getPath()) {
                case "multishot": mapping.remap(CrossbowEnchantments.MULTISHOT); break;
                case "piercing": mapping.remap(CrossbowEnchantments.PIERCING); break;
                case "quick_charge": mapping.remap(CrossbowEnchantments.QUICK_CHARGE);
            }
        });
    }

    @SubscribeEvent
    static void remapItems(@Nonnull final RegistryEvent.MissingMappings<Item> event) {
        event.getAllMappings().forEach(mapping -> {
            if(REMAPPED_IDS.contains(mapping.key.getNamespace()) && mapping.key.getPath().equals("crossbow")) {
                mapping.remap(CrossbowItems.CROSSBOW);
            }
        });
    }

    // ----

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void handleFutureMC(@Nonnull final RegistryEvent.Register<Item> event) throws IllegalAccessException {
        if(Loader.isModLoaded("futuremc")) {
            FieldUtils.writeField(FConfig.INSTANCE.getVillageAndPillage(), "crossbow", Boolean.FALSE, true);
            // FieldUtils.writeStaticField(FItems.class, "CROSSBOW", CrossbowItems.CROSSBOW, true);
            MinecraftForge.EVENT_BUS.unregister(CrossbowItem.Companion);
        }
    }
}
