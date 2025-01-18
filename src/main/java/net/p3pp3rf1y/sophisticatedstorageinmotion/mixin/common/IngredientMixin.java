package net.p3pp3rf1y.sophisticatedstorageinmotion.mixin.common;

import net.minecraft.world.item.crafting.Ingredient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.extensions.SophisticatedIngredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Ingredient.class)
public class IngredientMixin implements SophisticatedIngredient {
	@Shadow @Final private Ingredient.Value[] values;

	@Override
	public Ingredient.Value[] sophisticated_getValues() {
		if (((Ingredient) (Object) this).getCustomIngredient() != null) {
			throw new IllegalStateException("Cannot retrieve values from custom ingredient!");
		} else {
			return this.values;
		}
	}
}
