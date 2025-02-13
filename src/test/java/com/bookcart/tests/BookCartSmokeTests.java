package com.bookcart.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.parsing.Parser;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import org.junit.*;
import java.util.List;
import java.util.Random;

public class BookCartSmokeTests {

    private static String userId = "ac123";
    private static String bookId;
    private static String token;

    @BeforeClass
    public static void setup() {
        RestAssured.baseURI = "https://bookcart.azurewebsites.net/api";
        RestAssured.defaultParser = Parser.JSON;
        
        System.out.println("Fetching book list...");
        Response bookResponse = given()
                .header("Accept", "application/json")
            .when()
                .get("/book")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .extract().response();
        
        System.out.println("Book List Response: " + bookResponse.getBody().asString());
        int bookCount = bookResponse.jsonPath().getList("bookId").size();
        int randomIndex = new Random().nextInt(bookCount);
        bookId = bookResponse.jsonPath().getString("[" + randomIndex + "].bookId");
        
        System.out.println("Randomly selected bookId: " + bookId);
        Assert.assertNotNull("Book ID should not be null", bookId);
    }
    
    @AfterClass
    public static void cleanup() {
        if (token != null) {
            System.out.println("Cleaning up: Removing book from cart...");
            given()
                .header("Authorization", "Bearer " + token)
            .when()
                .delete("/ShoppingCart/{userId}/{bookId}", userId, bookId)
            .then()
                .statusCode(anyOf(is(200), is(204))); // API može vratiti 204 No Content
            System.out.println("Cleanup complete.");
        }
    }
    
    @Test
    public void testValidLogin() {
        System.out.println("Testing: Valid login to retrieve token...");
        String loginPayload = "{ \"username\": \"ac123\", \"password\": \"Ac12345678\" }";
        
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(loginPayload)
            .when()
                .post("/Login")
            .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().response();
        
        token = loginResponse.jsonPath().getString("token");
        System.out.println("Retrieved token: " + token);
        Assert.assertNotNull("Token should be present in login response", token);
        Assert.assertFalse("Token should not be empty", token.trim().isEmpty());
    }
    
    @Test
    public void testGetAllBooks() {
        System.out.println("Testing: Fetching all books...");
        Response response = given()
            .header("Accept", "application/json")
        .when()
            .get("/book")
        .then()
            .statusCode(200)
            .body("size()", greaterThan(0))
            .extract().response();
        
        System.out.println("Book List Response: " + response.getBody().asString());
        Assert.assertNotNull("Response should not be null", response.getBody().asString());
    }
    
    @Test
    public void testAddBookToCart() {
        System.out.println("Testing: Adding a random book to cart...");
        System.out.println("Book ID being sent to API: " + bookId);
        
        Response response = given()
            .header("Content-Type", "application/json")
        .when()
            .post("/ShoppingCart/AddToCart/{userId}/{bookId}", userId, bookId)
        .then()
            .statusCode(200)
            .extract().response();
        
        String responseBody = response.getBody().asString();
        System.out.println("API Response: " + responseBody);
        
        List<Integer> bookIdsInCart = response.jsonPath().getList("book.bookId");
        System.out.println("Books in cart: " + bookIdsInCart);
        Assert.assertTrue("Cart should contain added book", bookIdsInCart.contains(Integer.parseInt(bookId)));
    }

    @Test
    public void testLoginAndPurchaseCart() {
        System.out.println("Testing: Logging in...");
        token = loginAndGetToken();
        Assert.assertNotNull("Token should be present", token);
        System.out.println("Login successful, token: " + token);

        addBookToCart();
        checkoutCart();
    }

    private String loginAndGetToken() {
        String loginPayload = "{ \"username\": \"ac123\", \"password\": \"Ac12345678\" }";
        
        Response loginResponse = given()
                .header("Content-Type", "application/json")
                .body(loginPayload)
            .when()
                .post("/Login")
            .then()
                .statusCode(200)
                .extract().response();
        
        return loginResponse.jsonPath().getString("token");
    }

    private void addBookToCart() {
        System.out.println("Testing: Adding book to cart with token...");
        given()
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
        .when()
            .post("/ShoppingCart/AddToCart/{userId}/{bookId}", userId, bookId)
        .then()
            .statusCode(200)
            .body("[0].quantity", greaterThan(0));
        System.out.println("Book successfully added to cart.");
    }

    private void checkoutCart() {
        System.out.println("Testing: Checking out cart...");
        String checkoutPayload = "{ \"cartTotal\": 10, \"orderDetails\": [ { \"book\": { \"bookId\": " + bookId + ", \"title\": \"Test Book\", \"author\": \"Test Author\", \"category\": \"Test Category\", \"price\": 10, \"coverFileName\": \"test.jpg\" }, \"quantity\": 1 } ] }";
        
        Response checkoutResponse = given()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .body(checkoutPayload)
            .when()
                .post("/CheckOut/{userId}", userId)
            .then()
                .statusCode(200)
                .extract().response();
        
        String checkoutResponseBody = checkoutResponse.getBody().asString();
        System.out.println("Checkout response body: " + checkoutResponseBody);
    }
}
