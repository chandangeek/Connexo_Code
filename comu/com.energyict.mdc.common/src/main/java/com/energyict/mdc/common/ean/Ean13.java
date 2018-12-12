/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.ean;

import java.text.ParseException;

public class Ean13 extends EanCode {

    public Ean13(String ean) throws ParseException {
        super(ean);
        if (value.length() != 13) {
            throw new ParseException(value, value.length());
        }
    }

    public Ean13() {

    }
}
