/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.rest.impl;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.math.BigInteger;

/**
 * Created by bvn on 4/19/17.
 */
public class SerialNumberAdapter extends XmlAdapter<String, BigInteger> {
    @Override
    public BigInteger unmarshal(String value) {
        if (value!=null && value.toUpperCase().startsWith("0X")){
            value = value.substring(2);
        }
        return new BigInteger(value,16);
    }

    @Override
    public String marshal(BigInteger bigInteger) {
        return "0x" + bigInteger.toString(16).toUpperCase();
    }
}
