package com.energyict.mdc.protocol.pluggable;

import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;

/**
 * Defines the interface of a component that is interested
 * in receiving notification when a protocol related
 * component is being deployed while the platform is running.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-29 (10:04)
 */
public interface ProtocolDeploymentListener {

    /**
     * Notifies the receiver that a new {@link DeviceProtocolService} was deployed.
     *
     * @param service The DeviceProtocolService that was deployed
     */
    public void deviceProtocolServiceDeployed(DeviceProtocolService service);

    /**
     * Notifies the receiver that a new {@link DeviceProtocolService} was undeployed.
     *
     * @param service The DeviceProtocolService that was undeployed
     */
    public void deviceProtocolServiceUndeployed(DeviceProtocolService service);

    /**
     * Notifies the receiver that a new {@link DeviceProtocolService} was deployed.
     *
     * @param service The InboundDeviceProtocolService that was deployed
     */
    public void inboundDeviceProtocolServiceDeployed(InboundDeviceProtocolService service);

    /**
     * Notifies the receiver that a new {@link DeviceProtocolService} was undeployed.
     *
     * @param service The InboundDeviceProtocolService that was undeployed
     */
    public void inboundDeviceProtocolServiceUndeployed(InboundDeviceProtocolService service);

    /**
     * Notifies the receiver that a new {@link ConnectionTypeService} was deployed.
     *
     * @param service The ConnectionTypeService that was deployed
     */
    public void connectionTypeServiceDeployed(ConnectionTypeService service);

    /**
     * Notifies the receiver that a new {@link ConnectionTypeService} was undeployed.
     *
     * @param service The ConnectionTypeService that was undeployed
     */
    public void connectionTypeServiceUndeployed(ConnectionTypeService service);

}