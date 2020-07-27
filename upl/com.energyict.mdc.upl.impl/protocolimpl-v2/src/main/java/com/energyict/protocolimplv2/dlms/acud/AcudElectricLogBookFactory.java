package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;

import java.util.Arrays;
import java.util.List;

public class AcudElectricLogBookFactory extends AcudLogBookFactory{

    private static final ObisCode[] supportedLogBooks = new ObisCode[]{
            /* Event log (Power) */ ObisCode.fromString("0.0.99.98.0.255"),
            /* Event log (Synchro) */ ObisCode.fromString("0.0.99.98.1.255"),
            /* Event log (Common) */ ObisCode.fromString("0.0.99.98.2.255"),
            /* Event log (Memory) */ ObisCode.fromString("0.0.99.98.3.255"),
            /* Event log (Tamper1) */ ObisCode.fromString("0.0.99.98.4.255"),
            /* Event log (Tamper2) */ ObisCode.fromString("0.0.99.98.5.255"),
            /* Event log (Communication) */ ObisCode.fromString("0.0.99.98.6.255"),
            /* Event log (Quality) */ ObisCode.fromString("0.0.99.98.7.255"),
            /* Event log (Cut) */ ObisCode.fromString("0.0.99.98.8.255"),
            /* Event log (Current) */ ObisCode.fromString("0.0.99.98.9.255"),
            /* Event log (Disconnector) */ ObisCode.fromString("0.0.99.98.10.255"),
            /* Event log (Firmware) */ ObisCode.fromString("0.0.99.98.11.255"),
            /* Event log (Password) */ ObisCode.fromString("0.0.99.98.12.255"),
            /* Event log (Security) */ ObisCode.fromString("0.0.99.98.13.255"),
            /* Maximum demand Event Log */ ObisCode.fromString("1.0.94.20.62.255"),
    };

    public AcudElectricLogBookFactory(Acud protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
    }

    protected List<ObisCode> getSupportedLogBooks() {
        return Arrays.asList(supportedLogBooks);
    }
}
