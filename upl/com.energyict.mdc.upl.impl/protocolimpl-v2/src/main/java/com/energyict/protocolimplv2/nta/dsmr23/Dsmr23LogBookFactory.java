package com.energyict.protocolimplv2.nta.dsmr23;

import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.protocol.tasks.support.DeviceLogBookSupport;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaProtocol;

import java.util.List;

/**
 * @author: sva
 * @since: 13/11/12 (9:28)
 */
public class Dsmr23LogBookFactory implements DeviceLogBookSupport {

    private AbstractNtaProtocol protocol;

    public Dsmr23LogBookFactory(AbstractNtaProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
