package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import com.github.salandora.sophisticatedfabriclib.network.api.v0.NetworkEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record OpenMovingStorageInventoryMessage(int entityId) {

	public static void encode(OpenMovingStorageInventoryMessage msg, ByteBuf buffer) {
		buffer.writeInt(msg.entityId());
	}

	public static OpenMovingStorageInventoryMessage decode(ByteBuf buffer) {
		return new OpenMovingStorageInventoryMessage(buffer.readInt());
	}

	static void onMessage(OpenMovingStorageInventoryMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(context.getSender(), msg));
		context.setPacketHandled(true);
	}

	public static void handleMessage(@Nullable ServerPlayer player, OpenMovingStorageInventoryMessage payload) {
		if (player == null) {
			return;
		}

		Entity entity = player.level().getEntity(payload.entityId());
		if (entity instanceof IMovingStorageEntity storageEntity) {
			storageEntity.getStorageHolder().openContainerMenu(player);
		}
	}
}
