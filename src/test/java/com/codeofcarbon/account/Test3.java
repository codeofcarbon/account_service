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

import static org.hyperskill.hstest.common.JsonUtils.*;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isInteger;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isObject;

public class Test3 extends SpringTest {

    private final static String signUpApi = "/api/auth/signup";
    private final static String changePassApi = "/api/auth/changepass";
    private final static String paymentApi = "/api/empl/payment";
    private final TestRequest johnDoe = new TestRequest()
            .setProps("name", "John")
            .setProps("lastname", "Doe")
            .setProps("email", "JohnDoe@acme.com")
            .setProps("password", "oMoa3VvqnLxW");
    private final TestRequest maxMus = new TestRequest()
            .setProps("name", "Max")
            .setProps("lastname", "Mustermann")
            .setProps("email", "MaxMustermann@acme.com")
            .setProps("password", "ai0y9bMvyF6G");
    private final TestRequest captainNemo = new TestRequest()
            .setProps("name", "Captain")
            .setProps("lastname", "Nemo")
            .setProps("email", "nautilus@pompilius.com")
            .setProps("password", "wings");

    private final String jDCorrectUser = johnDoe.toJson();
    private final String jDEmptyName = new TestRequest(johnDoe).setProps("name", "").toJson();
    private final String jDNoName = new TestRequest(johnDoe).setProps("name", null).toJson();
    private final String jDEmptyLastName = new TestRequest(johnDoe).setProps("lastname", "").toJson();
    private final String jDNoLastName = new TestRequest(johnDoe).setProps("lastname", null).toJson();
    private final String jDEmptyEmail = new TestRequest(johnDoe).setProps("email", "").toJson();
    private final String jDNoEmail = new TestRequest(johnDoe).setProps("email", null).toJson();
    private final String jDEmptyPassword = new TestRequest(johnDoe).setProps("password", "").toJson();
    private final String jDNoPassword = new TestRequest(johnDoe).setProps("password", null).toJson();
    private final String jDWrongEmail1 = new TestRequest(johnDoe).setProps("email", "johndoeacme.com").toJson();
    private final String jDWrongEmail2 = new TestRequest(johnDoe).setProps("email", "johndoe@google.com").toJson();
    private final String maxMusCorrectUser = maxMus.toJson();
    private final String jDLower = new TestRequest(johnDoe).setProps("email", "johndoe@acme.com").toJson();
    private final String maxMusLower = new TestRequest(maxMus).setProps("email", "maxmustermann@acme.com").toJson();
    private final String jDWrongPassword = new TestRequest(johnDoe).setProps("password", "none").toJson();
    private final String maxMusWrongPassword = new TestRequest(maxMus).setProps("password", "none").toJson();
    private final String captainNemoWrongUser = captainNemo.toJson();
    private final String jDDuplicatePass = new TestRequest().setProps("new_password", "oMoa3VvqnLxW").toJson();
    private final String jDShortPass1 = new TestRequest().setProps("new_password", "o").toJson();
    private final String jDShortPass2 = new TestRequest().setProps("new_password", "oM").toJson();
    private final String jDShortPass3 = new TestRequest().setProps("new_password", "oMo").toJson();
    private final String jDShortPass4 = new TestRequest().setProps("new_password", "oMoa").toJson();
    private final String jDShortPass5 = new TestRequest().setProps("new_password", "oMoa3").toJson();
    private final String jDShortPass6 = new TestRequest().setProps("new_password", "oMoa3V").toJson();
    private final String jDShortPass7 = new TestRequest().setProps("new_password", "oMoa3Vv").toJson();
    private final String jDShortPass8 = new TestRequest().setProps("new_password", "oMoa3Vvq").toJson();
    private final String jDShortPass9 = new TestRequest().setProps("new_password", "oMoa3Vvqn").toJson();
    private final String jDShortPass10 = new TestRequest().setProps("new_password", "oMoa3Vvqno").toJson();
    private final String jDShortPass11 = new TestRequest().setProps("new_password", "oMoa3VvqnoM").toJson();
    private final String jDPass = new TestRequest().setProps("new_password", "aNob5VvqzRtb").toJson();

