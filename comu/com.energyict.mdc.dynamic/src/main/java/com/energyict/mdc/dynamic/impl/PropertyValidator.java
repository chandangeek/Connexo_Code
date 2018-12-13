/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.dynamic.impl;

public interface PropertyValidator<T> {

    boolean validate(T value);

}
