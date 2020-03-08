package net.stzups.tanks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class FileManager {
    static void load() {
        try (JarFile jarFile = new JarFile(new File(Tanks.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
            String root = Tanks.class.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
            root = root.substring(0, root.lastIndexOf("/")) + "/resources";
            Enumeration enums = jarFile.entries();

            while(enums.hasMoreElements()) {
                JarEntry entry = (JarEntry) enums.nextElement();
                File file = new File(entry.getName());

                if (file.toURI().toString().contains(root)) {
                    if (!file.exists()) {
                        if (file.getName().contains(".")) {
                            if (file.createNewFile()) {
                                try (InputStream inputStream = jarFile.getInputStream(entry);
                                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                                    while (inputStream.available() > 0) {
                                        fileOutputStream.write(inputStream.read());
                                    }
                                }
                            } else {
                                Logger.log("Couldn't create file at " + file.getAbsolutePath());
                            }
                        } else {
                            if (!file.mkdir()) {
                                Logger.log("Couldn't create folder at " + file.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }
}
