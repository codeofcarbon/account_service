package com.codeofcarbon.accountservice.repository;

import com.codeofcarbon.accountservice.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.*;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select p from Payment p where upper(p.user.email) = upper(?1) order by p.period DESC")
    List<Payment> findAllByUserEmailOrderByPeriodDesc(String userEmail);

    @Query("select (count(p) > 0) from Payment p where p.user = ?1 and p.period = ?2")
    Boolean existsByUserAndPeriod(User user, LocalDate period);

    @Query("select p from Payment p where upper(p.user.email) = upper(?1) and p.period = ?2")
    Optional<Payment> findPaymentByEmployeeIgnoreCaseAndPeriod(String userEmail, LocalDate period);
}