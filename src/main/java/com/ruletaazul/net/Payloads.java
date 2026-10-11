package com.ruletaazul.net;

import com.ruletaazul.RuletaAzulMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public final class Payloads {
	private Payloads() {}

	/** Servidor -> cliente: which = 0 menu de eventos, 1 menu del temporizador. */
	public record OpenMenu(int which) implements CustomPayload {
		public static final Id<OpenMenu> ID = new Id<>(Identifier.of(RuletaAzulMod.MOD_ID, "open_menu"));
		public static final PacketCodec<RegistryByteBuf, OpenMenu> CODEC =
			PacketCodec.tuple(PacketCodecs.VAR_INT, OpenMenu::which, OpenMenu::new);
		@Override public Id<? extends CustomPayload> getId() { return ID; }
	}

	/** Servidor -> cliente: tiempo restante de cada evento (-2 inactivo, -1 sin limite). */
	public record Sync(int[] remaining) implements CustomPayload {
		public static final Id<Sync> ID = new Id<>(Identifier.of(RuletaAzulMod.MOD_ID, "sync"));
		public static final PacketCodec<RegistryByteBuf, Sync> CODEC = new PacketCodec<RegistryByteBuf, Sync>() {
			@Override public Sync decode(RegistryByteBuf buf) {
				int[] r = new int[8];
				for (int i = 0; i < 8; i++) r[i] = buf.readInt();
				return new Sync(r);
			}
			@Override public void encode(RegistryByteBuf buf, Sync value) {
				for (int i = 0; i < 8; i++) buf.writeInt(value.remaining()[i]);
			}
		};
		@Override public Id<? extends CustomPayload> getId() { return ID; }
	}

	/** Servidor -> cliente: un evento acaba de comenzar (megafono, alerta, filtro rojo). */
	public record EventStart(int id) implements CustomPayload {
		public static final Id<EventStart> ID = new Id<>(Identifier.of(RuletaAzulMod.MOD_ID, "event_start"));
		public static final PacketCodec<RegistryByteBuf, EventStart> CODEC =
			PacketCodec.tuple(PacketCodecs.VAR_INT, EventStart::id, EventStart::new);
		@Override public Id<? extends CustomPayload> getId() { return ID; }
	}

	/** Cliente -> servidor: activar / desactivar un evento. seconds <= 0 = sin limite. */
	public record ToggleEvent(int id, boolean activate, int seconds) implements CustomPayload {
		public static final Id<ToggleEvent> ID = new Id<>(Identifier.of(RuletaAzulMod.MOD_ID, "toggle_event"));
		public static final PacketCodec<RegistryByteBuf, ToggleEvent> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, ToggleEvent::id,
			PacketCodecs.BOOL, ToggleEvent::activate,
			PacketCodecs.VAR_INT, ToggleEvent::seconds,
			ToggleEvent::new);
		@Override public Id<? extends CustomPayload> getId() { return ID; }
	}

	/** Cliente -> servidor: iniciar el temporizador (seconds <= 0 lo detiene). */
	public record StartTimer(int seconds, int color, String title) implements CustomPayload {
		public static final Id<StartTimer> ID = new Id<>(Identifier.of(RuletaAzulMod.MOD_ID, "start_timer"));
		public static final PacketCodec<RegistryByteBuf, StartTimer> CODEC = PacketCodec.tuple(
			PacketCodecs.VAR_INT, StartTimer::seconds,
			PacketCodecs.VAR_INT, StartTimer::color,
			PacketCodecs.string(40), StartTimer::title,
			StartTimer::new);
		@Override public Id<? extends CustomPayload> getId() { return ID; }
	}
}
