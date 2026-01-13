package org.example.kaos.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class UpdateManager {

    private static final Properties config = loadConfig();
    private static final String UPDATE_URL = config.getProperty("update.url");
    private static final String CURRENT_VERSION = config.getProperty("current.version");

    private static String latestVersion;
    private static String downloadUrl;

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream is = UpdateManager.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            // Handle error
        }
        return props;
    }

    public static boolean checkForUpdate() {
        if (UPDATE_URL == null || UPDATE_URL.isEmpty()) {
            return false;
        }
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(UPDATE_URL)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());
            latestVersion = json.get("version").asText();
            downloadUrl = json.get("url").asText();

            return isNewerVersion(latestVersion, CURRENT_VERSION);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        String[] l = latest.split("\\.");
        String[] c = current.split("\\.");
        for (int i = 0; i < Math.min(l.length, c.length); i++) {
            int lv = Integer.parseInt(l[i]);
            int cv = Integer.parseInt(c[i]);
            if (lv > cv) return true;
            if (lv < cv) return false;
        }
        return l.length > c.length;
    }

    public static String getLatestVersion() {
        return latestVersion;
    }

    public static void downloadUpdate() throws IOException, InterruptedException {
        if (downloadUrl == null) return;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build();
        Path tempFile = Paths.get(System.getProperty("java.io.tmpdir"), "kaos_update.exe");
        client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));

        // Replace the old exe with the new one
        String exePath = ProcessHandle.current().info().command().orElse(null);
        if (exePath != null) {
            Path exePathObj = Paths.get(exePath);
            Files.move(tempFile, exePathObj, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        restartApp();
    }

    private static void restartApp() {
        String exePath = ProcessHandle.current().info().command().orElse(null);
        if (exePath != null) {
            try {
                new ProcessBuilder(exePath).start();
                System.exit(0);
            } catch (Exception e) {
                // Log error
            }
        }
    }
}