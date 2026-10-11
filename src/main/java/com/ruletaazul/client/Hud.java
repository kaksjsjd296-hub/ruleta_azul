package com.ruletaazul.client;

import com.ruletaazul.ModSounds;
import com.ruletaazul.RuletaAzulMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public final class Hud {
	private static final Identifier MEGAFONO = Identifier.of(RuletaAzulMod.MOD_ID, "textures/gui/megafono.png");
	private static final Identifier[] ICONS = new Identifier[8];
	static {
		for (int i = 0; i < 8; i++) ICONS[i] = Identifier.of(RuletaAzulMod.MOD_ID, "textures/gui/event_" + (i + 1) + ".png");
	}

	private static final long MEGA_MS = 1700;
	private static final long FILTER_MS = 3500;
	private static long startMs = -1;

	private Hud() {}

	public static Identifier icon(int i) { return ICONS[i]; }

	public static void reset() { startMs = -1; }

	/** Se llama cuando el servidor avisa que un evento comienza. */
	public static void onEventStart(int id) {
		startMs = System.currentTimeMillis();
		MinecraftClient mc = MinecraftClient.getInstance();
		mc.getSoundManager().play(PositionedSoundInstance.master(ModSounds.MEGAFONO_ALERTA, 1.0f, 1.0f));
		mc.getSoundManager().play(PositionedSoundInstance.master(ModSounds.ALERTA_SISMICA, 1.0f, 0.9f));
	}

	public static void render(DrawContext ctx, RenderTickCounter counter) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden) return;
		int w = ctx.getScaledWindowWidth();
		int h = ctx.getScaledWindowHeight();

		ScreenFx.render(ctx, w, h);
		renderAlertFilter(ctx, w, h);
		renderIcons(ctx, w);
		renderMegaphone(ctx, w, h);
	}

	private static void renderAlertFilter(DrawContext ctx, int w, int h) {
		if (startMs < 0) return;
		long dt = System.currentTimeMillis() - startMs;
		if (dt > FILTER_MS) return;
		float f = 1f - dt / (float) FILTER_MS;
		float pulse = 0.65f + 0.35f * (float) Math.sin(dt / 160.0);
		int a = (int) (42 * f * pulse); // muy sutil
		if (a > 0) ctx.fill(0, 0, w, h, (a << 24) | 0xFF0000);
	}

	private static void renderIcons(DrawContext ctx, int w) {
		int slot = 0;
		for (int i = 0; i < 8; i++) {
			if (!ClientState.active(i)) continue;
			int size = 40;
			int x = w - 6 - (slot + 1) * (size + 3) + 3;
			int y = 6;
			// marco azul con esquinas redondeadas
			int blue = 0xFF1F4FFF;
			ctx.fill(x + 2, y, x + size - 2, y + size, blue);
			ctx.fill(x, y + 2, x + size, y + size - 2, blue);
			ctx.fill(x + 1, y + 1, x + size - 1, y + size - 1, blue);
			// interior blanco
			ctx.fill(x + 4, y + 3, x + size - 4, y + size - 3, 0xFFFFFFFF);
			ctx.fill(x + 3, y + 4, x + size - 3, y + size - 4, 0xFFFFFFFF);
			ctx.drawTexture(ICONS[i], x + 4, y + 4, 0, 0, 32, 32, 32, 32);
			slot++;
		}
	}

	private static void renderMegaphone(DrawContext ctx, int w, int h) {
		if (startMs < 0) return;
		long dt = System.currentTimeMillis() - startMs;
		if (dt > MEGA_MS) return;
		float t = dt / (float) MEGA_MS;
		float s;
		if (t < 0.25f) {                       // entra: pequeno -> grande (con rebote)
			float k = t / 0.25f;
			s = easeOutBack(k);
		} else if (t < 0.75f) {                // se mantiene con un leve pulso
			s = 1f + 0.05f * (float) Math.sin((t - 0.25f) * 40);
		} else {                               // sale igual que entro
			float k = (t - 0.75f) / 0.25f;
			s = 1f - easeInBack(k);
		}
		if (s < 0f) s = 0f;

		var m = ctx.getMatrices();
		m.push();
		m.translate(w / 2f, h * 0.27f, 0);
		m.scale(s, s, 1f);
		m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-12f));

		// lineas de sonido saliendo de la bocina (lado derecho)
		long now = System.currentTimeMillis();
		for (int k = 0; k < 3; k++) {
			float ang = (k - 1) * 38f;
			m.push();
			m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(ang));
			for (int j = 0; j < 3; j++) {
				float phase = ((now / 90f) + j * 1.2f + k * 0.5f) % 3.6f;
				float al = Math.max(0f, 1f - Math.abs(phase - 1.8f) / 1.8f);
				int a = (int) (230 * al);
				int x0 = 52 + j * 13;
				int len = 9 + j * 2;
				if (a > 4) {
					ctx.fill(x0, -2, x0 + len, 2, (a << 24) | 0xFFFFFF);
					ctx.fill(x0 + 1, -1, x0 + len - 1, 1, (a << 24) | 0xFFE066);
				}
			}
			m.pop();
		}

		m.scale(3f, 3f, 1f);
		ctx.drawTexture(MEGAFONO, -16, -16, 0, 0, 32, 32, 32, 32);
		m.pop();
	}

	private static float easeOutBack(float x) {
		float c1 = 1.70158f, c3 = c1 + 1f;
		float p = x - 1f;
		return 1f + c3 * p * p * p + c1 * p * p;
	}

	private static float easeInBack(float x) {
		float c1 = 1.70158f, c3 = c1 + 1f;
		return c3 * x * x * x - c1 * x * x;
	}
}
