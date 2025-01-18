package api;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class UserApi extends RestApi {

    public ValidatableResponse registerUser(String requestBody) {
        return RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/register")
                .then();
    }

    public String getToken(String email, String password) {
        String requestBody = String.format("{ \"email\": \"%s\", \"password\": \"%s\" }", email, password);
        ValidatableResponse response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/login")
                .then();

        return response.extract().path("accessToken");
    }


    public ValidatableResponse loginUser(String email, String password) {
        String requestBody = String.format("{ \"email\": \"%s\", \"password\": \"%s\" }", email, password);
        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post( BASE_URL + "/auth/login")
                .then();
    }

    public ValidatableResponse updateUser(String requestBody, String authToken) {
        var requestSpec = given()
                .contentType("application/json")
                .body(requestBody);

        if (authToken != null) { // Добавляем заголовок только если токен не null
            requestSpec.header("Authorization", authToken);
        }

        return requestSpec
                .when()
                .patch(BASE_URL + "/auth/user")
                .then();
    }



    public ValidatableResponse deleteUser(String token, String password) {
        return RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .delete(BASE_URL + "/auth/user")
                .then();
    }
}






