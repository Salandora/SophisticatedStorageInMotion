package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

import java.util.HashMap;
import java.util.Map;

public class TierUpgradeHandler {
	private TierUpgradeHandler() {
	}

	private static final Map<StorageTierUpgradeItem.TierUpgrade, Map<Item, IEntityTierUpgradeDefinition>> ENTITY_TIER_UPGRADE_DEFINITIONS = new HashMap<>();

	public static void onTierUpgradeInteract(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		ItemStack tierUpgrade;
		StorageTierUpgradeItem storageTierUpgradeItem;
		if (player.getMainHandItem().getItem() instanceof StorageTierUpgradeItem item) {
			tierUpgrade = player.getMainHandItem();
			storageTierUpgradeItem = item;
		} else if (player.getOffhandItem().getItem() instanceof StorageTierUpgradeItem item) {
			tierUpgrade = player.getOffhandItem();
			storageTierUpgradeItem = item;
		} else {
			return;
		}

		Map<Item, IEntityTierUpgradeDefinition> tierDefinitions = ENTITY_TIER_UPGRADE_DEFINITIONS.get(storageTierUpgradeItem.getTier());
		if (tierDefinitions == null) {
			SophisticatedStorageInMotion.LOGGER.warn("No tier upgrade definitions found for {}", storageTierUpgradeItem.getTier());
			return;
		}

		if (event.getTarget() instanceof IMovingStorageEntity movingStorage) {
			upgradeEntity(event, event.getTarget(), player, tierUpgrade, tierDefinitions, movingStorage.getStorageItem().getItem(), movingStorage.getStorageItem());
		} else if (event.getTarget() instanceof MinecartChest minecartChest) {
			upgradeEntity(event, minecartChest, player, tierUpgrade, tierDefinitions, Items.CHEST, ItemStack.EMPTY);
		}
	}

	private static void upgradeEntity(PlayerInteractEvent.EntityInteract event, Entity entity, Player player, ItemStack tierUpgrade, Map<Item, IEntityTierUpgradeDefinition> tierDefinitions, Item tierDefinitionItem, ItemStack storageItem) {
		IEntityTierUpgradeDefinition upgradeDefinition = tierDefinitions.get(tierDefinitionItem);
		if (upgradeDefinition == null) {
			SophisticatedStorageInMotion.LOGGER.warn("No tier upgrade definition found for {}", () -> BuiltInRegistries.ITEM.getKey(tierDefinitionItem));
			return;
		}

		if (!player.level().isClientSide()) {
			upgradeDefinition.upgradeEntity(entity, storageItem);

			if (!player.isCreative()) {
				tierUpgrade.shrink(1);
			}
		}

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
	}

	interface IEntityTierUpgradeDefinition {
		void upgradeEntity(Entity entity, ItemStack storageItem);
	}

	private static class VanillaMinecartChestTierUpgradeDefinition implements IEntityTierUpgradeDefinition {
		private final BlockItem upgradedItem;

		private VanillaMinecartChestTierUpgradeDefinition(BlockItem upgradedItem) {
			this.upgradedItem = upgradedItem;
		}

		public void upgradeEntity(Entity entity, ItemStack storageItem) {
			if (!(entity instanceof MinecartChest minecartChest)) {
				return;
			}

			StorageMinecart storageMinecart = new StorageMinecart(minecartChest.level(), minecartChest.getX(), minecartChest.getY(), minecartChest.getZ());
			storageMinecart.getStorageHolder().setStorageItem(WoodStorageBlockItem.setWoodType(new ItemStack(upgradedItem), WoodType.OAK));
			InventoryHandler inventoryHandler = storageMinecart.getStorageHolder().getStorageWrapper().getInventoryHandler();

			for (int slot = 0; slot < minecartChest.getContainerSize(); slot++) {
				inventoryHandler.setStackInSlot(slot, minecartChest.getItem(slot));
				minecartChest.setItem(slot, ItemStack.EMPTY);
			}

			minecartChest.discard();
			minecartChest.level().addFreshEntity(storageMinecart);
		}
	}

	private static class EntityTierUpgradeDefinition implements IEntityTierUpgradeDefinition {
		private final BlockItem upgradedItem;

		private EntityTierUpgradeDefinition(BlockItem upgradedItem) {
			this.upgradedItem = upgradedItem;
		}

