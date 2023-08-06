package com.greglturnquist.learningspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@SpringBootApplication
@EnableHystrixDashboard
public class LearningSpringBootHystrixDashboard {

	public static void main(String[] args) {
		SpringApplication.run(LearningSpringBootHystrixDashboard.class);
	}
}
