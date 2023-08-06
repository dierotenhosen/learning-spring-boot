package com.greglturnquist.learningspringboot;

import com.greglturnquist.learningspringboot.images.Image;
import com.greglturnquist.learningspringboot.images.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class ImageRepositoryTests {
    @Autowired
    ImageRepository repository;

    @Autowired
    MongoOperations operations;

    @BeforeEach
    public void setUp(){
        operations.dropCollection(Image.class);
        operations.insert(new Image("1", "learning-spring-boot-cover.jpg"));
        operations.insert(new Image("2", "learning-spring-boot-2nd-edition-cover.jpg"));
        operations.insert(new Image("3", "bazinga.png"));
        operations.findAll(Image.class).forEach(image -> System.out.println(image.toString()));
    }

    @Test
    public void findAllShouldWork() {
        Flux<Image> images = repository.findAll();
        StepVerifier.create(images)
                .recordWith(ArrayList::new)
                .expectNextCount(3)
                .consumeRecordedWith(results -> {
                    assertThat(results).hasSize(3);
                    assertThat(results).extracting(Image::getName).contains("bazinga.png");
                })
                .expectComplete()
                .verify();
    }

    @Test
    public void findByNameShouldWork() {
        Mono<Image> image = repository.findByName("bazinga.png");
        StepVerifier.create(image)
                .expectNextMatches(results -> {
                    assertThat(results.getName().equals("bazinga.png"));
                    assertThat(results.getId().equals("3"));
                    return true;
                })
                .verifyComplete();
    }

}
