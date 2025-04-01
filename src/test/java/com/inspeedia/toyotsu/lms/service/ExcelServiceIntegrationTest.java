package com.inspeedia.toyotsu.lms.service;

import com.inspeedia.toyotsu.lms.enums.HeaderLanguage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

@SpringBootTest
@ActiveProfiles("test")
public class ExcelServiceIntegrationTest {
    @Autowired
    private ExcelService es;

    @Test
    void shouldOutputExcel() {
        ByteArrayInputStream bais = es.exportDateWiseMainScreenData(HeaderLanguage.JAPANESE, "TLS", LocalDate.now());
        BufferedReader reader = new BufferedReader(new InputStreamReader(bais));
        String line;
        while (true) {
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(line);
        }
    }
}
