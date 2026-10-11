package com.ruletaazul.events;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/** Glaciacion: congela y ralentiza a los jugadores. */
public class DeepFreeze implements EventLogic {
	@Override public void start(MinecraftServer s) {}

	@Override
	public void tick(MinecraftServer s, int age) {
		for (ServerPlayerEntity p : EventUtil.players(s)) {
			p.setFrozenTicks(Math.min(p.getMinFreezeDamageTicks() + 20, p.getFrozenTicks() + 4));
			if (age % 20 == 0) {
				p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 0, false, false, true));
			}
			if (age % 60 == 0 && p.getFrozenTicks() >= p.getMinFreezeDamageTicks()) {
				p.damage(p.getDamageSources().freeze(), 1.0f);
			}
		}
	}

	@Override
	public void stop(MinecraftServer s) {
		for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
			p.removeStatusEffect(StatusEffects.SLOWNESS);
		}
	}
}
