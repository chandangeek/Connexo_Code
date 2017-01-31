/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProtocolInstantiator.java
 *
 * Created on 2 juni 2005, 10:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocols.util;

import com.energyict.mdc.protocol.api.DemandResetProtocol;
import com.energyict.mdc.protocol.api.DialinScheduleProtocol;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.legacy.BulkRegisterProtocol;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexEnabler;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.MultipleLoadProfileSupport;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;

import java.io.IOException;
import java.util.List;

/**
 * @author Koen
 */
public interface ProtocolInstantiator {

    MeterProtocol getMeterProtocol();

    SmartMeterProtocol getSmartMeterProtocol();

    HHUEnabler getHHUEnabler();

    HalfDuplexEnabler getHalfDuplexEnabler();

    SerialNumber getSerialNumber();

    CacheMechanism getCacheMechanism();

    RegisterProtocol getRegisterProtocol();

    BulkRegisterProtocol getBulkRegisterProtocol();

    DialinScheduleProtocol getDialinScheduleProtocol();

    DemandResetProtocol getDemandResetProtocol(); // KV 30082006

    MultipleLoadProfileSupport getMultipleLoadProfileSupport();

    boolean isRegisterProtocolEnabled();

    boolean isBulkRegisterProtocolEnabled();

    boolean isDialinScheduleProtocolEnabled();

    boolean isDemandResetProtocolEnabled(); // KV 30082006

    boolean isHHUEnabled();

    boolean isHalfDuplexEnabled();

    boolean isSerialNumber();

    boolean isCacheMechanism();

    boolean isMultipleLoadProfileSupported();

    List getOptionalKeys();

    List getRequiredKeys();

    void buildInstance(String className) throws IOException;

    EventMapper getEventMapper();

}
