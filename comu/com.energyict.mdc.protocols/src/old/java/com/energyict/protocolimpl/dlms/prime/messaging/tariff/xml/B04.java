/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class B04 implements Serializable {

    private final List<Contract> contracts = new ArrayList<Contract>();

    public List<Contract> getContracts() {
        return contracts;
    }

}
