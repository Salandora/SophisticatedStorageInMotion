package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.jei;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.compat.jei.subtypes.PropertyBasedSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

public class MovingStorageSubtypeInterpreter extends PropertyBasedSubtypeInterpreter {
	public MovingStorageSubtypeInterpreter() {
		addOptionalProperty(MovingStorageItem::getStorageItemType, "storageItemType", Item::toString);
		addOptionalProperty(MovingStorageItem::getStorageItemWoodType, "woodName", WoodType::name);
		addOptionalProperty(MovingStorageItem::getStorageItemMainColor, "mainColor", String::valueOf);
		addOptionalProperty(MovingStorageItem::getStorageItemAccentColor, "accentColor", String::valueOf);
		addProperty(MovingStorageItem::isStorageItemFlatTopBarrel, "flatTop", String::valueOf);
	}
}
