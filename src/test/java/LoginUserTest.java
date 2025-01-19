import api.UserApi;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserGenerator;
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
        user = UserGenerator.getRandomUser();
        userApi.registerUser(createUserJson(user));
    }

    @Test
    @Step("Логин под существующим пользователем")
    public void loginExistingUser() {
        ValidatableResponse response = userApi.loginUser(user.getEmail(), user.getPassword());

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @Step("Логин с неверным логином")
    public void loginWithInvalidEmail() {
        String requestBody = "{ \"email\": \"invalid@example.com\", \"password\": \"" + user.getPassword() + "\" }";
        ValidatableResponse response = userApi.loginUser("invalid@example.com", user.getPassword());

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
    }

    @Test
    @Step("Логин с неверным паролем")
    public void loginWithInvalidPassword() {
        ValidatableResponse response = userApi.loginUser(user.getEmail(), "wrongpassword");

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("email or password are incorrect"));
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
