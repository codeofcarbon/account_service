package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api")
@ResponseStatus(HttpStatus.OK)
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/empl/payment")
    public Object showPayments(@AuthenticationPrincipal UserDetails details,
                               @RequestParam(required = false) String period) {
        return paymentService.getEmployeePayments((User) details, period);
    }

    @PostMapping("/acct/payments")
    public Map<String, String> uploadPayrolls(@RequestBody List<Map<String, String>> payrollsData) {
        return paymentService.addPayrolls(payrollsData);
    }

    @PutMapping("/acct/payments")
    public Map<String, String> changeEmployeeSalary(@RequestBody Map<String, String> payroll) {
        return paymentService.updateEmployeeSalary(payroll);
    }
}