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

import java.util.*;

import static org.hyperskill.hstest.common.JsonUtils.*;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.*;

public class BusinessLogicTest extends SpringTest {

    private final static String databasePath = "./src/main/resources/service_db.mv.db";
    private final static String signUpApi = "/api/auth/signup";
    private final static String changePassApi = "/api/auth/changepass";
    private final static String getEmployeePaymentApi = "/api/empl/payment";
    private final static String postPaymentApi = "/api/acct/payments";
    private final static String putRoleApi = "/api/admin/user/role";
    private final static String adminApi = "/api/admin/user/";

    private final static String[] breachedPass = new String[]{
            "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
            "PasswordForApril", "PasswordForMay", "PasswordForJune",
            "PasswordForJuly", "PasswordForAugust", "PasswordForSeptember",
            "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"};

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
    private final String maxMusWrongPassword = new TestRequest(maxMus).setProps("password", "none").toJson();
    private final String maxMusWrongEmail = new TestRequest(maxMus).setProps("email", "maxmustermann@google.com").toJson();
    private final String captainNemoWrongUser = captainNemo.toJson();
    private final String jDNewPass = new TestRequest(johnDoe).setProps("password", "aNob5VvqzRtb").toJson();
    private final String jDDuplicatePass = new TestRequest().setProps("new_password", "oMoa3VvqnLxW").toJson();
    private final String jDShortPass = new TestRequest().setProps("new_password", "oMoa3Vvqn").toJson();
    private final String jDPass = new TestRequest().setProps("new_password", "aNob5VvqzRtb").toJson();

