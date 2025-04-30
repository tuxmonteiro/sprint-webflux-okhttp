# Spring WebFlux Okhttp Connector

1. Estrutura do Projeto

- OkHttpClientHttpConnector: Implementação personalizada do conector HTTP usando OkHttp
- OkHttpResponseAdapter: Classe interna que converte respostas do OkHttp para o formato exigido pelo Spring
- WebClientConfig: Configuração do WebClient para utilizar nosso conector personalizado
- ExemploService: Serviço de negócio demonstrando uso do WebClient
- OkHttpClientHttpConnectorTest: Testes unitários com MockWebServer

## Funcionalidades Principais

**OkHttpClientHttpConnector**

- Implementa AbstractClientHttpConnector do Spring
- Utiliza cliente OkHttp para executar requisições
- Converte requisições reativas do Spring para requisições síncronas do OkHttp
- Oferece suporte básico a métodos HTTP e cabeçalhos

**OkHttpResponseAdapter**

- Converte status codes, mensagens e cabeçalhos do OkHttp
- Fornece fluxo de dados da resposta através de InputStream
- Garante fechamento adequado dos recursos

**WebClientConfig**

- Configura cliente WebClient globalmente
- Define URL base para requisições
- Utiliza nosso conector personalizado

**Testes Unitários**

- Valida requisições GET básicas
- Verifica tratamento correto de cabeçalhos
- Testa cenários de erro de conexão
- Utiliza MockWebServer para simular servidores HTTP

## Limitações e Melhorias

- Atualmente só suporta requisições GET sem corpo
- Uso de chamadas síncronas do OkHttp (bloqueantes)
- Poderia ser estendido para:
  - Suportar outros métodos HTTP com corpo
  - Implementar execução assíncrona verdadeira
  - Manipular cookies e redirecionamentos
  - Adicionar configuração avançada (timeout, interceptors)

## Exemplo de uso

```java
package com.example.okhttpconnector.config;

import com.example.okhttpconnector.connector.OkHttpClientHttpConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // Cria WebClient com nosso conector personalizado
        return WebClient.builder()
                .clientConnector(new OkHttpClientHttpConnector())
                .baseUrl("https://api.example.com")
                .build();
    }
}

// Serviço de exemplo que usa o WebClient configurado
package com.example.okhttpconnector.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ExampleService {

    private final WebClient webClient;

    public ExampleService(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Faz uma requisição GET para buscar dados de usuário
     * @param userId ID do usuário
     * @return Dados do usuário como String
     */
    public Mono<String> getUser(int userId) {
        return webClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .bodyToMono(String.class);
    }
}
```
