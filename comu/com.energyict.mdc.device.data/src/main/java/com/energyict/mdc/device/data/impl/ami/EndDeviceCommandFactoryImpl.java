/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ami.ChangeTaxRatesInfo;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.FriendlyDayPeriodInfo;
import com.elster.jupiter.metering.ami.StepTariffInfo;
import com.elster.jupiter.metering.ami.UnsupportedCommandException;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CertificateType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.EndDeviceCommandFactory;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.commands.KeyRenewalCommand;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import com.energyict.cbo.Unit;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.device.data.impl.ami.EndDeviceCommandFactory",
        service = {EndDeviceCommandFactory.class},
        property = "name=EndDeviceCommandFactory", immediate = true)
public class EndDeviceCommandFactoryImpl implements EndDeviceCommandFactory {

    private static final String UNDEFINED_UNIT = "undefined";

    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;

    //For OSGI purposes
    public EndDeviceCommandFactoryImpl() {
    }

    public EndDeviceCommandFactoryImpl(MeteringService meteringService, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.meteringService = meteringService;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Activate
    public void activate() {
    }

    @Override
    public EndDeviceCommand createCommand(EndDevice endDevice, EndDeviceControlType endDeviceControlType) {
        EndDeviceControlTypeMapping endDeviceControlTypeMapping = EndDeviceControlTypeMapping.getMappingFor(endDeviceControlType);
        if (!multiSenseDeviceHasSupportForEndDeviceControlType(endDevice, endDeviceControlTypeMapping)) {
            throw new UnsupportedCommandException(thesaurus, endDeviceControlType.getName(), endDevice.getName());
        }
        return endDeviceControlTypeMapping.getNewEndDeviceCommand(
                endDevice,
                endDeviceControlType,
                endDeviceControlTypeMapping.getPossibleDeviceMessageIds(),
                deviceService,
                deviceMessageSpecificationService,
                thesaurus).orElseThrow(() -> new UnsupportedCommandException(thesaurus, endDeviceControlType.getName(), endDevice.getName()));
    }

    /**
     * Finds out if the multisense device supports execution of the necessary device commands, who are
     * associated with the given endDeviceControlTypeMapping.
     *
     * @param endDevice
     * @param endDeviceControlTypeMapping
     * @return true in case the device supports the given EndDeviceControlType, false otherwise
     */
    private boolean multiSenseDeviceHasSupportForEndDeviceControlType(EndDevice endDevice, EndDeviceControlTypeMapping endDeviceControlTypeMapping) {
        List<DeviceMessageId> supportedMessages = findDeviceForEndDevice(endDevice).getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                        .filter(id -> DeviceMessageId.find(id).isPresent())
                        .map(DeviceMessageId::from)
                        .collect(Collectors.toList())).orElse(Collections.emptyList());

        return endDeviceControlTypeMapping.getPossibleDeviceMessageIdGroups().stream().anyMatch(supportedMessages::containsAll);
    }

    @Override
    public EndDeviceCommand createArmCommand(EndDevice endDevice, boolean armForOpen, Instant activationDate) {
        EndDeviceControlType endDeviceControlType = armForOpen
                ? findEndDeviceControlType(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_OPEN)
                : findEndDeviceControlType(EndDeviceControlTypeMapping.ARM_REMOTE_SWITCH_FOR_CLOSURE);
        EndDeviceCommand command = this.createCommand(endDevice, endDeviceControlType);
        if (activationDate != null) {
            PropertySpec activationDatePropertySpec = getActivationDatePropertySpec(command);
            command.setPropertyValue(activationDatePropertySpec, Date.from(activationDate));
        }
        return command;
    }

