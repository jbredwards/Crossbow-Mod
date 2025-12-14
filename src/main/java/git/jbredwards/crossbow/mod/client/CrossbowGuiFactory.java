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

package git.jbredwards.crossbow.mod.client;

import git.jbredwards.crossbow.Tags;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.DefaultGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
public final class CrossbowGuiFactory extends DefaultGuiFactory
{
    public CrossbowGuiFactory() {
        super(Tags.MOD_ID, Tags.MOD_NAME);
    }

    @Nonnull
    @Override
    public GuiScreen createConfigGui(@Nonnull final GuiScreen parentScreen) {
        return new GuiConfig(parentScreen, modid, title) {
            @Override
            public void initGui() {
                if(entryList == null || needsRefresh) {
                    needsRefresh = false;
                    // Translucent config gui while in a world.
                    entryList = new GuiConfigEntries(this, mc) {
                        @Override
                        protected void overlayBackground(final int startY, final int endY, final int startAlpha, final int endAlpha) {
                            if(mc.world != null) drawGradientRect(left, endY, left + width, startY, -1072689136, -804253680);
                            else super.overlayBackground(startY, endY, startAlpha, endAlpha);
                        }

                        @Override
                        protected void drawContainerBackground(@Nonnull final Tessellator tessellator) {
                            if(mc.world != null) drawGradientRect(right, bottom, left, top, -1072689136, -804253680);
                            else super.drawContainerBackground(tessellator);
                        }

                        @Override
                        protected void drawSelectionBox(final int insideLeft, final int insideTop, final int mouseXIn, final int mouseYIn, final float partialTicks) {
                            final double scaleH = mc.displayHeight / new ScaledResolution(mc).getScaledHeight_double();
                            GL11.glEnable(GL11.GL_SCISSOR_TEST);
                            GL11.glScissor(0, (int)(mc.displayHeight - (bottom * scaleH)), mc.displayWidth, (int)((bottom - top) * scaleH));
                            super.drawSelectionBox(insideLeft, insideTop, mouseXIn, mouseYIn, partialTicks);
                            GL11.glDisable(GL11.GL_SCISSOR_TEST);
                        }
                    };
                }

                super.initGui();
            }
        };
    }
}
