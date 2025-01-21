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

import static org.hamcrest.Matchers.is;

public class LoginUserTest {

    private UserApi userApi;
    private UserDataLombok user;

    @Before
    public void setUp() {
        userApi = new UserApi();
        user = UserGenerator.getRandomUser(); // Генерируем данные для нового пользователя
        userApi.registerUser(user);
    }

    @Test
    @DisplayName("Логин существующего пользователя")
    @Description("Этот тест проверяет успешный логин для уже зарегистрированного пользователя.")
    public void loginExistingUser() {
        ValidatableResponse response = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword())); // Используем UserLogin для авторизации

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true)); // Проверяем успешный логин
    }

    @Test
    @DisplayName("Логин с некорректным email")
    @Description("Этот тест проверяет, что логин с некорректным email возвращает сообщение об ошибке.")
    public void loginWithInvalidEmail() {
        ValidatableResponse response = userApi.loginUser(new UserLogin("invalid@example.com", user.getPassword())); // Попробуем мошеннический email

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect")); // Проверяем сообщение об ошибке
    }

    @Test
    @DisplayName("Логин с некорректным паролем")
    @Description("Этот тест проверяет, что логин с некорректным паролем возвращает сообщение об ошибке.")
    public void loginWithInvalidPassword() {
        ValidatableResponse response = userApi.loginUser(new UserLogin(user.getEmail(), "wrongpassword")); // Пробуем неверный пароль

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect")); // Проверяем сообщение об ошибке
    }

    @After
    public void tearDown() {
        String deleteToken = userApi.getToken(new UserLogin(user.getEmail(), user.getPassword())); // Получаем токен для удаления пользователя
        userApi.deleteUser(deleteToken); // Удаление пользователя после теста
    }
}