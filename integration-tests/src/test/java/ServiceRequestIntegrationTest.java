import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ServiceRequestIntegrationTest {
    @BeforeAll
    static void beforeAll() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    void shouldReturnServiceRequestsWithValidToken() {
        String token = loginAndGetToken();

        given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/service-requests")
            .then()
            .statusCode(200)
            .body("$", notNullValue());
    }

    @Test
    void shouldCreateServiceRequestAndReturnProvisionedStatus() {
        String token = loginAndGetToken();
        long suffix = System.currentTimeMillis();
        String studentId = "S" + suffix;
        String studentEmail = "thread2.integration+" + suffix + "@campus.edu";

        String createPayload = """
                {
                  "requestType": "SOFTWARE_LICENSE",
                  "studentId": "%s",
                  "studentEmail": "%s",
                  "department": "Data Science",
                  "submittedAt": "2026-04-03T12:00:00"
                }
                """.formatted(studentId, studentEmail);

        given()
            .header("Authorization", "Bearer " + token)
            .contentType("application/json")
            .body(createPayload)
            .when()
            .post("/api/service-requests")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("requestType", equalTo("SOFTWARE_LICENSE"))
            .body("studentId", equalTo(studentId))
            .body("studentEmail", equalTo(studentEmail))
            .body("status", equalTo("PROVISIONED"));
    }

    private String loginAndGetToken() {
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;

        return given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");
    }
}
