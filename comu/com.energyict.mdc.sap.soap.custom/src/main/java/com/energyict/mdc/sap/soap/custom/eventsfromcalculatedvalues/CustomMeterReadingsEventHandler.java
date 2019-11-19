/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
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
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.time.DefaultDateTimeFormatters;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.custom.TranslationInstaller;
import com.energyict.mdc.sap.soap.custom.eventhandlers.CustomSAPDeviceEventHandler;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.Unit;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.cbo.ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATT;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;


@Component(name = CustomMeterReadingsEventHandler.NAME, service = TopicHandler.class, immediate = true)
public class CustomMeterReadingsEventHandler implements TopicHandler {
    static final String NAME = "com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.CustomMeterReadingsEventHandler";

    private static final Logger LOGGER = Logger.getLogger(CustomMeterReadingsEventHandler.class.getName());
    private static final String POWER_FACTOR_EVENT_CODE = "powerFactor";
    private static final String MAX_DEMAND_EVENT_CODE = "maxDemand";
    private static final String CT_RATIO_EVENT_CODE = "ctRatio";

    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile CustomSAPDeviceEventHandler handler;
    private volatile Thesaurus thesaurus;

    public static final String SAP_CALCULATEDEVENTS_POWERFACTOR = "sap.calculatedevents.powerfactor";
    public static final String SAP_CALCULATEDEVENTS_MAXDEMAND = "sap.calculatedevents.maxdemand";
    public static final String SAP_CALCULATEDEVENTS_CTRATIO = "sap.calculatedevents.ctratio";
    public static final String COLON_SEPARATOR = ":";
    public static final String SEMICOLON_SEPARATOR = ";";
    public static final String COMMA_SEPARATOR = ",";

    protected Map<String, Pair<String, String>> powerFactorEventReadingTypes = Collections.emptyMap();
    protected Map<String, String> maxDemandEventReadingTypes = Collections.emptyMap();
    protected Map<String, String> ctRatioEventReadingTypes = Collections.emptyMap();

    // For OSGi purposes
    public CustomMeterReadingsEventHandler() {
        super();
    }

    @Inject
    public CustomMeterReadingsEventHandler(EventService eventService, MeteringService meteringService,
                                           DeviceService deviceService, CustomPropertySetService customPropertySetService,
                                           CustomSAPDeviceEventHandler handler, Thesaurus thesaurus) {
        this();
        setEventService(eventService);
        setMeteringService(meteringService);
        setDeviceService(deviceService);
        setCustomPropertySetService(customPropertySetService);
        setCustomSAPDeviceEventHandler(handler);
        this.thesaurus = thesaurus;
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        Optional<String> property = getPropertyValue(bundleContext, SAP_CALCULATEDEVENTS_POWERFACTOR);
        if (property.isPresent()) {
            powerFactorEventReadingTypes = parsePowerFactorReadingTypes(SAP_CALCULATEDEVENTS_POWERFACTOR, property.get());
        }

        property = getPropertyValue(bundleContext, SAP_CALCULATEDEVENTS_MAXDEMAND);
        if (property.isPresent()) {
            maxDemandEventReadingTypes = parseReadingTypes(SAP_CALCULATEDEVENTS_MAXDEMAND, property.get(), WATT);
        }

        property = getPropertyValue(bundleContext, SAP_CALCULATEDEVENTS_CTRATIO);
        if (property.isPresent()) {
            ctRatioEventReadingTypes = parseReadingTypes(SAP_CALCULATEDEVENTS_CTRATIO, property.get(), null);
        }
    }

