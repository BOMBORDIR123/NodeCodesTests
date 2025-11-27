package by.javaguru.nordcodestests.tests;

import by.javaguru.nordcodestests.api.ApiClient;
import by.javaguru.nordcodestests.base.TestBase;
import by.javaguru.nordcodestests.utils.TokenGenerator;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Набор тестов, проверяющих работу операции LOGIN:
 * <ul>
 *     <li>успешную аутентификацию;</li>
 *     <li>ошибки валидации токена;</li>
 *     <li>ошибки внешнего сервиса /auth;</li>
 *     <li>работу API-Key;</li>
 *     <li>граничные значения токена.</li>
 * </ul>
 */
public class LoginTests extends TestBase {

    /**
     * Проверяет успешный LOGIN при валидном токене
     * и корректном ответе внешнего сервиса.
     */
    @Test
    @Description("Проверка успешной аутентификации при валидном токене и корректной работе внешнего сервиса /auth")
    void testLoginSuccess() {
        String token = TokenGenerator.generateToken();

        Response response = ApiClient.sendRequest(token, "LOGIN");

        assertEquals("OK", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что неверный API-Key приводит к ERROR.
     */
    @Test
    @Description("Отправка запроса с неправильным API-Key должна выдавать ошибку")
    void testLoginWrongApiKey() {
        String token = TokenGenerator.generateToken();

        Response response =
                given()
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("X-Api-Key", "WRONG")
                        .formParam("token", token)
                        .formParam("action", "LOGIN")
                        .when()
                        .post("/endpoint")
                        .then()
                        .extract().response();

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что токен неверного формата отклоняется приложением.
     */
    @Test
    @Description("Токен неверного формата должен приводить к ошибке валидации")
    void testLoginInvalidTokenFormat() {
        String token = "ABC123";

        Response response = ApiClient.sendRequest(token, "LOGIN");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет реакцию на ошибку внешнего сервиса /auth.
     */
    @Test
    @Description("Если внешний сервис /auth возвращает ошибку — должен быть ERROR")
    void testLoginExternalServiceFail() {
        stubFor(post("/auth")
                .willReturn(aResponse().withStatus(401)));

        String token = TokenGenerator.generateToken();

        Response response = ApiClient.sendRequest(token, "LOGIN");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что повторный LOGIN по тому же токену запрещён.
     */
    @Test
    @Description("Повторный LOGIN с тем же токеном — ожидаем ERROR")
    void testDoubleLogin() {
        String token = TokenGenerator.generateToken();

        Response first = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", first.jsonPath().getString("result"));

        Response second = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("ERROR", second.jsonPath().getString("result"));
    }

    /**
     * Проверяет отсутствие обязательного параметра action.
     */
    @Test
    @Description("LOGIN без параметра action — ERROR")
    void testLoginNoActionParam() {
        String token = TokenGenerator.generateToken();

        Response response =
                given()
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("X-Api-Key", "qazWSXedc")
                        .formParam("token", token)
                        .post("/endpoint")
                        .then()
                        .extract().response();

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Граничное значение: валидный токен длиной 32 символа,
     * состоящий из одинаковых символов — должен быть LOGIN OK.
     */
    @Test
    @Description("Граница: токен из одинаковых символов (32×A) — ожидаем LOGIN OK")
    void testTokenAllSameChar() {
        String token = "A".repeat(32);

        Response response = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", response.jsonPath().getString("result"));

        ApiClient.sendRequest(token, "LOGOUT");
    }

    /**
     * Граничное значение: валидный токен длиной 32 символа,
     * состоящий из случайных символов A-Z0-9 — должен быть LOGIN OK.
     */
    @Test
    @Description("Граница: случайный токен длиной 32 символа из диапазона A-Z0-9 — ожидаем LOGIN OK")
    void testTokenLength32ValidRandom() {
        String token = "0123456789ABCDEF0123456789ABCDEF";

        Response response = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", response.jsonPath().getString("result"));

        ApiClient.sendRequest(token, "LOGOUT");
    }
}
