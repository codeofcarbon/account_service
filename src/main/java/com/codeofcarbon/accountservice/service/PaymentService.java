package com.codeofcarbon.accountservice.service;

import com.codeofcarbon.accountservice.model.*;
import com.codeofcarbon.accountservice.model.dto.PaymentDTO;
import com.codeofcarbon.accountservice.repository.PaymentRepository;
import com.codeofcarbon.accountservice.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
    private final Validator validator;

    public Map<String, String> addPayrolls(List<Map<String, String>> payrollsData) {
        payrollsData.stream()
                .map(payroll -> validator.prepareValidPayment(payroll, new Payment(), false))
                .forEach(paymentRepository::save);
        return Map.of("status", "Added successfully!");
    }

    public Map<String, String> updateEmployeeSalary(Map<String, String> payrollToUpdate) {
        var validPayment = validator.prepareValidPayment(payrollToUpdate, new Payment(), true);
        var payment = validator.isAnyPaymentsInPeriod(validPayment.getUser(), validPayment.getPeriod().format(formatter));

        payment.setSalary(validPayment.getSalary());
        paymentRepository.save(payment);
        return Map.of("status", "Updated successfully!");
    }

    public Object getEmployeePayments(User user, String period) {
        if (period != null) {
            validator.validatePeriod(period);
            var payment = validator.isAnyPaymentsInPeriod(user, period);
            return PaymentDTO.mapToPaymentDTO(payment, user);
        }
        return paymentRepository.findAllByUserEmailOrderByPeriodDesc(user.getEmail()).stream()
                .map(payment -> PaymentDTO.mapToPaymentDTO(payment, user))
                .collect(Collectors.toList());
    }
}