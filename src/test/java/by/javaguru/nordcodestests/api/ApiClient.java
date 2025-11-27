package by.javaguru.nordcodestests.api;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public final class ApiClient {

    private static final String API_KEY = "qazWSXedc";

    private ApiClient() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Response sendRequest(String token, String action) {
        return
                given()
                        .log().all()
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("X-Api-Key", API_KEY)
                        .formParam("token", token)
                        .formParam("action", action)
                        .when()
                        .post(Endpoints.MAIN)
                        .then()
                        .log().all()
                        .extract()
                        .response();
    }

    public static Response sendRequestWithMock(String token, String action, String mockEndpoint) {
        return
                given()
                        .log().all()
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("X-Api-Key", API_KEY)
                        .formParam("token", token)
                        .formParam("action", action)
                        .when()
                        .post(mockEndpoint)
                        .then()
                        .log().all()
                        .extract()
                        .response();
    }
}