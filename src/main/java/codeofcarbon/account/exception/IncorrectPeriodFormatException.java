package codeofcarbon.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Incorrect period. Input desired period in MM-yyyy format")
public class IncorrectPeriodFormatException extends RuntimeException {
}
