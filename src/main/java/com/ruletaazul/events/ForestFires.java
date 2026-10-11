package com.ruletaazul.events;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

/** Los bosques se queman lentamente; al desactivar el evento se apaga todo el fuego cercano. */
public class ForestFires implements EventLogic {
	private final Random rnd = new Random();

	@Override public void start(MinecraftServer s) {}

	@Override
	public void tick(MinecraftServer s, int age) {
		if (age % 20 != 0) return;
		for (ServerPlayerEntity p : EventUtil.players(s)) {
			ServerWorld w = p.getServerWorld();
			for (int i = 0; i < 10; i++) {
				double a = rnd.nextDouble() * Math.PI * 2;
				double d = 8 + rnd.nextDouble() * 45;
				int x = MathHelper.floor(p.getX() + Math.cos(a) * d);
				int z = MathHelper.floor(p.getZ() + Math.sin(a) * d);
				if (!EventUtil.loaded(w, x, z)) continue;
				int y = EventUtil.canopyY(w, x, z);
				BlockState below = w.getBlockState(new BlockPos(x, y - 1, z));
				if (below.isIn(BlockTags.LEAVES) || below.isIn(BlockTags.LOGS_THAT_BURN)) {
					BlockPos fp = new BlockPos(x, y, z);
					if (w.isAir(fp)) {
						w.setBlockState(fp, Blocks.FIRE.getDefaultState());
						break;
					}
				}
			}
		}
	}

	@Override
	public void stop(MinecraftServer s) {
		for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
			ServerWorld w = p.getServerWorld();
			int pcx = p.getBlockX() >> 4, pcz = p.getBlockZ() >> 4;
			for (int cx = pcx - 12; cx <= pcx + 12; cx++) {
				for (int cz = pcz - 12; cz <= pcz + 12; cz++) {
					WorldChunk ch = w.getChunkManager().getWorldChunk(cx, cz);
					if (ch == null) continue;
					ChunkSection[] secs = ch.getSectionArray();
					for (int i = 0; i < secs.length; i++) {
						ChunkSection sec = secs[i];
						if (sec == null || sec.isEmpty() || !sec.hasAny(st -> st.isOf(Blocks.FIRE))) continue;
						int baseY = ch.sectionIndexToCoord(i) << 4;
						for (int x = 0; x < 16; x++) {
							for (int y = 0; y < 16; y++) {
								for (int z = 0; z < 16; z++) {
									if (sec.getBlockState(x, y, z).isOf(Blocks.FIRE)) {
										w.setBlockState(new BlockPos((cx << 4) + x, baseY + y, (cz << 4) + z), Blocks.AIR.getDefaultState(), 3);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
