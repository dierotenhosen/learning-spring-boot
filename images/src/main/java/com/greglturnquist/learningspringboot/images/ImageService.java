package com.greglturnquist.learningspringboot.images;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    private static final String UPLOAD_ROOT = "upload-dir";

    private final ResourceLoader resourceLoader;

    private final ImageRepository imageRepository;

    private final MeterRegistry meterRegistry;

    public ImageService(@Qualifier("webApplicationContext") ResourceLoader resourceLoader, ImageRepository imageRepository, MeterRegistry meterRegistry) {
        this.imageRepository = imageRepository;
        this.resourceLoader = resourceLoader;
        this.meterRegistry = meterRegistry;
        // If the directory to upload images does not exist, create it now.
        File uploadDir = new File(UPLOAD_ROOT);
        if (!uploadDir.isDirectory()) {
            uploadDir.mkdir();
        }
    }

    public Flux<Image> findAllImages() {
        return imageRepository.findAll().log("findAll");
    }

    public Mono<Resource> findOneImage(String filename) {
        return Mono.fromSupplier(() ->
                resourceLoader.getResource("file:" + UPLOAD_ROOT + "/" + filename))
                .log("findOneImage");
    }

    public Mono<Void> createImage(Flux<FilePart> files) {
        return files.flatMap(file -> {
                    Mono<Image> saveDatabaseImage = imageRepository.save(
                                    new Image(UUID.randomUUID().toString(), file.filename()))
                            .log("createImage-save");
                    //noinspection SpellCheckingInspection
                    Mono<Void> copyFile = Mono.just(Paths.get(UPLOAD_ROOT, "/", file.filename()).toFile())
                            .log("createImage-picktarget")
                            .map(destFile -> {
                                try {
                                    if (!destFile.createNewFile()) {
                                        throw new IOException();
                                    }
                                    return destFile;
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .log("createImage-newfile")
                            .flatMap(file::transferTo)
                            .log("createImage-copy");
                    Mono<Void> countFile = Mono.fromRunnable(() -> {
                        meterRegistry.summary("files.uploaded.bytes")
                                .record(Paths.get(UPLOAD_ROOT, file.filename()).toFile().length());
                    });
                    return Mono.when(saveDatabaseImage, copyFile, countFile);
                })
                .then()
                .log("createImage-done");
    }

    public Mono<Void> deleteImage(String filename) {
        Mono<Void> deleteDatabaseImage = imageRepository
                .findByName(filename)
                .log("deleteImage-find")
                .flatMap(imageRepository::delete)
                .log("deleteImage-deleteFromRepository");
        Mono<Void> deleteFile = Mono.fromRunnable(() -> {
            try {
                Files.deleteIfExists(Paths.get(UPLOAD_ROOT, filename));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return Mono.when(deleteDatabaseImage, deleteFile)
                .then()
                .log("deleteImage-done");
    }
}
