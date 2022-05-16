package java.com.codeofcarbon.account;

import com.codeofcarbon.account.AccountServiceApplication;
import com.google.gson.JsonObject;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.mocks.web.request.HttpRequest;
import org.hyperskill.hstest.mocks.web.response.HttpResponse;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.hyperskill.hstest.common.JsonUtils.*;
import static org.hyperskill.hstest.testing.expect.Expectation.expect;
import static org.hyperskill.hstest.testing.expect.json.JsonChecker.isObject;

public class Test1 extends SpringTest {

    private final static String signUpApi = "/api/auth/signup/";
    List<String> deniedSignUpMethods = new ArrayList<>() {{
        add("get");
        add("put");
        add("delete");
    }};

    private final MockUser johnDoe = new MockUser("John", "Doe", "johndoe@acme.com", "secret");
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

    public Test1() {
        super(AccountServiceApplication.class, 28852);
    }               // todo -> what about that port?

    /**
     * method for checking status code of response Post requests for API
     *
     * @param body   string representation of body content in JSON format (String)
     * @param status required http status for response (int)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testPostApi(String body, int status) {
        HttpResponse response = post(signUpApi, body).send();

        if (response.getStatusCode() != status) {
            return CheckResult.wrong("POST " + signUpApi + " should respond with " +
                                     "status code " + status + ", responded: " + response.getStatusCode() + "\n\n" +
                                     "Response body:\n" + response.getContent() + "\n" +
                                     "Request body:\n" + body);
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
        testPostApi(body, HttpStatus.OK.value());

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
                                     getPrettyJson(jsonResponse));
        }

        // check JSON in response
        expect(response.getContent()).asJson()
                .check(isObject()
                        .value("name", rightResponse.get("name").getAsString())
                        .value("lastname", rightResponse.get("lastname").getAsString())
                        .value("email", rightResponse.get("email").getAsString().toLowerCase()));

        return CheckResult.correct();
    }

    /**
     * method for check the prohibition of requests specified types
     *
     * @param deniedMethods list of prohibited type requests
     * @param body          string representation of body content in JSON format (String)
     * @return instance of CheckResult class containing result of checks (CheckResult)
     */
    CheckResult testDeniedMethods(List<String> deniedMethods, String body) {
        HttpRequest getReq = get(signUpApi);
        HttpRequest postReq = post(signUpApi, body);
        HttpRequest putReq = put(signUpApi, body);
        HttpRequest deleteReq = delete(signUpApi);

        Map<String, HttpRequest> methodsMap = new LinkedHashMap<>() {{
            put("get", getReq);
            put("post", postReq);
            put("put", putReq);
            put("delete", deleteReq);
        }};

        for (Map.Entry<String, HttpRequest> entry : methodsMap.entrySet()) {
            if (deniedMethods.contains(entry.getKey())) {
                HttpResponse response = entry.getValue().send();
                if (response.getStatusCode() != HttpStatus.METHOD_NOT_ALLOWED.value()) {
                    return CheckResult.wrong("Method " + entry.getKey().toUpperCase() +
                                             " is not allowed for " + signUpApi + " status code should be " +
                                             "405, responded: " + response.getStatusCode());
                }
            }
        }
        return CheckResult.correct();
    }

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // ======================================================================= test POST request for signup api
            () -> testPostApi(johnDoeCorrectUser, 200),
            () -> testPostApi(johnDoeEmptyName, 400),
            () -> testPostApi(johnDoeNoName, 400),
            () -> testPostApi(johnDoeEmptyLastName, 400),
            () -> testPostApi(johnDoeNoLastName, 400),
            () -> testPostApi(johnDoeEmptyEmail, 400),
            () -> testPostApi(johnDoeNoEmail, 400),
            () -> testPostApi(johnDoeEmptyPassword, 400),
            () -> testPostApi(johnDoeNoPassword, 400),
            () -> testPostApi(johnDoeWrongEmail1, 400),
            () -> testPostApi(johnDoeWrongEmail2, 400),

            // =================================================================================== test allowed methods
            () -> testDeniedMethods(deniedSignUpMethods, johnDoeCorrectUser),

            // =========================================================================== test response for signup api
            () -> testPostSignUpResponse(johnDoeCorrectUser)
    };
}