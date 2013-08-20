package com.elster.jupiter.util.cron;

import com.elster.jupiter.util.ExceptionTypes;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when parsing of a cron String fails.
 */
public final class InvalidCronExpression extends BaseException {

    public InvalidCronExpression(Throwable cause) {
        super(ExceptionTypes.INVALID_CRON_EXPRESSION, cause);
    }

    public InvalidCronExpression(String message) {
        super(ExceptionTypes.INVALID_CRON_EXPRESSION, message);
    }

    public InvalidCronExpression(String message, Throwable cause) {
        super(ExceptionTypes.INVALID_CRON_EXPRESSION, message, cause);
    }
}
