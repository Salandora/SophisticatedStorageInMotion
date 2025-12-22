package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import com.github.salandora.sophisticatedlibrary.util.Capabilities;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

import java.util.function.Supplier;

public class ModEntities {
	private ModEntities() {
	}

	public static final EntityType<StorageMinecart> STORAGE_MINECART = registerEntityType("storage_minecart", () -> EntityType.Builder.of((EntityType.EntityFactory<StorageMinecart>) StorageMinecart::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).build(SophisticatedStorageInMotion.MOD_ID + ":storage_minecart"));
	public static final EntityType<StorageBoat> STORAGE_BOAT = registerEntityType("storage_boat", () -> EntityType.Builder.of((EntityType.EntityFactory<StorageBoat>) StorageBoat::new, MobCategory.MISC).sized(1.375F, 0.5625F).clientTrackingRange(10).build(SophisticatedStorageInMotion.MOD_ID + ":storage_boat"));

	public static final MenuType<MovingStorageContainerMenu<?>> MOVING_STORAGE_CONTAINER_TYPE = registerMenu("moving_storage",
			() -> new ExtendedScreenHandlerType<>(MovingStorageContainerMenu::fromBuffer));

	public static final MenuType<MovingStorageSettingsContainerMenu> MOVING_STORAGE_SETTINGS_CONTAINER_TYPE = registerMenu("moving_storage_settings",
			() -> new ExtendedScreenHandlerType<>(MovingStorageSettingsContainerMenu::fromBuffer));

	public static final MenuType<MovingLimitedBarrelContainerMenu<?>> MOVING_LIMITED_BARREL_CONTAINER_TYPE = registerMenu("moving_limited_barrel",
			() -> new ExtendedScreenHandlerType<>(MovingLimitedBarrelContainerMenu::fromBuffer));

	public static final MenuType<MovingLimitedBarrelSettingsContainerMenu> MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE = registerMenu("moving_limited_barrel_settings",
			() -> new ExtendedScreenHandlerType<>(MovingLimitedBarrelSettingsContainerMenu::fromBuffer));

	private static void registerCapabilities() {
		Capabilities.ItemHandler.ENTITY_AUTOMATION.registerForType((entity, direction) -> entity.getStorageHolder().getStorageWrapper().getInventoryForInputOutput(), STORAGE_MINECART);
	}

	public static void registerHandlers() {
		ModEntities.registerCapabilities();
	}

	public static <T extends EntityType<?>> T registerEntityType(String id, Supplier<T> supplier) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, SophisticatedStorageInMotion.getRL(id), supplier.get());
	}
	public static <T extends MenuType<?>> T registerMenu(String id, Supplier<T> supplier) {
		return Registry.register(BuiltInRegistries.MENU, SophisticatedStorageInMotion.getRL(id), supplier.get());
	}
}
