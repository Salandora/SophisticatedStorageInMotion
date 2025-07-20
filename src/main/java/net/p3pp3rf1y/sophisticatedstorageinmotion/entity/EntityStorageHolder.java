package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.AABB;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IUpgradeRenderer;
import net.p3pp3rf1y.sophisticatedcore.client.render.UpgradeRenderRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.renderdata.IUpgradeRenderData;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.renderdata.UpgradeRenderDataType;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

public class EntityStorageHolder<T extends Entity & IMovingStorageEntity> implements ILockable, ICountDisplay, ITierDisplay, IUpgradeDisplay, IFillLevelDisplay, IMaterialHolder {
	private static final int AVERAGE_DROPPED_ITEM_ENTITY_STACK_SIZE = 20;
	private final T entity;

	@Nullable
	private StorageBlockEntity renderBlockEntity = null;

	private IStorageWrapper storageWrapper = NoopStorageWrapper.INSTANCE;

	private final MovingStorageOpenersCounter openersCounter;

	private boolean updateRenderBlockEntityAttributes = false;

	public EntityStorageHolder(T entity) {
		this.entity = entity;

		openersCounter = new MovingStorageOpenersCounter(entity) {
			@Override
			protected void onOpen() {
				if (isBarrel(entity.getStorageItem())) {
					playSound(SoundEvents.BARREL_OPEN);
					updateBarrelOpenBlockState(true);
				} else if (isShulkerBox(entity.getStorageItem())) {
					playSound(SoundEvents.SHULKER_BOX_OPEN);
					if (renderBlockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
						shulkerBoxBlockEntity.setAnimationStatus(ShulkerBoxBlockEntity.AnimationStatus.OPENING);
					}
				} else if (isChest(entity.getStorageItem())) {
					playSound(SoundEvents.CHEST_OPEN);
					if (renderBlockEntity instanceof ChestBlockEntity chestBlockEntity) {
						chestBlockEntity.getChestLidController().shouldBeOpen(true);
					}
				}
			}

			@Override
			protected void onClose() {
				if (isBarrel(entity.getStorageItem())) {
					playSound(SoundEvents.BARREL_CLOSE);
					updateBarrelOpenBlockState(false);
				} else if (isShulkerBox(entity.getStorageItem())) {
					playSound(SoundEvents.SHULKER_BOX_CLOSE);
					if (renderBlockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
						shulkerBoxBlockEntity.setAnimationStatus(ShulkerBoxBlockEntity.AnimationStatus.CLOSING);
					}
				} else if (isChest(entity.getStorageItem())) {
					playSound(SoundEvents.CHEST_CLOSE);
					if (renderBlockEntity instanceof ChestBlockEntity chestBlockEntity) {
						chestBlockEntity.getChestLidController().shouldBeOpen(false);
					}
				}
			}

			private void playSound(SoundEvent sound) {
				entity.level().playSound(null, entity, sound, SoundSource.BLOCKS, 0.5F, entity.level().random.nextFloat() * 0.1F + 0.9F);
			}
		};
	}

	private void updateBarrelOpenBlockState(boolean open) {
		if (getRenderBlockEntity() instanceof BarrelBlockEntity barrelBlockEntity && !(barrelBlockEntity instanceof LimitedBarrelBlockEntity)) {
			barrelBlockEntity.setBlockState(barrelBlockEntity.getBlockState().setValue(BarrelBlock.OPEN, open));
		}
	}

	private boolean isBarrel(ItemStack storageItem) {
		return storageItem.getItem() instanceof BarrelBlockItem;
	}

	private boolean isShulkerBox(ItemStack storageItem) {
		return storageItem.getItem() instanceof ShulkerBoxItem;
	}

	private boolean isChest(ItemStack storageItem) {
		return storageItem.getItem() instanceof ChestBlockItem;
	}

	public static boolean areUpgradesVisible(ItemStack storageItem) {
		return storageItem.sophisticatedLibrary_getOrDefault(ModDataComponents.UPGRADES_VISIBLE, false);
	}

	public static boolean areCountsVisible(ItemStack storageItem) {
		return storageItem.sophisticatedLibrary_getOrDefault(ModDataComponents.COUNTS_VISIBLE, true);
	}

