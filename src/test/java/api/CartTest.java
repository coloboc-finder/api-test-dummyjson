package api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CartTest {

    private static final String BASE_URL = "https://dummyjson.com";
    private static final String VALID_CART_ID = "/1";
    private static final String INVALID_CART_ID = "/99999999999";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = "/carts";
    }
    @Test
    @DisplayName("Получить список корзин")
    public void testGetAllCarts() {
        given()
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
    @DisplayName("Получить корзину с валидным ID")
    public void testGetCartById() {
        given()
                .when()
                .get(VALID_CART_ID)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("id", equalTo(VALID_CART_ID))
                .body("products", not(empty()))
                .body("total", notNullValue());
    }

    @Test
    @DisplayName("Ошибка при несуществующем ID")
    public void testGetCartByInvalidId() {
        given()
                .basePath(INVALID_CART_ID)
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
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/add")
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
    @DisplayName("Попытка добавить несуществующий товар")
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
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/add")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .body("products.size()", equalTo(0))
                .body("total", equalTo(0))
                .body("discountedTotal", equalTo(0));
    }

    @Test
    @DisplayName("Слишком большое/нулевое/отрицательное количество")
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
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/add")
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }
}
