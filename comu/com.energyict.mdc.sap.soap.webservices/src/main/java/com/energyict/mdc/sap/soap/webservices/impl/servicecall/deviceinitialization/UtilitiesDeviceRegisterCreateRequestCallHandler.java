/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl.servicecall.deviceinitialization;

import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;

import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.sap.soap.webservices.SAPCustomPropertySets;
import com.energyict.mdc.sap.soap.webservices.impl.CIMPattern;
import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import com.energyict.obis.ObisCode;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component(name = UtilitiesDeviceRegisterCreateRequestCallHandler.NAME, service = ServiceCallHandler.class,
        property = "name=" + UtilitiesDeviceRegisterCreateRequestCallHandler.NAME, immediate = true)
public class UtilitiesDeviceRegisterCreateRequestCallHandler implements ServiceCallHandler {

    public static final String NAME = "UtilitiesDeviceRegisterCreateRequestCallHandler";
    public static final String VERSION = "v1.0";
    public static final String APPLICATION = "MDC";

    private volatile SAPCustomPropertySets sapCustomPropertySets;
    private volatile WebServiceActivator webServiceActivator;

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

    private void processServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();

        Optional<Device> device = sapCustomPropertySets.getDevice(extension.getDeviceId());
        if (device.isPresent()) {
            String recurrence = extension.getRecurrenceCode();
            String obis = extension.getObis();
            String divisionCategory = extension.getDivisionCategory();
            CIMPattern cimPattern = null;

            if (recurrence == null) {
                recurrence = "0";
            }

            if (obis == null && divisionCategory == null) {
                failServiceCall(extension, MessageSeeds.NO_OBIS_OR_READING_TYPE_KIND);
                return;
            }

            Pair<MacroPeriod, TimeAttribute> period = webServiceActivator.getRecurrenceCodeMap().get(recurrence);
            if (period == null) {
                failServiceCall(extension, MessageSeeds.NO_UTILITIES_MEASUREMENT_RECURRENCE_CODE_MAPPING, recurrence,
                        WebServiceActivator.REGISTER_RECURRENCE_CODE);
                return;
            }

            if (divisionCategory != null) {
                cimPattern = webServiceActivator.getDivisionCategoryCodeMap().get(divisionCategory);
                if (cimPattern == null) {
                    failServiceCall(extension, MessageSeeds.NO_UTILITIES_DIVISION_CATEGORY_CODE_MAPPING, divisionCategory,
                            WebServiceActivator.REGISTER_DIVISION_CATEGORY_CODE);
                    return;
                }
            }

            if (period.getFirst() == MacroPeriod.NOTAPPLICABLE && period.getLast() == TimeAttribute.NOTAPPLICABLE) {
                processRegister(device.get(), serviceCall, obis, period, cimPattern);
            } else {
                processChannel(device.get(), serviceCall, obis, period, cimPattern);
            }
        } else {
            failServiceCall(extension, MessageSeeds.NO_DEVICE_FOUND_BY_SAP_ID, extension.getDeviceId());
        }

    }

    private void processChannel(Device device, ServiceCall serviceCall, String obis,
                                Pair<MacroPeriod, TimeAttribute> period, CIMPattern cimPattern) {
        UtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        Set<Channel> channels = findChannelByObis(device, obis, period);
        channels.addAll(findChannelByReadingType(device, period, cimPattern));
        if (!channels.isEmpty()) {
            if (channels.size() == 1) {
                try {
                    sapCustomPropertySets.setLrn(channels.stream().findFirst().get(), extension.getLrn(),
                            WebServiceActivator.getZonedDate(extension.getStartDate(), extension.getTimeZone()),
                            WebServiceActivator.getZonedDate(extension.getEndDate(), extension.getTimeZone()));
                } catch (LocalizedException ex) {
                    failServiceCall(extension, ex.getMessageSeed(), ex.getMessageArgs());
                    return;
                }
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
        UtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        Set<Register> registers = new HashSet<>();
        Optional<Register> register = device.getRegisterWithDeviceObisCode(ObisCode.fromString(obis));
        if (register.isPresent() && sapCustomPropertySets.doesRegisterHaveSapCPS(register.get())) {
            registers.add(register.get());
        }

        registers.addAll(findRegisterByReadingType(device, period, cimPattern));

        if (!registers.isEmpty()) {
            if (registers.size() == 1) {
                try {
                    sapCustomPropertySets.setLrn(registers.stream().findFirst().get(), extension.getLrn(),
                            WebServiceActivator.getZonedDate(extension.getStartDate(), extension.getTimeZone()),
                            WebServiceActivator.getZonedDate(extension.getEndDate(), extension.getTimeZone()));
                } catch (LocalizedException ex) {
                    failServiceCall(extension, ex.getMessageSeed(), ex.getMessageArgs());
                    return;
                }
                serviceCall.requestTransition(DefaultState.SUCCESSFUL);
            } else {
                failServiceCallBySeveralDataSources(extension, period, cimPattern, obis);
            }
        } else {
            failServiceCallByNoFound(extension, period, cimPattern, obis);
        }
    }

    private void cancelServiceCall(ServiceCall serviceCall) {
        UtilitiesDeviceRegisterCreateRequestDomainExtension extension = serviceCall.getExtensionFor(new UtilitiesDeviceRegisterCreateRequestCustomPropertySet()).get();
        extension.setError(MessageSeeds.REGISTER_SERVICE_CALL_WAS_CANCELLED, extension.getObis());
        serviceCall.update(extension);
    }

    private void failServiceCallBySeveralDataSources(UtilitiesDeviceRegisterCreateRequestDomainExtension extension,
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

    private void failServiceCallByNoFound(UtilitiesDeviceRegisterCreateRequestDomainExtension extension,
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

    private void failServiceCall(UtilitiesDeviceRegisterCreateRequestDomainExtension extension, MessageSeed messageSeed, Object... args) {
        ServiceCall serviceCall = extension.getServiceCall();

        extension.setError(messageSeed, args);
        serviceCall.update(extension);
        serviceCall.requestTransition(DefaultState.FAILED);
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

    @Reference
    public void setSAPCustomPropertySets(SAPCustomPropertySets sapCustomPropertySets) {
        this.sapCustomPropertySets = sapCustomPropertySets;
    }

    @Reference
    public void setWebServiceActivator(WebServiceActivator webServiceActivator) {
        this.webServiceActivator = webServiceActivator;
    }
}
