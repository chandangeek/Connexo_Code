/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues;

import com.elster.jupiter.cbo.EndDeviceDomain;
import com.elster.jupiter.cbo.MetricMultiplier;
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
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CTRatioDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.CustomPropertySets;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.MaxDemandDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorCustomPropertySet;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.PowerFactorDomainExtension;
import com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.custompropertyset.Units;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.elster.jupiter.cbo.MetricMultiplier.KILO;
import static com.elster.jupiter.cbo.MetricMultiplier.MEGA;
import static com.elster.jupiter.cbo.ReadingTypeUnit.VOLTAMPEREREACTIVEHOUR;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATT;
import static com.elster.jupiter.cbo.ReadingTypeUnit.WATTHOUR;
import static java.util.stream.Collectors.groupingBy;


@Component(name = CustomMeterReadingsEventHandler.NAME, service = TopicHandler.class, immediate = true)
public class CustomMeterReadingsEventHandler implements TopicHandler {
    static final String NAME = "com.energyict.mdc.sap.soap.custom.eventsfromcalculatedvalues.CustomMeterReadingsEventHandler";

    private static final Logger LOGGER = Logger.getLogger(CustomMeterReadingsEventHandler.class.getName());
    protected static final String POWER_FACTOR_EVENET_CODE = "powerFactor";
    protected static final String MAX_DEMAND_EVENET_CODE = "maxDemand";
    protected static final String CT_RATIO_EVENET_CODE = "ctRatio";

    private volatile DeviceService deviceService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile EventService eventService;
    private volatile MeteringService meteringService;
    private volatile Thesaurus thesaurus;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM ''yy 'at' HH:mm");

    // For OSGi purposes
    public CustomMeterReadingsEventHandler() {
        super();
    }

    @Inject
    public CustomMeterReadingsEventHandler(EventService eventService, MeteringService meteringService,
                                           DeviceService deviceService, CustomPropertySetService customPropertySetService,
                                           NlsService nlsService) {
        this();
        setEventService(eventService);
        setMeteringService(meteringService);
        setDeviceService(deviceService);
        setCustomPropertySetService(customPropertySetService);
        setNlsService(nlsService);
    }

