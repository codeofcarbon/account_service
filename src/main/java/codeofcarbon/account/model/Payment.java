package codeofcarbon.account.model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.PositiveOrZero;
import java.time.YearMonth;

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
    private User empl;
    @JsonFormat(pattern = "MM-yyyy")
    private YearMonth period;
    @PositiveOrZero
    private long salary;
}