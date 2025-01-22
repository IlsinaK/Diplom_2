package api;

import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;

import java.util.List;

import static io.restassured.RestAssured.given;

public class OrderApi extends RestApi {

    @Step ("Получение ID ингредиентов")
    public List<String> getIngredientIds() {
        ValidatableResponse response = RestAssured.given()
                .when()
                .get(BASE_URL + "/ingredients")
                .then();


        int statusCode = response.extract().statusCode();
        System.out.println("Response status code: " + statusCode);


        if (statusCode != 200) {

            String errorResponse = response.extract().body().asString();
            System.out.println("Error response body: " + errorResponse);
            throw new RuntimeException("Ошибка API: " + statusCode + ". Ответ: " + errorResponse);
        }


        return response.extract().jsonPath().getList("data._id"); // Возвращаем ID ингредиентов
    }


    @Step("Создание заказа с запросом: {orderRequest}")
    public ValidatableResponse createOrder(String orderRequest, String authToken) {
        var request = RestAssured.given()
                .contentType("application/json")
                .body(orderRequest);

        if (authToken != null) {
            request.header("Authorization", authToken);
        }

        return request.when()
                .post(BASE_URL + "/orders")
                .then();
    }


    @Step("Получение заказов")
    public ValidatableResponse getOrders(String authToken) {
        var request = given().baseUri(BASE_URL);

        if (authToken != null) {
            request.header("Authorization", authToken);
        }

        return request.when()
                .get(BASE_URL + "/orders")
                .then();
    }

}
