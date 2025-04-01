package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageContentsPayload;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.OpenMovingStorageInventoryPayload;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.RequestMovingStorageInventoryContentsPayload;

public class ModPayloads {
	private ModPayloads() {
	}

	public static void registerPayloads() {
		registerC2S(OpenMovingStorageInventoryPayload.TYPE, OpenMovingStorageInventoryPayload.STREAM_CODEC, OpenMovingStorageInventoryPayload::handlePayload);
		registerC2S(RequestMovingStorageInventoryContentsPayload.TYPE, RequestMovingStorageInventoryContentsPayload.STREAM_CODEC, RequestMovingStorageInventoryContentsPayload::handlePayload);

		PayloadTypeRegistry.playS2C().register(MovingStorageContentsPayload.TYPE, MovingStorageContentsPayload.STREAM_CODEC);
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			ClientPlayNetworking.registerGlobalReceiver(MovingStorageContentsPayload.TYPE, MovingStorageContentsPayload::handlePayload);
		}
	}

	public static <T extends CustomPacketPayload> void registerC2S(CustomPacketPayload.Type<T> id, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, ServerPlayNetworking.PlayPayloadHandler<T> handler) {
		PayloadTypeRegistry.playC2S().register(id, codec);
		ServerPlayNetworking.registerGlobalReceiver(id, handler);
	}
}
