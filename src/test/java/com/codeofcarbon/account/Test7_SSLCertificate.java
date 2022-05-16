package java.com.codeofcarbon.account;

import com.codeofcarbon.account.AccountServiceApplication;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.mocks.web.request.HttpRequest;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

public class Test7_SSLCertificate extends SpringTest {

    public Test7_SSLCertificate() {
        super(AccountServiceApplication.class, "../src/main/resources/service_db.mv.db");
    }

    SSLSocket socket;                                                             // todo --->>> private final????
    X509Certificate[] chain;

    // warning!!! trust all certificates only for testing reason!
    TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };

    // ================================================================================================== test SSL
    public CheckResult checkCertificateName(String nameCN) {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory factory = sc.getSocketFactory();
            HttpRequest request = get("");
            socket = (SSLSocket) factory.createSocket(request.getHost(), request.getPort());
            getCertificates();
            if (findCert(nameCN)) return CheckResult.correct();
            else throw new WrongAnswer("Not found certificate with CN - " + nameCN);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection not found");
            throw new WrongAnswer("Can't establish https connection!");
        }
    }

    // ===================================================================================== get certificate chain
    public void getCertificates() {
        try {
            chain = (X509Certificate[]) socket.getSession().getPeerCertificates();
        } catch (SSLPeerUnverifiedException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    // ====================================================================== searching certificate by Common Name
    public boolean findCert(String subject) {
        for (X509Certificate c : chain) {
//            String subjectName = c.getSubjectDN().getName();                      // todo --->> is that ok???
            String subjectName = c.getSubjectX500Principal().getName();
            System.out.println(subjectName + " " + c.getSigAlgName());
            if (subjectName.contains("CN=" + subject)) return true;
        }
        return false;
    }

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            // ============================================================================ check certificate name
            () -> checkCertificateName("accountant_service"),
    };
}