    @Override
    public void handle(LocalEvent localEvent) {
        try {
            // map Meter to Map of Reading Type to List of readings
            Map<Meter, Map<String, List<ReadingInfo>>> readings = ((ReadingStorer) localEvent.getSource()).getReadings().stream()
                    .filter(r -> r.getMeter().isPresent())
                    .collect(Collectors.groupingBy(r -> r.getMeter().get(), Collectors.groupingBy(r -> r.getReadingType().getMRID())));
            for (Map.Entry<Meter, Map<String, List<ReadingInfo>>> readingsByMeter : readings.entrySet()) {
                Optional<Device> device = deviceService.findDeviceByMeterId(readingsByMeter.getKey().getId());
                if (device.isPresent()) {
                    String deviceType = device.get().getDeviceType().getName();
                    // power factor events
                    Pair<String, String> pfReadingTypes = powerFactorEventReadingTypes.get(deviceType);
                    if (pfReadingTypes != null) {
                        Optional<Pair<BigDecimal, BigDecimal>> powerFactorCASValues = getPowerFactorCASValues(device.get());
                        if (powerFactorCASValues.isPresent()) {
                            generatePowerFactorEvents(device.get(), readingsByMeter.getValue().getOrDefault(pfReadingTypes.getFirst(), new ArrayList<>()),
                                    readingsByMeter.getValue().getOrDefault(pfReadingTypes.getLast(), new ArrayList<>()),
                                    powerFactorCASValues.get().getFirst(), powerFactorCASValues.get().getLast());
                        }
                    }
                    // max demand events
                    String mdReadingType = maxDemandEventReadingTypes.get(deviceType);
                    if (mdReadingType != null) {
                        Optional<Pair<BigDecimal, String>> maxDemandCASValues = getMaxDemandCASValues(device.get());
                        if (maxDemandCASValues.isPresent()) {
                            generateMaxDemandEvents(readingsByMeter.getValue().getOrDefault(mdReadingType, new ArrayList<>()),
                                    maxDemandCASValues.get().getFirst(), maxDemandCASValues.get().getLast());
                        }
                    }
                    // ct ratio events
                    String ctrReadingType = ctRatioEventReadingTypes.get(deviceType);
                    if (ctrReadingType != null) {
                        Optional<BigDecimal> ctRatioCASValues = getCtRatioCASValues(device.get());
                        if (ctRatioCASValues.isPresent()) {
                            generateCtRatioEvents(readingsByMeter.getValue().getOrDefault(ctrReadingType, new ArrayList<>()),
                                    ctRatioCASValues.get());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private Optional<Pair<BigDecimal, BigDecimal>> getPowerFactorCASValues(Device device) {
        Optional<CustomPropertySet> cps = getCAS(device, PowerFactorCustomPropertySet.CPS_ID);
        if (cps.isPresent()) {
            CustomPropertySetValues values = customPropertySetService.getUniqueValuesFor(cps.get(), device);
            if ((boolean) values.getProperty(PowerFactorDomainExtension.FieldNames.CHECK_ENABLED.javaName())) {
                return Optional.of(Pair.of(
                        (BigDecimal) values.getProperty(PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName()),
                        (BigDecimal) values.getProperty(PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName())));
            }
        }
        return Optional.empty();
    }

    private void generatePowerFactorEvents(Device device, List<ReadingInfo> activeReadings, List<ReadingInfo> reactiveReadings, BigDecimal setpointThreshold, BigDecimal hysteresisPercentage) {
        activeReadings.sort(Comparator.comparing(rI -> rI.getReading().getTimeStamp()));
        for (ReadingInfo reading : activeReadings) {
            Optional<ReadingInfo> reactiveReading = reactiveReadings.stream().filter(r -> r.getReading().getTimeStamp().equals(reading.getReading().getTimeStamp())).findFirst();
            if (reactiveReading.isPresent()) {
                double value = reading.getReading().getValue().scaleByPowerOfTen(reading.getReadingType().getMultiplier().getMultiplier()).doubleValue();
                double reactiveValue = reactiveReading.get().getReading().getValue()
                        .scaleByPowerOfTen(reactiveReading.get().getReadingType().getMultiplier().getMultiplier()).doubleValue();
                if (value == 0 && reactiveValue == 0) {
                    MessageSeeds.POWER_FACTOR_VALUES_ARE_NULL.log(LOGGER, thesaurus, device.getName(),
                            reading.getReadingType().getMRID() + ";" + reactiveReading.get().getReadingType().getMRID(),
                            DefaultDateTimeFormatters.shortDate().withShortTime().build().format(reading.getReading().getTimeStamp().atZone(ZoneId.systemDefault())));
                } else {
                    if (isSetpointThresholdViolated(value, reactiveValue, setpointThreshold.doubleValue(), hysteresisPercentage.doubleValue())) {
                        // generate event
                        sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), POWER_FACTOR_EVENT_CODE, EndDeviceDomain.POWER);
                    }
                }
                reactiveReadings.remove(reactiveReading.get());
            } else {
                MessageSeeds.POWER_FACTOR_MISSING_READING.log(LOGGER, thesaurus, device.getName(),
                        powerFactorEventReadingTypes.get(device.getDeviceType().getName()).getLast(),
                        DefaultDateTimeFormatters.shortDate().withShortTime().build().format(reading.getReading().getTimeStamp().atZone(ZoneId.systemDefault())));
            }
        }
        // generate error log for remaining reactive readings that do not have the corresponding active reading
        reactiveReadings.sort(Comparator.comparing(rI -> rI.getReading().getTimeStamp()));
        for (ReadingInfo reading : reactiveReadings) {
            MessageSeeds.POWER_FACTOR_MISSING_READING.log(LOGGER, thesaurus, device.getName(),
                    powerFactorEventReadingTypes.get(device.getDeviceType().getName()).getFirst(),
                    DefaultDateTimeFormatters.shortDate().withShortTime().build().format(reading.getReading().getTimeStamp().atZone(ZoneId.systemDefault())));
        }
    }

    private Optional<Pair<BigDecimal, String>> getMaxDemandCASValues(Device device) {
        Optional<CustomPropertySet> cps = getCAS(device, MaxDemandCustomPropertySet.CPS_ID);
        if (cps.isPresent()) {
            CustomPropertySetValues values = customPropertySetService.getUniqueValuesFor(cps.get(), device);
            if ((boolean) values.getProperty(MaxDemandDomainExtension.FieldNames.CHECK_ENABLED.javaName())) {
                return Optional.of(Pair.of(
                        (BigDecimal) values.getProperty(MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName()),
                        ((Unit) values.getProperty(MaxDemandDomainExtension.FieldNames.UNIT.javaName())).getValue()));
            }
        }
        return Optional.empty();
    }

    private void generateMaxDemandEvents(List<ReadingInfo> readings, BigDecimal connectedLoad, String unit) {
        readings.sort(Comparator.comparing(rI -> rI.getReading().getTimeStamp()));
        for (ReadingInfo reading : readings) {
            double value = reading.getReading().getValue().scaleByPowerOfTen(reading.getReadingType().getMultiplier().getMultiplier()).doubleValue();
            if (isMaxDemandExceeded(value, connectedLoad.doubleValue(), unit)) {
                // generate event
                sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), MAX_DEMAND_EVENT_CODE, EndDeviceDomain.DEMAND);
            }
        }
    }

    private Optional<BigDecimal> getCtRatioCASValues(Device device) {
        Optional<CustomPropertySet> cps = getCAS(device, CTRatioCustomPropertySet.CPS_ID);
        if (cps.isPresent()) {
            CustomPropertySetValues values = customPropertySetService.getUniqueValuesFor(cps.get(), device);
            if ((boolean) values.getProperty(CTRatioDomainExtension.FieldNames.CHECK_ENABLED.javaName())) {
                return Optional.of((BigDecimal) values.getProperty(CTRatioDomainExtension.FieldNames.CT_RATIO.javaName()));
            }
        }
        return Optional.empty();
    }

    private void generateCtRatioEvents(List<ReadingInfo> readings, BigDecimal ctRatio) {
        readings.sort(Comparator.comparing(rI -> rI.getReading().getTimeStamp()));
        for (ReadingInfo reading : readings) {
            if (!isEqualToCTRatio(reading.getReading().getValue().doubleValue(), ctRatio.doubleValue())) {
                // generate event
                sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), CT_RATIO_EVENT_CODE, EndDeviceDomain.NA);
            }
        }
    }

