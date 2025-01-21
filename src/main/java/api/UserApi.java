package api;

import io.qameta.allure.Step ;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserLogin;
import com.fasterxml.jackson.databind.ObjectMapper;
import static io.restassured.RestAssured.given;

public class UserApi extends RestApi {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Step("Регистрация пользователя с данными: {requestBody}")
    public ValidatableResponse registerUser(UserDataLombok userData) {
        String requestBody;

        try {
            requestBody = objectMapper.writeValueAsString(userData);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации данных пользователя", e);
        }

        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/register")
                .then();
    }

    @Step("Получение токена для пользователя с email: {email}")
    public String getToken(String email, String password) {
        UserLogin userLogin = new UserLogin(email, password);
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(userLogin);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации данных пользователя", e);
        }
        ValidatableResponse response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post(BASE_URL + "/auth/login")
                .then();

        return response.extract().path("accessToken");
    }


    @Step("Авторизация пользователя с email: {email}")
    public ValidatableResponse loginUser(UserLogin userLogin) {
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(userLogin);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации данных пользователя", e);
        }
        return given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post( BASE_URL + "/auth/login")
                .then();
    }

    @Step("Обновление данных пользователя")
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



    @Step("Удаление пользователя с токеном")
    public ValidatableResponse deleteUser(String token) {
        return given()
                .header("Authorization", "Bearer " + token)
                .delete(BASE_URL + "/auth/user")
                .then();
    }
}






