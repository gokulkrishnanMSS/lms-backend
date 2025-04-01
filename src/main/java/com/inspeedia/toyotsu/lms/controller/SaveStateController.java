package com.inspeedia.toyotsu.lms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspeedia.toyotsu.lms.service.MinioService;
import com.inspeedia.toyotsu.lms.service.ProductionActivityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@RestController
@RequestMapping("/api/save-state")
public class SaveStateController {
    private final Logger log = LoggerFactory.getLogger(SaveStateController.class);
    private final MinioService minioService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProductionActivityService productionActivityService;

    public SaveStateController(MinioService minioService, ProductionActivityService productionActivityService) {
        this.minioService = minioService;
        this.productionActivityService = productionActivityService;
    }

    @PostMapping("/{department}/{date}")
    public ResponseEntity<Object> pushSavedState(@RequestBody Map<String, Object> savedState, @PathVariable String department, @PathVariable String date) {
        try {
            // Ensure "listOfStates" is parsed properly
            Object listOfStatesObj = savedState.get("listOfStates");

            if (listOfStatesObj instanceof String) {
                // If "listOfStates" is a string, parse it to a JSON object
                Map<String, Object> listOfStates = objectMapper.readValue((String) listOfStatesObj, new TypeReference<Map<String, Object>>() {
                });
                savedState.put("listOfStates", listOfStates); // Replace string with object
            }

            // Save to MinIO
            Object response = minioService.updateFile("save-state-" + department + "-" + date + ".json", objectMapper.writeValueAsString(savedState));

            return (Objects.nonNull(response))
                    ? ResponseEntity.status(HttpStatus.OK).body("Saved Successfully")
                    : ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing JSON: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{department}/{date}")
    public ResponseEntity<?> deleteSavedState(@PathVariable String department, @PathVariable String date) {
        try {
            String message = minioService.deleteFile("save-state-" + department + "-" + date + ".json");
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing JSON: " + e.getMessage());
        }
    }


    @GetMapping("/{department}/{date}")
    public ResponseEntity<Object> getSavedState(@PathVariable String department, @PathVariable String date) {
        try {
            Object response = minioService.getObject("save-state-" + department + "-" + date + ".json", Object.class);
            if (response != null)
                return ResponseEntity.ok(response);
            Map<String, List<Integer>> todayLineConfig = productionActivityService.getLineConfigByDepartmentAndDate(department, LocalDate.parse(date));
            if (todayLineConfig.isEmpty()) {
                LocalDate prevDate = LocalDate.parse(date).minusDays(1);
                Object prevDateResponse = minioService.getObject("save-state-" + department + "-" + prevDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".json", Object.class);
                return prevDateResponse == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(prevDateResponse);
            }
            else return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
