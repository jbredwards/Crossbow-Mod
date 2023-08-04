/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common;

import com.cleanroommc.assetmover.AssetMoverAPI;
import com.google.gson.Gson;
import git.jbredwards.crossbow.mod.client.entity.RenderFirework;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowArrowData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Objects;

/**
 *
 * @author jbred
 *
 */
@Mod(modid = Crossbow.MODID, name = Crossbow.NAME, dependencies = "required-client:assetmover@[2.5,);")
public final class Crossbow
{
    @Nonnull
    public static final String MODID = "crossbow", NAME = "Crossbow";

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void constructClient(@Nonnull FMLConstructionEvent event) {
        //download vanilla assets
        final String[][] assets = new Gson().fromJson(new InputStreamReader(Objects.requireNonNull(
                Loader.class.getResourceAsStream(String.format("/assets/%s/assetmover.jsonc", MODID)))),
                String[][].class);

        for(String[] asset : assets) AssetMoverAPI.fromMinecraft(asset[0], Collections.singletonMap(asset[1], asset[2]));
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
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    static void preInitClient(@Nonnull FMLPreInitializationEvent event) {
        //firework entity renderer override
        RenderingRegistry.registerEntityRenderingHandler(EntityFireworkRocket.class, RenderFirework::new);
    }
}
