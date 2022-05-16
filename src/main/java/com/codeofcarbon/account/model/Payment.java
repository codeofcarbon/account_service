package com.codeofcarbon.account.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private long id;
    @ManyToOne
    private User user;
    @JsonFormat(pattern = "MM-yyyy")
    private LocalDate period;
    @PositiveOrZero
    private long salary;
}