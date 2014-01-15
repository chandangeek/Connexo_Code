package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

/**
 * Models an {@link InboundDeviceProtocol} that was registered as a {@link PluggableClass}.
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 10:16
 */
public interface InboundDeviceProtocolPluggableClass extends PluggableClass {

    public InboundDeviceProtocol getInboundDeviceProtocol ();

}