package java.com.codeofcarbon.account;

import com.codeofcarbon.account.AccountServiceApplication;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.UnexpectedError;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.util.ArrayList;
import java.util.List;

import static org.hyperskill.hstest.common.JsonUtils.getJson;
import static org.hyperskill.hstest.common.JsonUtils.getPrettyJson;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isInteger;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isObject;

public class Test4 extends SpringTest {

    private final String signUpApi = "/api/auth/signup";
    private final String changePassApi = "/api/auth/changepass";
    private final String getEmployeePaymentApi = "/api/empl/payment";
    private final String postPaymentApi = "api/acct/payments";


    static String[] breachedPass = new String[]{"PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch",
            "PasswordForApril", "PasswordForMay", "PasswordForJune",
            "PasswordForJuly", "PasswordForAugust", "PasswordForSeptember",
            "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"};

    List<Integer> userIdList = new ArrayList<>();

    private final TestRequest johnDoe = new TestRequest().setProps("name", "John")
            .setProps("lastname", "Doe")
            .setProps("email", "JohnDoe@acme.com")
            .setProps("password", "oMoa3VvqnLxW");
    private final TestRequest maxMus = new TestRequest().setProps("name", "Max")
            .setProps("lastname", "Mustermann")
            .setProps("email", "MaxMustermann@acme.com")
            .setProps("password", "ai0y9bMvyF6G");
    private final TestRequest captainNemo = new TestRequest().setProps("name", "Captain")
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
    private final String jDNewPass = new TestRequest(johnDoe).setProps("password", "aNob5VvqzRtb").toJson();

    private final String jDDuplicatePass = new TestRequest().setProps("new_password", "oMoa3VvqnLxW").toJson();
    private final String jDShortPass = new TestRequest().setProps("new_password", "oMoa3Vvqn").toJson();
    private final String jDPass = new TestRequest().setProps("new_password", "aNob5VvqzRtb").toJson();

    private String paymentsList = convert(new String[]{
            new TestRequest().setProps("employee", "johndoe@acme.com").setProps("period", "01-2021")
                    .setProps("salary", 123456).toJson(),
            new TestRequest().setProps("employee", "johndoe@acme.com").setProps("period", "02-2021")
                    .setProps("salary", 456789).toJson(),
            new TestRequest().setProps("employee", "johndoe@acme.com").setProps("period", "03-2021")
                    .setProps("salary", 12).toJson(),
            new TestRequest().setProps("employee", "maxmustermann@acme.com").setProps("period", "01-2021")
                    .setProps("salary", 54321).toJson(),
            new TestRequest().setProps("employee", "maxmustermann@acme.com").setProps("period", "02-2021")
                    .setProps("salary", 987654).toJson(),
            new TestRequest().setProps("employee", "maxmustermann@acme.com").setProps("period", "03-2021")
                    .setProps("salary", 120).toJson()
    });
    private String wrongPaymentListData = convert(new String[]{new TestRequest().setProps("employee", "johndoe@acme.com")
            .setProps("period", "13-2021").setProps("salary", 123456).toJson()});


    private String wrongPaymentListSalary = new TestRequest().setProps("employee", "johndoe@acme.com")
            .setProps("period", "13-2021").setProps("salary", -1).toJson();

    private String wrongPaymentListDuplicate = convert(new String[]{
            new TestRequest().setProps("employee", "johndoe@acme.com").setProps("period", "01-2021")
                    .setProps("salary", 123456).toJson(),
            new TestRequest().setProps("employee", "johndoe@acme.com").setProps("period", "01-2021")
                    .setProps("salary", 456789).toJson()
    });
    private String updatePayment = new TestRequest().setProps("employee", "johndoe@acme.com").setProps("period", "01-2021")
            .setProps("salary", 77777).toJson();
    private String updatePaymentResponse = new TestRequest().setProps("name", "John").setProps("lastname", "Doe")
            .setProps("period", "January-2021").setProps("salary", "777 dollar(s) 77 cent(s)").toJson();
    private String updatePaymentWrongDate = new TestRequest().setProps("employee", "johndoe@acme.com")
            .setProps("period", "13-2021").setProps("salary", 1234567).toJson();
    private String updatePaymentWrongSalary = new TestRequest().setProps("employee", "johndoe@acme.com")
            .setProps("period", "13-2021").setProps("salary", -1).toJson();
    private String correctPaymentResponse = convert(new String[]{
            new TestRequest().setProps("name", "John").setProps("lastname", "Doe")
                    .setProps("period", "March-2021").setProps("salary", "0 dollar(s) 12 cent(s)").toJson(),
            new TestRequest().setProps("name", "John").setProps("lastname", "Doe")
                    .setProps("period", "February-2021").setProps("salary", "4567 dollar(s) 89 cent(s)").toJson(),
            new TestRequest().setProps("name", "John").setProps("lastname", "Doe")
                    .setProps("period", "January-2021").setProps("salary", "1234 dollar(s) 56 cent(s)").toJson()
    });

