package dev.tuxmonteiro.spring.okhttpconnector;

import okhttp3.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.AbstractClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Executor;

/**
 * Cliente HTTP personalizado que utiliza OkHttp como motor subjacente.
 * Adapta requisições reativas do Spring WebFlux para chamadas sincrônicas do OkHttp.
 */
public class OkHttpClientHttpConnector extends AbstractClientHttpConnector {

    private final OkHttpClient httpClient;

    /**
     * Cria uma nova instância com cliente OkHttp padrão.
     */
    public OkHttpClientHttpConnector() {
        this(new OkHttpClient());
    }

    /**
     * Cria uma nova instância com cliente OkHttp personalizado.
     * @param httpClient Cliente OkHttp configurado
     */
    public OkHttpClientHttpConnector(OkHttpClient httpClient) {
        Assert.notNull(httpClient, "HttpClient não pode ser nulo");
        this.httpClient = httpClient;
    }

    @Override
    protected Mono<ClientHttpResponse> connectInternal(HttpMethod method, URI uri, Executor executor, Context context) {
        return Mono.fromCallable(() -> {
            // Constrói requisição OkHttp
            Request.Builder requestBuilder = new Request.Builder()
                    .url(uri.toString())
                    .method(method.name(), null); // Para POST/PUT, precisaria de RequestBody

            // Configura headers da requisição
            context.requestHeaders()
                    .forEach((key, value) -> value.forEach(val -> requestBuilder.addHeader(key, val)));

            Request request = requestBuilder.build();

            // Executa requisição síncrona (bloqueante) - apenas para demonstração
            Response response = httpClient.newCall(request).execute();
            
            // Converte resposta OkHttp para formato Spring
            return new OkHttpResponseAdapter(response);
        });
    }

    /**
     * Adaptador para converter resposta do OkHttp em ClientHttpResponse do Spring
     */
    private static class OkHttpResponseAdapter implements ClientHttpResponse {
        private final Response response;

        OkHttpResponseAdapter(Response response) {
            this.response = response;
        }

        @Override
        public int getRawStatusCode() throws IOException {
            return response.code();
        }

        @Override
        public String getStatusText() throws IOException {
            return response.message();
        }

        @Override
        public HttpHeaders getHeaders() {
            HttpHeaders headers = new HttpHeaders();
            for (String name : response.headers().names()) {
                headers.put(name, response.headers().values(name));
            }
            return headers;
        }

        @Override
        public InputStream getBody() throws IOException {
            return response.body().byteStream();
        }

        @Override
        public void close() {
            response.close();
        }
    }
}
