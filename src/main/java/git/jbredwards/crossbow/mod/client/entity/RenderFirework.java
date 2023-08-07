/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.client.entity;

import git.jbredwards.crossbow.mod.common.capability.ICrossbowFireworkData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author jbred
 *
 */
@SideOnly(Side.CLIENT)
public class RenderFirework extends Render<EntityFireworkRocket>
{
    public RenderFirework(@Nonnull RenderManager renderManager) { super(renderManager); }

    @Override
    public void doRender(@Nonnull EntityFireworkRocket entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.rotate(-renderManager.playerViewY, 0, 1, 0);
        GlStateManager.rotate((renderManager.options.thirdPersonView == 2 ? -1 : 1) * renderManager.playerViewX, 1, 0, 0);

        final ICrossbowFireworkData cap = ICrossbowFireworkData.get(entity);
        if(cap == null || !cap.wasShotByCrossbow()) GlStateManager.rotate(180, 0, 1, 0);
        else GlStateManager.rotate(90, 1, 0, 0);

        bindEntityTexture(entity);
        if(renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        final ItemStack stack = entity.getDataManager().get(EntityFireworkRocket.FIREWORK_ITEM);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack.isEmpty() ? new ItemStack(Items.FIREWORKS) : stack, ItemCameraTransforms.TransformType.GROUND);
        if(renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntityFireworkRocket entity) { return TextureMap.LOCATION_BLOCKS_TEXTURE; }
}
