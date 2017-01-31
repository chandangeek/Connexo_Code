/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.beans;

import com.elster.jupiter.util.MessageSeeds;
import com.elster.jupiter.util.exception.BaseException;

/**
 * Thrown when evaluation of a bean fails.
 */
public final class BeanEvaluationException extends BaseException {

	private static final long serialVersionUID = 1L;

	public BeanEvaluationException(Object bean, Throwable cause) {
        super(MessageSeeds.BEAN_EVALUATION_FAILED, cause, bean);
        set("bean", bean);
    }

	public BeanEvaluationException(Class beanClass, Throwable cause) {
        super(MessageSeeds.BEAN_EVALUATION_FAILED_ON_CLASS, cause, beanClass);
        set("beanClass", beanClass);
    }

}