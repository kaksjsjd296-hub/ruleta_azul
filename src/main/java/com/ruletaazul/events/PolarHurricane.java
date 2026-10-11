package com.ruletaazul.events;

import com.ruletaazul.HurricaneEntity;
import com.ruletaazul.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;

public class PolarHurricane implements EventLogic {
	private final List<HurricaneEntity> list = new ArrayList<>();
	private final Random rnd = new Random();

	@Override public void start(MinecraftServer s) { list.clear(); }

	@Override
	public void tick(MinecraftServer s, int age) {
		if (age % 20 != 0) return;
		list.removeIf(HurricaneEntity::isRemoved);
		if (!list.isEmpty()) return;
		List<ServerPlayerEntity> ps = EventUtil.players(s);
		if (ps.isEmpty()) return;
		ServerPlayerEntity p = ps.get(rnd.nextInt(ps.size()));
		ServerWorld w = p.getServerWorld();
		double a = rnd.nextDouble() * Math.PI * 2;
		double d = 45 + rnd.nextDouble() * 20;
		double x = p.getX() + Math.cos(a) * d;
		double z = p.getZ() + Math.sin(a) * d;
		int bx = MathHelper.floor(x), bz = MathHelper.floor(z);
		if (!EventUtil.loaded(w, bx, bz)) return;
		HurricaneEntity e = new HurricaneEntity(ModEntities.HURRICANE, w);
		e.setPosition(x, EventUtil.surfaceY(w, bx, bz), z);
		w.spawnEntity(e);
		list.add(e);
	}

	@Override
	public void stop(MinecraftServer s) {
		for (HurricaneEntity e : list) e.discard();
		list.clear();
	}
}
