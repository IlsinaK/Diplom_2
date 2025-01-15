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

public class CreateUserTest {

    private UserApi userApi;
    private UserDataLombok user;

    @Before
    public void setUp() {
        userApi = new UserApi();
        user = UserGenerator.getRandomUser();
    }

    @Test
    @Step("Создание уникального пользователя")
    public void createUniqueUser() {
        String requestBody = createUserJson(user);
        ValidatableResponse response = userApi.registerUser(requestBody);

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @Step("Создание пользователя, который уже существует")
    public void createExistingUser() {
        String requestBody = createUserJson(user);
        ValidatableResponse firstResponse = userApi.registerUser(requestBody);
        firstResponse.assertThat().statusCode(200); // Проверка успешного создания пользователя

        // Повторная регистрация
        ValidatableResponse response = userApi.registerUser(requestBody);
        response.log().all()
                .assertThat()
                .statusCode(403)
                .body("success", is(false))
                .body("message", is("User already exists"));
    }

    @Test
    @Step("Создание пользователя без электронной почты")
    public void createUserWithoutEmail() {
        String requestBody = "{ \"password\": \"password\", \"name\": \"Username\" }";
        ValidatableResponse response = userApi.registerUser(requestBody);

        response.log().all()
                .assertThat()
                .statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @Step("Создание пользователя без пароля")
    public void createUserWithoutPassword() {
        String requestBody = String.format("{ \"email\": \"%s\", \"name\": \"%s\" }", user.getEmail(), user.getName());
        ValidatableResponse response = userApi.registerUser(requestBody);

        response.log().all()
                .assertThat()
                .statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
                String deleteToken = userApi.getToken(user.getEmail(), user.getPassword()); // Получите токен
        userApi.deleteUser(deleteToken, user.getPassword()); // Удаление пользователя после теста
    }
    private String createUserJson(UserDataLombok user) {
        return String.format("{ \"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\" }",
                user.getEmail(), user.getPassword(), user.getName());
    }
}
