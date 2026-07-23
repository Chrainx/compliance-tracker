package com.chrainx.compliance_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling: turns on Spring's scheduling machinery app-wide. Without this,
// @Scheduled methods (see DeadlineSyncService) are never actually invoked - the annotation
// alone does nothing until this is present somewhere in the app.
@EnableScheduling
@SpringBootApplication
public class ComplianceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ComplianceTrackerApplication.class, args);
	}

}
