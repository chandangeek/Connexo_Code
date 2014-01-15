package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.tasks.support.DeviceTopologySupport;

import java.util.List;

/**
 * Adapter between a {@link MeterProtocol MeterProtocol} or
 * {@link SmartMeterProtocol} and the {@link DeviceTopologySupport}.
 * A <code>MeterProtocol</code> by default does not support topology updates ...
 *
 * @author gna
 * @since 5/04/12 - 11:23
 */
public class DeviceProtocolTopologyAdapter implements DeviceTopologySupport {

    private DeviceIdentifier deviceIdentifier;
    private CollectedDataFactory collectedDataFactory;

    public DeviceProtocolTopologyAdapter() {
        super();
    }

    /**
     * Collect the actual Topology from a Device. If for some reason the Topology could not be fetched,
     * a proper {@link ResultType} <b>and</b> {@link com.energyict.mdc.issues.Issue}
     * should be set so proper logging of this action can be performed.
     *
     * @return the current Topology
     */
    @Override
    public CollectedTopology getDeviceTopology() {
        CollectedTopology deviceTopology = this.getCollectedDataFactory().createCollectedTopology(deviceIdentifier);
        deviceTopology.setFailureInformation(ResultType.NotSupported, deviceIdentifier.findDevice(), "devicetopologynotsupported");
        return deviceTopology;
    }

    /**
     * The used DeviceIdentifier.
     *
     * @param deviceIdentifier the used DeviceIdentifier
     */
    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    private CollectedDataFactory getCollectedDataFactory() {
        if (this.collectedDataFactory == null) {
            List<CollectedDataFactory> factories = Environment.DEFAULT.get().getApplicationContext().getModulesImplementing(CollectedDataFactory.class);
            if (factories.isEmpty()) {
                throw CommunicationException.missingModuleException(CollectedDataFactory.class);
            }
            else {
                this.collectedDataFactory = factories.get(0);
            }
        }
        return this.collectedDataFactory;
    }

}