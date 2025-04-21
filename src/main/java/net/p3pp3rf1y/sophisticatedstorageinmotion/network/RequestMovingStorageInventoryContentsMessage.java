package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;

import javax.annotation.Nullable;
import java.util.UUID;

public class RequestMovingStorageInventoryContentsMessage extends SimplePacketBase {
	private final UUID storageUuid;

	public RequestMovingStorageInventoryContentsMessage(UUID storageUuid) {
		this.storageUuid = storageUuid;
	}

	public RequestMovingStorageInventoryContentsMessage(FriendlyByteBuf packetBuffer) {
		this(packetBuffer.readUUID());
	}

	@Override
	public void write(FriendlyByteBuf packetBuffer) {
		packetBuffer.writeUUID(this.storageUuid);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> handleMessage(context.getSender(), this));
		return true;
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
		StorageInMotionPacketHandler.sendToClient(player, new MovingStorageContentsMessage(msg.storageUuid, newBaseContentsTag));
	}
}
