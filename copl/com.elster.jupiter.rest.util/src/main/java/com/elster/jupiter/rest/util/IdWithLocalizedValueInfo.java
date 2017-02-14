/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

public class IdWithLocalizedValueInfo<T> {
    public T id;
    public String localizedValue;

    public IdWithLocalizedValueInfo(){}

    public IdWithLocalizedValueInfo(T id, String localizedValue) {
        this.id = id;
        this.localizedValue = localizedValue;
    }
}
