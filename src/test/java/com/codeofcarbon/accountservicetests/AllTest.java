package com.codeofcarbon.accountservicetests;

import com.codeofcarbon.accountservice.AccountServiceApplication;
import org.hyperskill.hstest.stage.SpringTest;
import java.io.IOException;
import java.nio.file.*;

public class AllTest extends SpringTest {
    private final static String databasePath = "./src/main/resources/service_db.mv.db";

    public AllTest() {
        super(AccountServiceApplication.class);
    }

    public static void main(String[] args) {
        var businessLogicTest = new BusinessLogicTest();
        var loggingSecurityEventsTest = new LoggingSecurityEventsTest();
        businessLogicTest.start();
        businessLogicTest.stopSpring();
        try {
            Files.delete(Path.of(databasePath));
            Files.delete(Path.of(databasePath + "-real"));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        loggingSecurityEventsTest.reloadSpring();
        loggingSecurityEventsTest.start();
        loggingSecurityEventsTest.stopSpring();
    }
}