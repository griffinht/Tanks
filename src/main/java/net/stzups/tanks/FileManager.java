package net.stzups.tanks;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class FileManager {

    private static final String SERVER_RESOURCES_PATH = "server";

    private final Map<String, File> cachedFiles = new HashMap<>();
    private final Map<String, byte[]> cachedFilesContents = new HashMap<>();

    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    static void load() {
        try (JarFile jarFile = new JarFile(new File(Tanks.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
            Enumeration enums = jarFile.entries();

            while(enums.hasMoreElements()) {
                JarEntry entry = (JarEntry) enums.nextElement();
                if (entry.getName().startsWith(SERVER_RESOURCES_PATH)) {
                    String path = entry.getName().substring(SERVER_RESOURCES_PATH.length());
                    File file = new File("./" + path);
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
                                logger.warning("Couldn't create file at " + file.getAbsolutePath());
                            }
                        } else if (!file.mkdir()) {
                            logger.warning("Couldn't create folder at " + file.getAbsolutePath());
                        }
                    } else if (file.isFile() && path.startsWith("/client/")) {
                        try (InputStream existingInputStream = new FileInputStream(file);
                        InputStream originalInputStream = jarFile.getInputStream(entry)) {
                            byte[] existingFile = new byte[existingInputStream.available()];
                            byte[] originalFile = new byte[originalInputStream.available()];
                            if ((originalFile.length > 0 && originalInputStream.read(originalFile) == -1)  || (existingFile.length > 0 && existingInputStream.read(existingFile) == -1)) {
                                throw new EOFException("Couldn't read while checking " + file.getAbsolutePath() + " with existing resources in jar");
                            }

                            if (!(existingFile.length == originalFile.length || Arrays.equals(existingFile, originalFile))) {
                                try (FileOutputStream fileOutputStream = new FileOutputStream(file, false)) {
                                    fileOutputStream.write(originalFile);
                                }
                            }
                        }
                    }
                }
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile(String request) {
        if (cachedFiles.containsKey(request)) {
            return cachedFiles.get(request);
        } else {
            File file = new File(request);
            cachedFiles.put(request, file);
            return file;
        }
    }

    public byte[] getFileContents(String request) {
        if (cachedFilesContents.containsKey(request)) {
            return cachedFilesContents.get(request);
        } else {
            File file = getFile(request);

            if (file.exists() && file.isFile()) {
                try (InputStream inputStream = new FileInputStream(file)) {
                    byte[] read = new byte[inputStream.available()];
                    if (inputStream.read(read) == -1) {
                        throw new EOFException();
                    } else {
                        cachedFilesContents.put(request, read);

                        return read;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return new byte[0];
    }
}