    private final static String[] breachedPass = new String[]{
            "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
            "PasswordForApril", "PasswordForMay", "PasswordForJune",
            "PasswordForJuly", "PasswordForAugust", "PasswordForSeptember",
            "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"};

    List<Integer> userIdList = new ArrayList<>();

    public Test3() {
        super(AccountServiceApplication.class, "../src/main/resources/service_db.mv.db");
    }

    /**
     * method for checking status code of response Post requests for API
     *
     * @param api    testing api (String)
     * @param body   string representation of body content in JSON format (String)
     * @param status required http status for response (int)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testPostApi(String api, String body, int status, String message) {
        HttpResponse response = post(api, body).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + api + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }
        return CheckResult.correct();
    }

    CheckResult testPostApiWithAuth(String body, String message) {
        HttpResponse response = post(changePassApi, body)
                .basicAuth("JohnDoe@acme.com", "oMoa3VvqnLxW").send();

        if (response.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
            return CheckResult.wrong("POST " + changePassApi + " should respond with "
                                     + "status code " + HttpStatus.BAD_REQUEST.value() +
                                     ", responded: " + response.getStatusCode() + "\n"
                                     + "Endpoint not found!" + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }

        if (response.getStatusCode() != HttpStatus.BAD_REQUEST.value()) {
            return CheckResult.wrong("POST " + changePassApi + " should respond with "
                                     + "status code " + HttpStatus.BAD_REQUEST.value() +
                                     ", responded: " + response.getStatusCode() + "\n"
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
     * method for checking status code of response Get request for API
     *
     * @param status   required http status for response (int)
     * @param password password
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testGetApiAuth(int status, String password) {
        HttpResponse response = get(paymentApi).basicAuth("JohnDoe@acme.com", password).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("GET " + paymentApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + "Password must be changed!");
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
        testPostApi(signUpApi, body, HttpStatus.OK.value(), "API must be available");

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
        String login = userJson.get("email").getAsString().toLowerCase();
        HttpResponse response = get(paymentApi).basicAuth(login, password).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("Get " + paymentApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Authentication with " + login + " / " + password);
        }
        // check JSON in response
        if (status == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("id", isInteger())
                            .value("name", userJson.get("name").getAsString())
                            .value("lastname", userJson.get("lastname").getAsString())
                            .value("email", userJson.get("email").getAsString().toLowerCase()));
        }
        return CheckResult.correct();
    }

    CheckResult testChangePassword(String body, String user) {
        JsonObject userJson = getJson(user).getAsJsonObject();
        String pass = userJson.get("password").getAsString();
        String login = userJson.get("email").getAsString().toLowerCase();
        HttpResponse response = post(changePassApi, body).basicAuth(login, pass).send();
        if (response.getStatusCode() != HttpStatus.OK.value()) {
            return CheckResult.wrong("POST " + changePassApi + " should respond with "
                                     + "status code " + HttpStatus.OK.value() + ", responded: " + response.getStatusCode() + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }
        // check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("email", userJson.get("email").getAsString().toLowerCase())
                            .value("status", "The password has been updated successfully"));
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
        expect(response.getContent()).asJson()
                .check(isObject()
                        .value("status", HttpStatus.BAD_REQUEST.value())
                        .value("error", "Bad Request")
                        .value("message", "User exist!")
                        .anyOtherValues());
        return CheckResult.correct();
    }

    private CheckResult testBreachedPass(String api, String login, String password, String body) {
        JsonObject json = getJson(body).getAsJsonObject();
        HttpResponse response;
        for (String pass : breachedPass) {
            if (json.has("password")) {
                json.remove("password");
                json.addProperty("password", pass);
            } else if (json.has("new_password")) {
                json.remove("new_password");
                json.addProperty("new_password", pass);
            }
            response = login.isEmpty() || password.isEmpty() ?
                    post(api, json.toString()).send()
                    : post(api, json.toString()).basicAuth(login, password).send();

            if (response.getStatusCode() != HttpStatus.BAD_REQUEST.value()) {
                return CheckResult.wrong("POST " + api + " should respond with "
                                         + "status code 400 , responded: " + response.getStatusCode() + "\n"
                                         + "Response body:\n" + response.getContent() + "\n"
                                         + "Request body:\n" + getPrettyJson(json) + "\n"
                                         + "Sending password from breached list");
            }
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("status", HttpStatus.BAD_REQUEST.value())
                            .value("error", "Bad Request")
                            .value("message", "The password is in the hacker's database!")
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // ================================================================= test wrong POST request for signup api
            () -> testPostApi(signUpApi, jDEmptyName, 400, "Empty name field!"),
            () -> testPostApi(signUpApi, jDNoName, 400, "Name field is absent!"),
            () -> testPostApi(signUpApi, jDEmptyLastName, 400, "Empty lastname field!"),
            () -> testPostApi(signUpApi, jDNoLastName, 400, "Lastname field is absent!"),
            () -> testPostApi(signUpApi, jDEmptyEmail, 400, "Empty email field!"),
            () -> testPostApi(signUpApi, jDNoEmail, 400, "Email field is absent!"),
            () -> testPostApi(signUpApi, jDEmptyPassword, 400, "Empty password field!"),
            () -> testPostApi(signUpApi, jDNoPassword, 400, "Password field is absent!"),
            () -> testPostApi(signUpApi, jDWrongEmail1, 400, "Wrong email!"),
            () -> testPostApi(signUpApi, jDWrongEmail2, 400, "Wrong email!"),

            // =================================================================== test user registration on signup api
            () -> testBreachedPass(signUpApi, "", "", jDCorrectUser),
            () -> testPostSignUpResponse(jDCorrectUser),
            () -> testPostApi(signUpApi, jDCorrectUser, 400, "User must be unique!"),
            () -> testUserDuplicates(jDCorrectUser),
            () -> testPostApi(signUpApi, jDLower, 400, "User must be unique (ignorecase)!"),
            () -> testPostSignUpResponse(maxMusLower),
            () -> testPostApi(signUpApi, maxMusLower, 400, "User must be unique!"),
            () -> testPostApi(signUpApi, maxMusCorrectUser, 400, "User must be unique (ignorecase)!"),

            // ==================================================================== test authentication, positive tests
            () -> testUserRegistration(jDLower, 200, "User must login!"),
            () -> testUserRegistration(jDCorrectUser, 200, "Login case insensitive!"),
            () -> testUserRegistration(maxMusLower, 200, "User must login!"),
            () -> testUserRegistration(maxMusCorrectUser, 200, "Login case insensitive!"),

            // ==================================================================== test authentication, negative tests
            () -> testUserRegistration(jDWrongPassword, 401, "Wrong password!"),
            () -> testUserRegistration(jDWrongEmail1, 401, "Wrong user!"),
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),
            () -> testUserRegistration(captainNemoWrongUser, 401, "Wrong user"),
            this::testGetApi,

            // ================================================================================= test changing password
            () -> testPostApi(changePassApi, jDDuplicatePass, 401, "This api only for authenticated user"),
            () -> testPostApiWithAuth(jDShortPass1, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass2, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass3, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass4, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass5, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass6, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass7, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass8, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass9, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass10, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDShortPass11, "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(jDDuplicatePass, "The passwords must be different!"),
            () -> testBreachedPass(changePassApi, "JohnDoe@acme.com", "oMoa3VvqnLxW", jDDuplicatePass),
            () -> testChangePassword(jDPass, jDCorrectUser),
            () -> testGetApiAuth(401, "oMoa3VvqnLxW"),
            () -> testGetApiAuth(200, "aNob5VvqzRtb"),

            // ======================================================================================= test persistence
            this::restartApplication,
            () -> testUserRegistration(maxMusCorrectUser, 200, "User must login, after restarting! Check persistence."),
    };
}