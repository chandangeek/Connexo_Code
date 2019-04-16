/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.upgrade;

import aQute.bnd.annotation.ConsumerType;

import java.sql.SQLException;

@ConsumerType
@FunctionalInterface
public interface SqlExceptionThrowingFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t) throws SQLException;
}