    @Override
    public void handle(LocalEvent localEvent) {
        // meter id to device
        Map<Long, Device> meterIds = new HashMap<>();

        // CAS values per device
        Map<Device, Pair<BigDecimal, BigDecimal>> powerFactorCASValues = new HashMap<>();
        Map<Device, Pair<BigDecimal, String>> maxDemandCASValues = new HashMap<>();
        Map<Device, BigDecimal> ctRatioCASValues = new HashMap<>();

        try {
            // Pair of <Meter, Reading type> to list of readings
            Map<Pair<Meter, String>, List<ReadingInfo>> readings = ((ReadingStorer) localEvent.getSource()).getReadings().stream()
                    .filter(r -> r.getMeter().isPresent())
                    .collect(groupingBy(r -> Pair.of(r.getMeter().get(), r.getReadingType().getMRID())));

            for (Map.Entry<Pair<Meter, String>, List<ReadingInfo>> entry : readings.entrySet()) {
                long meterId = entry.getKey().getFirst().getId();
                Device device = meterIds.get(meterId);
                if (device == null) {
                    Optional<Device> deviceOpt = deviceService.findDeviceByMeterId(meterId);
                    if (deviceOpt.isPresent()) {
                        meterIds.put(meterId, deviceOpt.get());
                        DeviceType deviceType = deviceOpt.get().getDeviceType();
                        String deviceTypeName = deviceType.getName();
                        // check power factor config for device
                        Pair<String, String> pfReadingTypes = CustomPropertySets.getPowerFactorEventReadingTypes().get(deviceTypeName);
                        if (pfReadingTypes != null && validatePowerFactorReadingTypes(deviceTypeName, pfReadingTypes.getFirst(), pfReadingTypes.getLast())) {
                            if (isCASAssigned(deviceType, PowerFactorCustomPropertySet.CPS_ID)) {
                                Optional<CustomPropertySet> cps = getCAS(deviceOpt.get(), PowerFactorCustomPropertySet.CPS_ID);
                                if ((boolean) getValue(cps.get(), deviceOpt.get(), PowerFactorDomainExtension.FieldNames.FLAG.javaName())) {
                                    device = deviceOpt.get();
                                    powerFactorCASValues.put(device, Pair.of(
                                            (BigDecimal) getValue(cps.get(), device, PowerFactorDomainExtension.FieldNames.SETPOINT_THRESHOLD.javaName()),
                                            (BigDecimal) getValue(cps.get(), device, PowerFactorDomainExtension.FieldNames.HYSTERESIS_PERCENTAGE.javaName())));
                                }
                            }
                        }
                        // check max demand config for device
                        String mdReadingType = CustomPropertySets.getMaxDemandEventReadingTypes().get(deviceTypeName);
                        if (mdReadingType != null && validateMaxDemandReadingType(mdReadingType)) {
                            if (isCASAssigned(deviceType, MaxDemandCustomPropertySet.CPS_ID)) {
                                Optional<CustomPropertySet> cps = getCAS(deviceOpt.get(), MaxDemandCustomPropertySet.CPS_ID);
                                if ((boolean) getValue(cps.get(), deviceOpt.get(), MaxDemandDomainExtension.FieldNames.FLAG.javaName())) {
                                    device = deviceOpt.get();
                                    maxDemandCASValues.put(device, Pair.of(
                                            (BigDecimal) getValue(cps.get(), device, MaxDemandDomainExtension.FieldNames.CONNECTED_LOAD.javaName()),
                                            (String) getValue(cps.get(), device, MaxDemandDomainExtension.FieldNames.UNIT.javaName())));
                                }
                            }
                        }
                        // check ct ratio config for device
                        String ctrReadingType = CustomPropertySets.getCTRatioEventReadingTypes().get(deviceTypeName);
                        if (ctrReadingType != null) {
                            if (isCASAssigned(deviceType, CTRatioCustomPropertySet.CPS_ID)) {
                                Optional<CustomPropertySet> cps = getCAS(deviceOpt.get(), CTRatioCustomPropertySet.CPS_ID);
                                if ((boolean) getValue(cps.get(), deviceOpt.get(), CTRatioDomainExtension.FieldNames.FLAG.javaName())) {
                                    device = deviceOpt.get();
                                    ctRatioCASValues.put(device, (BigDecimal) getValue(cps.get(), device, CTRatioDomainExtension.FieldNames.CT_RATIO.javaName()));
                                }
                            }
                        }
                    }
                }

                // generate events
                if (device != null) {
                    String deviceName = device.getName();
                    DeviceType deviceType = device.getDeviceType();
                    String deviceTypeName = deviceType.getName();

                    // power factor
                    Pair<BigDecimal, BigDecimal> pfCpsValues = powerFactorCASValues.get(device);
                    if (pfCpsValues != null) {
                        Pair<String, String> pfReadingTypes = CustomPropertySets.getPowerFactorEventReadingTypes().get(deviceTypeName);
                        if (entry.getKey().getLast().equals(pfReadingTypes.getFirst())) {
                            for (ReadingInfo reading : entry.getValue()) {
                                Optional<ReadingInfo> reactiveReading = readings.getOrDefault(Pair.of(device.getMeter(), pfReadingTypes.getLast()), new ArrayList<>())
                                        .stream().filter(r -> r.getReading().getTimeStamp().equals(reading.getReading().getTimeStamp())).findFirst();
                                if (reactiveReading.isPresent()) {
                                    double value = reading.getReading().getValue().doubleValue();
                                    double reactiveValue = reactiveReading.get().getReading().getValue().doubleValue();
                                    if (value == 0 && reactiveValue == 0) {
                                        MessageSeeds.POWER_FACTOR_VALUES_ARE_NULL.log(LOGGER, thesaurus, deviceName,
                                                reading.getReadingType().getMRID() + ";" + reactiveReading.get().getReadingType().getMRID(),
                                                dateFormatter.format(Date.from(reading.getReading().getTimeStamp())));
                                    } else {
                                        if (powerFactorEvent(value, reactiveValue, pfCpsValues.getFirst().doubleValue(), pfCpsValues.getLast().doubleValue())) {
                                            // generate event
                                            sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), POWER_FACTOR_EVENET_CODE, EndDeviceDomain.POWER);
                                        }
                                    }
                                } else {
                                    MessageSeeds.POWER_FACTOR_MISSING_READING.log(LOGGER, thesaurus, deviceName,
                                            reading.getReadingType().getMRID(), dateFormatter.format(Date.from(reading.getReading().getTimeStamp())));
                                }
                            }
                        }
                    }

                    // max demand
                    Pair<BigDecimal, String> mdCpsValues = maxDemandCASValues.get(device);
                    if (mdCpsValues != null) {
                        String mdReadingType = CustomPropertySets.getMaxDemandEventReadingTypes().get(deviceTypeName);
                        if (entry.getKey().getLast().equals(mdReadingType)) {
                            for (ReadingInfo reading : entry.getValue()) {
                                if (maxDemandEvent(reading.getReading().getValue().doubleValue(), reading.getReadingType().getMultiplier(),
                                        mdCpsValues.getFirst().doubleValue(), mdCpsValues.getLast())) {
                                    // generate event
                                    sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), MAX_DEMAND_EVENET_CODE, EndDeviceDomain.DEMAND);
                                }
                            }
                        }
                    }

