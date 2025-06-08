package net.p3pp3rf1y.sophisticatedstorageinmotion.extensions;

import net.minecraft.world.item.crafting.Ingredient;

public interface SophisticatedIngredient {
	default Ingredient.Value[] sophisticated_getValues() { throw new RuntimeException("Should have been overriden by mixin."); }
}
