package com.ruletaazul.events;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/** Niebla mortal: oscuridad + wither sobre todos los jugadores. */
public class ToxicFog implements EventLogic {
	@Override public void start(MinecraftServer s) {}

	@Override
	public void tick(MinecraftServer s, int age) {
		if (age % 20 != 0) return;
		for (ServerPlayerEntity p : EventUtil.players(s)) {
			p.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0, false, false, true));
			p.addStatusEffect(new StatusEffectInstance(StatusEffects.DARKNESS, 120, 0, false, false, false));
		}
	}

	@Override
	public void stop(MinecraftServer s) {
		for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
			p.removeStatusEffect(StatusEffects.WITHER);
			p.removeStatusEffect(StatusEffects.DARKNESS);
		}
	}
}
