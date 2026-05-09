package dev.revere.alley.bootstrap.internal;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.UpdaterService;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Remi
 * @project alley-practice
 * @date 24/07/2025
 */
@Service(provides = UpdaterService.class, priority = 1500)
public class UpdaterServiceImpl implements UpdaterService {
    private final LocaleService localeService;
    private final String currentVersion;

    private final String githubRepo = "revere-group/alley-practice";
    private String latestVersion;

    /**
     * DI Constructor for the UpdaterServiceImpl class.
     *
     * @param localeService The Locale service.
     */
    public UpdaterServiceImpl(LocaleService localeService) {
        this.localeService = localeService;
        this.currentVersion = AlleyPlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public void setup(AlleyContext context) {
        try {
            this.latestVersion = getLatestVersion();
        } catch (IOException e) {
            Logger.logException("Failed to fetch the latest version from GitHub", e);
        }
    }

    @Override
    public void initialize(AlleyContext context) {
        this.checkForUpdates();
    }

    @Override
    public void checkForUpdates() {
        AlleyPlugin.getInstance().getServer().getScheduler().runTaskAsynchronously(AlleyPlugin.getInstance(), () -> {
            try {
                if (latestVersion != null && !currentVersion.equals(latestVersion)) {
                    Logger.warn("New version available: " + latestVersion + " (Current: " + currentVersion + ")");

                    if (this.shouldAutoUpdate()) {
                        this.downloadAndUpdate(latestVersion);
                    }
                }
            } catch (Exception e) {
                Logger.logException("Failed to check for updates", e);
            }
        });
    }

    @Override
    public void downloadAndUpdate(String version) {
        try {
            String downloadUrl = "https://github.com/" + githubRepo + "/releases/download/v" + version + "/Alley-" + version + ".jar";

            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            File pluginFile = new File(AlleyPlugin.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            File pluginDirectory = pluginFile.getParentFile();

            File updateFile = new File(pluginDirectory, "Alley-" + version + ".jar");
            File tempFile = new File(pluginDirectory, "Alley-temp.jar");

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            if (pluginFile.delete()) {
                Logger.info("Successfully deleted old bootstrap file: " + pluginFile.getName());
            } else {
                Logger.warn("Failed to delete old bootstrap file: " + pluginFile.getName());
            }

            if (tempFile.renameTo(updateFile)) {
                Logger.info("Successfully updated bootstrap to version " + version);
            } else {
                Logger.warn("Failed to rename temporary file to bootstrap file: " + updateFile.getName());
            }

            Logger.info("Shutting down the server to apply the update. Please restart the server manually.");
            AlleyPlugin.getInstance().getServer().shutdown();
        } catch (Exception e) {
            Logger.logException("Failed to download and update to version " + version, e);
        }
    }

    @Override
    public String getLatestVersion() throws IOException {
        URL url = new URL("https://api.github.com/repos/" + githubRepo + "/releases/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        try (InputStream inputStream = connection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream)) {

            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(inputStreamReader).getAsJsonObject();
            return json.get("tag_name").getAsString().replace("v", "");
        }
    }

    /**
     * Determines if the plugin should auto-update based on the configuration setting.
     *
     * @return True if auto-update is enabled, false otherwise.
     */
    private boolean shouldAutoUpdate() {
        return this.localeService.getBoolean(SettingsLocaleImpl.AUTO_UPDATE_BOOLEAN);
    }
}
