package codeofcarbon.account.service;

import codeofcarbon.account.exception.DuplicatePeriodException;
import codeofcarbon.account.exception.IncorrectPeriodFormatException;
import codeofcarbon.account.exception.NegativeSalaryException;
import codeofcarbon.account.exception.WrongPeriodDateException;
import codeofcarbon.account.model.Payment;
import codeofcarbon.account.model.User;
import codeofcarbon.account.model.dto.PaymentDTO;
import codeofcarbon.account.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserService userService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserService userService) {
        this.paymentRepository = paymentRepository;
        this.userService = userService;
    }

    public List<PaymentDTO> getEmployeePayments(User user, String period) {
        if (period == null) {
            return paymentRepository.findAllByEmplEmailOrderByPeriodDesc(user.getEmail()).stream()
                    .map(payment -> PaymentDTO.mapResponseForAuthenticatedUser(payment, user))
                    .collect(Collectors.toList());
        } else try {
            var givenPeriod = YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy"));
            var givenPeriodPayment = paymentRepository.findAllByEmplEmailOrderByPeriodDesc(user.getEmail()).stream()
                    .filter(payment -> payment.getPeriod().equals(givenPeriod))
                    .findFirst().orElseThrow(WrongPeriodDateException::new);
            return List.of(PaymentDTO.mapResponseForAuthenticatedUser(givenPeriodPayment, user));
        } catch (DateTimeParseException ignored) {
            throw new IncorrectPeriodFormatException();
        }
    }

    @Transactional
    public void addPayrolls(List<Map<String, String>> payrollsData) {
        payrollsData.stream()
                .map(payroll -> checkValidPaymentRequirements(payroll, false))
                .forEach(paymentRepository::save);
    }

    @Transactional
    public void updateEmployeeSalary(Map<String, String> paymentToUpdate) {
        var validPayment = checkValidPaymentRequirements(paymentToUpdate, true);
        paymentRepository.save(validPayment);
    }

    public Payment checkValidPaymentRequirements(Map<String, String> payroll, boolean updatingData) {
        var validPayment = new Payment();
        try {
            validPayment.setPeriod(YearMonth.parse(payroll.get("period"), DateTimeFormatter.ofPattern("MM-yyyy")));
            if (userService.loadUserByUsername(payroll.get("employee")) != null)
                validPayment.setEmpl((User) userService.loadUserByUsername(payroll.get("employee")));
            if (payroll.get("salary").startsWith("-")) throw new NegativeSalaryException();
            else validPayment.setSalary(Long.parseLong(payroll.get("salary")));
        } catch (DateTimeParseException ignored) {
            throw new IncorrectPeriodFormatException();
        }
        var duplicates = paymentRepository.findAll().stream()
                .filter(payslip -> payslip.getEmpl().getEmail().equalsIgnoreCase(validPayment.getEmpl().getEmail())
                                   && payslip.getPeriod().equals(validPayment.getPeriod()))
                .collect(Collectors.toList());
        if (updatingData) duplicates.forEach(payment -> payment.setSalary(Long.parseLong(payroll.get("salary"))));
        if (!updatingData && duplicates.size() != 0) throw new DuplicatePeriodException();
        return validPayment;
    }
}