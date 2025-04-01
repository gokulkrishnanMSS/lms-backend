package com.inspeedia.toyotsu.lms.dto;
import lombok.Data;
import java.util.List;

@Data
public class SettingsTypeDTO {
    private String departmentName;
    private String workStartTime;
    private String workEndTime;
    private String language;
    private String plannedCount;
    private ColorPaletteDTO colorPalette;
    private String showAverageEfficiency;
}