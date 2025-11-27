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
 * Набор негативных тестов, проверяющих обработку ошибок API:
 * некорректные токены, неверные действия, проблемы с внешними сервисами
 * и ошибки валидации входящих параметров.
 */
public class NegativeTests extends TestBase {

    /**
     * Проверяет реакцию API на отсутствие X-Api-Key.
     */
    @Test
    @Description("Вызов LOGIN без X-Api-Key должен завершаться result=ERROR")
    void testMissingApiKey() {
        String token = TokenGenerator.generateToken();

        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", token)
                .formParam("action", "LOGIN")
                .post("http://localhost:8080/endpoint");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет обработку некорректного X-Api-Key.
     */
    @Test
    @Description("Неверный API-key при LOGIN должен приводить к ответу ERROR")
    void testInvalidApiKey() {
        String token = TokenGenerator.generateToken();

        Response response = given()
                .contentType("application/x-www-form-urlencoded")
                .header("X-Api-Key", "INVALID")
                .formParam("token", token)
                .formParam("action", "LOGIN")
                .post("http://localhost:8080/endpoint");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что слишком короткий токен считается невалидным.
     */
    @Test
    @Description("Токен слишком короткий — ожидается ERROR")
    void testShortToken() {
        Response response = ApiClient.sendRequest("ABC123", "LOGIN");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет отказ при использовании токена с недопустимыми символами.
     */
    @Test
    @Description("Токен с запрещёнными символами должен возвращать ERROR")
    void testTokenWithSpecialChars() {
        Response response = ApiClient.sendRequest("INVALID_TOKEN_@@@@_123456", "LOGIN");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет обработку неизвестного действия action.
     */
    @Test
    @Description("Неизвестное значение action приводит к ERROR")
    void testUnknownAction() {
        String token = TokenGenerator.generateToken();

        Response response = ApiClient.sendRequest(token, "UNKNOWN_ACTION");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что пустой action вызывает ошибку.
     */
    @Test
    @Description("Пустой action должен приводить к ERROR")
    void testEmptyAction() {
        String token = TokenGenerator.generateToken();

        Response response = ApiClient.sendRequest(token, "");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что пустой токен считается ошибочным.
     */
    @Test
    @Description("Пустой токен в LOGIN должен возвращать ERROR")
    void testEmptyToken() {
        Response response = ApiClient.sendRequest("", "LOGIN");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет обработку ошибки внешнего сервиса /auth.
     */
    @Test
    @Description("Если /auth вернул ошибку — LOGIN должен давать result=ERROR")
    void testAuthExternalFail() {
        String token = TokenGenerator.generateToken();

        Response response = ApiClient.sendRequestWithMock(token, "LOGIN", "/auth-fail");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет ошибку внешней операции ACTION.
     */
    @Test
    @Description("Если /doAction возвращает ошибку — ACTION должен завершаться ERROR")
    void testDoActionExternalFail() {
        String token = TokenGenerator.generateToken();

        ApiClient.sendRequest(token, "LOGIN");

        Response response = ApiClient.sendRequestWithMock(token, "ACTION", "/doAction-fail");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет реакцию API на зависание внешнего сервиса.
     */
    @Test
    @Description("/auth зависает → LOGIN должен вернуть ERROR")
    void testExternalTimeout() {
        stubFor(post("/auth")
                .willReturn(aResponse()
                        .withFixedDelay(10000)));

        String token = TokenGenerator.generateToken();

        Response response = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет граничное значение: токен слишком длинный.
     */
    @Test
    @Description("Токен длиной 33 символа невалидный — ожидается ERROR")
    void testTokenLengthTooLong() {
        String token = "A".repeat(33);

        Response response = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("ERROR", response.jsonPath().getString("result"));

        ApiClient.sendRequest(token, "LOGOUT");
    }

    /**
     * Проверяет ошибку при пустом API-key.
     */
    @Test
    @Description("Пустой X-Api-Key должен приводить к ERROR")
    void testEmptyApiKey() {
        String token = TokenGenerator.generateToken();

        Response response =
                given()
                        .contentType("application/x-www-form-urlencoded")
                        .header("X-Api-Key", "")
                        .formParam("token", token)
                        .formParam("action", "LOGIN")
                        .post("/endpoint");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Проверяет чувствительность API-key к регистру.
     */
    @Test
    @Description("API-key в неправильном регистре должен приводить к ERROR")
    void testApiKeyWrongCase() {
        String token = TokenGenerator.generateToken();

        Response response =
                given()
                        .header("X-Api-Key", "QAZwsxedc")
                        .formParam("token", token)
                        .formParam("action", "LOGIN")
                        .post("/endpoint");

        assertEquals("ERROR", response.jsonPath().getString("result"));
    }
}
