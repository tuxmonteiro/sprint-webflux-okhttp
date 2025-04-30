package dev.tuxmonteiro.spring.okhttpconnector;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.AbstractClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

public class OkHttpClientHttpRequest extends AbstractClientHttpRequest {

    private final HttpMethod method;
    private final java.net.URI uri;
    private final OkHttpClient client;
    private final HttpHeaders headers = new HttpHeaders();
    private DataBufferFactory dataBufferFactory;
    private Flux<DataBuffer> body = Flux.empty();
    private Request request;


    public OkHttpClientHttpRequest(HttpMethod method, java.net.URI uri, OkHttpClient client) {
        this.method = method;
        this.uri = uri;
        this.client = client;
    }

    private RequestBody createRequestBody(Flux<DataBuffer> body, MediaType contentType) {
        return new RequestBody() {
            public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
            }

            public okhttp3.MediaType contentType() {
                if (contentType != null) {
                    return okhttp3.MediaType.parse(contentType.toString());
                }
                return null;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getNativeRequest() {
        return (T) this.request;
    }

    @Override
    public HttpMethod getMethod() {
        return this.method;
    }


    @Override
    public java.net.URI getURI() {
        return this.uri;
    }


    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    @Override
    protected void applyHeaders() {

    }

    @Override
    protected void applyCookies() {

    }


    public void setBody(Flux<DataBuffer> body) {
        this.body = body;
    }


    public void setBody(DataBuffer body) {
        this.body = Flux.just(body);
    }


    @Override
    public DataBufferFactory bufferFactory() {
        return dataBufferFactory;
    }

    @Override
    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
        return null;
    }

    @Override
    public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return null;
    }

    @Override
    public Mono<Void> setComplete() {
        return null;
    }


    public void setDataBufferFactory(DataBufferFactory dataBufferFactory) {
        this.dataBufferFactory = dataBufferFactory;
    }


    public Mono<ClientHttpResponse> execute() {
        return Mono.fromCallable(() -> {
            Request.Builder builder = new Request.Builder().url(this.uri.toString());
            this.headers.forEach((name, values) -> values.forEach(value -> builder.addHeader(name, value)));
            RequestBody requestBody = createRequestBody(this.body, this.headers.getContentType());
            builder.method(this.method.name(), requestBody);
            this.request = builder.build();
            Response response = client.newCall(request).execute();
            return new OkHttpClientHttpResponse(response, this.dataBufferFactory);
        });
    }


    public void writeTo(BufferedSink sink) throws IOException {
        body.subscribe(buffer -> {
            try {
                sink.write(buffer.asByteBuffer().array());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
