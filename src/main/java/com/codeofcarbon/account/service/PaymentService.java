package com.codeofcarbon.account.service;

import com.codeofcarbon.account.model.*;
import com.codeofcarbon.account.model.dto.PaymentDTO;
import com.codeofcarbon.account.repository.PaymentRepository;
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
    private final UserService userService;

    public List<PaymentDTO> getEmployeePayments(User user, String period) {
        var paymentsStream = paymentRepository.findAllByUserEmailOrderByPeriodDesc(user.getEmail()).stream();
        if (period == null)
            return paymentsStream.map(payment -> PaymentDTO.mapToPaymentDTO(payment, user)).collect(Collectors.toList());

        if (!period.matches("(0[1-9]|1[0-2])-\\d{4}"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input desired period in MM-yyyy format");

        var yearMonth = YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy")).toString();
        var periodPayment = paymentsStream.filter(payment -> payment.getPeriod().equals(yearMonth)).findAny();
        if (periodPayment.isPresent())
            return List.of(PaymentDTO.mapToPaymentDTO(periodPayment.get(), user));
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No payments in that period");
    }

    public void addPayrolls(List<Map<String, String>> payrollsData) {
        payrollsData.stream()
                .map(payroll -> prepareValidPayment(payroll, new Payment(), false))
                .forEach(paymentRepository::save);
    }

    public void updateEmployeeSalary(Map<String, String> paymentToUpdate) {
        var validPayment = prepareValidPayment(paymentToUpdate, new Payment(), true);
        paymentRepository.save(validPayment);
    }

    private Payment prepareValidPayment(Map<String, String> payroll, Payment validPayment, boolean updating) {
        if (userService.loadUserByUsername(payroll.get("employee")) != null)
            validPayment.setUser((User) userService.loadUserByUsername(payroll.get("employee")));

        if (payroll.get("period").matches("(0[1-9]|1[0-2])-\\d{4}"))
            validPayment.setPeriod(YearMonth.parse(payroll.get("period"), DateTimeFormatter.ofPattern("MM-yyyy")).toString());
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input desired period in MM-yyyy format");

        if (payroll.get("salary").charAt(0) != '-')
            validPayment.setSalary(Long.parseLong(payroll.get("salary")));
        else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Salary must be non negative!");

        paymentRepository.findAll().stream()
                .filter(payment -> payment.getUser().getEmail().equals(validPayment.getUser().getEmail())
                                   && payment.getPeriod().equals(validPayment.getPeriod()))
                .findAny().ifPresent(payment -> {
                    if (updating) payment.setSalary(Long.parseLong(payroll.get("salary")));
                    else throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Periods can not duplicate!");
                });
        return validPayment;
    }
}