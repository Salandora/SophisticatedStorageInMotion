package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.p3pp3rf1y.sophisticatedcore.util.Capabilities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;

public class StorageWrapperLookup {
	static {
		Capabilities.ItemHandler.ENTITY_AUTOMATION.registerForType((entity, direction) -> entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, direction).getValueUnsafer(), ModEntities.STORAGE_MINECART);
	}
}
