package com.inspeedia.toyotsu.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;

@SpringBootApplication
@EnableScheduling
public class LmsApplication {
	public static void main(String[] args) throws IOException {
		ApplicationContext context = SpringApplication.run(LmsApplication.class, args);
	}

}
