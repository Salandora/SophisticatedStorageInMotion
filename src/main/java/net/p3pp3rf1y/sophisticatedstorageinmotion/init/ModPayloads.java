package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import com.github.salandora.sophisticatedfabriclib.network.api.v1.PayloadRegistrar;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageContentsPayload;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.OpenMovingStorageInventoryPayload;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.RequestMovingStorageInventoryContentsPayload;

public class ModPayloads {
	private ModPayloads() {
	}

	public static void registerPayloads() {
		final PayloadRegistrar registrar = PayloadRegistrar.registrar();
		registrar.playToServer(OpenMovingStorageInventoryPayload.TYPE, OpenMovingStorageInventoryPayload.STREAM_CODEC, OpenMovingStorageInventoryPayload::handlePayload);
		registrar.playToServer(RequestMovingStorageInventoryContentsPayload.TYPE, RequestMovingStorageInventoryContentsPayload.STREAM_CODEC, RequestMovingStorageInventoryContentsPayload::handlePayload);
		registrar.playToClient(MovingStorageContentsPayload.TYPE, MovingStorageContentsPayload.STREAM_CODEC, MovingStorageContentsPayload::handlePayload);
	}
}
