/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import java.util.function.Supplier;

/**
 * Holds a reference to a variable of type T
 * @param <T>
 */
public interface Holder<T> extends Supplier<T> {

}