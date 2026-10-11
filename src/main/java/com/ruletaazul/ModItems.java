package com.ruletaazul;

import com.ruletaazul.item.BlueRouletteItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModItems {
	public static final Item RULETA_AZUL = Registry.register(Registries.ITEM,
		Identifier.of(RuletaAzulMod.MOD_ID, "ruleta_azul"),
		new BlueRouletteItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC), 0));

	public static final Item TEMPORIZADOR = Registry.register(Registries.ITEM,
		Identifier.of(RuletaAzulMod.MOD_ID, "temporizador"),
		new BlueRouletteItem(new Item.Settings().maxCount(1).rarity(Rarity.RARE), 1));

	public static final ItemGroup GROUP = Registry.register(Registries.ITEM_GROUP,
		Identifier.of(RuletaAzulMod.MOD_ID, "main"),
		FabricItemGroup.builder()
			.icon(() -> new ItemStack(RULETA_AZUL))
			.displayName(Text.translatable("itemGroup.ruletaazul"))
			.entries((ctx, entries) -> {
				entries.add(RULETA_AZUL);
				entries.add(TEMPORIZADOR);
			})
			.build());

	public static void init() {}
}
