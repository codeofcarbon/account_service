package codeofcarbon.account.model.dto;

import codeofcarbon.account.model.Payment;
import codeofcarbon.account.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class PaymentDTO {
    private final String name;
    private final String lastname;
    private final String period;
    private final String salary;

    public static PaymentDTO mapResponseForAuthenticatedUser(Payment payment, User employee) {
        var paymentAmount = String.valueOf(payment.getSalary());
        var dollars = paymentAmount.length() > 2 ?
                paymentAmount.substring(0, paymentAmount.length() - 2) : "0";
        var cents = paymentAmount.substring(paymentAmount.length() - 2);

        return PaymentDTO.builder()
                .name(employee.getName())
                .lastname(employee.getLastname())
                .period(DateTimeFormatter.ofPattern("MMMM-yyyy").format(payment.getPeriod()))
                .salary(String.format("%s dollar(s) %s cent(s)", dollars, cents))
                .build();
    }
}