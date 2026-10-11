package com.ruletaazul;

import com.ruletaazul.events.EventUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Huracan polar gigante: persigue al jugador mas cercano, lo absorbe y lo lanza por los aires. */
public class HurricaneEntity extends Entity {
	private Vec3d wander = Vec3d.ZERO;
	private int wanderTicks = 0;

	public HurricaneEntity(EntityType<? extends HurricaneEntity> type, World world) {
		super(type, world);
		this.noClip = true;
		this.ignoreCameraFrustum = true;
		this.setNoGravity(true);
	}

	@Override protected void initDataTracker(DataTracker.Builder builder) {}
	@Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
	@Override protected void writeCustomDataToNbt(NbtCompound nbt) {}
	@Override public boolean isAttackable() { return false; }
	@Override public boolean damage(DamageSource source, float amount) { return false; }
	@Override public boolean shouldRender(double distance) { return true; }

	@Override
	public void tick() {
		super.tick();
		if (this.getWorld().isClient) return;
		if (!EventManager.isActive(EventManager.HURRICANE)) {
			this.discard();
			return;
		}
		ServerWorld w = (ServerWorld) this.getWorld();

		// ---- movimiento ----
		double nx = this.getX(), nz = this.getZ();
		PlayerEntity target = w.getClosestPlayer(this.getX(), this.getY(), this.getZ(), 250.0, false);
		if (target != null) {
			Vec3d d = new Vec3d(target.getX() - nx, 0, target.getZ() - nz);
			if (d.lengthSquared() > 4.0) {
				d = d.normalize().multiply(0.28);
				nx += d.x;
				nz += d.z;
			}
		} else {
			if (--wanderTicks <= 0) {
				double a = this.random.nextDouble() * Math.PI * 2;
				wander = new Vec3d(Math.cos(a) * 0.2, 0, Math.sin(a) * 0.2);
				wanderTicks = 100;
			}
			nx += wander.x;
			nz += wander.z;
		}
		int bx = MathHelper.floor(nx), bz = MathHelper.floor(nz);
		if (EventUtil.loaded(w, bx, bz)) {
			this.setPosition(nx, EventUtil.surfaceY(w, bx, bz), nz);
		}

		// ---- absorcion ----
		for (ServerPlayerEntity p : w.getPlayers()) {
			if (p.isSpectator()) continue;
			double dx = this.getX() - p.getX();
			double dz = this.getZ() - p.getZ();
			double dy = p.getY() - this.getY();
			double dist = Math.sqrt(dx * dx + dz * dz);
			if (dist > 32.0 || dy < -10.0 || dy > 70.0) continue;
			if (dist < 0.01) { dx = 1.0; dist = 1.0; }
			double ix = dx / dist, iz = dz / dist;       // hacia el centro
			double tx = -iz, tz = ix;                      // tangente (giro)
			if (dist > 8.0) {
				p.addVelocity(ix * 0.10 + tx * 0.05, 0.0, iz * 0.10 + tz * 0.05);
			} else {
				p.setVelocity(tx * 1.6 - ix * 0.2, 1.5, tz * 1.6 - iz * 0.2);
			}
			p.velocityModified = true;
			p.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(p));
		}

		if (this.age % 40 == 0) {
			w.playSound(null, this.getX(), this.getY() + 10, this.getZ(), ModSounds.VIENTO, SoundCategory.AMBIENT, 5.0f, 1.0f);
		}
	}
}
