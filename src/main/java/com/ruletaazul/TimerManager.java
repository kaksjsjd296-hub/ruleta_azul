package com.ruletaazul;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

/** Temporizador con boss bar. Es independiente de los eventos. */
public final class TimerManager {
	private static ServerBossBar bar;
	private static int remaining;
	private static int total;
	private static String title = "Tiempo";

	private TimerManager() {}

	public static void start(MinecraftServer server, int seconds, int colorIdx, String name) {
		stop();
		if (seconds <= 0) return;
		BossBar.Color[] colors = BossBar.Color.values();
		BossBar.Color color = colors[Math.floorMod(colorIdx, colors.length)];
		title = (name == null || name.isBlank()) ? "Tiempo" : name.trim();
		total = seconds * 20;
		remaining = total;
		bar = new ServerBossBar(Text.empty(), color, BossBar.Style.PROGRESS);
		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) bar.addPlayer(p);
		updateName();
	}

	public static void stop() {
		if (bar != null) {
			bar.clearPlayers();
			bar = null;
		}
	}

	public static void tick(MinecraftServer server) {
		if (bar == null) return;
		remaining--;
		bar.setPercent(Math.max(0f, Math.min(1f, remaining / (float) total)));
		if (remaining % 20 == 0) updateName();
		if (remaining <= 0) {
			for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
				p.playSoundToPlayer(ModSounds.DING, SoundCategory.MASTER, 1.0f, 1.0f);
			}
			stop();
		}
	}

	public static void addPlayer(ServerPlayerEntity p) { if (bar != null) bar.addPlayer(p); }
	public static void removePlayer(ServerPlayerEntity p) { if (bar != null) bar.removePlayer(p); }

	private static void updateName() {
		if (bar == null) return;
		int secs = Math.max(0, (remaining + 19) / 20);
		bar.setName(Text.literal(title + " - " + fmt(secs)));
	}

	public static String fmt(int secs) {
		if (secs >= 3600) return String.format("%d:%02d:%02d", secs / 3600, (secs / 60) % 60, secs % 60);
		return String.format("%02d:%02d", secs / 60, secs % 60);
	}
}
