package java.com.codeofcarbon.account;

import com.codeofcarbon.account.AccountServiceApplication;
import com.google.gson.JsonObject;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.UnexpectedError;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.hyperskill.hstest.common.JsonUtils.getJson;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.*;

public class Test2 extends SpringTest {

    private final static String signUpApi = "/api/auth/signup";
    private final static String paymentApi = "/api/empl/payment";
    private final MockUser johnDoe = new MockUser("John", "Doe", "JohnDoe@acme.com", "secret");
    private final MockUser maxMustermann = new MockUser("Max", "Mustermann", "MaxMustermann@acme.com", "secret");
    private final MockUser captainNemo = new MockUser("Captain", "Nemo", "nautilus@pompilius.com", "wings");
    private final String johnDoeCorrectUser = johnDoe.toJson();
    private final String johnDoeEmptyName = new MockUser(johnDoe).setName("").toJson();
    private final String johnDoeNoName = new MockUser(johnDoe).setName(null).toJson();
    private final String johnDoeEmptyLastName = new MockUser(johnDoe).setLastname("").toJson();
    private final String johnDoeNoLastName = new MockUser(johnDoe).setLastname(null).toJson();
    private final String johnDoeEmptyEmail = new MockUser(johnDoe).setEmail("").toJson();
    private final String johnDoeNoEmail = new MockUser(johnDoe).setLastname(null).toJson();
    private final String johnDoeEmptyPassword = new MockUser(johnDoe).setPassword("").toJson();
    private final String johnDoeNoPassword = new MockUser(johnDoe).setPassword(null).toJson();
    private final String johnDoeWrongEmail1 = new MockUser(johnDoe).setEmail("johndoeacme.com").toJson();
    private final String johnDoeWrongEmail2 = new MockUser(johnDoe).setEmail("johndoe@google.com").toJson();
    private final String maxMustermannCorrectUser = maxMustermann.toJson();
    private final String johnDoeCorrectUserLower = new MockUser(johnDoe)
            .setEmail(johnDoe.getEmail().toLowerCase()).toJson();
    private final String maxMustermannCorrectUserLower = new MockUser(maxMustermann)
            .setEmail(maxMustermann.getEmail().toLowerCase()).toJson();
    private final String johnDoeWrongPassword = new MockUser(johnDoe).setPassword("none").toJson();
    private final String johnDoeWrongPasswordCaseSensitive = new MockUser(johnDoe)
            .setPassword(johnDoe.getPassword().toUpperCase()).toJson();
    private final String maxMustermannWrongPassword = new MockUser(maxMustermann).setPassword("none").toJson();
    private final String captainNemoWrongUser = captainNemo.toJson();

    List<Integer> userIdList = new ArrayList<>();

    public Test2() {
        super(AccountServiceApplication.class, "../src/main/resources/service_db.mv.db");
    }

    /**
     * method for checking status code of response Post request for API
     *
     * @param body   string representation of body content in JSON format (String)
     * @param status required http status for response (int)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testPostApi(String body, int status, String message) {
        HttpResponse response = post(signUpApi, body).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + signUpApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }
        return CheckResult.correct();
    }

    /**
     * method for checking status code of response Get request for API
     *
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testGetApi() {
        HttpResponse response = get(paymentApi).send();

        if (response.getStatusCode() != HttpStatus.UNAUTHORIZED.value()) {
            return CheckResult.wrong("GET " + paymentApi + " should respond with "
                                     + "status code " + HttpStatus.UNAUTHORIZED.value() +
                                     ", responded: " + response.getStatusCode() + "\n"
                                     + "This api only for authenticated user");
        }
        return CheckResult.correct();
    }

    /**
     * method for checking response on Post request for signup API
     *
     * @param body string representation of body content in JSON format (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testPostSignUpResponse(String body) {
        HttpResponse response = post(signUpApi, body).send();
        testPostApi(body, HttpStatus.OK.value(), "API must be available");

        JsonObject rightResponse = getJson(body).getAsJsonObject();
        rightResponse.remove("password");

        // check is it JSON in response or something else
        if (!response.getJson().isJsonObject()) {
            return CheckResult.wrong("Wrong object in response, expected JSON but was \n" +
                                     response.getContent().getClass());
        }

        JsonObject jsonResponse = response.getJson().getAsJsonObject();

        // check if password is presence in response
        if (jsonResponse.get("password") != null) {
            return CheckResult.wrong("You must remove password from response\n" +
                                     jsonResponse);
        }

        if (!jsonResponse.get("email").getAsString().endsWith("@acme.com")) {
            return CheckResult.wrong("Service must accept only corporate emails that end with @acme.com\n" +
                                     jsonResponse);
        }

        if (jsonResponse.get("id") == null) {
            return CheckResult.wrong("Response must contain user ID\n" +
                                     "Received response:\n" +
                                     jsonResponse);
        }

        if (userIdList.contains(jsonResponse.get("id").getAsInt())) {
            return CheckResult.wrong("User ID must be unique!\n" +
                                     "Received response:\n" +
                                     jsonResponse);
        }
        rightResponse.addProperty("id", jsonResponse.get("id").toString());

        // check JSON in response
        expect(response.getContent()).asJson()
                .check(isObject()
                        .value("id", isInteger())
                        .value("name", rightResponse.get("name").getAsString())
                        .value("lastname", rightResponse.get("lastname").getAsString())
                        .value("email", rightResponse.get("email").getAsString().toLowerCase()));
        userIdList.add(jsonResponse.get("id").getAsInt());
        return CheckResult.correct();
    }

    /**
     * method for restarting application
     */
    private CheckResult restartApplication() {
        try {
            reloadSpring();
        } catch (Exception ex) {
            throw new UnexpectedError(ex.getMessage());
        }
        return CheckResult.correct();
    }

