package com.greglturnquist.learningspringboot.comments;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@EnableBinding(CustomProcessor.class) // receives input from the group "comments"
public class CommentService {

    private final CommentRepository repository;

    private final MeterRegistry meterRegistry;

    public CommentService(CommentRepository repository, MeterRegistry meterRegistry) {
        this.repository = repository;
        this.meterRegistry = meterRegistry;
    }

    @StreamListener
    @Output(CustomProcessor.OUTPUT)
    public Flux<Void> save(@Input(CustomProcessor.INPUT) Flux<Comment> newComments) {
        return repository
                .saveAll(newComments)
                .log("commentService-save")
                .flatMap(comment -> {
                   meterRegistry.counter("comments.consumed", "imageId", comment.getImageId())
                           .increment();
                   return Mono.empty();
                });
    }

    @Bean
    CommandLineRunner setUp(CommentRepository repository) {
        return args -> {
            repository.deleteAll().subscribe();
        };
    }
}
