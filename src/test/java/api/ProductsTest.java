package api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProductsTest {

    private static final String BASE_URL = "https://dummyjson.com";
    private static final String VALID_PRODUCT_ID = "1";
    private static final String INVALID_PRODUCT_ID = "99999999999";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.basePath = "/products";
    }

    @Test
    @DisplayName("Получить все продукты")
    public void testGetAllProducts() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get()
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("products.size", notNullValue());
    }

    @Test
    @DisplayName("Получить конкретный товар")
    public void testGetProductById() {
        given()
                .when()
                .get(VALID_PRODUCT_ID)
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("id", equalTo(Integer.parseInt(VALID_PRODUCT_ID)));
    }

    @Test
    @DisplayName("Ошибка на несуществующий ID")
    public void testGetProductByInvalidId() {
        given()
                .when()
                .get(INVALID_PRODUCT_ID)
                .then()
                .log().ifValidationFails()
                .statusCode(404);
    }
    @Test
    @DisplayName("Поиск продуктов по строке")
    public void testSearchProductsByQuery() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("q", "laptop")
                .when()
                .get("/search")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("products.size()", greaterThan(0));
    }

    @Test
    @DisplayName("Поиск продуктов с отсутствующим результатом")
    public void testSearchProductsWithNoResults() {
        given()
                .contentType(ContentType.JSON)
                .queryParam("q", "nonexistentproduct")
                .when()
                .get("/search")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("products.size()", equalTo(0));
    }

    @Test
    @DisplayName("Получить все категории")
    public void testGetAllCategories() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/categories")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("categories", not(empty()));
    }

    @Test
    @DisplayName("Получить товары по категории")
    public void testGetProductsByCategory() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/category/electronics")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("products.size()", equalTo(0));
    }

    @Test
    @DisplayName("Получить товары по несуществующей категории")
    public void testGetProductsByInvalidCategory() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/category/nonexistentcategory")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("products.size()", equalTo(0));
    }
}
