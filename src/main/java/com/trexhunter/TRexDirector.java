package com.trexhunter;

import com.trexhunter.entity.TRexEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Decide, aleatoriamente e por jogador, quando um T-Rex aparece para caçar
 * ou apenas para observar de longe.
 */
public final class TRexDirector {
    private static final Map<UUID, Timers> TIMERS = new HashMap<>();
    private static int ticks;

    private static final class Timers {
        int hunt;
        int watch;

        Timers(int hunt, int watch) {
            this.hunt = hunt;
            this.watch = watch;
        }
    }

    private TRexDirector() {}

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(TRexDirector::onTick);
    }

    private static void onTick(MinecraftServer server) {
        // Roda uma vez por segundo.
        if (++ticks % 20 != 0) {
            return;
        }

        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        if (ticks % 1200 == 0) {
            Set<UUID> online = new HashSet<>();
            for (ServerPlayer p : players) {
                online.add(p.getUUID());
            }
            TIMERS.keySet().retainAll(online);
        }

        for (ServerPlayer player : players) {
            if (!(player.level() instanceof ServerLevel level)) {
                continue;
            }
            if (TRexConfig.onlyOverworld && !level.dimension().equals(Level.OVERWORLD)) {
                continue;
            }

            Timers t = TIMERS.computeIfAbsent(player.getUUID(), k -> new Timers(rollHunt(), rollWatch()));

            if (TRexConfig.enableHunts && --t.hunt <= 0) {
                if (trySpawn(level, player, true)) {
                    t.hunt = rollHunt();
                    t.watch = Math.max(t.watch, 120); // sem observador logo depois da caçada
                } else {
                    t.hunt = 10;
                }
            }

            if (TRexConfig.enableWatchers && --t.watch <= 0) {
                t.watch = trySpawn(level, player, false) ? rollWatch() : 10;
            }
        }
    }

    private static boolean trySpawn(ServerLevel level, ServerPlayer player, boolean hunt) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        if (!player.isAlive() || player.isSpectator()) {
            return false;
        }
        if (player.isCreative() && !TRexConfig.affectCreative) {
            return false;
        }

        // Só um T-Rex por região por vez.
        AABB area = player.getBoundingBox().inflate(140.0);
        if (!level.getEntitiesOfClass(TRexEntity.class, area).isEmpty()) {
            return false;
        }

        Vec3 pos = findSpawnPos(level, player, !hunt);
        if (pos == null) {
            return false;
        }

        TRexEntity trex = ModEntities.TREX.create(level, EntitySpawnReason.EVENT);
        if (trex == null) {
            return false;
        }

        trex.setPos(pos.x, pos.y, pos.z);
        if (hunt) {
            trex.beginHunt(player);
        } else {
            trex.beginWatching(player);
        }
        level.addFreshEntity(trex);
        return true;
    }

    /**
     * Procura um ponto livre no chão a 38-62 blocos do jogador.
     * Para observadores, o ponto fica dentro do campo de visão do jogador.
     */
    private static Vec3 findSpawnPos(ServerLevel level, ServerPlayer player, boolean inView) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double min = inView ? 38.0 : 40.0;
        double max = inView ? 56.0 : 62.0;

        for (int i = 0; i < 40; i++) {
            double yawDeg = inView ? player.getYRot() + r.nextDouble(-40.0, 40.0) : r.nextDouble(360.0);
            double rad = Math.toRadians(yawDeg);
            double dist = min + r.nextDouble() * (max - min);
            double x = player.getX() - Math.sin(rad) * dist;
            double z = player.getZ() + Math.cos(rad) * dist;

            BlockPos column = BlockPos.containing(x, player.getY(), z);
            if (!level.hasChunkAt(column)) {
                continue;
            }

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
            if (Math.abs(y - player.getY()) > 14.0) {
                continue; // evita spawnar em cima de montanhas ou longe da altura do jogador
            }

            BlockPos feet = new BlockPos(column.getX(), y, column.getZ());
            BlockPos below = feet.below();
            if (!level.getFluidState(feet).isEmpty() || !level.getFluidState(below).isEmpty()) {
                continue;
            }
            BlockState ground = level.getBlockState(below);
            if (ground.getCollisionShape(level, below).isEmpty()) {
                continue;
            }

            double px = column.getX() + 0.5;
            double pz = column.getZ() + 0.5;
            AABB box = ModEntities.TREX.getDimensions().makeBoundingBox(px, y, pz);
            if (!level.noCollision(box)) {
                continue;
            }
            return new Vec3(px, y, pz);
        }
        return null;
    }

    private static int rollHunt() {
        if (TRexConfig.debugFastTimers) {
            return roll(30, 60);
        }
        return roll(TRexConfig.huntMinMinutes * 60, TRexConfig.huntMaxMinutes * 60);
    }

    private static int rollWatch() {
        if (TRexConfig.debugFastTimers) {
            return roll(15, 30);
        }
        return roll(TRexConfig.watchMinMinutes * 60, TRexConfig.watchMaxMinutes * 60);
    }

    private static int roll(int min, int max) {
        if (max <= min) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
