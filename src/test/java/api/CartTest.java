package api;

import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CartTest {

    private static final String BASE_URL = "https://dummyjson.com";

    @Test
    @DisplayName("получить список корзин")
    public void testGetAllCarts() {
        given()
                .baseUri(BASE_URL)
                .basePath("/carts")
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("carts", notNullValue())
                .body("carts.size()", greaterThan(0));
    }

    @Test
    @DisplayName("получить корзину с валидным ID")
    public void testGetCartById() {
        given()
                .baseUri("https://dummyjson.com")
                .basePath("/carts/1")
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("products", not(empty()))
                .body("total", notNullValue());
    }

    @Test
    @DisplayName("ошибка при несуществующем ID")
    public void testGetCartByInvalidId() {
        given()
                .baseUri("https://dummyjson.com")
                .basePath("/carts/99999999999")
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }

    @Test
    @DisplayName("POST с валидным телом запроса")
    public void testAddProductToCart() {
        String requestBody = """
        {
          "userId": 1,
          "products": [
            { "id": 1, "quantity": 2 },
            { "id": 50, "quantity": 1 }
          ]
        }
        """;
        given()
                .baseUri("https://dummyjson.com")
                .basePath("/carts/add")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("userId", equalTo(1))
                .body("products.size()", equalTo(2))
                .body("products[0].id", equalTo(1))
                .body("products[0].quantity", equalTo(2))
                .body("products[1].id", equalTo(50))
                .body("products[1].quantity", equalTo(1));
    }

    @Test
    @DisplayName("попытка добавить несуществующий товар")
    public void testAddProductWithInvalidProductId() {
        String requestBody = """
        {
          "userId": 1,
          "products": [
            { "id": 99999999999, "quantity": 1 }
          ]
        }
        """;
        given()
                .baseUri("https://dummyjson.com")
                .basePath("/carts/add")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("products.size()", equalTo(0))
                .body("total", equalTo(0))
                .body("discountedTotal", equalTo(0));
    }

    @Test
    @DisplayName("слишком большое/нулевое/отрицательное количество")
    public void testAddProductWithInvalidQuantity() {
        String requestBody = """
        {
          "userId": 99999999999,
          "products": [
            { "id": 99999999999, "quantity": 99999999999 }
          ]
        }
        """;
        given()
                .baseUri("https://dummyjson.com")
                .basePath("/carts/add")
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post()
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }
}
