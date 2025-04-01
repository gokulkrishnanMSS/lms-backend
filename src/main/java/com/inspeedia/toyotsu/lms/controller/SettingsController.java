package com.inspeedia.toyotsu.lms.controller;

import com.inspeedia.toyotsu.lms.dto.SettingsDTO;
import com.inspeedia.toyotsu.lms.dto.SettingsTypeDTO;
import com.inspeedia.toyotsu.lms.enums.EmployeeStatus;
import com.inspeedia.toyotsu.lms.model.Employee;
import com.inspeedia.toyotsu.lms.model.Line;
import com.inspeedia.toyotsu.lms.service.ExcelService;
import com.inspeedia.toyotsu.lms.service.IniexcelService;
import com.inspeedia.toyotsu.lms.service.MinioService;
import com.inspeedia.toyotsu.lms.service.ProductionActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final IniexcelService iniexcelService;
    private final ExcelService excelService;
    private final ProductionActivityService productionActivityService;
    private final MinioService minioService;

    public SettingsController(IniexcelService iniexcelService,
                              ExcelService excelService,
                              ProductionActivityService productionActivityService,
                              MinioService minioService) {
        this.iniexcelService = iniexcelService;
        this.excelService = excelService;
        this.productionActivityService = productionActivityService;
        this.minioService = minioService;
    }

    @PostMapping("/{department}")
    public ResponseEntity<Object> updateSettings(@RequestBody Object setting, @PathVariable String department) {
        Object response = minioService.updateFile("settings-" + department + ".json", setting);
        return (Objects.nonNull(response))
                ? ResponseEntity.status(HttpStatus.OK).body(response)
                : ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
    }

    @GetMapping("/{department}")
    public ResponseEntity<SettingsTypeDTO> getSettingsType(@PathVariable String department) {
        try {
            SettingsTypeDTO response = minioService.getObject("settings-" + department + ".json", SettingsTypeDTO.class);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
        }
    }

    @PostMapping(value = "/upload/line", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> excelReader(@RequestParam("file") MultipartFile file) throws IOException {
        if (Objects.equals(file.getContentType(), ExcelService.EXCEL_CONTENT_TYPE)) {
            List<Line> lineInfo = excelService.excelToLines(file.getInputStream());
            return lineInfo != null ?
                    ResponseEntity.status(HttpStatus.OK).body(lineInfo.toString())
                    : ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(null);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @PostMapping("/upload/employee/{date}")
    public ResponseEntity<?> uploadEmployee(@RequestBody MultipartFile file, @PathVariable LocalDate date) throws IOException {
        if (Objects.equals(file.getContentType(), ExcelService.EXCEL_CONTENT_TYPE)) {
            Map<EmployeeStatus,List<Employee>> employeeMap = excelService.excelToEmployee(file.getInputStream(), date);
            if (employeeMap.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body("Excel has no data");
            }
            return ResponseEntity.status(HttpStatus.OK).body(employeeMap);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @GetMapping("/ini/{company}")
    private ResponseEntity<?> readIni(@PathVariable("company") String name) {
        if (!IniexcelService.iniSettings.containsKey(name.toUpperCase())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company name not found in the ini file");
        }
        SettingsDTO settingsDTO = iniexcelService.getIniSettings(name.toUpperCase());
        return ResponseEntity.status(HttpStatus.OK).body(settingsDTO);
    }

    @PutMapping("/ini/{company}")
    private ResponseEntity<?> updateIni(@PathVariable("company") String name, @RequestBody SettingsDTO settings) {
        if (!IniexcelService.iniSettings.containsKey(name.toUpperCase())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Company name not found in ini the file");
        }
        iniexcelService.updateINISettings(name.toUpperCase(), settings);
        return ResponseEntity.status(HttpStatus.OK).body("Ini file updated successfully.");
    }

    @GetMapping("/line")
    private ResponseEntity<?> getSettings() {
        List<Line> lineList = productionActivityService.getLineSettings();
        return ResponseEntity.status(HttpStatus.OK).body(lineList);
    }

}
