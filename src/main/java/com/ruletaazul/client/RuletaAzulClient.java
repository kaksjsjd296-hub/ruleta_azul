package com.ruletaazul.client;

import com.ruletaazul.ModEntities;
import com.ruletaazul.net.Payloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;

public class RuletaAzulClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(Payloads.OpenMenu.ID, (payload, context) -> {
			MinecraftClient mc = context.client();
			if (payload.which() == 0) mc.setScreen(new EventMenuScreen());
			else mc.setScreen(new TimerScreen());
		});
		ClientPlayNetworking.registerGlobalReceiver(Payloads.Sync.ID, (payload, context) -> ClientState.set(payload.remaining()));
		ClientPlayNetworking.registerGlobalReceiver(Payloads.EventStart.ID, (payload, context) -> Hud.onEventStart(payload.id()));

		EntityRendererRegistry.register(ModEntities.HURRICANE, HurricaneRenderer::new);
		HudRenderCallback.EVENT.register(Hud::render);
		ClientTickEvents.END_CLIENT_TICK.register(ScreenFx::tick);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientState.reset());
	}
}
