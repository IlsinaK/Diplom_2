package api;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserLogin;
import com.google.gson.Gson;

import static io.restassured.RestAssured.given;

public class UserApi extends RestApi {


    private final Gson gson = new Gson();


    @Step("Регистрация пользователя")
    public ValidatableResponse registerUser(UserDataLombok userData) {
        String requestBody = gson.toJson(userData);

        return RestAssured
                .given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/register")
                .then();
    }


    @Step("Получение токена пользователя")
    public String getToken(UserLogin userLogin) {
        String requestBody = gson.toJson(userLogin);

        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/login")
                .then()
                .extract()
                .path("accessToken");
    }

    @Step("Авторизация пользователя")
    public ValidatableResponse loginUser(UserLogin userLogin) {
        String requestBody = gson.toJson(userLogin);

        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/login")
                .then();
    }

    @Step("Обновление данных пользователя")
    public ValidatableResponse updateUser(UserDataLombok userData, String authToken) {
        String requestBody = gson.toJson(userData);

        var requestSpec = given()
                .contentType("application/json")
                .body(requestBody);

        if (authToken != null) {
            requestSpec.header("Authorization", authToken);
        }

        return requestSpec
                .when()
                .patch(BASE_URL + "/auth/user")
                .then();
    }

    @Step("Удаление пользователя с токеном")
    public ValidatableResponse deleteUser(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .delete(BASE_URL + "/auth/user")
                .then();
    }
}







