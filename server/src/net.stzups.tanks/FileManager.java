package net.stzups.tanks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

class FileManager {
    static void load() {
        URL url = Tanks.class.getResource("/resources/client/index.html");
        try (InputStream inputStream = url.openStream()) {
            File file = new File("client/");
            file.mkdir();
            file = new File("client/index.html");
            if (!file.exists()) {
                Files.copy(inputStream, Paths.get("client/index.html"));
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}
