package com.codeofcarbon.accountservicetests;

import com.codeofcarbon.accountservice.AccountServiceApplication;
import com.google.gson.*;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.*;
import org.hyperskill.hstest.mocks.web.request.HttpRequest;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.hyperskill.hstest.common.JsonUtils.*;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.*;

public class LoggingSecurityEventsTest extends SpringTest {

    private final static String databasePath = "./src/main/resources/service_db.mv.db";
    private final static String signUpApi = "/api/auth/signup";
    private final static String changePassApi = "/api/auth/changepass";
    private final static String getEmployeePaymentApi = "/api/empl/payment";
    private final static String postPaymentApi = "/api/acct/payments";
    private final static String putRoleApi = "/api/admin/user/role";
    private final static String putAccessApi = "/api/admin/user/access";
    private final static String adminApi = "/api/admin/user/";
    private final static String auditorApi = "/api/security/events/";

    List<Integer> userIdList = new ArrayList<>();

    private final TestRequest ivanIvanov = new TestRequest()
            .setProps("name", "Ivan")
            .setProps("lastname", "Ivanov")
            .setProps("email", "IvanIvanov@acme.com")
            .setProps("password", "rXoa4CvqpLxW");
    private final TestRequest petrPetrov = new TestRequest()
            .setProps("name", "Petr")
            .setProps("lastname", "Petrov")
            .setProps("email", "PetrPetrov@acme.com")
            .setProps("password", "nWza98hjkLPE");
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

    private final String ivanIvanovCorrectUser = ivanIvanov.toJson();
    private final String petrPetrovCorrectUser = petrPetrov.toJson();
    private final String jDCorrectUser = johnDoe.toJson();
    private final String maxMusCorrectUser = maxMus.toJson();
    private final String captainNemoWrongUser = captainNemo.toJson();
    private final String jDNewPass = new TestRequest(johnDoe).setProps("password", "aNob5VvqzRtb").toJson();
    private final String jDPass = new TestRequest().setProps("new_password", "aNob5VvqzRtb").toJson();
    private final String maxMusLower = new TestRequest(maxMus).setProps("email", "maxmustermann@acme.com").toJson();
    private final String maxMusWrongPassword = new TestRequest(maxMus).setProps("password", "none").toJson();
    private final String petrPetrovWrongPassword = new TestRequest(petrPetrov).setProps("password", "none").toJson();
    private final String maxMusWrongEmail = new TestRequest(maxMus).setProps("email", "maxmustermann@google.com").toJson();

