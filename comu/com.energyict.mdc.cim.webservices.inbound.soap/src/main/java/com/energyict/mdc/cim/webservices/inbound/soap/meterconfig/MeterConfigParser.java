/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */


package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.elster.jupiter.domain.util.FieldMaxLengthException;
import com.elster.jupiter.domain.util.FieldMaxLengthValidator;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.cim.webservices.inbound.soap.MeterInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.MessageSeeds;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.SecurityKeyInfo;
import com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset.CasInfo;
import com.energyict.mdc.cim.webservices.outbound.soap.OperationEnum;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;
import ch.iec.tc57._2011.meterconfig.ConfigurationEvent;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.LifecycleDate;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SharedCommunicationSchedule;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfig.Zone;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import com.elster.connexo._2018.schema.securitykeys.SecurityKey;
import com.elster.connexo._2018.schema.securitykeys.SecurityKeys;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/*
 * Uses for parsing ch.iec.tc57._2011.meterconfig
 */
public class MeterConfigParser {
    private static final String METER_CONFIG_CUSTOM_ATTRIBUTE_SET_PREFIX = "MeterConfig.Meter.MeterCustomAttributeSet[";
    private static final String METER_CONFIG_SECURITY = "MeterConfig.Meter.SecurityKey.";
    private static final String SECURITY_ACCESSOR_NAME = "SecurityAccessorName";
    private static final String WRAP_KEY_LABEL = "WrapKeyInfo.Label";
    private static final String SYMMETRIC_KEY_VALUE = "WrapKeyInfo.SymmetricKey.CipherData.CipherValue";
    private static final String SECURITY_ACCESSOR_KEY_VALUE = "SecurityAccessorKey.CipherData.CipherValue";
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    @Inject
    public MeterConfigParser(MeterConfigFaultMessageFactory faultMessageFactory) {
        this.faultMessageFactory = faultMessageFactory;
    }

    public MeterInfo asMeterInfo(Meter meter) {
        MeterInfo meterInfo = new MeterInfo();
        meterInfo.setDeviceName(extractName(meter.getNames()).orElse(null));
        meterInfo.setmRID(extractMrid(meter).orElse(null));
        meterInfo.setSerialNumber(extractSerialNumber(meter).orElse(null));
        return meterInfo;
    }

