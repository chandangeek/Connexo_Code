package com.elster.jupiter.util.beans;

public class BeanEvaluationException extends RuntimeException {

    public BeanEvaluationException(Object bean, Throwable cause) {
        super("Exception occurred while evaluating bean " + bean.toString(), cause);
    }
}
