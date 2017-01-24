package com.energyict.protocolimpl.dlms.prime.messaging.tariff.xml;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActiveCalendars implements Serializable {

    private List<Contract> contract;

    public List<Contract> getContract() {
        if (contract == null) {
            contract = new ArrayList<Contract>();
        }
        return this.contract;
    }

    public void addContract(Contract contract) {
        getContract().add(contract);
    }

}
