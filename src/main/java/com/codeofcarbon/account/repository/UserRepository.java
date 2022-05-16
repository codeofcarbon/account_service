package com.codeofcarbon.account.repository;

import com.codeofcarbon.account.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("select u from User u where upper(u.email) = upper(?1)")
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("select (count(u) > 0) from User u where upper(u.email) = upper(?1)")
    Boolean existsUserByEmailIgnoreCase(String email);

    @Query("select u from User u where upper(u.email) = upper(?1)")
    User findUserByEmailIgnoreCase(String email);

    @Query("select u from User u order by u.id")
    List<User> findAllOrderById();
}