    public Test4() {
        super(AccountServiceApplication.class, "../src/main/resources/service_db.mv.db");
    }

    private String convert(String[] trs) {
        JsonArray jsonArray = new JsonArray();
        for (String tr : trs) {
            JsonElement jsonObject = JsonParser.parseString(tr);
            jsonArray.add(jsonObject);
        }
        return jsonArray.toString();
    }

    /**
     * Method for checking status code of response Post request for API
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

    CheckResult testPostApiWithAuth(String api, String body, int status, String login, String pass, String message) {
        HttpResponse response = post(api, body).basicAuth(login, pass).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + api + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }
        return CheckResult.correct();
    }

    /**
     * Method for checking status code of response Get request for API
     *
     * @param api     testing api (String)
     * @param status  required http status for response (int)
     * @param message hint about reason of error (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testGetApi(String api, int status, String message) {
        HttpResponse response = get(api).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("GET " + api + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message);
        }
        return CheckResult.correct();
    }

    /**
     * Method for checking status code of response Get request for API
     *
     * @param api      testing api (String)
     * @param status   required http status for response (int)
     * @param message  hint about reason of error (String)
     * @param login    login
     * @param password password
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testGetApiAuth(String api, int status, String login, String password, String message) {
        HttpResponse response = get(api).basicAuth(login, password).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("GET " + api + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message);
        }
        return CheckResult.correct();
    }

    /**
     * Method for checking response on Post request for signup API
     *
     * @param body   string representation of body content in JSON format (String)
     * @param status required http status for response (int)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testPostSignUpResponse(String body, int status) {
        HttpResponse response = post(signUpApi, body).send();
        testPostApi(signUpApi, body, status, "API must be available");

        JsonObject rightResponse = getJson(body).getAsJsonObject();
        rightResponse.remove("password");

        // Check is it JSON in response or something else
        if (!response.getJson().isJsonObject()) {
            return CheckResult.wrong("Wrong object in response, expected JSON but was \n" +
                                     response.getContent().getClass());

        }

        JsonObject jsonResponse = response.getJson().getAsJsonObject();

        // Check if password is presence in response
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
        // Check JSON in response
        expect(response.getContent()).asJson().check(
                isObject()
                        .value("id", isInteger())
                        .value("name", rightResponse.get("name").getAsString())
                        .value("lastname", rightResponse.get("lastname").getAsString())
                        .value("email", rightResponse.get("email").getAsString().toLowerCase()));
        userIdList.add(jsonResponse.get("id").getAsInt());
        return CheckResult.correct();
    }


    /**
     * Method for restarting application
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
     * Method for checking authentication
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
        HttpResponse response = get(getEmployeePaymentApi).basicAuth(login, password).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("Get " + getEmployeePaymentApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Authentication with " + login + " / " + password);
        }
        return CheckResult.correct();
    }

    CheckResult testChangePassword(String api, String body, int status, String user) {
        JsonObject userJson = getJson(user).getAsJsonObject();
        String pass = userJson.get("password").getAsString();
        String login = userJson.get("email").getAsString().toLowerCase();
        HttpResponse response = post(api, body).basicAuth(login, pass).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + api + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }
        // Check JSON in response
        if (status == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("email", userJson.get("email").getAsString().toLowerCase())
                            .value("status", "The password has been updated successfully"));
        }
        return CheckResult.correct();
    }

    /**
     * Method for testing duplicate users
     *
     * @param user string representation of user information in JSON format (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    private CheckResult testUserDuplicates(String user) {
        HttpResponse response = post(signUpApi, user).send();
        // Check error message field in JSON response
        expect(response.getContent()).asJson().check(
                isObject()
                        .value("status", 400)
                        .value("error", "Bad Request")
                        .value("message", "User exist!")
                        .anyOtherValues());
        return CheckResult.correct();
    }

    private CheckResult testBreachedPass(String api, String login, String password, String body, String message) {
        JsonObject json = getJson(body).getAsJsonObject();
        HttpResponse response;
        for (int index = 0; index < breachedPass.length; index++) {
            if (json.has("password")) {
                json.remove("password");
                json.addProperty("password", breachedPass[index]);
            } else if (json.has("new_password")) {
                json.remove("new_password");
                json.addProperty("new_password", breachedPass[index]);
            }
            if (login.isEmpty() || password.isEmpty()) {
                response = post(api, json.toString()).send();
            } else {
                response = post(api, json.toString()).basicAuth(login, password).send();
            }

            if (response.getStatusCode() != 400) {
                return CheckResult.wrong("POST " + api + " should respond with "
                                         + "status code 400 , responded: " + response.getStatusCode() + "\n"
                                         + "Response body:\n" + response.getContent() + "\n"
                                         + "Request body:\n" + json.toString() + "\n"
                                         + message);
            }
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("status", 400)
                            .value("error", "Bad Request")
                            .value("message", "The password is in the hacker's database!")
                            .anyOtherValues());

        }
        return CheckResult.correct();
    }


    CheckResult testPostPaymentResponse(String body, int status, String message) {
        HttpResponse response = post(postPaymentApi, body).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + postPaymentApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }

        // Check JSON in response
        if (response.getStatusCode() == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("status", "Added successfully!")
                            .anyOtherValues());
        }
        if (response.getStatusCode() == 400) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("error", "Bad Request")
                            .value("path", "/api/acct/payments")
                            .value("status", 400)
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    CheckResult testPutPaymentResponse(String body, int status, String message) {
        HttpResponse response = put(postPaymentApi, body).send();
        if (response.getStatusCode() != status) {
            return CheckResult.wrong("PUT " + postPaymentApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Response body:\n" + response.getContent() + "\n"
                                     + "Request body:\n" + body);
        }

        // Check JSON in response
        if (response.getStatusCode() == 200) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("status", "Updated successfully!")
                            .anyOtherValues());
        }
        if (response.getStatusCode() == 400) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("error", "Bad Request")
                            .value("path", "/api/acct/payments")
                            .value("status", 400)
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    CheckResult testGetPaymentResponse(String user, int status, String correctResponse, String message) {
        JsonObject userJson = getJson(user).getAsJsonObject();
        String password = userJson.get("password").getAsString();
        String login = userJson.get("email").getAsString().toLowerCase();
        HttpResponse response = get(getEmployeePaymentApi).basicAuth(login, password).send();
        JsonArray correctJson = getJson(correctResponse).getAsJsonArray();
        JsonArray responseJson = getJson(response.getContent()).getAsJsonArray();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + getEmployeePaymentApi + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Response body:\n" + response.getContent() + "\n");
        }

        if (responseJson.size() == 0) {
            return CheckResult.wrong("Payments was not added " + "\n"
                                     + "endpoint " + getEmployeePaymentApi + "\n"
                                     + "responded with " + getPrettyJson(responseJson) + "\n"
                                     + "must be " + getPrettyJson(correctJson));
        }

        if (correctJson.size() != responseJson.size()) {
            return CheckResult.wrong("New data should not be added" + "\n"
                                     + "in response " + getPrettyJson(responseJson) + "\n"
                                     + "must be " + getPrettyJson(correctJson));
        }

        // Check JSON in response
        if (response.getStatusCode() == 200) {
            for (int i = 0; i < responseJson.size(); i++) {
                if (!responseJson.get(i).equals(correctJson.get(i))) {
                    return CheckResult.wrong("Get " + getEmployeePaymentApi + " wrong data in response body" + "\n"
                                             + "in response " + getPrettyJson(responseJson) + "\n"
                                             + "must be " + getPrettyJson(correctJson));
                }
            }
        }
//    if (response.getStatusCode() == 400) {
//      expect(response.getContent()).asJson().check(
//              isObject()
//                      .value("error", "Bad Request")
//                      .value("path", "/api/acct/payments")
//                      .value("status", 400)
//                      .anyOtherValues());
//    }
        return CheckResult.correct();
    }

    CheckResult testGetPaymentResponseParam(String user, int status, String request, String correctResponse, String message) {
        JsonObject userJson = getJson(user).getAsJsonObject();
        String password = userJson.get("password").getAsString();
        String login = userJson.get("email").getAsString().toLowerCase();
        JsonObject json = getJson(correctResponse).getAsJsonObject();
        JsonObject jsonRequest = getJson(request).getAsJsonObject();
        String param = jsonRequest.get("period").getAsString();
        HttpResponse response = get(getEmployeePaymentApi).addParam("period", param).basicAuth(login, password).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("GET " + getEmployeePaymentApi + "?period=" + param + " should respond with "
                                     + "status code " + status + ", responded: " + response.getStatusCode() + "\n"
                                     + message + "\n"
                                     + "Response body:\n" + response.getContent() + "\n");
        }

        // Check JSON in response
        if (response.getStatusCode() == 200) {
            if (!response.getJson().equals(json)) {
                return CheckResult.wrong("Get " + getEmployeePaymentApi + "?period=" + param
                                         + " wrong data in response body" + "\n"
                                         + "in response " + getPrettyJson(response.getJson()) + "\n"
                                         + "must be " + getPrettyJson(json));
            }
        }

        if (response.getStatusCode() == 400) {
            expect(response.getContent()).asJson().check(
                    isObject()
                            .value("error", "Bad Request")
                            .value("path", "/api/empl/payment")
                            .value("status", 400)
                            .anyOtherValues());
        }
        return CheckResult.correct();
    }

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // Test wrong POST request for signup api
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
            // Test user registration on signup api
            () -> testBreachedPass(signUpApi, "", "",
                    jDCorrectUser, "Sending password from breached list"),
            () -> testPostSignUpResponse(jDCorrectUser, 200),
            () -> testPostApi(signUpApi, jDCorrectUser, 400, "User must be unique!"),
            () -> testUserDuplicates(jDCorrectUser),
            () -> testPostApi(signUpApi, jDLower, 400, "User must be unique (ignorecase)!"),
            () -> testPostSignUpResponse(maxMusLower, 200),
            () -> testPostApi(signUpApi, maxMusLower, 400, "User must be unique!"),
            () -> testPostApi(signUpApi, maxMusCorrectUser, 400, "User must be unique (ignorecase)!"),
            // Test authentication, positive tests
            () -> testUserRegistration(jDLower, 200, "User must login!"),
            () -> testUserRegistration(jDCorrectUser, 200, "Login case insensitive!"),
            () -> testUserRegistration(maxMusLower, 200, "User must login!"),
            () -> testUserRegistration(maxMusCorrectUser, 200, "Login case insensitive!"),
            // Test authentication, negative tests
            () -> testUserRegistration(jDWrongPassword, 401, "Wrong password!"),
            () -> testUserRegistration(jDWrongEmail1, 401, "Wrong user!"),
            () -> testUserRegistration(maxMusWrongPassword, 401, "Wrong password!"),
            () -> testUserRegistration(captainNemoWrongUser, 401, "Wrong user"),
            () -> testGetApi(getEmployeePaymentApi, 401, "This api only for authenticated user"),

            // Test changing password
            () -> testPostApi(changePassApi, jDDuplicatePass, 401, "This api only for authenticated user"),
            () -> testPostApiWithAuth(changePassApi, jDShortPass, 400,
                    "JohnDoe@acme.com", "oMoa3VvqnLxW", "The password length must be at least 12 chars!"),
            () -> testPostApiWithAuth(changePassApi, jDDuplicatePass, 400,
                    "JohnDoe@acme.com", "oMoa3VvqnLxW", "The passwords must be different!"),
            () -> testBreachedPass(changePassApi, "JohnDoe@acme.com", "oMoa3VvqnLxW",
                    jDDuplicatePass, "Sending password from breached list"),
            () -> testChangePassword(changePassApi, jDPass, 200, jDCorrectUser),
            () -> testGetApiAuth(getEmployeePaymentApi, 401, "JohnDoe@acme.com", "oMoa3VvqnLxW", "Password must be changed!"),
            () -> testGetApiAuth(getEmployeePaymentApi, 200, "JohnDoe@acme.com", "aNob5VvqzRtb", "Password must be changed!"),

            // Test persistence
            () -> restartApplication(),
            () -> testUserRegistration(maxMusCorrectUser, 200, "User must login, after restarting!" +
                                                               " Check persistence."),
            // Test business logic
            () -> testPostPaymentResponse(paymentsList, 200, "Payment list must be added"),
            () -> testGetPaymentResponse(jDNewPass, 200, correctPaymentResponse,
                    "Wrong status code!"),
            () -> testPostPaymentResponse(wrongPaymentListSalary, 400, "Wrong salary in payment list"),
            () -> testGetPaymentResponse(jDNewPass, 200, correctPaymentResponse,
                    "Wrong status code!"),
            () -> testPostPaymentResponse(wrongPaymentListData, 400, "Wrong data in payment list"),
            () -> testGetPaymentResponse(jDNewPass, 200, correctPaymentResponse,
                    "Wrong status code!"),
            () -> testPostPaymentResponse(wrongPaymentListDuplicate, 400, "Duplicated entry in payment list"),
            () -> testGetPaymentResponse(jDNewPass, 200, correctPaymentResponse,
                    "Wrong status code!"),
            () -> testPutPaymentResponse(updatePaymentWrongDate, 400, "Wrong date in request body!"),
            () -> testPutPaymentResponse(updatePaymentWrongSalary, 400, "Wrong salary in request body!"),
            () -> testPutPaymentResponse(updatePayment, 200, "Salary must be update!"),
            () -> testGetPaymentResponseParam(jDNewPass, 200, updatePayment, updatePaymentResponse,
                    "Salary must be update!"),
            () -> testGetPaymentResponseParam(jDNewPass, 400, updatePaymentWrongDate, updatePaymentResponse,
                    "Wrong date in request!"),
    };
}