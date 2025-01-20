package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class RequestMovingStorageInventoryContentsMessage {
	private final UUID storageUuid;

	public RequestMovingStorageInventoryContentsMessage(UUID storageUuid) {
		this.storageUuid = storageUuid;
	}

	public static void encode(RequestMovingStorageInventoryContentsMessage msg, FriendlyByteBuf packetBuffer) {
		packetBuffer.writeUUID(msg.storageUuid);
	}

	public static RequestMovingStorageInventoryContentsMessage decode(FriendlyByteBuf packetBuffer) {
		return new RequestMovingStorageInventoryContentsMessage(packetBuffer.readUUID());
	}

	static void onMessage(RequestMovingStorageInventoryContentsMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(context.getSender(), msg));
		context.setPacketHandled(true);
	}

	public static void handleMessage(@Nullable ServerPlayer player, RequestMovingStorageInventoryContentsMessage msg) {
		if (player == null) {
			return;
		}

		CompoundTag baseContentsTag = MovingStorageData.get(msg.storageUuid).getContents();
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
		StorageInMotionPacketHandler.INSTANCE.sendToClient(player, new MovingStorageContentsMessage(msg.storageUuid, newBaseContentsTag));
	}
}
