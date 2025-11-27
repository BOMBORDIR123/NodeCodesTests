package by.javaguru.nordcodestests.tests;

import by.javaguru.nordcodestests.api.ApiClient;
import by.javaguru.nordcodestests.base.TestBase;
import by.javaguru.nordcodestests.utils.TokenGenerator;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Набор тестов, покрывающий сценарии использования действия ACTION.
 * ACTION — это бизнес-операция пользователя, доступная только после успешного LOGIN.
 *
 * Тесты проверяют:
 * - корректное выполнение ACTION при валидной сессии
 * - невозможность выполнения действия без LOGIN
 * - реакцию сервиса на ошибку внешнего сервиса /doAction
 * - валидацию токенов
 * - поведение в пограничных состояниях
 */
public class ActionTests extends TestBase {

    /**
     * Пользователь проходит успешную аутентификацию (LOGIN),
     * после чего выполняет действие ACTION.
     *
     * Ожидается:
     * - внешний сервис /auth доступен и возвращает 200
     * - внешний сервис /doAction доступен и возвращает 200
     * - приложение корректно сохраняет токен после LOGIN
     * - выполнение ACTION завершается успешным ответом "OK"
     */
    @Test
    @Description("Пользователь успешно выполняет действие ACTION после успешного LOGIN. "
            + "Проверяется полный позитивный сценарий: успешная аутентификация и выполнение действия.")
    void testActionSuccess() {
        String token = TokenGenerator.generateToken();

        Response loginResponse = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", loginResponse.jsonPath().getString("result"));

        Response actionResponse = ApiClient.sendRequest(token, "ACTION");
        assertEquals("OK", actionResponse.jsonPath().getString("result"));
    }

    /**
     * Попытка выполнить ACTION без предварительного LOGIN.
     *
     * Ожидается:
     * - внутреннее хранилище не содержит токена
     * - действие запрещено
     * - сервис возвращает "ERROR"
     */
    @Test
    @Description("Попытка выполнить ACTION без аутентификации. Приложение должно корректно отклонить запрос и вернуть ERROR.")
    void testActionWithoutLogin() {
        String token = TokenGenerator.generateToken();

        Response actionResponse = ApiClient.sendRequest(token, "ACTION");

        assertEquals("ERROR", actionResponse.jsonPath().getString("result"));
    }

    /**
     * Внешний сервис /doAction возвращает ошибку (500 Internal Server Error).
     *
     * Ожидается:
     * - LOGIN проходит успешно
     * - при попытке ACTION внешний сервис недоступен
     * - приложение корректно обрабатывает ошибку и возвращает "ERROR"
     */
    @Test
    @Description("Ошибка внешнего сервиса /doAction. Даже при успешном LOGIN выполнение ACTION должно завершиться ERROR.")
    void testActionExternalServiceFail() {
        stubFor(post("/doAction")
                .willReturn(aResponse().withStatus(500)));

        String token = TokenGenerator.generateToken();

        Response loginResponse = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", loginResponse.jsonPath().getString("result"));

        Response actionResponse = ApiClient.sendRequest(token, "ACTION");

        assertEquals("ERROR", actionResponse.jsonPath().getString("result"));
    }

    /**
     * Токен содержит запрещённые символы.
     *
     * Ожидается:
     * - запрос сразу отклоняется
     * - к внешнему сервису /doAction запрос не отправляется
     * - приложение возвращает "ERROR"
     */
    @Test
    @Description("Отправка ACTION с токеном, содержащим недопустимые символы. Приложение должно вернуть ERROR на уровне валидации входных данных.")
    void testActionInvalidCharacters() {
        Response response = ApiClient.sendRequest("@@@###^^^", "ACTION");
        assertEquals("ERROR", response.jsonPath().getString("result"));
    }

    /**
     * Попытка выполнить ACTION после неуспешного LOGIN.
     *
     * Ожидается:
     * - LOGIN отклонён
     * - токен не сохраняется
     * - ACTION невозможен
     * - сервис возвращает "ERROR"
     */
    @Test
    @Description("ACTION после неуспешного LOGIN. Если токен не был сохранён в сессии, ACTION должен возвращать ERROR.")
    void testActionAfterFailedLogin() {
        String token = "BAD_TOKEN";

        Response login = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("ERROR", login.jsonPath().getString("result"));

        Response action = ApiClient.sendRequest(token, "ACTION");
        assertEquals("ERROR", action.jsonPath().getString("result"));
    }
}
