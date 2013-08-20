package com.elster.jupiter.util.beans;

import com.elster.jupiter.util.ExceptionTypes;
import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

/**
 * Thrown when a property of a bean is accessed, which it does not have.
 */
public final class NoSuchPropertyException extends BaseException {

    public NoSuchPropertyException(Object bean, String property) {
        super(ExceptionTypes.NO_SUCH_PROPERTY, MessageFormat.format("Bean ''{0}'' has no property named ''{1}''.", bean, property));
    }

    public NoSuchPropertyException(Object bean, String property, Throwable e) {
        super(ExceptionTypes.NO_SUCH_PROPERTY, MessageFormat.format("Bean ''{0}'' has no property named ''{1}''.", bean, property), e);
    }

}
