package api;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

import java.util.List;

import static io.restassured.RestAssured.given;

public class OrderApi extends RestApi {

    public List<String> getIngredientIds() {
        ValidatableResponse response = RestAssured.given()
                .when()
                .get("https://stellarburgers.nomoreparties.site/api/ingredients")
                .then()
                .statusCode(200);

        // Получаем список ID ингредиентов из ответа
        List<String> ingredientIds = response.extract().jsonPath().getList("data._id");

        return ingredientIds;
    }


    public ValidatableResponse createOrder(String orderRequest, String token) {
        return RestAssured.given()
                .contentType("application/json")
                .body(orderRequest)
                .when()
                .post("https://stellarburgers.nomoreparties.site/api/orders")
                .then();
    }

    public ValidatableResponse getOrders(String token) {
        return given()
                .baseUri(BASE_URL)
                // Убираем токен из заголовка, если он не передан
                .header("Authorization", token != null ? "Bearer " + token : "")
                .when()
                .get("/orders")
                .then();
    }
}
