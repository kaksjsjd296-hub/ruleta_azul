package com.ruletaazul.client;

import com.ruletaazul.net.Payloads;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Menu transparente del temporizador: minutos, segundos, nombre y color de la boss bar. */
public class TimerScreen extends Screen {
	private static final int PW = 240, PH = 164;
	// mismo orden que BossBar.Color: PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
	private static final int[] COLORS = {0xE86FA8, 0x4C8DF5, 0xE53935, 0x43A047, 0xFDD835, 0x8E24AA, 0xFFFFFF};

	private static int lastColor = 1;
	private TextFieldWidget minField, secField, nameField;
	private int px, py;

	public TimerScreen() {
		super(Text.translatable("screen.ruletaazul.timer"));
	}

	@Override
	protected void init() {
		px = (this.width - PW) / 2;
		py = (this.height - PH) / 2;
		minField = new TextFieldWidget(this.textRenderer, px + 12, py + 46, 50, 18, Text.empty());
		minField.setMaxLength(3);
		minField.setText("1");
		minField.setTextPredicate(t -> t.matches("[0-9]*"));
		secField = new TextFieldWidget(this.textRenderer, px + 74, py + 46, 50, 18, Text.empty());
		secField.setMaxLength(3);
		secField.setText("0");
		secField.setTextPredicate(t -> t.matches("[0-9]*"));
		nameField = new TextFieldWidget(this.textRenderer, px + 136, py + 46, 92, 18, Text.empty());
		nameField.setMaxLength(24);
		nameField.setPlaceholder(Text.translatable("timer.ruletaazul.default"));
		this.addDrawableChild(minField);
		this.addDrawableChild(secField);
		this.addDrawableChild(nameField);
	}

	@Override public boolean shouldPause() { return false; }
	@Override public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

	private static boolean in(double x, double y, int x0, int y0, int w, int h) { return x >= x0 && x < x0 + w && y >= y0 && y < y0 + h; }

	@Override
	public void render(DrawContext ctx, int mx, int my, float delta) {
		ctx.fill(px, py, px + PW, py + PH, 0xFF000000);
		ctx.fill(px + 3, py + 3, px + PW - 3, py + PH - 3, 0xFFFFFFFF);

		var m = ctx.getMatrices();
		m.push();
		m.translate(px + 12f, py + 12f, 0f);
		m.scale(1.5f, 1.5f, 1f);
		ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.timer_title").formatted(Formatting.BOLD), 0, 0, 0xFF000000, false);
		m.pop();

		int lc = 0xFF222222;
		ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.minutes"), px + 12, py + 35, lc, false);
		ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.seconds"), px + 74, py + 35, lc, false);
		ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.name"), px + 136, py + 35, lc, false);
		ctx.drawText(this.textRenderer, Text.translatable("gui.ruletaazul.color"), px + 12, py + 76, lc, false);

		for (int i = 0; i < COLORS.length; i++) {
			int x = px + 12 + i * 28, y = py + 88;
			boolean sel = i == lastColor;
			ctx.fill(x - 2, y - 2, x + 24, y + 24, sel ? 0xFF000000 : 0xFFBBBBBB);
			ctx.fill(x, y, x + 22, y + 22, 0xFF000000 | COLORS[i]);
		}

		button(ctx, px + 12, py + 124, 100, 22, in(mx, my, px + 12, py + 124, 100, 22) ? 0xFF7DFF7D : 0xFF00FF00, "gui.ruletaazul.start");
		button(ctx, px + 128, py + 124, 100, 22, in(mx, my, px + 128, py + 124, 100, 22) ? 0xFFFF7D7D : 0xFFFF0000, "gui.ruletaazul.stop");
		super.render(ctx, mx, my, delta);
	}

	private void button(DrawContext ctx, int x, int y, int w, int h, int color, String key) {
		ctx.fill(x, y, x + w, y + h, color);
		Text t = Text.translatable(key);
		ctx.drawText(this.textRenderer, t, x + (w - this.textRenderer.getWidth(t)) / 2, y + (h - 8) / 2, 0xFF000000, false);
	}

	private void click() {
		this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
	}

	private static int num(String s) {
		try { return s.isEmpty() ? 0 : Integer.parseInt(s); } catch (Exception e) { return 0; }
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			for (int i = 0; i < COLORS.length; i++) {
				if (in(mx, my, px + 12 + i * 28, py + 88, 22, 22)) {
					lastColor = i;
					click();
					return true;
				}
			}
			if (in(mx, my, px + 12, py + 124, 100, 22)) {
				int total = num(minField.getText()) * 60 + num(secField.getText());
				ClientPlayNetworking.send(new Payloads.StartTimer(Math.min(total, 86400), lastColor, nameField.getText()));
				click();
				return true;
			}
			if (in(mx, my, px + 128, py + 124, 100, 22)) {
				ClientPlayNetworking.send(new Payloads.StartTimer(0, lastColor, ""));
				click();
				return true;
			}
		}
		return super.mouseClicked(mx, my, button);
	}
}
