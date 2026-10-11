package com.ruletaazul;

import com.ruletaazul.events.*;
import com.ruletaazul.net.Payloads;
import java.util.Arrays;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/** Estado global de los 8 eventos (por servidor). */
public final class EventManager {
	public static final int COUNT = 8;
	public static final int INACTIVE = -2;
	public static final int HURRICANE = 1;

	private static final int[] remaining = new int[COUNT];
	private static final int[] total = new int[COUNT];
	private static final int[] ages = new int[COUNT];
	private static final ServerBossBar[] bars = new ServerBossBar[COUNT];

	private static final BossBar.Color[] COLORS = {
		BossBar.Color.YELLOW, BossBar.Color.WHITE, BossBar.Color.BLUE, BossBar.Color.RED,
		BossBar.Color.PINK, BossBar.Color.GREEN, BossBar.Color.PURPLE, BossBar.Color.BLUE
	};

	private static final EventLogic[] LOGIC = {
		new Earthquake(), new PolarHurricane(), new ElectricRain(), new ForestFires(),
		new Meteors(), new ToxicFog(), new ZombieHorde(), new DeepFreeze()
	};

	static { Arrays.fill(remaining, INACTIVE); }

	private EventManager() {}

	public static boolean isActive(int id) { return remaining[id] != INACTIVE; }

	public static void activate(MinecraftServer server, int id, int seconds) {
		boolean was = isActive(id);
		remaining[id] = seconds > 0 ? seconds * 20 : -1;
		total[id] = remaining[id];
		if (!was) {
			ages[id] = 0;
			LOGIC[id].start(server);
			for (ServerPlayerEntity p : PlayerLookup.all(server)) {
				ServerPlayNetworking.send(p, new Payloads.EventStart(id));
			}
		}
		if (seconds > 0) {
			if (bars[id] == null) {
				bars[id] = new ServerBossBar(Text.empty(), COLORS[id], BossBar.Style.PROGRESS);
				for (ServerPlayerEntity p : PlayerLookup.all(server)) bars[id].addPlayer(p);
			}
			updateBar(id);
		} else {
			removeBar(id);
		}
		sync(server);
	}

	public static void deactivate(MinecraftServer server, int id) {
		if (!isActive(id)) return;
		remaining[id] = INACTIVE;
		removeBar(id);
		LOGIC[id].stop(server);
		sync(server);
	}

	public static void tick(MinecraftServer server) {
		for (int id = 0; id < COUNT; id++) {
			if (!isActive(id)) continue;
			LOGIC[id].tick(server, ages[id]++);
			if (remaining[id] > 0) {
				remaining[id]--;
				if (bars[id] != null) {
					bars[id].setPercent(Math.max(0f, Math.min(1f, remaining[id] / (float) total[id])));
					if (remaining[id] % 20 == 0) updateBar(id);
				}
				if (remaining[id] == 0) deactivate(server, id);
			}
		}
		if (server.getTicks() % 20 == 0) sync(server);
	}

	public static void sync(MinecraftServer server) {
		for (ServerPlayerEntity p : PlayerLookup.all(server)) sync(p);
	}

	public static void sync(ServerPlayerEntity p) {
		ServerPlayNetworking.send(p, new Payloads.Sync(remaining.clone()));
	}

	public static void addPlayerToBars(ServerPlayerEntity p) {
		for (ServerBossBar b : bars) if (b != null) b.addPlayer(p);
	}

	public static void removePlayerFromBars(ServerPlayerEntity p) {
		for (ServerBossBar b : bars) if (b != null) b.removePlayer(p);
	}

	/** Reinicio duro (al cerrar un mundo) para que no sobrevivan datos entre mundos. */
	public static void reset() {
		for (int i = 0; i < COUNT; i++) {
			remaining[i] = INACTIVE;
			removeBar(i);
		}
	}

	private static void removeBar(int id) {
		if (bars[id] != null) {
			bars[id].clearPlayers();
			bars[id] = null;
		}
	}

	private static void updateBar(int id) {
		if (bars[id] == null) return;
		int secs = Math.max(0, (remaining[id] + 19) / 20);
		MutableText name = Text.translatable("event.ruletaazul." + (id + 1) + ".name");
		name.append(Text.literal(" - " + TimerManager.fmt(secs)));
		bars[id].setName(name);
	}
}
