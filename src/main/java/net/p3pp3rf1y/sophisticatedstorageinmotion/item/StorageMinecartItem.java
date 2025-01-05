package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import org.jetbrains.annotations.Nullable;

public class StorageMinecartItem extends MovingStorageItem {
	public static final DispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
		private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

		public ItemStack execute(BlockSource blockSource, ItemStack stack) {
			Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
			ServerLevel serverlevel = blockSource.level();
			BlockPos blockpos = blockSource.pos().relative(direction);
			BlockState blockstate = serverlevel.getBlockState(blockpos);
			RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock baseRailBlock ? baseRailBlock.sophisticated_getRailDirection(blockstate, serverlevel, blockpos, null) : RailShape.NORTH_SOUTH;
			double slopeOffset;
			if (blockstate.is(BlockTags.RAILS)) {
				if (railshape.isAscending()) {
					slopeOffset = 0.6;
				} else {
					slopeOffset = 0.1;
				}
			} else {
				if (!blockstate.isAir() || !serverlevel.getBlockState(blockpos.below()).is(BlockTags.RAILS)) {
					return this.defaultDispenseItemBehavior.dispense(blockSource, stack);
				}

				BlockState stateBelow = serverlevel.getBlockState(blockpos.below());
				RailShape railShapeBelow = stateBelow.getBlock() instanceof BaseRailBlock baseRailBlock ? baseRailBlock.sophisticated_getRailDirection(stateBelow, serverlevel, blockpos.below(), null) : RailShape.NORTH_SOUTH;
				if (direction != Direction.DOWN && railShapeBelow.isAscending()) {
					slopeOffset = -0.4;
				} else {
					slopeOffset = -0.9;
				}
			}

			serverlevel.addFreshEntity(createMinecart(serverlevel, blockpos, slopeOffset, stack, null));
			stack.shrink(1);
			return stack;
		}

		protected void playSound(BlockSource blockSource) {
			blockSource.level().levelEvent(1000, blockSource.pos(), 0);
		}
	};

	public StorageMinecartItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = level.getBlockState(blockpos);
		if (!blockstate.is(BlockTags.RAILS)) {
			return InteractionResult.FAIL;
		} else {
			ItemStack stack = context.getItemInHand();
			if (level instanceof ServerLevel serverlevel) {
				RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock baseRailBlock ? baseRailBlock.sophisticated_getRailDirection(blockstate, level, blockpos, null) : RailShape.NORTH_SOUTH;
				double ascendingOffset = 0.0;
				if (railshape.isAscending()) {
					ascendingOffset = 0.5;
				}

				Player player = context.getPlayer();

				serverlevel.addFreshEntity(createMinecart(serverlevel, blockpos, ascendingOffset, stack, player));
				serverlevel.gameEvent(GameEvent.ENTITY_PLACE, blockpos, GameEvent.Context.of(player, serverlevel.getBlockState(blockpos.below())));
			}

			stack.shrink(1);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	private static StorageMinecart createMinecart(ServerLevel serverlevel, BlockPos blockpos, double ascendingOffset, ItemStack stack, @Nullable Player player) {
		StorageMinecart minecart = new StorageMinecart(serverlevel, blockpos.getX() + 0.5, blockpos.getY() + 0.0625 + ascendingOffset, blockpos.getZ() + 0.5);
		minecart.getStorageHolder().setStorageItemFrom(stack, true);
		EntityType.createDefaultStackConfig(serverlevel, stack, player).accept(minecart);
		return minecart;
	}

	@Override
	public ItemStack getUncraftRemainingItem() {
		return new ItemStack(Items.MINECART);
	}
}
