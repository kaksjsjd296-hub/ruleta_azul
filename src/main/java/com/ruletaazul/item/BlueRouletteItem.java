package com.ruletaazul.item;

import com.ruletaazul.net.Payloads;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class BlueRouletteItem extends Item {
	private final int which;

	/** which = 0 -> menu de eventos, 1 -> menu del temporizador */
	public BlueRouletteItem(Settings settings, int which) {
		super(settings);
		this.which = which;
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if (!world.isClient && user instanceof ServerPlayerEntity sp) {
			ServerPlayNetworking.send(sp, new Payloads.OpenMenu(which));
		}
		return TypedActionResult.success(stack, world.isClient);
	}
}
