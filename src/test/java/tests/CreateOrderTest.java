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

public class CreateOrderTest {

    private OrderApi orderApi;
    private UserApi userApi;
    private List<String> ingredientIds;
    private UserDataLombok user;
    private String authToken;
    private String refreshToken;

    @Before
    public void setUp() {
        orderApi = new OrderApi();
        userApi = new UserApi();
        ingredientIds = orderApi.getIngredientIds();

        user = UserGenerator.getRandomUser();
        String requestBody = String.format("{ \"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\" }",
                user.getEmail(), user.getPassword(), user.getName());

        userApi.registerUser(requestBody); // Регистрация пользователя

        ValidatableResponse authResponse = userApi.loginUser(user.getEmail(), user.getPassword()); // Авторизация
        authToken = authResponse.extract().path("accessToken");
    }

    @Test
    @Step("Создание заказа с авторизацией")
    public void createOrderWithAuth() {
        if (ingredientIds.isEmpty()) {
            throw new AssertionError("Список идентификаторов ингредиентов пустой. Проверьте метод getIngredientIds.");
        }

        String ingredientId = "\"" + ingredientIds.get(0) + "\""; // Обернуть в кавычки
        String orderRequest = "{ \"ingredients\": [" + ingredientId + "] }"; // Используем один ингредиент
        System.out.println("Создаем заказ с ингредиентом: " + orderRequest); // Отладочная информация
        ValidatableResponse response = orderApi.createOrder(orderRequest, authToken);

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @Step("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        String orderRequest = "{ \"ingredients\": [] }";
        ValidatableResponse response = orderApi.createOrder(orderRequest, authToken);

        response.log().all()
                .assertThat()
                .statusCode(400)
                .body("success", is(false))
                .body("message", is("Ingredient ids must be provided"));
    }

    @Test
    @Step("Создание заказа с некорректным хешем ингредиентов")
    public void createOrderWithInvalidIngredientHash() {
        String orderRequest = "{ \"ingredients\": [\"61c0c5a71d1f82001bdaaa6l\"] }"; // Некорректный хэш
        ValidatableResponse response = orderApi.createOrder(orderRequest, authToken);

        response.log().all()
                .assertThat()
                .statusCode(500);
    }

    @Test
    @Step("Создание заказа без авторизации")
    public void createOrderWithoutRegistration() {
        // Удаляем пользователя перед проверкой без авторизации
        if (user != null) {
            String deleteToken = userApi.getToken(user.getEmail(), user.getPassword());
            userApi.deleteUser(deleteToken, user.getPassword());
        }

        String orderRequest = "{ \"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\"] }";
        // Не передаем authToken
        ValidatableResponse response = orderApi.createOrder(orderRequest, null);

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }


    @After
    public void tearDown() {
        if (user != null) {
            String deleteToken = userApi.getToken(user.getEmail(), user.getPassword());
            userApi.deleteUser(deleteToken, user.getPassword());
        }
    }
}
