package com.inspeedia.toyotsu.lms.controller;


import com.inspeedia.toyotsu.lms.dto.LineConfigDTO;
import com.inspeedia.toyotsu.lms.dto.ProductionActivityDTO;
import com.inspeedia.toyotsu.lms.model.LineConfig;
import com.inspeedia.toyotsu.lms.model.ProductionActivity;
import com.inspeedia.toyotsu.lms.service.ProductionActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prod")
public class ProductionActivityController {
    private final Logger log = LoggerFactory.getLogger(SaveStateController.class);
    private final ProductionActivityService productionActivityService;

    public ProductionActivityController(ProductionActivityService productionActivityService) {
        this.productionActivityService = productionActivityService;
    }

    @PostMapping("/save")
    private ResponseEntity<?> save(@RequestBody List<ProductionActivityDTO> listOfActivities) {
        if (listOfActivities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body("Activity list is empty");
        }
        productionActivityService.saveAll(listOfActivities);
        return ResponseEntity.status(HttpStatus.OK).body("Production activities added successfully.");

    }
    @GetMapping("/{date}")
    private ResponseEntity<?> getProductionActivity(@PathVariable String date) {
        if (date.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Date is empty");
        }

        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            List<ProductionActivity> productionActivityList = productionActivityService.getActivityByDate(localDate);
            return ResponseEntity.status(HttpStatus.OK).body(productionActivityList);
        } catch (DateTimeParseException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    @PostMapping("/{department}/line-config/{date}")
    public ResponseEntity<?> writeProductionConfig(@PathVariable String department, @PathVariable LocalDate date, @RequestBody MultipartFile file){
        try {
            return ResponseEntity.ok(productionActivityService.setLineConfigByDepartment(file, department, date));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.badRequest().body("Can't upload line configuration");
        }
    }

    @GetMapping("/{department}/line-config/{date}")
    public ResponseEntity<Map<String, List<Integer>>> getLineConfigByDepartment(@PathVariable String department, @PathVariable LocalDate date){
        Map<String, List<Integer>> lineConfig = productionActivityService.getLineConfigByDepartmentAndDate(department, date);
        return lineConfig.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok().body(lineConfig);
    }

    @PostMapping("/{department}/line-config/compare")
    public ResponseEntity<Boolean> compareLineConfigByDepartment(@PathVariable String department
            , @RequestBody LineConfigDTO lineConfigDTO){
        return  ResponseEntity.ok().body(
                productionActivityService.compareLineConfigByDepartment(department, lineConfigDTO.getLineConfig())
        );
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<LineConfig>> getAllActivities(){
        return ResponseEntity.ok().body(
                productionActivityService.getAllActivity()
        );
    }
}
