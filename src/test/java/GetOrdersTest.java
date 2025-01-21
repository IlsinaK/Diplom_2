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


public class GetOrdersTest {

    private OrderApi orderApi;
    private UserApi userApi;
    private UserDataLombok user;
    private String authToken;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        userApi = new UserApi();
        orderApi = new OrderApi();
        user = UserGenerator.getRandomUser();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Создание и получение заказа для авторизованного пользователя")
    @Description("Этот тест проверяет создание заказа для авторизованного пользователя и получение списка заказов.")
    public void createAndGetOrderForAuthorizedUser() {
        userApi.registerUser(user);

        authToken = userApi.loginUser(new UserLogin(user.getEmail(), user.getPassword()))  // Авторизация и получение токена
                .extract()
                .path("accessToken");

        List<String> ingredientIds = orderApi.getIngredientIds();   // Получаем список ID ингредиентов перед созданием заказа

        if (ingredientIds.isEmpty()) {
            throw new IllegalStateException("Ингредиенты не найдены");// Убедимся, что ID ингредиентов получены
        }

        String orderRequest;
        try {
            orderRequest = objectMapper.writeValueAsString(new OrderRequest(Collections.singletonList(ingredientIds.get(0))));
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сериализации объекта заказа", e);
        }
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
    @DisplayName("Получение заказов неавторизованного пользователя")
    @Description("Этот тест проверяет, что неавторизованный пользователь не может получить заказы и получает соответствующее сообщение об ошибке.")
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
            String deleteToken = userApi.getToken(user.getEmail(), user.getPassword()); // Получаем токен после авторизации для удаления пользователя
            userApi.deleteUser(deleteToken);
        }
    }
}
