package com.energyict.mdc.device.config;

import com.energyict.mdc.device.config.impl.PartialConnectionTask;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;

/**
 * Models the communication aspects of a {@link DeviceConfiguration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-02-15 (10:27)
 */
public interface DeviceCommunicationConfiguration extends HasId {

    public DeviceConfiguration getDeviceConfiguration();

    void setSupportsAllMessageCategories(boolean supportAllMessageCategories);

    void addSecurityPropertySet(SecurityPropertySet securityPropertySet);

    PartialOutboundConnectionTaskBuilder createPartialOutboundConnectionTask();

    PartialInboundConnectionTaskBuilder createPartialInboundConnectionTask();

    PartialConnectionInitiationTaskBuilder createPartialConnectionInitiationTask();

    void addPartialConnectionTask(PartialConnectionTask partialConnectionTask);

    void save();

    void delete();

    ProtocolDialectConfigurationProperties createProtocolDialectConfigurationProperties(String name, DeviceProtocolDialect protocolDialect);
}