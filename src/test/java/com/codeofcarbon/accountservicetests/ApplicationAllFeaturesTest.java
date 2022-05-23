package com.codeofcarbon.accountservicetests;

import com.codeofcarbon.accountservice.AccountServiceApplication;
import org.hyperskill.hstest.dynamic.SystemHandler;
import org.hyperskill.hstest.stage.SpringTest;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;

public class ApplicationAllFeaturesTest extends SpringTest {
    private final static String databasePath = "./src/main/resources/service_db.mv.db";

    public ApplicationAllFeaturesTest() {
        super(AccountServiceApplication.class);
    }

    @Test
    public void allFeaturesTest() {
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
        try {
            SystemHandler.tearDownSystem();
        } catch (Throwable ignored) {
        }
    }
}