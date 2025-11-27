package by.javaguru.nordcodestests.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;

/**
 * Утилитный класс для запуска и остановки локального WireMock-сервера,
 * а также регистрации стандартных моков, используемых в тестах.
 * <p>
 * Сервер поднимается на порту {@code 8888} и мокаeт два эндпойнта:
 * <ul>
 *     <li><b>POST /auth</b> — отвечает {@code {"result": "OK"}}</li>
 *     <li><b>POST /doAction</b> — отвечает {@code {"result": "OK"}}</li>
 * </ul>
 * <p>
 * Сервер создаётся лениво (lazy init): если он уже существует и запущен —
 * повторный запуск не выполняется.
 */
public class MockServer {
    private static WireMockServer wireMockServer;

    public static void start() {

        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(
                    WireMockConfiguration.options()
                            .port(8888)
                            .notifier(new Slf4jNotifier(true))
            );
        }

        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
            WireMock.configureFor("localhost", 8888);
        }

        setupStubs();
    }

    public static void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    private static void setupStubs() {
        WireMock.stubFor(
                WireMock.post(WireMock.urlEqualTo("/auth"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"result\":\"OK\"}")
                        )
        );

        WireMock.stubFor(
                WireMock.post(WireMock.urlEqualTo("/doAction"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{\"result\":\"OK\"}")
                        )
        );
    }
}
