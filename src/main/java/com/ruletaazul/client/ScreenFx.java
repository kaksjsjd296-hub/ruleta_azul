package com.ruletaazul.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Efectos en la pantalla (tierra, gotas, nieve, brasas...) segun los eventos activos. */
public final class ScreenFx {
	private static class P {
		float x, y, vx, vy, ay;
		int w, h, rgb, life, max;
		float alpha;
	}

	private static final List<P> list = new ArrayList<>();
	private static final Random R = new Random();
	private static float flash = 0f;

	private ScreenFx() {}

	public static void clear() { list.clear(); flash = 0f; }

	private static void add(float x, float y, float vx, float vy, float ay, int w, int h, int rgb, float alpha, int max) {
		if (list.size() > 600) return;
		P p = new P();
		p.x = x; p.y = y; p.vx = vx; p.vy = vy; p.ay = ay;
		p.w = w; p.h = h; p.rgb = rgb; p.alpha = alpha; p.max = max;
		list.add(p);
	}

	private static float rf(float a, float b) { return a + R.nextFloat() * (b - a); }

	public static void tick(MinecraftClient mc) {
		if (mc.player == null) return;
		if (mc.isPaused()) return;
		int w = mc.getWindow().getScaledWidth();
		int h = mc.getWindow().getScaledHeight();

		Iterator<P> it = list.iterator();
		while (it.hasNext()) {
			P p = it.next();
			p.x += p.vx; p.y += p.vy; p.vy += p.ay; p.life++;
			if (p.life >= p.max || p.y > h + 20 || p.y < -30 || p.x > w + 40 || p.x < -40) it.remove();
		}
		flash = Math.max(0f, flash - 0.12f);

		int[] dirt = {0x6B4A2B, 0x8B6B43, 0x4B3A2A, 0x7A7468};
		if (ClientState.active(0) && R.nextFloat() < 0.7f) {
			int s = 2 + R.nextInt(4);
			add(rf(0, w), -4, rf(-0.3f, 0.3f), rf(0.5f, 1.5f), 0.12f, s, s, dirt[R.nextInt(dirt.length)], 0.9f, 90);
		}
		if (ClientState.active(1)) {
			for (int i = 0; i < 2; i++) {
				add(-20, rf(0, h), rf(9, 15), rf(-1f, 1f), 0f, 14 + R.nextInt(14), 1 + R.nextInt(2), R.nextBoolean() ? 0xFFFFFF : 0xD0E8FF, 0.7f, 40);
			}
		}
		if (ClientState.active(2)) {
			for (int i = 0; i < 3; i++) {
				add(rf(0, w), -12, -1f, rf(8, 12), 0f, 1, 8 + R.nextInt(6), 0x9FD0FF, 0.6f, 40);
			}
			if (R.nextInt(3) == 0) {
				add(rf(0, w), rf(0, h), 0f, 0.15f, 0.002f, 3 + R.nextInt(3), 4 + R.nextInt(3), 0x8FBFFF, 0.55f, 70);
			}
			if (R.nextInt(100) == 0) flash = 0.45f;
		}
		if (ClientState.active(3)) {
			if (R.nextFloat() < 0.8f) add(rf(0, w), h + 4, rf(-0.5f, 0.5f), rf(-3f, -1.5f), 0f, 2 + R.nextInt(2), 2 + R.nextInt(2), R.nextBoolean() ? 0xFF8A00 : 0xFFD23C, 0.9f, 60);
			if (R.nextFloat() < 0.6f) add(rf(0, w), -4, rf(-0.4f, 0.4f), rf(0.5f, 1.2f), 0f, 2, 2, 0x555555, 0.6f, 90);
		}
		if (ClientState.active(4)) {
			if (R.nextFloat() < 0.7f) add(rf(0, w), -4, rf(-1f, -3f), rf(3f, 5f), 0f, 2, 2 + R.nextInt(2), 0xFF8800, 0.9f, 50);
			if (R.nextFloat() < 0.6f) add(rf(0, w), -4, rf(-0.3f, 0.3f), rf(0.6f, 1.4f), 0f, 3, 3, 0x444444, 0.6f, 90);
		}
		if (ClientState.active(5) && R.nextFloat() < 0.5f) {
			int s = 6 + R.nextInt(9);
			add(rf(0, w), rf(0, h), rf(-0.4f, 0.4f), rf(-0.4f, 0.1f), 0f, s, s, 0x6FAF3A, 0.25f, 100);
		}
		if (ClientState.active(6) && R.nextFloat() < 0.15f) {
			int s = 6 + R.nextInt(5);
			add(rf(0, w), rf(0, h * 0.7f), 0f, 0.1f, 0.002f, s, s + 2, 0x7A0A0A, 0.8f, 120);
		}
		if (ClientState.active(7)) {
			for (int i = 0; i < 2; i++) {
				if (R.nextFloat() < 0.75f) add(rf(0, w), -4, rf(-1f, 1f), rf(1f, 2f), 0f, 2 + R.nextInt(2), 2 + R.nextInt(2), 0xFFFFFF, 0.8f, 90);
			}
		}
	}

	public static void render(DrawContext ctx, int w, int h) {
		float t = (float) (System.nanoTime() / 1.0e9);
		if (ClientState.active(5)) {
			int a = (int) (36 + 14 * Math.sin(t * 1.5));
			ctx.fill(0, 0, w, h, (a << 24) | 0x4CAF2A);
		}
		if (ClientState.active(3)) {
			ctx.fillGradient(0, h / 2, w, h, 0x00FF6A00, 0x44FF4400);
		}
		if (ClientState.active(7)) {
			ctx.fillGradient(0, 0, w, h / 3, 0x66CFEFFF, 0x00CFEFFF);
			ctx.fillGradient(0, h * 2 / 3, w, h, 0x00CFEFFF, 0x66CFEFFF);
		}
		if (flash > 0f) {
			ctx.fill(0, 0, w, h, ((int) (flash * 255) << 24) | 0xFFFFFF);
		}
		for (P p : list) {
			float fadeIn = Math.min(1f, p.life / 5f);
			float fadeOut = Math.min(1f, (p.max - p.life) / 20f);
			int a = (int) (255 * p.alpha * fadeIn * fadeOut);
			if (a <= 3) continue;
			int x = (int) p.x, y = (int) p.y;
			ctx.fill(x, y, x + p.w, y + p.h, (a << 24) | p.rgb);
			if (p.rgb == 0x8FBFFF && p.w > 2) ctx.fill(x + 1, y + 1, x + 2, y + 2, ((a) << 24) | 0xFFFFFF); // brillo de la gota
		}
	}
}
