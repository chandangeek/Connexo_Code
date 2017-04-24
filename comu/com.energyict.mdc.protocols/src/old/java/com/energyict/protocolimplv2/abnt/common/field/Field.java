/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 10:12
 */
public interface Field<T extends Field> {

    byte[] getBytes();

    T parse(byte[] rawData, int offset) throws ParsingException;

    int getLength();

}