    private PropertySpec getActivationDatePropertySpec(EndDeviceCommand command) {
        return command.getCommandArgumentSpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(Date.class))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_SHOULD_HAVE_AN_ACTIVATION_DATE_ATTRIBUTE).format(command.getEndDeviceControlType().getName())));
    }

    @Override
    public EndDeviceCommand createConnectCommand(EndDevice endDevice, Instant activationDate) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.CLOSE_REMOTE_SWITCH));
        if (activationDate != null) {
            PropertySpec activationDatePropertySpec = getActivationDatePropertySpec(command);
            command.setPropertyValue(activationDatePropertySpec, Date.from(activationDate));
        }
        return command;
    }

    @Override
    public EndDeviceCommand createDisconnectCommand(EndDevice endDevice, Instant activationDate) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.OPEN_REMOTE_SWITCH));
        if (activationDate != null) {
            PropertySpec activationDatePropertySpec = getActivationDatePropertySpec(command);
            command.setPropertyValue(activationDatePropertySpec, Date.from(activationDate));
        }
        return command;
    }

    @Override
    public EndDeviceCommand createEnableLoadLimitCommand(EndDevice endDevice, Quantity quantity) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.LOAD_CONTROL_INITIATE));
        command.setPropertyValue(getNormalThresholdPropertySpec(command), quantity.getValue());
        Unit unit = Unit.get(quantity.getUnit().getSymbol(), quantity.getMultiplier());
        command.setPropertyValue(
                getUnitPropertySpec(command),
                unit.isUndefined() ? UNDEFINED_UNIT : unit.toString()
        );
        return command;
    }

    private PropertySpec getNormalThresholdPropertySpec(EndDeviceCommand command) {
        return command.getCommandArgumentSpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(BigDecimal.class))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_SHOULD_HAVE_A_NORMAL_THRESHOLD_ATTRIBUTE).format(command.getEndDeviceControlType().getName())));
    }

    private PropertySpec getUnitPropertySpec(EndDeviceCommand command) {
        return command.getCommandArgumentSpecs()
                .stream()
                .filter(propertySpec -> propertySpec.getValueFactory().getValueType().equals(String.class))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_SHOULD_HAVE_A_UNIT_ATTRIBUTE).format(command.getEndDeviceControlType().getName())));
    }

    @Override
    public EndDeviceCommand createDisableLoadLimitCommand(EndDevice endDevice) {
        return this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.LOAD_CONTROL_TERMINATE));
    }

    @Override
    public EndDeviceCommand createKeyRenewalCommand(EndDevice endDevice, List<SecurityAccessorType> securityAccessorTypes, boolean isServiceKey) {
        Device deviceForEndDevice = findDeviceForEndDevice(endDevice);
        final Set<String> securityAccessorTypesNames = securityAccessorTypes.stream().map(s -> s.getName()).collect(Collectors.toSet());
        List<SecurityAccessorTypeOnDeviceType> securityAccessorTypeOnDeviceTypes = deviceForEndDevice.getDeviceConfiguration()
                .getDeviceType()
                .getSecurityAccessors()
                .stream()
                .filter(sec -> securityAccessorTypesNames.contains(sec.getSecurityAccessorType().getName())).collect(Collectors.toList());

        if (securityAccessorTypesNames.size() != securityAccessorTypeOnDeviceTypes.size()) {
            Set<String> securityAccessorTypeOnDeviceTypesNames = securityAccessorTypeOnDeviceTypes.stream().map(s -> s.getSecurityAccessorType().getName()).collect(Collectors.toSet());
            String missingSecurityAccessorsNames = String.join(", ", securityAccessorTypesNames.stream()
                    .filter(sA -> !securityAccessorTypeOnDeviceTypesNames.contains(sA))
                    .collect(Collectors.toList()));
            throw new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_SECURITY_ACCESSORS_ON_DEVICE_TYPE_FOR_NAMES)
                    .format(missingSecurityAccessorsNames));
        }
        KeyRenewalCommand command = (KeyRenewalCommand) this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.KEY_RENEWAL));
        command.setSecurityAccessorOnDeviceTypes(securityAccessorTypeOnDeviceTypes);
        command.setServiceKey(isServiceKey);

        return command;
    }

    @Override
    public EndDeviceCommand createGenerateKeyPairCommand(EndDevice endDevice, CertificateType certificateType) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.GENERATE_KEY_PAIR));
        command.setPropertyValue(getKeyTypePropertySpec(command), certificateType.getName());
        return command;
    }

    @Override
    public EndDeviceCommand createGenerateCSRCommand(EndDevice endDevice, CertificateType certificateType) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.GENERATE_CSR));
        command.setPropertyValue(getKeyTypePropertySpec(command), certificateType.getName());
        return command;
    }

    @Override
    public EndDeviceCommand createImportCertificateCommand(EndDevice endDevice, SecurityAccessorType securityAccessorType) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.IMPORT_CERTIFICATE));
        command.setPropertyValue(getKeyAccessorTypePropertySpec(command), securityAccessorType);
        return command;
    }

    @Override
    public EndDeviceCommand createUpdateCreditAmountCommand(EndDevice endDevice, String creditType, BigDecimal creditAmount) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.UPDATE_CREDIT_AMOUNT));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.creditType), creditType);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.creditAmount), creditAmount);
        return command;
    }

    @Override
    public EndDeviceCommand createUpdateCreditDaysLimitCommand(EndDevice endDevice, BigDecimal creditDaysLimitFirst, BigDecimal creditDaysLimitScnd) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.UPDATE_CREDIT_DAYS_LIMIT));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.creditDaysLimitFirst), creditDaysLimitFirst);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.creditDaysLimitScnd), creditDaysLimitScnd);
        return command;
    }

    @Override
    public EndDeviceCommand createChangeStepTariffCommand(EndDevice endDevice, StepTariffInfo stepTariffInfo) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.CHANGE_STEP_TARIFF));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.tariffCode), stepTariffInfo.tariffCode);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxesType), stepTariffInfo.additionalTaxesType);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceRecalculationType), stepTariffInfo.graceRecalculationType);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceRecalculationValue), stepTariffInfo.graceRecalculationValue);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep1), stepTariffInfo.chargeStep1);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep1), stepTariffInfo.priceStep1);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep1), stepTariffInfo.recalculationTypeStep1);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep1), stepTariffInfo.graceWarningStep1);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep1), stepTariffInfo.additionalTaxStep1);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep2), stepTariffInfo.chargeStep2);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep2), stepTariffInfo.priceStep2);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep2), stepTariffInfo.recalculationTypeStep2);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep2), stepTariffInfo.graceWarningStep2);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep2), stepTariffInfo.additionalTaxStep2);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep3), stepTariffInfo.chargeStep3);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep3), stepTariffInfo.priceStep3);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep3), stepTariffInfo.recalculationTypeStep3);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep3), stepTariffInfo.graceWarningStep3);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep3), stepTariffInfo.additionalTaxStep3);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep4), stepTariffInfo.chargeStep4);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep4), stepTariffInfo.priceStep4);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep4), stepTariffInfo.recalculationTypeStep4);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep4), stepTariffInfo.graceWarningStep4);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep4), stepTariffInfo.additionalTaxStep4);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep5), stepTariffInfo.chargeStep5);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep5), stepTariffInfo.priceStep5);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep5), stepTariffInfo.recalculationTypeStep5);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep5), stepTariffInfo.graceWarningStep5);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep5), stepTariffInfo.additionalTaxStep5);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep6), stepTariffInfo.chargeStep6);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep6), stepTariffInfo.priceStep6);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep6), stepTariffInfo.recalculationTypeStep6);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep6), stepTariffInfo.graceWarningStep6);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep6), stepTariffInfo.additionalTaxStep6);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep7), stepTariffInfo.chargeStep7);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep7), stepTariffInfo.priceStep7);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep7), stepTariffInfo.recalculationTypeStep7);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep7), stepTariffInfo.graceWarningStep7);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep7), stepTariffInfo.additionalTaxStep7);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep8), stepTariffInfo.chargeStep8);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep8), stepTariffInfo.priceStep8);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep8), stepTariffInfo.recalculationTypeStep8);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep8), stepTariffInfo.graceWarningStep8);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep8), stepTariffInfo.additionalTaxStep8);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep9), stepTariffInfo.chargeStep9);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep9), stepTariffInfo.priceStep9);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep9), stepTariffInfo.recalculationTypeStep9);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep9), stepTariffInfo.graceWarningStep9);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep9), stepTariffInfo.additionalTaxStep9);

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeStep10), stepTariffInfo.chargeStep10);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.priceStep10), stepTariffInfo.priceStep10);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.recalculationTypeStep10), stepTariffInfo.recalculationTypeStep10);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.graceWarningStep10), stepTariffInfo.graceWarningStep10);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.additionalTaxStep10), stepTariffInfo.additionalTaxStep10);

        return command;
    }

    @Override
    public EndDeviceCommand createChangeTaxRatesCommand(EndDevice endDevice, ChangeTaxRatesInfo taxRatesInfo) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.CHANGE_TAX_RATES));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.monthlyTax), taxRatesInfo.monthlyTax);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.zeroConsumptionTax), taxRatesInfo.zeroConsumptionTax);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.consumptionTax), taxRatesInfo.consumptionTax);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.consumptionAmount), taxRatesInfo.consumptionAmount);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.consumptionLimit), taxRatesInfo.consumptionLimit);
        return command;
    }

    @Override
    public EndDeviceCommand createSwitchTaxAndStepTariffCommand(EndDevice endDevice, String tariffType, Instant activationDate) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.SWITCH_TAX_AND_STEP_TARIFF));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.tariffType), tariffType);
        if (activationDate != null) {
            PropertySpec activationDatePropertySpec = getActivationDatePropertySpec(command);
            command.setPropertyValue(activationDatePropertySpec, Date.from(activationDate));
        }
        return command;
    }

    @Override
    public EndDeviceCommand createSwitchChargeModeCommand(EndDevice endDevice, String chargeMode, Instant activationDate) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.SWITCH_CHARGE_MODE));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.chargeMode), chargeMode);
        if (activationDate != null) {
            PropertySpec activationDatePropertySpec = getActivationDatePropertySpec(command);
            command.setPropertyValue(activationDatePropertySpec, Date.from(activationDate));
        }
        return command;
    }
    @Override
    public EndDeviceCommand createUpdateFriendlyDayPeriodCommand(EndDevice endDevice, FriendlyDayPeriodInfo friendlyDayPeriodInfo) throws UnsupportedCommandException {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.FRIENDLY_DAY_PERIOD_UPDATE));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlyHourStart), friendlyDayPeriodInfo.friendlyHourStart);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlyMinuteStart), friendlyDayPeriodInfo.friendlyMinuteStart);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlySecondStart), friendlyDayPeriodInfo.friendlySecondStart);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlyHundredthsStart), friendlyDayPeriodInfo.friendlySecondHundredthsStart);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlyHourStop), friendlyDayPeriodInfo.friendlyHourStop);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlyMinuteStop), friendlyDayPeriodInfo.friendlyMinuteStop);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlySecondStop), friendlyDayPeriodInfo.friendlySecondStop);
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlyHundredthsStop), friendlyDayPeriodInfo.friendlySecondHundredthsStop);
        return command;
    }

    @Override
    public EndDeviceCommand createUpdateFriendlyWeekdaysCommand(EndDevice endDevice, String friendlyWeekdays) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.FRIENDLY_WEEKDAYS_UPDATE));
        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.friendlyWeekdays), friendlyWeekdays);
        return command;
    }

    @Override
    public EndDeviceCommand createSendSpecialDayCalendarCommand(EndDevice endDevice, String specialDaysCalendarName) {
        EndDeviceCommand command = this.createCommand(endDevice, findEndDeviceControlType(EndDeviceControlTypeMapping.SPECIAL_DAY_CALENDAR_SEND));
        Device device = findDeviceForEndDevice(endDevice);
        AllowedCalendar calendar = device.getDeviceType().getAllowedCalendars().stream()
                .filter(allowedCalendar -> allowedCalendar.getCalendar().isPresent())
                .filter(allowedCalendar -> !allowedCalendar.isGhost() && allowedCalendar.getCalendar().get().getName().equals(specialDaysCalendarName))
                .findFirst().orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.CAN_NOT_FIND_FOR_ALLOWED_CALENDAR).format(specialDaysCalendarName)));

        command.setPropertyValue(getCommandArgumentSpec(command, DeviceMessageConstants.specialDaysAttributeName), calendar.getCalendar().get());

        return command;
    }


    private PropertySpec getKeyTypePropertySpec(EndDeviceCommand command) {
        return command.getCommandArgumentSpecs()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_SHOULD_HAVE_A_KEY_TYPE_ATTRIBUTE)
                        .format(command.getEndDeviceControlType().getName())));
    }

    private PropertySpec getKeyAccessorTypePropertySpec(EndDeviceCommand command) {
        return command.getCommandArgumentSpecs()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_SHOULD_HAVE_A_KEY_ACCESSOR_TYPE_REFERENCE_ATTRIBUTE)
                        .format(command.getEndDeviceControlType().getName())));
    }

    private PropertySpec getCommandArgumentSpec(EndDeviceCommand endDeviceCommand, String commandArgumentName) {
        return endDeviceCommand.getCommandArgumentSpecs().stream()
                .filter(propertySpec -> propertySpec.getName().equals(commandArgumentName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.COMMAND_ARGUMENT_SPEC_NOT_FOUND)
                        .format(commandArgumentName, endDeviceCommand.getEndDeviceControlType().getName())));
    }


    private EndDeviceControlType findEndDeviceControlType(EndDeviceControlTypeMapping controlTypeMapping) {
        String mrid = controlTypeMapping.getEndDeviceControlTypeMRID();
        return meteringService.getEndDeviceControlType(mrid).orElseThrow(NoSuchElementException.endDeviceControlTypeWithMRIDNotFound(thesaurus, mrid));
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        long deviceId = Long.parseLong(endDevice.getAmrId());
        return deviceService.findDeviceById(deviceId).orElseThrow(NoSuchElementException.deviceWithIdNotFound(thesaurus, deviceId));
    }
}
