/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common;

import git.jbredwards.crossbow.Tags;
import git.jbredwards.crossbow.api.capability.CapabilityCrossbowAmmo;
import git.jbredwards.crossbow.api.capability.ICrossbowAmmo;
import git.jbredwards.crossbow.mod.client.entity.RenderFirework;
import git.jbredwards.crossbow.mod.client.model.CrossbowArmPose;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowSoundData;
import git.jbredwards.crossbow.mod.common.capability.util.EmptyStorage;
import git.jbredwards.crossbow.mod.common.network.MessageSyncArrowData;
import git.jbredwards.crossbow.mod.common.network.MessageSyncFireworkData;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
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
@Mod(modid = Crossbow.MODID, name = Crossbow.NAME, version = Crossbow.VERSION, dependencies = "required-client:assetmover@[2.5,);" +
"after:futuremc@[0.2.6,);after:spartanweaponry@[1.5.3,);") // Optional mod compatibility versions.
public final class Crossbow
{
    @Nonnull public static final String MODID = Tags.MOD_ID, NAME = "Crossbow", VERSION = Tags.VERSION;
    @Nonnull public static final SimpleNetworkWrapper WRAPPER = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
    public static final boolean hasSpartanWeaponry = Loader.isModLoaded("spartanweaponry");

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void constructClient(@Nonnull FMLConstructionEvent event) {
        CrossbowArmPose.init();
    }

    @Mod.EventHandler
    static void preInit(@Nonnull FMLPreInitializationEvent event) {
        //register capabilities
        CapabilityManager.INSTANCE.register(ICrossbowAmmo.class, new EmptyStorage<>(), () -> (user, crossbow, projectile) -> null);
        MinecraftForge.EVENT_BUS.register(CapabilityCrossbowAmmo.class);
        CapabilityManager.INSTANCE.register(ICrossbowArrowData.class, ICrossbowArrowData.Storage.INSTANCE, ICrossbowArrowData.Impl::new);
        MinecraftForge.EVENT_BUS.register(ICrossbowArrowData.class);
        CapabilityManager.INSTANCE.register(ICrossbowFireworkData.class, ICrossbowFireworkData.Storage.INSTANCE, ICrossbowFireworkData.Impl::new);
        MinecraftForge.EVENT_BUS.register(ICrossbowFireworkData.class);
        CapabilityManager.INSTANCE.register(ICrossbowProjectiles.class, ICrossbowProjectiles.Storage.INSTANCE, () -> { throw new UnsupportedOperationException(); });
        MinecraftForge.EVENT_BUS.register(ICrossbowProjectiles.class);
        CapabilityManager.INSTANCE.register(ICrossbowSoundData.class, new EmptyStorage<>(), ICrossbowSoundData.Impl::new);
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
