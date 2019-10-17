/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues;

import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cim.webservices.outbound.soap.SendMeterReadingsProvider;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.events.TopicHandler;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingInfo;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.custom.MessageSeeds;
import com.energyict.mdc.sap.soap.custom.eventhandlers.TranslationKeys;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CustomPropertySets;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorDomainExtension;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.cbo.ReadingTypeUnit.WATT;


@Component(name = CustomMeterReadingsEventHandler.NAME, service = TopicHandler.class, immediate = true)
public class CustomMeterReadingsEventHandler implements TopicHandler, TranslationKeyProvider {
    static final String COMPONENT_NAME = "CME";
//    static final String APPLICATION_NAME = "MultiSense";
    static final String NAME = "com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.CustomMeterReadingsEventHandler";

    private static final String POWER_FACTOR_EVENET_CODE = "8001";
    private static final String MAX_DEMAND_EVENET_CODE = "8002";
    private static final String CT_RATIO_EVENET_CODE = "8003";

    private static final Logger LOGGER = Logger.getLogger(CustomMeterReadingsEventHandler.class.getName());
    private volatile SendMeterReadingsProvider sendMeterReadingsProvider;
    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    private final SimpleDateFormat formatter = new SimpleDateFormat("dd MMM ''yy 'at' HH:mm");

    // For OSGi purposes
    public CustomMeterReadingsEventHandler() {
        super();
    }

    @Inject
    public CustomMeterReadingsEventHandler(Thesaurus thesaurus, MeteringService meteringService, SendMeterReadingsProvider sendMeterReadingsProvider) {
        this();
        setSendMeterReadingsProvider(sendMeterReadingsProvider);
        this.thesaurus = thesaurus;
        this.meteringService = meteringService;
    }

    @Reference
    public void setSendMeterReadingsProvider(SendMeterReadingsProvider sendMeterReadingsProvider) {
        this.sendMeterReadingsProvider = sendMeterReadingsProvider;
    }

