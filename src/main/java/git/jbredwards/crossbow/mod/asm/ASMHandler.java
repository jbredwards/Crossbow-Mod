/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 *
 * @author jbred
 *
 */
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("Crossbow Plugin")
public final class ASMHandler implements IFMLLoadingPlugin
{
    @Nonnull
    public static final Logger LOGGER = LogManager.getFormatterLogger("Crossbow Plugin");

    @Nonnull
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                //"git.jbredwards.crossbow.mod.asm.transformer.TransformerEntityArrow",
                "git.jbredwards.crossbow.mod.asm.transformer.TransformerEntityFireworkRocket",
                "git.jbredwards.crossbow.mod.asm.transformer.TransformerEntityLivingBase",
                "git.jbredwards.crossbow.mod.asm.transformer.TransformerModelBiped",
                "git.jbredwards.crossbow.mod.asm.transformer.TransformerRenderPlayer"
        };
    }

    @Nullable
    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Override
    public void injectData(@Nonnull Map<String, Object> data) {}

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }
}
