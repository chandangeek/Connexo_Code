package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.ProfileType;

import java.io.IOException;
import java.util.Date;

public class RadioCommandFactory {


    private RTM rtm;
    private RSSILevel rssiLevel = null;

    // Cached, only needs to be read out once.
    private FirmwareVersion firmwareVersion = null;
    EncoderModelDetection encoderModel = null;
    private ExtendedDataloggingTable cachedExtendedDataloggingTable = null;
    private CurrentRegisterReading registerReading = null;

    public RadioCommandFactory(RTM rtm) {
        this.rtm = rtm;
    }

    public final FirmwareVersion readFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(rtm);
            firmwareVersion.set();
        }
        return firmwareVersion;
    }

    public ReadTOUBuckets readTOUBuckets() throws IOException {
        ReadTOUBuckets touBuckets = new ReadTOUBuckets(rtm);
        touBuckets.set();
        return touBuckets;
    }


    public ExtendedDataloggingTable getMostRecentRecord() throws IOException {
        ExtendedDataloggingTable mostRecentRecord = new ExtendedDataloggingTable(rtm, 1, 1, 0);
        mostRecentRecord.set();
        return mostRecentRecord;
    }

    public ReadEncoderInternalData readEncoderInternalData() throws IOException {
        ReadEncoderInternalData internalData = new ReadEncoderInternalData(rtm);
        internalData.set();
        return internalData;
    }

    public CurrentRegisterReading readCurrentRegister() throws IOException {
        if (registerReading == null) {
            registerReading = new CurrentRegisterReading(rtm);
            registerReading.set();
        }
        return registerReading;
    }

    public void writeIndex(int value, int port) throws IOException {
        if (rtm.getParameterFactory().readProfileType().isPulse()) {
            WriteIndexes writeIndexes = new WriteIndexes(rtm, port);
            writeIndexes.setIndex(value);
            writeIndexes.set();
        }
    }

    final public ExtendedDataloggingTable readExtendedDataloggingTable(int portNr, final int nrOfValues, final Date toDate) throws IOException {
        ExtendedDataloggingTable table = new ExtendedDataloggingTable(rtm, portNr, nrOfValues, toDate);
        table.set();
        return table;
    }

    final public ExtendedDataloggingTable readExtendedDataloggingTable(int portNr, final int nrOfValues, final int offset) throws IOException {
        ExtendedDataloggingTable table = new ExtendedDataloggingTable(rtm, portNr, nrOfValues, offset);
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
    final public ExtendedDataloggingTable readExtendedDataloggingTable(int portMask, int numberOfReadings) throws IOException {
        cachedExtendedDataloggingTable = new ExtendedDataloggingTable(rtm, portMask, numberOfReadings);
        cachedExtendedDataloggingTable.set();
        return cachedExtendedDataloggingTable;
    }

    public ExtendedDataloggingTable getCachedExtendedDataloggingTable() {
        return cachedExtendedDataloggingTable;
    }

    final public LeakageEventTable readLeakageEventTable() throws IOException {
        LeakageEventTable leakageEventTable = new LeakageEventTable(rtm);
        leakageEventTable.set();
        return leakageEventTable;
    }

    public void configureRoute() throws IOException {
        RouteConfiguration routeConfiguration = new RouteConfiguration(rtm);
        routeConfiguration.setAlarmConfig(rtm.getParameterFactory().readAlarmConfiguration().getConfig());
        routeConfiguration.set();
    }

    final public DailyConsumption readDailyConsumption() throws IOException {
        DailyConsumption dailyConsumption = new DailyConsumption(rtm);
        dailyConsumption.set();
        return dailyConsumption;
    }


    public boolean openWaterValve() throws IOException {
        ProfileType profileType = rtm.getParameterFactory().readProfileType();
        if (profileType.isValve()) {
            OpenWaterValveCommand openWaterValveCommand = new OpenWaterValveCommand(rtm);
            openWaterValveCommand.set();
            return openWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean closeWaterValve() throws IOException {
        ProfileType profileType = rtm.getParameterFactory().readProfileType();
        if (profileType.isValve()) {
            CloseWaterValveCommand closeWaterValveCommand = new CloseWaterValveCommand(rtm);
            closeWaterValveCommand.set();
            return closeWaterValveCommand.isSuccess();
        }
        return false;
    }

    public boolean cleanWaterValve() throws IOException {
        ProfileType profileType = rtm.getParameterFactory().readProfileType();
        if (profileType.isValve()) {
            CleanWaterValveCommand cleanWaterValveCommand = new CleanWaterValveCommand(rtm);
            cleanWaterValveCommand.set();
            return cleanWaterValveCommand.isSuccess();
        }
        return false;
    }

    public ValveStatus readValveStatus() throws IOException {
        ValveStatus status = new ValveStatus(rtm);
        status.set();
        return status;
    }

    public RSSILevel readRSSI() throws IOException {
        if (rssiLevel == null) {
            rssiLevel = new RSSILevel(rtm);
            rssiLevel.set();
        }
        return rssiLevel;
    }

    public void setRSSILevel(RSSILevel rssiLevel) {
        this.rssiLevel = rssiLevel;
    }

    public void detectEncoderModel() throws IOException {
        encoderModel = new EncoderModelDetection(rtm);
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