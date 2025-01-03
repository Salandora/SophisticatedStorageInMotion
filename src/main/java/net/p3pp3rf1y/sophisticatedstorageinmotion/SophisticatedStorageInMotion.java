package net.p3pp3rf1y.sophisticatedstorageinmotion;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModPayloads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SophisticatedStorageInMotion implements ModInitializer {
	public static final String MOD_ID = "sophisticatedstorageinmotion";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.registerHandlers();
		ModEntities.registerHandlers();
		ModDataComponents.register();
		CommonEventHandler.registerHandlers();
		ModPayloads.registerPayloads();

		SophisticatedStorageInMotion.setup();
	}

	public static ResourceLocation getRL(String regName) {
		return ResourceLocation.parse(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}

	private static void setup() {
		ModItems.registerDispenseBehavior();
	}
}
