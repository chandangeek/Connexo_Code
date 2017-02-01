/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.metering.MeteringService;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Generates ReadingTypes for specific parameters<br/>
 * The generator contains several enums which serve as a sort of artificial <i>grouping</i>.
 * Each enum implements the {@link ReadingTypeTemplate} so the actual generator can generate ReadingTypes based on the templates.
 * Each template can indicate if it needs expansion for a certain attribute.
 */
class ReadingTypeGeneratorForParameters extends AbstractReadingTypeGenerator{

    private enum ReadingTypesForDeviceParameters implements ReadingTypeTemplate {

        SERIAL_NUMBER("Serial number", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.ASSETNUMBER).in(ReadingTypeUnit.ENCODEDVALUE)),
        ACTIVE_METER_FIRMWARE_VERSION("Active meter firmware version", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.MFGASSIGNEDCONFIGURATIONID).in(ReadingTypeUnit.BOOLEAN)),
        PASSIVE_METER_FIRMWARE_VERSION("Passive meter firmware version", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.MFGASSIGNEDCONFIGURATIONID).in(ReadingTypeUnit.ENCODEDVALUE)),
        ACTIVE_MODEM_FIRMWARE_VERSION("Active modem firmware version", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.MFGASSIGNEDCONFIGURATIONID).in(ReadingTypeUnit.BOOLEAN)),
        PASSIVE_MODEM_FIRMWARE_VERSION("Passive modem firmware version", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).measure(MeasurementKind.MFGASSIGNEDCONFIGURATIONID).in(ReadingTypeUnit.ENCODEDVALUE)),
        METER_ALARM_CONFIGURATION("Meter alarm configuration", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.ALARM).in(ReadingTypeUnit.ENCODEDVALUE)),
        METER_ALARM_STATUS("Meter alarm status", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.ALARM).in(ReadingTypeUnit.BOOLEAN)),
        METER_CONFIGURATION("Meter configuration", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.PROGRAMMED).in(ReadingTypeUnit.ENCODEDVALUE)),
        METER_STATUS("Meter status", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.PROGRAMMED).in(ReadingTypeUnit.BOOLEAN)),
        BREAKER_STATUS("Breaker status", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.SWITCHPOSITION).in(ReadingTypeUnit.BOOLEAN)),
        VALVE_STATUS("Valve status", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TAPPOSITION).in(ReadingTypeUnit.BOOLEAN)),
        BATTERY_STATUS("Battery status", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.ENERGIZATION).in(MetricMultiplier.CENTI)), // -2 is also percentage
        CT_RATIO("CT ratio", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.CTRATIO).in(ReadingTypeUnit.ENCODEDVALUE)),
        MULTIPLIER("Multiplier", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.MULTIPLIER).in(ReadingTypeUnit.ENCODEDVALUE)),
        LOAD_LIMITATION_AMPERE("Load limitation", ReadingTypeCodeBuilder.of(Commodity.NOTAPPLICABLE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.DEMANDLIMIT).in(ReadingTypeUnit.AMPERE)){
            @Override
            public boolean needsMetricMultiplierExpansion() {
                return true;
            }
        },
        LOAD_LIMITATION_WATT("Load limitation", ReadingTypeCodeBuilder.of(Commodity.NOTAPPLICABLE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.DEMANDLIMIT).in(ReadingTypeUnit.WATT)){
            @Override
            public boolean needsMetricMultiplierExpansion() {
                return true;
            }
        },
        CURRENT_TARIFF("Current tariff", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TARIFFRATE).in(ReadingTypeUnit.BOOLEAN)),
        ACTIVE_TOU_TABLE("Active tou table", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.TARIFFRATE).argument(0, 1).tou(1).in(ReadingTypeUnit.ENCODEDVALUE)),
        PASSIVE_TOU_TABLE("Passive tou table", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.TARIFFRATE).argument(0, 2).tou(1).in(ReadingTypeUnit.ENCODEDVALUE)),
        END_DEVICE_ID("End device ID", ReadingTypeCodeBuilder.of(Commodity.DEVICE).measure(MeasurementKind.ENDDEVICEID).in(ReadingTypeUnit.ENCODEDVALUE)),
        IP_ADDRESS("IP address", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.IPADDRESS).in(ReadingTypeUnit.BOOLEAN)),
        MAC_ADDRESS("MAC address", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.MACADDRESS).in(ReadingTypeUnit.BOOLEAN)),
        SIGNAL_STRENGTH("Signal strength", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.SIGNALSTRENGTH).in(MetricMultiplier.CENTI)),
        DEVICE_TEMPERATURE_K("Device temperature", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TEMPERATURE).in(ReadingTypeUnit.KELVIN)),
        DEVICE_TEMPERATURE_C("Device temperature", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TEMPERATURE).in(ReadingTypeUnit.DEGREESCELSIUS)),
        DEVICE_TEMPERATURE_F("Device temperature", ReadingTypeCodeBuilder.of(Commodity.DEVICE).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.TEMPERATURE).in(ReadingTypeUnit.DEGREESFAHRENHEIT)),

        ;

        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForDeviceParameters(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
            this.name = name;
            this.readingTypeCodeBuilder = readingTypeCodeBuilder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ReadingTypeCodeBuilder getReadingTypeCodeBuilder() {
            return readingTypeCodeBuilder;
        }
    }

    private enum ReadingTypesForG3 implements ReadingTypeTemplate {

        SHORT_ADDRESS("Short address", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).measure(MeasurementKind.MFGASSIGNEDUNIQUECOMMUNICATIONADDRESS).in(ReadingTypeUnit.ENCODEDVALUE)),
        PAN_ID("Pan ID", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).measure(MeasurementKind.ONEWAYADDRESS).in(ReadingTypeUnit.ENCODEDVALUE)),
        SECURITY_STATE("Security state", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.SECURITYSTATE).in(ReadingTypeUnit.BOOLEAN)),
        BLACKLIST_TABLE("Blacklist table", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.BLACKLISTTABLE)),
        MULTICAST_TABLE("Multicast table", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.MULITCASTADDRESS)),
        TX_PACKET_COUNT("TX packet count", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.CONTINUOUSCUMULATIVE).flow(FlowDirection.REVERSE).measure(MeasurementKind.POSITIVESEQUENCE).in(ReadingTypeUnit.COUNT)),
        RX_PACKET_COUNT("RX packet count", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.CONTINUOUSCUMULATIVE).flow(FlowDirection.FORWARD).measure(MeasurementKind.POSITIVESEQUENCE).in(ReadingTypeUnit.COUNT)),
        BAD_CRC_COUNT("Bad crc count", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.CONTINUOUSCUMULATIVE).measure(MeasurementKind.POSITIVESEQUENCE).in(ReadingTypeUnit.COUNT)),
        MODULATION("Modulation", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).measure(MeasurementKind.BANDWIDTH).in(ReadingTypeUnit.BOOLEAN)),
        LQI("LQI", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.FORWARD).measure(MeasurementKind.SIGNALSTRENGTH).in(MetricMultiplier.DECI, ReadingTypeUnit.BEL)),
        REVERSED_LQI("Reversed LQI", ReadingTypeCodeBuilder.of(Commodity.COMMUNICATION).accumulate(Accumulation.INSTANTANEOUS).flow(FlowDirection.REVERSE).measure(MeasurementKind.SIGNALSTRENGTH).in(MetricMultiplier.DECI, ReadingTypeUnit.BEL)),
        ;

        final String name;
        final ReadingTypeCodeBuilder readingTypeCodeBuilder;

        ReadingTypesForG3(String name, ReadingTypeCodeBuilder readingTypeCodeBuilder) {
            this.name = name;
            this.readingTypeCodeBuilder = readingTypeCodeBuilder;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public ReadingTypeCodeBuilder getReadingTypeCodeBuilder() {
            return readingTypeCodeBuilder;
        }
    }

    ReadingTypeGeneratorForParameters() {
        super();
    }

    @Override
    Stream<ReadingTypeTemplate> getReadingTypeTemplates() {
        return Stream.of(ReadingTypesForDeviceParameters.values(), ReadingTypesForG3.values()).flatMap(Arrays::stream);
    }

}
