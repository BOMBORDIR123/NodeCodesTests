package by.javaguru.nordcodestests.base;

import by.javaguru.nordcodestests.wiremock.MockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Базовый класс для всех интеграционных тестов.
 * <p>
 * Он автоматически:
 * <ul>
 *     <li>запускает WireMock перед каждым тестом;</li>
 *     <li>поднимает тестируемое приложение из JAR;</li>
 *     <li>проверяет, что приложение успешно стартовало;</li>
 *     <li>настраивает REST Assured на localhost:8080;</li>
 *     <li>останавливает приложение после выполнения теста;</li>
 *     <li>очищает WireMock (reset) после каждого теста.</li>
 * </ul>
 *
 * Класс обеспечивает единый и воспроизводимый тестовый контекст
 * для всех тестов, взаимодействующих с локальным сервисом.
 */
public class TestBase {

    /**
     * Процесс, в котором запускается тестируемое приложение (.jar).
     * Используется для корректного завершения после выполнения тестов.
     */
    private Process appProcess;

    /**
     * Подготавливает окружение перед выполнением каждого теста.
     * <p>
     * Последовательность шагов:
     * <ol>
     *     <li>Запускается WireMock для симуляции внешних сервисов.</li>
     *     <li>Запускается приложение из JAR-файла.</li>
     *     <li>Происходит ожидание готовности приложения.</li>
     *     <li>Настраивается базовый URI для REST Assured.</li>
     * </ol>
     *
     * @throws Exception если приложение не удалось запустить или старт занял слишком много времени.
     */
    @BeforeEach
    void setUp() throws Exception {
        MockServer.start();
        startApplication();
        RestAssured.baseURI = "http://localhost:8080";
    }

    /**
     * Выполняет финализацию после каждого теста.
     * <p>
     * Включает:
     * <ul>
     *     <li>завершение процесса запущенного приложения;</li>
     *     <li>сброс WireMock конфигураций;</li>
     *     <li>остановку WireMock сервера.</li>
     * </ul>
     *
     * Метод гарантирует чистое окружение для следующего теста.
     */
    @AfterEach
    void tearDown() {
        if (appProcess != null) {
            appProcess.destroy();
        }

        WireMock.reset();
        MockServer.stop();
    }

    /**
     * Запускает тестируемое приложение в отдельном процессе.
     * <p>
     * Приложение запускается с параметрами:
     * <ul>
     *     <li>-Dsecret=qazWSXedc — секрет приложения</li>
     *     <li>-Dmock=http://localhost:8888 — адрес WireMock</li>
     * </ul>
     *
     * После старта вызывается {@link #waitForAppStart()}, чтобы убедиться,
     * что приложение действительно поднялось.
     *
     * @throws IOException если JAR не найден или запуск невозможен
     * @throws InterruptedException если ожидание старта было прервано
     */
    private void startApplication() throws IOException, InterruptedException {
        System.out.println("Starting application JAR...");

        ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-jar",
                "-Dsecret=qazWSXedc",
                "-Dmock=http://localhost:8888",
                "internal-0.0.1-SNAPSHOT.jar"
        );

        pb.directory(new java.io.File("src/test/resources"));
        pb.redirectErrorStream(true);
        appProcess = pb.start();

        waitForAppStart();
    }

    /**
     * Ожидает, когда приложение станет доступным по адресу http://localhost:8080/.
     * <p>
     * Пытается подключиться до 40 раз с задержкой 500 мс между попытками.
     * Выполняется простая проверка доступности HTTP-ответа (код 100–599).
     *
     * @throws InterruptedException если ожидание было прервано
     * @throws RuntimeException     если приложение так и не стало доступным
     */
    private void waitForAppStart() throws InterruptedException {
        System.out.println("Waiting for application to start...");

        int maxAttempts = 40;
        int delayMs = 500;

        for (int i = 0; i < maxAttempts; i++) {
            try {
                HttpURLConnection conn =
                        (HttpURLConnection) new URL("http://localhost:8080/").openConnection();

                conn.setConnectTimeout(500);
                conn.setReadTimeout(500);

                int code = conn.getResponseCode();

                if (code >= 100 && code <= 599) {
                    System.out.println("Application is UP! Status: " + code);
                    return;
                }

            } catch (Exception ignored) {}

            Thread.sleep(delayMs);
        }

        throw new RuntimeException("Application did not start within allowed time.");
    }
}