    boolean validatePowerFactorReadingTypes(String deviceTypeName, String readingType, String reactiveReadingType) {
        TimeAttribute defaultMeasurementPeriod = null;
        for (String rType : Arrays.asList(readingType, reactiveReadingType)) {
            Optional<ReadingType> readingTypeRef = meteringService.getReadingType(rType);
            if (!readingTypeRef.isPresent()) {
                MessageSeeds.READING_TYPE_IS_NOT_FOUND.log(LOGGER, thesaurus, rType);
                return false;
            }
            if ((rType.equals(readingType) && !readingTypeRef.get().getUnit().equals(WATTHOUR)) ||
                    (rType.equals(reactiveReadingType) && !readingTypeRef.get().getUnit().equals(VOLTAMPEREREACTIVEHOUR))) {
                MessageSeeds.UNEXPECTED_UNIT_FOR_READING_TYPE.log(LOGGER, thesaurus, readingType);
                return false;
            }
            if (!readingTypeRef.get().isRegular()) {
                MessageSeeds.POWER_FACTOR_INVALID_READING_TYPE.log(LOGGER, thesaurus, deviceTypeName);
                return false;
            }
            TimeAttribute measuringPeriod = readingTypeRef.get().getMeasuringPeriod();
            if (defaultMeasurementPeriod != null && measuringPeriod != defaultMeasurementPeriod) {
                MessageSeeds.POWER_FACTOR_READING_TYPES_MUST_HAVE_THE_SAME_INTERVAL.log(LOGGER, thesaurus, readingType + ";" + reactiveReadingType);
                return false;
            }
            defaultMeasurementPeriod = measuringPeriod;
        }
        return true;
    }

    boolean validateReadingType(String readingType, ReadingTypeUnit unit) {
        Optional<ReadingType> readingTypeRef = meteringService.getReadingType(readingType);
        if (!readingTypeRef.isPresent()) {
            MessageSeeds.READING_TYPE_IS_NOT_FOUND.log(LOGGER, thesaurus, readingType);
            return false;
        }
        if (unit != null && !readingTypeRef.get().getUnit().equals(unit)) {
            MessageSeeds.UNEXPECTED_UNIT_FOR_READING_TYPE.log(LOGGER, thesaurus, readingType);
            return false;
        }
        return true;
    }

    @Override
    public String getTopicMatcher() {
        return EventType.READINGS_CREATED.topic();
    }

