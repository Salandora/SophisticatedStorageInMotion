package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import com.github.salandora.sophisticatedfabriclib.network.api.v0.NetworkEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public record MovingStorageContentsMessage(UUID storageUuid, @Nullable CompoundTag contents) {

	public static void encode(MovingStorageContentsMessage msg, FriendlyByteBuf buffer) {
		buffer.writeUUID(msg.storageUuid);
		buffer.writeNbt(msg.contents);
	}

	public static MovingStorageContentsMessage decode(FriendlyByteBuf buffer) {
		return new MovingStorageContentsMessage(buffer.readUUID(), buffer.readNbt());
	}

	static void onMessage(MovingStorageContentsMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(msg));
		context.setPacketHandled(true);
	}


	private static void handleMessage(MovingStorageContentsMessage msg) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || msg.contents == null) {
			return;
		}

		MovingStorageData.get(msg.storageUuid).setContents(msg.storageUuid, msg.contents);
		ClientStorageContentsTooltipBase.refreshContents();
	}
}
