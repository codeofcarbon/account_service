package com.codeofcarbon.account.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.time.YearMonth;
import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private long id;
    @ManyToOne
    private User user;
    @JsonFormat(pattern = "MM-yyyy")
    private YearMonth period;
    @PositiveOrZero
    private long salary;
}