    private final String firstResponseAdminApi = convert(new String[]{
            new TestRequest()
                    .setProps("id", 1)
                    .setProps("name", "John")
                    .setProps("lastname", "Doe")
                    .setProps("email", "johndoe@acme.com")
                    .setProps("roles", new String[]{"ROLE_ADMINISTRATOR"}).toJson(),
            new TestRequest()
                    .setProps("id", 2)
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("email", "ivanivanov@acme.com")
                    .setProps("roles", new String[]{"ROLE_AUDITOR", "ROLE_USER"}).toJson(),
            new TestRequest()
                    .setProps("id", 3)
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("email", "maxmustermann@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson()
    });

    private final String secondResponseAdminApi = convert(new String[]{
            new TestRequest()
                    .setProps("id", 1)
                    .setProps("name", "John")
                    .setProps("lastname", "Doe")
                    .setProps("email", "johndoe@acme.com")
                    .setProps("roles", new String[]{"ROLE_ADMINISTRATOR"}).toJson(),
            new TestRequest()
                    .setProps("id", 2)
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("email", "ivanivanov@acme.com")
                    .setProps("roles", new String[]{"ROLE_AUDITOR", "ROLE_USER"}).toJson(),
            new TestRequest()
                    .setProps("id", 3)
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("email", "maxmustermann@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson(),
            new TestRequest()
                    .setProps("id", 5)
                    .setProps("name", "Petr")
                    .setProps("lastname", "Petrov")
                    .setProps("email", "petrpetrov@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson()
    });

    private final String[] auditorResponseApi = new String[]{
            new TestRequest()
                    .setProps("action", "CREATE_USER")
                    .setProps("subject", "Anonymous")
                    .setProps("object", "johndoe@acme.com")
                    .setProps("path", "/api/auth/signup").toJson(),
            new TestRequest()
                    .setProps("action", "CREATE_USER")
                    .setProps("subject", "Anonymous")
                    .setProps("object", "ivanivanov@acme.com")
                    .setProps("path", "/api/auth/signup").toJson(),
            new TestRequest()
                    .setProps("action", "GRANT_ROLE")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "Grant role AUDITOR to ivanivanov@acme.com")
                    .setProps("path", "/api/admin/user/role").toJson(),
            new TestRequest()
                    .setProps("action", "CREATE_USER")
                    .setProps("subject", "Anonymous")
                    .setProps("object", "maxmustermann@acme.com")
                    .setProps("path", "/api/auth/signup").toJson(),
            new TestRequest()
                    .setProps("action", "CREATE_USER")
                    .setProps("subject", "Anonymous")
                    .setProps("object", "petrpetrov@acme.com")
                    .setProps("path", "/api/auth/signup").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@google.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "nautilus@pompilius.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "GRANT_ROLE")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "Grant role ACCOUNTANT to petrpetrov@acme.com")
                    .setProps("path", "/api/admin/user/role").toJson(),
            new TestRequest()
                    .setProps("action", "REMOVE_ROLE")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "Remove role ACCOUNTANT from petrpetrov@acme.com")
                    .setProps("path", "/api/admin/user/role").toJson(),
            new TestRequest()
                    .setProps("action", "DELETE_USER")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "petrpetrov@acme.com")
                    .setProps("path", "/api/admin/user").toJson(),
            new TestRequest()
                    .setProps("action", "CHANGE_PASSWORD")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "johndoe@acme.com")
                    .setProps("path", "/api/auth/changepass").toJson(),
            new TestRequest()
                    .setProps("action", "ACCESS_DENIED")
                    .setProps("subject", "ivanivanov@acme.com")
                    .setProps("object", "/api/admin/user/role")
                    .setProps("path", "/api/admin/user/role").toJson(),
            new TestRequest()
                    .setProps("action", "ACCESS_DENIED")
                    .setProps("subject", "ivanivanov@acme.com")
                    .setProps("object", "/api/admin/user")
                    .setProps("path", "/api/admin/user").toJson(),
            new TestRequest()
                    .setProps("action", "ACCESS_DENIED")
                    .setProps("subject", "ivanivanov@acme.com")
                    .setProps("object", "/api/admin/user")
                    .setProps("path", "/api/admin/user").toJson(),
            new TestRequest()
                    .setProps("action", "ACCESS_DENIED")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "/api/acct/payments")
                    .setProps("path", "/api/acct/payments").toJson(),
            new TestRequest()
                    .setProps("action", "ACCESS_DENIED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/acct/payments")
                    .setProps("path", "/api/acct/payments").toJson(),
            new TestRequest()
                    .setProps("action", "ACCESS_DENIED")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "ACCESS_DENIED")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "/api/security/events")
                    .setProps("path", "/api/security/events").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "BRUTE_FORCE")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOCK_USER")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "Lock user maxmustermann@acme.com")
                    .setProps("path", "/api/admin/user/access").toJson(),
            new TestRequest()
                    .setProps("action", "UNLOCK_USER")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "Unlock user maxmustermann@acme.com")
                    .setProps("path", "/api/admin/user/access").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "LOGIN_FAILED")
                    .setProps("subject", "maxmustermann@acme.com")
                    .setProps("object", "/api/empl/payment")
                    .setProps("path", "/api/empl/payment").toJson(),
            new TestRequest()
                    .setProps("action", "UNLOCK_USER")
                    .setProps("subject", "johndoe@acme.com")
                    .setProps("object", "Unlock user maxmustermann@acme.com")
                    .setProps("path", "/api/admin/user/access").toJson()
    };

    public LoggingSecurityEventsTest() {
        super(AccountServiceApplication.class, databasePath);
    }

    private String convert(String[] trs) {
        JsonArray jsonArray = new JsonArray();
        for (String tr : trs) {
            JsonElement jsonObject = JsonParser.parseString(tr);
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    private CheckResult testApi() {
        checkResponseStatus(null, "", HttpStatus.UNAUTHORIZED.value(),
                getEmployeePaymentApi, "GET", "This api only for authenticated user");
        return CheckResult.correct();
    }

    /**
     * method for checking response on Post request for signup API
     *
     * @param body   string representation of body content in JSON format (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    private CheckResult testPostSignUpResponse(String body, String[] role) {

        HttpResponse response = checkResponseStatus(null, body, HttpStatus.OK.value(), signUpApi, "POST", "");
        JsonObject rightResponse = getJson(body).getAsJsonObject();
        rightResponse.remove("password");

        // ============================================================= check is it JSON in response or something else
        if (!response.getJson().isJsonObject()) {
            return CheckResult.wrong("Wrong object in response, expected JSON but was \n" +
                                     response.getContent().getClass());
        }

        JsonObject jsonResponse = response.getJson().getAsJsonObject();

        // ===================================================================== check if password presence in response
        if (jsonResponse.get("password") != null) {
            return CheckResult.wrong("You must remove password from response\n" + jsonResponse);
        }

        if (jsonResponse.get("id") == null) {
            return CheckResult.wrong("Response must contain user ID\n" +
                                     "Received response:\n" +
                                     jsonResponse);
        }

        // ===================================================================================== check JSON in response
        expect(response.getContent()).asJson()
                .check(isObject()
                        .value("id", isInteger())
                        .value("name", rightResponse.get("name").getAsString())
                        .value("lastname", rightResponse.get("lastname").getAsString())
                        .value("email", isString(s -> s.equalsIgnoreCase(rightResponse.get("email").getAsString())))
                        .value("roles", role));

        if (userIdList.contains(jsonResponse.get("id").getAsInt())) {
            return CheckResult.wrong("User ID must be unique!\n" +
                                     "Received response:\n" +
                                     jsonResponse);
        }

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
        checkResponseStatus(user, "", status, getEmployeePaymentApi, "GET", message);
        return CheckResult.correct();
    }

    private CheckResult testChangePassword(String body, String user) {

        JsonObject userJson = getJson(user).getAsJsonObject();
        HttpResponse response = checkResponseStatus(user, body, HttpStatus.OK.value(), changePassApi, "POST", "");

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("email", userJson.get("email").getAsString().toLowerCase())
                            .value("status", "The password has been updated successfully"));
        }
        return CheckResult.correct();
    }

    private CheckResult testGetAdminApi(int status, String user,
                                        String answer, String message) {

        HttpResponse response = checkResponseStatus(user, "", status, adminApi, "GET", message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            // ================================================ check is it array of JSON in response or something else
            if (!response.getJson().isJsonArray()) {
                return CheckResult.wrong("Wrong object in response, expected array of JSON but was \n" +
                                         response.getContent().getClass());
            }
            JsonArray correctJson = getJson(answer).getAsJsonArray();
            JsonArray responseJson = getJson(response.getContent()).getAsJsonArray();

            if (responseJson.size() == 0) {
                return CheckResult.wrong("No data in response body" + "\n"
                                         + "in response " + getPrettyJson(responseJson) + "\n"
                                         + "must be " + getPrettyJson(correctJson));
            }

            if (responseJson.size() != correctJson.size()) {
                return CheckResult.wrong("No data in response body" + "\n"
                                         + "in response " + getPrettyJson(responseJson) + "\n"
                                         + "must be " + getPrettyJson(correctJson));
            }

            for (int i = 0; i < responseJson.size(); i++) {
                String[] roles = new String[correctJson.get(i).getAsJsonObject().getAsJsonArray("roles").size()];
                for (int j = 0; j < correctJson.get(i).getAsJsonObject().getAsJsonArray("roles").size(); j++) {
                    roles[j] = correctJson.get(i).getAsJsonObject().getAsJsonArray("roles").get(j).getAsString();
                }
                expect(responseJson.get(i).getAsJsonObject().toString()).asJson()
                        .check(isObject()
                                .value("id", isInteger())
                                .value("name", correctJson.get(i).getAsJsonObject().get("name").getAsString())
                                .value("lastname", correctJson.get(i).getAsJsonObject().get("lastname").getAsString())
                                .value("email", correctJson.get(i).getAsJsonObject().get("email").getAsString())
                                .value("roles", isArray(roles)));
            }
        }
        return CheckResult.correct();
    }

    private CheckResult testPutAdminApi(HttpStatus status, String user,
                                String reqUser, String role,
                                String operation, String[] respRoles, String message) {

        JsonObject jsonUser = getJson(reqUser).getAsJsonObject();
        JsonObject request = new JsonObject();
        request.addProperty("user", jsonUser.get("email").getAsString());
        request.addProperty("operation", operation);
        request.addProperty("role", role);
        HttpResponse response = checkResponseStatus(
                user, request.toString(), status.value(), putRoleApi, "PUT", message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("id", isInteger())
                            .value("name", jsonUser.get("name").getAsString())
                            .value("lastname", jsonUser.get("lastname").getAsString())
                            .value("email", jsonUser.get("email").getAsString().toLowerCase())
                            .value("roles", isArray(respRoles)));
        }

        if (response.getStatusCode() != HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", status.getReasonPhrase())
                            .value("path", putRoleApi)
                            .value("status", status.value())
                            .value("message", respRoles[0])
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testPutAccessApi(HttpStatus status, String user,
                                         String reqUser, String operation,
                                         String answer, String message) {

        JsonObject jsonUser = getJson(reqUser).getAsJsonObject();
        JsonObject request = new JsonObject();
        request.addProperty("user", jsonUser.get("email").getAsString());
        request.addProperty("operation", operation);
        HttpResponse response = checkResponseStatus(
                user, request.toString(), status.value(), putAccessApi, "PUT", message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("status", answer));
        } else {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", status.getReasonPhrase())
                            .value("path", "/api/admin/user/access")
                            .value("status", status.value())
                            .value("message", answer)
                            .anyOtherValues());
        }

        return CheckResult.correct();
    }

    private CheckResult testLocking(String user, String message) {

        HttpResponse response = checkResponseStatus(
                user, "", HttpStatus.UNAUTHORIZED.value(), getEmployeePaymentApi, "GET", message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() != HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", HttpStatus.UNAUTHORIZED.getReasonPhrase())
                            .value("path", getEmployeePaymentApi)
                            .value("status", HttpStatus.UNAUTHORIZED.value())
                            .value("message", "User account is locked")
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testRoleModelNegative(String api, String method,
                                              String user, String message) {

        HttpResponse response = checkResponseStatus(
                user, "", HttpStatus.FORBIDDEN.value(), api, method.toUpperCase(), message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() != HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", HttpStatus.FORBIDDEN.getReasonPhrase())
                            .value("path", api)
                            .value("status", HttpStatus.FORBIDDEN.value())
                            .value("message", "Access Denied!")
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testAuditorApi(String user, String answer,
                                       int position, String message) {

        HttpResponse response = checkResponseStatus(user, "", HttpStatus.OK.value(), auditorApi, "GET", message);

        // ==================================================== check is it array of JSON in response or something else
        if (!response.getJson().isJsonArray()) {
            return CheckResult.wrong("Wrong object in response, expected array of JSON but was \n" +
                                     response.getContent().getClass());
        }

        JsonArray correctJson = getJson(answer).getAsJsonArray();
        JsonArray responseJson = getJson(response.getContent()).getAsJsonArray();
        if (responseJson.size() == 0) {
            throw new WrongAnswer("Empty array in response!");
        }

        if (responseJson.size() != correctJson.size()) {
            throw new WrongAnswer("Incorrect number - " + responseJson.size() +
                                  " events in response, must be - " + correctJson.size() + "\n" +
                                  "response:\n" + getPrettyJson(responseJson) + "\n" +
                                  "must be:\n " + getPrettyJson(correctJson));
        }

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(responseJson.get(position).toString()).asJson()
                    .check(isObject()
                            .value("action", correctJson.get(position).getAsJsonObject().get("action").getAsString())
                            .value("subject", correctJson.get(position).getAsJsonObject().get("subject").getAsString())
                            .value("object", isString(o -> o.contains(
                                    correctJson.get(position).getAsJsonObject()
                                    .get("object").getAsString())))
                            .value("path", isString())
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testDeleteAdminApi(String user) {

        HttpResponse response = checkResponseStatus(
                user, "", HttpStatus.OK.value(), adminApi + "petrpetrov@acme.com", "DELETE", "Trying to delete user");

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("user", "petrpetrov@acme.com".toLowerCase())
                            .value("status", "Deleted successfully!"));
        }

        if (response.getStatusCode() != HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", HttpStatus.OK.getReasonPhrase())
                            .value("path", adminApi + "petrpetrov@acme.com")
                            .value("status", HttpStatus.OK.value())
                            .value("message", "Deleted successfully!")
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    /**
     * method for testing api response
     *
     * @param user    string representation of user information in JSON format (String)
     * @param body    request body (String)
     * @param status  expected response status (int)
     * @param api     testing api (String)
     * @param method  method for api (String)
     * @param message test hints for student (String)
     * @return response (HttpResponse)
     */
    private HttpResponse checkResponseStatus(String user, String body,
                                             int status, String api,
                                             String method, String message) {
        HttpRequest request = switch (method) {
            case "GET" -> get(api);
            case "POST" -> post(api, body);
            case "PUT" -> put(api, body);
            case "DELETE" -> delete(api);
            default -> null;
        };

        if (user != null) {
            JsonObject userJson = getJson(user).getAsJsonObject();
            String password = userJson.get("password").getAsString();
            String login = userJson.get("email").getAsString().toLowerCase();
            if (request != null) {
                request = request.basicAuth(login, password);
                System.out.println("""
                        ------------------------------------------------
                        --------------------------------- request ------
                        ------------------------------------------------""");
                System.out.println(getPrettyJson(getJson(request.getContent())));
            }
        }
        assert request != null;
        HttpResponse response = request.send();

        if (response.getStatusCode() != status) {
            throw new WrongAnswer(method + " " + api + " should respond with status code " + status +
                                  ", responded: " + response.getStatusCode() + "\n"
                                  + message + "\n"
                                  + "Response body:\n" + response.getContent() + "\n");
        }
        System.out.println("""
                ------------------------------------------------
                --------------------------------- response -----
                ------------------------------------------------""");
        System.out.println(response.getContent());
        return response;
    }

    private CheckResult printTestingFields(String testingField) {
        System.out.print(testingField);
        return CheckResult.correct();
    }

    private String getSub(String[] src, int position) {
        return convert(Arrays.copyOfRange(src, 0, position));
    }

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ========================================================== create administrator and auditor
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 1
            () -> testPostSignUpResponse(jDCorrectUser, new String[]{"ROLE_ADMINISTRATOR"}),                    // 2
            () -> testPostSignUpResponse(ivanIvanovCorrectUser, new String[]{"ROLE_USER"}),                     // 3
            () -> testPutAdminApi(HttpStatus.OK, jDCorrectUser, ivanIvanovCorrectUser, "AUDITOR", "GRANT",
                    new String[]{"ROLE_AUDITOR", "ROLE_USER"}, ""),                                             // 4
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ================================================== testing user registration positive tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 5
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 3),
                    0, "'CREATE_USER' security event missing"),                                                 // 6
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 3),
                    1, "'CREATE_USER' security event missing"),                                                 // 7
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 3),
                    2, "'GRANT_ROLE' security event missing"),                                                  // 8
            () -> testPostSignUpResponse(maxMusLower, new String[]{"ROLE_USER"}),                               // 9
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 4),
                    3, "'CREATE_USER' security event missing"),                                                 // 10
            () -> testPostSignUpResponse(petrPetrovCorrectUser, new String[]{"ROLE_USER"}),                     // 11
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 5),
                    4, "'CREATE_USER' security event missing"),                                                 // 12
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ======================================================= test authentication, positive tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 13
            () -> testUserRegistration(maxMusLower, 200, "User must login!"),                                   // 14
            () -> testUserRegistration(maxMusCorrectUser, 200, "Login case insensitive!"),                      // 15
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ======================================================= test authentication, negative tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 16
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 17
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 6),
                    5, "'LOGIN_FAILED' security event missing"),                                                // 18
            () -> testUserRegistration(maxMusWrongEmail, 401, "Wrong password!"),                               // 19
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 7),
                    6, "'LOGIN_FAILED' security event missing"),                                                // 20
            () -> testUserRegistration(captainNemoWrongUser, 401, "Wrong user"),                                // 21
            this::testApi,                                                                                      // 22
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 8),
                    7, "'LOGIN_FAILED' security event missing"),                                                // 23
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ========================================================================== test persistence
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 24
            this::restartApplication,                                                                           // 25
            () -> testUserRegistration(maxMusCorrectUser, 200,
                    "User must login, after restarting! Check persistence."),                                   // 26
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ===================================================== test admin functions - changing roles
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 27
            () -> testPutAdminApi(HttpStatus.BAD_REQUEST, jDCorrectUser, jDCorrectUser, "AUDITOR", "GRANT",
                    new String[]{"The user cannot combine administrative and business roles!"},
                    "Trying add administrative role to business user!"),                                        // 28
            () -> testPutAdminApi(HttpStatus.OK, jDCorrectUser, petrPetrovCorrectUser, "ACCOUNTANT", "GRANT",
                    new String[]{"ROLE_ACCOUNTANT", "ROLE_USER"}, "Trying to add role ACCOUNTANT to user"),     // 29
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 9),
                    8, "'GRANT_ROLE' security event missing"),                                                  // 30
            () -> testPutAdminApi(HttpStatus.OK, jDCorrectUser, petrPetrovCorrectUser, "ACCOUNTANT", "REMOVE",
                    new String[]{"ROLE_USER"}, "Trying to remove role ACCOUNTANT from user"),                   // 31
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 10),
                    9, "'REMOVE_ROLE' security event missing"),                                                 // 32
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ======================================================== test admin functions - delete user
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 33
            () -> testDeleteAdminApi(jDCorrectUser),                                                            // 34
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 11),
                    10, "'DELETE_USER' security event missing"),                                                // 35
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ==================================================================== test changing password
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 36
            () -> testChangePassword(jDPass, jDCorrectUser),                                                    // 37
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 12),
                    11, "'CHANGE_PASSWORD' security event missing"),                                            // 38
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ========================================================== test role model - negative cases
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 39
            () -> testRoleModelNegative(putRoleApi, "PUT", ivanIvanovCorrectUser,
                    "Trying to access administrative endpoint with business user"),                             // 40
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 13),
                    12, "'ACCESS_DENIED' security event missing"),                                              // 41
            () -> testRoleModelNegative(adminApi, "GET", ivanIvanovCorrectUser,
                    "Trying to access administrative endpoint with business user"),                             // 42
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 14),
                    13, "'ACCESS_DENIED' security event missing"),                                              // 43
            () -> testRoleModelNegative(adminApi, "DELETE", ivanIvanovCorrectUser,
                    "Trying to access administrative endpoint with business user"),                             // 44
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 15),
                    14, "'ACCESS_DENIED' security event missing"),                                              // 45
            () -> testRoleModelNegative(postPaymentApi, "POST", jDNewPass,
                    "Trying to access business endpoint with administrative user"),                             // 46
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 16),
                    15, "'ACCESS_DENIED' security event missing"),                                              // 47
            () -> testRoleModelNegative(postPaymentApi, "POST", maxMusCorrectUser,
                    "Trying to access endpoint with wrong role"),                                               // 48
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 17),
                    16, "'ACCESS_DENIED' security event missing"),                                              // 49
            () -> testRoleModelNegative(getEmployeePaymentApi, "GET", jDNewPass,
                    "Trying to access business endpoint with administrative user"),                             // 50
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 18),
                    17, "'ACCESS_DENIED' security event missing"),                                              // 51
            () -> testRoleModelNegative(auditorApi, "GET", jDNewPass,
                    "Trying to access business endpoint with administrative user"),                             // 52
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 19),
                    18, "'ACCESS_DENIED' security event missing"),                                              // 53
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ================================ test admin functions - testing locking & unlocking an user
                    ============== testing locking user after consecutive failed login attempts ("brute force")
                    ===========================================================================================
                    """),                                                                                       // 54
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 55
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 20),
                    19, "'LOGIN_FAILED' security event missing"),                                               // 56
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 57
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 21),
                    20, "'LOGIN_FAILED' security event missing"),                                               // 58
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 59
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 22),
                    21, "'LOGIN_FAILED' security event missing"),                                               // 60
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 61
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 23),
                    22, "'LOGIN_FAILED' security event missing"),                                               // 62
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 63
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 26),
                    23, "'LOGIN_FAILED' security event missing"),                                               // 64
            () -> testLocking(maxMusCorrectUser, "User must be locked after 5 attempts with wrong password"),   // 65
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 26),
                    24, "'BRUTE_FORCE' security event missing"),                                                // 66
            () -> testPutAccessApi(HttpStatus.OK, jDNewPass, maxMusCorrectUser, "UNLOCK",
                    "User maxmustermann@acme.com unlocked!", "User must be unlocked through admin endpoint"),   // 67
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 27),
                    25, "'LOCK_USER' security event missing"),                                                  // 68
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 27),
                    26, "'UNLOCK_USER' security event missing"),                                                // 69
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 70
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 28),
                    27, "'LOGIN_FAILED' security event missing"),                                               // 71
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 72
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 29),
                    28, "'LOGIN_FAILED' security event missing"),                                               // 73
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 74
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 30),
                    29, "'LOGIN_FAILED' security event missing"),                                               // 75
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 76
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 31),
                    30, "'LOGIN_FAILED' security event missing"),                                               // 77
            () -> testUserRegistration(maxMusCorrectUser, 200, "User must login!"),                             // 78
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 79
            () -> testUserRegistration(maxMusCorrectUser, 200,
                    "Counter of failed login attempts must be reset after successfully login!"),                // 80
            () -> testPutAccessApi(HttpStatus.OK, jDNewPass, maxMusCorrectUser, "LOCK",
                    "User maxmustermann@acme.com locked!", ""),                                                 // 81
            () -> testAuditorApi(ivanIvanovCorrectUser, getSub(auditorResponseApi, 33),
                    31, "'LOCK_USER' security event missing"),                                                  // 82
            () -> testLocking(maxMusCorrectUser, "User must be locked through admin endpoint"),                 // 83
            () -> testPutAccessApi(HttpStatus.BAD_REQUEST, jDNewPass,
                    jDCorrectUser, "LOCK", "Can't lock the ADMINISTRATOR!", ""),                                // 84
            () -> testGetAdminApi(200, jDNewPass, firstResponseAdminApi, "Api must be available to admin user"),// 85
            () -> testPostSignUpResponse(petrPetrovCorrectUser, new String[]{"ROLE_USER"}),                     // 86
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 87
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 88
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 89
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 90
            () -> testUserRegistration(petrPetrovCorrectUser, 200,
                    "User must be locked only after 5 attempts with wrong password"),                           // 91
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 92
            () -> testUserRegistration(petrPetrovCorrectUser, 200,
                    "In case of a successful login, reset the counter of the failed attempt."),                 // 93
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 94
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 95
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 96
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 97
            () -> testUserRegistration(petrPetrovWrongPassword, 401, "Wrong password!"),                        // 98
            () -> testLocking(petrPetrovCorrectUser,
                    "User must be locked after 5 attempts with wrong password!"),                               // 99
            () -> testGetAdminApi(401, jDCorrectUser, firstResponseAdminApi, "Wrong password for admin"),       // 100
            () -> testGetAdminApi(401, jDCorrectUser, firstResponseAdminApi, "Wrong password for admin"),       // 101
            () -> testGetAdminApi(401, jDCorrectUser, firstResponseAdminApi, "Wrong password for admin"),       // 102
            () -> testGetAdminApi(401, jDCorrectUser, firstResponseAdminApi, "Wrong password for admin"),       // 103
            () -> testGetAdminApi(401, jDCorrectUser, firstResponseAdminApi, "Wrong password for admin"),       // 104
            () -> testGetAdminApi(200, jDNewPass, secondResponseAdminApi,
                    "Api must be available to admin user")                                                      // 105
    };
}