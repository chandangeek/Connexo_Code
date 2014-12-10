package com.elster.jupiter.util.beans;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when a property of a bean is accessed, which it does not have.
 */
public final class NoSuchPropertyException extends BaseException {

	private static final long serialVersionUID = 1L;

	public NoSuchPropertyException(Object bean, String property) {
        super(MessageSeeds.NO_SUCH_PROPERTY, bean, property);
    }

    public NoSuchPropertyException(Object bean, String property, Throwable e) {
        super(MessageSeeds.NO_SUCH_PROPERTY, e, bean, property);
    }

}
