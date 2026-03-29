package tests;

import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CandidateApiTest {

    private static final String BASE_URL  = "https://opensource-demo.orangehrmlive.com";
    private static final String USERNAME  = "Admin";
    private static final String PASSWORD  = "admin123";

    private static final CookieFilter cookieFilter = new CookieFilter();
    private static int candidateId;

    @BeforeSuite
    public void globalSetup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeClass
    public void login() {
        System.out.println("🔐 Step 0 – Authenticating with OrangeHRM …");

        // 1. Fetch the login page to grab the dynamic CSRF token
        String loginPage = given()
                .filter(cookieFilter)
                .when()
                .get("/web/index.php/auth/login")
                .then()
                .statusCode(200)
                .extract().body().asString();

        String csrfToken = extractCsrfToken(loginPage);
        System.out.println("   CSRF token : " + csrfToken);

        // 2. Submit login form
        given()
            .filter(cookieFilter)
            .contentType("application/x-www-form-urlencoded")
            .formParam("_username",   USERNAME)
            .formParam("_password",   PASSWORD)
            .formParam("_csrf_token", csrfToken)
        .when()
            .post("/web/index.php/auth/validate")
        .then()
            .statusCode(anyOf(is(200), is(302)));

        System.out.println("✅ Login successful – session cookie stored");
    }

  
    @Test(priority = 1, description = "Verify viewCandidates page returns 200")
    public void navigateToCandidatesPage() {
        System.out.println("\n── STEP 1 : Navigate to View Candidates ──");

        given()
            .filter(cookieFilter)
        .when()
            .get("/web/index.php/recruitment/viewCandidates")
        .then()
            .statusCode(200)
            .body(containsString("OrangeHRM"));

        System.out.println("✅ /recruitment/viewCandidates returned 200");
    }

  
    @Test(priority = 2, description = "Verify Add a new candidate via REST API")
    public void addCandidate() {
        System.out.println("\n── STEP 2a : Add Candidate via REST API ──");

        Map<String, Object> payload = new HashMap<>();
        payload.put("firstName",         "John");
        payload.put("middleName",        "REST");
        payload.put("lastName",          "Assured");
        payload.put("email",             "john.assured." + System.currentTimeMillis() + "@example.com");
        payload.put("contactNumber",     "0501234567");
        payload.put("keywords",          "java, restassured, automation");
        payload.put("dateOfApplication", LocalDate.now().toString());   // YYYY-MM-DD
        payload.put("comment",           "Added via REST Assured Java automation");
        payload.put("consentToKeepData", true);

        System.out.println("📤 POST /api/v2/recruitment/candidates");
        System.out.println("   Payload : " + payload);

        Response response = given()
                .filter(cookieFilter)
                .contentType(ContentType.JSON)
                .body(payload)
            .when()
                .post("/web/index.php/api/v2/recruitment/candidates")
            .then()
                .statusCode(anyOf(is(200), is(201)))
                .body("data.id",        notNullValue())
                .body("data.firstName", equalTo("John"))
                .body("data.lastName",  equalTo("Assured"))
                .extract().response();

        candidateId = response.jsonPath().getInt("data.id");
        System.out.println("✅ Candidate created – ID: " + candidateId);
    }

  
    @Test(priority = 3,
          description  = "GET candidate by ID and verify details",
          dependsOnMethods = "addCandidate")
    public void verifyCandidate() {
        System.out.println("\n── Verify Candidate in System ──");

        given()
            .filter(cookieFilter)
        .when()
            .get("/web/index.php/api/v2/recruitment/candidates/" + candidateId)
        .then()
            .statusCode(200)
            .body("data.id",        equalTo(candidateId))
            .body("data.firstName", equalTo("John"))
            .body("data.lastName",  equalTo("Assured"));

        System.out.println("✅ Candidate ID " + candidateId + " confirmed in OrangeHRM");
    }

  
    @Test(priority = 4,
          description  = "Delete candidate via REST API",
          dependsOnMethods = "verifyCandidate")
    public void deleteCandidate() {
        System.out.println("\n── Delete Candidate via REST API ──");

        Map<String, List<Integer>> deletePayload = new HashMap<>();
        deletePayload.put("ids", List.of(candidateId));

        System.out.println("🗑️  DELETE /api/v2/recruitment/candidates  body=" + deletePayload);

        given()
            .filter(cookieFilter)
            .contentType(ContentType.JSON)
            .body(deletePayload)
        .when()
            .delete("/web/index.php/api/v2/recruitment/candidates")
        .then()
            .statusCode(anyOf(is(200), is(204)));

        System.out.println("✅ Candidate ID " + candidateId + " deleted successfully");
    }

    
    @Test(priority = 5,
          description  = "Confirm deleted candidate returns 404",
          dependsOnMethods = "deleteCandidate")
    public void confirmDeletion() {
        System.out.println("\n── Confirm Deletion (404) ──");

        given()
            .filter(cookieFilter)
        .when()
            .get("/web/index.php/api/v2/recruitment/candidates/" + candidateId)
        .then()
            .statusCode(404);

        System.out.println("✅ Candidate ID " + candidateId + " confirmed deleted (404)");
    }

    private String extractCsrfToken(String html) {
        String marker = "name=\"_csrf_token\"";
        int idx = html.indexOf(marker);
        if (idx == -1) throw new RuntimeException("CSRF token input not found in login page HTML");

        int valueStart = html.indexOf("value=\"", idx) + 7;
        int valueEnd   = html.indexOf("\"", valueStart);
        return html.substring(valueStart, valueEnd);
    }
}
