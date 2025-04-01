package com.inspeedia.toyotsu.lms.service;

import com.inspeedia.toyotsu.lms.enums.HeaderLanguage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ExcelExportService {
    private final ExcelService excelService;
    private final String department;
    private final String exportFolderPath;

    public ExcelExportService(ExcelService excelService, @Value("${app.department}") String department, @Value("${app.export-folder}")String exportFolderPath) {
        this.excelService = excelService;
        this.department = department;
        this.exportFolderPath = exportFolderPath;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void runTaskAtFixedRate() {
        LocalDate localDate = LocalDate.now();

        String formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = formattedDate + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_main.xlsx";

        // Export the data and return the file
        InputStreamResource file = new InputStreamResource(excelService.exportDateWiseMainScreenData(HeaderLanguage.ENGLISH, department.toUpperCase(), localDate));
    }
}
