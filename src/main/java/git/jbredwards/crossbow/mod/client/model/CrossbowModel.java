/*
 * Copyright (C) <2025 to Present> <jbredwards>
 *
 * All rights are reserved, except where explicitly granted by the original
 * copyright holder or where explicitly granted by the Mod Permissions License as
 * published by Jbredwards, either version 1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See the Mod Permissions License for more details
 * <https://www.github.com/jbredwards/mod-permissions-license>.
 */

package git.jbredwards.crossbow.mod.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import git.jbredwards.crossbow.api.capability.CapabilityCrossbowAmmo;
import git.jbredwards.crossbow.api.capability.ICrossbowAmmo;
import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.capability.ICrossbowProjectiles;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelPart;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public final class CrossbowModel implements IModel
{
    @Nonnull private static final ResourceLocation MISSING = new ResourceLocation(ModelLoader.MODEL_MISSING.getNamespace(), ModelLoader.MODEL_MISSING.getPath());
    @Nonnull public static final CrossbowModel INSTANCE = new CrossbowModel(MISSING, MISSING, new ResourceLocation[0]);

    @Nonnull private final ResourceLocation normal, loaded;
    @Nonnull private final ResourceLocation[] pulling;

    public CrossbowModel(@Nonnull final ResourceLocation normalIn, @Nonnull final ResourceLocation loadedIn, @Nonnull final ResourceLocation[] pullingIn) {
        normal = normalIn;
        loaded = loadedIn;
        pulling = pullingIn;
    }

    @Nonnull
    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.<ResourceLocation>builder().addAll(ICrossbowAmmo.AMMO_MODELS).add(normal).add(loaded).add(pulling).build();
    }

    @Nonnull
    @Override
    public IBakedModel bake(@Nonnull final IModelState state, @Nonnull final VertexFormat format, @Nonnull final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        @Nonnull final UnaryOperator<IModelState> stateGetter = modelState -> new ModelStateComposition(state, modelState);
        return new BakedNormal(state, format, bakedTextureGetter, bake(normal, stateGetter, format, bakedTextureGetter), bake(loaded, stateGetter, format, bakedTextureGetter),
                Arrays.stream(pulling).map(location -> bake(location, stateGetter, format, bakedTextureGetter)).toArray(IBakedModel[]::new));
    }

    @Nonnull
    private static IBakedModel bake(@Nonnull final ResourceLocation location, @Nonnull final UnaryOperator<IModelState> state, @Nonnull final VertexFormat format, @Nonnull final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        @Nonnull final IModel model = ModelLoaderRegistry.getModelOrLogError(location, "Couldn't load CrossbowModel dependency: " + location);
        return model.bake(state.apply(model.getDefaultState()), format, bakedTextureGetter);
    }

    @SideOnly(Side.CLIENT)
    public static final class BakedNormal extends BakedModelWrapper<IBakedModel>
    {
        @Nonnull private final VertexFormat format;
        @Nonnull private final UnaryOperator<IModelState> state;
        @Nonnull private final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter;

        @Nonnull private final IBakedModel loaded;
        @Nonnull private final IBakedModel[] pulling;

        public BakedNormal(@Nonnull final IModelState stateIn, @Nonnull VertexFormat formatIn, @Nonnull final Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetterIn,
                           @Nonnull final IBakedModel normalIn, @Nonnull final IBakedModel loadedIn, @Nonnull final IBakedModel[] pullingIn) {
            super(normalIn);
            loaded = loadedIn;
            pulling = pullingIn;
            format = formatIn;
            bakedTextureGetter = bakedTextureGetterIn;
            // Prevent z-fighting on the front of crossbows, and fix the enchantment glint being applied twice to top/bottom faces.
            state = modelState -> new ModelStateComposition(stateIn, modelState) {
                @Nonnull
                @Override
                public Optional<TRSRTransformation> apply(@Nonnull final Optional<? extends IModelPart> part) {
                    return super.apply(part).map(TRSRTransformation.blockCenterToCorner(new TRSRTransformation(null, null, new Vector3f(1.001f, 1.001f, 1.001f), null))::compose);
                }
            };
        }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() {
            return new ItemOverrideList(ImmutableList.of()) {
                @Nonnull
                @Override
                public IBakedModel handleItemState(@Nonnull final IBakedModel originalModel, @Nonnull final ItemStack stack, @Nullable final World world, @Nullable final EntityLivingBase entity) {
                    // Loaded model.
                    @Nullable final ICrossbowProjectiles projectiles = ICrossbowProjectiles.get(stack);
                    if(projectiles != null && !projectiles.isEmpty()) {
                        @Nonnull final ItemStack ammo = projectiles.get(0);
                        @Nullable final ICrossbowAmmo cap = CapabilityCrossbowAmmo.get(ammo);
                        return new BakedLoaded(loaded, Loader.INSTANCE.cache.computeIfAbsent(
                                cap != null ? cap.getAmmoModelLocation(entity, stack, ammo) : new ModelResourceLocation(new ResourceLocation(Crossbow.MODID, "crossbow"), "arrow"),
                                ammoLocation -> bake(ammoLocation, state, format, bakedTextureGetter)), cap != null ? cap.getAmmoModelColor(entity, stack, ammo) : Int2IntMaps.EMPTY_MAP);
                    }

                    // Pulling model.
                    else if(pulling.length != 0 && entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack) {
                        final float pullTime = (float)(stack.getMaxItemUseDuration() - entity.getItemInUseCount()) / stack.getMaxItemUseDuration();
                        return pulling[Math.min(pulling.length - 1, (int)(pullTime * (pulling.length - 1)))];
                    }

                    // Normal model.
                    return originalModel;
                }
            };
        }

        @Nonnull
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull final ItemCameraTransforms.TransformType cameraTransformType) {
            return Pair.of(this, super.handlePerspective(cameraTransformType).getRight());
        }
    }

    @SideOnly(Side.CLIENT)
    public static final class BakedLoaded extends BakedModelWrapper<IBakedModel>
    {
        @Nonnull private final IBakedModel ammo;
        @Nonnull private final Int2IntMap colors;

        public BakedLoaded(@Nonnull final IBakedModel loadedIn, @Nonnull final IBakedModel ammoIn, @Nonnull final Int2IntMap colorsIn) {
            super(loadedIn);
            ammo = ammoIn;
            colors = colorsIn;
        }

        @Nonnull
        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable final IBlockState state, @Nullable final EnumFacing side, final long rand) {
            @Nonnull final ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            builder.addAll(super.getQuads(state, side, 0));
            builder.add(ammo.getQuads(state, side, 0).stream().map(quad -> {
                @Nonnull final VertexFormat format = quad.getFormat();
                if(!format.hasColor() || !quad.hasTintIndex() || !colors.containsKey(quad.getTintIndex())) return quad;

                final int size = format.getIntegerSize();
                final int offset = format.getColorOffset() / 4; // assumes that color is aligned

                final int argb = colors.get(quad.getTintIndex());
                int bgr = (argb & 0xFF) << 16 | (argb & 0xFF00) | (argb >>> 16 & 0xFF);
                bgr |= (argb & 0xFF000000) != 0 ? argb & 0xFF000000 : 0xFF000000;

                @Nonnull final BakedQuad newQuad = new BakedQuad(quad.getVertexData().clone(), quad.getTintIndex(), quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(), quad.getFormat());
                for(int i = 0; i < 4; i++) newQuad.getVertexData()[offset + size * i] = bgr;
                return newQuad;
            }).toArray(BakedQuad[]::new));
            return builder.build();
        }

        @Nonnull
        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(@Nonnull final ItemCameraTransforms.TransformType cameraTransformType) {
            return Pair.of(this, super.handlePerspective(cameraTransformType).getRight());
        }
    }

    @SideOnly(Side.CLIENT)
    public enum Loader implements ICustomModelLoader
    {
        INSTANCE;

        @Nonnull
        private final Map<ResourceLocation, IBakedModel> cache = new HashMap<>();

        @Nonnull
        @Override
        public IModel loadModel(@Nonnull final ResourceLocation modelLocation) {
            return CrossbowModel.INSTANCE;
        }

        @Override
        public boolean accepts(@Nonnull final ResourceLocation modelLocation) {
            return modelLocation.getNamespace().equals(Crossbow.MODID) && modelLocation.getPath().endsWith("builtin");
        }

        @Override
        public void onResourceManagerReload(@Nonnull final IResourceManager resourceManager) {
            cache.clear();
        }
    }

    @Nonnull
    @Override
    public IModel process(@Nonnull final ImmutableMap<String, String> customData) {
        if(!customData.containsKey("normal") || !customData.containsKey("loaded") || !customData.containsKey("pulling")) return INSTANCE;

        @Nonnull final ResourceLocation normal = ModelLoader.getInventoryVariant(JsonUtils.getString(new JsonParser().parse(customData.get("normal")), "normal"));
        @Nonnull final ResourceLocation loaded = ModelLoader.getInventoryVariant(JsonUtils.getString(new JsonParser().parse(customData.get("loaded")), "loaded"));

        @Nonnull final JsonArray pullingArray = JsonUtils.getJsonArray(new JsonParser().parse(customData.get("pulling")), "pulling");
        @Nonnull final ResourceLocation[] pulling = new ResourceLocation[pullingArray.size()];

        for(int i = 0; i < pulling.length; i++) pulling[i] = ModelLoader.getInventoryVariant(JsonUtils.getString(pullingArray.get(i), "pulling[" + i + ']'));
        return new CrossbowModel(normal, loaded, pulling);
    }
}
