package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.offline.OfflineLogBook;

/**
 * Copyrights EnergyICT
 * Date: 28/11/12
 * Time: 10:02
 */
public interface BaseLogBook extends CanGoOffline<OfflineLogBook> {

    ObisCode getDeviceObisCode();

    long getId();
}