package tests;

import api.UserApi;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

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

    }

    @Test
    @Step("Изменение имени пользователя с авторизацией")
    public void updateUserNameWithAuth() {
        ValidatableResponse authResponse = userApi.loginUser(user.getEmail(), user.getPassword());// Авторизация пользователя и получение токена

        authToken = authResponse.extract().path("accessToken");
        System.out.println("Полученный токен: " + authToken);// Печать токена для проверки

        String updatedUserName = "NewName"; // новое имя пользователя
        String requestBody = String.format("{ \"name\": \"%s\" }", updatedUserName);

        ValidatableResponse response = userApi.updateUser(requestBody, authToken);

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

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }


    @Test
    @Step("Изменение email пользователя с авторизацией")
    public void updateUserEmailWithAuth() {

        ValidatableResponse authResponse = userApi.loginUser(user.getEmail(), user.getPassword());// Авторизация пользователя и получение токена

        authToken = authResponse.extract().path("accessToken");
        System.out.println("Полученный токен: " + authToken);

        String uniqueEmail = "test-" + UUID.randomUUID() + "@yandex.ru";
        String requestBody = "{ \"email\": \"" + uniqueEmail + "\", \"name\": \"NewName\" }";

        ValidatableResponse response = userApi.updateUser(requestBody, authToken);

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @Step("Попытка изменить email пользователя без авторизации")
    public void updateUserEmailWithoutAuth() {
        String requestBody = "{ \"email\": \"test-new_email@yandex.ru\" }";
        ValidatableResponse response = userApi.updateUser(requestBody, null);

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
