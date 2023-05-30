package com.circuitbreaker;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CircuitBreakerApplicationTests {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig()
                    .port(9090))
            .build();

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void checkMinimumNumberOfCalls() throws InterruptedException {
        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(ok()));
        IntStream.rangeClosed(1, 3)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                });

        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(serverError()));

        IntStream.rangeClosed(1, 3)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        //turns to open mode
        IntStream.rangeClosed(1, 4)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

    @Test
    public void checkOpenStateProperties() throws InterruptedException {
        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(ok()));

        IntStream.rangeClosed(1, 3)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                });

        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(serverError()));

        IntStream.rangeClosed(1, 3)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        //turn to open mode
        IntStream.rangeClosed(1, 4)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });

        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(ok()));

        Thread.sleep(2000);//will turn to half-open state as wait-duration-in-open-state is 1s

        IntStream.rangeClosed(1, 2)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); //turning to open state
                });


        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(serverError()));

        IntStream.rangeClosed(1, 4)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });

        IntStream.rangeClosed(1, 1)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });

        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(ok()));

        Thread.sleep(2000);//will turn to half-open state as wait-duration-in-open-state is 1s

        IntStream.rangeClosed(1, 3)//should return to close state and ringbit buffer will be cleared
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK); //turning to open state
                });

        IntStream.rangeClosed(1, 1)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                });
        wireMockServer.stubFor(WireMock.get("/api/external")
                .willReturn(serverError()));
        IntStream.rangeClosed(1, 2)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
        IntStream.rangeClosed(1, 2)
                .forEach(i -> {
                    ResponseEntity response = restTemplate.getForEntity("/api/circuit-breaker", String.class);
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                });
    }

}
