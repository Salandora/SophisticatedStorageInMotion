package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
import net.p3pp3rf1y.sophisticatedcore.network.SimplePacketBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MovingStorageContentsMessage extends SimplePacketBase {
	private UUID storageUuid;
	private @Nullable CompoundTag contents;

	public MovingStorageContentsMessage(UUID storageUuid, @Nullable CompoundTag contents) {
		this.storageUuid = storageUuid;
		this.contents = contents;
	}

	public MovingStorageContentsMessage(FriendlyByteBuf buffer) {
		this(buffer.readUUID(), buffer.readNbt());
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUUID(this.storageUuid);
		buffer.writeNbt(this.contents);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean handle(SimplePacketBase.Context context) {
		context.enqueueWork(() -> {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player == null || this.contents == null) {
				return;
			}

			MovingStorageData.get(this.storageUuid).setContents(this.storageUuid, this.contents);
			ClientStorageContentsTooltipBase.refreshContents();
		});
		return true;
	}

}
