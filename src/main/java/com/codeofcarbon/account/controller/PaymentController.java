package com.codeofcarbon.account.controller;

import com.codeofcarbon.account.model.User;
import com.codeofcarbon.account.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api")
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // gives access to the employee payrolls
    @GetMapping("/empl/payment")
    public ResponseEntity<Object> showPayments(@RequestParam(value = "period", required = false) String period,
                                               @AuthenticationPrincipal UserDetails details) {
        var response = paymentService.getEmployeePayments((User) details, period);
        return period == null ? ResponseEntity.ok(response) : ResponseEntity.ok(response.get(0));
    }

    // changes the salary of a specific user
    @PostMapping("/acct/payments")
    public ResponseEntity<Object> uploadPayrolls(@RequestBody List<Map<String, String>> payrollsData) {
        paymentService.addPayrolls(payrollsData);
        return ResponseEntity.ok(Map.of("status", "Added successfully!"));
    }

    // uploads employee payrolls
    @PutMapping("/acct/payments")
    public ResponseEntity<Object> changeEmployeeSalary(@RequestBody Map<String, String> payroll) {
        paymentService.updateEmployeeSalary(payroll);
        return ResponseEntity.ok(Map.of("status", "Updated successfully!"));
    }
}