                    // ct ratio
                    BigDecimal ctCpsValue = ctRatioCASValues.get(device);
                    if (ctCpsValue != null) {
                        String ctrReadingType = CustomPropertySets.getCTRatioEventReadingTypes().get(deviceTypeName);
                        if (entry.getKey().getLast().equals(ctrReadingType)) {
                            for (ReadingInfo reading : entry.getValue()) {
                                if (ctRatioEvent(reading.getReading().getValue().doubleValue(), ctCpsValue.doubleValue())) {
                                    // generate event
                                    sendEvent(reading.getMeter().get(), reading.getReading().getTimeStamp(), CT_RATIO_EVENET_CODE, EndDeviceDomain.NA);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    boolean validatePowerFactorReadingTypes(String deviceTypeName, String readingType, String reactiveReadingType) {
        TimeAttribute defaultMeasurementPeriod = null;
        for (String rType : Arrays.asList(readingType, reactiveReadingType)) {
            Optional<ReadingType> readingTypeRef = meteringService.getReadingType(rType);
            if (!readingTypeRef.isPresent()) {
                MessageSeeds.READING_TYPE_NOT_FOUND.log(LOGGER, thesaurus, rType);
                return false;
            }
            if ((rType.equals(readingType) && !readingTypeRef.get().getUnit().equals(WATTHOUR)) ||
                    (rType.equals(reactiveReadingType) && !readingTypeRef.get().getUnit().equals(VOLTAMPEREREACTIVEHOUR)) ||
                    (!readingTypeRef.get().getMultiplier().equals(KILO))) {
                MessageSeeds.UNEXPECTED_UNIT_ON_READING_TYPE.log(LOGGER, thesaurus, readingType);
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

    boolean validateMaxDemandReadingType(String readingType) {
        Optional<ReadingType> readingTypeRef = meteringService.getReadingType(readingType);
        if (!readingTypeRef.isPresent()) {
            MessageSeeds.READING_TYPE_NOT_FOUND.log(LOGGER, thesaurus, readingType);
            return false;
        }
        if (!readingTypeRef.get().getUnit().equals(WATT) || (!readingTypeRef.get().getMultiplier().equals(KILO) && !readingTypeRef.get().getMultiplier().equals(MEGA))) {
            MessageSeeds.UNEXPECTED_UNIT_ON_READING_TYPE.log(LOGGER, thesaurus, readingType);
            return false;
        }
        return true;
    }

    @Override
    public String getTopicMatcher() {
        return EventType.READINGS_CREATED.topic();
    }

    public boolean powerFactorEvent(double realPower, double reactivePower, double setpointThreshold, double hysteresisPercentage) {
        double powerFactor = realPower / Math.sqrt(Math.pow(realPower, 2) + Math.pow(reactivePower, 2));
        double deltaPercentage = Math.abs(setpointThreshold) * hysteresisPercentage / 100;
        if (powerFactor <= setpointThreshold - deltaPercentage || powerFactor > setpointThreshold + deltaPercentage) {
            return true;
        }
        return false;
    }

    public boolean maxDemandEvent(double value, MetricMultiplier valueUnit, double connectedLoad, String unit) {
        // convert both values to kilo if needed
        if (valueUnit.equals(MEGA)) {
            value = value * 1000;
        }
        if (unit.equals(Units.MW.getValue())) {
            connectedLoad = connectedLoad * 1000;
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

    public Object getValue(CustomPropertySet customPropertySet, Device device, String propertyName) {
        CustomPropertySetValues values = customPropertySetService.getUniqueValuesFor(customPropertySet, device);
        return values.getProperty(propertyName);
    }

    public void sendEvent(Meter meter, Instant date, String code, EndDeviceDomain domain) {
        CalculatedEventRecordImpl eventRecord = new CalculatedEventRecordImpl(meter, code, date, domain);
        eventService.postEvent(EventType.END_DEVICE_EVENT_CREATED.topic(), eventRecord);
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
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(NAME, Layer.DOMAIN);
    }
}
