package dev.tuxmonteiro.spring.okhttpconnector;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


import java.net.URI;


import static org.assertj.core.api.Assertions.assertThat;


public class OkHttpClientHttpConnectorTest {


    private MockWebServer server;
    private ClientHttpConnector connector;


    @BeforeEach
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        connector = new OkHttpClientHttpConnector();
    }


    @AfterEach
    public void tearDown() throws Exception {
        server.shutdown();
    }


    @Test
    public void get() {
        server.enqueue(new MockResponse().setBody("Hello World").setResponseCode(200));


        Mono<ClientHttpResponse> responseMono = connector.connect(HttpMethod.GET, URI.create(server.url("/").toString()),
                request -> Mono.empty());


        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    StepVerifier.create(response.getBody())
                            .assertNext(dataBuffer -> {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                assertThat(new String(content)).isEqualTo("Hello World");
                            })
                            .verifyComplete();
                })
                .verifyComplete();
    }


    @Test
    public void post() {
        server.enqueue(new MockResponse().setResponseCode(201));


        String requestBody = "Test Body";
        byte[] requestBodyBytes = requestBody.getBytes();
        DataBuffer requestDataBuffer = new DefaultDataBufferFactory().wrap(requestBodyBytes);


        Mono<ClientHttpResponse> responseMono = connector.connect(HttpMethod.POST, URI.create(server.url("/").toString()),
                request -> {
                    ((OkHttpClientHttpRequest) request).setBody(Flux.just(requestDataBuffer));
                    return Mono.empty();
                });


        StepVerifier.create(responseMono)
                .assertNext(response -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED))
                .verifyComplete();
    }


    @Test
    public void headers() {
        server.enqueue(new MockResponse().setResponseCode(200).addHeader("Custom-Header", "Custom-Value"));


        Mono<ClientHttpResponse> responseMono = connector.connect(HttpMethod.GET, URI.create(server.url("/").toString()),
                request -> {
                    request.getHeaders().add("Request-Header", "Request-Value");
                    return Mono.empty();
                });


        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertThat(response.getHeaders().getFirst("Custom-Header")).isEqualTo("Custom-Value");
                })
                .verifyComplete();
    }
}
