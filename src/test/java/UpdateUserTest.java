import api.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserGenerator;
import model.UserLogin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.is;

public class UpdateUserTest {

    private UserApi userApi;
    private UserDataLombok user; // Обновлено на UserRegistration
    private String authToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
        user = UserGenerator.getRandomUser(); // Генерация пользователя

        // Регистрация пользователя с уникальными данными
        userApi.registerUser(user);
    }

    @Test
    @DisplayName("Обновление имени пользователя с авторизацией")
    @Description("Этот тест проверяет обновление имени пользователя для авторизованного пользователя.")
    public void updateUserNameWithAuth() {
        ValidatableResponse authResponse = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword())); // Авторизация пользователя и получение токена
        authToken = authResponse.extract().path("accessToken");
        System.out.println("Полученный токен: " + authToken); // Печать токена для проверки

        UserDataLombok updatedUser = new UserDataLombok(user.getEmail(), user.getPassword(), "NewName"); // Обновленное имя пользователя

        ValidatableResponse response = userApi.updateUser(updatedUser, authToken); // Используем объект UserDataLombok

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true))
                .body("user.name", is("NewName")); // Проверка изменения имени
    }

    @Test
    @DisplayName("Обновление имени пользователя без авторизации")
    @Description("Этот тест проверяет попытку обновления имени пользователя без авторизации.")
    public void updateUserNameWithoutAuth() {
        UserDataLombok updatedUser = new UserDataLombok("", "", "NewName"); // Имя без авторизации
        ValidatableResponse response = userApi.updateUser(updatedUser, null);

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
        ValidatableResponse authResponse = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword())); // Авторизация пользователя и получение токена
        authToken = authResponse.extract().path("accessToken");
        System.out.println("Полученный токен: " + authToken);

        String uniqueEmail = "test-" + UUID.randomUUID() + "@yandex.ru";
        UserDataLombok updatedUser = new UserDataLombok(uniqueEmail, user.getPassword(), "NewName"); // Обновляемый email и имя

        ValidatableResponse response = userApi.updateUser(updatedUser, authToken); // Используем объект UserDataLombok

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("Обновление email пользователя без авторизации")
    @Description("Этот тест проверяет попытку обновления email пользователя без авторизации.")
    public void updateUserEmailWithoutAuth() {
        UserDataLombok updatedUser = new UserDataLombok("test-new_email@yandex.ru", "", ""); // Email без авторизации
        ValidatableResponse response = userApi.updateUser(updatedUser, null);

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @After
    public void tearDown() {
        String deleteToken = userApi.getToken(new UserLogin(user.getEmail(), user.getPassword())); // Получаем токен для удаления пользователя
        userApi.deleteUser(deleteToken); // Удаление пользователя после теста
    }
}

