package com.codeofcarbon.accountservice.repository;

import com.codeofcarbon.accountservice.model.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u where upper(u.email) = upper(?1)")
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("select (count(u) > 0) from User u where upper(u.email) = upper(?1)")
    Boolean existsUserByEmailIgnoreCase(String email);

    @Query("select u from User u order by u.id")
    List<User> findAllOrderById();
}