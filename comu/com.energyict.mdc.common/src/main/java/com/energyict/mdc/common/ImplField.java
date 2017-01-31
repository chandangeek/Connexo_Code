/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common;

/**
 * Interface for all enums that represent the private fields of *Impl classes.
 * Intended to prevent duplicating string literals throughout the code base.
 */
public interface ImplField {
    public String fieldName();
}
