/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.meterreplacement;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.TimeUtils;
import com.energyict.mdc.common.device.data.CIMLifecycleDates;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.CIMPattern;
import com.energyict.mdc.sap.soap.webservices.impl.DeviceSharedCommunicationScheduleRemover;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.SAPWebServiceException;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Component(name = MeterRegisterChangeRequestServiceCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + MeterRegisterChangeRequestServiceCallHandler.NAME, immediate = true)
public class MeterRegisterChangeRequestServiceCallHandler implements ServiceCallHandler {

    public static final String NAME = "MeterRegisterChangeRequest";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile Thesaurus thesaurus;
    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile WebServiceActivator webServiceActivator;
    private volatile Clock clock;
    private volatile DeviceSharedCommunicationScheduleRemover deviceSharedCommunicationScheduleRemover;

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINE, "Now entering state " + newState.getDefaultFormat());
        switch (newState) {
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            case ONGOING:
                processServiceCall(serviceCall);
                break;
            case CANCELLED:
                cancelServiceCall(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(WebServiceActivator.COMPONENT_NAME, Layer.SOAP);
    }

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setDeviceSharedCommunicationScheduleRemover(DeviceSharedCommunicationScheduleRemover deviceSharedCommunicationScheduleRemover) {
        this.deviceSharedCommunicationScheduleRemover = deviceSharedCommunicationScheduleRemover;
    }

    private void processServiceCall(ServiceCall serviceCall) {
        ServiceCall subParent = serviceCall.getParent().orElseThrow(() -> new IllegalStateException("Can not find parent for service call"));
        SubMasterMeterRegisterChangeRequestDomainExtension subParentExtension = subParent.getExtensionFor(new SubMasterMeterRegisterChangeRequestCustomPropertySet())
                .orElseThrow(() -> new IllegalStateException("Can not find domain extension for parent service call"));
        MeterRegisterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet())
                .orElseThrow(() -> new IllegalStateException("Can not find domain extension for service call"));

        Optional<Device> device = sapCustomPropertySets.getDevice(subParentExtension.getDeviceId());
        if (device.isPresent()) {
            try {
                CIMLifecycleDates lifecycleDates = device.get().getLifecycleDates();
                Instant shipmentDate = lifecycleDates.getReceivedDate().orElse(device.get().getCreateTime());
                if (extension.getStartDate().isBefore(shipmentDate)) {
                    failServiceCall(extension, MessageSeeds.START_DATE_IS_BEFORE_SHIPMENT_DATE);
                    return;
                }

                if (!subParentExtension.isCreateRequest()) {
                    sapCustomPropertySets.truncateCpsInterval(device.get(), extension.getLrn(), TimeUtils.convertFromTimeZone(extension.getEndDate(), extension.getTimeZone()));
                    serviceCall.requestTransition(DefaultState.SUCCESSFUL);
                    long deviceId = device.get().getId();
                    if (sapCustomPropertySets.areAllProfileIdsClosedBeforeDate(deviceId, clock.instant())) {
                        serviceCall.log(LogLevel.INFO, "All profiles are closed, removing shared com schedules from device " + device.get().getName());
                        deviceSharedCommunicationScheduleRemover.removeComSchedules(deviceId);
                    }
                } else {
                    processDeviceRegisterCreation(extension, device.get());
                }
            } catch (SAPWebServiceException sapEx) {
                failServiceCallWithException(extension, sapEx);
            } catch (Exception e) {
                failServiceCallWithException(extension, new SAPWebServiceException(thesaurus, MessageSeeds.ERROR_PROCESSING_METER_REPLACEMENT_REQUEST, e.getLocalizedMessage()));
            }
        } else {
            failServiceCall(extension, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, subParentExtension.getDeviceId());
        }
    }

    private void cancelServiceCall(ServiceCall serviceCall) {
        MeterRegisterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()).get();
        extension.setError(MessageSeeds.REGISTER_LRN_SERVICE_CALL_WAS_CANCELLED, extension.getLrn());
        serviceCall.update(extension);
    }

    private void failServiceCall(MeterRegisterChangeRequestDomainExtension extension, MessageSeed messageSeed, Object... args) {
        ServiceCall serviceCall = extension.getServiceCall();

        extension.setError(messageSeed, args);
        serviceCall.update(extension);
        serviceCall.requestTransition(DefaultState.FAILED);
    }

    private void failServiceCallWithException(MeterRegisterChangeRequestDomainExtension extension, SAPWebServiceException e) {
        ServiceCall serviceCall = extension.getServiceCall();

        extension.setErrorCode(e.getErrorCode());
        extension.setErrorMessage(e.getLocalizedMessage());
        serviceCall.update(extension);
        serviceCall.requestTransition(DefaultState.FAILED);
    }

    private void processDeviceRegisterCreation(MeterRegisterChangeRequestDomainExtension extension, Device device) {
        String recurrence = extension.getRecurrenceCode();
        String obis = extension.getObis();
        String divisionCategory = extension.getDivisionCategory();
        CIMPattern cimPattern = null;

        if (recurrence == null) {
            recurrence = "0";
        }

        if (obis == null) {
            if (divisionCategory == null) {
                failServiceCall(extension, MessageSeeds.NO_OBIS_OR_READING_TYPE_KIND);
                return;
            }
            if (WebServiceActivator.getExternalSystemName().equals(WebServiceActivator.EXTERNAL_SYSTEM_EDA)) {
                failServiceCall(extension, MessageSeeds.NO_OBIS);
                return;
            }
        }

        Pair<MacroPeriod, TimeAttribute> period = webServiceActivator.getRecurrenceCodeMap().get(recurrence);
        if (period == null) {
            failServiceCall(extension, MessageSeeds.NO_UTILITIES_MEASUREMENT_RECURRENCE_CODE_MAPPING, recurrence,
                    WebServiceActivator.REGISTER_RECURRENCE_CODE);
            return;
        }

        if (divisionCategory != null && !WebServiceActivator.getExternalSystemName().equals(WebServiceActivator.EXTERNAL_SYSTEM_EDA)) {
            cimPattern = webServiceActivator.getDivisionCategoryCodeMap().get(divisionCategory);
            if (cimPattern == null) {
                failServiceCall(extension, MessageSeeds.NO_UTILITIES_DIVISION_CATEGORY_CODE_MAPPING, divisionCategory,
                        WebServiceActivator.REGISTER_DIVISION_CATEGORY_CODE);
                return;
            }
        }

        if (period.getFirst() == MacroPeriod.NOTAPPLICABLE && period.getLast() == TimeAttribute.NOTAPPLICABLE) {
            processRegister(device, extension.getServiceCall(), obis, period, cimPattern);
        } else {
            processChannel(device, extension.getServiceCall(), obis, period, cimPattern);
        }
    }

    private void processChannel(Device device, ServiceCall serviceCall, String obis,
                                Pair<MacroPeriod, TimeAttribute> period, CIMPattern cimPattern) {
        MeterRegisterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()).get();
        Set<Channel> channels = findChannelByObis(device, obis, period);

        if (cimPattern != null) {
            channels.addAll(findChannelByReadingType(device, period, cimPattern));
        }
        if (!channels.isEmpty()) {
            if (channels.size() == 1) {
                sapCustomPropertySets.setLrn(channels.stream().findFirst().get(), extension.getLrn(),
                        TimeUtils.convertFromTimeZone(extension.getStartDate(), extension.getTimeZone()),
                        TimeUtils.convertFromTimeZone(extension.getEndDate(), extension.getTimeZone()));
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                failServiceCallBySeveralDataSources(extension, period, cimPattern, obis);
            }
        } else {
            failServiceCallByNoFound(extension, period, cimPattern, obis);
        }
    }

    private void processRegister(Device device, ServiceCall serviceCall, String obis,
                                 Pair<MacroPeriod, TimeAttribute> period, CIMPattern cimPattern) {
        MeterRegisterChangeRequestDomainExtension extension = serviceCall.getExtensionFor(new MeterRegisterChangeRequestCustomPropertySet()).get();
        Set<Register> registers = new HashSet<>();
        Optional<Register> register = device.getRegisterWithDeviceObisCode(ObisCode.fromString(obis));
        if (register.isPresent() && sapCustomPropertySets.doesRegisterHaveSapCPS(register.get())) {
            registers.add(register.get());
        }

        if (cimPattern != null) {
            registers.addAll(findRegisterByReadingType(device, period, cimPattern));
        }

        if (!registers.isEmpty()) {
            if (registers.size() == 1) {
                sapCustomPropertySets.setLrn(registers.stream().findFirst().get(), extension.getLrn(),
                        TimeUtils.convertFromTimeZone(extension.getStartDate(), extension.getTimeZone()),
                        TimeUtils.convertFromTimeZone(extension.getEndDate(), extension.getTimeZone()));
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                failServiceCallBySeveralDataSources(extension, period, cimPattern, obis);
            }
        } else {
            failServiceCallByNoFound(extension, period, cimPattern, obis);
        }
    }

    private void failServiceCallBySeveralDataSources(MeterRegisterChangeRequestDomainExtension extension,
                                                     Pair<MacroPeriod, TimeAttribute> period, CIMPattern cimPattern, String obis) {
        String strPeriod = convertPeriodToString(period);

        if (obis == null) {
            failServiceCall(extension, MessageSeeds.SEVERAL_DATA_SOURCES_WITH_KIND, strPeriod, period.getFirst().getId(), period.getLast().getId(),
                    cimPattern.code());
        } else if (cimPattern == null) {
            failServiceCall(extension, MessageSeeds.SEVERAL_DATA_SOURCES_WITH_OBIS, strPeriod, period.getFirst().getId(), period.getLast().getId(), obis);
        } else {
            failServiceCall(extension, MessageSeeds.SEVERAL_DATA_SOURCES_WITH_OBIS_OR_KIND, strPeriod, period.getFirst().getId(), period.getLast().getId(),
                    cimPattern.code(), obis);
        }
    }

    private void failServiceCallByNoFound(MeterRegisterChangeRequestDomainExtension extension,
                                          Pair<MacroPeriod, TimeAttribute> period, CIMPattern cimPattern, String obis) {
        String strPeriod = convertPeriodToString(period);

        if (obis == null) {
            failServiceCall(extension, MessageSeeds.NO_DATA_SOURCES_WITH_KIND, strPeriod, period.getFirst().getId(), period.getLast().getId(),
                    cimPattern.code());
        } else if (cimPattern == null) {
            failServiceCall(extension, MessageSeeds.NO_DATA_SOURCES_WITH_OBIS, strPeriod, period.getFirst().getId(), period.getLast().getId(), obis);
        } else {
            failServiceCall(extension, MessageSeeds.NO_DATA_SOURCES_WITH_OBIS_OR_KIND, strPeriod, period.getFirst().getId(), period.getLast().getId(),
                    cimPattern.code(), obis);
        }
    }

    private String convertPeriodToString(Pair<MacroPeriod, TimeAttribute> period) {
        String strPeriod;
        if (period.getFirst() != MacroPeriod.NOTAPPLICABLE) {
            strPeriod = period.getFirst().getDescription();
        } else if (period.getLast() != TimeAttribute.NOTAPPLICABLE) {
            strPeriod = period.getLast().getDescription();
        } else {
            strPeriod = "Empty";
        }
        return strPeriod;
    }

    private Set<Channel> findChannelByObis(Device device, String obis, Pair<MacroPeriod, TimeAttribute> period) {
        return device.getChannels().stream().filter(c -> c.getObisCode().toString().equals(obis))
                .filter(c -> c.getReadingType().getMacroPeriod() == period.getFirst()
                        && c.getReadingType().getMeasuringPeriod() == period.getLast())
                .filter(c -> sapCustomPropertySets.doesChannelHaveSapCPS(c))
                .collect(Collectors.toSet());
    }

    //MacroPeriod.*.TimeAttribute.*.*.*.measurementKind.*.*.*.*.*.*.*.*.*.*.*
    private Set<Channel> findChannelByReadingType(Device device, Pair<MacroPeriod, TimeAttribute> period, CIMPattern cimPattern) {
        return device.getChannels()
                .stream()
                .filter(channel -> channel.getReadingType().getMacroPeriod() == period.getFirst()
                        && channel.getReadingType().getMeasuringPeriod() == period.getLast()
                        && cimPattern.matches(channel.getReadingType()))
                .filter(channel -> sapCustomPropertySets.doesChannelHaveSapCPS(channel))
                .collect(Collectors.toSet());
    }

    //MacroPeriod.*.TimeAttribute.*.*.*.measurementKind.*.*.*.*.*.*.*.*.*.*.*
    private Set<Register> findRegisterByReadingType(Device device, Pair<MacroPeriod, TimeAttribute> period, CIMPattern cimPattern) {
        return device.getRegisters()
                .stream()
                .filter(register -> register.getReadingType().getMacroPeriod() == period.getFirst()
                        && register.getReadingType().getMeasuringPeriod() == period.getLast()
                        && cimPattern.matches(register.getReadingType()))
                .filter(register -> sapCustomPropertySets.doesRegisterHaveSapCPS(register))
                .collect(Collectors.toSet());
    }

}
