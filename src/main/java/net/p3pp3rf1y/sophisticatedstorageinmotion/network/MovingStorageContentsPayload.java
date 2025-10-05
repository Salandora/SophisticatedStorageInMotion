package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import com.github.salandora.sophisticatedlibrary.network.api.v1.IPayloadContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;

import java.util.UUID;

public record MovingStorageContentsPayload(UUID storageUuid, CompoundTag contents) implements CustomPacketPayload {
	public static final Type<MovingStorageContentsPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getRL("storage_contents"));
	public static final StreamCodec<ByteBuf, MovingStorageContentsPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			MovingStorageContentsPayload::storageUuid,
			ByteBufCodecs.COMPOUND_TAG,
			MovingStorageContentsPayload::contents,
			MovingStorageContentsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MovingStorageContentsPayload payload, IPayloadContext context) {
		MovingStorageData.get(payload.storageUuid).setContents(payload.storageUuid, payload.contents);
		ClientStorageContentsTooltipBase.refreshContents();
	}
}
