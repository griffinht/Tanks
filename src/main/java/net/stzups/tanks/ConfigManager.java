package net.stzups.tanks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

public class ConfigManager {

    private static final Logger logger = java.util.logging.Logger.getLogger(Tanks.class.getName());

    private static Properties properties = new Properties();

    static void load() {

        try {
            try {
                properties.load(new FileInputStream("config.txt"));
            } catch (FileNotFoundException e) {
                InputStream inputStream = Tanks.class.getClassLoader().getResourceAsStream("config.txt");
                properties.load(inputStream);
            }
        } catch (IOException e) {
            logger.warning("");
            e.printStackTrace();
        }
    }

    public static String getConfigProperty(String property) {
        return properties.getProperty(property, "");
    }
}//todo error handling
