package tests;

import api.OrderApi;
import api.UserApi;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.UserDataLombok;
import model.UserGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;

public class GetOrdersTest {

    private OrderApi orderApi;
    private UserApi userApi;
    private UserDataLombok user;
    private String authToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
        orderApi = new OrderApi();
        user = UserGenerator.getRandomUser();

        // Регистрация пользователя
        String userJson = createUserJson(user);
        userApi.registerUser(userJson);

        // Авторизация и получение токена
        authToken = userApi.loginUser(user.getEmail(), user.getPassword())
                .extract()
                .path("accessToken");
    }

    @Test
    @Step("Получение заказов авторизованного пользователя")
    public void getOrdersForAuthorizedUser() {
        ValidatableResponse response = orderApi.getOrders(authToken); // Используем токен

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true))
                .body("orders", is(List.of())) // Проверка на наличие заказов, ожидаем пустой список за нового пользователя
                .body("total", is(0)) // Ожидаем общее количество 0
                .body("totalToday", is(0)); // Ожидаем количество заказов за сегодня 0
    }

    @Test
    @Step("Получение заказов неавторизованного пользователя")
    public void getOrdersForUnauthorizedUser() {
        ValidatableResponse response = orderApi.getOrders(null);

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @After
    public void tearDown() {
        // Удаляем пользователя, если служба это поддерживает
        if (user != null) {
            // Получаем токен после авторизации для удаления пользователя
            String deleteToken = userApi.loginUser(user.getEmail(), user.getPassword())
                    .extract()
                    .path("accessToken");

            userApi.deleteUser(deleteToken, user.getPassword());
        }
    }

    private String createUserJson(UserDataLombok user) {
        return String.format("{ \"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\" }",
                user.getEmail(), user.getPassword(), user.getName());
    }
}