    /**
     * method for checking authentication
     *
     * @param user    string representation of user information in JSON format (String)
     * @param status  required http status for response (int)
     * @param message hint about reason of error (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    private CheckResult testUserRegistration(String user, int status, String message) {
        JsonObject userJson = getJson(user).getAsJsonObject();
        String password = userJson.get("password").getAsString();
        String login = userJson.get("email").getAsString();
        HttpResponse response = get(paymentApi).basicAuth(login, password).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("Get " + paymentApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Authentication with " + login + " / " + password);
        }
        // check JSON in response
        if (status == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("id", isInteger())
                            .value("name", userJson.get("name").getAsString())
                            .value("lastname", userJson.get("lastname").getAsString())
                            .value("email", userJson.get("email").getAsString().toLowerCase()));
        }
        return CheckResult.correct();
    }

    /**
     * method for testing duplicate users
     *
     * @param user string representation of user information in JSON format (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    private CheckResult testUserDuplicates(String user) {
        HttpResponse response = post(signUpApi, user).send();
        // check error message field in JSON response
        expect(response.getContent()).asJson().check(
                isObject()
                        .value("status", 400)
                        .value("error", "Bad Request")
                        .value("message", "User exist!")
                        .anyOtherValues());
        return CheckResult.correct();
    }

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // ===================================================================== test user registration on signup api
            () -> testPostSignUpResponse(johnDoeCorrectUser),
            () -> testPostApi(johnDoeCorrectUser, 400, "User must be unique!"),
            () -> testUserDuplicates(johnDoeCorrectUser),
            () -> testPostApi(johnDoeCorrectUserLower, 400, "User must be unique (ignorecase)!"),
            () -> testPostSignUpResponse(maxMustermannCorrectUserLower),
            () -> testPostApi(maxMustermannCorrectUserLower, 400, "User must be unique!"),
            () -> testPostApi(maxMustermannCorrectUser, 400, "User must be unique (ignorecase)!"),

            // =================================================================== test wrong POST request for signup api
            () -> testPostApi(johnDoeEmptyName, 400, "Empty name field!"),
            () -> testPostApi(johnDoeNoName, 400, "Name field is absent!"),
            () -> testPostApi(johnDoeEmptyLastName, 400, "Empty lastname field!"),
            () -> testPostApi(johnDoeNoLastName, 400, "Lastname field is absent!"),
            () -> testPostApi(johnDoeEmptyEmail, 400, "Empty email field!"),
            () -> testPostApi(johnDoeNoEmail, 400, "Email field is absent!"),
            () -> testPostApi(johnDoeEmptyPassword, 400, "Empty password field!"),
            () -> testPostApi(johnDoeNoPassword, 400, "Password field is absent!"),
            () -> testPostApi(johnDoeWrongEmail1, 400, "Wrong email!"),
            () -> testPostApi(johnDoeWrongEmail2, 400, "Wrong email!"),

            // ====================================================================== test authentication, positive tests
            () -> testUserRegistration(johnDoeCorrectUserLower, 200, "User must login!"),
            () -> testUserRegistration(johnDoeCorrectUser, 200, "Login case insensitive!"),
            () -> testUserRegistration(maxMustermannCorrectUserLower, 200, "User must login!"),
            () -> testUserRegistration(maxMustermannCorrectUser, 200, "Login case insensitive!"),

            // ====================================================================== test authentication, negative tests
            () -> testUserRegistration(johnDoeWrongPassword, 401, "Wrong password!"),
            () -> testUserRegistration(johnDoeWrongPasswordCaseSensitive, 401, "Password must be case sensitive!"),
            () -> testUserRegistration(johnDoeWrongEmail1, 401, "Wrong user!"),
            () -> testUserRegistration(maxMustermannWrongPassword, 401, "Wrong password!"),
            () -> testUserRegistration(captainNemoWrongUser, 401, "Wrong user"),
            () -> testGetApi(),

            // ========================================================================================= test persistence
            this::restartApplication,
            () -> testUserRegistration(johnDoeCorrectUser, 200, "User must login, after restarting! Check persistence."),
    };
}