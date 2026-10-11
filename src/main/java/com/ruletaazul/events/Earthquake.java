package com.ruletaazul.events;

import com.ruletaazul.ModSounds;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/** Terremoto nivel 10: grietas gigantes que parten el suelo (la camara tiembla en el cliente). */
public class Earthquake implements EventLogic {
	private static class Crack {
		ServerWorld w;
		double x, z, dir;
		int left, width, depth;
	}

	private final List<Crack> cracks = new ArrayList<>();
	private final Random rnd = new Random();

	@Override public void start(MinecraftServer s) { cracks.clear(); }
	@Override public void stop(MinecraftServer s) { cracks.clear(); }

	@Override
	public void tick(MinecraftServer s, int age) {
		List<ServerPlayerEntity> ps = EventUtil.players(s);
		if (ps.isEmpty()) return;

		if (age % 40 == 0 && cracks.size() < 8) {
			ServerPlayerEntity p = ps.get(rnd.nextInt(ps.size()));
			Crack c = new Crack();
			c.w = p.getServerWorld();
			double a = rnd.nextDouble() * Math.PI * 2;
			double d = 6 + rnd.nextDouble() * 24;
			c.x = p.getX() + Math.cos(a) * d;
			c.z = p.getZ() + Math.sin(a) * d;
			c.dir = rnd.nextDouble() * Math.PI * 2;
			c.left = 70 + rnd.nextInt(60);
			c.width = 2 + rnd.nextInt(3);
			c.depth = 18 + rnd.nextInt(14);
			cracks.add(c);
		}

		Iterator<Crack> it = cracks.iterator();
		while (it.hasNext()) {
			Crack c = it.next();
			step(c);
			step(c);
			if (c.left <= 0) it.remove();
		}

		if (age % 30 == 0) {
			for (ServerPlayerEntity p : ps) {
				EventUtil.sound(p.getServerWorld(), p.getX(), p.getY(), p.getZ(), ModSounds.TEMBLOR, 3.0f, 0.9f + rnd.nextFloat() * 0.2f);
			}
		}
	}

	private void step(Crack c) {
		c.dir += (rnd.nextDouble() - 0.5) * 0.35;
		c.x += Math.cos(c.dir);
		c.z += Math.sin(c.dir);
		c.left--;
		double r = c.width * 0.5 + 0.5 + rnd.nextDouble() * 0.6;
		int ri = (int) Math.ceil(r);
		int cx = MathHelper.floor(c.x), cz = MathHelper.floor(c.z);
		BlockPos.Mutable m = new BlockPos.Mutable();
		int minY = c.w.getBottomY() + 3;
		for (int dx = -ri; dx <= ri; dx++) {
			for (int dz = -ri; dz <= ri; dz++) {
				if (dx * dx + dz * dz > r * r) continue;
				int bx = cx + dx, bz = cz + dz;
				if (!EventUtil.loaded(c.w, bx, bz)) continue;
				int top = EventUtil.surfaceY(c.w, bx, bz);
				m.set(bx, top - 1, bz);
				if (!c.w.getFluidState(m).isEmpty()) continue; // no vaciar oceanos
				int bottom = Math.max(minY, top - c.depth);
				for (int y = top - 1; y >= bottom; y--) {
					m.set(bx, y, bz);
					BlockState st = c.w.getBlockState(m);
					if (st.isAir() || st.isOf(Blocks.BEDROCK)) continue;
					boolean lava = y <= bottom + 1 && rnd.nextInt(3) == 0;
					c.w.setBlockState(m, lava ? Blocks.LAVA.getDefaultState() : Blocks.AIR.getDefaultState(), 2);
				}
			}
		}
	}
}
