package com.codeofcarbon.account.model.dto;

import com.codeofcarbon.account.model.*;
import lombok.*;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class PaymentDTO {
    private final String name;
    private final String lastname;
    private final String period;
    private final String salary;

    public static PaymentDTO mapToPaymentDTO(Payment payment, User user) {
        return PaymentDTO.builder()
                .name(user.getName())
                .lastname(user.getLastname())
                .period(DateTimeFormatter.ofPattern("MMMM-yyyy").format(YearMonth.parse(payment.getPeriod())))
                .salary(String.format("%s dollar(s) %s cent(s)", payment.getSalary() / 100, payment.getSalary() % 100))
                .build();
    }
}