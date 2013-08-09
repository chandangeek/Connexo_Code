package com.elster.jupiter.util.beans;

import java.text.MessageFormat;

public class NoSuchPropertyException extends RuntimeException {

    public NoSuchPropertyException(Object bean, String property) {
        super(MessageFormat.format("Bean ''{0}'' has no property named ''{1}''.", bean, property));
    }

    public NoSuchPropertyException(Object bean, String property, Throwable e) {
        super(MessageFormat.format("Bean ''{0}'' has no property named ''{1}''.", bean, property), e);
    }

}
