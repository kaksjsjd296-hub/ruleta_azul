package com.ruletaazul.client;

import com.ruletaazul.RuletaAzulMod;
import com.ruletaazul.net.Payloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

/** Menu transparente de la ruleta azul: rueda + numeros 1-8 + tarjeta del evento. */
public class EventMenuScreen extends Screen {
	private static final Identifier WHEEL = Identifier.of(RuletaAzulMod.MOD_ID, "textures/gui/wheel.png");

	// distribucion logica (centrada en x = 0)
	private static final int CARD_X0 = -120, CARD_Y0 = 192, CARD_X1 = 120, CARD_Y1 = 282;
	private static final int BTN_Y = 262, BTN_H = 14;
	private static final int ACT_X = -44, ACT_W = 62, DEA_X = 24, DEA_W = 80;

	private int selected = -1;
	private float wheelAngle = 0f, wheelStart = 0f, wheelTarget = 0f;
	private long spinStart = -1;
	private final float[] hover = new float[8];
	private TextFieldWidget timeField;
	private float s = 1f;

	public EventMenuScreen() {
		super(Text.translatable("screen.ruletaazul.menu"));
	}

	@Override
	protected void init() {
		s = Math.min(1f, (this.height - 8) / 292f);
		timeField = new TextFieldWidget(this.textRenderer, 10, this.height - 28, 110, 18, Text.empty());
		timeField.setMaxLength(8);
		timeField.setPlaceholder(Text.translatable("gui.ruletaazul.time_hint"));
		timeField.setTextPredicate(str -> str.matches("[0-9:msMS]*"));
		timeField.visible = selected >= 0;
		this.addDrawableChild(timeField);
	}

	@Override public boolean shouldPause() { return false; }

	/** Fondo totalmente transparente. */
	@Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

	private float lx(double mx) { return (float) ((mx - this.width / 2.0) / s); }
	private float ly(double my) { return (float) (my / s); }

	private static int numX(int i) { return -69 + (i % 4) * 36; }
	private static int numY(int i) { return 114 + (i / 4) * 38; }
	private static boolean in(float x, float y, int x0, int y0, int w, int h) { return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h; }

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		float lx = lx(mouseX), ly = ly(mouseY);

		if (spinStart >= 0) {
			float f = Math.min(1f, (System.currentTimeMillis() - spinStart) / 1200f);
			float ease = 1f - (1f - f) * (1f - f) * (1f - f);
			wheelAngle = wheelStart + (wheelTarget - wheelStart) * ease;
			if (f >= 1f) spinStart = -1;
		}

		var m = ctx.getMatrices();
		m.push();
		m.translate(this.width / 2f, 0f, 0f);
		m.scale(s, s, 1f);

