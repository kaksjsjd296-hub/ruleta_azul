package com.ruletaazul;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class ModSounds {
	public static final SoundEvent ALERTA_SISMICA = reg("alerta_sismica");
	public static final SoundEvent MEGAFONO_ALERTA = reg("megafono_alerta");
	public static final SoundEvent TEMBLOR = reg("temblor");
	public static final SoundEvent VIENTO = reg("viento");
	public static final SoundEvent METEORO = reg("meteoro");
	public static final SoundEvent DING = reg("ding");

	private static SoundEvent reg(String name) {
		Identifier id = Identifier.of(RuletaAzulMod.MOD_ID, name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	public static void init() {}
}
