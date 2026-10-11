package com.ruletaazul;

import com.ruletaazul.net.Payloads;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class RuletaAzulMod implements ModInitializer {
	public static final String MOD_ID = "ruletaazul";

	@Override
	public void onInitialize() {
		ModSounds.init();
		ModEntities.init();
		ModItems.init();

		PayloadTypeRegistry.playS2C().register(Payloads.OpenMenu.ID, Payloads.OpenMenu.CODEC);
		PayloadTypeRegistry.playS2C().register(Payloads.Sync.ID, Payloads.Sync.CODEC);
		PayloadTypeRegistry.playS2C().register(Payloads.EventStart.ID, Payloads.EventStart.CODEC);
		PayloadTypeRegistry.playC2S().register(Payloads.ToggleEvent.ID, Payloads.ToggleEvent.CODEC);
		PayloadTypeRegistry.playC2S().register(Payloads.StartTimer.ID, Payloads.StartTimer.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(Payloads.ToggleEvent.ID, (payload, context) -> {
			int id = payload.id();
			if (id < 0 || id >= EventManager.COUNT) return;
			if (payload.activate()) {
				EventManager.activate(context.server(), id, Math.min(payload.seconds(), 86400));
			} else {
				EventManager.deactivate(context.server(), id);
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(Payloads.StartTimer.ID, (payload, context) -> {
			TimerManager.start(context.server(), Math.min(payload.seconds(), 86400), payload.color(), payload.title());
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			EventManager.tick(server);
			TimerManager.tick(server);
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			EventManager.addPlayerToBars(handler.player);
			TimerManager.addPlayer(handler.player);
			EventManager.sync(handler.player);
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			EventManager.removePlayerFromBars(handler.player);
			TimerManager.removePlayer(handler.player);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			EventManager.reset();
			TimerManager.stop();
		});
	}
}
