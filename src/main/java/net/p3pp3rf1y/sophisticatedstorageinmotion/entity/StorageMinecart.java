package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.StorageInMotionTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StorageMinecart extends MinecartChest implements IMovingStorageEntity {
	private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(StorageMinecart.class, EntityDataSerializers.OPTIONAL_COMPONENT);
	static final EntityDataAccessor<ItemStack> DATA_STORAGE_ITEM = SynchedEntityData.defineId(StorageMinecart.class, EntityDataSerializers.ITEM_STACK);

	private final EntityStorageHolder<StorageMinecart> entityStorageHolder;

	public StorageMinecart(EntityType<StorageMinecart> entityType, Level level) {
		super(entityType, level);
		entityStorageHolder = new EntityStorageHolder<>(this);
	}

	public StorageMinecart(Level level) {
		this(ModEntities.STORAGE_MINECART.get(), level);
	}

	public StorageMinecart(Level level, double x, double y, double z) {
		this(level);
		this.setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STORAGE_ITEM, ItemStack.EMPTY);
		builder.define(DATA_CUSTOM_NAME, Optional.empty());
	}

	@Override
	public ItemStack getStorageItem() {
		return this.entityData.get(DATA_STORAGE_ITEM);
	}

	@Override
	public void setStorageItem(ItemStack storageItem) {
		this.entityData.set(DATA_STORAGE_ITEM, storageItem.copy());
	}

	@Override
	public EntityStorageHolder<?> getStorageHolder() {
		return entityStorageHolder;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return getStorageHolder().hurt(source, amount, super::hurt);
	}

	@Override
	public void destroy(DamageSource source) {
		this.kill();
		getStorageHolder().onDestroy();
	}

	@Override
	public ItemStack getDropStack() {
		return new ItemStack(ModItems.STORAGE_MINECART.get());
	}

	@Override
	public ItemStack getPickResult() {
		ItemStack result = new ItemStack(ModItems.STORAGE_MINECART.get());
		ItemStack storageItemCopy = getStorageItem().copy();
		storageItemCopy.sophisticatedLibrary_remove(ModCoreDataComponents.STORAGE_UUID);
		result.sophisticatedLibrary_set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItemCopy));
		return result;
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.put("storageHolder", entityStorageHolder.saveData(level().registryAccess()));
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityStorageHolder.readData(level().registryAccess(), tag.getCompound("storageHolder"));
	}

	@Override
	public void tick() {
		super.tick();
		entityStorageHolder.tick();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DATA_STORAGE_ITEM && level().isClientSide()) {
			entityStorageHolder.onStorageItemSynced();
		}
	}

	@Override
	public void setCustomName(@Nullable Component customName) {
		entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(customName));
	}

	@Override
	public Component getCustomName() {
		return entityData.get(DATA_CUSTOM_NAME).orElseGet(() -> Component.translatable(StorageInMotionTranslationHelper.INSTANCE.translEntity("storage_minecart"), getStorageItem().getHoverName()));
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			return getStorageHolder().openContainerMenu(serverPlayer);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
		return getStorageHolder().createMenu(id, playerInventory.player);
	}

	@Override
	public void remove(RemovalReason pReason) {
		//overriden to prevent default minecart logic from using container overrides to drop items when in some cases they are not supposed to be dropped
		setRemoved(pReason);
	}

	@Override
	public int getContainerSize() {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().getSlotCount();
	}

	@Override
	public NonNullList<ItemStack> getItemStacks() {
		return NonNullList.create();
	}

	@Override
	public void addChestVehicleSaveData(CompoundTag tag, HolderLookup.Provider levelRegistry) {
		if (getLootTable() != null) {
			tag.putString("LootTable", this.getLootTable().location().toString());
			if (getLootTableSeed() != 0L) {
				tag.putLong("LootTableSeed", this.getLootTableSeed());
			}
		}
	}

	@Override
	public void readChestVehicleSaveData(CompoundTag tag, HolderLookup.Provider levelRegistry) {
		clearItemStacks();
		if (tag.contains("LootTable", 8)) {
			setLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString("LootTable"))));
			setLootTableSeed(tag.getLong("LootTableSeed"));
		}
	}

	@Override
	public void chestVehicleDestroyed(DamageSource damageSource, Level level, Entity p_entity) {
		//noop
	}

	@Override
	public void clearChestVehicleContent() {
		unpackChestVehicleLootTable(null);
		InventoryHandler inventoryHandler = getStorageHolder().getStorageWrapper().getInventoryHandler();
		for (int slot = 0; slot < inventoryHandler.getSlotCount(); slot++) {
			inventoryHandler.setStackInSlot(slot, ItemStack.EMPTY);
		}
	}

	@Override
	public ItemStack removeChestVehicleItemNoUpdate(int slot) {
		ITrackedContentsItemHandler inventoryHandler = getStorageHolder().getStorageWrapper().getInventoryForInputOutput();
		return inventoryHandler.extractItem(slot, inventoryHandler.getStackInSlot(slot).getCount(), false);
	}

	@Override
	public ItemStack getChestVehicleItem(int slot) {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().getStackInSlot(slot);
	}

	@Override
	public ItemStack removeChestVehicleItem(int slot, int amount) {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().extractItem(slot, amount, false);
	}

	@Override
	public void setChestVehicleItem(int slot, ItemStack stack) {
		getStorageHolder().getStorageWrapper().getInventoryForInputOutput().setStackInSlot(slot, stack);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().isItemValid(slot, stack);
	}

	@Override
	public SlotAccess getChestVehicleSlot(int index) {
		return SlotAccess.NULL;
	}
}
