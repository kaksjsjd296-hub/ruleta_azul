package com.ruletaazul.client;

import com.ruletaazul.HurricaneEntity;
import com.ruletaazul.RuletaAzulMod;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

/** Dibuja el huracan como una pila de placas girando que se ensanchan hacia arriba. */
public class HurricaneRenderer extends EntityRenderer<HurricaneEntity> {
	private static final Identifier TEX = Identifier.of(RuletaAzulMod.MOD_ID, "textures/entity/hurricane.png");
	private static final int FULL_LIGHT = 15728880;

	public HurricaneRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override public Identifier getTexture(HurricaneEntity entity) { return TEX; }

	@Override
	public boolean shouldRender(HurricaneEntity entity, Frustum frustum, double x, double y, double z) { return true; }

	@Override
	public void render(HurricaneEntity e, float yaw, float tickDelta, MatrixStack m, VertexConsumerProvider vcp, int light) {
		float t = e.age + tickDelta;
		VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(TEX));
		int layers = 28;
		for (int i = 0; i < layers; i++) {
			float h = i / (float) (layers - 1);
			float radius = 1.5f + h * h * 10f + h * 4f;
			m.push();
			m.translate(Math.sin(t * 0.1 + i * 0.5) * 0.8 * h, i * 2.0f, Math.cos(t * 0.12 + i * 0.4) * 0.8 * h);
			m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(t * (16f - h * 7f) + i * 23f));
			int shade = 255 - (int) (h * 60);
			for (int k = 0; k < 4; k++) {
				m.push();
				m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(k * 90f));
				m.translate(radius, 0, 0);
				float tang = 1.2f + radius * 0.7f;
				box(vc, m.peek(), -0.5f, 0f, -tang, 0.5f, 2.0f, tang, shade - 30, shade - 8, 255, 150);
				m.pop();
			}
			m.pop();
		}
	}

	private static void box(VertexConsumer vc, MatrixStack.Entry e, float x0, float y0, float z0, float x1, float y1, float z1, int r, int g, int b, int a) {
		// arriba / abajo
		quad(vc, e, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1, r, g, b, a);
		quad(vc, e, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, r, g, b, a);
		// lados
		quad(vc, e, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0, r, g, b, a);
		quad(vc, e, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, r, g, b, a);
		quad(vc, e, x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, r, g, b, a);
		quad(vc, e, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0, r, g, b, a);
	}

	private static void quad(VertexConsumer vc, MatrixStack.Entry e,
							 float ax, float ay, float az, float bx, float by, float bz,
							 float cx, float cy, float cz, float dx, float dy, float dz,
							 int r, int g, int b, int a) {
		v(vc, e, ax, ay, az, 0f, 0f, r, g, b, a);
		v(vc, e, bx, by, bz, 1f, 0f, r, g, b, a);
		v(vc, e, cx, cy, cz, 1f, 1f, r, g, b, a);
		v(vc, e, dx, dy, dz, 0f, 1f, r, g, b, a);
	}

	private static void v(VertexConsumer vc, MatrixStack.Entry e, float x, float y, float z, float u, float v, int r, int g, int b, int a) {
		vc.vertex(e, x, y, z).color(r, g, b, a).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(e, 0f, 1f, 0f);
	}
}
