/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface FirmwareCampaign extends HasId, HasName {
    void setName(String name);

    DeviceType getDeviceType();

    FirmwareType getFirmwareType();

    void setFirmwareType(FirmwareType firmwareType);

    FirmwareVersion getFirmwareVersion();

    ProtocolSupportedFirmwareOptions getFirmwareManagementOption();

    void setManagementOption(ProtocolSupportedFirmwareOptions upgradeOption);

    Map<String, Object> getProperties();

    Optional<DeviceMessageSpec> getFirmwareMessageSpec();

    String getDeviceGroup();

    Instant getUploadPeriodStart();

    Instant getUploadPeriodEnd();

    void setUploadPeriodStart(Instant start);

    void setUploadPeriodEnd(Instant end);

    Instant getActivationDate();

    TimeDuration getValidationTimeout();

    ServiceCall getServiceCall();

    Map<DefaultState, Long> getNumbersOfChildrenWithStatuses();

    void update();

    void cancel();

    void delete();

    long getVersion();

    FirmwareCampaign addProperties(Map<PropertySpec, Object> map);

    void clearProperties();

    ComWindow getComWindow();

    boolean isWithVerification();

    Instant getStartedOn();

    Instant getFinishedOn();

    List<DeviceInFirmwareCampaign> getDevices();

    void setFirmwareUploadComTaskId(long firmwareUploadComTaskId);

    void setFirmwareUploadConnectionStrategy(ConnectionStrategy firmwareUploadConnectionStrategy);

    void setValidationComTaskId(long validationComTaskId);

    void setValidationConnectionStrategy(ConnectionStrategy validationConnectionStrategy);

    long getFirmwareUploadComTaskId();

    Optional<ConnectionStrategy> getFirmwareUploadConnectionStrategy();

    long getValidationComTaskId();

    Optional<ConnectionStrategy> getValidationConnectionStrategy();
}
