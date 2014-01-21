package com.elster.jupiter.util.beans;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when evaluation of a bean fails.
 */
public final class BeanEvaluationException extends BaseException {

    public BeanEvaluationException(Object bean, Throwable cause) {
        super(MessageSeeds.BEAN_AVALUATION_FAILED, cause, bean);
        set("bean", bean);
    }
}