    public MeterInfo asMeterInfo(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions,
                                 OperationEnum operationEnum, boolean failOnExistentDevice) throws FaultMessage, FieldMaxLengthException {
        MeterInfo meterInfo = new MeterInfo();
        meterInfo.setSerialNumber(extractSerialNumber(meter).orElse(null));

        switch (operationEnum) {
            case CREATE:
                meterInfo.setDeviceName(extractDeviceNameForCreate(meter));
                meterInfo.setDeviceType(extractDeviceTypeName(meter));
                meterInfo.setZones(extractDeviceZones(meter, endDeviceFunctions));
                meterInfo.setShipmentDate(extractShipmentDate(meter));
                meterInfo.setFailOnExistentDevice(failOnExistentDevice);
                break;
            case UPDATE:
                meterInfo.setDeviceName(extractDeviceNameForUpdate(meter));
                meterInfo.setShipmentDate(extractOptionalShipmentDate(meter));
                meterInfo.setmRID(extractMrid(meter).orElse(null));
                meterInfo.setConfigurationEventReason(extractConfigurationReason(meter).orElse(null));
                meterInfo.setStatusValue(extractStatusValue(meter).orElse(null));
                meterInfo.setStatusEffectiveDate(extractConfigurationEffectiveDate(meter).orElse(null));
                meterInfo.setMultiplierEffectiveDate(extractConfigurationEffectiveDate(meter).orElse(null));
                meterInfo.setZones(extractDeviceZones(meter, endDeviceFunctions));
                // at least one of name, serial number and mrid should be present
                if (meterInfo.getDeviceName() == null && meterInfo.getSerialNumber() == null
                        && meterInfo.getmRID() == null) {
                    throw faultMessageFactory
                            .meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.DEVICE_IDENTIFIER_MISSING)
                            .get();
                }
                break;
            case DELETE:
                meterInfo.setDeviceName(extractName(meter.getNames()).orElse(null));
                meterInfo.setmRID(extractMrid(meter).orElse(null));
                break;
            default:
                break;
        }
        meterInfo.setBatch(extractBatch(meter).orElse(null));
        meterInfo.setManufacturer(extractManufacturer(meter).orElse(null));
        meterInfo.setModelNumber(extractModelNumber(meter).orElse(null));
        meterInfo.setModelVersion(extractModelVersion(meter).orElse(null));
        meterInfo.setMultiplier(extractMultiplier(meter).orElse(null));
        meterInfo.setElectronicAddress(meter.getElectronicAddress());
        meterInfo.setCustomAttributeSets(extractCustomPropertySets(meter));
        meterInfo.setDeviceConfigurationName(extractDeviceConfig(meter, endDeviceFunctions));
        meterInfo.setSecurityInfo(extractSecurityInfo(meter));
        meterInfo.setConnectionAttributes(meter.getConnectionAttributes());
        meterInfo.setSharedCommunicationSchedules(extractSharedCommunicationSchedules(meter));
        FieldMaxLengthValidator.validate(meterInfo);
        return meterInfo;
    }

    private List<SharedCommunicationSchedule> extractSharedCommunicationSchedules(Meter meter) throws FaultMessage {
        List<SharedCommunicationSchedule> result = new ArrayList<>();
        if (meter.getSharedCommunicationSchedules() != null) {
            result=meter.getSharedCommunicationSchedules().getSharedCommunicationSchedule();
        }
        return result;
    }

    private List<CasInfo> extractCustomPropertySets(Meter meter) throws FaultMessage {
        List<CasInfo> result = new ArrayList<>();
        String meterName = getMeterName(meter);
        int index = 0;
        for (CustomAttributeSet cas : meter.getMeterCustomAttributeSet()) {
            CasInfo info = extractCustomPropertySet(meterName, index, cas);
            result.add(info);
            index++;
        }
        return result;
    }

    private CasInfo extractCustomPropertySet(String meterName, int customPropertySetIndex,
                                             CustomAttributeSet cas) throws FaultMessage {
        CasInfo info = new CasInfo();
        info.setId(extractCpsId(meterName, customPropertySetIndex, cas));
        info.setVersionId(cas.getVersionId());
        info.setUpdateRange(cas.isUpdateRange());
        info.setFromDate(cas.getFromDateTime());
        info.setEndDate(cas.getToDateTime());
        Map<String, String> attributes = new HashMap<>();
        int attributeIndex = 0;
        for (com.elster.connexo._2017.schema.customattributes.Attribute attribute : cas.getAttribute()) {
            attributes.put(extractCpsAttributeName(meterName, customPropertySetIndex, attribute, attributeIndex),
                    extractCpsAttributeValue(meterName, customPropertySetIndex, attribute, attributeIndex));
            attributeIndex++;
        }
        info.setAttributes(attributes);
        return info;
    }

    private SecurityInfo extractSecurityInfo(Meter meter) throws FaultMessage {
        final SecurityInfo securityInfo = new SecurityInfo();
        securityInfo.setSecurityKeys(new ArrayList<>());
        securityInfo.setDeviceStatuses(new ArrayList<>());
        if (meter.getSecurityKeys() != null) {
            final SecurityKeys securityKeys = meter.getSecurityKeys();
            if (securityKeys.getAllowedDeviceStatuses() != null) {
                securityInfo.setDeviceStatusesElementPresent(true);
                securityInfo.setDeviceStatuses(securityKeys.getAllowedDeviceStatuses().getAllowedDeviceStatus());
            } else {
                securityInfo.setDeviceStatusesElementPresent(false);
            }
            List<SecurityKeyInfo> infos = new ArrayList<>();
            for (SecurityKey key : securityKeys.getSecurityKey()) {
                SecurityKeyInfo info = new SecurityKeyInfo();
                info.setSecurityAccessorName(extractSecurityAccessorName(key, meter));
                info.setSecurityAccessorKey(extractSecurityAccessorKey(key, meter));
                if (key.getWrapKeyInfo() != null) {
                    info.setPublicKeyLabel(extractPublicKeyLabel(key, meter));
                    info.setSymmetricKey(extractSymmetricKey(key, meter));
                }
                infos.add(info);
            }
            securityInfo.setSecurityKeys(infos);
        }
        return securityInfo;
    }

    private String extractSecurityAccessorName(SecurityKey key, Meter meter) throws FaultMessage {
        return Optional.ofNullable(key.getSecurityAccessorName())
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                        MessageSeeds.MISSING_ELEMENT, METER_CONFIG_SECURITY + SECURITY_ACCESSOR_NAME));
    }

    private String extractPublicKeyLabel(SecurityKey key, Meter meter) throws FaultMessage {
        return Optional.ofNullable(key.getWrapKeyInfo().getLabel())
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                        MessageSeeds.MISSING_ELEMENT, METER_CONFIG_SECURITY + WRAP_KEY_LABEL));
    }

    private byte[] extractSymmetricKey(SecurityKey key, Meter meter) throws FaultMessage {
        return Optional.ofNullable(key.getWrapKeyInfo().getSymmetricKey().getCipherData().getCipherValue())
                .filter(value -> value != null)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                        MessageSeeds.MISSING_ELEMENT, METER_CONFIG_SECURITY + SYMMETRIC_KEY_VALUE));
    }

    private byte[] extractSecurityAccessorKey(SecurityKey key, Meter meter) throws FaultMessage {
        return Optional.ofNullable(key.getSecurityAccessorKey().getCipherData().getCipherValue())
                .filter(value -> value != null)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                        MessageSeeds.MISSING_ELEMENT, METER_CONFIG_SECURITY + SECURITY_ACCESSOR_KEY_VALUE));
    }

    public Optional<String> extractMrid(Meter meter) {
        return Optional.ofNullable(meter.getMRID()).filter(mrid -> !Checks.is(mrid).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractSerialNumber(Meter meter) {
        return Optional.ofNullable(meter.getSerialNumber())
                .filter(serialNumber -> !Checks.is(serialNumber).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractName(List<Name> names) {
        return names.stream().map(Name::getName).filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace()).findFirst();
    }

    public Optional<ProductAssetModel> extractAssetModel(Meter meter) {
        return extractEndDeviceInfo(meter).map(EndDeviceInfo::getAssetModel);
    }

    public Optional<String> extractManufacturer(Meter meter) {
        return extractAssetModel(meter).map(ProductAssetModel::getManufacturer)
                .flatMap(manufacturer -> extractName(manufacturer.getNames()));
    }

    public Optional<String> extractModelNumber(Meter meter) {
        return extractAssetModel(meter).map(ProductAssetModel::getModelNumber)
                .filter(modelNumber -> !Checks.is(modelNumber).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractModelVersion(Meter meter) {
        return extractAssetModel(meter).map(ProductAssetModel::getModelVersion)
                .filter(modelVersion -> !Checks.is(modelVersion).emptyOrOnlyWhiteSpace());
    }

    public Optional<BigDecimal> extractMultiplier(Meter meter) {
        return meter.getMeterMultipliers().stream().map(MeterMultiplier::getValue).filter(Objects::nonNull).findFirst()
                .map(BigDecimal::valueOf);
    }

    public Optional<Status> extractMeterStatus(Meter meter) {
        return Optional.ofNullable(meter.getStatus());
    }

    public Optional<String> extractStatusReason(Meter meter) {
        return extractMeterStatus(meter).map(Status::getReason)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());

    }

    public Optional<String> extractStatusValue(Meter meter) {
        return extractMeterStatus(meter).map(Status::getValue)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    public Optional<Instant> extractStatusEffectiveDate(Meter meter) {
        return extractMeterStatus(meter).map(Status::getDateTime);
    }

    public String extractDeviceConfig(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions)
            throws FaultMessage {
        Optional<String> comFuncReference = extractEndDeviceFunctionRef(meter);
        if (comFuncReference.isPresent()) {
            SimpleEndDeviceFunction endDeviceFunction = endDeviceFunctions.stream()
                    .filter(endDeviceFunc -> comFuncReference.get().equals(endDeviceFunc.getMRID())).findAny()
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                            MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND, "MeterConfig.Meter.SimpleEndDeviceFunction",
                            "MeterConfig.SimpleEndDeviceFunction"));
            return endDeviceFunction.getConfigID();
        }
        return null;
    }

    public String extractDeviceNameForCreate(Meter meter) throws FaultMessage {
        return Stream.of(extractName(meter.getNames()), extractSerialNumber(meter), extractMrid(meter))
                .flatMap(Functions.asStream())
                .findFirst()
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                        MessageSeeds.DEVICE_IDENTIFIER_MISSING));
    }

    public String extractDeviceNameForUpdate(Meter meter) throws FaultMessage {
        return extractName(meter.getNames()).orElse(null);
    }

    public String extractDeviceTypeName(Meter meter) throws FaultMessage {
        return Optional.ofNullable(meter.getType())
                .filter(deviceTypeName -> !Checks.is(deviceTypeName).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                        MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter.type"));
    }

    public Optional<String> extractEndDeviceFunctionRef(Meter meter) throws FaultMessage {
        return meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction()
                .stream()
                .filter(Meter.SimpleEndDeviceFunction.class::isInstance)
                .map(Meter.SimpleEndDeviceFunction.class::cast)
                .map(Meter.SimpleEndDeviceFunction::getRef)
                .filter(ref -> !Checks.is(ref).emptyOrOnlyWhiteSpace())
                .findFirst();
    }

    public Instant extractShipmentDate(Meter meter) throws FaultMessage {
        return Optional.ofNullable(meter.getLifecycle())
                .map(LifecycleDate::getReceivedDate)
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter),
                        MessageSeeds.MISSING_ELEMENT, "MeterConfig.Meter.lifecycle.receivedDate"));
    }

    public Instant extractOptionalShipmentDate(Meter meter) {
        return Optional.ofNullable(meter.getLifecycle())
                .map(LifecycleDate::getReceivedDate)
                .orElse(null);
    }

    public Optional<String> extractBatch(Meter meter) {
        return Optional.ofNullable(meter.getLotNumber())
                .filter(lotNumber -> !Checks.is(lotNumber).emptyOrOnlyWhiteSpace());
    }

    public Optional<String> extractConfigurationReason(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter)
                .map(ConfigurationEvent::getReason)
                .filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace());
    }

    public Optional<Instant> extractConfigurationEffectiveDate(Meter meter) throws FaultMessage {
        return extractConfigurationEvent(meter).map(ConfigurationEvent::getEffectiveDateTime);
    }

    public List<Zone> extractDeviceZones(Meter meter, List<SimpleEndDeviceFunction> endDeviceFunctions) throws FaultMessage {
        Optional<String> comFuncReference = extractEndDeviceFunctionRef(meter);
        if (comFuncReference.isPresent()) {
            SimpleEndDeviceFunction endDeviceFunction = endDeviceFunctions
                    .stream()
                    .filter(endDeviceFunc -> comFuncReference.get().equals(endDeviceFunc.getMRID()))
                    .findAny()
                    .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND,
                            "MeterConfig.Meter.SimpleEndDeviceFunction", "MeterConfig.SimpleEndDeviceFunction"));

            if (endDeviceFunction.getZones() != null) {
                endDeviceFunction.getZones().getZone()
                        .stream()
                        .findAny()
                        .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.EMPTY_LIST,
                                "MeterConfig.SimpleEndDeviceFunction[" + endDeviceFunctions.indexOf(endDeviceFunction) + "].Zones"));

                if (endDeviceFunction.getZones().getZone().size() != endDeviceFunction.getZones().getZone().stream().map(Zone::getZoneType).distinct().count()) {
                    throw faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.IS_NOT_ALLOWED_TO_HAVE_DUPLICATED_ZONE_TYPES).get();
                }

                if (endDeviceFunction.getZones().getZone().stream().anyMatch(zone -> zone.getZoneName() == null || zone.getZoneName().isEmpty())) {
                    throw faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND_OR_EMPTY,
                            "MeterConfig.Meter.SimpleEndDeviceFunction.Zones.Zone.zoneName").get();
                }

                if (endDeviceFunction.getZones().getZone().stream().anyMatch(zone -> zone.getZoneType() == null || zone.getZoneType().isEmpty())) {
                    throw faultMessageFactory.meterConfigFaultMessageSupplier(getMeterName(meter), MessageSeeds.ELEMENT_BY_REFERENCE_NOT_FOUND_OR_EMPTY,
                            "MeterConfig.Meter.SimpleEndDeviceFunction.Zones.Zone.zoneType").get();
                }

                return endDeviceFunction.getZones().getZone();
            }
        }
        return Collections.emptyList();
    }

    private Optional<EndDeviceInfo> extractEndDeviceInfo(Meter meter) {
        return Optional.ofNullable(meter.getEndDeviceInfo());
    }

    private Optional<ConfigurationEvent> extractConfigurationEvent(Meter meter) {
        return Optional.ofNullable(meter.getConfigurationEvents());
    }

    private String extractCpsId(String meterName, int customPropertySetIndex, CustomAttributeSet cas)
            throws FaultMessage {
        return Optional.ofNullable(cas.getId()).filter(id -> !Checks.is(id).emptyOrOnlyWhiteSpace()).orElseThrow(
                faultMessageFactory.meterConfigFaultMessageSupplier(meterName, MessageSeeds.MISSING_ELEMENT,
                        METER_CONFIG_CUSTOM_ATTRIBUTE_SET_PREFIX + customPropertySetIndex + "].id"));
    }

    private String extractCpsAttributeName(String meterName, int customPropertySetIndex, com.elster.connexo._2017.schema.customattributes.Attribute attribute,
                                           int attributeIndex) throws FaultMessage {
        return Optional.ofNullable(attribute.getName()).filter(name -> !Checks.is(name).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meterName,
                        MessageSeeds.MISSING_ELEMENT, METER_CONFIG_CUSTOM_ATTRIBUTE_SET_PREFIX + customPropertySetIndex
                                + "].Attribute[" + attributeIndex + "].name"));
    }

    private String extractCpsAttributeValue(String meterName, int customPropertySetIndex, com.elster.connexo._2017.schema.customattributes.Attribute attribute,
                                            int attributeIndex) throws FaultMessage {
        return Optional.ofNullable(attribute.getValue()).filter(value -> !Checks.is(value).emptyOrOnlyWhiteSpace())
                .orElseThrow(faultMessageFactory.meterConfigFaultMessageSupplier(meterName,
                        MessageSeeds.MISSING_ELEMENT, METER_CONFIG_CUSTOM_ATTRIBUTE_SET_PREFIX + customPropertySetIndex
                                + "].Attribute[" + attributeIndex + "].value"));
    }

    private String getMeterName(Meter meter) {
        return meter.getNames().stream().findFirst().map(Name::getName).orElse(null);
    }
}