	public static boolean areFillLevelsVisible(ItemStack storageItem) {
		return storageItem.sophisticatedLibrary_getOrDefault(ModDataComponents.FILL_LEVELS_VISIBLE, false);
	}

	public void setStorageItemFrom(ItemStack stack, boolean setupDefaults) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM.get());
		if (storageItemContents == null) {
			ItemStack barrel = new ItemStack(ModBlocks.BARREL_ITEM.get());
			WoodStorageBlockItem.setWoodType(barrel, WoodType.SPRUCE);
			setStorageItem(barrel);
		} else {
			ItemStack storageItem = storageItemContents.copy();
			setStorageItem(storageItem);
			if (setupDefaults && isLimitedBarrel(storageItem)) {
				LimitedBarrelBlock.setupDefaultSettings(getStorageWrapper(), storageWrapper instanceof MovingStorageWrapper movingStorageWrapper ? movingStorageWrapper.getNumberOfInventorySlots() : storageWrapper.getInventoryHandler().getSlotCount());
			}
		}
	}

	public CompoundTag saveData(HolderLookup.Provider registries) {
		CompoundTag ret = new CompoundTag();
		ItemStack storageItem = entity.getStorageItem();
		if (!storageItem.isEmpty()) {
			ret.put("storageItem", storageItem.save(registries, new CompoundTag()));
		}
		return ret;
	}

	public void readData(HolderLookup.Provider registries, CompoundTag tag) {
		if (tag.contains("storageItem")) {
			setStorageItem(ItemStack.parseOptional(registries, tag.getCompound("storageItem")));
		}
	}

	public void setStorageItem(ItemStack storageItem) {
		entity.setStorageItem(storageItem);
		storageWrapper = NoopStorageWrapper.INSTANCE; //reset storage wrapper to force update when it's next requested
		updateRenderBlockEntityAttributes = true;
	}

	public void updateStorageWrapper() {
		ItemStack storageItem = entity.getStorageItem();
		UUID id = storageItem.sophisticatedLibrary_get(ModCoreDataComponents.STORAGE_UUID);
		if (id == null) {
			id = UUID.randomUUID();
			storageItem.sophisticatedLibrary_set(ModCoreDataComponents.STORAGE_UUID, id);
			setStorageItem(storageItem);
		}

		storageWrapper = MovingStorageWrapper.fromStack(storageItem, this::onContentsChanged, this::onStackChanged);
	}

	public IStorageWrapper getStorageWrapper() {
		if (!entity.getStorageItem().isEmpty() && storageWrapper == NoopStorageWrapper.INSTANCE) {
			updateStorageWrapper();
		}

		return storageWrapper;
	}

	private void setRenderBlockEntity(StorageBlockEntity storageBlockEntity) {
		this.renderBlockEntity = storageBlockEntity;
	}

	private void onStackChanged() {
		entity.setStorageItem(getStorageWrapper().getWrappedStorageStack());
		updateRenderBlockEntityAttributes = true;
	}

	private void onContentsChanged() {
		if (entity.level().isClientSide()) {
			return;
		}

		ItemStack storageItem = entity.getStorageItem();
		@Nullable UUID storageId = storageItem.sophisticatedLibrary_get(ModCoreDataComponents.STORAGE_UUID);
		if (storageId == null) {
			return;
		}
		MovingStorageData.get(storageId).setDirty();
	}

	public void startOpen(Player player) {
		if (!entity.isRemoved() && !player.isSpectator()) {
			openersCounter.incrementOpeners(player);
		}
		PiglinAi.angerNearbyPiglins(player, true);
		if (renderBlockEntity != null) {
			renderBlockEntity.startOpen(player);
		}
	}

	public void stopOpen(Player player) {
		if (!entity.isRemoved() && !player.isSpectator()) {
			openersCounter.decrementOpeners(player);
		}
		if (renderBlockEntity != null) {
			renderBlockEntity.stopOpen(player);
		}
	}

	public void tick() {
		openersCounter.tick();
		if (entity.level().isClientSide()) {
			clientTick();
			return;
		}
		getStorageWrapper().getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class).forEach(upgrade -> upgrade.tick(entity, entity.level(), entity.blockPosition()));
		runPickupOnItemEntities();
	}

	private void clientTick() {
		if (entity.level().random.nextInt(10) == 0) {
			RenderInfo renderInfo = getStorageWrapper().getRenderInfo();
			renderUpgrades(entity.level(), entity.level().random, renderInfo);
		}
		if (renderBlockEntity instanceof ChestBlockEntity chestBlockEntity) {
			ChestBlockEntity.lidAnimateTick(chestBlockEntity);
		} else if (renderBlockEntity instanceof ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
			ShulkerBoxBlockEntity.tick(null, BlockPos.ZERO, renderBlockEntity.getBlockState(), shulkerBoxBlockEntity);
		}
	}

	protected void renderUpgrades(Level level, RandomSource rand, RenderInfo renderInfo) {
		if (Minecraft.getInstance().isPaused()) {
			return;
		}
		renderInfo.getUpgradeRenderData().forEach((type, data) -> UpgradeRenderRegistry.getUpgradeRenderer(type).ifPresent(renderer -> {
			renderUpgrade(renderer, level, rand, type, data);
		}));
	}

	private <T extends IUpgradeRenderData> void renderUpgrade(IUpgradeRenderer<T> renderer, Level level, RandomSource rand, UpgradeRenderDataType<?> type, IUpgradeRenderData data) {
		//noinspection unchecked
		type.cast(data).ifPresent(renderData -> renderer.render(level, rand, vector -> vector.add((float) entity.position().x(), (float) entity.position().y() + 0.8f, (float) entity.position().z()), (T) renderData));
	}

	private void runPickupOnItemEntities() {
		AABB aabb = entity.getBoundingBox().inflate(0.2D);
		List<ItemEntity> collidedWithItemEntities = entity.level().getEntitiesOfClass(ItemEntity.class, aabb);
		collidedWithItemEntities.forEach(itemEntity -> {
			if (itemEntity.isAlive()) {
				tryToPickup(entity.level(), itemEntity);
			}
		});
	}

	protected void tryToPickup(Level level, ItemEntity itemEntity) {
		ItemStack remainingStack = itemEntity.getItem().copy();
		remainingStack = InventoryHelper.runPickupOnPickupResponseUpgrades(level, getStorageWrapper().getUpgradeHandler(), remainingStack, false);
		if (remainingStack.getCount() < itemEntity.getItem().getCount()) {
			itemEntity.setItem(remainingStack);
		}
	}

	public static boolean isLocked(ItemStack stack) {
		return stack.sophisticatedLibrary_getOrDefault(ModDataComponents.LOCKED, false);
	}

	public static boolean isLockVisible(ItemStack storageItem) {
		return storageItem.sophisticatedLibrary_getOrDefault(ModDataComponents.LOCK_VISIBLE, true);
	}

	public static CompoundTag getRenderInfoNbt(ItemStack storageItem) {
		return storageItem.sophisticatedLibrary_getOrDefault(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.EMPTY).copyTag();
	}

	public StorageBlockEntity getRenderBlockEntity() {
		ItemStack storageItem = entity.getStorageItem();
		if (renderBlockEntity == null) {
			if (storageItem.getItem() instanceof BlockItem blockItem) {
				if (blockItem.getBlock() instanceof ChestBlock) {
					renderBlockEntity = new ChestBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
				} else if (blockItem.getBlock() instanceof LimitedBarrelBlock) {
					renderBlockEntity = new LimitedBarrelBlockEntity(BlockPos.ZERO,
							blockItem.getBlock().defaultBlockState()
									.setValue(LimitedBarrelBlock.HORIZONTAL_FACING, Direction.NORTH)
									.setValue(LimitedBarrelBlock.VERTICAL_FACING, VerticalFacing.UP)
					);
				} else if (blockItem.getBlock() instanceof BarrelBlock) {
					renderBlockEntity = new BarrelBlockEntity(BlockPos.ZERO,
							blockItem.getBlock().defaultBlockState()
									.setValue(BarrelBlock.FACING, Direction.UP)
					);
				} else if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
					renderBlockEntity = new ShulkerBoxBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
				}
			}

			if (renderBlockEntity == null) {
				renderBlockEntity = new ChestBlockEntity(BlockPos.ZERO, ModBlocks.CHEST.get().defaultBlockState());
			}
			setRenderBlockEntity(renderBlockEntity);
		}

		if (renderBlockEntity != null && updateRenderBlockEntityAttributes) {
			updateRenderBlockEntityAttributes = false;
			if (renderBlockEntity.isLocked() != EntityStorageHolder.isLocked(storageItem)) {
				renderBlockEntity.toggleLock();
			}
			if (renderBlockEntity.shouldShowLock() != EntityStorageHolder.isLockVisible(storageItem)) {
				renderBlockEntity.toggleLockVisibility();
			}
			if (renderBlockEntity.shouldShowTier() != StorageBlockItem.showsTier(storageItem)) {
				renderBlockEntity.toggleTierVisiblity();
			}
			renderBlockEntity.getStorageWrapper().getRenderInfo().deserializeFrom(getRenderInfoNbt(storageItem));
			if (renderBlockEntity.shouldShowUpgrades() != EntityStorageHolder.areUpgradesVisible(storageItem)) {
				renderBlockEntity.toggleUpgradesVisiblity();
			}
			if (storageItem.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				renderBlockEntity.getStorageWrapper().setColors(tintableBlockItem.getMainColor(storageItem).orElse(-1), tintableBlockItem.getAccentColor(storageItem).orElse(-1));
			}
			if (renderBlockEntity instanceof WoodStorageBlockEntity woodStorage) {
				WoodStorageBlockItem.getWoodType(storageItem).ifPresent(woodType -> {
					if (woodStorage.getWoodType() != WoodStorageBlockItem.getWoodType(storageItem)) {
						woodStorage.setWoodType(woodType);
					}
				});
				boolean isPacked = EntityStorageHolder.isPacked(storageItem);
				if (woodStorage.isPacked() != isPacked) {
					woodStorage.setPacked(isPacked);
				}
			}
			if (renderBlockEntity instanceof BarrelBlockEntity barrel) {
				Map<BarrelMaterial, ResourceLocation> materials = BarrelBlockItem.getMaterials(storageItem);
				if (!barrel.getMaterials().equals(materials)) {
					barrel.setMaterials(materials);
				}
				barrel.setDynamicRenderTracker(new IDynamicRenderTracker() {
					@Override
					public boolean isDynamicRenderer() {
						return true;
					}

					@Override
					public boolean isFullyDynamicRenderer() {
						return true;
					}

					@Override
					public void onRenderInfoUpdated(RenderInfo ri) {
						//noop
					}
				});

				if (renderBlockEntity instanceof LimitedBarrelBlockEntity limitedBarrelBlockEntity) {
					if (limitedBarrelBlockEntity.shouldShowFillLevels() != EntityStorageHolder.areFillLevelsVisible(storageItem)) {
						limitedBarrelBlockEntity.toggleFillLevelVisibility();
					}
					if (limitedBarrelBlockEntity.shouldShowCounts() != EntityStorageHolder.areCountsVisible(storageItem)) {
						limitedBarrelBlockEntity.toggleCountVisibility();
					}
				}
			}
			if (renderBlockEntity instanceof ChestBlockEntity chestBlockEntity) {
				chestBlockEntity.showUpgradesOnTop = true;
			}
		}

		return renderBlockEntity;
	}

	private static boolean isPacked(ItemStack storageItem) {
		return WoodStorageBlockItem.isPacked(storageItem);
	}

	public void onStorageItemSynced() {
		if (renderBlockEntity != null && renderBlockEntity.getBlockState().getBlock().asItem() != entity.getStorageItem().getItem()) {
			renderBlockEntity = null;
		}
		updateRenderBlockEntityAttributes = true;
		storageWrapper = NoopStorageWrapper.INSTANCE;
	}

	public InteractionResult openContainerMenu(Player player) {
		if (isPacked(entity.getStorageItem())) {
			return InteractionResult.PASS;
		}

		player.sophisticatedCore_openMenu(new SophisticatedMenuProvider((w, p, pl) -> createMenu(w, pl), entity.getName(), false), buffer -> buffer.writeInt(entity.getId()));
		return player.level().isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
	}

	public MovingStorageContainerMenu<? extends Entity> createMenu(int id, Player pl) {
		if (isLimitedBarrel(entity.getStorageItem())) {
			return new MovingLimitedBarrelContainerMenu<>(id, pl, entity.getId());
		} else {
			return new MovingStorageContainerMenu<>(id, pl, entity.getId());
		}
	}

	public static boolean isLimitedBarrel(ItemStack storageItem) {
		return storageItem.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof LimitedBarrelBlock;
	}

	public void onDestroy() {
		if (entity.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			if (Config.COMMON.dropPacked.get()) {
				pack();
			}
			ItemStack storageItem = entity.getStorageItem();
			if (!isShulkerBox(storageItem) && !isPacked(storageItem)) {
				dropAllItems();
				if (storageItem.sophisticatedLibrary_has(ModCoreDataComponents.STORAGE_UUID)) {
					MovingStorageData.get(storageItem.sophisticatedLibrary_get(ModCoreDataComponents.STORAGE_UUID)).removeStorageContents();
					storageItem.sophisticatedLibrary_remove(ModCoreDataComponents.STORAGE_UUID);
				}
			}
			ItemStack drop = entity.getDropStack();
			drop.sophisticatedLibrary_set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItem));
			if (entity.hasCustomName()) {
				drop.set(DataComponents.CUSTOM_NAME, entity.getCustomName());
			}
			entity.spawnAtLocation(drop);
		}
	}

	private void dropAllItems() {
		InventoryHelper.dropItems(getStorageWrapper().getInventoryHandler(), entity.level(), entity.position().x(), entity.position().y(), entity.position().z());
		InventoryHelper.dropItems(getStorageWrapper().getUpgradeHandler(), entity.level(), entity.position().x(), entity.position().y(), entity.position().z());
	}

	@Override
	public void toggleLock() {
		ItemStack storageItem = entity.getStorageItem();
		boolean locked = !isLocked(storageItem);

		if (memorizesItemsWhenLocked()) {
			if (locked) {
				getStorageWrapper().getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).selectSlots(0, getStorageWrapper().getInventoryHandler().getSlotCount());
			} else {
				getStorageWrapper().getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).unselectAllSlots();
				ItemDisplaySettingsCategory itemDisplaySettings = getStorageWrapper().getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class);
				InventoryHelper.iterate(getStorageWrapper().getInventoryHandler(), (slot, stack) -> {
					if (stack.isEmpty()) {
						itemDisplaySettings.itemChanged(slot);
					}
				});
			}
		}

		storageItem.sophisticatedLibrary_set(ModDataComponents.LOCKED, locked);
		setStorageItem(storageItem);
	}

	private boolean memorizesItemsWhenLocked() {
		return isLimitedBarrel(entity.getStorageItem());
	}

	@Override
	public boolean isLocked() {
		return isLocked(entity.getStorageItem());
	}

	@Override
	public boolean shouldShowLock() {
		return isLockVisible(entity.getStorageItem());
	}

	@Override
	public void toggleLockVisibility() {
		ItemStack storageItem = entity.getStorageItem();
		storageItem.sophisticatedLibrary_set(ModDataComponents.LOCK_VISIBLE, !isLockVisible(storageItem));
		setStorageItem(storageItem);
	}

	@Override
	public boolean shouldShowCounts() {
		return areCountsVisible(entity.getStorageItem());
	}

	@Override
	public void toggleCountVisibility() {
		ItemStack storageItem = entity.getStorageItem();
		storageItem.sophisticatedLibrary_set(ModDataComponents.COUNTS_VISIBLE, !areCountsVisible(storageItem));
		setStorageItem(storageItem);
	}

	@Override
	public List<Integer> getSlotCounts() {
		return isLimitedBarrel(entity.getStorageItem()) ? getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getSlotCounts() : List.of();
	}

	@Override
	public boolean shouldShowFillLevels() {
		return areFillLevelsVisible(entity.getStorageItem());
	}

	@Override
	public void toggleFillLevelVisibility() {
		ItemStack storageItem = entity.getStorageItem();
		storageItem.sophisticatedLibrary_set(ModDataComponents.FILL_LEVELS_VISIBLE, !areFillLevelsVisible(storageItem));
		setStorageItem(storageItem);
	}

	@Override
	public List<Float> getSlotFillLevels() {
		return isLimitedBarrel(entity.getStorageItem()) ? getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getSlotFillRatios() : List.of();
	}

	@Override
	public boolean shouldShowTier() {
		return StorageBlockItem.showsTier(entity.getStorageItem());
	}

	@Override
	public void toggleTierVisiblity() {
		ItemStack storageItem = entity.getStorageItem();
		StorageBlockItem.setShowsTier(storageItem, !StorageBlockItem.showsTier(storageItem));
		setStorageItem(storageItem);
	}

	@Override
	public boolean shouldShowUpgrades() {
		return areUpgradesVisible(entity.getStorageItem());
	}

	@Override
	public void toggleUpgradesVisiblity() {
		ItemStack storageItem = entity.getStorageItem();
		storageItem.sophisticatedLibrary_set(ModDataComponents.UPGRADES_VISIBLE, !areUpgradesVisible(storageItem));
		setStorageItem(storageItem);
	}

	public boolean isOpen() {
		return openersCounter.getOpenerCount() > 0;
	}

	public boolean pack() {
		if (isShulkerBox(entity.getStorageItem()) || isPacked(entity.getStorageItem())) {
			return false;
		}

		ItemStack storageItem = entity.getStorageItem();
		WoodStorageBlockItem.setPacked(storageItem, true);
		setStorageItem(storageItem);

		return true;
	}

	public void onPlace() {
		if (isPacked(entity.getStorageItem())) {
			ItemStack storageItem = entity.getStorageItem();
			WoodStorageBlockItem.setPacked(storageItem, false);
			setStorageItem(storageItem);
		}
	}

	private boolean canBeHurtByWithFeedback(DamageSource source) {
		if (Config.COMMON.dropPacked.get() || isPacked() || !(source.getEntity() instanceof Player player)) {
			return true;
		}

		if (player.isCrouching() || isShulkerBox(entity.getStorageItem())) {
			return true;
		}

		AtomicInteger droppedItemEntityCount = new AtomicInteger(0);
		InventoryHelper.iterate(getStorageWrapper().getInventoryHandler(), (slot, stack) -> {
			if (stack.isEmpty()) {
				return;
			}
			droppedItemEntityCount.addAndGet((int) Math.ceil(stack.getCount() / (double) Math.min(stack.getMaxStackSize(), AVERAGE_DROPPED_ITEM_ENTITY_STACK_SIZE)));
		});

		if (droppedItemEntityCount.get() <= Config.SERVER.tooManyItemEntityDrops.get()) {
			return true;
		}

		ItemBase packingTapeItem = ModItems.PACKING_TAPE.get();
		Component packingTapeItemName = packingTapeItem.getName(new ItemStack(packingTapeItem)).copy().withStyle(ChatFormatting.GREEN);
		player.sendSystemMessage(StorageTranslationHelper.INSTANCE.translStatusMessage("too_many_item_entity_drops",
				entity.getName().copy().withStyle(ChatFormatting.GREEN),
				Component.literal(String.valueOf(droppedItemEntityCount.get())).withStyle(ChatFormatting.RED),
				packingTapeItemName)
		);
		return false;
	}

	public boolean isPacked() {
		return isPacked(entity.getStorageItem());
	}

	@Override
	public void setMaterials(Map<BarrelMaterial, ResourceLocation> materials) {
		ItemStack storageItem = entity.getStorageItem();
		if (isBarrel(storageItem)) {
			BarrelBlockItem.setMaterials(storageItem, materials);
			setStorageItem(storageItem);
		}
	}

	@Override
	public Map<BarrelMaterial, ResourceLocation> getMaterials() {
		return isBarrel(entity.getStorageItem()) ? BarrelBlockItem.getMaterials(entity.getStorageItem()) : Collections.emptyMap();
	}

	@Override
	public boolean canHoldMaterials() {
		return isBarrel(entity.getStorageItem());
	}

	public boolean hurt(DamageSource source, float amount, BiFunction<DamageSource, Float, Boolean> superHurt) {
		if (canBeHurtByWithFeedback(source) && superHurt.apply(source, amount)) {
			if (source.getEntity() instanceof Player player && player.getAbilities().instabuild && entity.isRemoved()) {
				dropAllItems();
			}
			return true;
		}
		return false;
	}
}
