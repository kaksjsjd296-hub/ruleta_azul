package com.ruletaazul.events;

import com.ruletaazul.ModSounds;
import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/** Lluvia de meteoritos: bolas de fuego explosivas caen del cielo. */
public class Meteors implements EventLogic {
	private final Random rnd = new Random();

	@Override public void start(MinecraftServer s) {}
	@Override public void stop(MinecraftServer s) {}

	@Override
	public void tick(MinecraftServer s, int age) {
		if (age % 10 != 0) return;
		for (ServerPlayerEntity p : EventUtil.players(s)) {
			if (rnd.nextInt(2) == 0) continue;
			ServerWorld w = p.getServerWorld();
			double a = rnd.nextDouble() * Math.PI * 2;
			double d = 3 + rnd.nextDouble() * 40;
			double x = p.getX() + Math.cos(a) * d;
			double z = p.getZ() + Math.sin(a) * d;
			double y = p.getY() + 65;
			FireballEntity f = new FireballEntity(EntityType.FIREBALL, w);
			f.setPosition(x, y, z);
			f.setVelocity((rnd.nextDouble() - 0.5) * 0.8, -1.6, (rnd.nextDouble() - 0.5) * 0.8);
			NbtCompound n = new NbtCompound();
			f.writeNbt(n);
			n.putByte("ExplosionPower", (byte) 3);
			f.readNbt(n);
			w.spawnEntity(f);
			if (rnd.nextInt(3) == 0) EventUtil.sound(w, x, y, z, ModSounds.METEORO, 6.0f, 0.9f + rnd.nextFloat() * 0.3f);
		}
	}
}
