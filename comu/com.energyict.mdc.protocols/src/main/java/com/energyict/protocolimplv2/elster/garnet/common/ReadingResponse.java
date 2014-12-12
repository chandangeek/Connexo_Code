package com.energyict.protocolimplv2.elster.garnet.common;

import com.energyict.protocolimplv2.elster.garnet.structure.ReadingResponseStructure;
import com.energyict.protocolimplv2.elster.garnet.structure.field.ConcentratorConfiguration;
import com.energyict.protocolimplv2.elster.garnet.structure.field.DateTime;
import com.energyict.protocolimplv2.elster.garnet.structure.field.MeterConsumption;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterInstallationStatusBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterModel;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterReadingStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterRelayStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterSensorStatus;
import com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField.MeterTariffStatus;

/**
 * @author sva
 * @since 23/06/2014 - 9:44
 */
public class ReadingResponse {

    private DateTime dateTime;
    private DateTime readingDateTime;
    private MeterTariffStatus meterTariffStatus;
    private ConcentratorConfiguration concentratorConfiguration;
    private MeterRelayStatus meterRelayStatus;
    private MeterSensorStatus meterSensorStatus;
    private MeterReadingStatus meterReadingStatus;
    private MeterModel meterModel;
    private MeterInstallationStatusBitMaskField meterInstallationStatus;
    private MeterConsumption consumption;

    public ReadingResponse(ReadingResponseStructure readingResponseStructure, String serialNumber) {
        this.dateTime = readingResponseStructure.getDateTime();
        this.readingDateTime = readingResponseStructure.getReadingDateTime();
        this.meterTariffStatus = readingResponseStructure.getMeterTariff(serialNumber);
        this.concentratorConfiguration = readingResponseStructure.getConcentratorConfiguration();
        this.meterRelayStatus = readingResponseStructure.getMeterRelayStatus(serialNumber);
        this.meterSensorStatus = readingResponseStructure.getMeterSensorStatus(serialNumber);
        this.meterReadingStatus = readingResponseStructure.getMeterReadingStatus(serialNumber);
        this.meterModel = readingResponseStructure.getMeterModel(serialNumber);
        this.meterInstallationStatus = readingResponseStructure.getMeterInstallationStatus(serialNumber);
        this.consumption = readingResponseStructure.getConsumption(serialNumber);
    }

    public ReadingResponse(ReadingResponseStructure readingResponseStructure, int index) {
        this.dateTime = readingResponseStructure.getDateTime();
        this.readingDateTime = readingResponseStructure.getReadingDateTime();
        this.meterTariffStatus = readingResponseStructure.getMeterTariffCollection().getAllBitMasks().get(index);
        this.concentratorConfiguration = readingResponseStructure.getConcentratorConfiguration();
        this.meterRelayStatus = readingResponseStructure.getMeterRelayStatusCollection().getAllBitMasks().get(index);
        this.meterSensorStatus = readingResponseStructure.getMeterSensorStatusCollection().getAllBitMasks().get(index);
        this.meterReadingStatus = readingResponseStructure.getMeterReadingStatusCollection().getAllBitMasks().get(index);
        this.meterModel = readingResponseStructure.getMeterModelCollection().getAllBitMasks().get(index);
        this.meterInstallationStatus = readingResponseStructure.getMeterInstallationStatusCollection().getAllBitMasks().get(index);
        this.consumption = readingResponseStructure.getConsumptionList().get(index);
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public DateTime getReadingDateTime() {
        return readingDateTime;
    }

    public MeterTariffStatus getMeterTariffStatus() {
        return meterTariffStatus;
    }

    public ConcentratorConfiguration getConcentratorConfiguration() {
        return concentratorConfiguration;
    }

    public MeterRelayStatus getMeterRelayStatus() {
        return meterRelayStatus;
    }

    public MeterSensorStatus getMeterSensorStatus() {
        return meterSensorStatus;
    }

    public MeterReadingStatus getMeterReadingStatus() {
        return meterReadingStatus;
    }

    public MeterModel getMeterModel() {
        return meterModel;
    }

    public MeterInstallationStatusBitMaskField getMeterInstallationStatus() {
        return meterInstallationStatus;
    }

    public MeterConsumption getConsumption() {
        return consumption;
    }
}