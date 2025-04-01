package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StorageBoatItem extends MovingStorageItem {
	private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
	private static final String RAFT_DESCRIPTION_ID = "item." + SophisticatedStorageInMotion.MOD_ID + ".storage_raft";
	public static final Map<Boat.Type, Supplier<Item>> SUPPORTED_BOAT_TYPES = Map.of(
			Boat.Type.ACACIA, () -> Items.ACACIA_BOAT,
			Boat.Type.BAMBOO, () -> Items.BAMBOO_RAFT,
			Boat.Type.BIRCH, () -> Items.BIRCH_BOAT,
			Boat.Type.CHERRY, () -> Items.CHERRY_BOAT,
			Boat.Type.DARK_OAK, () -> Items.DARK_OAK_BOAT,
			Boat.Type.JUNGLE, () -> Items.JUNGLE_BOAT,
			Boat.Type.MANGROVE, () -> Items.MANGROVE_BOAT,
			Boat.Type.OAK, () -> Items.OAK_BOAT,
			Boat.Type.SPRUCE, () -> Items.SPRUCE_BOAT
	);

	public static final DefaultDispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
		private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

		public ItemStack execute(BlockSource source, ItemStack stack) {
			Direction direction = source.state().getValue(DispenserBlock.FACING);
			Level level = source.level();
			double halfWidth = 0.5625D + EntityType.BOAT.getWidth() / 2D;
			Vec3 center = source.center();
			double x = center.x() + direction.getStepX() * halfWidth;
			double y = center.y() + direction.getStepY() * 1.125D;
			double z = center.z() + direction.getStepZ() * halfWidth;
			BlockPos blockpos = source.pos().relative(direction);
			Boat boat = createBoat(level, null, stack, x, y, z);
			boat.setYRot(direction.toYRot());
			double yOffset;
			if (boat.canBoatInFluid(level.getFluidState(blockpos))) {
				yOffset = 1;
			} else {
				if (!level.getBlockState(blockpos).isAir() || !boat.canBoatInFluid(level.getFluidState(blockpos.below()))) {
					return this.defaultDispenseItemBehavior.dispense(source, stack);
				}

				yOffset = 0;
			}

			boat.setPos(x, y + yOffset, z);
			level.addFreshEntity(boat);
			stack.shrink(1);
			return stack;
		}

		protected void playSound(BlockSource source) {
			source.level().levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, source.pos(), 0);
		}
	};

	public StorageBoatItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public ItemStack getUncraftRemainingItem(ItemStack input) {
		return new ItemStack(SUPPORTED_BOAT_TYPES.getOrDefault(getBoatType(input), () -> Items.OAK_BOAT).get());
	}

	@Override
	public List<ItemStack> getBaseMovingStorageItems() {
		return SUPPORTED_BOAT_TYPES.keySet().stream().map(type -> setBoatType(new ItemStack(this), type)).toList();
	}

	public static Boat.Type getBoatType(ItemStack boatStack) {
		return boatStack.getOrDefault(ModDataComponents.BOAT_TYPE.get(), Boat.Type.OAK);
	}

	public static ItemStack setBoatType(ItemStack boatStack, Boat.Type type) {
		boatStack.set(ModDataComponents.BOAT_TYPE.get(), type);
		return boatStack;
	}

	@Override
	public Component getName(ItemStack stack) {
		SimpleItemContent storageItemContent = stack.get(ModDataComponents.STORAGE_ITEM);
		Boat.Type boatType = getBoatType(stack);
		String descriptionId = boatType.isRaft() ? RAFT_DESCRIPTION_ID : getDescriptionId();
		return Component.translatable(descriptionId, getWoodName(boatType), storageItemContent != null ? storageItemContent.copy().getHoverName() : "");
	}

	private Component getWoodName(Boat.Type type) {
		return Component.translatable("wood_name." + SophisticatedStorage.MOD_ID + "." + type.name().toLowerCase(Locale.ROOT));
	}

	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
		if (hitresult.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(itemstack);
		} else {
			Vec3 playerViewVector = player.getViewVector(1.0F);
			List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(playerViewVector.scale(5)).inflate(1.0), ENTITY_PREDICATE);
			if (!list.isEmpty()) {
				Vec3 eyePosition = player.getEyePosition();
				for (Entity entity : list) {
					AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
					if (aabb.contains(eyePosition)) {
						return InteractionResultHolder.pass(itemstack);
					}
				}
			}

			if (hitresult.getType() == HitResult.Type.BLOCK) {
				Vec3 location = hitresult.getLocation();
				Boat boat = createBoat(level, player, itemstack, location.x, location.y, location.z);
				if (!level.noCollision(boat, boat.getBoundingBox())) {
					return InteractionResultHolder.fail(itemstack);
				} else {
					if (!level.isClientSide) {
						level.addFreshEntity(boat);
						level.gameEvent(player, GameEvent.ENTITY_PLACE, hitresult.getLocation());
						itemstack.consume(1, player);
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
				}
			} else {
				return InteractionResultHolder.pass(itemstack);
			}
		}
	}

	private static Boat createBoat(Level level, @Nullable Player player, ItemStack stack, double x, double y, double z) {
		StorageBoat boat = new StorageBoat(level, x, y, z);
		EntityStorageHolder<?> storageHolder = boat.getStorageHolder();
		storageHolder.setStorageItemFrom(stack, true);
		storageHolder.onPlace();
		boat.setVariant(StorageBoatItem.getBoatType(stack));
		if (player != null) {
			boat.setYRot(player.getYRot());
		}
		if (level instanceof ServerLevel serverLevel) {
			EntityType.createDefaultStackConfig(serverLevel, stack, player).accept(boat);
		}
		return boat;
	}
}
