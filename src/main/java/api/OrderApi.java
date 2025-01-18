package api;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

import java.util.List;

import static io.restassured.RestAssured.given;

public class OrderApi extends RestApi {

    public List<String> getIngredientIds() {
        ValidatableResponse response = RestAssured.given()
                .when()
                .get(BASE_URL + "/api/ingredients")
                .then()
                .statusCode(200);

        List<String> ingredientIds = response.extract().jsonPath().getList("data._id");  // Получаем список ID ингредиентов из ответа

        return ingredientIds;
    }


    public ValidatableResponse createOrder(String orderRequest, String authToken) {
        var request = RestAssured.given()
                .contentType("application/json")
                .body(orderRequest);

        // Добавляем заголовок только если токен не null
        if (authToken != null) {
            request.header("Authorization", authToken);
        }

        return request.when()
                .post(BASE_URL + "/api/orders")
                .then();
    }


    public ValidatableResponse getOrders(String authToken) {
        var request = given().baseUri(BASE_URL);

        if (authToken != null) {
            request.header("Authorization", authToken);
        }

        return request.when()
                .get("/orders")
                .then();
    }

}
