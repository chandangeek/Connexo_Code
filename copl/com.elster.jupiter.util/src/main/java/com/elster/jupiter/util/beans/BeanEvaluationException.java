package com.elster.jupiter.util.beans;

import com.elster.jupiter.util.ExceptionTypes;
import com.elster.jupiter.util.exception.BaseException;

public final class BeanEvaluationException extends BaseException {

    public BeanEvaluationException(Object bean, Throwable cause) {
        super(ExceptionTypes.BEAN_AVALUATION_FAILED, "Exception occurred while evaluating bean " + bean.toString(), cause);
    }
}