		@Override
		public void upgradeEntity(Entity entity, ItemStack storageItem) {
			if (!(entity instanceof IMovingStorageEntity movingStorage)) {
				return;
			}

			ItemStack newStorageItem = new ItemStack(upgradedItem);

			newStorageItem.applyComponents(storageItem.getComponents());

			movingStorage.getStorageHolder().setStorageItem(newStorageItem);

			if (upgradedItem.getBlock() instanceof StorageBlockBase storageBlock) {
				IStorageWrapper storageWrapper = movingStorage.getStorageHolder().getStorageWrapper();
				if (storageWrapper instanceof MovingStorageWrapper movingStorageWrapper) {
					int additionalInventorySlots = storageBlock.getNumberOfInventorySlots() - storageWrapper.getInventoryHandler().getSlots();
					int additionalUpgradeSlots = storageBlock.getNumberOfUpgradeSlots() - storageWrapper.getUpgradeHandler().getSlots();
					movingStorageWrapper.changeSize(additionalInventorySlots, additionalUpgradeSlots);
				}
			}
		}
	}

	static {
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC, Map.of(
				Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.CHEST_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_COPPER, Map.of(
				Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.COPPER_CHEST_ITEM.get()),
				ModBlocks.BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.COPPER_BARREL_ITEM.get()),
				ModBlocks.CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.COPPER_CHEST_ITEM.get()),
				ModBlocks.SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.COPPER_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_IRON, Map.of(
				Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.IRON_CHEST_ITEM.get()),
				ModBlocks.BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.IRON_BARREL_ITEM.get()),
				ModBlocks.CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.IRON_CHEST_ITEM.get()),
				ModBlocks.SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.IRON_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_GOLD, Map.of(
				Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.GOLD_CHEST_ITEM.get()),
				ModBlocks.BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_BARREL_ITEM.get()),
				ModBlocks.CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_CHEST_ITEM.get()),
				ModBlocks.SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_DIAMOND, Map.of(
				Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.DIAMOND_CHEST_ITEM.get()),
				ModBlocks.BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_BARREL_ITEM.get()),
				ModBlocks.CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_CHEST_ITEM.get()),
				ModBlocks.SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_NETHERITE, Map.of(
				Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.NETHERITE_CHEST_ITEM.get()),
				ModBlocks.BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_BARREL_ITEM.get()),
				ModBlocks.CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_CHEST_ITEM.get()),
				ModBlocks.SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get())
		));

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_IRON, Map.of(
				ModBlocks.COPPER_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.IRON_BARREL_ITEM.get()),
				ModBlocks.COPPER_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.IRON_CHEST_ITEM.get()),
				ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.IRON_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_GOLD, Map.of(
				ModBlocks.COPPER_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_BARREL_ITEM.get()),
				ModBlocks.COPPER_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_CHEST_ITEM.get()),
				ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_DIAMOND, Map.of(
				ModBlocks.COPPER_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_BARREL_ITEM.get()),
				ModBlocks.COPPER_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_CHEST_ITEM.get()),
				ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_NETHERITE, Map.of(
				ModBlocks.COPPER_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_BARREL_ITEM.get()),
				ModBlocks.COPPER_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_CHEST_ITEM.get()),
				ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get())
		));

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.IRON_TO_GOLD, Map.of(
				ModBlocks.IRON_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_BARREL_ITEM.get()),
				ModBlocks.IRON_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_CHEST_ITEM.get()),
				ModBlocks.IRON_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.GOLD_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.IRON_TO_DIAMOND, Map.of(
				ModBlocks.IRON_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_BARREL_ITEM.get()),
				ModBlocks.IRON_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_CHEST_ITEM.get()),
				ModBlocks.IRON_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.IRON_TO_NETHERITE, Map.of(
				ModBlocks.IRON_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_BARREL_ITEM.get()),
				ModBlocks.IRON_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_CHEST_ITEM.get()),
				ModBlocks.IRON_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get())
		));

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_DIAMOND, Map.of(
				ModBlocks.GOLD_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_BARREL_ITEM.get()),
				ModBlocks.GOLD_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_CHEST_ITEM.get()),
				ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_NETHERITE, Map.of(
				ModBlocks.GOLD_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_BARREL_ITEM.get()),
				ModBlocks.GOLD_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_CHEST_ITEM.get()),
				ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get())
		));

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.DIAMOND_TO_NETHERITE, Map.of(
				ModBlocks.DIAMOND_BARREL_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_BARREL_ITEM.get()),
				ModBlocks.DIAMOND_CHEST_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_CHEST_ITEM.get()),
				ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get()),
				ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get()),
				ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get()),
				ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get()),
				ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get(), new EntityTierUpgradeDefinition(ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get())
		));

	}
}
