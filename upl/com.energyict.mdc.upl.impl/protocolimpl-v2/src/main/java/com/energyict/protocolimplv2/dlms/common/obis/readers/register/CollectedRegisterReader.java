package com.energyict.protocolimplv2.dlms.common.obis.readers.register;

import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;

public interface CollectedRegisterReader<T> extends ObisReader<CollectedRegister, OfflineRegister, T> {

}
