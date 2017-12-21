/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.customattributes;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.data.importers.impl.properties.SupportedNumberFormat;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CustomAttributesImportProcessor extends AbstractDeviceDataFileImportProcessor<CustomAttributesImportRecord> {


    CustomAttributesImportProcessor(DeviceDataImporterContext context, SupportedNumberFormat numberFormat, SecurityManagementService securityManagementService) {
        super(context);
    }

    @Override
    public void process(CustomAttributesImportRecord data, FileImportLogger logger) throws ProcessorException {
        Device device = findDeviceByIdentifier(data.getDeviceIdentifier())
                .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        if (!Checks.is(data.getReadingType()).emptyOrOnlyWhiteSpace()) {
            ReadingType readingType = getContext().getMeteringService().getReadingType(data.getReadingType())
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_SUCH_READING_TYPE, data.getLineNumber(), data.getReadingType()));
            if (readingType.isRegular()) {
                Channel channel = device.getChannels().stream().filter(ch -> ch.getReadingType().equals(readingType)).findAny()
                        .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_READINGTYPE_ON_DEVICE, data.getLineNumber(), data.getReadingType(), data.getDeviceIdentifier()));
                RegisteredCustomPropertySet registeredCustomPropertySet = device.getDeviceType()
                        .getLoadProfileTypeCustomPropertySet(channel.getChannelSpec().getLoadProfileSpec().getLoadProfileType())
                        .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_CUSTOMATTRIBUTE_ON_READINGTYPE_OF_DEVICE,
                                data.getLineNumber(), "", data.getReadingType(), data.getDeviceIdentifier()));
                validateCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet(), channel.getChannelSpec(), data, device.getId());
                addCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet(), channel.getChannelSpec(), data, device.getId());
            } else {
                Register register = device.getRegisters().stream().filter(reg -> reg.getReadingType().equals(readingType)).findAny()
                        .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_READINGTYPE_ON_DEVICE, data.getLineNumber(), data.getReadingType(), data.getDeviceIdentifier()));
                RegisteredCustomPropertySet registeredCustomPropertySet = device.getDeviceType()
                        .getRegisterTypeTypeCustomPropertySet(register.getRegisterSpec().getRegisterType())
                        .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_CUSTOMATTRIBUTE_ON_READINGTYPE_OF_DEVICE,
                                data.getLineNumber(), "", data.getReadingType(), data.getDeviceIdentifier()));
                validateCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet(), register.getRegisterSpec(), data, device.getId());
                addCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet(), register.getRegisterSpec(), data, device.getId());
            }
        } else {
            validateCustomPropertySetValues(device, data);
            addCustomPropertySetValues(device, data);
        }
    }

    @Override
    public void complete(FileImportLogger logger) {
        // do nothing
    }

    private void validateMandatoryCustomProperties(CustomPropertySet<?, ?> customPropertySet, CustomPropertySetValues values, CustomAttributesImportRecord data) {
        List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
        for (PropertySpec spec : propertySpecs) {
            if (spec.isRequired() && values.getProperty(spec.getName()) == null) {
                throw new ProcessorException(MessageSeeds.MISSING_REQUIRED_CUSTOMATTRIBUTE_VALUE_ON_DEVICE,
                        data.getLineNumber(),
                        spec.getName(),
                        customPropertySet.getId(),
                        data.getDeviceIdentifier());
            }
        }
    }

    private void validatePossibleValues(CustomPropertySet<?, ?> customPropertySet, CustomPropertySetValues values, CustomAttributesImportRecord data) {
        List<PropertySpec> propertySpecs = customPropertySet.getPropertySpecs();
        for (PropertySpec spec : propertySpecs) {
            if (spec.getValueFactory().getValueType().equals(Quantity.class) && !isValidQuantityValues(spec, (Quantity) values.getProperty(spec.getName()))) {
                throw new ProcessorException(MessageSeeds.WRONG_QUANTITY_FORMAT,
                        data.getLineNumber(),
                        spec.getName(),
                        spec.getPossibleValues().getAllValues().stream().map(q -> String.valueOf(((Quantity) q).getMultiplier())).collect(Collectors.joining(",")),
                        spec.getPossibleValues().getAllValues().stream().map(q -> String.valueOf(((Quantity) q).getUnit())).collect(Collectors.joining(",")));
            } else if (spec.getPossibleValues() != null
                    && spec.getPossibleValues().isExhaustive()
                    && !isValidEnumValues(spec, values.getProperty(spec.getName()))) {
                throw new ProcessorException(MessageSeeds.WRONG_ENUM_FORMAT,
                        data.getLineNumber(),
                        spec.getName(),
                        spec.getPossibleValues().getAllValues().stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
        }
    }

    private void validateCustomPropertySetValues(Device device, CustomAttributesImportRecord data) {
        List<CustomPropertySet> customPropertySets = device.getDeviceType().getCustomPropertySets().stream()
                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                .filter(cps -> data.getCustomAttributes().keySet().stream().allMatch(k -> k.contains(cps.getId())))
                .collect(Collectors.toList());
        for (CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet : customPropertySets) {
            validateCustomPropertySetValues(customPropertySet, device, data, null);
        }
    }

    private void validateCustomPropertySetValues(CustomPropertySet customPropertySet, Object businessObject, CustomAttributesImportRecord data, Object additionalPrimaryKey) {
        if (!customPropertySet.isVersioned()) {
            validateCreateOrUpdateNonVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        } else {
            validateCreateOrUpdateVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        }
    }


    private void validateCreateOrUpdateVersionedSet(Object businessObject, CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, Object additionalObject) {
        Optional versionId = data.getCustomAttributes()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(customPropertySet.getId() + ".versionId"))
                .map(Map.Entry::getValue)
                .findFirst();
        CustomPropertySetValues values = null;
        if (versionId.isPresent()) {
            if (additionalObject != null) {
                values = getContext().getCustomPropertySetService().getUniqueValuesFor(customPropertySet, businessObject, (Instant) versionId.get(), additionalObject);
            } else {
                values = getContext().getCustomPropertySetService().getUniqueValuesFor(customPropertySet, businessObject, (Instant) versionId.get());
            }

            if (values == null) {
                throw new ProcessorException(MessageSeeds.NO_CUSTOMATTRIBUTE_VERSION_ON_DEVICE,
                        data.getLineNumber(),
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(((Instant) versionId.get()).atZone(getContext().getClock().getZone())),
                        data.getDeviceIdentifier());
            }
        } else {
            values = CustomPropertySetValues.empty();
        }
        validateCreateOrUpdateCustomAttributeValues(customPropertySet, data, values);
    }


    private void validateCreateOrUpdateNonVersionedSet(Object businessObject, CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, Object additionalObject) {
        CustomPropertySetValues values = getValues(businessObject, customPropertySet, data, additionalObject);
        validateCreateOrUpdateCustomAttributeValues(customPropertySet, data, values);
    }

    private void validateCreateOrUpdateCustomAttributeValues(CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, CustomPropertySetValues values) {
        if (values != null) {
            customPropertySet.getPropertySpecs().forEach(spec ->
                    findCustomAttributeValue(customPropertySet, data, spec).ifPresent(v -> values.setProperty(spec.getName(), v)));
            validateMandatoryCustomProperties(customPropertySet, values, data);
            validatePossibleValues(customPropertySet, values, data);
            getContext().getCustomPropertySetService()
                    .validateCustomPropertySetValues(customPropertySet, values);
        }
    }

    private CustomPropertySetValues getValues(Object businessObject, CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, Object additionalPrimaryKeyObject) {
        CustomPropertySetValues values;
        if (additionalPrimaryKeyObject != null) {
            values = getContext().getCustomPropertySetService().getUniqueValuesFor(customPropertySet, businessObject, additionalPrimaryKeyObject);
        } else {
            values = getContext().getCustomPropertySetService().getUniqueValuesFor(customPropertySet, businessObject);
        }
        return updateValues(customPropertySet, data, values);
    }

    private CustomPropertySetValues getValuesVersion(Object businessObject, CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, Instant versionId, Object additionalPrimaryKeyObject) {
        if (additionalPrimaryKeyObject != null) {
            return getContext().getCustomPropertySetService().getUniqueValuesFor(customPropertySet, businessObject, versionId, additionalPrimaryKeyObject);
        } else {
            return getContext().getCustomPropertySetService().getUniqueValuesFor(customPropertySet, businessObject, versionId);
        }
    }

    private CustomPropertySetValues updateValues(CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, CustomPropertySetValues values) {
        customPropertySet.getPropertySpecs().forEach(spec ->
                findCustomAttributeValue(customPropertySet, data, spec).ifPresent(v -> values.setProperty(spec.getName(), v)));
        return values;
    }

    private Optional<Object> findCustomAttributeValue(CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, PropertySpec spec) {
        return data.getCustomAttributes().entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(customPropertySet.getId() + "." + spec.getName()))
                .findFirst().map(Map.Entry::getValue);
    }

    private void copyValues(CustomPropertySetValues source, CustomPropertySetValues target) {
        source.propertyNames().forEach(propertyName -> {
            Object propertyValue = source.getProperty(propertyName);
            target.setProperty(propertyName, propertyValue);
        });
    }

    private void addCustomPropertySetValues(Device device, CustomAttributesImportRecord data) {
        for (RegisteredCustomPropertySet registeredCustomPropertySet : device.getDeviceType().getCustomPropertySets().stream()
                .filter(cps -> data.getCustomAttributes().keySet().stream().anyMatch(k -> k.contains(cps.getCustomPropertySetId()))).collect(Collectors.toList())) {
            addCustomPropertySetValues(registeredCustomPropertySet.getCustomPropertySet(), device, data, null);
        }
    }

    private void addCustomPropertySetValues(CustomPropertySet customPropertySet, Object businessObject, CustomAttributesImportRecord data, Object additionalPrimaryKey) {
        if (customPropertySet.isVersioned()) {
            createOrUpdateVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        } else {
            createOrUpdateNonVersionedSet(businessObject, customPropertySet, data, additionalPrimaryKey);
        }
    }

    private void createOrUpdateVersionedSet(Object businessObject, CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, Object additionalPrimaryKeyObject) {
        Optional<Instant> startTime = data.getCustomAttributes()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(customPropertySet.getId() + ".startTime"))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(Instant.class::cast);
        Optional<Instant> endTime = data.getCustomAttributes()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(customPropertySet.getId() + ".endTime"))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(Instant.class::cast);
        Optional<Instant> versionId = data.getCustomAttributes()
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(customPropertySet.getId() + ".versionId"))
                .map(Map.Entry::getValue)
                .findFirst()
                .map(Instant.class::cast);
        checkInterval(startTime, endTime, customPropertySet, data);
        if (versionId.isPresent()) {
            CustomPropertySetValues values = getValuesVersion(businessObject, customPropertySet, data, versionId.get(), additionalPrimaryKeyObject);
            if (!values.isEmpty()) {
                values = updateValues(customPropertySet, data, values);
                Range<Instant> range = data.isAutoResolution() ? getRangeToUpdate(startTime, endTime, values.getEffectiveRange()) : getRangeToCreate(startTime, endTime);
                OverlapCalculatorBuilder overlapCalculatorBuilder;
                if (additionalPrimaryKeyObject != null) {
                    overlapCalculatorBuilder = getContext().getCustomPropertySetService()
                            .calculateOverlapsFor(customPropertySet, businessObject, additionalPrimaryKeyObject);
                } else {
                    overlapCalculatorBuilder = getContext().getCustomPropertySetService()
                            .calculateOverlapsFor(customPropertySet, businessObject);
                }
                if (data.isAutoResolution()) {
                    for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenUpdating(versionId.get(), range)) {
                        if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                            range = getRangeToUpdate(Optional.ofNullable(conflict.getConflictingRange().lowerEndpoint()), endTime, values.getEffectiveRange());
                        }
                        if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                            range = getRangeToUpdate(startTime, Optional.ofNullable(conflict.getConflictingRange().upperEndpoint()), values.getEffectiveRange());
                        }
                    }
                } else if (overlapCalculatorBuilder.whenCreating(range).size() > 0) {
                    throw new ProcessorException(MessageSeeds.UNRESOLVABLE_CUSTOMATTRIBUTE_CONFLICT,
                            data.getLineNumber(),
                            customPropertySet.getId(),
                            data.getDeviceIdentifier());
                }
                if (additionalPrimaryKeyObject != null) {
                    getContext().getCustomPropertySetService()
                            .setValuesVersionFor(customPropertySet, businessObject, values, range, versionId.get(), additionalPrimaryKeyObject);
                } else {
                    getContext().getCustomPropertySetService()
                            .setValuesVersionFor(customPropertySet, businessObject, values, range, versionId.get());
                }
            } else {
                throw new ProcessorException(MessageSeeds.NO_CUSTOMATTRIBUTE_VERSION_ON_DEVICE,
                        data.getLineNumber(),
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(versionId.get().atZone(getContext().getClock().getZone())),
                        data.getDeviceIdentifier());
            }
        } else {
            Range<Instant> range = getRangeToCreate(startTime, endTime);
            OverlapCalculatorBuilder overlapCalculatorBuilder;
            if (additionalPrimaryKeyObject != null) {
                overlapCalculatorBuilder = getContext().getCustomPropertySetService()
                        .calculateOverlapsFor(customPropertySet, businessObject, additionalPrimaryKeyObject);
            } else {
                overlapCalculatorBuilder = getContext().getCustomPropertySetService()
                        .calculateOverlapsFor(customPropertySet, businessObject);
            }
            if (data.isAutoResolution()) {
                for (ValuesRangeConflict conflict : overlapCalculatorBuilder.whenCreating(range)) {
                    if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_AFTER)) {
                        range = getRangeToCreate(Optional.ofNullable(conflict.getConflictingRange().lowerEndpoint()), endTime);
                    }
                    if (conflict.getType().equals(ValuesRangeConflictType.RANGE_GAP_BEFORE)) {
                        range = getRangeToCreate(startTime, Optional.ofNullable(conflict.getConflictingRange().upperEndpoint()));
                    }
                }
            } else if (overlapCalculatorBuilder.whenCreating(range).size() > 0) {
                throw new ProcessorException(MessageSeeds.UNRESOLVABLE_CUSTOMATTRIBUTE_CONFLICT,
                        data.getLineNumber(),
                        customPropertySet.getId(),
                        data.getDeviceIdentifier());
            }
            CustomPropertySetValues newValues = CustomPropertySetValues.emptyDuring(range);
            if (additionalPrimaryKeyObject != null) {
                getContext().getCustomPropertySetService()
                        .setValuesVersionFor(customPropertySet, businessObject, updateValues(customPropertySet, data, newValues), range, additionalPrimaryKeyObject);
            } else {
                getContext().getCustomPropertySetService().setValuesVersionFor(customPropertySet, businessObject, updateValues(customPropertySet, data, newValues), range);
            }
        }
    }

    private void createOrUpdateNonVersionedSet(Object businessObject, CustomPropertySet<Object, ? extends PersistentDomainExtension> customPropertySet, CustomAttributesImportRecord data, Object additionalPrimaryKeyObject) {
        CustomPropertySetValues values = getValues(businessObject, customPropertySet, data, additionalPrimaryKeyObject);
        if (additionalPrimaryKeyObject != null) {
            getContext().getCustomPropertySetService().setValuesFor(customPropertySet, businessObject, values, additionalPrimaryKeyObject);
        } else {
            getContext().getCustomPropertySetService().setValuesFor(customPropertySet, businessObject, values);
        }
    }

    private Range<Instant> getRangeToCreate(Optional<Instant> startTime, Optional<Instant> endTime) {
        if ((!startTime.isPresent() || startTime.get().equals(Instant.EPOCH))
                && (!endTime.isPresent() || endTime.get().equals(Instant.EPOCH))) {
            return Range.all();
        } else if (!startTime.isPresent() || startTime.get().equals(Instant.EPOCH)) {
            return Range.lessThan(endTime.get());
        } else if (!endTime.isPresent() || endTime.get().equals(Instant.EPOCH)) {
            return Range.atLeast(startTime.get());
        } else {
            return Range.closedOpen(
                    startTime.get(),
                    endTime.get());
        }
    }

    private Range<Instant> getRangeToUpdate(Optional<Instant> startTime, Optional<Instant> endTime, Range<Instant> oldRange) {
        if (!startTime.isPresent() && !endTime.isPresent()) {
            return oldRange;
        } else if (!startTime.isPresent()) {
            if (oldRange.hasUpperBound()) {
                return Range.closedOpen(startTime.get(), oldRange.upperEndpoint());
            } else if (endTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.lessThan(endTime.get());
            }
        } else if (!endTime.isPresent()) {
            if (oldRange.hasUpperBound()) {
                return Range.closedOpen(oldRange.lowerEndpoint(), endTime.get());
            } else if (startTime.get().equals(Instant.EPOCH)) {
                return Range.all();
            } else {
                return Range.atLeast(startTime.get());
            }
        } else {
            return getRangeToCreate(startTime, endTime);
        }
    }

    private void checkInterval(Optional<Instant> startTime, Optional<Instant> endTime, CustomPropertySet customPropertySet, CustomAttributesImportRecord data) {
        if (startTime.isPresent() && endTime.isPresent() && !endTime.get().equals(Instant.EPOCH) && endTime.get().isBefore(startTime.get())) {
            throw new ProcessorException(MessageSeeds.NO_CUSTOMATTRIBUTE_VERSION_ON_DEVICE,
                    data.getLineNumber(),
                    customPropertySet.getId(),
                    data.getDeviceIdentifier());
        }
    }

    private boolean isValidQuantityValues(PropertySpec spec, Quantity value) {
        if (spec.getPossibleValues().getAllValues().stream()
                .noneMatch(pv -> ((Quantity) pv).getUnit().equals(value.getUnit()) && ((Quantity) pv).getMultiplier() == ((value.getMultiplier())))) {
            return false;
        }
        return true;
    }


    private boolean isValidEnumValues(PropertySpec spec, Object value) {
        if (spec.getPossibleValues().getAllValues().stream()
                .noneMatch(pv -> pv.equals(value))) {
            return false;
        }
        return true;
    }
}
