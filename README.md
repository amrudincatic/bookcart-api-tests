Overview

This project contains automated API tests for the BookCart application using RestAssured and JUnit. The tests cover smoke tests - authentication, book retrieval, cart operations, and order checkout.


Technologies Used

    Java (JUnit for test execution)
    RestAssured (for API testing)
    Maven (for dependency management)
    Git (for version control)
    
    
Project Structure

/bookcart-api-tests
│── src/test/java/com/bookcart/tests
│   ├── BookCartSmokeTests.java         # Main API test suite
│   ├── Utility methods for token handling and setup
│── pom.xml                             # Maven dependencies
│── README.md                           # Project documentation
│── atlantbh_ac_API_test_cases.xlsx     # Test cases documentation


How to Run Tests

Clone the Repository
    git clone https://github.com/amrudincatic/bookcart-api-tests.git
    cd bookcart-api-tests

Install Dependencies
    mvn clean install

Run the Test Suite
    mvn test
    

Test Cases

The test suite covers the following functionalities:
    Authentication Tests: Valid login attempts.
    Book Tests: Fetch all books, retrieve details, handle invalid book IDs.
    Cart Operations: Add, remove, and verify cart contents.
    Order Checkout: Successful and failed order placements.
    

Notes

    Ensure you have Java 8+ and Maven installed before running tests.
    API endpoints are based on https://bookcart.azurewebsites.net.
    A valid token is required for certain test cases (handled in test setup).
