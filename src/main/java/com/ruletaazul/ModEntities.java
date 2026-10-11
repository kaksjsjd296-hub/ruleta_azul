package com.ruletaazul;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModEntities {
	public static final EntityType<HurricaneEntity> HURRICANE = Registry.register(
		Registries.ENTITY_TYPE,
		Identifier.of(RuletaAzulMod.MOD_ID, "hurricane"),
		EntityType.Builder.create(HurricaneEntity::new, SpawnGroup.MISC)
			.dimensions(16.0f, 56.0f)
			.maxTrackingRange(16)
			.trackingTickInterval(1)
			.build(RuletaAzulMod.MOD_ID + ":hurricane"));

	public static void init() {}
}