		// rueda
		m.push();
		m.translate(0f, 58f, 0f);
		m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(wheelAngle));
		ctx.drawTexture(WHEEL, -48, -48, 0, 0, 96, 96, 96, 96);
		m.pop();
		// puntero rojo
		ctx.fill(-5, 2, 5, 4, 0xFFE02020);
		ctx.fill(-4, 4, 4, 6, 0xFFE02020);
		ctx.fill(-3, 6, 3, 8, 0xFFE02020);
		ctx.fill(-2, 8, 2, 10, 0xFFE02020);
		ctx.fill(-1, 10, 1, 12, 0xFFE02020);

		// numeros
		for (int i = 0; i < 8; i++) {
			int x = numX(i), y = numY(i);
			boolean over = in(lx, ly, x, y, 30, 30);
			hover[i] += ((over ? 1f : 0f) - hover[i]) * 0.3f;
			float sc = 1f + 0.12f * hover[i];
			m.push();
			m.translate(x + 15f, y + 15f, 0f);
			m.scale(sc, sc, 1f);
			ctx.fill(-15, -15, 15, 15, 0xFF000000);
			int border = 0;
			if (ClientState.active(i)) border = 0xFF00FF00;
			else if (selected == i) border = 0xFFFFD83C;
			if (border != 0) {
				ctx.fill(-15, -15, 15, -13, border);
				ctx.fill(-15, 13, 15, 15, border);
				ctx.fill(-15, -15, -13, 15, border);
				ctx.fill(13, -15, 15, 15, border);
			}
			m.push();
			m.scale(2f, 2f, 1f);
			String n = String.valueOf(i + 1);
			ctx.drawText(this.textRenderer, n, -this.textRenderer.getWidth(n) / 2, -4, 0xFFFFFFFF, false);
			m.pop();
			m.pop();
		}

		if (selected >= 0) drawCard(ctx, lx, ly);
		m.pop();

		if (selected >= 0) {
			ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.time"), 10, this.height - 42, 0xFFFFFFFF, true);
		}
		super.render(ctx, mouseX, mouseY, delta);
	}

	private void drawCard(DrawContext ctx, float lx, float ly) {
		var m = ctx.getMatrices();
		ctx.fill(CARD_X0, CARD_Y0, CARD_X1, CARD_Y1, 0xFF000000);
		ctx.fill(CARD_X0 + 3, CARD_Y0 + 3, CARD_X1 - 3, CARD_Y1 - 3, 0xFFFFFFFF);

		// icono grande
		m.push();
		m.translate(CARD_X0 + 10f, CARD_Y0 + 12f, 0f);
		m.scale(1.75f, 1.75f, 1f);
		ctx.drawTexture(Hud.icon(selected), 0, 0, 0, 0, 32, 32, 32, 32);
		m.pop();

		int tx = CARD_X0 + 76;
		m.push();
		m.translate(tx, CARD_Y0 + 8f, 0f);
		m.scale(1.25f, 1.25f, 1f);
		ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.card_title"), 0, 0, 0xFF000000, false);
		m.pop();
		ctx.drawText(this.textRenderer, Text.translatable("event.ruletaazul." + (selected + 1) + ".name").formatted(Formatting.BOLD), tx, CARD_Y0 + 23, 0xFF000000, false);
		List<OrderedText> lines = this.textRenderer.wrapLines(Text.translatable("event.ruletaazul." + (selected + 1) + ".desc"), 156);
		for (int i = 0; i < lines.size() && i < 3; i++) {
			ctx.drawText(this.textRenderer, lines.get(i), tx, CARD_Y0 + 36 + i * 10, 0xFF333333, false);
		}
		if (ClientState.active(selected)) {
			ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.active").formatted(Formatting.BOLD), CARD_X0 + 14, CARD_Y0 + 74, 0xFF008800, false);
		}

		button(ctx, ACT_X, BTN_Y, ACT_W, BTN_H, in(lx, ly, ACT_X, BTN_Y, ACT_W, BTN_H) ? 0xFF7DFF7D : 0xFF00FF00, "gui.ruletaazul.activate");
		button(ctx, DEA_X, BTN_Y, DEA_W, BTN_H, in(lx, ly, DEA_X, BTN_Y, DEA_W, BTN_H) ? 0xFFFF7D7D : 0xFFFF0000, "gui.ruletaazul.deactivate");
	}

	private void button(DrawContext ctx, int x, int y, int w, int h, int color, String key) {
		ctx.fill(x, y, x + w, y + h, color);
		Text t = Text.translatable(key);
		ctx.drawText(this.textRenderer, t, x + (w - this.textRenderer.getWidth(t)) / 2, y + (h - 8) / 2 + 1, 0xFF000000, false);
	}

	private void click() {
		this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			float lx = lx(mouseX), ly = ly(mouseY);
			for (int i = 0; i < 8; i++) {
				if (in(lx, ly, numX(i), numY(i), 30, 30)) {
					select(i);
					click();
					return true;
				}
			}
			if (selected >= 0) {
				if (in(lx, ly, ACT_X, BTN_Y, ACT_W, BTN_H)) {
					ClientPlayNetworking.send(new Payloads.ToggleEvent(selected, true, parseSeconds(timeField.getText())));
					click();
					return true;
				}
				if (in(lx, ly, DEA_X, BTN_Y, DEA_W, BTN_H)) {
					ClientPlayNetworking.send(new Payloads.ToggleEvent(selected, false, 0));
					click();
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	private void select(int i) {
		if (selected != i) timeField.setText("");
		selected = i;
		timeField.visible = true;
		// gira la ruleta hasta dejar el sector elegido bajo el puntero
		float base = -(i * 45f + 22.5f);
		float diff = (((wheelAngle - base) % 360f) + 360f) % 360f;
		wheelStart = wheelAngle;
		wheelTarget = wheelAngle - (diff + 720f);
		spinStart = System.currentTimeMillis();
	}

	/** Acepta "90", "90s", "5m" o "1:30". Vacio = sin limite (0). */
	public static int parseSeconds(String str) {
		String s = str == null ? "" : str.trim().toLowerCase();
		if (s.isEmpty()) return 0;
		try {
			int r;
			if (s.contains(":")) {
				String[] a = s.split(":");
				int mi = a[0].isEmpty() ? 0 : Integer.parseInt(a[0]);
				int se = (a.length > 1 && !a[1].isEmpty()) ? Integer.parseInt(a[1]) : 0;
				r = mi * 60 + se;
			} else if (s.endsWith("m")) {
				r = Integer.parseInt(s.substring(0, s.length() - 1)) * 60;
			} else {
				if (s.endsWith("s")) s = s.substring(0, s.length() - 1);
				r = Integer.parseInt(s);
			}
			return Math.max(0, Math.min(r, 86400));
		} catch (Exception e) {
			return 0;
		}
	}
}
