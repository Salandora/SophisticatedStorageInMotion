package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.api.IStashStorageItem;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.ColorHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelMaterial;
import net.p3pp3rf1y.sophisticatedstorage.block.DecorationTableBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static net.p3pp3rf1y.sophisticatedstorage.block.DecorationTableBlockEntity.STORAGE_DECORATOR;

public abstract class MovingStorageItem extends ItemBase implements IStashStorageItem {
	public MovingStorageItem(Properties properties) {
		super(properties);
	}

	public static void setStorageItem(ItemStack movingStorageItem, ItemStack storageItem) {
		NBTHelper.setCompoundNBT(movingStorageItem, EntityStorageHolder.STORAGE_ITEM_TAG, storageItem.save(new CompoundTag()));
	}

	public static Optional<Item> getStorageItemType(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).map(ItemStack::getItem);
	}

	public static Optional<WoodType> getStorageItemWoodType(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).flatMap(WoodStorageBlockItem::getWoodType);
	}

	public static Optional<Integer> getStorageItemMainColor(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).flatMap(StorageBlockItem::getMainColorFromStack);
	}

	public static Optional<Integer> getStorageItemAccentColor(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).flatMap(StorageBlockItem::getAccentColorFromStack);
	}

	public static boolean isStorageItemFlatTopBarrel(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).map(BarrelBlockItem::isFlatTop).orElse(false);
	}

	public static ItemStack getStorageItem(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).orElse(ItemStack.EMPTY);
	}

	public abstract ItemStack getUncraftRemainingItem();

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM), WoodType.SPRUCE)));
			ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM);
			if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.setMainColor(limitedIStack, ColorHelper.getColor(DyeColor.YELLOW.getTextureDiffuseColors()));
				tintableBlockItem.setAccentColor(limitedIStack, ColorHelper.getColor(DyeColor.LIME.getTextureDiffuseColors()));
			}
			itemConsumer.accept(createWithStorage(this, limitedIStack));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2), WoodType.BIRCH)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_3), WoodType.ACACIA)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_DIAMOND_BARREL_4), WoodType.CRIMSON)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM), WoodType.BAMBOO)));
			itemConsumer.accept(createWithStorage(this, new ItemStack(ModBlocks.IRON_SHULKER_BOX_ITEM)));
		}
	}

	public static ItemStack createWithStorage(Item movingStorageItem, ItemStack storageStack) {
		ItemStack movingStorage = new ItemStack(movingStorageItem);
		MovingStorageItem.setStorageItem(movingStorage, storageStack);
		return movingStorage;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, level, tooltip, tooltipFlag);
		if (tooltipFlag.isAdvanced()) {
			getMovingStorageWrapper(stack).getContentsUuid().ifPresent(uuid -> {
				tooltip.add(Component.literal("UUID: " + uuid).withStyle(ChatFormatting.DARK_GRAY));
			});
		}
		if (!Screen.hasShiftDown() && getMovingStorageWrapper(stack).getContentsUuid().isPresent()) {
			tooltip.add(Component.translatable(
					TranslationHelper.INSTANCE.translItemTooltip("storage") + ".press_for_contents",
					Component.translatable(TranslationHelper.INSTANCE.translItemTooltip("storage") + ".shift").withStyle(ChatFormatting.AQUA)
			).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of).<Component>map(storageItem -> Component.translatable(getDescriptionId(), storageItem.getHoverName())).orElse(super.getName(stack));
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			return Optional.ofNullable(MovingStorageItemClient.getTooltipImage(stack));
		}
		return Optional.empty();
	}

	@Override
	public Optional<TooltipComponent> getInventoryTooltip(ItemStack stack) {
		return Optional.of(new MovingStorageContentsTooltip(stack));
	}

	@Override
	public StashResult getItemStashable(ItemStack storageStack, ItemStack stack) {
		if (getStorageItemType(storageStack).map(item -> item instanceof ShulkerBoxItem).orElse(false)) {
			MovingStorageWrapper wrapper = getMovingStorageWrapper(storageStack);

			if (StorageUtil.simulateInsert(wrapper.getInventoryForUpgradeProcessing(), ItemVariant.of(stack), stack.getCount(), null) == 0) {
				return StashResult.NO_SPACE;
			}
			if (wrapper.getInventoryHandler().getSlotTracker().getItems().contains(stack.getItem()) || wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(stack)) {
				return StashResult.MATCH_AND_SPACE;
			}

			return StashResult.SPACE;
		}

		return StashResult.NO_SPACE;
	}

	public static MovingStorageWrapper getMovingStorageWrapper(ItemStack movingStorageStack) {
		ItemStack storageItem = getStorageItem(movingStorageStack);
		MovingStorageWrapper wrapper = MovingStorageWrapper.fromStack(storageItem, () -> {},
				() -> MovingStorageItem.setStorageItem(movingStorageStack, storageItem));
		return wrapper;
	}

	public ItemStack stash(ItemStack movingStorageStack, ItemStack stack, @Nullable Transaction ctx) {
		MovingStorageWrapper wrapper = getMovingStorageWrapper(movingStorageStack);
		if (wrapper.getContentsUuid().isEmpty()) {
			wrapper.setContentsUuid(UUID.randomUUID());
		}
		try (Transaction inner = Transaction.openNested(ctx)) {
			long inserted = wrapper.getInventoryForUpgradeProcessing().insert(ItemVariant.of(stack), stack.getCount(), inner);
			inner.commit();
			return stack.copyWithCount(stack.getCount() - (int) inserted);
		}
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		if (hasCreativeScreenContainerOpen(player) || stack.getCount() > 1 || !slot.mayPickup(player) || slot.getItem().isEmpty() || action != ClickAction.SECONDARY || !isShulkerBoxMovingStorage(stack)) {
			return super.overrideStackedOnOther(stack, slot, action, player);
		}

		ItemStack stackToStash = slot.getItem();
		ItemStack stashResult;
		try(Transaction simulate = Transaction.openOuter()) {
			stashResult = stash(stack, stackToStash, simulate);
		}
		if (stashResult.getCount() != stackToStash.getCount()) {
			int countToTake = stackToStash.getCount() - stashResult.getCount();
			ItemStack takeResult = slot.safeTake(countToTake, countToTake, player);
			stash(stack, takeResult, null);
			return true;
		}

		return super.overrideStackedOnOther(stack, slot, action, player);
	}

	private boolean isShulkerBoxMovingStorage(ItemStack movingStorageStack) {
		return getStorageItemType(movingStorageStack).map(item -> item instanceof ShulkerBoxItem).orElse(false);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
		if (hasCreativeScreenContainerOpen(player) || stack.getCount() > 1 || !slot.mayPlace(stack) || action != ClickAction.SECONDARY || !isShulkerBoxMovingStorage(stack)) {
			return super.overrideOtherStackedOnMe(stack, otherStack, slot, action, player, carriedAccess);
		}

		ItemStack result = stash(stack, otherStack, null);
		if (result.getCount() != otherStack.getCount()) {
			carriedAccess.set(result);
			slot.set(stack);
			return true;
		}

		return super.overrideOtherStackedOnMe(stack, otherStack, slot, action, player, carriedAccess);
	}

	private boolean hasCreativeScreenContainerOpen(Player player) {
		return player.level().isClientSide() && player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu;
	}

	public record MovingStorageContentsTooltip(ItemStack movingStorage) implements TooltipComponent {
		public ItemStack getMovingStorage() {
			return movingStorage;
		}
	}

	static {
		DecorationTableBlockEntity.registerItemDecorator(stack -> stack.getItem() instanceof MovingStorageItem, new DecorationTableBlockEntity.IItemDecorator() {
			@Override
			public boolean supportsMaterials(ItemStack input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsMaterials(storageItem);
			}

			@Override
			public boolean supportsTints(ItemStack input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsTints(storageItem);
			}

			@Override
			public boolean supportsTopInnerTrim(ItemStack input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsTopInnerTrim(storageItem);
			}

			@Override
			public ItemStack decorateWithMaterials(ItemStack input, Map<BarrelMaterial, ResourceLocation> materialsToApply) {
				ItemStack storageItem = getStorageItem(input);
				ItemStack storageResult = STORAGE_DECORATOR.decorateWithMaterials(storageItem, materialsToApply);
				if (storageResult.isEmpty()) {
					return ItemStack.EMPTY;
				}
				ItemStack result = input.copy();
				setStorageItem(result, storageResult);
				return result;
			}

			@Override
			public DecorationTableBlockEntity.TintDecorationResult decorateWithTints(ItemStack input, int mainColorToSet, int accentColorToSet) {
				ItemStack storageItem = getStorageItem(input);
				DecorationTableBlockEntity.TintDecorationResult tintResult = STORAGE_DECORATOR.decorateWithTints(storageItem, mainColorToSet, accentColorToSet);
				if (tintResult.result().isEmpty()) {
					return DecorationTableBlockEntity.TintDecorationResult.EMPTY;
				}
				ItemStack result = input.copy();
				setStorageItem(result, tintResult.result());
				return new DecorationTableBlockEntity.TintDecorationResult(result, tintResult.requiredDyeParts());
			}
		});
	}
}
