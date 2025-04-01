package com.inspeedia.toyotsu.lms.service;

import java.io.*;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.inspeedia.toyotsu.lms.dto.EmployeeChangeResult;
import com.inspeedia.toyotsu.lms.enums.*;
import com.inspeedia.toyotsu.lms.model.Employee;
import com.inspeedia.toyotsu.lms.model.Line;
import com.inspeedia.toyotsu.lms.model.ProductionActivity;
import com.inspeedia.toyotsu.lms.repository.EmployeeRepository;
import com.inspeedia.toyotsu.lms.repository.ProductionActivityRepository;
import io.minio.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import org.apache.poi.xssf.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;


@Service
public class ExcelService {
    public static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final Logger log = LoggerFactory.getLogger(ExcelService.class);
    static String SHEET_SETTINGS = "LineDetails";
    static String SHEET_ACTIVITY = "Prod_Activity";
    public static final List<Line> lineSettings = new ArrayList<>();
    static String[] ROSTER_HEADERS = {"ID", "Employee", "Supplier", "Line", "Date", "WorkStartTime", "WorkEndTime", "TotalWorkTime", "note"};
    static String[] EXPORT_MAIN_HEADERS = {"Line", "08:30~09:30", "09:30~12:00", "13:00~15:00", "15:20~17:30", "out of time", "Supplier"};
    static String[] TIME_SLOTS = {"08:30~09:30", "09:30~12:00", "13:00~15:00", "15:20~17:30", "out of time"};
    private static final int ROW_GAP = 3;
    int rowCount = 0;
    private final MinioClient minioClient;
    private final EmployeeRepository employeeRepository;
    private final ProductionActivityRepository productionActivityRepository;
    private final EmployeeService employeeService;

    public ExcelService(MinioClient minioClient, EmployeeRepository employeeRepository, ProductionActivityRepository productionActivityRepository, EmployeeService employeeService) {
        this.minioClient = minioClient;
        this.employeeRepository = employeeRepository;
        this.productionActivityRepository = productionActivityRepository;
        this.employeeService = employeeService;
    }

    public List<Line> excelToLines(InputStream is) {
        try {
            lineSettings.clear();
            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET_SETTINGS);
            int rowIndex = 2;

            outerLoop:
            for (int i = rowIndex; i <= sheet.getLastRowNum(); i++) {
                Row currentRow = sheet.getRow(i);
                if (currentRow == null) continue; // Skip null rows

                int supplierCount = 0;
                Line line = new Line();
                for (int cellIdx = 0; cellIdx < currentRow.getLastCellNum(); cellIdx++) {

                    Cell currentCell = currentRow.getCell(cellIdx);
                    if (cellIdx == 0 && Objects.equals(currentCell.getStringCellValue(), "")) {
                        break outerLoop; // Break out of both loops
                    }

                    switch (cellIdx) {
                        case 0:
                            // Line Name
                            line.setName(currentCell.getStringCellValue());
                            break;

                        case 1:
                            // Supplier Count
                            if (currentCell != null && currentCell.getCellType() == CellType.NUMERIC) {
                                supplierCount = (int) currentCell.getNumericCellValue();
                                line.setSupplierCount(supplierCount);
                            }
                            break;

                        default:
                            //Packer Count
                            if (supplierCount > 0 && cellIdx - 2 < supplierCount) {
                                int supplierIndex = (cellIdx - 2) + 1; // Start Supplier indexing from 1
                                if (currentCell != null && currentCell.getCellType() == CellType.NUMERIC) {
                                    line.addOperatorCount(supplierIndex, (int) currentCell.getNumericCellValue());
                                }
                            }
                            break;
                    }

                }
                lineSettings.add(line);
            }
            workbook.close();
            return lineSettings;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            log.error(e.getMessage(), e);
        }

