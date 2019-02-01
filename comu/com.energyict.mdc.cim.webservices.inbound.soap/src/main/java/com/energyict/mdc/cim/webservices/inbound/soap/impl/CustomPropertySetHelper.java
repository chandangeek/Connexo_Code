/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.Ranges;

import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import com.google.common.collect.Range;

import javax.inject.Inject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomPropertySetHelper {
    private static final Logger LOGGER = Logger.getLogger(CustomPropertySetHelper.class.getName());

    private final CustomPropertySetService customPropertySetService;
    private final Thesaurus thesaurus;
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public CustomPropertySetHelper(CustomPropertySetService customPropertySetService, Thesaurus thesaurus,
            MeterConfigFaultMessageFactory faultMessageFactory) {
        this.customPropertySetService = customPropertySetService;
        this.thesaurus = thesaurus;
        this.faultMessageFactory = faultMessageFactory;
    }

    /**
     * Sets values for CustomPropertySets on specific device logging detailed error messages if possible
     *
     * @param device
     * @param customPropertySetsData
     *
     * @return
     */
    public List<FaultMessage> addCustomPropertySetsData(Device device,
            List<CustomPropertySetInfo> customPropertySetsData) {
        return addCustomPropertySetsData(device, customPropertySetsData, null);
    }

    /**
     * Sets values for CustomPropertySets on specific device logging detailed error messages if possible
     *
     * @param device
     * @param customPropertySetsData
     * @param serviceCall
     *            service call for logging purposes
     * @return
     */
    public List<FaultMessage> addCustomPropertySetsData(Device device,
            List<CustomPropertySetInfo> customPropertySetsData, ServiceCall serviceCall) {
        List<FaultMessage> allFaults = new ArrayList<>();
        for (CustomPropertySetInfo info : customPropertySetsData) {
            List<FaultMessage> faults = addCustomPropertySet(device, info, serviceCall);
            if (faults.isEmpty()) {
                logInfo(serviceCall, MessageSeeds.ASSIGNED_VALUES_FOR_CUSTOM_ATTRIBUTE_SET, info.getId());
            } else {
                allFaults.addAll(faults);
            }
        }
        return allFaults;
    }

    /**
     * Sets values for CustomPropertySet on specific device logging detailed error messages if possible
     *
     * @param device
     * @param newCustomProperySetInfo
     * @param serviceCall
     *            service call for logging purposes
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<FaultMessage> addCustomPropertySet(Device device, CustomPropertySetInfo newCustomProperySetInfo,
            ServiceCall serviceCall) {
        List<FaultMessage> faults = new ArrayList<>();
        try {
            Optional<RegisteredCustomPropertySet> registeredCustomPropertySet = customPropertySetService
                    .findActiveCustomPropertySet(newCustomProperySetInfo.getId());
            if (!registeredCustomPropertySet.isPresent()) {
                logSevere(device, faults, serviceCall, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE_SET, newCustomProperySetInfo.getId());
                return faults;
            }
            CustomPropertySet<Device, ?> customPropertySet = registeredCustomPropertySet.get().getCustomPropertySet();
            List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
            CustomPropertySetValues values = CustomPropertySetValues.empty();
            for (Entry<String, String> newAttributeNameAndValue : newCustomProperySetInfo.getAttributes().entrySet()) {
                String attributeName = newAttributeNameAndValue.getKey();
                Optional<PropertySpec> optionalPropertySpec = propertySpecs.stream()
                        .filter(spec -> spec.getName().equals(attributeName)).findAny();
                if (optionalPropertySpec.isPresent()) {
                    setAttributeValue(device, newCustomProperySetInfo, serviceCall, faults, values, newAttributeNameAndValue,
                            optionalPropertySpec.get());
                } else {
                    logSevere(device, faults, serviceCall, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE, attributeName,
                            newCustomProperySetInfo.getId());
                }
            }
            if (faults.isEmpty()) {
                if (customPropertySet.isVersioned()) {
                    Range<Instant> range = Ranges.closedOpen(newCustomProperySetInfo.getFromDate(), newCustomProperySetInfo.getEndDate());
                    if (newCustomProperySetInfo.getVersionId() == null) {
                        // add new
                        customPropertySetService.setValuesVersionFor(customPropertySet, device, values, range);
                    } else {
                        // modify existing
                        customPropertySetService.setValuesVersionFor(customPropertySet, device, values, range,
                                newCustomProperySetInfo.getVersionId());
                    }
                } else {
                    customPropertySetService.setValuesFor(customPropertySet, device, values);
                }
            }
            return faults;
        } catch (Exception ex) {
            logException(device, faults, serviceCall, ex, MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET,
                    newCustomProperySetInfo.getId());
            return faults;
        }
    }

    private void setAttributeValue(Device device, CustomPropertySetInfo info, ServiceCall serviceCall,
            List<FaultMessage> faults, CustomPropertySetValues values, Entry<String, String> attributeEntry,
            PropertySpec propertySpec) {
        Object fromStringValue;
        try {
            fromStringValue = propertySpec.getValueFactory().fromStringValue(attributeEntry.getValue());
            values.setProperty(attributeEntry.getKey(), fromStringValue);
        } catch (Exception ex) {
            logException(device, faults, serviceCall, ex, MessageSeeds.CANT_CONVERT_VALUE_OF_CUSTOM_ATTRIBUTE,
                    attributeEntry.getValue(), attributeEntry.getKey(), info.getId());
        }
    }

    private void logSevere(Device device, List<FaultMessage> allFaults, ServiceCall serviceCall,
            MessageSeeds messageSeeds, Object... args) {
        if (serviceCall != null) {
            serviceCall.log(LogLevel.SEVERE, messageSeeds.translate(thesaurus, args));
        } else {
            LOGGER.log(Level.SEVERE, messageSeeds.translate(thesaurus, args));
        }
        allFaults.add(faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), messageSeeds, args).get());
    }

    private void logInfo(ServiceCall serviceCall, MessageSeeds messageSeeds, Object... args) {
        if (serviceCall != null) {
            serviceCall.log(LogLevel.INFO, messageSeeds.translate(thesaurus, args));
        } else {
            LOGGER.log(Level.INFO, messageSeeds.translate(thesaurus, args));
        }
    }

    private void logException(Device device, List<FaultMessage> faults, ServiceCall serviceCall, Exception ex,
            MessageSeeds messageSeeds, Object... args) {
        if (serviceCall != null) {
            serviceCall.log(messageSeeds.translate(thesaurus, args), ex);
        } else {
            LOGGER.log(Level.SEVERE, messageSeeds.translate(thesaurus, args), ex);
        }
        faults.add(faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), messageSeeds, args).get());
    }

}
