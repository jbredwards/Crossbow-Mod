/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.asm.transformer;

import net.minecraft.launchwrapper.IClassTransformer;

import javax.annotation.Nonnull;

/**
 *
 * @author jbred
 *
 */
@SuppressWarnings("unused")
public final class TransformerRenderPlayer implements IClassTransformer
{
    @Nonnull
    @Override
    public byte[] transform(@Nonnull String name, @Nonnull String transformedName, @Nonnull byte[] basicClass) {
        return basicClass;
    }
}
