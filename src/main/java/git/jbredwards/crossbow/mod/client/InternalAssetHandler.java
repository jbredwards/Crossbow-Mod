/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.client;

import com.cleanroommc.assetmover.AssetMoverAPI;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import git.jbredwards.crossbow.Tags;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import javax.annotation.Nonnull;

/**
 * Fake mod container. Downloads assets from AssetMover as early as possible.
 * @author jbred
 *
 */
public final class InternalAssetHandler extends DummyModContainer
{
    public InternalAssetHandler() {
        super(new ModMetadata());
        getMetadata().modId = "crossbow/resources";
        getMetadata().version = "0";
        getMetadata().name = getModId();
        getMetadata().parent = Tags.MOD_ID;
    }

    @Override
    public boolean registerBus(@Nonnull final EventBus bus, @Nonnull final LoadController controller) {
        // Run AssetMover. Any dependency exception is handled by the actual mod container.
        if(FMLCommonHandler.instance().getSide().isClient()) try { AssetMoverAPI.fromMinecraft("1.18.2", ImmutableMap.<String, String>builder()
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
                .put("assets/minecraft/textures/item/crossbow_pulling_0.png", "assets/crossbow/textures/items/crossbow_pulling_0.png")
                .put("assets/minecraft/textures/item/crossbow_pulling_1.png", "assets/crossbow/textures/items/crossbow_pulling_1.png")
                .put("assets/minecraft/textures/item/crossbow_pulling_2.png", "assets/crossbow/textures/items/crossbow_pulling_2.png")
                .put("assets/minecraft/textures/item/crossbow_standby.png", "assets/crossbow/textures/items/crossbow_standby.png")
                .build());
        } catch(@Nonnull final Throwable ignored) {}
        return true;
    }
}
