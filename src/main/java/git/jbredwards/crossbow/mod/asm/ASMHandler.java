/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm;

import com.google.common.collect.Lists;
import git.jbredwards.crossbow.mod.asm.transformer.*;
import git.jbredwards.crossbow.mod.asm.transformer.modded.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
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
    public String[] getASMTransformerClass() { return new String[] {"git.jbredwards.crossbow.mod.asm.ASMHandler$Transformer"}; }
    public static final class Transformer implements IClassTransformer
    {
        @Nonnull
        private static final List<IClassTransformer> transformers = Lists.newArrayList(
                new TransformerEntityArrow(),
                new TransformerEntityFireworkRocket(),
                new TransformerEntityLivingBase(),
                new TransformerItemFirework(),
                new TransformerModelBiped(),
                new TransformerRenderPlayer(),
                //modded
                new TransformerSpartanWeaponry());

        @Nullable
        @Override
        public byte[] transform(@Nullable final String name, @Nullable final String transformedName, @Nullable final byte[] basicClass) {
            return basicClass != null && transformedName != null ? transformers.stream().reduce(basicClass, (bc, ct) -> ct.transform(name, transformedName, bc), (bc1, bc2) -> bc2) : basicClass;
        }
    }

    // -----
    // NO-OP
    // -----

    @Override
    public void injectData(@Nonnull final Map<String, Object> data) {}

    @Nullable
    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getAccessTransformerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }
}
