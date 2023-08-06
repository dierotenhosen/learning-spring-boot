package com.greglturnquist.learningspringboot.comments;

import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.converter.CompositeMessageConverterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.reactive.HiddenHttpMethodFilter;

@SpringBootApplication
public class LearningSpringBootCommentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearningSpringBootCommentsApplication.class, args);
	}

	@Bean
    HiddenHttpMethodFilter hiddenHttpMethodFilter() {
		return new HiddenHttpMethodFilter();
	}

	@Bean
	ParameterMessageInterpolator parameterMessageInterpolator() {
		return new ParameterMessageInterpolator();
	}

	@Bean
	CompositeMessageConverterFactory compositeMessageConverterFactory() { return new CompositeMessageConverterFactory(); }

}
