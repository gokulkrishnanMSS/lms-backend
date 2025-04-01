package com.inspeedia.toyotsu.lms.controller;

import com.inspeedia.toyotsu.lms.enums.HeaderLanguage;
import com.inspeedia.toyotsu.lms.service.ExcelService;
import com.inspeedia.toyotsu.lms.service.ProductionActivityService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final ExcelService excelService;
    private final ProductionActivityService productionActivityService;

    public ExportController(ExcelService excelService, ProductionActivityService productionActivityService) {
        this.excelService = excelService;
        this.productionActivityService = productionActivityService;
    }

    @SuppressWarnings("unused")
    @GetMapping("/{department}")
    public ResponseEntity<?> exportActivityByDept(@PathVariable("department") String dept) {
        String fileName =LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + dept + ".xlsx";
        InputStreamResource file = new InputStreamResource(excelService.exportDeptWiseActivityData(productionActivityService.activityList(dept.toUpperCase())));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
    }

    @SuppressWarnings("unused")
    @GetMapping("/{department}/{line}/main")
    public ResponseEntity<?> exportMainScreenByLine(@PathVariable("line") String line, @PathVariable("department") String department) {
        String fileName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_" + department+line+"main" + ".xlsx";
        InputStreamResource file = new InputStreamResource(excelService.exportLineWiseMainScreenData(department.toUpperCase(), line));
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
    }

    @SuppressWarnings("unused")
    @GetMapping("/{department}/{line}/main/{startDate}")
    public ResponseEntity<?> exportMainScreenByLineAndDate(@PathVariable("department") String department, @PathVariable("line") String line, @PathVariable("startDate") String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = formattedDate + "_" +LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+"main" + ".xlsx";
            InputStreamResource file = new InputStreamResource(excelService.exportLineAndDateWiseMainScreenData(department.toUpperCase(), line, localDate));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    @SuppressWarnings("unused")
    @GetMapping("/{language}/{department}/{date}")
    public ResponseEntity<?> exportMainScreenByDate(@PathVariable("language") String language, @PathVariable("department") String dept, @PathVariable("date") String date) {
        if (date.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Date is empty");
        }
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = formattedDate + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "_main.xlsx";

            // Export the data and return the file
            InputStreamResource file = new InputStreamResource(excelService.exportDateWiseMainScreenData(HeaderLanguage.fromString(language), dept.toUpperCase(), localDate));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(file);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    @SuppressWarnings("unused")
    @GetMapping("/{department}/back-screen/{date}")
    public ResponseEntity<?> exportBackSreenByDate(@PathVariable("department") String dept, @PathVariable("date") String date) {
        if (date.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Date is empty");
        }
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String fileName = formattedDate + "_" +LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))+"back" + ".xlsx";
            InputStreamResource file = new InputStreamResource(excelService.exportDateWiseBackScreenData(dept.toUpperCase(), localDate));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName).contentType(MediaType.parseMediaType("application/vnd.ms-excel")).body(file);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }
}
