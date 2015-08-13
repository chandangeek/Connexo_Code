package com.elster.protocolimpl.dlms;

import com.energyict.protocol.MeterEvent;

import java.util.Date;
import java.util.List;

/**
 * Copyrights
 * Date: 10/06/11
 * Time: 15:52
 */
public interface EventProtocol {

    List<MeterEvent> getMeterEvents(Date from);

    List<MeterEvent> getMeterEvents(Date from, Date to);

}
