/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;

import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import com.google.common.collect.Range;

import javax.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomPropertySetHelper {
    private static final Logger LOGGER = Logger.getLogger(CustomPropertySetHelper.class.getName());

    private final CustomPropertySetService customPropertySetService;
    private final LoggerUtils loggerUtils;

    private Clock clock;

    @Inject
    public CustomPropertySetHelper(CustomPropertySetService customPropertySetService, Thesaurus thesaurus,
                                   MeterConfigFaultMessageFactory faultMessageFactory, Clock clock) {
        this.customPropertySetService = customPropertySetService;
        this.clock = clock;
        loggerUtils = new LoggerUtils(LOGGER, thesaurus, faultMessageFactory);
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

    private Range<Instant> solveConflictsForCreate(Device businessObject, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
                                                   Range<Instant> range) {
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(customPropertySet, businessObject);
        for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenCreating(range)) {
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                range = getRangeToCreate(conflict.getConflictingRange().lowerEndpoint(), range.hasUpperBound() ? range.upperEndpoint() : Instant.EPOCH);
            }
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                range = getRangeToCreate(range.hasLowerBound() ? range.lowerEndpoint() : Instant.EPOCH,conflict.getConflictingRange().upperEndpoint());
            }
        }
        return range;
    }

    private Range<Instant> getRangeToCreate(Instant startTime, Instant endTime) {
        Optional<Instant> startTimeOptional = Optional.ofNullable(startTime);
        Optional<Instant> endTimeOptional = Optional.ofNullable(endTime);
        if (notDefinedOrIsInfinite(startTimeOptional) && notDefinedOrIsInfinite(endTimeOptional)) {
            return Range.all();
        } else if (notDefinedOrIsInfinite(startTimeOptional) && isNotInfinite(endTimeOptional)) {
            return Range.lessThan(endTimeOptional.get());
        } else if (notDefinedOrIsInfinite(endTimeOptional)) {
            return Range.atLeast(startTimeOptional.get());
        } else {
            return Range.closedOpen(startTimeOptional.get(), endTimeOptional.get());
        }
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
                loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE_SET, newCustomProperySetInfo.getId());
                return faults;
            }
            CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet = registeredCustomPropertySet.get().getCustomPropertySet();

            CustomPropertySetValues values = CustomPropertySetValues.empty();
            setCASValues(device, newCustomProperySetInfo, serviceCall, faults, customPropertySet, values);
            if (faults.isEmpty()) {
                if (customPropertySet.isVersioned()) {
                    //TODO what happens of from or end are infinite?
                    Range<Instant> range = Ranges.closedOpen(newCustomProperySetInfo.getFromDate(), newCustomProperySetInfo.getEndDate());

                    if (newCustomProperySetInfo.getVersionId() == null) {
                        // add new
                        range = solveConflictsForCreate(device, customPropertySet, range);
                        customPropertySetService.setValuesVersionFor(customPropertySet, device, values, range);
                    } else {
                        // modify existing TODO
                        updateExistingVersion(device, customPropertySet, newCustomProperySetInfo, serviceCall, faults);
//                        customPropertySetService.setValuesVersionFor(customPropertySet, device, values, range,
//                                newCustomProperySetInfo.getVersionId());
                    }
                } else {
                    customPropertySetService.setValuesFor(customPropertySet, device, values);
                }
            }
            return faults;
        } catch (Exception ex) {
            loggerUtils.logException(device, faults, serviceCall, ex, MessageSeeds.CANT_ASSIGN_VALUES_FOR_CUSTOM_ATTRIBUTE_SET,
                    newCustomProperySetInfo.getId());
            return faults;
        }
    }

    private void setCASValues(Device device, CustomPropertySetInfo newCustomProperySetInfo,
                              ServiceCall serviceCall, List<FaultMessage> faults,
                              CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
                              CustomPropertySetValues values) {
        List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
        for (Entry<String, String> newAttributeNameAndValue : newCustomProperySetInfo.getAttributes().entrySet()) {
            String attributeName = newAttributeNameAndValue.getKey();
            Optional<PropertySpec> propertySpec = propertySpecs.stream()
                    .filter(spec -> spec.getName().equals(attributeName)).findAny();
            if (propertySpec.isPresent()) {
                setAttributeValue(device, newCustomProperySetInfo, serviceCall, faults, values, newAttributeNameAndValue,
                        propertySpec.get());
            } else {
                loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.CANT_FIND_CUSTOM_ATTRIBUTE, attributeName,
                        newCustomProperySetInfo.getId());
            }
        }
    }

    private void updateExistingVersion(Device device, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
                                       CustomPropertySetInfo newCustomProperySetInfo, ServiceCall serviceCall, List<FaultMessage> faults)
            throws FaultMessage {
        Optional<Instant> startTime = Optional.ofNullable(newCustomProperySetInfo.getFromDate());
        Optional<Instant> endTime = Optional.ofNullable(newCustomProperySetInfo.getEndDate());
        Optional<Instant> versionId= Optional.ofNullable(newCustomProperySetInfo.getVersionId());
        CustomPropertySetValues existingValues = customPropertySetService.getUniqueValuesFor(customPropertySet,
                device, versionId.get());
        if (existingValues.isEmpty()) {
            loggerUtils.logSevere(device, faults, serviceCall, MessageSeeds.NO_CUSTOM_ATTRIBUTE_VERSION,
                    DefaultDateTimeFormatters.shortDate().withShortTime().build().format(versionId.get().atZone(clock.getZone())));
        } else {
            setCASValues(device, newCustomProperySetInfo, serviceCall, faults,  customPropertySet, existingValues);//updateAttributesValues(customPropertySet, newCustomProperySetInfo, existingValues);
            if(!endTime.isPresent()){
                endTime = Optional.of(Instant.EPOCH);
            }
            Range<Instant> range = solveConflictsForUpdate(device, customPropertySet, startTime, endTime, versionId, existingValues);
            customPropertySetService.setValuesVersionFor(customPropertySet, device, existingValues, range, versionId.get());
        }
    }

    private boolean isNotInfinite(Optional<Instant> endTime) {
        return endTime.isPresent() && !endTime.get().equals(Instant.EPOCH);
    }

    private boolean notDefinedOrIsInfinite(Optional<Instant> dateTime) {
        return !dateTime.isPresent() || dateTime.get().equals(Instant.EPOCH);
    }

    private Range<Instant> solveConflictsForUpdate(Device device, CustomPropertySet<Device, ? extends PersistentDomainExtension> customPropertySet,
                       Optional<Instant> startTime, Optional<Instant> endTime, Optional<Instant> versionId, CustomPropertySetValues existingValues) {
        Range<Instant> range = getRangeToUpdate(startTime, endTime, existingValues.getEffectiveRange());
        OverlapCalculatorBuilder overlapCalculatorBuilder = customPropertySetService.calculateOverlapsFor(customPropertySet, device);

        for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenUpdating(versionId.get(), range)) {
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                range = getRangeToUpdate(Optional.ofNullable(conflict.getConflictingRange().lowerEndpoint()), endTime, existingValues.getEffectiveRange());
            }
            if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                range = getRangeToUpdate(startTime, Optional.ofNullable(conflict.getConflictingRange().upperEndpoint()), existingValues.getEffectiveRange());
            }
        }
        return range;
    }

    private Range<Instant> getRangeToUpdate(Optional<Instant> startTime, Optional<Instant> endTime, Range<Instant> oldRange) {
        if (!startTime.isPresent() && !endTime.isPresent()) {
            return oldRange;
        } else if (!startTime.isPresent()) {
            if (oldRange.hasLowerBound()) {
                if(!endTime.get().equals(Instant.EPOCH)) {
                    return Range.closedOpen(oldRange.lowerEndpoint(), endTime.get());
                } else {
                    return Range.atLeast(oldRange.lowerEndpoint());
                }
            } else if (endTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.lessThan(endTime.get());
            }
        } else if (!endTime.isPresent()) {
            if (oldRange.hasUpperBound()) {
                if(!startTime.get().equals(Instant.EPOCH)) {
                    return Range.closedOpen(startTime.get(), oldRange.upperEndpoint());
                } else {
                    return Range.lessThan(oldRange.upperEndpoint());
                }
            } else if (startTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.atLeast(startTime.get());
            }
        } else {
            return getRangeToCreate(startTime.get(), endTime.get());
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
            loggerUtils.logException(device, faults, serviceCall, ex, MessageSeeds.CANT_CONVERT_VALUE_OF_CUSTOM_ATTRIBUTE,
                    attributeEntry.getValue(), attributeEntry.getKey(), info.getId());
        }
    }
}
