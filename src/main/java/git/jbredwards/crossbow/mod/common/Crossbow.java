/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common;

import com.cleanroommc.assetmover.AssetMoverAPI;
import com.google.common.collect.ImmutableMap;
import git.jbredwards.crossbow.mod.client.entity.RenderFirework;
import git.jbredwards.crossbow.mod.client.model.CrossbowArmPose;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowSoundData;
import git.jbredwards.crossbow.mod.common.network.MessageSyncArrowData;
import git.jbredwards.crossbow.mod.common.network.MessageSyncFireworkData;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = Crossbow.MODID, name = Crossbow.NAME, dependencies = "required-client:assetmover@[2.5,);")
public final class Crossbow
{
    @Nonnull public static final String MODID = "crossbow", NAME = "Crossbow";
    @Nonnull public static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void constructClient(@Nonnull FMLConstructionEvent event) {
        CrossbowArmPose.init();
        //download vanilla assets
        AssetMoverAPI.fromMinecraft("1.18.2", ImmutableMap.<String, String>builder()
                .put("assets/minecraft/sounds/item/crossbow/loading_end.ogg", "assets/crossbow/sounds/loading_end.ogg")
                .put("assets/minecraft/sounds/item/crossbow/loading_middle1.ogg", "assets/crossbow/sounds/loading_middle1.ogg")
                .put("assets/minecraft/sounds/item/crossbow/loading_middle2.ogg", "assets/crossbow/sounds/loading_middle2.ogg")
                .put("assets/minecraft/sounds/item/crossbow/loading_middle3.ogg", "assets/crossbow/sounds/loading_middle3.ogg")
                .put("assets/minecraft/sounds/item/crossbow/loading_middle4.ogg", "assets/crossbow/sounds/loading_middle4.ogg")
                .put("assets/minecraft/sounds/item/crossbow/loading_start.ogg", "assets/crossbow/sounds/loading_start.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick1_1.ogg", "assets/crossbow/sounds/quick_charge/quick1_1.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick1_2.ogg", "assets/crossbow/sounds/quick_charge/quick1_2.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick1_3.ogg", "assets/crossbow/sounds/quick_charge/quick1_3.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick2_1.ogg", "assets/crossbow/sounds/quick_charge/quick2_1.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick2_2.ogg", "assets/crossbow/sounds/quick_charge/quick2_2.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick2_3.ogg", "assets/crossbow/sounds/quick_charge/quick2_3.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick3_1.ogg", "assets/crossbow/sounds/quick_charge/quick3_1.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick3_2.ogg", "assets/crossbow/sounds/quick_charge/quick3_2.ogg")
                .put("assets/minecraft/sounds/item/crossbow/quick_charge/quick3_3.ogg", "assets/crossbow/sounds/quick_charge/quick3_3.ogg")
                .put("assets/minecraft/sounds/item/crossbow/shoot1.ogg", "assets/crossbow/sounds/shoot1.ogg")
                .put("assets/minecraft/sounds/item/crossbow/shoot2.ogg", "assets/crossbow/sounds/shoot2.ogg")
                .put("assets/minecraft/sounds/item/crossbow/shoot3.ogg", "assets/crossbow/sounds/shoot3.ogg")
                .put("assets/minecraft/textures/item/crossbow_arrow.png", "assets/crossbow/textures/items/crossbow_arrow.png")
                .put("assets/minecraft/textures/item/crossbow_firework.png", "assets/crossbow/textures/items/crossbow_firework.png")
                .put("assets/minecraft/textures/item/crossbow_pulling_0.png", "assets/crossbow/textures/items/crossbow_pulling_0.png")
                .put("assets/minecraft/textures/item/crossbow_pulling_1.png", "assets/crossbow/textures/items/crossbow_pulling_1.png")
                .put("assets/minecraft/textures/item/crossbow_pulling_2.png", "assets/crossbow/textures/items/crossbow_pulling_2.png")
                .put("assets/minecraft/textures/item/crossbow_standby.png", "assets/crossbow/textures/items/crossbow_standby.png")
                .build());
    }

    @Mod.EventHandler
    static void preInit(@Nonnull FMLPreInitializationEvent event) {
        //register capabilities
        CapabilityManager.INSTANCE.register(ICrossbowArrowData.class, ICrossbowArrowData.Storage.INSTANCE, ICrossbowArrowData.Impl::new);
        MinecraftForge.EVENT_BUS.register(ICrossbowArrowData.class);
        CapabilityManager.INSTANCE.register(ICrossbowFireworkData.class, ICrossbowFireworkData.Storage.INSTANCE, ICrossbowFireworkData.Impl::new);
        MinecraftForge.EVENT_BUS.register(ICrossbowFireworkData.class);
        CapabilityManager.INSTANCE.register(ICrossbowProjectiles.class, ICrossbowProjectiles.Storage.INSTANCE, ICrossbowProjectiles.Impl::new);
        MinecraftForge.EVENT_BUS.register(ICrossbowProjectiles.class);
        CapabilityManager.INSTANCE.register(ICrossbowSoundData.class, ICrossbowSoundData.Storage.INSTANCE, ICrossbowSoundData.Impl::new);
        MinecraftForge.EVENT_BUS.register(ICrossbowSoundData.class);

        //register packets
        WRAPPER.registerMessage(MessageSyncArrowData.Handler.INSTANCE, MessageSyncArrowData.class, 0, Side.CLIENT);
        WRAPPER.registerMessage(MessageSyncFireworkData.Handler.INSTANCE, MessageSyncFireworkData.class, 1, Side.CLIENT);
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void preInitClient(@Nonnull FMLPreInitializationEvent event) {
        //firework entity renderer override
        RenderingRegistry.registerEntityRenderingHandler(EntityFireworkRocket.class, RenderFirework::new);
    }
}
