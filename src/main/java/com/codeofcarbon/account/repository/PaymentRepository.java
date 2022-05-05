package com.codeofcarbon.account.repository;

import com.codeofcarbon.account.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByEmplEmailOrderByPeriodDesc(String employee_email);
}