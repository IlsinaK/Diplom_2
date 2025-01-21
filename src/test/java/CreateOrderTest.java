import api.OrderApi;
import api.UserApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import model.OrderRequest;
import model.UserDataLombok;
import model.UserGenerator;
import model.UserLogin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.is;

public class CreateOrderTest {

    private OrderApi orderApi;
    private UserApi userApi;
    private List<String> ingredientIds;
    private String authToken;
    private UserLogin user;

    @Before
    public void setUp() {
        orderApi = new OrderApi();
        userApi = new UserApi();
        ingredientIds = orderApi.getIngredientIds();

        UserDataLombok user = UserGenerator.getRandomUser();
        userApi.registerUser(new UserDataLombok(user.getEmail(), user.getPassword(), user.getName())); // Исправлено на использование UserRegistration

        ValidatableResponse authResponse = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword())); // Авторизация
        authToken = authResponse.extract().path("accessToken");
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Тест проверяет возможность создания заказа с использованием действительного токена авторизации и одного ингредиента.")
    public void createOrderWithAuth() {
        if (ingredientIds.isEmpty()) {
            throw new AssertionError("Список идентификаторов ингредиентов пустой. Проверьте метод getIngredientIds.");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String orderRequest;
        try {
            orderRequest = objectMapper.writeValueAsString(new OrderRequest(Collections.singletonList(ingredientIds.get(0))));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сериализации объекта заказа", e);
        }
        System.out.println("Создаем заказ с ингредиентом: " + orderRequest); // Отладочная информация
        ValidatableResponse response = orderApi.createOrder(orderRequest, authToken);

        response.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Тест проверяет процесс создания заказа без указания ингредиентов и ожидает ошибку 400.")
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
    @DisplayName("Создание заказа с некорректным хэшем ингредиента")
    @Description("Тест проверяет создание заказа с некорректным хэшем ингредиента и ожидает ошибку 500.")
    public void createOrderWithInvalidIngredientHash() {
        String orderRequest = "{ \"ingredients\": [\"61c0c5a71d1f82001bdaaa6l\"] }"; // Некорректный хэш
        ValidatableResponse response = orderApi.createOrder(orderRequest, authToken);

        response.log().all()
                .assertThat()
                .statusCode(500);
    }

    @Test
    @DisplayName("Создание заказа без регистрации")
    @Description("Тест проверяет возможность создания заказа без авторизации и ожидает успешный ответ.")
    public void createOrderWithoutRegistration() {
        // Удаляем пользователя перед проверкой без авторизации
        if (user != null) {
            String deleteToken = userApi.getToken(new UserLogin(user.getEmail(), user.getPassword())); // Теперь используем UserLogin
            userApi.deleteUser(deleteToken);
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
            String deleteToken = userApi.getToken(new UserLogin(user.getEmail(), user.getPassword())); // Теперь используем UserLogin
            userApi.deleteUser(deleteToken);
        }
    }
}

