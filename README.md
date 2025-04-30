# Spring WebFlux Okhttp Connector (WIP)

## Estrutura do Projeto

**OkHttpClientHttpConnector.java:**
Implementa a interface ClientHttpConnector do Spring WebFlux.
Utiliza OkHttpClient para realizar as requisições HTTP.
A classe interna OkHttpClientHttpRequest extende AbstractClientHttpRequest e prepara a requisição OkHttp.
A classe interna OkHttpClientHttpResponse extende AbstractClientHttpResponse e processa a resposta OkHttp.

**OkHttpClientHttpConnectorTest.java:**
Utiliza MockWebServer do OkHttp para simular um servidor HTTP.
Testa os métodos GET, POST e o envio de headers.
Utiliza StepVerifier do Reactor para verificar os resultados das chamadas reativas.
