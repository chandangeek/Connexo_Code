/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

public class DeviceAttributeInfo<T> {
    public long attributeId;
    public T displayValue;
    public boolean editable;
    public boolean available;
}
