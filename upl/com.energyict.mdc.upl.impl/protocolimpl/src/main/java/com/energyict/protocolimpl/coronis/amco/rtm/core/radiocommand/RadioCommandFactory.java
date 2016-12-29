package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ProfileType;

import java.io.IOException;
import java.util.Date;

public class RadioCommandFactory {

    private final RTM rtm;
    private final PropertySpecService propertySpecService;
    private RSSILevel rssiLevel = null;

    // Cached, only needs to be read out once.
    private FirmwareVersion firmwareVersion = null;
    EncoderModelDetection encoderModel = null;
    private ExtendedDataloggingTable cachedExtendedDataloggingTable = null;
    private CurrentRegisterReading registerReading = null;

    public RadioCommandFactory(RTM rtm, PropertySpecService propertySpecService) {
        this.rtm = rtm;
        this.propertySpecService = propertySpecService;
    }

    public final FirmwareVersion readFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(this.propertySpecService, rtm);
            firmwareVersion.set();
        }
        return firmwareVersion;
    }

    public ReadTOUBuckets readTOUBuckets() throws IOException {
        ReadTOUBuckets touBuckets = new ReadTOUBuckets(this.propertySpecService, rtm);
        touBuckets.set();
        return touBuckets;
    }


    public ExtendedDataloggingTable getMostRecentRecord() throws IOException {
        ExtendedDataloggingTable mostRecentRecord = new ExtendedDataloggingTable(this.propertySpecService, rtm, 1, 1, 0);
        mostRecentRecord.set();
        return mostRecentRecord;
    }

    public ReadEncoderInternalData readEncoderInternalData() throws IOException {
        ReadEncoderInternalData internalData = new ReadEncoderInternalData(this.propertySpecService, rtm);
        internalData.set();
        return internalData;
    }

    public CurrentRegisterReading readCurrentRegister() throws IOException {
        if (registerReading == null) {
            registerReading = new CurrentRegisterReading(this.propertySpecService, rtm);
            registerReading.set();
        }
        return registerReading;
    }

    public void writeIndex(int value, int port) throws IOException {
        if (rtm.getParameterFactory().readProfileType().isPulse()) {
            WriteIndexes writeIndexes = new WriteIndexes(rtm, port, this.propertySpecService);
            writeIndexes.setIndex(value);
            writeIndexes.set();
        }
    }

    public final ExtendedDataloggingTable readExtendedDataloggingTable(int portNr, final int nrOfValues, final Date toDate) throws IOException {
        ExtendedDataloggingTable table = new ExtendedDataloggingTable(this.propertySpecService, rtm, portNr, nrOfValues, toDate);
        table.set();
        return table;
    }

    public final ExtendedDataloggingTable readExtendedDataloggingTable(int portNr, final int nrOfValues, final int offset) throws IOException {
        ExtendedDataloggingTable table = new ExtendedDataloggingTable(this.propertySpecService, rtm, portNr, nrOfValues, offset);
        table.set();
        return table;
    }

    /**
     * Depending on the number of configured channels (in EiServer), the port mask is either 0x01, 0x03, 0x07 or 0x0F.
     *
     * @param portMask         indicates which channels should be read out. E.g. 3 first channels = 0x07 (00000111)
     * @param numberOfReadings how many LP entries per port (total max is 24, aka 8 entries for 3 ports, or 6 entries for 4 ports, etc..)
     * @return a table containing the requested profile data entries
     * @throws IOException timeout errors etc
     */
    public final ExtendedDataloggingTable readExtendedDataloggingTable(int portMask, int numberOfReadings) throws IOException {
        cachedExtendedDataloggingTable = new ExtendedDataloggingTable(this.propertySpecService, rtm, portMask, numberOfReadings);
        cachedExtendedDataloggingTable.set();
        return cachedExtendedDataloggingTable;
    }

    public ExtendedDataloggingTable getCachedExtendedDataloggingTable() {
        return cachedExtendedDataloggingTable;
    }

    public final LeakageEventTable readLeakageEventTable() throws IOException {
        LeakageEventTable leakageEventTable = new LeakageEventTable(this.propertySpecService, rtm);
        leakageEventTable.set();
        return leakageEventTable;
    }

    public void configureRoute() throws IOException {
        RouteConfiguration routeConfiguration = new RouteConfiguration(this.propertySpecService, rtm);
        routeConfiguration.setAlarmConfig(rtm.getParameterFactory().readAlarmConfiguration().getConfig());
        routeConfiguration.set();
    }

    public final DailyConsumption readDailyConsumption() throws IOException {
        DailyConsumption dailyConsumption = new DailyConsumption(this.propertySpecService, rtm);
        dailyConsumption.set();
        return dailyConsumption;
    }

    public boolean openWaterValve() throws IOException {
        ProfileType profileType = rtm.getParameterFactory().readProfileType();
        if (profileType.isValve()) {
            OpenWaterValveCommand openWaterValveCommand = new OpenWaterValveCommand(rtm, this.propertySpecService);
            openWaterValveCommand.set();
            return openWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean closeWaterValve() throws IOException {
        ProfileType profileType = rtm.getParameterFactory().readProfileType();
        if (profileType.isValve()) {
            CloseWaterValveCommand closeWaterValveCommand = new CloseWaterValveCommand(rtm, this.propertySpecService);
            closeWaterValveCommand.set();
            return closeWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean cleanWaterValve() throws IOException {
        ProfileType profileType = rtm.getParameterFactory().readProfileType();
        if (profileType.isValve()) {
            CleanWaterValveCommand cleanWaterValveCommand = new CleanWaterValveCommand(this.propertySpecService, rtm);
            cleanWaterValveCommand.set();
            return cleanWaterValveCommand.isSuccess();
        }
        return false;
    }

    public ValveStatus readValveStatus() throws IOException {
        ValveStatus status = new ValveStatus(rtm, this.propertySpecService);
        status.set();
        return status;
    }

    public RSSILevel readRSSI() throws IOException {
        if (rssiLevel == null) {
            rssiLevel = new RSSILevel(this.propertySpecService, rtm);
            rssiLevel.set();
        }
        return rssiLevel;
    }

    public void setRSSILevel(RSSILevel rssiLevel) {
        this.rssiLevel = rssiLevel;
    }

    public void detectEncoderModel() throws IOException {
        encoderModel = new EncoderModelDetection(this.propertySpecService, rtm);
        encoderModel.set();
    }

    public EncoderModelDetection getEncoderModelDetection() throws IOException {
        if (encoderModel == null) {
            detectEncoderModel();
        }
        return encoderModel;
    }

    public String readEncoderModelTypeA() throws IOException {
        getEncoderModelDetection();
        return encoderModel.getEncoderManufacturerDescriptionA() + ", " + encoderModel.getEncoderModelDescriptionA();
    }

    public String readEncoderModelTypeB() throws IOException {
        getEncoderModelDetection();
        return encoderModel.getEncoderManufacturerDescriptionB() + ", " + encoderModel.getEncoderModelDescriptionB();
    }
}