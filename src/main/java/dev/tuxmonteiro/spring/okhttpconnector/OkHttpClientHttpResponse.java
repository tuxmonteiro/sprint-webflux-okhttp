package dev.tuxmonteiro.spring.okhttpconnector;

import okhttp3.Response;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.io.IOException;

public class OkHttpClientHttpResponse implements ClientHttpResponse {


    private final Response response;
    private final DataBufferFactory dataBufferFactory;
    private HttpHeaders headers;


    public OkHttpClientHttpResponse(Response response, DataBufferFactory dataBufferFactory) {
        this.response = response;
        this.dataBufferFactory = dataBufferFactory;
    }


    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.valueOf(this.response.code());
    }


    @Override
    public MultiValueMap<String, ResponseCookie> getCookies() {
        return null;
    }


    @Override
    public HttpHeaders getHeaders() {
        if (this.headers == null) {
            this.headers = new HttpHeaders();
            okhttp3.Headers responseHeaders = this.response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                String name = responseHeaders.name(i);
                String value = responseHeaders.value(i);
                this.headers.add(name, value);
            }
        }
        return this.headers;
    }


    @Override
    public Flux<DataBuffer> getBody() {
        try {
            if (this.response.body() != null) {
                return Flux.just(dataBufferFactory.wrap(this.response.body().bytes()));
            } else {
                return Flux.empty();
            }
        } catch (IOException e) {
            return Flux.error(e);
        }
    }
}
