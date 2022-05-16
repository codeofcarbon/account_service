package com.codeofcarbon.account.validation;

import com.codeofcarbon.account.model.*;
import com.codeofcarbon.account.repository.*;
import com.codeofcarbon.account.security.AppPasswordEncoder;
import com.codeofcarbon.account.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class Validator {
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final AppPasswordEncoder encoder;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
    private final List<String> breachedPasswords =
            List.of("PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust",
                    "PasswordForSeptember", "PasswordForOctober", "PasswordForNovember", "PasswordForDecember");

    public User validateUser(String userEmail) {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));
    }

    public void isNewUser(User newUser) {
        if (userRepository.existsUserByEmailIgnoreCase(newUser.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User exist!");
    }

    public AdminService.Operation validateOperation(String operation) {
        return Arrays.stream(AdminService.Operation.values())
                .filter(op -> op.name().equals(operation))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operation aborted"));
    }

    public Role validateRole(User user, Role role, AdminService.Operation operation) {
        Arrays.stream(Role.values())
                .filter(r -> r.name().equals(role.name()))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!"));
        switch (operation) {
            case GRANT:
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "The user cannot combine administrative and business roles!");
                break;
            case REMOVE:
                if (role == Role.ROLE_ADMINISTRATOR)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR role!");
                if (!user.getRoles().contains(role))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user does not have a role!");
                if (user.getRoles().size() == 1)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The user must have at least one role!");
                break;
            case DELETE:
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't remove ADMINISTRATOR!");
                }
                break;
            case LOCK:
            case UNLOCK:
                if (user.getRoles().contains(Role.ROLE_ADMINISTRATOR))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't lock the ADMINISTRATOR!");
        }
        return role;
    }

    public String validatePassword(String newPassword, String currentPassword) {
        if (newPassword.length() < 12)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password length must be at least 12 chars!");
        if (breachedPasswords.contains(newPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password is in the hacker's database!");
        if (encoder.matches(newPassword, currentPassword))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The passwords must be different!");
        return encoder.encode(newPassword);
    }

    public String validatePeriod(String period) {
        if (!period.matches("(0[1-9]|1[0-2])-\\d{4}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input desired period in MM-yyyy format");
        return period;
    }

    public long validateSalary(String salary) {
        if (salary.charAt(0) == '-')
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be non negative!");
        return Long.parseLong(salary);
    }

    public Payment prepareValidPayment(Map<String, String> payroll, Payment validPayment, boolean isUpdating) {
        var period = validatePeriod(payroll.get("period"));
        var salary = validateSalary(payroll.get("salary"));
        var employee = validateUser(payroll.get("employee"));

        validPayment.setUser(employee);
        validPayment.setPeriod(YearMonth.parse(period, formatter).atDay(1));
        validPayment.setSalary(salary);

        if (!isUpdating &&
            paymentRepository.existsByUserAndPeriod(employee, YearMonth.parse(period, formatter).atDay(1)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Periods can not duplicate!");

        return validPayment;
    }

    public Payment isAnyPaymentsInPeriod(User user, String period) {
        return paymentRepository.findPaymentByEmployeeIgnoreCaseAndPeriod(
                        user.getEmail(), YearMonth.parse(period, formatter).atDay(1))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No payments in that period"));
    }
}