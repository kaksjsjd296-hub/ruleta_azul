package com.ruletaazul.events;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

/** Cada 10 s caen 5 truenos sobre cada jugador y hay rayos por todo el mundo. */
public class ElectricRain implements EventLogic {
	private final Random rnd = new Random();

	@Override
	public void start(MinecraftServer s) {
		for (ServerWorld w : s.getWorlds()) w.setWeather(0, 24000, true, true);
	}

	@Override
	public void tick(MinecraftServer s, int age) {
		List<ServerPlayerEntity> ps = EventUtil.players(s);
		if (age % 1200 == 0 && age > 0) {
			for (ServerWorld w : s.getWorlds()) w.setWeather(0, 24000, true, true);
		}
		int phase = age % 200;
		if (phase < 20 && phase % 4 == 0) {
			for (ServerPlayerEntity p : ps) {
				strike(p.getServerWorld(), p.getX() + (rnd.nextDouble() - 0.5) * 2, p.getY(), p.getZ() + (rnd.nextDouble() - 0.5) * 2);
			}
		}
		if (age % 5 == 0 && !ps.isEmpty()) {
			ServerPlayerEntity p = ps.get(rnd.nextInt(ps.size()));
			ServerWorld w = p.getServerWorld();
			double a = rnd.nextDouble() * Math.PI * 2;
			double d = 8 + rnd.nextDouble() * 70;
			double x = p.getX() + Math.cos(a) * d, z = p.getZ() + Math.sin(a) * d;
			int bx = MathHelper.floor(x), bz = MathHelper.floor(z);
			if (EventUtil.loaded(w, bx, bz)) strike(w, x, EventUtil.surfaceY(w, bx, bz), z);
		}
	}

	private void strike(ServerWorld w, double x, double y, double z) {
		LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w);
		if (bolt == null) return;
		bolt.setPosition(x, y, z);
		w.spawnEntity(bolt);
	}

	@Override
	public void stop(MinecraftServer s) {
		for (ServerWorld w : s.getWorlds()) w.setWeather(6000, 0, false, false);
	}
}
