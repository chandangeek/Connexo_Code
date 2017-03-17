package com.energyict.mdc.protocol.api;

import aQute.bnd.annotation.ProviderType;
import com.energyict.mdc.pluggable.Pluggable;
import com.energyict.mdc.protocol.api.tasks.support.ConnectionTypeSupport;
import com.energyict.mdc.protocol.api.tasks.support.DeviceProtocolDialectSupport;

/**
 * Defines an Interface between the Data Collection System and a Device. The interface can both be
 * used at operational time and at configuration time.
 */
@ProviderType
public interface DeviceProtocol extends Pluggable, DeviceProtocolDialectSupport,
        DeviceSecuritySupport, ConnectionTypeSupport, com.energyict.mdc.upl.DeviceProtocol {

}