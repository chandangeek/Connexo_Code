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
    public BigInteger unmarshal(String value) throws Exception {
        return new BigInteger(value);
    }

    @Override
    public String marshal(BigInteger bigInteger) throws Exception {
        return bigInteger.toString();
    }
}