        return null;
    }

    public ByteArrayInputStream exportDeptWiseActivityData(List<ProductionActivity> productionActivityList) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET_ACTIVITY);
            CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true); // Make the font bold
            // font.setFontHeightInPoints((short) 12); // Set font size to 12 points


            headerStyle.setFont(font);
            headerStyle.setAlignment(HorizontalAlignment.CENTER); // Center align horizontally
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            //Header
            Row header = sheet.createRow(0);
            for (int col = 0; col < ROSTER_HEADERS.length; col++) {
                Cell cell = header.createCell(col);
                cell.setCellValue(ROSTER_HEADERS[col]);
                cell.setCellStyle(headerStyle);

            }
            int rowIdx = 1;
            for (ProductionActivity productionActivity : productionActivityList) {
                Row row = sheet.createRow(rowIdx++);

                // Create a centered cell style
                CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.CENTER); // Center align horizontally
                cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // Center align vertically

                // Create cells and apply the style
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(productionActivity.getId());
                cell0.setCellStyle(cellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(productionActivity.getEmployee() != null && productionActivity.getEmployee().getName() != null ? productionActivity.getEmployee().getName() : "");
                cell1.setCellStyle(cellStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(productionActivity.getSupplier() != null ? (productionActivity.getSupplier().getName()) : "");
                cell2.setCellStyle(cellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(productionActivity.getLine() != null ? productionActivity.getLine() : "");
                cell3.setCellStyle(cellStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(productionActivity.getDate() != null ? productionActivity.getDate().toString() : "");
                cell4.setCellStyle(cellStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(productionActivity.getWorkStartTime() != null ? productionActivity.getWorkStartTime().toString() : "");
                cell5.setCellStyle(cellStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(productionActivity.getWorkEndTime() != null ? productionActivity.getWorkEndTime().toString() : "");
                cell6.setCellStyle(cellStyle);

                Cell cell7 = row.createCell(7);
                //calculate total working time
                long totalTime = calculateTotalWorkTime(productionActivity.getDate(), productionActivity.getWorkStartTime(), productionActivity.getWorkEndTime());
                cell7.setCellValue(totalTime);
                cell7.setCellStyle(cellStyle);

                Cell cell8 = row.createCell(8);
                cell8.setCellValue(productionActivity.getNote() != null ? productionActivity.getNote() : "");
                cell8.setCellStyle(cellStyle);
            }

            for (int col = 0; col < ROSTER_HEADERS.length; col++) {
                sheet.autoSizeColumn(col);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("fail to import data to excel file: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportLineWiseMainScreenData(String department, String line) {
        rowCount = 0;
        try (Workbook workbook = createWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.getSheetAt(0);  // Create sheet and setup styles

            String[] timeSlots = {"08:30~09:30", "09:30~12:00", "13:00~15:00", "15:20~17:30", "out of time"};
            createHeaderRow(sheet, EXPORT_MAIN_HEADERS);

            // Fetch and filter production activities
            List<ProductionActivity> productionActivityList = productionActivityRepository.findProductionActivityByDepartmentAndLine(department, line);

            Map<String, List<ProductionActivity>> groupedBySupplier = productionActivityList.stream().collect(Collectors.groupingBy(productionActivity -> productionActivity.getSupplier().getName()));

            int rowIdx = 1;
            for (Map.Entry<String, List<ProductionActivity>> entry : groupedBySupplier.entrySet()) {
                List<ProductionActivity> filteredList = entry.getValue().stream().filter(productionActivity -> productionActivity.getSupplier() != productionActivity.getEmployee()).toList();
                fillActivityData(line, entry.getKey(), sheet, filteredList, timeSlots, rowIdx + rowCount);
                rowIdx += ROW_GAP;
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    public List<ProductionActivity> generateDummyProductionActivities(int count) {
        List<ProductionActivity> dummyData = new ArrayList<>();
        Random random = new Random();
        String[] departments = {"Assembly", "Welding", "Painting", "Quality Control"};
        String[] supplierNames = {"Supplier A", "Supplier B", "Supplier C", "Supplier D"};
        String[] lines = {"Line 1", "Line 2", "Line 3"};

        for (int i = 0; i < count; i++) {
            Employee employee = Employee.builder()
                    .id(i) // Auto-increment ID
                    .name("Employee " + (i + 1))
                    .department(departments[random.nextInt(departments.length)])
                    .companyName("Company XYZ")
                    .startDate(String.valueOf(LocalDate.now().minusMonths(random.nextInt(24))))
                    .employmentPeriod("2 years")
                    .qualityCases(random.nextInt(10))
                    .productivity(random.nextInt(1000))
                    .isActive(true)
                    .build();

            Employee supplier = Employee.builder()
                    .id(i) // Unique ID for suppliers
                    .name(supplierNames[random.nextInt(supplierNames.length)])
                    .department("Supplier Department")
                    .companyName("Supplier Co.")
                    .isActive(true)
                    .build();

            LocalTime startTime = LocalTime.of(8 + random.nextInt(8), random.nextInt(60));
            LocalTime endTime = startTime.plusHours(1 + random.nextInt(4)); // Work period between 1-4 hours

            ProductionActivity activity = new ProductionActivity();
            activity.setId(UUID.randomUUID().toString());
            activity.setEmployee(employee);
            activity.setSupplier(supplier);
            activity.setLine(lines[random.nextInt(lines.length)]);
            activity.setDate(LocalDate.now());
            activity.setWorkStartTime(startTime);
            activity.setWorkEndTime(endTime);
            activity.setDepartment(employee.getDepartment());
            activity.setNote("Production task " + (i + 1));

            dummyData.add(activity);
        }
        return dummyData;
    }

    public ByteArrayInputStream exportLineAndDateWiseMainScreenData(String department, String line, LocalDate date) {
        rowCount = 0;
        try (Workbook workbook = createWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.getSheetAt(0);  // Create sheet and setup styles

            String[] timeSlots = {"08:30~09:30", "09:30~12:00", "13:00~15:00", "15:20~17:30", "out of time"};
            createHeaderRow(sheet, EXPORT_MAIN_HEADERS);  // Create header row

            // Fetch and filter production activities
            List<ProductionActivity> productionActivityList = productionActivityRepository.findProductionActivityByDepartmentAndLineAndDate(department, line, date);

            Map<String, List<ProductionActivity>> groupedBySupplier = productionActivityList.stream().collect(Collectors.groupingBy(productionActivity -> productionActivity.getSupplier().getName()));

            int rowIdx = 1;
            for (Map.Entry<String, List<ProductionActivity>> entry : groupedBySupplier.entrySet()) {
                List<ProductionActivity> filteredList = entry.getValue().stream().filter(productionActivity -> productionActivity.getSupplier() != productionActivity.getEmployee()).toList();
                fillActivityData(line, entry.getKey(), sheet, filteredList, timeSlots, rowIdx + rowCount);
                rowIdx += ROW_GAP;
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    private void createARowAndInsertAData(Sheet sheet , int rowNumber , List<ProductionActivity> productionActivities , String LineName){
        Row row  = sheet.createRow(rowNumber);
        int rowIndex  = 0;
        for (ProductionActivity productionActivit : productionActivities){
            row.createCell(0).setCellValue(LineName);
            row.createCell(1).setCellValue(productionActivit.getEmployee().getName());
        }
    }
    public static void updateEmployeeShift(Map<String, List<String>> map, String key, String newEmployee) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(newEmployee);
    }
    private String getCorrectTimeSlot(LocalTime workStartTime ,  LocalTime workEndTime){
        for(String timeSlot : TIME_SLOTS){
            List<String> listOFtimeSlot = Arrays.asList(timeSlot.split("~"));
            if (listOFtimeSlot.size() < 2) {
                return null;
            }
            LocalTime actualStartTime =  LocalTime.parse(listOFtimeSlot.get(0));
            LocalTime actualEndTime = LocalTime.parse(listOFtimeSlot.get(0)).equals("out of time") ? LocalTime.MAX :   LocalTime.parse(listOFtimeSlot.get(1));
            if(workStartTime.isAfter(actualStartTime) && workEndTime.isBefore(actualEndTime)){
                return timeSlot;
            }
        }
        return null;
    }


    private List<Map<String , List<String>>> calculateTimeDuration(List<ProductionActivity> productionActivities , String lineName){
        Map<String , List<String>> respectiveEmployees  = new HashMap<>();
        Map<String , List<String>> respectiveNotes  = new HashMap<>();
        Map<String , List<String> >lineDurations = new HashMap<>();
        for(ProductionActivity productionActivity : productionActivities){
            Long slotDuration = 0l;
            LocalTime workStartTime = productionActivity.getWorkStartTime();
            LocalTime workEndTime = productionActivity.getWorkEndTime();
            for (String timeslot : TIME_SLOTS){
               Long duration =  calculateDurationInSlot(productionActivity , timeslot);
               if(duration != 0){
                   slotDuration+= duration;
                   updateEmployeeShift(respectiveEmployees , timeslot , productionActivity.getEmployee().getName());
                   updateEmployeeShift(lineDurations , timeslot , duration.toString());
                   updateEmployeeShift(respectiveNotes , timeslot , productionActivity.getNote());
               }
            }
        }
        return  List.of(respectiveEmployees , respectiveNotes , lineDurations);
    }

    private int getNumberOfColumntoMerge(Map<String , List<String> >employessName){
        int mergingValue = 0 ;
       return mergingValue;
    }

    private int createRowAndColumn(Sheet sheet , Map.Entry<String, Map<String, List<String>>> entities){
        int mergeValue =  0 ;
        Map<String ,List<String>> entity = entities.getValue();
        for (Map.Entry<String, List<String>> entry : entity.entrySet()){
            if(entry.getValue().size() > mergeValue){
                mergeValue = entry.getValue().size();
            }
        }
        return mergeValue;
    }

    private List<List<Integer>> mergeCellsAndLeaveGap(Sheet sheet , Map<String , Integer> gapEntities ,  Map<String ,
            Map<String , String>> totalNotes , Map<String , Map<String , Integer>> seperatedTime , Map<String , Set<String>> lineSupplier){

        int previousMergeCount =  1 ;
        int rowIndex = 1 ;
        int columnCount = 0;
        List<Integer> supplierStartIndex = new ArrayList<>();
        List<Integer> lineStartIndex = new ArrayList<>();
        for (Map.Entry<String , Integer> enity : gapEntities.entrySet()){
          int endColumn = (previousMergeCount+1) + (enity.getValue() +1);
            sheet.addMergedRegion(new CellRangeAddress(previousMergeCount , endColumn, 0 , 0));

            Row row = sheet.getRow(previousMergeCount);
            if(row == null){
                row =  sheet.createRow(previousMergeCount);
                Cell cell = row.getCell(0);
                if(cell == null){
                    cell = row.createCell(0);
                    cell.setCellValue(enity.getKey());
                    CellStyle style = sheet.getWorkbook().createCellStyle();
                    style.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    style.setAlignment(HorizontalAlignment.CENTER);
                    style.setVerticalAlignment(VerticalAlignment.CENTER);
                    style.setBorderTop(BorderStyle.THIN);
                    style.setBorderBottom(BorderStyle.THIN);
                    cell.setCellStyle(style);
                }
            }
            previousMergeCount =+ endColumn+1;
            supplierStartIndex.add(previousMergeCount);
            lineStartIndex.add(previousMergeCount);
            Row endRow =  sheet.getRow(endColumn-1);
            if (endRow == null){
                endRow = sheet.createRow(endColumn-1);
                for(String time : TIME_SLOTS){
                   Cell cell = endRow.createCell(columnCount+1);
                    CellStyle style = sheet.getWorkbook().createCellStyle();
                    style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    style.setAlignment(HorizontalAlignment.CENTER);
                    style.setVerticalAlignment(VerticalAlignment.CENTER);
                    style.setBorderTop(BorderStyle.THIN);
                    style.setBorderBottom(BorderStyle.THIN);
                    cell.setCellStyle(style);
                    String cellValue =  totalNotes.get(enity.getKey()).get(time);
                    cell.setCellValue(cellValue);
                    System.out.println(cell.getRow().getRowNum());
                    columnCount++;
                }
            }
            columnCount = 0;
            Row DurationRow = sheet.getRow(endColumn);
            if (DurationRow == null){
                DurationRow = sheet.createRow(endColumn);
                for(String time : TIME_SLOTS){
                    Cell cell = DurationRow.createCell(columnCount+1);
                    CellStyle style = sheet.getWorkbook().createCellStyle();
                    style.setFillForegroundColor(IndexedColors.BLUE.getIndex());
                    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    style.setAlignment(HorizontalAlignment.CENTER);
                    style.setVerticalAlignment(VerticalAlignment.CENTER);
                    style.setBorderTop(BorderStyle.THIN);
                    style.setBorderBottom(BorderStyle.THIN);
                    cell.setCellStyle(style);
                    Integer cellValue =  seperatedTime.get(enity.getKey()).get(time);
                    if (cellValue == null){
                        cell.setCellValue(0);
                    }else {
                        cell.setCellValue(cellValue);
                    }
                    System.out.println(cell.getRow().getRowNum());
                    columnCount++;
                }
            }
            columnCount = 0;
        }
        return List.of(lineStartIndex , supplierStartIndex);
    }

    private void insertValueInRow(Sheet sheet ,List<List<String>> entities  , int fromRow ){
        int rowIndex=  fromRow;
        int startingColumn = 1;
        for (List<String> innerList : entities){
                for (String names : innerList){
                    Row row = sheet.getRow(rowIndex);
                    if(row == null){
                        row = sheet.createRow(rowIndex);
                    }
                    Cell cell = row.getCell(startingColumn);
                    if (cell == null){
                        cell = row.createCell(startingColumn);
                    }
                    cell.setCellValue(names);
                    rowIndex++;
                }
                rowIndex = fromRow;
            startingColumn++;
        }
    }

    private void insertSupplier(Sheet sheet , List<Integer> supplierInsertInteger , Map<String , Set<String>> supplierNamesONline){
        int insertionRowIndex = 0;
        List<Integer> modifiedInteger =  supplierInsertInteger;
        modifiedInteger.add(0 , 1);
        modifiedInteger.remove(modifiedInteger.size()-1);
        for (Map.Entry<String, Set<String>> entry : supplierNamesONline.entrySet()){
            int insersionIndex = supplierInsertInteger.get(insertionRowIndex);
            for (String name : entry.getValue()){
                Row row = sheet.getRow(insersionIndex);
                if (row == null){
                    row = sheet.createRow(insersionIndex);
                }
                Cell cell = row.getCell(6);
                if (cell == null){
                    cell = row.createCell(6);
                }
                cell.setCellValue(name);
                insersionIndex++;
            }
            insertionRowIndex++;
        }
        return;
    }

    private  void drawSheet(Sheet sheet , Map<String ,  Map<String , List<String>>> writingActivity , Map<String ,
            Map<String , String>> totalNotes,Map<String , Map<String , Integer>> seperatedTime , Map<String , Set<String>> lineSupplier
    ){
        List<String> headers = new ArrayList<>(Arrays.asList(TIME_SLOTS));
        headers.add(0 , "L.NO");
        headers.add("Suppliers");
        String[] headersArray = headers.toArray(new String[0]);
        createHeaderRow(sheet , headersArray);
        Map<String , Integer> lineSize =  new HashMap<>();
        for (Map.Entry<String, Map<String, List<String>>> outerEntry : writingActivity.entrySet()) {
            lineSize.put( outerEntry.getKey() , createRowAndColumn(sheet , outerEntry));
        }
        int startColIndex = 0;
        List<List<Integer>> indexList =   mergeCellsAndLeaveGap(sheet , lineSize , totalNotes , seperatedTime , lineSupplier);
        List<Integer> lineIndexed =  indexList.get(0);
        List<Integer> supplierInsertionIndex = indexList.get(1);
        insertSupplier(sheet , supplierInsertionIndex , lineSupplier);
        lineIndexed.add(0 , 1);
        for (Map.Entry<String, Map<String, List<String>>> outerEntry : writingActivity.entrySet()) {
            List<List<String>> insertingEntity = new ArrayList<>();
            lineSize.put( outerEntry.getKey() , createRowAndColumn(sheet , outerEntry));
            Map<String, List<String>> innerMap = outerEntry.getValue();
            for (Map.Entry<String, List<String>> innerEntry: innerMap.entrySet()){
                    List<String> employeesName = innerEntry.getValue();
                    insertingEntity.add(employeesName);
            }
            insertValueInRow(sheet  ,  insertingEntity , lineIndexed.get(startColIndex));
            startColIndex++;
        }
    }

    private Map<String ,  Map<String , String>> seperateNotes( Map<String ,  Map<String , List<String>>> notes){
        Map<String ,  Map<String , String>> nonDuplicated  = new HashMap<>();
        for (Map.Entry<String, Map<String, List<String>>> outerEntry : notes.entrySet()) {
            Map<String, String> innerMap = new HashMap<>();
            for (Map.Entry<String , List<String >> innerEntiy: outerEntry.getValue().entrySet() ){
                String concadedNotes =  "";
                for (String timeNote : innerEntiy.getValue()){
                    if(!timeNote.isBlank()){
                        concadedNotes = concadedNotes.concat(timeNote);
                    }
                }
                innerMap.put(innerEntiy.getKey() , concadedNotes);
            }
            nonDuplicated.put(outerEntry.getKey() , innerMap);
        }
        return nonDuplicated;
    }

    private Map<String , Integer> getfinalizedTimeCalculation(  Map<String , List<String>> timeMap){
        Map<String , Integer> finalTimeMap = new HashMap<>();
        for (Map.Entry<String  , List<String>> outerEntity: timeMap.entrySet()) {
            int finalTime = 0;
            for (String timeNote : outerEntity.getValue()) {
                finalTime += Integer.parseInt(timeNote);
            }
            finalTimeMap.put(outerEntity.getKey(), finalTime);
        }
        return finalTimeMap;
    }
    public ByteArrayInputStream exportDateWiseMainScreenData(HeaderLanguage language, String dept, LocalDate startDate) {
        rowCount = 0;
        try (
                Workbook workbook = createWorkbook();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            workbook.createSheet(startDate.toString());
            Sheet sheet = workbook.getSheetAt(0);

            String[] timeSlots = {"08:30~09:30", "09:30~12:00", "13:00~15:00", "15:20~17:30", "out of time"};
            createHeaderRow(sheet, EXPORT_MAIN_HEADERS);  // Create header row
            List<ProductionActivity> productionActivityList = productionActivityRepository.findProductionActivityByDepartmentAndDate(dept, startDate);
            //List<ProductionActivity> productionActivityList = generateDummyProductionActivities(80);
            Map<String ,  Map<String , List<String>>> sepearationBasedOnTimeandLine = new HashMap<>();
            Map<String ,  Map<String , List<String>>> seperatedNotes = new HashMap<>();
            Map<String , Map<String , Integer>> seperatedTime = new HashMap<>();
            Map<String , Set<String>> lineSuplier = new HashMap<>();
            Map<String, List<ProductionActivity>> groupedByLine = productionActivityList.stream().filter(productionActivity -> productionActivity.getLine() != null && !productionActivity.getLine().isEmpty()).collect(Collectors.groupingBy(ProductionActivity::getLine));
            for (Map.Entry<String, List<ProductionActivity>> entryLine : groupedByLine.entrySet()) {
                Set<String> supliersName = new HashSet<>();
                List<ProductionActivity>  productionActivities =  entryLine.getValue();
                for (ProductionActivity prodAct : productionActivities){
                    supliersName.add(prodAct.getSupplier().getName());
                }
                lineSuplier.put(entryLine.getKey() , supliersName);
                List<Map<String , List<String>>> overAllCalulation = calculateTimeDuration(entryLine.getValue() , entryLine.getKey());
                Map<String , List<String>> lineSeperation = overAllCalulation.get(0);
                Map<String , Integer> durationBasedOnLine =  getfinalizedTimeCalculation(overAllCalulation.get(2));
                Map<String , List<String>> notes = overAllCalulation.get(1);
                sepearationBasedOnTimeandLine.put( entryLine.getKey() , lineSeperation);
                seperatedNotes.put( entryLine.getKey() , notes);
                seperatedTime.put(entryLine.getKey() , durationBasedOnLine);
            }
            Map<String ,  Map<String , String>> mapedNotes =  seperateNotes(seperatedNotes);
            drawSheet(sheet,  sepearationBasedOnTimeandLine , mapedNotes , seperatedTime , lineSuplier);

            List<ProductionActivity> backScreenActivities = productionActivityList.stream()
                    .filter(activity -> activity.getNote() != null && !activity.getNote().isEmpty())
                    .collect(Collectors.toList());
            Map<String, List<String>> backScreenData = formatBackScreenActivities(backScreenActivities, dept, language);
            int COL_GAP = 1;
            int backScreenStartCol = (1 + TIME_SLOTS.length + 1) + COL_GAP;
            writeBackScreenActivitiesToExcel(dept, language, backScreenData, sheet, 1, backScreenStartCol);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public InputStream exportDateWiseBackScreenData(String dept, LocalDate startDate) {
        try (Workbook workbook = createWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Create sheet and setup styles
            workbook.createSheet(LocalDate.now().toString());
            Sheet sheet = workbook.getSheetAt(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM EEEE", Locale.ENGLISH);
            String formattedDate = LocalDate.now().format(formatter);

            // Header row
            String[] headersBack = {formattedDate, ""};
            createHeaderRow(sheet, headersBack);

            // Get production activity and filter for back screen data
            List<ProductionActivity> backScreenList = productionActivityRepository
                    .findProductionActivityByDepartmentAndDate(dept, startDate)
                    .stream().filter(data -> data.getNote().contains("->")).toList();

            List<String> suddenLeave = new ArrayList<>();
            List<String> administrator = new ArrayList<>();
            List<String> exterior = new ArrayList<>();
            List<String> sorting = new ArrayList<>();
            List<String> tac = new ArrayList<>();
            List<String> others = new ArrayList<>();


            for (ProductionActivity data : backScreenList) {
                if (!data.getNote().contains("->"))
                    continue;
                String type = data.getNote().split("->")[1];
                String employeeName = data.getEmployee().getName();

                switch (type) {
                    case "break" -> suddenLeave.add(employeeName);
                    case "administrator" -> administrator.add(employeeName);
                    case "exterior" -> exterior.add(employeeName);
                    case "sorting" -> sorting.add(employeeName);
                    case "tac" -> tac.add(employeeName);
                    case "others" -> others.add(employeeName);
                    default -> {
                    }
                }
            }

            String[] rowLabels = {"有給休暇", "Sudden leave", "Administrator", "Exterior / Sorting", "tac", "Others"};
            List[] dataLists = new List[]{new ArrayList<>(), suddenLeave, administrator, Collections.singletonList(combineLists(exterior, sorting)), tac, others};

            // Populate sheet
            for (int row = 0; row < rowLabels.length; row++) {
                Row backRow = sheet.createRow(row + 1);
                backRow.setHeightInPoints(20);

                for (int col = 0; col < headersBack.length; col++) {
                    Cell dataCell = backRow.createCell(col);
                    if (col == 0) {
                        dataCell.setCellValue(rowLabels[row]);
                    } else {
                        String joinedData = String.join(", ", dataLists[row]);
                        dataCell.setCellValue(joinedData);
                    }
                }
            }

            // Auto-size columns
            for (int col = 0; col < headersBack.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }

    }

    private String combineLists(List<String> list1, List<String> list2) {
        String part1 = String.join(",", list1);
        String part2 = String.join(",", list2);
        if (part1.isEmpty() && part2.isEmpty()) return "";
        return part1 + " / " + part2;
    }


    private Workbook createWorkbook() {
        Workbook workbook = new XSSFWorkbook();

        // Header Style
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Data Cell Style
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        return workbook;
    }

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(20);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        for (int col = 0; col < headers.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(headers[col]);
            cell.setCellStyle(headerStyle);
        }
        for (int col = 0; col < headers.length; col++) {
            int width = headers[col].length() * 600; // Customize multiplier
            sheet.setColumnWidth(col, width);
        }
    }

    private static int getNoteTimeSlotIndex(String note, LocalTime workStartTime, LocalTime workEndTime) {
        boolean hasIn = note.contains("in");
        boolean hasOut = note.contains("out");

        if (hasIn || (hasIn && hasOut)) {
            return getTimeSlotIndex(workStartTime);
        }

        if (hasOut && !hasIn) {
            return getTimeSlotIndex(workEndTime);
        }
        return -1;
    }

    private static int getTimeSlotIndex(LocalTime time) {
        for (int i = 0; i < TIME_SLOTS.length; i++) {
            if (isTimeInSlot(time, TIME_SLOTS[i])) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isTimeInSlot(LocalTime time, String timeSlot) {
        String[] times = timeSlot.split("~");
        if (times.length < 2) {
            return false;
        }

        LocalTime slotStart = LocalTime.parse(times[0]);
        LocalTime slotEnd = times[1].equals("out of time") ? LocalTime.MAX : LocalTime.parse(times[1]);

        return !time.isBefore(slotStart) && !time.isAfter(slotEnd);
    }

    private static boolean isInTimeSlot(ProductionActivity activity, String timeSlot) {
        String[] times = timeSlot.split("~");
        if (times.length < 2) {
            return false;
        }
        LocalTime slotStart = LocalTime.parse(times[0]);
        LocalTime slotEnd = times[1].equals("out of time") ? LocalTime.MAX : LocalTime.parse(times[1]);
        LocalTime workStart = activity.getWorkStartTime();
        LocalTime workEnd = activity.getWorkEndTime();
        return !(workEnd.isBefore(slotStart) || workStart.isAfter(slotEnd));
    }

    public static List<String[]> formatMainActivities(List<ProductionActivity> activityList) {
        Map<String, String[]> rowMap = new LinkedHashMap<>();
        String[] notesRow = new String[TIME_SLOTS.length];
        String[] timeRow = new String[TIME_SLOTS.length];
        long[] timeSlotDurations = new long[TIME_SLOTS.length];

        for (ProductionActivity activity : activityList) {
            // Skip if supplier_id and employee_id are the same
            if (activity.getSupplier().getId() == activity.getEmployee().getId()) {
                continue;
            }

            String rowKey = activity.getLine() + "-" + (Objects.equals(activity.getSubstituteId(), "")
                    ? activity.getId()
                    : activity.getSubstituteId());

            // Initialize row if not present
            rowMap.putIfAbsent(rowKey, initializeRow());

            String[] row = rowMap.get(rowKey);

            for (int i = 0; i < TIME_SLOTS.length; i++) {
                if (isInTimeSlot(activity, TIME_SLOTS[i])) {
                    if (row[i] == null || row[i].isEmpty()) {
                        row[i] = activity.getEmployee().getName();
                    } else {
                        row[i] += " / " + activity.getEmployee().getName();
                    }
                    long duration = calculateDurationInSlot(activity, TIME_SLOTS[i]);
                    timeSlotDurations[i] += duration;
                }
            }

            // Fill the correct time slot with the notes
            if (activity.getNote() != null && !activity.getNote().isEmpty()) {
                int noteSlotIndex = getNoteTimeSlotIndex(activity.getNote(), activity.getWorkStartTime(), activity.getWorkEndTime());

                if (noteSlotIndex != -1) {
                    if (notesRow[noteSlotIndex] == null || notesRow[noteSlotIndex].isEmpty()) {
                        notesRow[noteSlotIndex] = activity.getNote();
                    } else {
                        notesRow[noteSlotIndex] += " \n " + activity.getNote(); // Concatenate multiple notes
                    }
                }
            }

            for (int i = 0; i < TIME_SLOTS.length; i++) {
                if (timeSlotDurations[i] > 0) {
//                    long hours = timeSlotDurations[i] / 60;
//                    long minutes = timeSlotDurations[i] % 60;
//                    timeRow[i] = String.format("%02d:%02d", hours, minutes);
                    timeRow[i] = String.valueOf(timeSlotDurations[i]);
                } else {
                    timeRow[i] = "0"; // Default if no duration
                }
            }
        }

        List<String[]> result = new ArrayList<>(rowMap.values());
        result.add(notesRow);
        result.add(timeRow);
        return result;
    }

    private static String[] initializeRow() {
//        String[] row = new String[TIME_SLOTS.length]; // +2 for Line and Supplier columns
//        row[0] = activity.getLine(); // Line name
//        row[row.length - 1] = activity.getSupplier().getName(); // Supplier name
        return new String[TIME_SLOTS.length];
    }

    private static long calculateDurationInSlot(ProductionActivity activity, String timeSlot) {
        String[] times = timeSlot.split("~");
        if (times.length < 2) {
            return 0;
        }

        LocalTime slotStart = LocalTime.parse(times[0]);
        LocalTime slotEnd = times[1].equals("out of time") ? LocalTime.MAX : LocalTime.parse(times[1]);

        LocalTime workStart = activity.getWorkStartTime();
        LocalTime workEnd = activity.getWorkEndTime();

        // Get the effective start and end time within the slot
        LocalTime effectiveStart = workStart.isBefore(slotStart) ? slotStart : workStart;
        LocalTime effectiveEnd = workEnd.isAfter(slotEnd) ? slotEnd : workEnd;

        // Calculate duration if within the slot
        if (!effectiveStart.isAfter(effectiveEnd)) {
            return Duration.between(effectiveStart, effectiveEnd).toMinutes();
        }
        return 0;
    }

    public void writeMainActivitiesToExcel(List<String[]> data, Sheet sheet, int startRow, int startCol) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle timeCalcRowStyle = workbook.createCellStyle();
        XSSFColor accent4Color = new XSSFColor(new byte[]{(byte) 15, (byte) 158, (byte) 213}, null);
        ((XSSFCellStyle) timeCalcRowStyle).setFillForegroundColor(accent4Color);
        timeCalcRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        timeCalcRowStyle.setBorderTop(BorderStyle.THIN);
        timeCalcRowStyle.setBorderBottom(BorderStyle.THIN);
        timeCalcRowStyle.setBorderLeft(BorderStyle.THIN);
        timeCalcRowStyle.setBorderRight(BorderStyle.THIN);
        timeCalcRowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        timeCalcRowStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle notesRowStyle = workbook.createCellStyle();
        notesRowStyle.setBorderTop(BorderStyle.THIN);
        notesRowStyle.setBorderBottom(BorderStyle.THIN);
        notesRowStyle.setBorderLeft(BorderStyle.THIN);
        notesRowStyle.setBorderRight(BorderStyle.THIN);
        notesRowStyle.setWrapText(true);
        try {
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.getRow(startRow + i);
                if (row == null) {
                    row = sheet.createRow(startRow + i);
                }

                String[] rowData = data.get(i);
                for (int j = 0; j < rowData.length; j++) {
                    Cell cell = row.getCell(startCol + j);
                    if (cell == null) {
                        cell = row.createCell(startCol + j);
                    }

                    if (rowData[j] != null && !rowData[j].isEmpty()) {
                        cell.setCellValue(rowData[j]);
                    } else {
                        cell.setCellValue("");
                    }
                    if (i == data.size() - 1) { // Notes row (second-last row)
                        cell.setCellStyle(timeCalcRowStyle);
                    } else if (i == data.size() - 2) {
                        cell.setCellStyle(notesRowStyle);
                    } else {
                        cell.setCellStyle(cellStyle);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void fillActivityData(String lineName, String supplierName, Sheet sheet, List<ProductionActivity> activities, String[] timeSlots, int rowIdx) {
        List<String[]> mainScreenData = formatMainActivities(activities);
        rowIdx += 1;
        Workbook workbook = sheet.getWorkbook();
        CellStyle SuppliserCellStyle = workbook.createCellStyle();
        SuppliserCellStyle.setAlignment(HorizontalAlignment.CENTER);
        SuppliserCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        CellStyle LineNameCellStyle = workbook.createCellStyle();
        LineNameCellStyle.setAlignment(HorizontalAlignment.CENTER);
        LineNameCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFColor accent6GreenColor = new XSSFColor(new byte[]{(byte) 198, (byte) 224, (byte) 180}, null); // Green, Accent 6, Lighter 80%
        ((XSSFCellStyle) LineNameCellStyle).setFillForegroundColor(accent6GreenColor);
        LineNameCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Row row = sheet.createRow(rowIdx);
        Cell lineCell = row.createCell(0);
        lineCell.setCellStyle(LineNameCellStyle);
        lineCell.setCellValue(lineName);
        Cell supplierCell = row.createCell(TIME_SLOTS.length + 1);
        supplierCell.setCellStyle(SuppliserCellStyle);
        supplierCell.setCellValue(supplierName);
        writeMainActivitiesToExcel(mainScreenData, sheet, rowIdx, 1);
        rowCount += mainScreenData.size();
        if (!activities.isEmpty()) {
            sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx + mainScreenData.size() - 1, 0, 0));
        }
        for (int i = 0; i <= timeSlots.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public static Map<String, String> getTranslatedHeaders(String department, HeaderLanguage language) {
        Map<String, String> translatedHeaders = new LinkedHashMap<>();

        switch (Department.valueOf(department.toUpperCase())) {
            case ARMS:
                for (BackScreenHeadersARMS header : BackScreenHeadersARMS.values()) {
                    translatedHeaders.put(header.getName(), header.getTranslation(language));
                }
                break;
            case PROCAST:
                for (BackScreenHeadersPROCAST header : BackScreenHeadersPROCAST.values()) {
                    translatedHeaders.put(header.getName(), header.getTranslation(language));
                }
                break;
            case TLS:
                for (BackScreenHeadersTLS header : BackScreenHeadersTLS.values()) {
                    translatedHeaders.put(header.getName(), header.getTranslation(language));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid department: " + department);
        }
        return translatedHeaders;
    }


    private Map<String, List<String>> formatBackScreenActivities(List<ProductionActivity> activities, String department, HeaderLanguage language) {
        Map<String, List<String>> backScreenData = new LinkedHashMap<>();
        Map<String, String> translatedHeaders = getTranslatedHeaders(department, language);
        for (String headerKey : translatedHeaders.keySet()) {
            backScreenData.put(headerKey, new ArrayList<>());
        }
        for (ProductionActivity data : activities) {
            if (data.getNote() == null || !data.getNote().contains("->")) {
                continue;
            }
            String[] noteParts = data.getNote().split("->");
            if (noteParts.length < 2) {
                continue;
            }
            String type = noteParts[1];
            String headerKey = translatedHeaders.containsKey(type) ? type : "others";
            List<String> employeeList = backScreenData.computeIfAbsent(headerKey, k -> new ArrayList<>());
            employeeList.add(data.getEmployee().getName());
        }
        return backScreenData;
    }

    public void writeBackScreenActivitiesToExcel(String department, HeaderLanguage language, Map<String, List<String>> data, Sheet sheet, int startRow, int startCol) {
        CellStyle headerStyle = getBackScreenCellStyle(sheet);
        Map<String, String> translatedHeaders = getTranslatedHeaders(department, language);
        int rowIndex = startRow;
        for (var headerKey : data.keySet()) {
            List<String> employeeList = data.get(headerKey);
            String translatedHeader = translatedHeaders.getOrDefault(headerKey, headerKey); // Fallback if not found

            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            Cell headerCell = row.createCell(startCol);
            headerCell.setCellValue(translatedHeader); // Write translated header
            headerCell.setCellStyle(headerStyle); // Apply header style

            Cell valueCell = row.createCell(startCol + 1);
            valueCell.setCellValue(String.join(", ", employeeList)); // Concatenate names with commas
            rowIndex++;
        }

        sheet.autoSizeColumn(startCol);
        sheet.autoSizeColumn(startCol + 1);
    }


    @NotNull
    private static CellStyle getBackScreenCellStyle(Sheet sheet) {
        Workbook workbook = sheet.getWorkbook();

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    @Nullable
    private static LocalTime getNoteTime(String note) {
        if (note == null || note.isEmpty()) {
            return null;
        }

        if ((note.contains("時in") && !note.contains("時out")) || (note.contains("時out") && !note.contains("時in"))) {
            String timePattern = "\\d{2}:\\d{2}"; // Matches "HH:mm"
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(timePattern);
            java.util.regex.Matcher matcher = pattern.matcher(note);

            if (matcher.find()) {
                String timeStr = matcher.group(0); // Extract the matched time string
                try {
                    return LocalTime.parse(timeStr); // Parse the time string
                } catch (Exception e) {
                    // Log or handle invalid time format
                    System.err.println("Invalid time format in note: " + note);
                    return null;
                }
            }
        }

        return null;
    }

    private void addNote(Matcher matcher, LocalTime rangeStart, LocalTime rangeEnd,
                         int index, int colIdx, Map<Integer, List<String>> notes, String note) {
        String time = matcher.group(4);

        LocalTime moveTime = LocalTime.parse(time);
        LocalTime adjustedStart = rangeStart;

        if (isMoveTimeInRange(moveTime)) {
            if (index == 2) {
                adjustedStart = rangeStart.minusHours(1);
            } else if (index == 3) {
                adjustedStart = rangeStart.minusMinutes(20);
            }
        }
        if (!moveTime.isBefore(adjustedStart) && !moveTime.isAfter(rangeEnd)) {
            notes.computeIfAbsent(colIdx, k -> new ArrayList<>()).add(note);
        }
    }

    public boolean isMoveTimeInRange(LocalTime moveTime) {
        LocalTime range1Start = LocalTime.of(12, 1);
        LocalTime range1End = LocalTime.of(12, 59);

        LocalTime range2Start = LocalTime.of(15, 1);
        LocalTime range2End = LocalTime.of(15, 19);

        return (!moveTime.isBefore(range1Start) && !moveTime.isAfter(range1End)) || (!moveTime.isBefore(range2Start) && !moveTime.isAfter(range2End));
    }

    private EmployeeChangeResult getValue(Row row, List<XSSFShape> picList, LocalDate date) {
        // Extract ID from the Excel row
        int id = convertCellToInt(row.getCell(0));

        // Check if the employee exists in the database
        Optional<Employee> existingEmployeeOpt = employeeRepository.findById(id);

        if (existingEmployeeOpt.isPresent()) {
            Employee existingEmployee = existingEmployeeOpt.get();

            // Make a deep copy of the existing employee before modification
            Employee previousState = new Employee(existingEmployee);

            // Update fields of the existing employee
            updateEmployeeFields(existingEmployee, row, picList);

            // Compare if any field has actually changed
            if (!isEmployeeEqual(previousState, existingEmployee)) {
                // Save the updated employee
                employeeRepository.save(existingEmployee);
                employeeService.updateEmployeeInExistingData(existingEmployee, date);
                if (previousState.getIsActive() && checkDepartmentChanges(previousState, row).isEmpty()) {
                    return new EmployeeChangeResult(EmployeeStatus.EDITED, existingEmployee);
                } else {
                    employeeService.addNewEmployeeIntoExistingData(existingEmployee.getDepartment(), existingEmployee, date);
                    return new EmployeeChangeResult(EmployeeStatus.ADDED, existingEmployee);
                }
            } else {
                return new EmployeeChangeResult(EmployeeStatus.NOTEDITED, existingEmployee);
            }

        }

        // If employee does not exist, create a new one
        Employee newEmployee = new Employee();
        newEmployee.setId(id);
        updateEmployeeFields(newEmployee, row, picList);

        employeeService.addNewEmployeeIntoExistingData(newEmployee.getDepartment(), newEmployee, date);

        // Save new employee and return it
        employeeRepository.save(newEmployee);
        return new EmployeeChangeResult(EmployeeStatus.ADDED, newEmployee);
    }

    private boolean isEmployeeEqual(Employee oldEmp, Employee newEmp) {
        return Objects.equals(oldEmp.getName(), newEmp.getName()) && Objects.equals(oldEmp.getDepartment(), newEmp.getDepartment()) && Objects.equals(oldEmp.getCompanyName(), newEmp.getCompanyName()) && Objects.equals(oldEmp.getMainProcess(), newEmp.getMainProcess()) && Objects.equals(oldEmp.getSubProcess(), newEmp.getSubProcess()) && Objects.equals(oldEmp.getStartDate(), newEmp.getStartDate()) && Objects.equals(oldEmp.getEmploymentPeriod(), newEmp.getEmploymentPeriod()) && Objects.equals(oldEmp.getQualityCases(), newEmp.getQualityCases()) && Objects.equals(oldEmp.getProductivity(), newEmp.getProductivity()) && Objects.equals(oldEmp.getSuitableArea(), newEmp.getSuitableArea()) && Objects.equals(oldEmp.getGroupId(), newEmp.getGroupId()) && Objects.equals(oldEmp.getIsActive(), newEmp.getIsActive()) && Objects.equals(oldEmp.getPhoto(), newEmp.getPhoto()) && Objects.equals(oldEmp.getSkillA(), newEmp.getSkillA()) && Objects.equals(oldEmp.getSkillB(), newEmp.getSkillB()) && Objects.equals(oldEmp.getSkillC(), newEmp.getSkillC()) && Objects.equals(oldEmp.getSkillD(), newEmp.getSkillD()) && Objects.equals(oldEmp.getSkillE(), newEmp.getSkillE()) && Objects.equals(oldEmp.getSkillF(), newEmp.getSkillF());
    }

    private void ConvertCellToDate(int column, Cell dateCell, Row row, Employee employee) {
        if (dateCell != null) {
            if (dateCell.getCellType() == CellType.STRING) {
                employee.setStartDate(row.getCell(column).getStringCellValue());
            } else if (dateCell.getCellType() == CellType.NUMERIC) {
                Date date = row.getCell(column).getDateCellValue();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                employee.setStartDate(sdf.format(date));
            } else {
                employee.setStartDate(null);
            }
        } else {
            System.out.println("Warning: Date cell is null at row " + row.getRowNum());
        }
    }

    private int convertCellToInt(Cell dateCell) {
        try {
            if (dateCell != null) {
                if (dateCell.getCellType() == CellType.STRING) {
                    String data = dateCell.getStringCellValue();
                    return Integer.parseInt(data);
                }
                if (dateCell.getCellType() == CellType.NUMERIC) {
                    return (int) dateCell.getNumericCellValue();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    private long convertCellToLong(Cell dateCell) {
        try {
            if (dateCell != null) {
                switch (dateCell.getCellType()) {
                    case STRING:
                        String data = dateCell.getStringCellValue();
                        return Long.parseLong(data);
                    case NUMERIC:
                        return (long) dateCell.getNumericCellValue();
                    case FORMULA:
                        return switch (dateCell.getCachedFormulaResultType()) {
                            case NUMERIC -> (long) dateCell.getNumericCellValue();
                            case STRING -> {
                                String formulaResult = dateCell.getStringCellValue();
                                yield Long.parseLong(formulaResult);
                            }
                            default -> {
                                log.warn("Unsupported formula result type: " + dateCell.getCachedFormulaResultType());
                                yield 0L;
                            }
                        };
                    default:
                        log.warn("Unsupported cell type: " + dateCell.getCellType());
                        return 0L;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    private String convertCellToString(Cell dateCell) {
        try {
            if (dateCell != null) {
                if (dateCell.getCellType() == CellType.STRING) {
                    return dateCell.getStringCellValue();
                }
                if (dateCell.getCellType() == CellType.NUMERIC) {
                    int data = (int) dateCell.getNumericCellValue();
                    return String.valueOf(data);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    private void updateEmployeeFields(Employee employee, Row row, List<XSSFShape> picList) {
        employee.setName(convertCellToString(row.getCell(1)));
        employee.setDepartment(convertCellToString(row.getCell(2)));
        employee.setCompanyName(convertCellToString(row.getCell(3)));
        // Handle photo upload
        InputStream inputStream = getPicData(picList, row.getRowNum());
        if (inputStream != null) {
            employee.setPhoto(postImageToMinio(inputStream, employee.getId(), employee.getDepartment(), employee.getPhoto()));
        } else {
            employee.setPhoto(null);
        }
        if (!employee.getIsActive()) {
            employee.setIsActive(true);
        }
        employee.setMainProcess(convertCellToString(row.getCell(5)));
        employee.setSubProcess(convertCellToString(row.getCell(6)));
        Cell dateCell = row.getCell(7);
        if (dateCell != null) {
            if (dateCell.getCellType() == CellType.STRING) {
                employee.setStartDate(row.getCell(7).getStringCellValue());
            } else if (dateCell.getCellType() == CellType.NUMERIC) {
                Date date = row.getCell(7).getDateCellValue();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                employee.setStartDate(sdf.format(date));
            } else {
                employee.setStartDate(null);
            }
        } else {
            System.out.println("Warning: Date cell is null at row " + row.getRowNum());
        }
        employee.setEmploymentPeriod(convertCellToString(row.getCell(8)));
        employee.setQualityCases(convertCellToInt(row.getCell(9)));
        long productivity = convertCellToLong(row.getCell(10));
        employee.setProductivity(Math.max(productivity, 0L));
        employee.setSuitableArea(checkTheSuitableValueForSuitedArea(convertCellToString(row.getCell(11))));
        employee.setGroupId(convertCellToInt(row.getCell(12)));
        employee.setSkillA(convertCellToInt(row.getCell(13)));
        employee.setSkillB(convertCellToInt(row.getCell(14)));
        employee.setSkillC(convertCellToInt(row.getCell(15)));
        employee.setSkillD(convertCellToInt(row.getCell(16)));
        employee.setSkillE(convertCellToInt(row.getCell(17)));
        employee.setSkillF(convertCellToInt(row.getCell(18)));
    }

    public String checkTheSuitableValueForSuitedArea(String suitedArea) {
        List<String> splitValues = Arrays.asList(suitedArea.split(""));
        int size = splitValues.size();
        if (size > 3) {
            return null;
        } else {
            StringBuilder result = new StringBuilder();
            for (String name : splitValues) {
                if (name.equals("A") || name.equals("B") || name.equals("C") || name.equals("D") || name.equals("/")) {
                    result.append(name);
                }
            }
            return result.toString();
        }
    }

    private List<String> checkDepartmentChanges(Employee existingEmployee, Row row) {
        List<String> changedDepartments = new ArrayList<>();

        String newDepartment = convertCellToString(row.getCell(2));
        String newMainProcess = convertCellToString(row.getCell(5));
        String newSubProcess = convertCellToString(row.getCell(6));
        int newGroupId = convertCellToInt(row.getCell(12));

        // Check if any of the fields have changed
        if (!existingEmployee.getDepartment().equals(newDepartment) || !existingEmployee.getMainProcess().equals(newMainProcess) || !existingEmployee.getSubProcess().equals(newSubProcess) || !Objects.equals(existingEmployee.getGroupId(), newGroupId)) {
            changedDepartments.add(existingEmployee.getDepartment());
        }

        return changedDepartments;
    }

    private InputStream getPicData(List<XSSFShape> shapes, int targetRow) {
        return shapes.stream().filter((shape) -> {
            if (shape instanceof XSSFPicture picture) {
                XSSFClientAnchor anchor = picture.getClientAnchor();
                int row = anchor.getRow1();
                int col = anchor.getCol1();
                int SHEET_PHOTO_COLUMN = 4;
                return row == targetRow && col == SHEET_PHOTO_COLUMN;
            }
            return false;
        }).map((shape) -> {
            XSSFPicture picture = (XSSFPicture) shape;
            byte[] imageData = picture.getPictureData().getData();
            return new ByteArrayInputStream(imageData);
        }).findFirst().orElse(null);
//        for (XSSFShape shape : shapes) {
//            if (shape instanceof XSSFPicture picture) {
//                XSSFClientAnchor anchor = picture.getClientAnchor();
//
//                int row = anchor.getRow1(); // Row position
//                int col = anchor.getCol1(); // Column position
//                int SHEET_PHOTO_COLUMN = 4;
//                if (row == targetRow && col == SHEET_PHOTO_COLUMN) {
//                    byte[] imageData = picture.getPictureData().getData();
//                    return new ByteArrayInputStream(imageData);
//                }
//            }
//        }
//
//        return null;
    }

    public String postImageToMinio(InputStream inputStream, Integer id, String department, String oldImageName) {
        try {
            // Read the input stream fully into a byte array (for hashing and later re-upload)
            byte[] fileBytes = inputStream.readAllBytes();

            // Determine content type and file extension
            String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(fileBytes));
            String extension = contentType.split("/")[1];

            // Compute MD5 hash of the file
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            String computedHash = sb.toString();

            // Check if the object already exists in the bucket
            try {
                StatObjectResponse stat = minioClient.statObject(StatObjectArgs.builder().bucket(department.toLowerCase() + "-bucket").object(oldImageName).build());
                String existing_eTag = stat.etag().replace("\"", ""); // Remove any quotes from the eTag
                if (existing_eTag.equals(computedHash)) {
                    // The image is identical; skip upload.
                    return oldImageName;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                // If statObject fails, the file likely doesn't exist. Proceed with upload.
            }

            String imageName = id + "_" + department + "_" + System.currentTimeMillis() + "." + extension;
            // Upload the new image if it's not already present or is different
            ByteArrayInputStream uploadStream = new ByteArrayInputStream(fileBytes);
            minioClient.putObject(PutObjectArgs.builder().bucket(department.toLowerCase() + "-bucket").object(imageName).stream(uploadStream, fileBytes.length, PutObjectArgs.MIN_MULTIPART_SIZE).contentType(contentType)  // Use the actual content type if available
                    .build());
            return imageName;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error while uploading image to Minio");
        }
    }

    public List<String> checkTheEmployeePropertyChanges(InputStream fis) throws IOException {
        List<String> changedDepartments = new ArrayList<>();

        Set<Integer> foundEmployeeIds = new HashSet<>();

        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        List<Employee> allEmployees = employeeRepository.findAll();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            try {
                Integer id = convertCellToInt(row.getCell(0));
                foundEmployeeIds.add(id);
                Optional<Employee> existingEmployee = employeeRepository.findById(id);

                if (existingEmployee.isPresent()) {
                    List<String> changes = checkDepartmentChanges(existingEmployee.get(), row);
                    for (String department : changes) {
                        if (!changedDepartments.contains(department)) {
                            changedDepartments.add(department);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        for (Employee employee : allEmployees) {
            if (!foundEmployeeIds.contains(employee.getId())) {
                String department = employee.getDepartment();
                if (!changedDepartments.contains(department) && employee.getIsActive()) {
                    changedDepartments.add(department);  // Add the department of the missing employee
                }
            }
        }

        return changedDepartments;
    }

    public Map<EmployeeStatus, List<Employee>> excelToEmployee(InputStream fis, LocalDate date) throws IOException {
        Map<EmployeeStatus, List<Employee>> employeeMap = new HashMap<>();
        employeeMap.put(EmployeeStatus.ADDED, new ArrayList<>());
        employeeMap.put(EmployeeStatus.EDITED, new ArrayList<>());
        employeeMap.put(EmployeeStatus.DELETED, new ArrayList<>());
        employeeMap.put(EmployeeStatus.NOTEDITED, new ArrayList<>());
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);
        XSSFDrawing drawing = sheet.getDrawingPatriarch();

        List<XSSFShape> shapes;
        try {
            shapes = drawing.getShapes();
        } catch (Exception e) {
            shapes = new ArrayList<>();
        }
        Set<Integer> processedEmployeeIds = new HashSet<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                continue;
            }
            try {
                EmployeeChangeResult result = getValue(row, shapes, date);
                if (result.getEmployee() != null) {
                    employeeMap.get(result.getStatus()).add(result.getEmployee());
                    processedEmployeeIds.add(result.getEmployee().getId());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        // Find deleted employees
        List<Employee> allExistingEmployees = employeeRepository.findAll();
        for (Employee existingEmployee : allExistingEmployees) {
            if (!processedEmployeeIds.contains(existingEmployee.getId())) {
                existingEmployee.setIsActive(false);
                employeeMap.get(EmployeeStatus.DELETED).add(existingEmployee);
                employeeService.removeEmployeeFromExistingData(existingEmployee.getDepartment(), String.valueOf(existingEmployee.getId()), date);
                employeeRepository.save(existingEmployee);
            }
        }
        return employeeMap;
    }


    public InputStreamResource getImageFromMinio(String objectName) {
        String bucketName = objectName.split("_")[1].split("\\.")[0].toLowerCase().concat("-bucket");
        try {

            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return new InputStreamResource(inputStream, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Error while fetching image from Minio");
        }
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public List<Employee> filterByDepartment(String department) {
        return employeeRepository.findByDepartmentAndIsActiveTrue(department);
    }

    public String detectContentType(String objectName, InputStream inputStream) throws IOException {
        // Option 1: Use file extension
        String extension = objectName.substring(objectName.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "webp":
                return "image/webp";
            default:
                break;
        }

        String contentType = URLConnection.guessContentTypeFromStream(inputStream);
        return contentType != null ? contentType : "application/octet-stream"; // Default to binary if undetectable
    }

    public long calculateTotalWorkTime(LocalDate workDate, LocalTime workStartTime, LocalTime workEndTime) {
        long totalMinutes = 0;

        // Define the work segments
        LocalTime[][] timeRanges = {{LocalTime.of(8, 30), LocalTime.of(9, 30)},  // 8:30 ~ 9:30
                {LocalTime.of(9, 30), LocalTime.of(12, 0)},  // 9:30 ~ 12:00
                {LocalTime.of(13, 0), LocalTime.of(15, 0)},  // 13:00 ~ 15:00
                {LocalTime.of(15, 20), LocalTime.of(17, 30)} // 15:20 ~ 17:30
        };

        // For each time range, calculate overlap with work start and end times
        for (LocalTime[] range : timeRanges) {
            LocalTime rangeStart = range[0];
            LocalTime rangeEnd = range[1];

            // Calculate the overlap between the segment and the work times
            LocalTime effectiveStart = (workStartTime.isBefore(rangeStart)) ? rangeStart : workStartTime;
            LocalTime effectiveEnd = (workEndTime.isAfter(rangeEnd)) ? rangeEnd : workEndTime;

            // If there is an overlap, calculate the duration for this segment
            if (effectiveStart.isBefore(effectiveEnd)) {
                long segmentMinutes = ChronoUnit.MINUTES.between(effectiveStart, effectiveEnd);
                totalMinutes += segmentMinutes;
            }
        }

        if (isDaylightSavingTime(workDate)) {
            totalMinutes -= 10;
        }

        return totalMinutes;
    }

    private static boolean isDaylightSavingTime(LocalDate date) {
        int month = date.getMonthValue();
        return month == 7 || month == 8 || month == 9; // July, August, September
    }

}