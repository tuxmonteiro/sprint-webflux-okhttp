package dev.tuxmonteiro.spring.okhttpconnector;

import okhttp3.OkHttpClient;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import reactor.core.publisher.Mono;

import org.springframework.util.Assert;


/**
 * {@link ClientHttpConnector} implementation that uses OkHttp 3.x.
 */
public class OkHttpClientHttpConnector implements ClientHttpConnector {


    private final OkHttpClient client;


    public OkHttpClientHttpConnector(OkHttpClient client) {
        Assert.notNull(client, "OkHttpClient must not be null");
        this.client = client;
    }


    public OkHttpClientHttpConnector() {
        this(new OkHttpClient());
    }


    @Override
    public Mono<ClientHttpResponse> connect(HttpMethod method, java.net.URI uri,
                                            java.util.function.Function<? super ClientHttpRequest, Mono<Void>> requestCallback) {
        return Mono.defer(() -> {
            OkHttpClientHttpRequest request = new OkHttpClientHttpRequest(method, uri, this.client);
            return requestCallback.apply(request)
                    .then(Mono.defer(request::execute));
        });
    }
}