    private final String paymentsList = convert(new String[]{
            new TestRequest()
                    .setProps("employee", "ivanivanov@acme.com")
                    .setProps("period", "01-2021")
                    .setProps("salary", 654321).toJson(),
            new TestRequest()
                    .setProps("employee", "ivanivanov@acme.com")
                    .setProps("period", "02-2021")
                    .setProps("salary", 987).toJson(),
            new TestRequest()
                    .setProps("employee", "ivanivanov@acme.com")
                    .setProps("period", "03-2021")
                    .setProps("salary", 21).toJson(),
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "01-2021")
                    .setProps("salary", 123456).toJson(),
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "02-2021")
                    .setProps("salary", 456789).toJson(),
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "03-2021")
                    .setProps("salary", 12).toJson()
    });
    private final String wrongPaymentListData = convert(new String[]{
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "13-2021")
                    .setProps("salary", 123456).toJson()});
    private final String wrongPaymentListSalary = convert(new String[]{
            new TestRequest()
                    .setProps("employee", "johndoe@acme.com")
                    .setProps("period", "11-2022")
                    .setProps("salary", -1).toJson()});
    private final String wrongPaymentListDuplicate = convert(new String[]{
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "01-2021")
                    .setProps("salary", 123456).toJson(),
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "01-2021")
                    .setProps("salary", 456789).toJson()
    });
    private final String updatePayment =
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "01-2021")
                    .setProps("salary", 77777).toJson();
    private final String updatePaymentResponse =
            new TestRequest()
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("period", "January-2021")
                    .setProps("salary", "777 dollar(s) 77 cent(s)").toJson();
    private final String updatePaymentWrongDate =
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "13-2021")
                    .setProps("salary", 1234567).toJson();
    private final String updatePaymentWrongSalary =
            new TestRequest()
                    .setProps("employee", "maxmustermann@acme.com")
                    .setProps("period", "11-2022").setProps("salary", -1).toJson();
    private final String correctPaymentResponse = convert(new String[]{
            new TestRequest()
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("period", "March-2021")
                    .setProps("salary", "0 dollar(s) 12 cent(s)").toJson(),
            new TestRequest()
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("period", "February-2021")
                    .setProps("salary", "4567 dollar(s) 89 cent(s)").toJson(),
            new TestRequest()
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("period", "January-2021")
                    .setProps("salary", "1234 dollar(s) 56 cent(s)").toJson()
    });
    private final String correctPaymentResponseIvanov = convert(new String[]{
            new TestRequest()
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("period", "March-2021")
                    .setProps("salary", "0 dollar(s) 21 cent(s)").toJson(),
            new TestRequest()
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("period", "February-2021")
                    .setProps("salary", "9 dollar(s) 87 cent(s)").toJson(),
            new TestRequest()
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("period", "January-2021")
                    .setProps("salary", "6543 dollar(s) 21 cent(s)").toJson()
    });
    private final String firstResponseAdminApi = convert(new String[]{
            new TestRequest()
                    .setProps("id", 1)
                    .setProps("name", "John")
                    .setProps("lastname", "Doe")
                    .setProps("email", "johndoe@acme.com")
                    .setProps("roles", new String[]{"ROLE_ADMINISTRATOR"}).toJson(),
            new TestRequest()
                    .setProps("id", 2)
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("email", "maxmustermann@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson(),
            new TestRequest()
                    .setProps("id", 3)
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("email", "ivanivanov@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson(),
            new TestRequest()
                    .setProps("id", 4)
                    .setProps("name", "Petr")
                    .setProps("lastname", "Petrov")
                    .setProps("email", "petrpetrov@acme.com")
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
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("email", "maxmustermann@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson(),
            new TestRequest()
                    .setProps("id", 3)
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("email", "ivanivanov@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson()
    });
    private final String thirdResponseAdminApi = convert(new String[]{
            new TestRequest()
                    .setProps("id", 1)
                    .setProps("name", "John")
                    .setProps("lastname", "Doe")
                    .setProps("email", "johndoe@acme.com")
                    .setProps("roles", new String[]{"ROLE_ADMINISTRATOR"}).toJson(),
            new TestRequest()
                    .setProps("id", 2)
                    .setProps("name", "Max")
                    .setProps("lastname", "Mustermann")
                    .setProps("email", "maxmustermann@acme.com")
                    .setProps("roles", new String[]{"ROLE_USER"}).toJson(),
            new TestRequest()
                    .setProps("id", 3)
                    .setProps("name", "Ivan")
                    .setProps("lastname", "Ivanov")
                    .setProps("email", "ivanivanov@acme.com")
                    .setProps("roles", new String[]{"ROLE_ACCOUNTANT", "ROLE_USER"}).toJson()
    });

    public BusinessLogicTest() {
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

    private CheckResult testApi(String user, String body, int status, String api, String method, String message) {
        checkResponseStatus(user, body, status, api, method, message);

        return CheckResult.correct();
    }

    /**
     * method for checking response on Post request for signup API
     *
     * @param body string representation of body content in JSON format (String)
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

        // ======================================================================== check password presence in response
        if (jsonResponse.get("password") != null) {
            return CheckResult.wrong("You must remove password from response\n" + jsonResponse);
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

        // ===================================================================================== check JSON in response
        expect(response.getContent()).asJson()
                .check(isObject()
                        .value("id", isInteger())
                        .value("name", rightResponse.get("name").getAsString())
                        .value("lastname", rightResponse.get("lastname").getAsString())
                        .value("email", rightResponse.get("email").getAsString().toLowerCase())
                        .value("roles", role));
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

    /**
     * method for testing duplicate users
     *
     * @param user string representation of user information in JSON format (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    private CheckResult testUserDuplicates(String user) {

        HttpResponse response = post(signUpApi, user).send();
        // ================================================================= check error message field in JSON response
        expect(response.getContent()).asJson()
                .check(isObject()
                        .value("status", HttpStatus.BAD_REQUEST.value())
                        .value("error", "Bad Request")
                        .value("message", "User exist!")
                        .anyOtherValues());
        return CheckResult.correct();
    }

    private CheckResult testBreachedPass(String api, String login, String password, String body) {
        System.out.println("""
                        ----------------------------------------------------------------------------------------
                        --------------------------------- checking passwords from dummy hackers database ------
                        ----------------------------------------------------------------------------------------""");
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
                return CheckResult.wrong("POST " + api +
                                         " should respond with status code 400 , responded: " +
                                         response.getStatusCode() + "\n"
                                         + "Response body:\n" + response.getContent() + "\n"
                                         + "Request body:\n" + json + "\n"
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

    private CheckResult testPostPaymentResponse(String user, String body, int status, String message) {

        HttpResponse response = checkResponseStatus(user, body, status, postPaymentApi, "POST", message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("status", "Added successfully!")
                            .anyOtherValues());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", "Bad Request")
                            .value("path", postPaymentApi)
                            .value("status", HttpStatus.BAD_REQUEST.value())
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testPutPaymentResponse(String user, String body, int status, String message) {

        HttpResponse response = checkResponseStatus(user, body, status, postPaymentApi, "PUT", message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("status", "Updated successfully!")
                            .anyOtherValues());
        }
        if (response.getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", "Bad Request")
                            .value("path", postPaymentApi)
                            .value("status", HttpStatus.BAD_REQUEST.value())
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testGetPaymentResponse(String user, String correctResponse) {

        HttpResponse response = checkResponseStatus(
                user, "", HttpStatus.OK.value(), getEmployeePaymentApi, "GET", "Wrong status code!");
        JsonArray correctJson = getJson(correctResponse).getAsJsonArray();
        JsonArray responseJson = getJson(response.getContent()).getAsJsonArray();

        if (responseJson.size() == 0) {
            return CheckResult.wrong("No data in response body" + "\n"
                                     + "in response " + getPrettyJson(responseJson) + "\n"
                                     + "must be " + getPrettyJson(correctJson));
        }

        if (correctJson.size() != responseJson.size()) {
            return CheckResult.wrong("New data should not be added" + "\n"
                                     + "in response " + getPrettyJson(responseJson) + "\n"
                                     + "must be " + getPrettyJson(correctJson));
        }

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            for (int i = 0; i < responseJson.size(); i++) {
                if (!responseJson.get(i).equals(correctJson.get(i))) {
                    return CheckResult.wrong("Get " + getEmployeePaymentApi + " wrong data in response body" + "\n"
                                             + "in response " + getPrettyJson(responseJson) + "\n"
                                             + "must be " + getPrettyJson(correctJson));
                }
            }
        }
        return CheckResult.correct();
    }

    private CheckResult testGetPaymentResponseParam(String user, int status, String request,
                                                    String correctResponse, String message) {
        JsonObject userJson = getJson(user).getAsJsonObject();
        String password = userJson.get("password").getAsString();
        String login = userJson.get("email").getAsString().toLowerCase();
        JsonObject json = getJson(correctResponse).getAsJsonObject();
        JsonObject jsonRequest = getJson(request).getAsJsonObject();
        String param = jsonRequest.get("period").getAsString();
        HttpResponse response = get(getEmployeePaymentApi).addParam("period", param).basicAuth(login, password).send();

        if (response.getStatusCode() != status) {
            throw new WrongAnswer("GET" + " " + getEmployeePaymentApi + "?" + param + " should respond with "
                                  + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                  + message + "\n"
                                  + "Response body:\n" + response.getContent() + "\n");
        }
        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            if (!response.getJson().equals(json)) {
                return CheckResult.wrong("Get " + getEmployeePaymentApi + "?period=" + param
                                         + " wrong data in response body" + "\n"
                                         + "in response " + getPrettyJson(response.getJson()) + "\n"
                                         + "must be " + getPrettyJson(json));
            }
        }

        if (response.getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", "Bad Request")
                            .value("path", getEmployeePaymentApi)
                            .value("status", HttpStatus.BAD_REQUEST.value())
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testGetAdminApi(String user, String answer, String message) {

        HttpResponse response = checkResponseStatus(user, "", HttpStatus.OK.value(), adminApi, "GET", message);
        JsonArray correctJson = getJson(answer).getAsJsonArray();
        JsonArray responseJson = getJson(response.getContent()).getAsJsonArray();

        if (responseJson.size() == 0) {
            return CheckResult.wrong("No data in response body" + "\n"
                                     + "in response " + getPrettyJson(responseJson) + "\n"
                                     + "must be " + getPrettyJson(correctJson));
        }

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
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

    private CheckResult testDeleteAdminApi(HttpStatus status, String user, String param, String answer, String message) {

        HttpResponse response = checkResponseStatus(user, "", status.value(),
                adminApi + param, "DELETE", message);

        // ===================================================================================== check JSON in response
        if (response.getStatusCode() == HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("user", param.toLowerCase())
                            .value("status", answer));
        }

        if (response.getStatusCode() != HttpStatus.OK.value()) {
            expect(response.getContent()).asJson()
                    .check(isObject()
                            .value("error", status.getReasonPhrase())
                            .value("path", adminApi + param)
                            .value("status", status.value())
                            .value("message", answer)
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    private CheckResult testPutAdminApi(HttpStatus status, String user, String reqUser,
                                        String role, String operation, String[] respRoles, String message) {

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

    private CheckResult testRoleModelNegative(String api, String method, String user, String message) {

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

        System.out.println("endpoint ----->>> " + api);

        if (user != null) {
            JsonObject userJson = getJson(user).getAsJsonObject();

            if (!api.equals(signUpApi)) {
                System.out.println("""
                        ------------------------------------------------
                        ------------------------------------- user -----
                        ------------------------------------------------""");
                System.out.println(getPrettyJson(userJson));
            }

            String password = userJson.get("password").getAsString();
            String login = userJson.get("email").getAsString().toLowerCase();
            if (request != null) {

                            System.out.println("""
                        ------------------------------------------------
                        ----------------------------- request body -----
                        ------------------------------------------------""");
            System.out.println(getPrettyJson(getJson(request.getContent())));

                request = request.basicAuth(login, password);
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

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ================================================== testing user registration negative tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 1
            () -> testApi(null, jDEmptyName, 400, signUpApi, "POST", "Empty name field!"),                      // 2
            () -> testApi(null, jDNoName, 400, signUpApi, "POST", "Name field is absent!"),                     // 3
            () -> testApi(null, jDEmptyLastName, 400, signUpApi, "POST", "Empty lastname field!"),              // 4
            () -> testApi(null, jDNoLastName, 400, signUpApi, "POST", "Lastname field is absent!"),             // 5
            () -> testApi(null, jDEmptyEmail, 400, signUpApi, "POST", "Empty email field!"),                    // 6
            () -> testApi(null, jDNoEmail, 400, signUpApi, "POST", "Email field is absent!"),                   // 7
            () -> testApi(null, jDEmptyPassword, 400, signUpApi, "POST", "Empty password field!"),              // 8
            () -> testApi(null, jDNoPassword, 400, signUpApi, "POST", "Password field is absent!"),             // 9
            () -> testApi(null, jDWrongEmail1, 400, signUpApi, "POST", "Wrong email!"),                         // 10
            () -> testApi(null, jDWrongEmail2, 400, signUpApi, "POST", "Wrong email!"),                         // 11
            () -> testBreachedPass(signUpApi, "", "", jDCorrectUser),                                           // 12
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ================================================== testing user registration positive tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 13
            () -> testPostSignUpResponse(jDCorrectUser, new String[]{"ROLE_ADMINISTRATOR"}),                    // 14
            () -> testPostSignUpResponse(maxMusLower, new String[]{"ROLE_USER"}),                               // 15
            () -> testPostSignUpResponse(ivanIvanovCorrectUser, new String[]{"ROLE_USER"}),                     // 16
            () -> testPostSignUpResponse(petrPetrovCorrectUser, new String[]{"ROLE_USER"}),                     // 17
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ================================================== testing user registration negative tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 18
            () -> testApi(null, jDCorrectUser, 400, signUpApi, "POST", "User must be unique!"),                 // 19
            () -> testUserDuplicates(jDCorrectUser),                                                            // 20
            () -> testApi(null, jDLower, 400, signUpApi, "POST", "User must be unique (ignorecase)!"),          // 21
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ======================================================= test authentication, positive tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 22
            () -> testUserRegistration(maxMusLower, 200, "User must login!"),                                   // 23
            () -> testUserRegistration(maxMusCorrectUser, 200, "Login case insensitive!"),                      // 24
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ======================================================= test authentication, negative tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 25
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),                            // 26
            () -> testUserRegistration(maxMusWrongEmail, 401, "Wrong password!"),                               // 27
            () -> testUserRegistration(captainNemoWrongUser, 401, "Wrong user"),                                // 28
            () -> testApi(null, "", 401, getEmployeePaymentApi, "GET", "This api only for authenticated user"), // 29
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ==================================================================== test changing password
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 30
            () -> testApi(null, jDDuplicatePass, 401, changePassApi, "POST",
                    "This api only for authenticated user"),                                                    // 31
            () -> testApi(jDCorrectUser, jDShortPass, 400, changePassApi, "POST",
                    "The password length must be at least 12 chars!"),                                          // 32
            () -> testApi(jDCorrectUser, jDDuplicatePass, 400, changePassApi, "POST",
                    "The passwords must be different!"),                                                        // 33
            () -> testBreachedPass(changePassApi, "JohnDoe@acme.com", "oMoa3VvqnLxW", jDDuplicatePass),         // 34
            () -> testChangePassword(jDPass, jDCorrectUser),                                                    // 35
            () -> testApi(jDCorrectUser, "", 401, adminApi, "GET", "Password must be changed!"),                // 36
            () -> testApi(jDNewPass, "", 200, adminApi, "GET", "Password must be changed!"),                    // 37
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ========================================================================== test persistence
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 38
            this::restartApplication,                                                                           // 39
            () -> testUserRegistration(maxMusCorrectUser, 200,
                    "User must login, after restarting! Check persistence."),                                   // 40
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ======================================================== test admin functions - delete user
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 41
            () -> testGetAdminApi(jDNewPass, firstResponseAdminApi, "Api must be available to admin user"),     // 42
            () -> testDeleteAdminApi(HttpStatus.OK, jDNewPass, "petrpetrov@acme.com",
                    "Deleted successfully!", "Trying to delete user"),                                          // 43
            () -> testGetAdminApi(jDNewPass, secondResponseAdminApi, "User must be deleted!"),                  // 44
            () -> testDeleteAdminApi(HttpStatus.BAD_REQUEST, jDNewPass,
                    "johndoe@acme.com", "Can't remove ADMINISTRATOR role!", "Trying to delete admin"),          // 45
            () -> testDeleteAdminApi(HttpStatus.NOT_FOUND, jDNewPass,
                    "johndoe@goole.com", "User not found!", "Trying to delete non existing user"),              // 46
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ===================================================== test admin functions - changing roles
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 47
            () -> testPutAdminApi(HttpStatus.OK, jDNewPass, ivanIvanovCorrectUser,
                    "ACCOUNTANT", "GRANT", new String[]{"ROLE_ACCOUNTANT", "ROLE_USER"}, ""),                   // 48
            () -> testGetAdminApi(jDNewPass, thirdResponseAdminApi, "Role must be changed!"),                   // 49
            () -> testPutAdminApi(HttpStatus.OK, jDNewPass, ivanIvanovCorrectUser,
                    "ACCOUNTANT", "REMOVE", new String[]{"ROLE_USER"}, ""),                                     // 50
            () -> testGetAdminApi(jDNewPass, secondResponseAdminApi, "Role must be changed!"),                  // 51
            () -> testPutAdminApi(HttpStatus.OK, jDNewPass, ivanIvanovCorrectUser,
                    "ACCOUNTANT", "GRANT", new String[]{"ROLE_ACCOUNTANT", "ROLE_USER"}, ""),                   // 52
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ===================================================== test admin functions - negative tests
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 53
            () -> testPutAdminApi(HttpStatus.NOT_FOUND, jDNewPass, ivanIvanovCorrectUser,
                    "NEW_ROLE", "GRANT", new String[]{"Role not found!"}, "Trying add not existing role!"),     // 54
            () -> testPutAdminApi(HttpStatus.BAD_REQUEST, jDNewPass,
                    ivanIvanovCorrectUser, "ADMINISTRATOR", "GRANT",
                    new String[]{"The user cannot combine administrative and business roles!"},
                    "Trying add administrative role to business user!"),                                        // 55
            () -> testPutAdminApi(HttpStatus.BAD_REQUEST, jDNewPass, jDNewPass,
                    "USER", "GRANT", new String[]{"The user cannot combine administrative and business roles!"},
                    "Trying add business role to administrator!"),                                              // 56
            () -> testPutAdminApi(HttpStatus.BAD_REQUEST, jDNewPass, jDNewPass,
                    "ADMINISTRATOR", "REMOVE", new String[]{"Can't remove ADMINISTRATOR role!"},
                    "Trying remove administrator role!"),                                                       // 57
            () -> testPutAdminApi(HttpStatus.BAD_REQUEST, jDNewPass, maxMusCorrectUser,
                    "USER", "REMOVE", new String[]{"The user must have at least one role!"},
                    "Trying remove single role!"),                                                              // 58
            () -> testPutAdminApi(HttpStatus.BAD_REQUEST, jDNewPass, maxMusCorrectUser,
                    "ACCOUNTANT", "REMOVE", new String[]{"The user does not have a role!"},
                    "Trying remove not granted role!"),                                                         // 59
            () -> testPutAdminApi(HttpStatus.NOT_FOUND, jDNewPass, captainNemoWrongUser,
                    "ACCOUNTANT", "REMOVE", new String[]{"User not found!"},
                    "Trying remove role from non existing user!"),                                              // 60
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ========================================================== test role model - negative cases
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 61
            () -> testRoleModelNegative(putRoleApi, "PUT", ivanIvanovCorrectUser,
                    "Trying to access administrative endpoint with business user"),                             // 62
            () -> testRoleModelNegative("/api/admin/user/", "GET", ivanIvanovCorrectUser,
                    "Trying to access administrative endpoint with business user"),                             // 63
            () -> testRoleModelNegative("/api/admin/user", "DELETE", ivanIvanovCorrectUser,
                    "Trying to access administrative endpoint with business user"),                             // 64
            () -> testRoleModelNegative(postPaymentApi, "POST", jDNewPass,
                    "Trying to access business endpoint with administrative user"),                             // 65
            () -> testRoleModelNegative(postPaymentApi, "POST", maxMusCorrectUser,
                    "Trying to access endpoint with wrong role"),                                               // 66
            () -> testRoleModelNegative(getEmployeePaymentApi, "GET", jDNewPass,
                    "Trying to access business endpoint with administrative user"),                             // 67
            () -> printTestingFields("""
                    ===========================================================================================
                    ===========================================================================================
                    ==================================================================== testing business logic
                    ===========================================================================================
                    ===========================================================================================
                    """),                                                                                       // 68
            () -> testPostPaymentResponse(ivanIvanovCorrectUser, paymentsList, 200,
                    "Payment list must be added"),                                                              // 69
            () -> testGetPaymentResponse(maxMusCorrectUser, correctPaymentResponse),                            // 70
            () -> testGetPaymentResponse(ivanIvanovCorrectUser, correctPaymentResponseIvanov),                  // 71
            () -> testPostPaymentResponse(ivanIvanovCorrectUser, wrongPaymentListSalary, 400,
                    "Wrong salary in payment list"),                                                            // 72
            () -> testGetPaymentResponse(maxMusCorrectUser, correctPaymentResponse),                            // 73
            () -> testPostPaymentResponse(ivanIvanovCorrectUser, wrongPaymentListData, 400,
                    "Wrong data in payment list"),                                                              // 74
            () -> testGetPaymentResponse(maxMusCorrectUser, correctPaymentResponse),                            // 75
            () -> testPostPaymentResponse(ivanIvanovCorrectUser, wrongPaymentListDuplicate, 400,
                    "Duplicated entry in payment list"),                                                        // 76
            () -> testGetPaymentResponse(maxMusCorrectUser, correctPaymentResponse),                            // 77
            () -> testPutPaymentResponse(ivanIvanovCorrectUser, updatePaymentWrongDate, 400,
                    "Wrong date in request body!"),                                                             // 78
            () -> testPutPaymentResponse(ivanIvanovCorrectUser, updatePaymentWrongSalary, 400,
                    "Wrong salary in request body!"),                                                           // 79
            () -> testPutPaymentResponse(ivanIvanovCorrectUser, updatePayment, 200,
                    "Salary must be update!"),                                                                  // 80
            () -> testGetPaymentResponseParam(maxMusCorrectUser, 200, updatePayment, updatePaymentResponse,
                    "Salary must be update!"),                                                                  // 81
            () -> testGetPaymentResponseParam(maxMusCorrectUser, 400, updatePaymentWrongDate, updatePaymentResponse,
                    "Wrong date in request!"),                                                                  // 82
    };
}