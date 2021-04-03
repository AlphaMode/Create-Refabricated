// PORTED CREATE SOURCE

package com.simibubi.create.content.logistics.item.filter.attribute;

/*public class EnchantAttribute implements ItemAttribute {
    public static final EnchantAttribute EMPTY = new EnchantAttribute(null);

    private final Enchantment enchantment;

    public EnchantAttribute(@Nullable Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return EnchantmentHelper.get(itemStack).containsKey(enchantment);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        return EnchantmentHelper.get(itemStack).keySet().stream().map(EnchantAttribute::new).collect(Collectors.toList());
    }

    @Override
    public String getTranslationKey() {
        return "has_enchant";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if(enchantment != null)
            parameter = new TranslatableText(enchantment.getTranslationKey()).getString();
        return new Object[] { parameter };
    }

    @Override
    public void writeNBT(CompoundTag nbt) {
        if (enchantment == null)
            return;
        Identifier id = ForgeRegistries.ENCHANTMENTS.getKey(enchantment);
        if (id == null)
            return;
        nbt.putString("id", id.toString());
    }

    @Override
    public ItemAttribute readNBT(CompoundTag nbt) {
        return nbt.contains("id") ? new EnchantAttribute(ForgeRegistries.ENCHANTMENTS.getValue(Identifier.tryParse(nbt.getString("id")))) : EMPTY;
    }
}
*/