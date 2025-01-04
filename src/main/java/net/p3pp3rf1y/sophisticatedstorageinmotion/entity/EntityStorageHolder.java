package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.api.IUpgradeRenderer;
import net.p3pp3rf1y.sophisticatedcore.client.render.UpgradeRenderRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.renderdata.IUpgradeRenderData;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.renderdata.UpgradeRenderDataType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EntityStorageHolder<T extends Entity & IMovingStorageEntity> {
	private final T entity;

	@Nullable
	private StorageBlockEntity renderBlockEntity = null;

	private IStorageWrapper storageWrapper = NoopStorageWrapper.INSTANCE;

	public EntityStorageHolder(T entity) {
		this.entity = entity;
	}

	public static boolean areUpgradesVisible(ItemStack storageItem) {
		return storageItem.sophisticatedCore_getOrDefault(ModDataComponents.UPGRADES_VISIBLE, false);
	}

	public void setStorageItemFrom(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM.get());
		if (storageItemContents == null) {
			ItemStack barrel = new ItemStack(ModBlocks.BARREL_ITEM.get());
			WoodStorageBlockItem.setWoodType(barrel, WoodType.SPRUCE);
			setStorageItem(barrel);
		} else {
			ItemStack storageItem = storageItemContents.copy();
			setStorageItem(storageItem);
			if (isLimitedBarrel(storageItem)) {
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
		renderBlockEntity = null;
	}

	public void updateStorageWrapper() {
		ItemStack storageItem = entity.getStorageItem();
		UUID id = storageItem.sophisticatedCore_get(ModCoreDataComponents.STORAGE_UUID);
		if (id == null) {
			id = UUID.randomUUID();
			storageItem.sophisticatedCore_set(ModCoreDataComponents.STORAGE_UUID, id);
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
		renderBlockEntity = null;
	}

	private void onContentsChanged() {
		if (entity.level().isClientSide()) {
			return;
		}

		ItemStack storageItem = entity.getStorageItem();
		@Nullable UUID storageId = storageItem.sophisticatedCore_get(ModCoreDataComponents.STORAGE_UUID);
		if (storageId == null) {
			return;
		}
		MovingStorageData.get(storageId).setDirty();
	}

	public void startOpen(Player player) {
		entity.gameEvent(GameEvent.CONTAINER_OPEN, player);
		PiglinAi.angerNearbyPiglins(player, true);
	}

	public void stopOpen(Player player) {
		//noop
	}

	public void tick() {
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
		AABB aabb = entity.getBoundingBox();
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
		return stack.sophisticatedCore_getOrDefault(ModDataComponents.LOCKED, false);
	}

	public static boolean isLockVisible(ItemStack storageItem) {
		return storageItem.sophisticatedCore_getOrDefault(ModDataComponents.LOCK_VISIBLE, true);
	}

	public static CompoundTag getRenderInfoNbt(ItemStack storageItem) {
		return storageItem.sophisticatedCore_getOrDefault(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.EMPTY).copyTag();
	}

	public StorageBlockEntity getRenderBlockEntity() {
		if (renderBlockEntity == null) {
			ItemStack storageItem = entity.getStorageItem();
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

				if (renderBlockEntity != null) {
					if (renderBlockEntity.isLocked() != EntityStorageHolder.isLocked(storageItem)) {
						renderBlockEntity.toggleLock();
					}
					if (renderBlockEntity.shouldShowLock() != EntityStorageHolder.isLockVisible(storageItem)) {
						renderBlockEntity.toggleLockVisibility();
					}
					if (renderBlockEntity.shouldShowTier() != StorageBlockItem.showsTier(storageItem)) {
						renderBlockEntity.toggleTierVisiblity();
					}
					renderBlockEntity.getStorageWrapper().getRenderInfo().deserializeFrom(EntityStorageHolder.getRenderInfoNbt(storageItem));
					if (renderBlockEntity.shouldShowUpgrades() != EntityStorageHolder.areUpgradesVisible(storageItem)) {
						renderBlockEntity.toggleUpgradesVisiblity();
					}
					if (storageItem.getItem() instanceof ITintableBlockItem tintableBlockItem) {
						renderBlockEntity.getStorageWrapper().setMainColor(tintableBlockItem.getMainColor(storageItem).orElse(-1));
						renderBlockEntity.getStorageWrapper().setAccentColor(tintableBlockItem.getAccentColor(storageItem).orElse(-1));
					}
					if (renderBlockEntity instanceof WoodStorageBlockEntity woodStorage) {
						WoodStorageBlockItem.getWoodType(storageItem).ifPresent(woodType -> {
							if (woodStorage.getWoodType() != WoodStorageBlockItem.getWoodType(storageItem)) {
								woodStorage.setWoodType(woodType);
							}
						});
						boolean isPacked = WoodStorageBlockItem.isPacked(storageItem);
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
					}
				}
			}

			if (renderBlockEntity == null) {
				renderBlockEntity = new ChestBlockEntity(BlockPos.ZERO, ModBlocks.CHEST.get().defaultBlockState());
			}
			setRenderBlockEntity(renderBlockEntity);
		}
		return renderBlockEntity;
	}

	public void onStorageItemSynced() {
		renderBlockEntity = null;
		storageWrapper = NoopStorageWrapper.INSTANCE;
	}

	public InteractionResult openContainerMenu(Player player) {
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
			ItemStack drop = new ItemStack(entity.getDropItem());
			drop.sophisticatedCore_set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(entity.getStorageItem()));
			if (entity.hasCustomName()) {
				drop.set(DataComponents.CUSTOM_NAME, entity.getCustomName());
			}
			entity.spawnAtLocation(drop);
			if (!(entity.getStorageItem().getItem() instanceof ShulkerBoxItem)) {
				dropAllItems();
			}
		}
	}

	private void dropAllItems() {
		InventoryHelper.dropItems(getStorageWrapper().getInventoryHandler(), entity.level(), entity.position().x(), entity.position().y(), entity.position().z());
		InventoryHelper.dropItems(getStorageWrapper().getUpgradeHandler(), entity.level(), entity.position().x(), entity.position().y(), entity.position().z());
	}
}
