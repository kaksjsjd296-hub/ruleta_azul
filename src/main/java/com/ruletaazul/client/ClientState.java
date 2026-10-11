package com.ruletaazul.client;

import java.util.Arrays;

public final class ClientState {
	public static final int COUNT = 8;
	public static final int[] remaining = new int[COUNT];
	static { Arrays.fill(remaining, -2); }

	private ClientState() {}

	public static boolean active(int id) { return remaining[id] != -2; }

	public static void set(int[] r) {
		for (int i = 0; i < COUNT && i < r.length; i++) remaining[i] = r[i];
	}

	public static void reset() {
		Arrays.fill(remaining, -2);
		ScreenFx.clear();
		Hud.reset();
	}

	/** Intensidad (en grados) del temblor de camara segun los eventos activos. */
	public static float shakeAmount() {
		float s = 0f;
		double t = System.nanoTime() / 1.0e9;
		if (active(0)) s = Math.max(s, (float) (1.5 * (0.65 + 0.35 * Math.sin(t * 1.3))));
		if (active(4)) s = Math.max(s, 0.45f);
		if (active(1)) s = Math.max(s, 0.3f);
		return s;
	}
}
