package com.trexhunter.entity;

import com.trexhunter.TRexConfig;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * O T-Rex. Tem dois modos:
 *  - HUNTER: aparece, ruge e inicia uma contagem (20s por padrão) em que fica parado.
 *            Quando acaba, caça o jogador. Ele precisa ENXERGAR (ou sentir de muito perto) o
 *            jogador; se perder o rastro por ~45s, desiste e some.
 *  - WATCHER: fica parado ao longe, olhando para o jogador (estilo From the Fog). Some se o
 *             jogador encarar por ~3s ou chegar perto. Raramente vira uma caçada de verdade.
 */
public class TRexEntity extends Monster {
    public enum Mode { HUNTER, WATCHER }

    private static final EntityDataAccessor<Boolean> DATA_ROARING =
            SynchedEntityData.defineId(TRexEntity.class, EntityDataSerializers.BOOLEAN);

    private Mode mode = Mode.HUNTER;
    private int countdown;
    private boolean introPlayed;
    private UUID targetId;
    private Vec3 lastKnownPos;
    private int lostTicks;
    private int stareTicks;
    private int life;

    public TRexEntity(EntityType<? extends TRexEntity> type, Level level) {
        super(type, level);
        this.xpReward = 60;
        this.countdown = TRexConfig.countdownSeconds * 20;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 220.0)
                .add(Attributes.MOVEMENT_SPEED, 0.17)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.FOLLOW_RANGE, 96.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 6.0)
                .add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ROARING, false);
    }

    public boolean isRoaring() {
        return this.entityData.get(DATA_ROARING);
    }

    private void setRoaring(boolean roaring) {
        this.entityData.set(DATA_ROARING, roaring);
    }

    // ------------------------------------------------------------------ setup

    public void beginHunt(Player player) {
        this.mode = Mode.HUNTER;
        this.targetId = player.getUUID();
        this.countdown = TRexConfig.countdownSeconds * 20;
        this.introPlayed = false;
        this.stareTicks = 0;
        this.lostTicks = 0;
        this.life = 0;
        this.setInvulnerable(false);
        this.setSilent(false);
        this.faceEntity(player, 1.0F);
    }

    public void beginWatching(Player player) {
        this.mode = Mode.WATCHER;
        this.targetId = player.getUUID();
        this.countdown = 0;
        this.stareTicks = 0;
        this.life = 0;
        this.setInvulnerable(true);
        this.setSilent(true);
        this.faceEntity(player, 1.0F);
    }

    private boolean isHuntActive() {
        return this.mode == Mode.HUNTER && this.countdown <= 0;
    }

    // ------------------------------------------------------------------ goals

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0, true) {
            @Override
            public boolean canUse() {
                return isHuntActive() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return isHuntActive() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(2, new SearchLastKnownGoal());
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.7) {
            @Override
            public boolean canUse() {
                return isHuntActive() && getTarget() == null && lastKnownPos == null && super.canUse();
            }
        });
    }

    /** Depois de perder o jogador de vista, vai até o último lugar onde o viu. */
    private class SearchLastKnownGoal extends Goal {
        private int repath;

        SearchLastKnownGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return isHuntActive() && getTarget() == null && lastKnownPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            this.repath = 0;
        }

        @Override
        public void tick() {
            if (lastKnownPos == null) {
                return;
            }
            if (position().distanceToSqr(lastKnownPos) < 9.0) {
                lastKnownPos = null;
                getNavigation().stop();
                return;
            }
            if (--this.repath <= 0) {
                this.repath = 10;
                getNavigation().moveTo(lastKnownPos.x, lastKnownPos.y, lastKnownPos.z, 1.1);
            }
        }
    }

    // ------------------------------------------------------------------- tick

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        this.life++;

        Player player = resolveTarget(serverLevel);
        if (this.mode == Mode.WATCHER) {
            tickWatcher(serverLevel, player);
        } else {
            tickHunter(serverLevel, player);
        }
    }

    private Player resolveTarget(ServerLevel level) {
        if (this.targetId != null) {
            Player p = level.getPlayerByUUID(this.targetId);
            if (p != null && p.isAlive() && !p.isSpectator()) {
                return p;
            }
            this.targetId = null;
        }
        Player nearest = level.getNearestPlayer(this, 128.0);
        if (nearest != null) {
            this.targetId = nearest.getUUID();
        }
        return nearest;
    }

    // ----------------------------------------------------------------- hunter

    private void tickHunter(ServerLevel level, Player player) {
        if (player == null) {
            if (++this.lostTicks > 200) {
                vanish(level);
            }
            return;
        }

        if (this.countdown > 0) {
            tickCountdown(player);
            return;
        }

        if (canDetect(player)) {
            this.setTarget(player);
            this.lastKnownPos = player.position();
            this.lostTicks = 0;
        } else {
            this.lostTicks++;
            if (this.lostTicks > 30 && this.getTarget() != null) {
                this.setTarget(null);
            }
            // Perdeu o rastro (jogador escondido ou muito longe): desiste.
            if (this.lostTicks > 20 * 45 || this.distanceToSqr(player) > 140.0 * 140.0) {
                vanish(level);
                return;
            }
        }

        if (this.life > 20 * 60 * 4) {
            vanish(level);
            return;
        }

        double dist = this.distanceTo(player);
        if (this.life % 160 == 0) {
            this.playSound(SoundEvents.RAVAGER_ROAR, 8.0F, 0.5F);
        }
        if (this.life % 40 == 0 && dist < 30.0) {
            player.playSound(SoundEvents.WARDEN_HEARTBEAT, 1.2F, 0.9F);
        }
    }

    private boolean canDetect(Player player) {
        double dist = this.distanceTo(player);
        double sight = player.isCrouching() ? 28.0 : 72.0;
        if (dist <= sight && this.hasLineOfSight(player)) {
            return true;
        }
        // Cheiro/barulho: muito perto, ou correndo por perto, mesmo sem linha de visão.
        return dist <= 10.0 || (player.isSprinting() && dist <= 20.0);
    }

    private void tickCountdown(Player player) {
        this.setRoaring(true);
        this.getNavigation().stop();
        this.setTarget(null);
        this.faceEntity(player, 0.25F);

        if (!this.introPlayed) {
            this.introPlayed = true;
            this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 10.0F, 0.55F);
            sendTitle(player);
        }

        if (this.countdown % 20 == 0) {
            int secs = this.countdown / 20;
            player.displayClientMessage(
                    Component.translatable("message.trexhunter.countdown", secs)
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                    true);
            if (secs > 0 && secs <= 5) {
                player.playSound(SoundEvents.WARDEN_HEARTBEAT, 1.5F, 0.8F);
            }
            if (secs == 10) {
                this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 8.0F, 0.5F);
            }
        }

        this.countdown--;
        if (this.countdown <= 0) {
            this.setRoaring(false);
            this.playSound(SoundEvents.RAVAGER_ROAR, 10.0F, 0.4F);
            this.lastKnownPos = player.position();
            player.displayClientMessage(
                    Component.translatable("message.trexhunter.run")
                            .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                    true);
        }
    }

    private void sendTitle(Player player) {
        if (player instanceof ServerPlayer sp) {
            int secs = TRexConfig.countdownSeconds;
            sp.connection.send(new ClientboundSetSubtitleTextPacket(
                    Component.translatable("title.trexhunter.subtitle", secs).withStyle(ChatFormatting.RED)));
            sp.connection.send(new ClientboundSetTitlesAnimationPacket(5, 50, 15));
            sp.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("title.trexhunter.warning").withStyle(ChatFormatting.DARK_RED)));
        }
    }

    // ---------------------------------------------------------------- watcher

    private void tickWatcher(ServerLevel level, Player player) {
        if (player == null || this.life > 20 * 90) {
            vanish(level);
            return;
        }

        this.getNavigation().stop();
        this.setTarget(null);
        this.faceEntity(player, 0.3F);

        // Chegou perto demais: some, como se nunca tivesse estado ali.
        if (this.distanceToSqr(player) < 14.0 * 14.0) {
            vanish(level);
            return;
        }

        if (isBeingLookedAt(player)) {
            this.stareTicks++;
        } else {
            this.stareTicks = Math.max(0, this.stareTicks - 1);
        }

        if (this.stareTicks > 60) {
            if (this.random.nextFloat() < 0.15F) {
                // Surpresa: o observador vira caçador.
                beginHunt(player);
            } else {
                vanish(level);
            }
        }
    }

    private boolean isBeingLookedAt(Player player) {
        Vec3 look = player.getViewVector(1.0F).normalize();
        Vec3 toMe = this.getEyePosition().subtract(player.getEyePosition()).normalize();
        return look.dot(toMe) > 0.96 && player.hasLineOfSight(this);
    }

    // ----------------------------------------------------------------- common

    private void faceEntity(Entity target, float smoothing) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double dy = target.getEyeY() - this.getEyeY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) -(Mth.atan2(dy, horizontal) * (180.0 / Math.PI));

        float newYaw = Mth.rotLerp(smoothing, this.getYRot(), yaw);
        this.setYRot(newYaw);
        this.setYHeadRot(newYaw);
        this.setYBodyRot(newYaw);
        this.setXRot(Mth.clamp(pitch, -25.0F, 25.0F));
    }

    private void vanish(ServerLevel level) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 2.0, this.getZ(),
                50, 1.2, 1.8, 1.2, 0.02);
        this.discard();
    }

    // ------------------------------------------------------------- misc/sounds

    /** Estado (contagem, modo) não é salvo: ao recarregar o mundo, o T-Rex simplesmente some. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RAVAGER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 1.6F, 0.55F);
    }
}
