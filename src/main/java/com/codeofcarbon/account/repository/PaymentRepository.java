package com.codeofcarbon.account.repository;

import com.codeofcarbon.account.model.Payment;
import com.codeofcarbon.account.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("select p from Payment p where upper(p.user.email) = upper(?1) order by p.period DESC")
    List<Payment> findAllByUserEmailOrderByPeriodDesc(String userEmail);

    @Query("select (count(p) > 0) from Payment p where p.user = ?1 and p.period = ?2")
    Boolean existsByUserAndPeriod(User user, LocalDate period);

    @Query("select p from Payment p where upper(p.user.email) = upper(?1) and p.period = ?2")
    Optional<Payment> findPaymentByEmployeeIgnoreCaseAndPeriod(String userEmail, LocalDate period);
}