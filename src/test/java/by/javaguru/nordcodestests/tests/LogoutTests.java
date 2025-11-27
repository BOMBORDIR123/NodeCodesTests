package by.javaguru.nordcodestests.tests;

import by.javaguru.nordcodestests.api.ApiClient;
import by.javaguru.nordcodestests.base.TestBase;
import by.javaguru.nordcodestests.utils.TokenGenerator;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Набор тестов, проверяющих корректность обработки операции LOGOUT:
 * <ul>
 *     <li>успешное завершение сессии;</li>
 *     <li>удаление токена после LOGOUT;</li>
 *     <li>ошибки при отсутствии LOGIN;</li>
 *     <li>обработка повторного LOGOUT.</li>
 * </ul>
 */
public class LogoutTests extends TestBase {

    /**
     * Проверяет успешный LOGOUT сразу после успешной аутентификации.
     */
    @Test
    @Description("Успешный LOGOUT после успешного LOGIN")
    void testLogoutSuccess() {
        String token = TokenGenerator.generateToken();

        Response login = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", login.jsonPath().getString("result"));

        Response logout = ApiClient.sendRequest(token, "LOGOUT");
        assertEquals("OK", logout.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что LOGOUT без LOGIN приводит к ошибке.
     */
    @Test
    @Description("LOGOUT без предварительного LOGIN — должен вернуть ERROR")
    void testLogoutWithoutLogin() {
        String token = TokenGenerator.generateToken();

        Response logout = ApiClient.sendRequest(token, "LOGOUT");

        assertEquals("ERROR", logout.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что после LOGOUT приложение удаляет токен,
     * и дальнейший ACTION невозможен.
     */
    @Test
    @Description("После LOGOUT токен должен быть удалён: ACTION → ERROR")
    void testLogoutRemovesToken() {
        String token = TokenGenerator.generateToken();

        Response login = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", login.jsonPath().getString("result"));

        Response logout = ApiClient.sendRequest(token, "LOGOUT");
        assertEquals("OK", logout.jsonPath().getString("result"));

        Response action = ApiClient.sendRequest(token, "ACTION");
        assertEquals("ERROR", action.jsonPath().getString("result"));
    }

    /**
     * Проверяет, что повторный LOGOUT для уже завершённой сессии возвращает ошибку.
     */
    @Test
    @Description("Повторный LOGOUT должен вернуть ERROR")
    void testDoubleLogout() {
        String token = TokenGenerator.generateToken();

        Response login = ApiClient.sendRequest(token, "LOGIN");
        assertEquals("OK", login.jsonPath().getString("result"));

        Response logout1 = ApiClient.sendRequest(token, "LOGOUT");
        assertEquals("OK", logout1.jsonPath().getString("result"));

        Response logout2 = ApiClient.sendRequest(token, "LOGOUT");
        assertEquals("ERROR", logout2.jsonPath().getString("result"));
    }
}
