/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.impl.ami.EndDeviceControlTypeMapping;

import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControlAttribute;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditAmount;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditDaysLimitFirst;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditDaysLimitScnd;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditTypeAttributeName;

public class HeadEndController {

    private final Thesaurus thesaurus;
    private final Clock clock;

    @Inject
    public HeadEndController(Thesaurus thesaurus, Clock clock) {
        this.thesaurus = thesaurus;
        this.clock = clock;
    }

    public DeviceCommandInfo checkOperation(String commandCode, List<EndDeviceControlAttribute> attributes) {
        DeviceCommandInfo deviceCommandInfo = new DeviceCommandInfo();

        EndDeviceControlTypeMapping endDeviceControlTypeMapping = EndDeviceControlTypeMapping.getMappingWithoutDeviceTypeFor(commandCode);
        if (endDeviceControlTypeMapping.equals(EndDeviceControlTypeMapping.OTHER)) {
            throw CommandException.unsupportedEndDeviceControlType(thesaurus, commandCode);
        }

        switch (endDeviceControlTypeMapping) {
            case CLOSE_REMOTE_SWITCH:
            case OPEN_REMOTE_SWITCH:
                deviceCommandInfo.setActivationDate(checkAndGetContactorActivationDateAttribute(commandCode, attributes));
                break;
            case UPDATE_CREDIT_DAYS_LIMIT:
                checkAndSetUpdateCreditDaysLimitAttributes(deviceCommandInfo, attributes, commandCode);
                break;
            case UPDATE_CREDIT_AMOUNT:
                checkAndSetUpdateCreditAmountAttributes(deviceCommandInfo, attributes, commandCode);
                break;
            default:
                throw CommandException.unsupportedEndDeviceControlType(thesaurus, commandCode);
        }

        deviceCommandInfo.setEndDeviceControlTypeMapping(endDeviceControlTypeMapping);

        return deviceCommandInfo;
    }

    public void performOperations(EndDevice endDevice, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Instant releaseDate) {
        HeadEndInterface headEndInterface = getHeadEndInterface(endDevice);
        EndDeviceCommand deviceCommand;
        switch (deviceCommandInfo.getEndDeviceControlTypeMapping()) {
            case CLOSE_REMOTE_SWITCH:
                deviceCommand = headEndInterface.getCommandFactory().createConnectCommand(endDevice, deviceCommandInfo.getActivationDate());
                break;
            case OPEN_REMOTE_SWITCH:
                deviceCommand = headEndInterface.getCommandFactory().createDisconnectCommand(endDevice, deviceCommandInfo.getActivationDate());
                break;
            case UPDATE_CREDIT_DAYS_LIMIT:
                deviceCommand = headEndInterface.getCommandFactory().createUpdateCreditDaysLimitCommand(endDevice, deviceCommandInfo.getCreditDaysLimitFirst(),
                        deviceCommandInfo.getCreditDaysLimitScnd());
                break;
            case UPDATE_CREDIT_AMOUNT:
                deviceCommand = headEndInterface.getCommandFactory().createUpdateCreditAmountCommand(endDevice, deviceCommandInfo.getCreditType(),
                        deviceCommandInfo.getCreditAmount());
                break;
            default:
                throw CommandException.unsupportedEndDeviceControlType(thesaurus, deviceCommandInfo.getEndDeviceControlTypeMapping().getEndDeviceControlTypeMRID());
        }

        headEndInterface.sendCommand(deviceCommand,
                releaseDate.isBefore(clock.instant()) ? clock.instant() : releaseDate,
                serviceCall);
    }

    public void scheduleDeviceCommandsComTask(Device device, List<DeviceMessage> deviceMessages) {
        HeadEndInterface headEndInterface = getHeadEndInterface(device.getMeter());
        ((MultiSenseHeadEndInterface) headEndInterface).scheduleRequiredComTasks(device, deviceMessages);
    }

    private HeadEndInterface getHeadEndInterface(EndDevice endDevice) {
        return endDevice.getHeadEndInterface().orElseThrow(CommandException.noHeadEndInterface(thesaurus, endDevice.getMRID()));
    }

    private Instant checkAndGetContactorActivationDateAttribute(String commandCode, List<EndDeviceControlAttribute> attributes) {
        Instant activationDate = null;
        List<EndDeviceControlAttribute> copyAttributes = new ArrayList<>(attributes);
        Optional<EndDeviceControlAttribute> activationDateAttr = findAndGetAttribute(copyAttributes, contactorActivationDateAttributeName);
        if (activationDateAttr.isPresent()) {
            copyAttributes.remove(activationDateAttr.get());
            try {
                activationDate = XsdDateTimeConverter.unmarshal(activationDateAttr.get().getValue());
            } catch (Exception ex) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
        }

        if (!copyAttributes.isEmpty()) {
            throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
        }

        return activationDate;
    }

    private void checkAndSetUpdateCreditAmountAttributes(DeviceCommandInfo deviceCommandInfo, List<EndDeviceControlAttribute> attributes, String commandCode) {
        List<EndDeviceControlAttribute> copyAttributes = new ArrayList<>(attributes);
        try {
            deviceCommandInfo.setCreditType(extractMandatoryAttribute(copyAttributes, commandCode, creditTypeAttributeName));
            deviceCommandInfo.setCreditAmount(new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, creditAmount)));
        } catch (Exception ex) {
            throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
        }

        if (!copyAttributes.isEmpty()) {
            throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
        }
    }

    private void checkAndSetUpdateCreditDaysLimitAttributes(DeviceCommandInfo deviceCommandInfo, List<EndDeviceControlAttribute> attributes, String commandCode) {
        List<EndDeviceControlAttribute> copyAttributes = new ArrayList<>(attributes);
        try {
            deviceCommandInfo.setCreditDaysLimitFirst(new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, creditDaysLimitFirst)));
            deviceCommandInfo.setCreditDaysLimitScnd(new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, creditDaysLimitScnd)));
        } catch (Exception ex) {
            throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
        }

        if (!copyAttributes.isEmpty()) {
            throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
        }
    }

    private Optional<EndDeviceControlAttribute> findAndGetAttribute(List<EndDeviceControlAttribute> attributes, String attributeName) {
        return attributes.stream().filter(attr -> attr.getName().equals(attributeName))
                .findFirst();
    }

    private String extractMandatoryAttribute(List<EndDeviceControlAttribute> attributes, String commandCode, String attributeName) {
        Optional<EndDeviceControlAttribute> attr = findAndGetAttribute(attributes, attributeName);
        if (attr.isPresent()) {
            attributes.remove(attr.get());
            return attr.get().getValue();
        } else {
            throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
        }
    }

}
