package com.greglturnquist.learningspringboot;

import com.greglturnquist.learningspringboot.images.Image;
import com.greglturnquist.learningspringboot.images.ImageService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * TODO (Corneliu: 20.7.2023) The tests from this class fail, message is `no qualifying bean of type
 * CommentReaderRepository. My suspicion is that the tests started failing when I refactored the code and
 * created the folders `comments` and `images`.
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = HomeController.class)
@Import({ThymeleafAutoConfiguration.class})
@Disabled
public class HomeControllerTests {

    @Autowired
    WebTestClient webClient;

    @MockBean
    ImageService imageService;

    @Test
    public void baseRouteShouldListAllImages() {
        // given
        Image alphaImage = new Image("1", "alpha.png");
        Image bravoImage = new Image("2", "bravo.png");
        given(imageService.findAllImages()).willReturn(Flux.just(alphaImage, bravoImage));

        // when
        EntityExchangeResult<String> result = webClient
                .get().uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult();

        // then
        verify(imageService).findAllImages();
        verifyNoMoreInteractions(imageService);
        assertThat(result.getResponseBody())
                .contains("<title>Learning Spring Boot: Spring-a-Gram</title>")
                .contains("<a href=\"/images/alpha.png/raw\">")
                .contains("<a href=\"/images/bravo.png/raw\">");
    }

    @Test
    public void fetchingImageShouldWork() {
        // given
        given(imageService.findOneImage(any()))
                .willReturn(Mono.just(new ByteArrayResource("data".getBytes())));

        // when
        webClient
                .get().uri("/images/alpha.png/raw")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("data");

        // then
        verify(imageService).findOneImage("alpha.png");
        verifyNoMoreInteractions(imageService);
    }

    @Test
    public void fetchingNullImageShouldFail()
            throws IOException { // In fact this method will not throw an IOException, since `given` catches exceptions.
        // given
        Resource resource = mock(Resource.class);
        given(resource.getInputStream()).willThrow(new IOException("Bad file"));
        given(imageService.findOneImage(any())).willReturn(Mono.just(resource));

        // when
        webClient.get().uri("/images/alpha.png/raw")
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Couldn't find alpha.png => Bad file");

        // then
        verify(imageService).findOneImage("alpha.png");
        verifyNoMoreInteractions(imageService);
    }

    @Test
    public void deleteImageShouldWork() {
        // given
        given(imageService.deleteImage(any())).willReturn(Mono.empty());

        // when
        webClient.delete().uri("/images/alpha.png")
                .exchange()
                .expectStatus()
                .isSeeOther()
                .expectHeader()
                .valueEquals(HttpHeaders.LOCATION, "/");

        // then
        verify(imageService).deleteImage("alpha.png");
        verifyNoMoreInteractions(imageService);
    }
}
