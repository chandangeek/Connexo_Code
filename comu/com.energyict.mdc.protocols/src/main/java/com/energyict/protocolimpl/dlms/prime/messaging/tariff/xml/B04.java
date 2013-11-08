package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/20/12
 * Time: 1:51 PM
 */
public class B04 implements Serializable {

    private final List<Contract> contracts = new ArrayList<Contract>();

    public List<Contract> getContracts() {
        return contracts;
    }

}
