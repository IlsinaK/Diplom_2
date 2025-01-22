import api.OrderApi;
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

public class GetOrdersTest {

    private OrderApi orderApi;
    private UserApi userApi;
    private UserDataLombok user;
    private String authToken;

    @Before
    public void setUp() {
        userApi = new UserApi();
        orderApi = new OrderApi();
        user = UserGenerator.getRandomUser(); // Генерация данных для нового пользователя
        userApi.registerUser(user); // Регистрация пользователя

        // Авторизация и получение токена
        authToken = userApi.getToken(new UserLogin(user.getEmail(), user.getPassword())); // Использование getToken
    }

    @Test
    @DisplayName("Получение заказа для авторизованного пользователя")
    public void getOrderForAuthorizedUser() {
        // Получение заказов для авторизованного пользователя
        ValidatableResponse getOrdersResponse = orderApi.getOrders(authToken);
        getOrdersResponse.log().all();

        getOrdersResponse.assertThat()
                .statusCode(200)
                .body("success", is(true)); // Проверка успешности ответа
    }



    @Test
    @DisplayName("Получение заказов неавторизованного пользователя")
    @Description("Этот тест проверяет, что неавторизованный пользователь не может получить заказы и получает соответствующее сообщение об ошибке.")
    public void getOrdersForUnauthorizedUser() {
        ValidatableResponse response = orderApi.getOrders(null); // Получаем заказы без авторизации

        response.log().all()
                .assertThat()
                .statusCode(401)
                .body("success", is(false))
                .body("message", is("You should be authorised"));
    }

    @After
    public void tearDown() {
        if (user != null) {
            String deleteToken = userApi.getToken(new UserLogin(user.getEmail(), user.getPassword())); // Получаем токен после авторизации для удаления пользователя
            userApi.deleteUser(deleteToken); // Удаление пользователя после теста
        }
    }
}
