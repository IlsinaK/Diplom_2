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

public class CreateUserTest {

    private UserApi userApi;
    private UserDataLombok user; // Теперь это UserRegistration

    @Before
    public void setUp() {
        userApi = new UserApi();
        user = UserGenerator.getRandomUser(); // Теперь возвращает UserRegistration
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Тест проверяет возможность регистрации нового пользователя с уникальными данными.")
    public void createUniqueUser() {
        ValidatableResponse response = userApi.registerUser(user); // Используем UserRegistration для регистрации

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("Создание существующего пользователя")
    @Description("Тест проверяет попытку регистрации пользователя с уже существующими данными.")
    public void createExistingUser() {

        ValidatableResponse firstResponse = userApi.registerUser(user);
        firstResponse.assertThat().statusCode(200); // Проверка успешного создания пользователя

        ValidatableResponse response = userApi.registerUser(user);
        response.log().all()
                .assertThat()
                .statusCode(403)
                .body("success", is(false))
                .body("message", is("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без email")
    @Description("Тест проверяет попытку регистрации пользователя без указания email.")
    public void createUserWithoutEmail() {
        UserDataLombok newUser = new UserDataLombok(null, "password", "Username");
        ValidatableResponse response = userApi.registerUser(newUser);

        response.log().all()
                .assertThat()
                .statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без пароля")
    @Description("Тест проверяет попытку регистрации пользователя без указания пароля.")
    public void createUserWithoutPassword() {
        UserDataLombok newUser = new UserDataLombok(user.getEmail(), null, user.getName());
        ValidatableResponse response = userApi.registerUser(newUser);

        response.log().all()
                .assertThat()
                .statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без имени")
    @Description("Тест проверяет попытку регистрации пользователя без указания имени.")
    public void createUserWithoutName() {
        UserDataLombok newUser = new UserDataLombok(user.getEmail(), user.getPassword(), null);
        ValidatableResponse response = userApi.registerUser(newUser);

        response.log().all()
                .assertThat()
                .statusCode(403)
                .body("success", is(false))
                .body("message", is("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
            String deleteToken = userApi.getToken(new UserLogin(user.getEmail(), user.getPassword())); // Получаем токен для удаления пользователя
            userApi.deleteUser(deleteToken);
        }

}



