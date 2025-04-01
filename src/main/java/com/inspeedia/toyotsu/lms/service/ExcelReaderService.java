package com.inspeedia.toyotsu.lms.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings("unused")
public class ExcelReaderService {
    private final Logger log = LoggerFactory.getLogger(ExcelReaderService.class);

    public List<List<String>> readExcelList(String filePath) throws IOException {
        List<List<String>> data = new ArrayList<>();

        try (FileInputStream fs = new FileInputStream(filePath)) {
            try (Workbook workbook = new XSSFWorkbook(fs)) {

                Sheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    List<String> rowData = new ArrayList<>();
                    for(Cell cell : row) {
                        rowData.add(getCellValue(cell));
                    }
                    data.add(rowData);
                }
            }
        }

        return data;
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "Unknown Type";
        }
    }

    public void extractImagesFromExcel(String excelFilePath) throws IOException {
        try(FileInputStream fileInputStream = new FileInputStream(excelFilePath)) {
            try(XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {

                // Loop through the pictures in the workbook
                List<XSSFPictureData> pictures = workbook.getAllPictures();
                for (XSSFPictureData picture : pictures) {
                    byte[] imageData = picture.getData();
                    String imageFormat = picture.suggestFileExtension(); // Get the image format (jpeg, png, etc.)

                    // Define where to save the image
                    String imageName = "extracted-image-" + picture.getPackagePart().getPartName().getName();
                    File imageFile = new File(imageName + "." + imageFormat);

                    try (FileOutputStream imageOut = new FileOutputStream(imageFile)) {
                        imageOut.write(imageData); // Write the image data to a file
                    }
                    log.error("Extracted image saved as: {}", imageFile.getAbsolutePath());
                }
            }
        }
    }
}
