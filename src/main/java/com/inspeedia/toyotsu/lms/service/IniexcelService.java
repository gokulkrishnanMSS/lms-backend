package com.inspeedia.toyotsu.lms.service;

import com.inspeedia.toyotsu.lms.dto.SettingsDTO;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class IniexcelService {
    private final INIConfiguration iniConfiguration;
    public static Map<String, SettingsDTO> iniSettings = new HashMap<>();

    @Value("${ini.file-path}")
    String iniFilePath;

    @SuppressWarnings("unused")
    private IniexcelService(@Value("${ini.file-path}") String iniFilePath) throws IOException, ConfigurationException {
        this.iniFilePath = iniFilePath;
        iniConfiguration = new INIConfiguration();
//        FileHandler fileHandler = new FileHandler(iniConfiguration);
//
//        // Load the INI file
//        File iniFile = new File(iniFilePath);
//        if (!iniFile.exists()) {
//            throw new IOException("INI file not found at: " + iniFilePath);
//        }
//        fileHandler.load(iniFile);
//        loadIniSettings();

    }

    public String getValue(String section, String key) {
        if (iniConfiguration == null) {
            throw new IllegalStateException("INI file has not been loaded");
        }

        // Retrieve a value from a section and key
        return iniConfiguration.getString(section + "." + key);
    }

    private void loadIniSettings() {
        if (iniConfiguration == null) {
            throw new IllegalStateException("INI file has not been loaded");
        }
        iniSettings.clear();
        for (String section : iniConfiguration.getSections()) {
            String worker = iniConfiguration.getString(section + ".worker");
            String admin = iniConfiguration.getString(section + ".admin");
            String manager = iniConfiguration.getString(section + ".manager");
            String actualPeople = iniConfiguration.getString(section + ".actualPeople");
            String startTime = iniConfiguration.getString(section + ".startTime");
            String endTime = iniConfiguration.getString(section + ".endTime");
            SettingsDTO dto = new SettingsDTO(section, worker, admin, manager, actualPeople, startTime, endTime);
            iniSettings.put(section, dto);
        }
    }

    public SettingsDTO getIniSettings(String companyName) {
        return iniSettings.get(companyName);
    }

    public void updateINISettings(String companyName, SettingsDTO settings) {
        if (iniConfiguration == null) {
            throw new IllegalStateException("INI file has not been loaded");
        }

        iniConfiguration.setProperty(companyName + ".worker", settings.getWorkerName());
        iniConfiguration.setProperty(companyName + ".admin", settings.getAdminName());
        iniConfiguration.setProperty(companyName + ".manager", settings.getManagerName());
        iniConfiguration.setProperty(companyName + ".actualPeople", settings.getActualNoOfPeople());
        iniConfiguration.setProperty(companyName + ".startTime", settings.getWorkStartTime());
        iniConfiguration.setProperty(companyName + ".endTime", settings.getWorkEndTime());

        try (FileWriter writer = new FileWriter(iniFilePath)) {
            iniConfiguration.write(writer);
            loadIniSettings();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update the INI file", e);
        }
    }

}