    @Override
    public void handle(LocalEvent localEvent) {
        // meter id to device
        Map<Long, Device> meterIds = new HashMap<>();

        // CAS values per device
        Map<Device, Pair<BigDecimal, BigDecimal>> powerFactorCASValues = new HashMap<>();
        Map<Device, Pair<BigDecimal, String>> maxDemandCASValues = new HashMap<>();
        Map<Device, BigDecimal> ctRatioCASValues = new HashMap<>();

        // device type to config CIM codes of reading types
        Map<String, Pair<String, String>> powerFactorDeviceTypes = new HashMap<>();
        Map<String, String> maxDemandDeviceTypes = new HashMap<>();
        Map<String, String> ctRatioDeviceTypes = new HashMap<>();

        // readings selected for events
        Map<Pair<Device, String>, List<ReadingInfo>> matchedReadings = new HashMap<>();

        try {
            ReadingStorer readingStorer = (ReadingStorer) localEvent.getSource();
            for (ReadingInfo reading : readingStorer.getReadings()) {
                if (reading.getMeter().isPresent()) {
                    long meterId = reading.getMeter().get().getId();
                    Optional<Device> deviceOpt = Optional.ofNullable(meterIds.get(meterId));
                    if (!deviceOpt.isPresent()) {
                        deviceOpt = deviceService.findDeviceByMeterId(meterId);
                        if (deviceOpt.isPresent()) {
                            Device device = deviceOpt.get();
                            DeviceType deviceType = device.getDeviceType();
                            meterIds.put(meterId, device);
                            boolean isMatchedReading = false;
                            // check for power factor
                            Optional<Pair<String, String>> readingTypes = Optional.ofNullable(powerFactorDeviceTypes.get(deviceType));
                            if (!readingTypes.isPresent()) {
                                readingTypes = Optional.ofNullable(getReadingTypesForPowerFactorEvent(deviceType));
                                if (readingTypes.isPresent() && isCASAssigned(deviceType, PowerFactorCustomPropertySet.CPS_ID)) {
                                    powerFactorDeviceTypes.put(deviceType.getName(), readingTypes.get());
                                }
                            }
                            readingTypes = Optional.ofNullable(powerFactorDeviceTypes.get(deviceType));
                            if (readingTypes.isPresent() && (reading.getReadingType().getFullAliasName().equals(readingTypes.get().getFirst())
                                    || reading.getReadingType().getFullAliasName().equals(readingTypes.get().getLast()))) {
                                Optional<CustomPropertySet> cps = getCAS(device, PowerFactorCustomPropertySet.CPS_ID);
                                if (powerFactorCASValues.get(device) == null) {
                                    if ((boolean) getValue(cps.get(), device, PowerFactorDomainExtension.FieldNames.FLAG.javaName())) {
                                        powerFactorCASValues.put(device, Pair.of(
                                                (BigDecimal) getValue(cps.get(), device, PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName()),
                                                (BigDecimal) getValue(cps.get(), device, PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName())));
                                        isMatchedReading = true;
                                    }
                                } else {
                                    isMatchedReading = true;
                                }
                            }

                            // check for max demand
                            Optional<String> readingType = Optional.ofNullable(maxDemandDeviceTypes.get(deviceType));
                            if (!readingType.isPresent()) {
                                readingType = Optional.ofNullable(getReadingTypesForMaxDemandEvent(deviceType));
                                if (readingType.isPresent() && isCASAssigned(deviceType, MaxDemandCustomPropertySet.CPS_ID)) {
                                    maxDemandDeviceTypes.put(deviceType.getName(), readingType.get());
                                }
                            }
                            readingType = Optional.ofNullable(maxDemandDeviceTypes.get(deviceType));
                            if (readingType.isPresent() && (reading.getReadingType().getFullAliasName().equals(readingType.get()))) {
                                Optional<CustomPropertySet> cps = getCAS(device, MaxDemandCustomPropertySet.CPS_ID);
                                if (maxDemandCASValues.get(device) == null) {
                                    if ((boolean) getValue(cps.get(), device, MaxDemandDomainExtension.FieldNames.FLAG.javaName())) {
                                        maxDemandCASValues.put(device, Pair.of(
                                                (BigDecimal) getValue(cps.get(), device, MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName()),
                                                (String) getValue(cps.get(), device, MaxDemandDomainExtension.FieldNames.UNIT.javaName())));
                                        isMatchedReading = true;
                                    }
                                } else {
                                    isMatchedReading = true;
                                }
                            }

                            // check for ct ratio
                            readingType = Optional.ofNullable(ctRatioDeviceTypes.get(deviceType));
                            if (!readingType.isPresent()) {
                                readingType = Optional.ofNullable(getReadingTypesForCTRatioEvent(deviceType));
                                if (readingType.isPresent() && isCASAssigned(deviceType, CTRatioCustomPropertySet.CPS_ID)) {
                                    ctRatioDeviceTypes.put(deviceType.getName(), readingType.get());
                                }
                            }
                            readingType = Optional.ofNullable(ctRatioDeviceTypes.get(deviceType));
                            if (readingType.isPresent() && (reading.getReadingType().getFullAliasName().equals(readingType.get()))) {
                                Optional<CustomPropertySet> cps = getCAS(device, CTRatioCustomPropertySet.CPS_ID);
                                if (ctRatioCASValues.get(device) == null) {
                                    if ((boolean) getValue(cps.get(), device, CTRatioDomainExtension.FieldNames.FLAG.javaName())) {
                                        ctRatioCASValues.put(device, (BigDecimal) getValue(cps.get(), device, CTRatioDomainExtension.FieldNames.CT_RATIO.javaName()));
                                        isMatchedReading = true;
                                    }
                                } else {
                                    isMatchedReading = true;
                                }
                            }

                            // add reading to consider for calculated events
                            if (isMatchedReading) {
                                String cimCode = reading.getReadingType().getFullAliasName(); // cim code
                                List<ReadingInfo> list = matchedReadings.getOrDefault(Pair.of(device, cimCode), new ArrayList<>());
                                list.add(reading);
                                matchedReadings.put(Pair.of(device, cimCode), list);
                            }
                        }
                    }
                }
            }

            for (Map.Entry<Pair<Device, String>, List<ReadingInfo>> entry : matchedReadings.entrySet()) {
                Device device = entry.getKey().getFirst();
                String deviceTypeName = device.getDeviceType().getName();

                // generate power factor events
                Optional<Pair<String, String>> readingTypes = Optional.ofNullable(powerFactorDeviceTypes.get(deviceTypeName));
                if (readingTypes.isPresent()) {
                    if (entry.getKey().getLast().equals(readingTypes.get().getFirst())) {
                        if (!entry.getValue().get(0).getReadingType().isRegular()) {
                            MessageSeeds.POWER_FACTOR_INVALID_READING_TYPE.log(LOGGER, thesaurus, deviceTypeName);
                        } else {
                            List<ReadingInfo> reactiveReadings = matchedReadings.getOrDefault(Pair.of(device, readingTypes.get().getLast()), new ArrayList<>());
                            for (ReadingInfo reading : entry.getValue()) {
                                Optional<ReadingInfo> reactiveReading = reactiveReadings.stream().filter(r -> r.getReading().getTimeStamp().equals(reading.getReading().getTimeStamp())).findFirst();
                                if (reactiveReading.isPresent()) {
                                    if (!reactiveReading.get().getReadingType().isRegular()) {
                                        MessageSeeds.POWER_FACTOR_INVALID_READING_TYPE.log(LOGGER, thesaurus, deviceTypeName);
                                    } else {
                                        double value = reading.getReading().getValue().doubleValue();
                                        double reactiveValue = reactiveReading.get().getReading().getValue().doubleValue();
                                        if (value == 0 && reactiveValue == 0) {
                                            MessageSeeds.POWER_FACTOR_VALUES_ARE_NULL.log(LOGGER, thesaurus, device.getName(),
                                                    reading.getReadingType().getFullAliasName() + "; " + reactiveReading.get().getReadingType().getFullAliasName(),
                                                    formatter.format(reading.getReading().getTimeStamp()));
                                        } else {
                                            if (powerFactorEvent(value, reactiveValue, powerFactorCASValues.get(device).getFirst().doubleValue(), powerFactorCASValues.get(device).getLast().doubleValue())) {
                                                // generate event
                                                sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), POWER_FACTOR_EVENET_CODE);
                                            }
                                        }
                                    }
                                } else {
                                    MessageSeeds.POWER_FACTOR_MISSING_READING.log(LOGGER, thesaurus, device.getName(),
                                            reading.getReadingType().getFullAliasName(), formatter.format(reading.getReading().getTimeStamp()));
                                }
                            }
                        }
                    }
                }

                // generate max demand events
                Optional<String> readingType = Optional.ofNullable(maxDemandDeviceTypes.get(deviceTypeName));
                if (readingTypes.isPresent()) {
                    if (entry.getKey().getLast().equals(readingType.get())) {
                        for (ReadingInfo reading : entry.getValue()) {
                            ReadingTypeUnit readingTypeUnit = reading.getReadingType().getUnit();
                            if (readingTypeUnit.equals(WATT)) {
                                if (maxDemandEvent(reading.getReading().getValue().doubleValue(), readingTypeUnit,
                                        maxDemandCASValues.get(device).getFirst().doubleValue(), maxDemandCASValues.get(device).getLast())) {
                                    // generate event
                                    sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), MAX_DEMAND_EVENET_CODE);
                                }
                            } else {
                                MessageSeeds.UNEXPECTED_UNIT_ON_READING_TYPE.log(LOGGER, thesaurus,
                                        reading.getReadingType().getFullAliasName(), formatter.format(reading.getReading().getTimeStamp()));

                            }
                        }
                    }
                }

                // generate ct ratio events
                readingType = Optional.ofNullable(ctRatioDeviceTypes.get(deviceTypeName));
                if (readingTypes.isPresent()) {
                    if (entry.getKey().getLast().equals(readingType.get())) {
                        for (ReadingInfo reading : entry.getValue()) {
                            if (ctRatioEvent(reading.getReading().getValue().doubleValue(), ctRatioCASValues.get(device).doubleValue())) {
                                // generate event
                                sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), CT_RATIO_EVENET_CODE);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public String getTopicMatcher() {
        return EventType.READINGS_CREATED.topic();
    }

    public boolean powerFactorEvent(double realPower, double reactivePower, double setpointThreshold,
                                    double hysteresisPercentage) {
        double powerFactor = realPower / Math.sqrt(Math.pow(realPower, 2) + Math.pow(reactivePower, 2));
        double deltaPercentage = Math.abs(setpointThreshold) * hysteresisPercentage / 100;
        if (powerFactor <= setpointThreshold - deltaPercentage || powerFactor > setpointThreshold + deltaPercentage) {
            return true;
        }
        return false;
    }

    public boolean maxDemandEvent(double value, ReadingTypeUnit readingTypeUnit, double connectedLoad, String unit) {
        if (unit.equals(Units.kW.getValue())) {
            value = value / 1000;
        } else if (unit.equals(Units.MW.getValue())) {
            value = value / 1000000;
        }
        return value > connectedLoad;
    }

    public boolean ctRatioEvent(double value, double ctRatio) {
        return value != ctRatio;
    }

    public boolean isCASAssigned(DeviceType deviceType, String cpsId) {
        return deviceType.getCustomPropertySets().stream().anyMatch(cps -> cps.getCustomPropertySet().getId().equals(cpsId));
    }

    public Optional<CustomPropertySet> getCAS(Device device, String cpsId) {
        return device.getDeviceType().getCustomPropertySets().stream().map(cps -> cps.getCustomPropertySet()).filter(c -> c.getId().equals(cpsId)).findFirst();

    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }


    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public CustomPropertySetValues getValues(CustomPropertySet customPropertySet, Device device) {
        return customPropertySetService.getUniqueValuesFor(customPropertySet, device);
    }

    public Object getValue(CustomPropertySet customPropertySet, Device device, String propertyName) {
        CustomPropertySetValues values = customPropertySetService.getUniqueValuesFor(customPropertySet, device);
        return values.getProperty(propertyName);
    }

    public Pair<String, String> getReadingTypesForPowerFactorEvent(DeviceType deviceType) {
        return CustomPropertySets.getPowerFactorEventReadingTypes().get(deviceType.getName());
    }

    public String getReadingTypesForMaxDemandEvent(DeviceType deviceType) {
        return CustomPropertySets.getMaxDemandEventReadingTypes().get(deviceType.getName());
    }

    public String getReadingTypesForCTRatioEvent(DeviceType deviceType) {
        return CustomPropertySets.getCTRatioEventReadingTypes().get(deviceType.getName());
    }

    public void sendEvent(Meter meter, Instant date, String code) {
        CalculatedEventRecordImpl eventRecord = new CalculatedEventRecordImpl(meter, meteringService.createEndDeviceEventType(code), date);
        eventService.postEvent(EventType.END_DEVICE_EVENT_CREATED.topic(), eventRecord);
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.SOAP;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(TranslationKeys.values());
    }
}
