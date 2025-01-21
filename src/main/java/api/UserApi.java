package api;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserLogin;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import static io.restassured.RestAssured.given;

public class UserApi extends RestApi {

    private final ObjectMapper objectMapper = new ObjectMapper();


        @Step("Регистрация пользователя")
        public ValidatableResponse registerUser(UserDataLombok userData) {
            String requestBody = serializeUserDataLombok(userData); // Сериализация объекта UserDataLombok

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
        String requestBody = serializeUserLogin(userLogin); // Сериализация объекта

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
        String requestBody = serializeUserLogin(userLogin); // Сериализация объекта UserLogin

        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/login")
                .then();
    }

    @Step("Обновление данных пользователя")
    public ValidatableResponse updateUser(UserDataLombok userData, String authToken) {
        String requestBody = serializeUserData(userData); // Сериализация объекта UserDataLombok

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

    @Step("Удаление пользователя с токеном")
    public ValidatableResponse deleteUser(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .delete(BASE_URL + "/auth/user")
                .then();
    }

    // Метод для сериализации объекта UserRegistration в JSON-строку
    private String serializeUserDataLombok(UserDataLombok userDataLombok) {
        try {
            return objectMapper.writeValueAsString(userDataLombok);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации данных пользователя", e);
        }
    }

    // Метод для сериализации объекта UserLogin в JSON-строку
    private String serializeUserLogin(UserLogin userLogin) {
        try {
            return objectMapper.writeValueAsString(userLogin);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации данных пользователя", e);
        }
    }

    // Метод для сериализации объекта UserDataLombok в JSON-строку
    private String serializeUserData(UserDataLombok userData) {
        try {
            return objectMapper.writeValueAsString(userData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации данных пользователя", e);
        }
    }
}







