package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import com.github.salandora.sophisticatedlibrary.network.handling.IPayloadContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

public record OpenMovingStorageInventoryPayload(int entityId) implements CustomPacketPayload {
	public static final Type<OpenMovingStorageInventoryPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getRL("open_storage_inventory"));
	public static final StreamCodec<ByteBuf, OpenMovingStorageInventoryPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			OpenMovingStorageInventoryPayload::entityId,
			OpenMovingStorageInventoryPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(OpenMovingStorageInventoryPayload payload, IPayloadContext context) {
		Player player = context.player();
		Entity entity = player.level().getEntity(payload.entityId());
		if (entity instanceof IMovingStorageEntity storageEntity) {
			storageEntity.getStorageHolder().openContainerMenu(player);
		}
	}
}
