package com.ruletaazul.events;

import net.minecraft.server.MinecraftServer;

public interface EventLogic {
	void start(MinecraftServer server);
	void tick(MinecraftServer server, int age);
	void stop(MinecraftServer server);
}
