package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class MovingStorageData extends SavedData {
	private static final String SAVED_DATA_PREFIX = SophisticatedStorageInMotion.MOD_ID + "/";

	private CompoundTag movingStorageContents = new CompoundTag();

	private boolean toRemove = false;
	private static final Map<UUID, MovingStorageData> clientStorageCopy = new HashMap<>(); //TODO maybe change to cache so that deleted ones get removed?
	private final Set<UUID> updatedStorageSettingsFlags = new HashSet<>();

	private MovingStorageData() {
	}

	public static MovingStorageData get(UUID storageId) {
		if (SophisticatedCore.isLogicalServerThread()) {
			MinecraftServer server = SophisticatedCore.getCurrentServer();
			if (server != null) {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				//noinspection ConstantConditions - by this time overworld is loaded
				DimensionDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(MovingStorageData::load, MovingStorageData::new, SAVED_DATA_PREFIX + storageId);
			}
		}
		return clientStorageCopy.computeIfAbsent(storageId, id -> new MovingStorageData());
	}

	public static MovingStorageData load(CompoundTag nbt) {
		MovingStorageData storageData = new MovingStorageData();
		storageData.movingStorageContents = nbt;
		return storageData;
	}

	@Override
	public CompoundTag save(CompoundTag compound) {
		if (movingStorageContents != null) {
			return movingStorageContents;
		}
		return new CompoundTag();
	}

	public void removeStorageContents() {
		toRemove = true;
		setDirty();
	}

	@Override
	public void save(File file) {
		if (toRemove) {
			file.delete();
		} else {
			try {
				Files.createDirectories(file.toPath().getParent());
			} catch (IOException e) {
				SophisticatedStorageInMotion.LOGGER.error("Failed to create directories for moving storage data", e);
			}
			super.save(file);
		}
	}

	public void setContents(UUID storageUuid, CompoundTag contents) {
		for (String key : contents.getAllKeys()) {
			//noinspection ConstantConditions - the key is one of the tag keys so there's no reason it wouldn't exist here
			movingStorageContents.put(key, contents.get(key));

			if (key.equals(MovingStorageWrapper.SETTINGS_TAG)) {
				updatedStorageSettingsFlags.add(storageUuid);
			}
		}
		setDirty();
	}

	public CompoundTag getContents() {
		return movingStorageContents;
	}

	public void setContents(CompoundTag contents) {
		movingStorageContents = contents;
		setDirty();
	}

	public boolean removeUpdatedStorageSettingsFlag(UUID backpackUuid) {
		return updatedStorageSettingsFlags.remove(backpackUuid);
	}
}
