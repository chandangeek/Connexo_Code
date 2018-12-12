/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.transaction;

@FunctionalInterface
public interface Transaction<T> {

    T perform();

}
