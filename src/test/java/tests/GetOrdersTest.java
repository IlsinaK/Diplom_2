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
    }

    @Test
    @Step("Создание заказа и получение его для авторизованного пользователя")
    public void createAndGetOrderForAuthorizedUser() {
        String userJson = createUserJson(user);
        userApi.registerUser(userJson);

        authToken = userApi.loginUser(user.getEmail(), user.getPassword())  // Авторизация и получение токена
                .extract()
                .path("accessToken");
        List<String> ingredientIds = orderApi.getIngredientIds();   // Получаем список ID ингредиентов перед созданием заказа

        if (ingredientIds.isEmpty()) {
            throw new IllegalStateException("Ингредиенты не найдены");// Убедимся, что ID ингредиентов получены
        }

        String ingredientId = "\"" + ingredientIds.get(0) + "\""; //  Создаем заказ с первым ингредиентом
        String orderRequest = "{ \"ingredients\": [" + ingredientId + "] }"; // Используем один ингредиент
        System.out.println("Создаем заказ с ингредиентом: " + orderRequest);

        ValidatableResponse createResponse = orderApi.createOrder(orderRequest, authToken); // Создаем заказ

        createResponse.log().all()
                .assertThat()
                .statusCode(200)
                .body("success", is(true)); // Ожидаем успех


        ValidatableResponse getOrdersResponse = orderApi.getOrders(authToken); // Получаем заказы для авторизованного пользователя
        getOrdersResponse.log().all();


        getOrdersResponse.assertThat()
                .statusCode(200)
                .body("success", is(true))
                .body("orders[0].ingredients", is(List.of(ingredientIds.get(0)))); // Проверяем, что ingredients заказа совпадает с ожидаемым
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
        if (user != null) {
            String deleteToken = userApi.loginUser(user.getEmail(), user.getPassword())// Получаем токен после авторизации для удаления пользователя
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
