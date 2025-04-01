package com.inspeedia.toyotsu.lms;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class EnvLoader {

    public static void loadEnv() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(new File(System.getProperty("user.dir"), "app.env"))) {
            properties.load(fis);
            properties.forEach((key, value) -> {
            });
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


}
