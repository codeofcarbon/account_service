package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.*;
import com.codeofcarbon.account.model.dto.PaymentDTO;
import com.codeofcarbon.account.repository.PaymentRepository;
import com.codeofcarbon.account.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");

    public Map<String, String> addPayrolls(List<Map<String, String>> payrollsData) {
        payrollsData.stream()
                .map(payroll -> prepareValidPayment(payroll, new Payment(), false))
                .forEach(paymentRepository::save);
        return Map.of("status", "Added successfully!");
    }

    public Map<String, String> updateEmployeeSalary(Map<String, String> payrollToUpdate) {
        var validPayment = prepareValidPayment(payrollToUpdate, new Payment(), true);
        var payment = paymentRepository.findPaymentByEmployeeIgnoreCaseAndPeriod(
                validPayment.getUser().getEmail(), validPayment.getPeriod())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No payments in that period"));

        payment.setSalary(validPayment.getSalary());
        paymentRepository.save(payment);
        return Map.of("status", "Updated successfully!");
    }

    public Object getEmployeePayments(User user, String period) {
        if (period != null) {
            if (!period.matches("(0[1-9]|1[0-2])-\\d{4}"))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input desired period in MM-yyyy format");

            var payment = paymentRepository.findPaymentByEmployeeIgnoreCaseAndPeriod(
                            user.getEmail(), YearMonth.parse(period, formatter).atDay(1))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No payments in that period"));
            return PaymentDTO.mapToPaymentDTO(payment, user);
        }
        return paymentRepository.findAllByUserEmailOrderByPeriodDesc(user.getEmail()).stream()
                .map(payment -> PaymentDTO.mapToPaymentDTO(payment, user))
                .collect(Collectors.toList());
    }

    public Payment prepareValidPayment(Map<String, String> payroll, Payment validPayment, boolean isUpdating) {
       if (!payroll.get("period").matches("(0[1-9]|1[0-2])-\\d{4}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input desired period in MM-yyyy format");

       if (payroll.get("salary").charAt(0) == '-')
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be non negative!");

        var employee = userRepository.findByEmailIgnoreCase(payroll.get("employee"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found!"));

        validPayment.setUser(employee);
        validPayment.setPeriod(YearMonth.parse(payroll.get("period"), formatter).atDay(1));
        validPayment.setSalary(Long.parseLong(payroll.get("salary")));

        if (!isUpdating &&
            paymentRepository.existsByUserAndPeriod(employee, YearMonth.parse(payroll.get("period"), formatter).atDay(1)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Periods can not duplicate!");

        return validPayment;
    }


}