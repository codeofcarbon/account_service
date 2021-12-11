package codeofcarbon.account.controller;

import codeofcarbon.account.model.User;
import codeofcarbon.account.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api")
public class EmployeeController {
    private final PaymentService paymentService;

    @Autowired
    public EmployeeController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //    gives access to the payroll of an employee
    @GetMapping("/empl/payment")
    public ResponseEntity<Object> showPayments(@RequestParam(value = "period", required = false) String period,
                                               @AuthenticationPrincipal UserDetails details) throws Exception {
        var response = paymentService.getEmployeePayments((User) details, period);
        return period == null ? ResponseEntity.ok(response) : ResponseEntity.ok(response.get(0));
    }

    //    changes the salary of a specific user
    @PostMapping("/acct/payments")
    public ResponseEntity<Object> uploadPayrolls(@RequestBody List<Map<String, String>> payrollsData) {
        paymentService.addPayrolls(payrollsData);
        return ResponseEntity.ok(Map.of("status", "Added successfully!"));
    }

    //    uploads employee payrolls
    @PutMapping("/acct/payments")
    public ResponseEntity<Object> changeEmployeeSalary(@RequestBody Map<String, String> payroll) {
        paymentService.updateEmployeeSalary(payroll);
        return ResponseEntity.ok(Map.of("status", "Updated successfully!"));
    }
}