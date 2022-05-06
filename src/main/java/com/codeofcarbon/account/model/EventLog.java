package com.codeofcarbon.account.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@ToString
@RequiredArgsConstructor
//@NoArgsConstructor
@AllArgsConstructor
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long id;
    @DateTimeFormat//(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private Action action;
    private String subject;
    private String object;
    private String path;
}