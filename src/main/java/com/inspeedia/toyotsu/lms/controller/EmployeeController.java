package com.inspeedia.toyotsu.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspeedia.toyotsu.lms.dto.EmployeeDTO;
import com.inspeedia.toyotsu.lms.enums.EmployeeStatus;
import com.inspeedia.toyotsu.lms.exception.StorageFileNotFoundException;
import com.inspeedia.toyotsu.lms.model.Employee;
import com.inspeedia.toyotsu.lms.repository.EmployeeRepository;
import com.inspeedia.toyotsu.lms.service.EmployeeService;
import com.inspeedia.toyotsu.lms.service.ExcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/employee")
@SuppressWarnings("unused")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;
    private final ExcelService excelService;

    private final  EmployeeRepository employeeRepository;
    private final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    public EmployeeController(EmployeeService employeeService, ObjectMapper objectMapper, ExcelService excelService , EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
        this.excelService = excelService;
        this
                .employeeRepository = employeeRepository;
    }

    @GetMapping("/profile-image")
    public ResponseEntity<InputStreamResource> getWorkerImage(@RequestParam String objectName) {
        try{
        InputStreamResource inputStream = excelService.getImageFromMinio(objectName);
        HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "image/".concat(objectName.substring(objectName.lastIndexOf(".")+1 , objectName.length())));
            return ResponseEntity.ok().headers(headers).body(inputStream);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> postWorkerData(
            @RequestParam("file") MultipartFile image, @RequestParam("data") String employeeData,
            RedirectAttributes redirectAttributes) {
        try {
            EmployeeDTO employeeDto = objectMapper.readValue(employeeData, EmployeeDTO.class);
            Employee employee = employeeService.addOperator(employeeDto, image);
            return ResponseEntity.ok("Worker successfully added");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body("Error processing image or worker data.");
        }
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<String> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exc.getMessage());
    }

    @PostMapping("/edited")
    public ResponseEntity<?> isEmployeeEdited(@RequestBody MultipartFile file) throws IOException {
        if (Objects.equals(file.getContentType(), ExcelService.EXCEL_CONTENT_TYPE)) {
            List<String> depatmentList = excelService.checkTheEmployeePropertyChanges(file.getInputStream());
            if (depatmentList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK).body(false);
            }
            return ResponseEntity.status(HttpStatus.OK).body(depatmentList);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<Employee>> getAllData() {
        List<Employee> employeeList = excelService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(employeeList);
    }

    @GetMapping("/{department}/list")
    public ResponseEntity<List<Employee>> getAllData(@PathVariable String department) {
        List<Employee> employeeList = excelService.filterByDepartment(department);
        return ResponseEntity.status(HttpStatus.OK).body(employeeList);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllUser(){
        employeeRepository.deleteAll();
        return ResponseEntity.ok().body("deleted");
    }
}
