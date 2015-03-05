package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.impl.device.messages.FirmwareDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 3/5/15
 * Time: 10:33 AM
 */
public class FirmwareServiceImpl implements FirmwareService {

    @Override
    public Set<ProtocolSupportedFirmwareOptions> getFirmwareOptionsFor(DeviceType deviceType) {
        Set<DeviceMessageId> supportedMessages = deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();

        return Stream.of(FirmwareDeviceMessage.values())
                .filter(deviceMessage -> supportedMessages.contains(deviceMessage.getId()))
                .map(FirmwareDeviceMessage::getProtocolSupportedFirmwareOption)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
