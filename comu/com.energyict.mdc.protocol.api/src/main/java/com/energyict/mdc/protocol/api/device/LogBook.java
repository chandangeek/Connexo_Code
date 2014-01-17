package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Protectable;
import com.energyict.mdc.protocol.api.device.events.MeterProtocolEvent;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/11/12
 * Time: 10:02
 */
public interface LogBook extends IdBusinessObject, Protectable, CanGoOffline<OfflineLogBook> {

    int getDeviceId();

    Device getDevice();

    ObisCode getDeviceObisCode();

    Date getLastLogBook();

    void store(MeterProtocolEvent event) throws BusinessException, SQLException;

    void store(List<MeterProtocolEvent> event) throws BusinessException, SQLException;

    String getPath();

    void updateLastLogbookIfLater(Date lastDate) throws BusinessException, SQLException;

}