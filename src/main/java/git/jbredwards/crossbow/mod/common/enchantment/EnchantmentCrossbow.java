/*
 * Copyright (c) 2023. jbredwards
 * All rights reserved.
 */

package git.jbredwards.crossbow.mod.common.enchantment;

import git.jbredwards.crossbow.mod.common.Crossbow;
import git.jbredwards.crossbow.mod.common.item.ItemCrossbow;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraftforge.common.util.EnumHelper;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.IntUnaryOperator;
import java.util.function.Predicate;

/**
 *
 * @author jbred
 *
 */
public class EnchantmentCrossbow extends Enchantment
{
    @Nonnull
    public static final EnumEnchantmentType CROSSBOW = Objects.requireNonNull(EnumHelper.addEnchantmentType(Crossbow.MODID + "_crossbow", item -> item instanceof ItemCrossbow));

    @Nonnull
    protected IntUnaryOperator minEnch = super::getMinEnchantability, maxEnch = super::getMaxEnchantability;
    protected int maxLevel = 1;

    @Nonnull
    protected Predicate<Enchantment> canApplyWith = ench -> true;
    public EnchantmentCrossbow(@Nonnull Rarity rarityIn) { this(rarityIn, CROSSBOW, EntityEquipmentSlot.MAINHAND); }
    public EnchantmentCrossbow(@Nonnull Rarity rarityIn, @Nonnull EnumEnchantmentType typeIn, @Nonnull EntityEquipmentSlot... slots) {
        super(rarityIn, typeIn, slots);
    }

    @Nonnull
    public EnchantmentCrossbow setMinEnchantability(@Nonnull IntUnaryOperator enchantability) {
        minEnch = enchantability;
        return this;
    }

    @Nonnull
    public EnchantmentCrossbow setMaxEnchantability(@Nonnull IntUnaryOperator enchantability) {
        maxEnch = enchantability;
        return this;
    }

    @Nonnull
    public EnchantmentCrossbow setApplicableCondition(@Nonnull Predicate<Enchantment> condition) {
        canApplyWith = condition;
        return this;
    }

    @Nonnull
    public EnchantmentCrossbow setMaxLevel(int lvl) {
        maxLevel = lvl;
        return this;
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) { return minEnch.applyAsInt(enchantmentLevel); }

    @Override
    public int getMaxEnchantability(int enchantmentLevel) { return maxEnch.applyAsInt(enchantmentLevel); }

    @Override
    protected boolean canApplyTogether(@Nonnull Enchantment ench) { return this != ench && canApplyWith.test(ench); }

    @Override
    public int getMaxLevel() { return maxLevel; }
}
