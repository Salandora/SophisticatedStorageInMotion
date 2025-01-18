package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

public class OpenMovingStorageInventoryMessage extends SimplePacketBase {
	private int entityId;

	public OpenMovingStorageInventoryMessage(int entityId) {
		this.entityId = entityId;
	}

	public OpenMovingStorageInventoryMessage(ByteBuf buffer) {
		this(buffer.readInt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeInt(this.entityId);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player == null) {
				return;
			}

			Entity entity = player.level().getEntity(this.entityId);
			if (entity instanceof IMovingStorageEntity storageEntity) {
				storageEntity.getStorageHolder().openContainerMenu(player);
			}
		});
		return true;
	}
}
