package com.energyict.mdc.protocol.api.device;

import com.energyict.mdc.common.CanGoOffline;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;

import java.util.List;

/**
 * Register represents a single register in a rtu
 */
public interface BaseRegister extends CanGoOffline<OfflineRegister> {

    ObisCode getRegisterMappingObisCode();

    ObisCode getRegisterSpecObisCode();

    ObisCode getDeviceObisCode();

    long getRegisterSpecId();
}