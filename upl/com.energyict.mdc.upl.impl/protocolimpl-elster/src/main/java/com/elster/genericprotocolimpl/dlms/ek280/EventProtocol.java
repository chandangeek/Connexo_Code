package com.elster.genericprotocolimpl.dlms.ek280;

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

}
