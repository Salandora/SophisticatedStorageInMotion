package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SettingsScreen;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.CraftingContainerRecipeTransferHandlerBase;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.SettingsGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.StorageGhostIngredientHandler;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@SuppressWarnings("unused")
@JeiPlugin
public class StorageInMotionPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return SophisticatedStorageInMotion.getRL("default");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		IIngredientSubtypeInterpreter<ItemStack> movingStorageNbtInterpreter = (itemStack, context) -> {
			StringJoiner result = new StringJoiner(",");
			MovingStorageItem.getStorageItemType(itemStack).ifPresent(storageItemType -> result.add("storageItemType:" + storageItemType));
			MovingStorageItem.getStorageItemWoodType(itemStack).ifPresent(woodName -> result.add("woodName:" + woodName));
			MovingStorageItem.getStorageItemMainColor(itemStack).ifPresent(mainColor -> result.add("mainColor:" + mainColor));
			MovingStorageItem.getStorageItemAccentColor(itemStack).ifPresent(accentColor -> result.add("accentColor:" + accentColor));
			result.add("flatTop:" + MovingStorageItem.isStorageItemFlatTopBarrel(itemStack));
			return "{" + result + "}";
		};

		registration.registerSubtypeInterpreter(ModItems.STORAGE_MINECART, movingStorageNbtInterpreter);
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		registration.addGuiContainerHandler(MovingStorageScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MovingStorageScreen gui) {
				List<Rect2i> ret = new ArrayList<>();
				gui.getUpgradeSlotsRectangle().ifPresent(ret::add);
				ret.addAll(gui.getUpgradeSettingsControl().getTabRectangles());
				gui.getSortButtonsRectangle().ifPresent(ret::add);
				return ret;
			}
		});

		registration.addGuiContainerHandler(MovingStorageSettingsScreen.class, new IGuiContainerHandler<>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(MovingStorageSettingsScreen gui) {
				return new ArrayList<>(gui.getSettingsTabControl().getTabRectangles());
			}
		});

		registration.addGhostIngredientHandler(StorageScreen.class, new StorageGhostIngredientHandler<>());
		registration.addGhostIngredientHandler(SettingsScreen.class, new SettingsGhostIngredientHandler<>());
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		registration.addRecipes(RecipeTypes.CRAFTING, AssembleRecipesMaker.getShapelessCraftingRecipes(ModItems.STORAGE_MINECART));
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
		IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
		registration.addRecipeTransferHandler(new CraftingContainerRecipeTransferHandlerBase<MovingStorageContainerMenu<?>, CraftingRecipe>(handlerHelper, stackHelper) {
			@Override
			public Class<MovingStorageContainerMenu<?>> getContainerClass() {
				//noinspection unchecked
				return (Class<MovingStorageContainerMenu<?>>) (Class<?>) MovingStorageContainerMenu.class;
			}

			@Override
			public mezz.jei.api.recipe.RecipeType<CraftingRecipe> getRecipeType() {
				return RecipeTypes.CRAFTING;
			}
		}, RecipeTypes.CRAFTING);
	}
}
