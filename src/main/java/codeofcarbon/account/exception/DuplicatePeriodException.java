package codeofcarbon.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Incorrect period data. Periods can not duplicate!")
public class DuplicatePeriodException extends RuntimeException {
}
