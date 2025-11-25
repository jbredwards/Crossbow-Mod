/*
 * Copyright (c) 2025. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.client;

import com.cleanroommc.assetmover.AssetMoverAPI;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import git.jbredwards.crossbow.Tags;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.*;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Fake mod container, downloads assets for AssetMover as early as possible.
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
        return FMLCommonHandler.instance().getSide().isClient();
    }

    @Nonnull
    @Override
    public Class<?> getCustomResourcePackClass() { return InternalResourcePack.class; }
    public static final class InternalResourcePack extends FMLFolderResourcePack
    {
        public InternalResourcePack(@Nonnull final ModContainer containerIn) {
            super(containerIn);

            // Check that assets can actually be downloaded.
            if(SystemUtils.JAVA_VERSION != null && !SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_9) && System.getProperty("javax.net.ssl.trustStore") == null) {
                @Nonnull final String error = "Update Java to at least 1.8.0_311 or use CensoredASM with \"outdatedCaCertsFix\" enabled.";
                if(Integer.parseInt(SystemUtils.JAVA_VERSION.split("_")[1]) < 311) throw new RuntimeException(error);
            }

            // Run AssetMover. Any dependency exception is handled by the actual mod container.
            if(Loader.isModLoaded("assetmover")) AssetMoverAPI.fromMinecraft("1.18.2", ImmutableMap.<String, String>builder()
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
        }

        @Nonnull
        @Override
        public Set<String> getResourceDomains() { return Collections.emptySet(); }

        @Nullable
        @Override
        public <T extends IMetadataSection> T getPackMetadata(@Nonnull final MetadataSerializer serializer, @Nonnull final String name) { return null; }
    }
}
