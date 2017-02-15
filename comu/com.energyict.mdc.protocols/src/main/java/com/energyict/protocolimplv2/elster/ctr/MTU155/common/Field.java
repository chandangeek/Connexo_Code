/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.common;

import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;

public interface Field<T extends Field> {

    byte[] getBytes();

    T parse(byte[] rawData, int offset) throws CTRParsingException;

    int getLength();

}
