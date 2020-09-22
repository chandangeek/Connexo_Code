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
import com.elster.jupiter.metering.ami.StepTariffInfo;

import ch.iec.tc57._2011.enddevicecontrols.EndDeviceControlAttribute;
import com.energyict.protocolimplv2.messages.ChargeDeviceMessage;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep10;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep3;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep4;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep5;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep6;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep7;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep8;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.additionalTaxStep9;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep10;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep3;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep4;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep5;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep6;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep7;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep8;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.chargeStep9;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditAmount;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditDaysLimitFirst;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditDaysLimitScnd;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.creditTypeAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep10;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep3;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep4;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep5;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep6;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep7;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep8;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.graceWarningStep9;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep10;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep3;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep4;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep5;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep6;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep7;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep8;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.priceStep9;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep1;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep10;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep2;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep3;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep4;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep5;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep6;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep7;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep8;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.recalculationTypeStep9;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.tariffCode;

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
            case CHANGE_STEP_TARIFF:
                checkAndSetChangeStepTariffAttributes(deviceCommandInfo, attributes, commandCode);
                break;
            default:
                throw CommandException.unsupportedEndDeviceControlType(thesaurus, commandCode);
        }

        deviceCommandInfo.setEndDeviceControlTypeMapping(endDeviceControlTypeMapping);

        return deviceCommandInfo;
    }

    public void performOperations(EndDevice endDevice, ServiceCall serviceCall, DeviceCommandInfo deviceCommandInfo, Instant releaseDate, boolean withPriority) {
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
            case CHANGE_STEP_TARIFF:
                deviceCommand = headEndInterface.getCommandFactory().createChangeStepTariffCommand(endDevice, deviceCommandInfo.getStepTariffInfo());
                break;
            default:
                throw CommandException.unsupportedEndDeviceControlType(thesaurus, deviceCommandInfo.getEndDeviceControlTypeMapping().getEndDeviceControlTypeMRID());
        }

        headEndInterface.sendCommand(deviceCommand,
                releaseDate.isBefore(clock.instant()) ? clock.instant() : releaseDate,
                serviceCall, withPriority);
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

    private void checkAndSetChangeStepTariffAttributes(DeviceCommandInfo deviceCommandInfo, List<EndDeviceControlAttribute> attributes, String commandCode) {
        List<EndDeviceControlAttribute> copyAttributes = new ArrayList<>(attributes);
        try {
            StepTariffInfo stepTariffInfo = new StepTariffInfo();
            List<String> exhaustiveValuesForRecalculationTypeStep = Arrays.stream(ChargeDeviceMessage.RecalculationType.getDescriptionValues())
                    .collect(Collectors.toList());
            stepTariffInfo.tariffCode = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, tariffCode));

            stepTariffInfo.chargeStep1 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep1));
            stepTariffInfo.priceStep1 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep1));
            stepTariffInfo.recalculationTypeStep1 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep1);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep1)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep1 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep1));
            stepTariffInfo.additionalTaxStep1 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep1));

            stepTariffInfo.chargeStep2 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep2));
            stepTariffInfo.priceStep2 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep2));
            stepTariffInfo.recalculationTypeStep2 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep2);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep2)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep2 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep2));
            stepTariffInfo.additionalTaxStep2 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep2));

            stepTariffInfo.chargeStep3 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep3));
            stepTariffInfo.priceStep3 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep3));
            stepTariffInfo.recalculationTypeStep3 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep3);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep3)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep3 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep3));
            stepTariffInfo.additionalTaxStep3 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep3));

            stepTariffInfo.chargeStep4 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep4));
            stepTariffInfo.priceStep4 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep4));
            stepTariffInfo.recalculationTypeStep4 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep4);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep4)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep4 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep4));
            stepTariffInfo.additionalTaxStep4 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep4));

            stepTariffInfo.chargeStep5 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep5));
            stepTariffInfo.priceStep5 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep5));
            stepTariffInfo.recalculationTypeStep5 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep5);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep5)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep5 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep5));
            stepTariffInfo.additionalTaxStep5 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep5));

            stepTariffInfo.chargeStep6 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep6));
            stepTariffInfo.priceStep6 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep6));
            stepTariffInfo.recalculationTypeStep6 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep6);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep6)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep6 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep6));
            stepTariffInfo.additionalTaxStep6 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep6));

            stepTariffInfo.chargeStep7 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep7));
            stepTariffInfo.priceStep7 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep7));
            stepTariffInfo.recalculationTypeStep7 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep7);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep7)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep7 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep7));
            stepTariffInfo.additionalTaxStep7 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep7));

            stepTariffInfo.chargeStep8 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep8));
            stepTariffInfo.priceStep8 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep8));
            stepTariffInfo.recalculationTypeStep8 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep8);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep8)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep8 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep8));
            stepTariffInfo.additionalTaxStep8 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep8));

            stepTariffInfo.chargeStep9 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep9));
            stepTariffInfo.priceStep9 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep9));
            stepTariffInfo.recalculationTypeStep9 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep9);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep9)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep9 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep9));
            stepTariffInfo.additionalTaxStep9 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep9));

            stepTariffInfo.chargeStep10 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, chargeStep10));
            stepTariffInfo.priceStep10 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, priceStep10));
            stepTariffInfo.recalculationTypeStep10 = extractMandatoryAttribute(copyAttributes, commandCode, recalculationTypeStep10);
            if (!exhaustiveValuesForRecalculationTypeStep.contains(stepTariffInfo.recalculationTypeStep10)) {
                throw CommandException.inappropriateCommandAttributes(thesaurus, commandCode);
            }
            stepTariffInfo.graceWarningStep10 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, graceWarningStep10));
            stepTariffInfo.additionalTaxStep10 = new BigDecimal(extractMandatoryAttribute(copyAttributes, commandCode, additionalTaxStep10));

            deviceCommandInfo.setStepTariffInfo(stepTariffInfo);
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
