package com.ruletaazul.events;

import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;

/** Horda zombi: oleadas de zombis rapidos alrededor de cada jugador. */
public class ZombieHorde implements EventLogic {
	private static final String TAG = "ruletaazul_horde";
	private final Random rnd = new Random();

	@Override public void start(MinecraftServer s) {}

	@Override
	public void tick(MinecraftServer s, int age) {
		if (age % 80 != 0) return;
		for (ServerPlayerEntity p : EventUtil.players(s)) {
			ServerWorld w = p.getServerWorld();
			Box box = p.getBoundingBox().expand(40);
			List<ZombieEntity> near = w.getEntitiesByClass(ZombieEntity.class, box, z -> z.getCommandTags().contains(TAG));
			if (near.size() >= 14) continue;
			for (int i = 0; i < 4; i++) {
				double a = rnd.nextDouble() * Math.PI * 2;
				double d = 14 + rnd.nextDouble() * 8;
				double x = p.getX() + Math.cos(a) * d;
				double z = p.getZ() + Math.sin(a) * d;
				int bx = MathHelper.floor(x), bz = MathHelper.floor(z);
				if (!EventUtil.loaded(w, bx, bz)) continue;
				double y = EventUtil.surfaceY(w, bx, bz);
				ZombieEntity zombie = EntityType.ZOMBIE.create(w);
				if (zombie == null) continue;
				zombie.refreshPositionAndAngles(x, y, z, rnd.nextFloat() * 360f, 0f);
				zombie.initialize(w, w.getLocalDifficulty(zombie.getBlockPos()), SpawnReason.EVENT, null);
				zombie.addCommandTag(TAG);
				zombie.setPersistent();
				zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, StatusEffectInstance.INFINITE, 1, false, false));
				zombie.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, StatusEffectInstance.INFINITE, 0, false, false));
				zombie.setTarget(p);
				w.spawnEntity(zombie);
			}
		}
	}

	@Override
	public void stop(MinecraftServer s) {
		for (ServerWorld w : s.getWorlds()) {
			for (ZombieEntity z : w.getEntitiesByType(EntityType.ZOMBIE, e -> e.getCommandTags().contains(TAG))) {
				z.discard();
			}
		}
	}
}
