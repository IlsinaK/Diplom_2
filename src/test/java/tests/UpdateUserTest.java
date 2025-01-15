package tests;

import api.UserApi;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.is;

public class UpdateUserTest {

    private UserApi userApi;
    private UserDataLombok user;
    private String authToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
        user = UserGenerator.getRandomUser();

        // Регистрация пользователя с уникальными данными
        String requestBody = createUserJson(user);
        userApi.registerUser(requestBody);

        // Авторизация пользователя и получение токена
        ValidatableResponse authResponse = userApi.loginUser(user.getEmail(), user.getPassword());

        // Печать токена для проверки
        authToken = authResponse.extract().path("accessToken");
        System.out.println("Полученный токен: " + authToken);
    }

    @Test
    @Step("Изменение имени пользователя с авторизацией")
    public void updateUserNameWithAuth() {
        String updatedUserName = "NewName"; // новое имя пользователя
        String requestBody = String.format("{ \"name\": \"%s\" }", updatedUserName);

        ValidatableResponse response = userApi.updateUser(requestBody, "Bearer " + authToken);


        // Проверка кода и тела ответа
        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true))
                .body("user.name", is(updatedUserName)); // Проверка изменения имени
    }

    @Test
    @Step("Попытка изменить имя пользователя без авторизации")
    public void updateUserNameWithoutAuth() {
        String requestBody = "{ \"name\": \"NewName\" }";
        ValidatableResponse response = userApi.updateUser(requestBody, null);

        // Проверка кода и тела ответа
        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @Test
    @Step("Изменение email пользователя с авторизацией")
    public void updateUserEmailWithAuth() {
        String requestBody = "{ \"email\": \"new_email@example.com\" }";
        ValidatableResponse response = userApi.updateUser(requestBody, authToken); // используем authToken

        // Проверка кода и тела ответа
        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @Step("Попытка изменить email пользователя без авторизации")
    public void updateUserEmailWithoutAuth() {
        String requestBody = "{ \"email\": \"new_email@example.com\" }";
        ValidatableResponse response = userApi.updateUser(requestBody, null);

        // Проверка кода и тела ответа
        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @After
    public void tearDown() {

        String deleteToken = userApi.getToken(user.getEmail(), user.getPassword());
        userApi.deleteUser(deleteToken, user.getPassword());
    }

    private String createUserJson(UserDataLombok user) {
        return String.format("{ \"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\" }",
                user.getEmail(), user.getPassword(), user.getName());
    }
}
