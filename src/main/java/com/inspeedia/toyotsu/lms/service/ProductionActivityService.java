package com.inspeedia.toyotsu.lms.service;


import com.inspeedia.toyotsu.lms.dto.ProductionActivityDTO;
import com.inspeedia.toyotsu.lms.model.Employee;
import com.inspeedia.toyotsu.lms.model.LineConfig;
import com.inspeedia.toyotsu.lms.model.Line;
import com.inspeedia.toyotsu.lms.model.ProductionActivity;
import com.inspeedia.toyotsu.lms.repository.LineConfigRepository;
import com.inspeedia.toyotsu.lms.repository.EmployeeRepository;
import com.inspeedia.toyotsu.lms.repository.ProductionActivityRepository;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductionActivityService {

    private final ExcelService excelService;

    private final ProductionActivityRepository productionActivityRepository;

    private final LineConfigRepository lineConfigRepository;

    private final EmployeeRepository employeeRepository;

    private final MinioService minioService;


    public List<ProductionActivity> saveAll(List<ProductionActivityDTO> productionActivityDTOList) {
        List<ProductionActivity> listOfActivities = productionActivityDTOList.stream().map(this::convertToEntity).collect(Collectors.toList());

        for (ProductionActivity data : listOfActivities) {
            Optional<ProductionActivity> existingData = productionActivityRepository.findById(data.getId());
            if (existingData.isPresent()) {
                ProductionActivity activity = getActivity(data, existingData);
                productionActivityRepository.save(activity);
            } else {
                Optional<Employee> employee=employeeRepository.findById(data.getEmployee().getId());
                Employee supplier = data.getSupplier() != null ?
                        employeeRepository.findById(data.getSupplier().getId()).orElse(null) : null;
                employee.ifPresent(data::setEmployee);
                data.setSupplier(supplier);
                productionActivityRepository.save(data);
            }
        }

        return listOfActivities;
    }

    @NotNull
    private static ProductionActivity getActivity(ProductionActivity data, Optional<ProductionActivity> existingData) {
        ProductionActivity activity = existingData.get();
        activity.setSubstituteId(data.getSubstituteId());
        activity.setEmployee(data.getEmployee());
        activity.setSupplier(data.getSupplier() == null ? null : data.getSupplier().getId() != 0 ? data.getSupplier() : null);
        activity.setLine(data.getLine());
        activity.setDate(data.getDate());
        activity.setWorkStartTime(data.getWorkStartTime());
        activity.setWorkEndTime(data.getWorkEndTime());
        activity.setTotalWorkTime(data.getTotalWorkTime());
        activity.setNote(data.getNote());
        activity.setDepartment(data.getDepartment());
        return activity;
    }

    public List<Line> getLineSettings() {
        return ExcelService.lineSettings;
    }

    public List<ProductionActivity> activityList(String dept) {
        return productionActivityRepository.findProductionActivityByDepartment(dept);
    }

    public List<LineConfig> setLineConfigByDepartment(MultipartFile excelFile, String department, LocalDate date) throws IOException {
        Workbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        List<LineConfig> listOFActivities = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        lineConfigRepository.deleteByDepartmentAndDate(department, date);
        for (Row row : sheet) {
            if (row.getRowNum() == 0 || row.getRowNum() == 1) {
                continue;
            }
            for (int i = 0; i < (int) row.getCell(1).getNumericCellValue(); i++) {
                if (row.getCell(1).getCellType() == CellType.BLANK) {
                    continue;
                }
                LineConfig lineConfig = LineConfig.builder()
                        .supplier(i + 1)
                        .packerCount((int) row.getCell(2 + i).getNumericCellValue())
                        .lineName(row.getCell(0).getStringCellValue())
                        .date(date)
                        .department(department)
                        .build();
                listOFActivities.add(lineConfig);
                lineConfigRepository.save(lineConfig);
            }
        }

        minioService.deleteFile("save-state-"+department+"-"+date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+".json");
        return listOFActivities;
    }


    public List<LineConfig> getAllActivity() {
        return lineConfigRepository.findAll();
    }

    public Map<String, List<Integer>> getLineConfigByDepartment(String department) {
        return lineConfigRepository.findByDepartment(department)
                .stream().collect(Collectors.groupingBy(LineConfig::getLineName,
                        Collectors.mapping(LineConfig::getPackerCount, Collectors.toList())));
    }

    public Map<String, List<Integer>> getLineConfigByDepartmentAndDate(String department, LocalDate date) {
        return lineConfigRepository.findByDepartmentAndDate(department, date)
                .stream()
                .collect(Collectors.groupingBy(
                        LineConfig::getLineName,
                        LinkedHashMap::new,
                        Collectors.mapping(LineConfig::getPackerCount,
                                Collectors.toList())
                ));
    }

    public Boolean compareLineConfigByDepartment(String department, Map<String, List<Integer>> lineConfig) {
        Map<String, List<Integer>> prevLineConfig= lineConfigRepository.findByDepartment(department)
                .stream().collect(Collectors.groupingBy(LineConfig::getLineName,
                        Collectors.mapping(LineConfig::getPackerCount, Collectors.toList())));

        return areMapsDifferent(prevLineConfig, lineConfig);
    }

    public static boolean areMapsDifferent(Map<String, List<Integer>> map1, Map<String, List<Integer>> map2) {
        if (map1.size() != map2.size()) {
            return true; // Different sizes
        }

        for (String key : map1.keySet()) {
            if (!map2.containsKey(key)) {
                return true; // Different keys
            }

            List<Integer> list1 = map1.get(key);
            List<Integer> list2 = map2.get(key);

            if (!Objects.equals(list1, list2)) {
                return true; // Different values
            }
        }

        return false; // No differences
    }

    private ProductionActivity convertToEntity(ProductionActivityDTO dto) {
        ProductionActivity activity = new ProductionActivity();
        activity.setId(dto.getId());
        activity.setSubstituteId(dto.getSubstituteId());
        activity.setEmployee(dto.getEmployee());
        activity.setDepartment(dto.getDepartment());
        activity.setDate(LocalDate.parse(dto.getDate()));
        activity.setSupplier(dto.getSupplier());
        activity.setLine(dto.getLine());
        activity.setWorkStartTime(parseTime(dto.getWorkStartTime()));
        activity.setWorkEndTime(parseTime(dto.getWorkEndTime()));
        activity.setNote(dto.getNote());
        return activity;
    }

    private LocalTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss"); // 24-hour format with seconds
        LocalTime localTime = LocalTime.parse(time, formatter);
        return localTime;
    }

    public List<ProductionActivity> getActivityByDate(LocalDate localDate) {
        return productionActivityRepository.findProductionActivityByDate(localDate);
    }
}
