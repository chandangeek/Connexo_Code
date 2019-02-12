/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import com.google.common.collect.Range;

import javax.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Logger;

public class CustomPropertySetHelper {
    private static final Logger LOGGER = Logger.getLogger(CustomPropertySetHelper.class.getName());

    private final CustomPropertySetService customPropertySetService;
    private final LoggerUtils loggerUtils;
    private final CASConflictsSolver casConflictsSolver;

    private Clock clock;

    @Inject
    public CustomPropertySetHelper(CustomPropertySetService customPropertySetService, Thesaurus thesaurus,
            MeterConfigFaultMessageFactory faultMessageFactory, Clock clock) {
        this.customPropertySetService = customPropertySetService;
        this.clock = clock;
        loggerUtils = new LoggerUtils(LOGGER, thesaurus, faultMessageFactory);
        casConflictsSolver = new CASConflictsSolver(customPropertySetService);
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
                loggerUtils.logInfo(serviceCall, MessageSeeds.ASSIGNED_VALUES_FOR_CUSTOM_ATTRIBUTE_SET, info.getId());
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
                loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE_SET,
                        newCustomProperySetInfo.getId());
                return faults;
            }
            CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet = registeredCustomPropertySet
                    .get().getCustomPropertySet();

            CustomPropertySetValues values = CustomPropertySetValues.empty();
            setCasValues(device, newCustomProperySetInfo, serviceCall, faults, customPropertySet, values);
            if (!faults.isEmpty()) {
                return faults;
            }
            if (customPropertySet.isVersioned()) {
                handleVersionedCas(device, newCustomProperySetInfo, serviceCall, faults, customPropertySet, values);
            } else {
                customPropertySetService.setValuesFor(customPropertySet, device, values);
            }
        } catch (Exception ex) {
            loggerUtils.logException(device, faults, serviceCall, ex,
                    MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET, newCustomProperySetInfo.getId());
            return faults;
        }
        return faults;
    }

    private void handleVersionedCas(Device device, CustomPropertySetInfo newCustomProperySetInfo,
            ServiceCall serviceCall, List<FaultMessage> faults,
            CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
            CustomPropertySetValues values) {

        if (newCustomProperySetInfo.getVersionId() == null) {
            createNewVersion(device, newCustomProperySetInfo, serviceCall, faults, customPropertySet, values);
        } else {
            updateExistingVersion(device, customPropertySet, newCustomProperySetInfo, serviceCall, faults);
        }
    }

    private Instant getFromDate(Device device, CustomPropertySetInfo newCustomProperySetInfo, ServiceCall serviceCall, List<FaultMessage> faults){
        Instant fromDate = newCustomProperySetInfo.getFromDate();
        if (fromDate == null && !newCustomProperySetInfo.isUpdateRange()) {
            loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.START_DATE_LOWER_CREATED_DATE,
                    device.getName());
            return null;
        }
        if (fromDate == null){
            return device.getCreateTime();
        }
        return fromDate;
    }

    private void createNewVersion(Device device, CustomPropertySetInfo newCustomProperySetInfo, ServiceCall serviceCall, List<FaultMessage> faults,
                                  CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet, CustomPropertySetValues values) {
        Instant fromDate = getFromDate(device, newCustomProperySetInfo, serviceCall, faults);
        if(!faults.isEmpty()){
            return;
        }
        Range<Instant> range = casConflictsSolver.solveConflictsForCreate(device, customPropertySet,
            fromDate, newCustomProperySetInfo.getEndDate());
        customPropertySetService.setValuesVersionFor(customPropertySet, device, values, range);
    }

    private void setCasValues(Device device, CustomPropertySetInfo newCustomProperySetInfo, ServiceCall serviceCall,
            List<FaultMessage> faults, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
            CustomPropertySetValues values) {
        List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
        for (Entry<String, String> newAttributeNameAndValue : newCustomProperySetInfo.getAttributes().entrySet()) {
            String attributeName = newAttributeNameAndValue.getKey();
            Optional<PropertySpec> propertySpec = propertySpecs.stream()
                    .filter(spec -> spec.getName().equals(attributeName)).findAny();
            if (propertySpec.isPresent()) {
                setAttributeValue(device, newCustomProperySetInfo, serviceCall, faults, values,
                        newAttributeNameAndValue, propertySpec.get());
            } else {
                loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE,
                        attributeName, newCustomProperySetInfo.getId());
            }
        }
    }

    private void updateExistingVersion(Device device,
            CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
            CustomPropertySetInfo newCustomProperySetInfo, ServiceCall serviceCall, List<FaultMessage> faults) {
        Optional<Instant> startTime = Optional.ofNullable(newCustomProperySetInfo.getFromDate());
        Optional<Instant> endTime = Optional.ofNullable(newCustomProperySetInfo.getEndDate());
        Instant versionId = newCustomProperySetInfo.getVersionId();
        CustomPropertySetValues existingValues = customPropertySetService.getUniqueValuesFor(customPropertySet, device,
                versionId);
        if (existingValues.isEmpty()) {
            loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.NO_CUSTOM_ATTRIBUTE_VERSION,
                    DefaultDateTimeFormatters.shortDate().withShortTime().build()
                            .format(versionId.atZone(clock.getZone())));
        } else {
            setCasValues(device, newCustomProperySetInfo, serviceCall, faults, customPropertySet, existingValues);
            if (!endTime.isPresent()) {
                endTime = Optional.of(Instant.EPOCH);
            }
            Range<Instant> range = casConflictsSolver.solveConflictsForUpdate(device, customPropertySet, startTime,
                    endTime, versionId, existingValues);
            customPropertySetService.setValuesVersionFor(customPropertySet, device, existingValues, range, versionId);
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
            loggerUtils.logException(device, faults, serviceCall, ex,
                    MessageSeeds.CANT_CONVERT_VALUE_OF_CUSTOM_ATTRIBUTE, attributeEntry.getValue(),
                    attributeEntry.getKey(), info.getId());
        }
    }
}
