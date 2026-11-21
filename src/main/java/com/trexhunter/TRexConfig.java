package com.trexhunter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/** Configuração simples em config/trexhunter.properties (criada automaticamente). */
public final class TRexConfig {
    public static boolean enableHunts = true;
    public static boolean enableWatchers = true;
    public static boolean onlyOverworld = true;
    public static boolean affectCreative = false;
    /** Modo de teste: caçadas a cada 30-60s e observações a cada 15-30s. */
    public static boolean debugFastTimers = false;

    public static int countdownSeconds = 20;
    public static int huntMinMinutes = 12;
    public static int huntMaxMinutes = 25;
    public static int watchMinMinutes = 6;
    public static int watchMaxMinutes = 14;

    private TRexConfig() {}

    public static void load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("trexhunter.properties");
        Properties props = new Properties();

        if (Files.exists(file)) {
            try (var in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                TRexHunterMod.LOGGER.warn("Não consegui ler {}: {}", file, e.getMessage());
            }
        }

        enableHunts = bool(props, "enableHunts", enableHunts);
        enableWatchers = bool(props, "enableWatchers", enableWatchers);
        onlyOverworld = bool(props, "onlyOverworld", onlyOverworld);
        affectCreative = bool(props, "affectCreative", affectCreative);
        debugFastTimers = bool(props, "debugFastTimers", debugFastTimers);

        countdownSeconds = Math.max(3, Math.min(300, num(props, "countdownSeconds", countdownSeconds)));
        huntMinMinutes = Math.max(1, num(props, "huntMinMinutes", huntMinMinutes));
        huntMaxMinutes = Math.max(huntMinMinutes, num(props, "huntMaxMinutes", huntMaxMinutes));
        watchMinMinutes = Math.max(1, num(props, "watchMinMinutes", watchMinMinutes));
        watchMaxMinutes = Math.max(watchMinMinutes, num(props, "watchMaxMinutes", watchMaxMinutes));

        save(file);
    }

    private static void save(Path file) {
        Properties out = new Properties();
        out.setProperty("enableHunts", String.valueOf(enableHunts));
        out.setProperty("enableWatchers", String.valueOf(enableWatchers));
        out.setProperty("onlyOverworld", String.valueOf(onlyOverworld));
        out.setProperty("affectCreative", String.valueOf(affectCreative));
        out.setProperty("debugFastTimers", String.valueOf(debugFastTimers));
        out.setProperty("countdownSeconds", String.valueOf(countdownSeconds));
        out.setProperty("huntMinMinutes", String.valueOf(huntMinMinutes));
        out.setProperty("huntMaxMinutes", String.valueOf(huntMaxMinutes));
        out.setProperty("watchMinMinutes", String.valueOf(watchMinMinutes));
        out.setProperty("watchMaxMinutes", String.valueOf(watchMaxMinutes));

        try (Writer w = Files.newBufferedWriter(file)) {
            out.store(w, "T-Rex Hunter - tempos em minutos; countdownSeconds = tempo para se esconder; debugFastTimers = teste rapido");
        } catch (IOException e) {
            TRexHunterMod.LOGGER.warn("Não consegui salvar {}: {}", file, e.getMessage());
        }
    }

    private static boolean bool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return v == null ? def : Boolean.parseBoolean(v.trim());
    }

    private static int num(Properties p, String key, int def) {
        try {
            return Integer.parseInt(p.getProperty(key, "").trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
