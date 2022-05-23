package com.codeofcarbon.accountservicetests;

import com.codeofcarbon.accountservice.AccountServiceApplication;
import org.hyperskill.hstest.dynamic.SystemHandler;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

//        businessLogicTest.reloadSpring();
        deleteDatabaseFiles(Path.of(databasePath), Path.of(databasePath + "-real"));
        loggingSecurityEventsTest.reloadSpring();
//        businessLogicTest.tearDown();
        loggingSecurityEventsTest.start();
        businessLogicTest.stopSpring();

//        try {
//            SystemHandler.tearDownSystem();
//        } catch (Throwable ignored) {
//        }
//        System.exit(0);
//        this.reloadSpring();
    }

    private static void deleteDatabaseFiles(Path database, Path tempDatabase) {
        try {
            Files.delete(database);
            Files.delete(tempDatabase);
//        } catch (NoSuchFileException e) {
//            System.err.format("%s: no such" + " file or directory\n", database);
//        } catch (DirectoryNotEmptyException e) {
//            System.err.format("%s not empty\n", database);
//        } catch (IOException e) {
//            // =============================================================== file permission problems are caught here
//            System.err.println(e.getMessage());
//        }
//        try {
//            Files.delete(database);
//        } catch (NoSuchFileException e) {
//            System.err.format("%s: no such" + " file or directory\n", database);
//        } catch (DirectoryNotEmptyException e) {
//            System.err.format("%s not empty\n", database);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
//        try {
//            SystemHandler.tearDownSystem();
//        } catch (Throwable ignored) {
//        }
    }
}
