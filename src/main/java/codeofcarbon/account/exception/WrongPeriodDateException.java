package codeofcarbon.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Wrong date! An employee had no payments in that period")
public class WrongPeriodDateException extends RuntimeException {
}
