package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;

import java.util.UUID;

public record RequestMovingStorageInventoryContentsPayload(UUID storageUuid) implements CustomPacketPayload {
	public static final Type<RequestMovingStorageInventoryContentsPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getRL("request_moving_storage_inventory_contents"));
	public static final StreamCodec<ByteBuf, RequestMovingStorageInventoryContentsPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			RequestMovingStorageInventoryContentsPayload::storageUuid,
			RequestMovingStorageInventoryContentsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(RequestMovingStorageInventoryContentsPayload payload, IPayloadContext context) {
		CompoundTag baseContentsTag = MovingStorageData.get(payload.storageUuid).getContents();
		if (!baseContentsTag.contains(StorageWrapper.CONTENTS_TAG)) {
			return;
		}
		CompoundTag contentsTag = baseContentsTag.getCompound(StorageWrapper.CONTENTS_TAG);

		CompoundTag inventoryContents = new CompoundTag();
		Tag inventoryNbt = contentsTag.get(InventoryHandler.INVENTORY_TAG);
		if (inventoryNbt != null) {
			inventoryContents.put(InventoryHandler.INVENTORY_TAG, inventoryNbt);
		}
		Tag upgradeNbt = contentsTag.get(UpgradeHandler.UPGRADE_INVENTORY_TAG);
		if (upgradeNbt != null) {
			inventoryContents.put(UpgradeHandler.UPGRADE_INVENTORY_TAG, upgradeNbt);
		}
		CompoundTag newBaseContentsTag = new CompoundTag();
		newBaseContentsTag.put(StorageWrapper.CONTENTS_TAG, inventoryContents);
		if (context.player() instanceof ServerPlayer serverPlayer) {
			PacketDistributor.sendToPlayer(serverPlayer, new MovingStorageContentsPayload(payload.storageUuid, newBaseContentsTag));
		}
	}
}