    public boolean isSetpointThresholdViolated(double realPower, double reactivePower, double setpointThreshold, double hysteresisPercentage) {
        double powerFactor = realPower / Math.sqrt(Math.pow(realPower, 2) + Math.pow(reactivePower, 2));
        double delta = Math.abs(setpointThreshold) * hysteresisPercentage / 100.0;
        if (powerFactor <= setpointThreshold - delta || powerFactor > setpointThreshold + delta) {
            return true;
        }
        return false;
    }

    public boolean isMaxDemandExceeded(double value, double connectedLoad, String unit) {
        if (unit.equals(Unit.kW.getValue())) {
            connectedLoad = connectedLoad * 1000;
        } else if (unit.equals(Unit.MW.getValue())) {
            connectedLoad = connectedLoad * 1000000;
        }
        return value > connectedLoad;
    }

    public boolean isEqualToCTRatio(double value, double ctRatio) {
        return value == ctRatio;
    }

    public Optional<CustomPropertySet> getCAS(Device device, String cpsId) {
        return device.getDeviceType().getCustomPropertySets().stream().map(cps -> cps.getCustomPropertySet()).filter(c -> c.getId().equals(cpsId)).findFirst();

    }

    public void sendEvent(Meter meter, Instant date, String code, EndDeviceDomain domain) {
        CalculatedEventRecordImpl eventRecord = new CalculatedEventRecordImpl(meter, code, date, domain);
        handler.handle(eventRecord);
    }

    private Optional<String> getPropertyValue(BundleContext context, String propertyName) {
        Optional<String> value = getValidValue(context.getProperty(propertyName));
        if (!value.isPresent()) {
            LOGGER.log(Level.WARNING, MessageSeeds.PROPERTY_IS_NOT_SET.getDefaultFormat(), propertyName);
        }
        return value;
    }

    private Optional<String> getValidValue(String value) {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            return Optional.empty();
        }
        return Optional.of(value.trim());
    }

    private Map<String, Pair<String, String>> parsePowerFactorReadingTypes(String name, String value) {
        Map<String, Pair<String, String>> map = new HashMap<>();
        List<String> list = Arrays.asList(value.split(SEMICOLON_SEPARATOR));
        if (list.size() > 0) {
            for (String item : list) {
                String[] params = item.split(COLON_SEPARATOR);
                if (params.length == 2) {
                    String[] readingTypes = params[1].split(COMMA_SEPARATOR);
                    if (readingTypes.length == 2) {
                        Optional<String> deviceType = getValidValue(params[0]);
                        Optional<String> activeReadingType = getValidValue(readingTypes[0]);
                        Optional<String> reactiveReadingType = getValidValue(readingTypes[1]);
                        if (deviceType.isPresent() && activeReadingType.isPresent() && reactiveReadingType.isPresent()) {
                            if (validatePowerFactorReadingTypes(deviceType.get(), activeReadingType.get(), reactiveReadingType.get())) {
                                map.put(deviceType.get(), Pair.of(activeReadingType.get(), reactiveReadingType.get()));
                            }
                        } else {
                            LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_VALUE_CANNOT_BE_EMPTY.getDefaultFormat(), name);
                        }
                    } else {
                        LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID.getDefaultFormat(), name);
                    }
                } else {
                    LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID.getDefaultFormat(), name);
                }
            }
        } else {
            LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID.getDefaultFormat(), name);
        }
        return map;
    }

    private Map<String, String> parseReadingTypes(String name, String value, ReadingTypeUnit unit) {
        Map<String, String> map = new HashMap<>();
        String[] list = value.split(SEMICOLON_SEPARATOR);
        if (list.length > 0) {
            for (String item : list) {
                String[] params = item.split(COLON_SEPARATOR);
                if (params.length == 2) {
                    Optional<String> deviceType = getValidValue(params[0]);
                    Optional<String> readingType = getValidValue(params[1]);
                    if (deviceType.isPresent() && readingType.isPresent()) {
                        if (validateReadingType(readingType.get(), unit)) {
                            map.put(deviceType.get(), readingType.get());
                        }
                    } else {
                        LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_VALUE_CANNOT_BE_EMPTY.getDefaultFormat(), name);
                    }
                } else {
                    LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID.getDefaultFormat(), name);
                }
            }
        } else {
            LOGGER.log(Level.SEVERE, MessageSeeds.PROPERTY_VALUE_FORMAT_IS_INVALID.getDefaultFormat(), name);
        }
        return map;
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

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setCustomSAPDeviceEventHandler(CustomSAPDeviceEventHandler handler) {
        this.handler = handler;
    }

    @Reference
    public void setThesaurus(TranslationInstaller translationInstaller) {
        this.thesaurus = translationInstaller.getThesaurus();
    }
}
