package com.greglturnquist.learningspringboot.comments;

import org.springframework.data.repository.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CommentRepository extends Repository<Comment, String> {

    Flux<Comment> findByImageId(String imageId);

    Flux<Comment> saveAll(Flux<Comment> newComments);

    Mono<Comment> save(Comment newComment);

    Mono<Comment> findById(String id);

    Mono<Void> deleteAll();

}
