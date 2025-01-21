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


import static org.hamcrest.Matchers.is;

public class LoginUserTest {

    private UserApi userApi;
    private UserDataLombok user;

    @Before
    public void setUp() {
        userApi = new UserApi();
        user = UserGenerator.getRandomUser();
        userApi.registerUser(user);
    }

    @Test
    @DisplayName("Логин существующего пользователя")
    @Description("Этот тест проверяет успешный логин для уже зарегистрированного пользователя.")
    public void loginExistingUser() {
        ValidatableResponse response = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword()));

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("Логин с некорректным email")
    @Description("Этот тест проверяет, что логин с некорректным email возвращает сообщение об ошибке.")
    public void loginWithInvalidEmail() {
        ValidatableResponse response = userApi.loginUser(new UserLogin("invalid@example.com", user.getPassword()));

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с некорректным паролем")
    @Description("Этот тест проверяет, что логин с некорректным паролем возвращает сообщение об ошибке.")
    public void loginWithInvalidPassword() {
        ValidatableResponse response = userApi.loginUser(new UserLogin(user.getEmail(), "wrongpassword"));

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        String deleteToken = userApi.getToken(user.getEmail(), user.getPassword());
        userApi.deleteUser(deleteToken);
    }
}
