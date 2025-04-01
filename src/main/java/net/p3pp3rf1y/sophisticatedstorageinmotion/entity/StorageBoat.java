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
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.StorageInMotionTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public class StorageBoat extends ChestBoat implements IMovingStorageEntity {
	private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(StorageBoat.class, EntityDataSerializers.OPTIONAL_COMPONENT);
	static final EntityDataAccessor<ItemStack> DATA_STORAGE_ITEM = SynchedEntityData.defineId(StorageBoat.class, EntityDataSerializers.ITEM_STACK);

	private final EntityStorageHolder<StorageBoat> entityStorageHolder;

	public StorageBoat(EntityType<? extends Boat> entityType, Level level) {
		super(entityType, level);
		entityStorageHolder = new EntityStorageHolder<>(this);
	}

	public StorageBoat(Level level) {
		this(ModEntities.STORAGE_BOAT.get(), level);
	}

	public StorageBoat(Level level, double x, double y, double z) {
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
		return StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT.get()), getVariant());
	}

	@Override
	public ItemStack getPickResult() {
		ItemStack result = getDropStack();
		ItemStack storageItemCopy = getStorageItem().copy();
		storageItemCopy.remove(ModCoreDataComponents.STORAGE_UUID);
		result.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItemCopy));
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
		String boatDescId = getVariant().isRaft() ? "storage_raft" : "storage_boat";
		return entityData.get(DATA_CUSTOM_NAME).orElseGet(() -> Component.translatable(StorageInMotionTranslationHelper.INSTANCE.translEntity(boatDescId), getWoodName(getVariant()), getStorageItem().getHoverName()));
	}

	private Component getWoodName(Boat.Type type) {
		return Component.translatable("wood_name." + SophisticatedStorage.MOD_ID + "." + type.name().toLowerCase(Locale.ROOT));
	}

	@Override
	protected Component getTypeName() {
		if (getVariant().isRaft()) {
			return Component.translatable("entity." + SophisticatedStorageInMotion.MOD_ID + ".storage_raft");
		}

		return super.getTypeName();
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (!player.isSecondaryUseActive()) {
			InteractionResult result = super.interact(player, hand);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}

		if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		} else {
			if (player instanceof ServerPlayer serverPlayer) {
				return getStorageHolder().openContainerMenu(serverPlayer);
			}
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
		return getStorageHolder().createMenu(id, player);
	}

	@Override
	public void openCustomInventoryScreen(Player player) {
		getStorageHolder().openContainerMenu(player);
	}

	@Override
	public void remove(RemovalReason pReason) {
		//overriden to prevent default boat logic from using container overrides to drop items when in some cases they are not supposed to be dropped
		setRemoved(pReason);
	}

	@Override
	public int getContainerSize() {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().getSlots();
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
		for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
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
