package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;

public class RadioCommandFactory {


    private WaveFlow100mW waveFlow100mW;

    // cached
    private FirmwareVersion firmwareVersion=null;
    private InternalDataCommand internalDataCommand=null;
    private VoltageRequest voltageRequest = null;

    private MBusInternalLogs[] mBusInternalLogs=new MBusInternalLogs[2];

    RadioCommandFactory(WaveFlow100mW waveFlow100mW) {
        this.waveFlow100mW = waveFlow100mW;
    }


    final EncoderCurrentReading readEncoderCurrentReading() throws IOException {
        EncoderCurrentReading o = new EncoderCurrentReading(waveFlow100mW);
        o.invoke();
        return o;
    }

    final public EncoderDataloggingTable readEncoderDataloggingTable() throws IOException {
        EncoderDataloggingTable o = new EncoderDataloggingTable(waveFlow100mW);
        o.invoke();
        return o;
    }

    final public EncoderDataloggingTable readEncoderDataloggingTable(final boolean portA, final boolean portB, final int nrOfValues, final int offsetFromMostRecentValue) throws IOException {
        EncoderDataloggingTable o = new EncoderDataloggingTable(waveFlow100mW,portA,portB,nrOfValues,offsetFromMostRecentValue);
        o.invoke();
        return o;
    }

    final FirmwareVersion readFirmwareVersion() throws IOException {
        if (firmwareVersion == null) {
            firmwareVersion = new FirmwareVersion(waveFlow100mW);
            firmwareVersion.invoke();
        }
        return firmwareVersion;
    }

    final InternalDataCommand readInternalData() throws IOException {
        if (internalDataCommand == null) {
            internalDataCommand = new InternalDataCommand(waveFlow100mW);
            internalDataCommand.invoke();
        }
        return internalDataCommand;
    }

    final public MBusInternalLogs readMBusInternalLogs(int portId) throws IOException {
        int pId = portId<=0?0:1;
        if (mBusInternalLogs[pId] == null) {
            mBusInternalLogs[pId] = new MBusInternalLogs(waveFlow100mW,portId);
            mBusInternalLogs[pId].invoke();
        }
        return mBusInternalLogs[pId];
    }


    final public LeakageEventTable readLeakageEventTable() throws IOException {
        LeakageEventTable leakageEventTable = new LeakageEventTable(waveFlow100mW, propertySpecService);
        leakageEventTable.invoke();
        return leakageEventTable;
    }

    final public void startMeterDetection() throws IOException {
        MeterDetection o = new MeterDetection(waveFlow100mW);
        o.invoke();
    }

    /**
     * Set the alarmconfiguration and implicit the alarm route path with the sender's address and path.
     *
     * @param alarmConfiguration
     * @throws IOException
     */
    public final void setAlarmRoute(int alarmConfiguration) throws IOException {
        AlarmRoute o = new AlarmRoute(waveFlow100mW);
        o.setAlarmConfiguration(alarmConfiguration);
        o.invoke();
    }

    public double readVoltageV1() throws IOException {
        return getVoltageRequest().getVoltageV1();
    }

    public double readVoltageV2() throws IOException {
        return getVoltageRequest().getVoltageV2();
    }

    public double readVoltageVPAL() throws IOException {
        return getVoltageRequest().getVoltageVPAL();
    }

    /**
     * Send a service request (0x80, not 0x20!) that reads a part of the RAM memory
     * Retry in case of unexpected response.
     */
    private VoltageRequest getVoltageRequest() throws IOException {
        if (voltageRequest == null) {
            int attemptNumber = 0;
            int numberOfRetries = waveFlow100mW.getInfoTypeProtocolRetriesProperty();
            while (true) {
                try {
                    voltageRequest = new VoltageRequest(waveFlow100mW);

                    //Use 0x80 instead of 0x20
                    waveFlow100mW.getWaveFlowConnect().getEscapeCommandFactory().sendUsingServiceRequest();

                    //Do the service request
                    voltageRequest.invoke();
                    break;  //Exit the loop if successful

                } catch (WaveFlow100mwEncoderException e) {
                    voltageRequest = null;          //Remove it from the cache
                    if (attemptNumber < numberOfRetries) {
                        attemptNumber++;            //Retry service request
                    } else {
                        throw e;                    //Stop trying after X attempts
                    }
                } finally {
                    //Use 0x20 for the next requests!
                    waveFlow100mW.getWaveFlowConnect().getEscapeCommandFactory().sendUsingSendFrame();
                }
            }
        }
        return voltageRequest;
    }
}
