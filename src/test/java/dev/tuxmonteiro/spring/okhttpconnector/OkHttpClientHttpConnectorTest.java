package dev.tuxmonteiro.spring.okhttpconnector;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpResponse;
import reactor.test.StepVerifier;

import java.net.URI;

class OkHttpClientHttpConnectorTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void testRequisicaoGetBasica() throws Exception {
        // Configura resposta simulada
        server.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.OK.value())
                .setBody("Conteúdo da resposta"));

        // Cria conector e faz requisição
        OkHttpClientHttpConnector connector = new OkHttpClientHttpConnector(
                new OkHttpClient()
        );

        // Realiza requisição e verifica resposta
        StepVerifier.create(connector.connect(HttpMethod.GET, server.url("/").uri()))
                .assertNext(response -> {
                    Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
                    Assertions.assertTrue(response.getBody().readAllBytes().length > 0);
                })
                .verifyComplete();
    }

    @Test
    void testCabecalhosResposta() throws Exception {
        // Configura servidor com cabeçalhos personalizados
        server.enqueue(new MockResponse()
                .addHeader("X-Tipo-Teste", "Integracao")
                .setResponseCode(HttpStatus.OK.value()));

        // Cria cliente e realiza requisição
        OkHttpClientHttpConnector connector = new OkHttpClientHttpConnector(
                new OkHttpClient()
        );

        // Verifica cabeçalhos na resposta
        StepVerifier.create(connector.connect(HttpMethod.GET, server.url("/").uri()))
                .assertNext(response -> {
                    Assertions.assertNotNull(response.getHeaders().getFirst("X-Tipo-Teste"));
                    Assertions.assertEquals("Integracao", response.getHeaders().getFirst("X-Tipo-Teste"));
                })
                .verifyComplete();
    }

    @Test
    void testTratamentoErroConexao() {
        // Tenta conectar a um host inválido
        OkHttpClientHttpConnector connector = new OkHttpClientHttpConnector(
                new OkHttpClient()
        );

        // Verifica se erro é tratado corretamente
        StepVerifier.create(connector.connect(HttpMethod.GET, URI.create("http://host-invalido:9999")))
                .expectErrorMatches(throwable -> 
                    throwable.getMessage().contains("Failed to connect"))
                .verify();
    }
}
