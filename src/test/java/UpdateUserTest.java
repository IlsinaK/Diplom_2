import api.UserApi;
import io.qameta.allure.Description;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserGenerator;
import model.UserLogin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

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
        userApi.registerUser(user);

    }

    @Test
    @DisplayName("Обновление имени пользователя с авторизацией")
    @Description("Этот тест проверяет обновление имени пользователя для авторизованного пользователя.")
    public void updateUserNameWithAuth() {
        ValidatableResponse authResponse = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword()));// Авторизация пользователя и получение токена

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
    @DisplayName("Обновление имени пользователя без авторизации")
    @Description("Этот тест проверяет попытку обновления имени пользователя без авторизации.")
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
    @DisplayName("Обновление email пользователя с авторизацией")
    @Description("Этот тест проверяет обновление email пользователя для авторизованного пользователя.")
    public void updateUserEmailWithAuth() {

        ValidatableResponse authResponse = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword()));// Авторизация пользователя и получение токена

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
    @DisplayName("Обновление email пользователя без авторизации")
    @Description("Этот тест проверяет попытку обновления email пользователя без авторизации.")
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
        userApi.deleteUser(deleteToken);
    }
}
