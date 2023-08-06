package com.greglturnquist.learningspringboot;

import com.greglturnquist.learningspringboot.images.Comment;
import com.greglturnquist.learningspringboot.images.CommentController;
import com.greglturnquist.learningspringboot.images.ImageRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.support.BindingAwareModelMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("simulator")
@Component
public class CommentSimulator {
    private final HomeController homeController;
    private final CommentController commentController;
    private final ImageRepository repository;
    private final AtomicInteger counter;

    public CommentSimulator(HomeController homeController, CommentController commentController, ImageRepository repository) {
        this.homeController = homeController;
        this.commentController = commentController;
        this.repository = repository;
        this.counter = new AtomicInteger(1);
    }

    @EventListener
    public void simulateActivity(ApplicationReadyEvent event) {
        Flux.interval(Duration.ofMillis(1000))
                .flatMap(tick -> repository.findAll())
                .map(image -> {
                    Comment comment = new Comment();
                    comment.setImageId(image.getId());
                    comment.setComment("Comment #" + counter.getAndIncrement());
                    return Mono.just(comment);
                })
                .flatMap(newComment -> Mono.defer(() -> commentController.addComment(newComment)))
                .subscribe();
    }

    @EventListener
    public void simulateUsersClicking(ApplicationReadyEvent event) {
        Flux.interval(Duration.ofMillis(500))
                .flatMap(tick ->
                        Mono.defer(() -> homeController.index(new BindingAwareModelMap()))).subscribe();
    }
}
