package com.ruletaazul.events;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.Heightmap;

public final class EventUtil {
	private EventUtil() {}

	public static List<ServerPlayerEntity> players(MinecraftServer s) {
		List<ServerPlayerEntity> l = new ArrayList<>();
		for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
			if (!p.isSpectator()) l.add(p);
		}
		return l;
	}

	public static boolean loaded(ServerWorld w, int x, int z) {
		return w.getChunkManager().isChunkLoaded(x >> 4, z >> 4);
	}

	/** Primer bloque libre sobre el suelo (ignora hojas). */
	public static int surfaceY(ServerWorld w, int x, int z) {
		return w.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
	}

	/** Primer bloque libre sobre el bloque mas alto (incluye hojas). */
	public static int canopyY(ServerWorld w, int x, int z) {
		return w.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z);
	}

	public static void sound(ServerWorld w, double x, double y, double z, SoundEvent s, float vol, float pitch) {
		w.playSound(null, x, y, z, s, SoundCategory.AMBIENT, vol, pitch);
	}
}
