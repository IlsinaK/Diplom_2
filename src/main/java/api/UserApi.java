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
                .post("https://stellarburgers.nomoreparties.site/api/auth/register")
                .then();
    }

    public String getToken(String email, String password) {
        String requestBody = String.format("{ \"email\": \"%s\", \"password\": \"%s\" }", email, password);
        ValidatableResponse response = given()
                .baseUri(BASE_URL)
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
                .baseUri(BASE_URL)
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post( "/auth/login")
                .then();
    }

    public ValidatableResponse updateUser(String requestBody, String token) {
        return given()
                .baseUri(BASE_URL)
                .contentType("application/json")
                .header("Authorization", "Bearer " + token)
                .body(requestBody)
                .when()
                .patch("/auth/user") // Используйте относительный путь
                .then();
    }

    public void logoutUser(String refreshToken) {
        String requestBody = String.format("{ \"token\": \"%s\" }", refreshToken);
        ValidatableResponse response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/auth/logout")
                .then();
        response.assertThat().statusCode(200); // Проверяем, что выход успешен
    }



    public ValidatableResponse deleteUser(String token, String password) {
        return RestAssured
                .given()
                .header("Authorization", "Bearer " + token)
                .delete(BASE_URL + "/auth/user")
                .then();
    }
}






