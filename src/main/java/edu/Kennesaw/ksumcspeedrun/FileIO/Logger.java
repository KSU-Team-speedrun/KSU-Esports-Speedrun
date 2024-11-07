package edu.Kennesaw.ksumcspeedrun.FileIO;

import edu.Kennesaw.ksumcspeedrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@SuppressWarnings("SpellCheckingInspection")
public class Logger {

    // This Async method can be used to log severe error messages if necessary in an errorlog folder
    public static void logError(String message, Throwable e, Main plugin) {

        plugin.runAsyncTask(() -> {
            plugin.getLogger().severe(message);
            plugin.getLogger().severe(e.getMessage());
            Date date = new Date();
            String timeStamp = String.valueOf(date.getTime());
            File errorDir = new File(plugin.getDataFolder(), "errorlog");
            if (errorDir.mkdirs()) {
                plugin.getLogger().info("Creating subdirectory \"errorlog\"");
            }
            try {
                File errorFile = new File(errorDir, "error-log-" + timeStamp + ".txt");
                if (errorFile.createNewFile()) {
                    try (PrintWriter pw = new PrintWriter(errorFile)) {
                        pw.println("Severe error occurred on: " + date + " at " + date.getTime() + ":");
                        pw.println(message);
                        pw.println(e.getMessage());
                    }
                }
            } catch (IOException k) {
                Logger.logError("Error logging error file.", k, plugin);
            }
        });
    }

    public static void logError(String[] message, Throwable e, Main plugin) {
        plugin.runAsyncTask(() -> {
            for (String s : message) {
                plugin.getLogger().severe(s);
            }
            plugin.getLogger().severe(e.getMessage());
            Date date = new Date();
            String timeStamp = String.valueOf(date.getTime());
            File errorDir = new File(plugin.getDataFolder(), "errorlog");
            if (errorDir.mkdirs()) {
                plugin.getLogger().info("Creating subdirectory \"errorlog\"");
            }
            try {
                File errorFile = new File(errorDir, "error-log-" + timeStamp + ".txt");
                if (errorFile.createNewFile()) {
                    try (PrintWriter pw = new PrintWriter(errorFile)) {
                        pw.println("Severe error occurred on: " + date + " at " + date.getTime());
                        for (String s : message) {
                            pw.println(s);
                        }
                        for (StackTraceElement s : e.getStackTrace()) {
                            pw.println(s.toString());
                        }
                    }
                }
            } catch (IOException k) {
                Logger.logError("Error logging error file.", k, plugin);
            }
        });

    }

    public static void logInfo(String message, Main plugin) {
        plugin.runAsyncTask(() -> {
            Date date = new Date();
            String timeStamp = String.valueOf(date.getTime());
            File errorDir = new File(plugin.getDataFolder(), "infolog");
            if (errorDir.mkdirs()) {
                plugin.getLogger().info("Creating subdirectory \"infolog\"");
            }
            try {
                File errorFile = new File(errorDir, "info-log-" + timeStamp + ".txt");
                if (errorFile.createNewFile()) {
                    try (PrintWriter pw = new PrintWriter(errorFile)) {
                        pw.println(message);
                    }
                }
            } catch (IOException k) {
                Logger.logError("Error logging info file.", k, plugin);
            }
        });
    }

}
