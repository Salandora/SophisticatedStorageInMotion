package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import com.mojang.serialization.Codec;
import io.github.fabricators_of_create.porting_lib.util.DeferredRegister;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.vehicle.Boat;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedcore.util.StreamCodecHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

import java.util.function.Supplier;

public class ModDataComponents {
	private ModDataComponents() {
	}

	private static final StreamCodec<FriendlyByteBuf, Boat.Type> BOAT_TYPE_STREAM_CODEC = StreamCodecHelper.enumCodec(Boat.Type.class);

	private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, SophisticatedStorageInMotion.MOD_ID);

	public static final Supplier<DataComponentType<SimpleItemContent>> STORAGE_ITEM = DATA_COMPONENT_TYPES.register("storage_item",
			() -> new DataComponentType.Builder<SimpleItemContent>().persistent(SimpleItemContent.CODEC).networkSynchronized(SimpleItemContent.STREAM_CODEC).build());

	public static final Supplier<DataComponentType<Boolean>> LOCKED = DATA_COMPONENT_TYPES.register("locked",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> LOCK_VISIBLE = DATA_COMPONENT_TYPES.register("lock_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> UPGRADES_VISIBLE = DATA_COMPONENT_TYPES.register("upgrades_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> COUNTS_VISIBLE = DATA_COMPONENT_TYPES.register("counts_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> FILL_LEVELS_VISIBLE = DATA_COMPONENT_TYPES.register("fill_levels_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boat.Type>> BOAT_TYPE = DATA_COMPONENT_TYPES.register("boat_type",
			() -> new DataComponentType.Builder<Boat.Type>().persistent(Boat.Type.CODEC).networkSynchronized(BOAT_TYPE_STREAM_CODEC).build());

	public static void register() {
		DATA_COMPONENT_TYPES.register();
	}
}
