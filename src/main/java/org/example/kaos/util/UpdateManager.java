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
            HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(UPDATE_URL))
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Update check status: " + response.statusCode());
            System.out.println("Update check response: " + response.body());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.body());
            if (json.has("version") && json.has("url")) {
                latestVersion = json.get("version").asText();
                downloadUrl = json.get("url").asText();
                return isNewerVersion(latestVersion, CURRENT_VERSION);
            } else {
                System.out.println("JSON does not contain 'version' and 'url' fields");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error checking for update: " + e.getMessage());
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
        if (downloadUrl == null) {
            System.out.println("Download URL is null");
            return;
        }

        System.out.println("Downloading from: " + downloadUrl);

        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        // Download to Desktop
        String userHome = System.getProperty("user.home");
        Path desktopPath = Paths.get(userHome, "Desktop");
        Path updateFile = desktopPath.resolve("kaos_update.jar"); // Assuming it's a jar

        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(updateFile));

        System.out.println("Download status: " + response.statusCode());

        if (response.statusCode() != 200) {
            throw new IOException("Download failed with status: " + response.statusCode());
        }

        // Since we can't replace a running jar, just notify the user
        System.out.println("Update downloaded to: " + updateFile.toString());
        // Optionally, show a dialog or something, but since this is called from App, perhaps throw an exception with the path

        throw new IOException("Update downloaded to Desktop. Please replace the old kaos.jar manually and restart.");
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