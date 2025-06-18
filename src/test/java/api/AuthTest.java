package api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthTest {

    private static final String BASE_URL = "https://dummyjson.com";
    private static final String CORRECT_USERNAME = "emilys";
    private static final String CORRECT_PASSWORD = "emilyspass";
    private static final String INCORRECT_USERNAME = "emilys000";
    private static final String INCORRECT_PASSWORD = "emilyspass000";
    private static final String USERNAME_CASE_VARIANT = "EmIlYs";
    private static final String EMPTY_USERNAME = "";
    private static final String EMPTY_PASSWORD = "";


    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    @DisplayName("Успешный вход")
    public void testSuccessfulLogin() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"username\": \"%s\", \"password\": \"%s\"}",
                        CORRECT_USERNAME, CORRECT_PASSWORD))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("username", equalTo(CORRECT_USERNAME))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Повторный вход")
    public void testRepeatLoginWithSameUser() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"username\": \"%s\", \"password\": \"%s\"}",
                        CORRECT_USERNAME, CORRECT_PASSWORD))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("username", equalTo(CORRECT_USERNAME))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Вход с логином в другом регистре")
    public void testLoginWithCaseInsensitiveUsername() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"username\": \"%s\", \"password\": \"%s\"}",
                        USERNAME_CASE_VARIANT, CORRECT_PASSWORD))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("username", equalTo(CORRECT_USERNAME))
                .body("accessToken", notNullValue());
    }

    @Test
    @DisplayName("Вход с не верным паролем")
    public void testLoginWithIncorrectPassword() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"username\": \"%s\", \"password\": \"%s\"}",
                        CORRECT_USERNAME, INCORRECT_PASSWORD))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход с не верным логином")
    public void testLoginWithIncorrectUsername() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"username\": \"%s\", \"password\": \"%s\"}",
                        INCORRECT_USERNAME, CORRECT_PASSWORD))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход с пустыми логином и паролем")
    public void testLoginWithEmptyUsernameAndPassword() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"username\": \"%s\", \"password\": \"%s\"}",
                        EMPTY_USERNAME, EMPTY_PASSWORD))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход, где передан только логин, без пароля")
    public void testLoginWithOnlyUsername() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"username\": \"%s\"}",
                        INCORRECT_USERNAME))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход,где передан только пароль, без логина")
    public void testLoginWithOnlyPassword() {
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"password\": \"%s\"}",
                        CORRECT_PASSWORD))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход с невалидным JSON-форматом")
    public void testLoginWithInvalidJsonFormat() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"%s\", \"password\": \"%s\"")
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход с SQL-инъекцией в логине")
    public void testLoginWithSqlInjectionInUsername() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"' OR '1'='1\", \"password\": \"any\"")
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход с XSS в пароле")
    public void testLoginWithXssInPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\": \"emilys\", \"password\": \"<script>alert('XSS')</script>\"")
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход с очень длинными логином и паролем")
    public void testLoginWithLongUsernameAndPassword() {
        String longUsername = "a".repeat(1000);
        String longPassword = "b".repeat(1000);

        given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", longUsername, longPassword))
                .when()
                .post("/auth/login")
                .then()
                .log().ifValidationFails()
                .statusCode(400);
    }

}
