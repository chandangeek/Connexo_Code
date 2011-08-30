package com.energyict.protocolimpl.coronis.amco.rtm.core.parameter;

import com.energyict.cbo.Unit;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 24/08/11
 * Time: 14:54
 */
public class GenericHeader {

    private final static int INITIAL_BATTERY_LIFE_COUNT_SRTM_AND_EVOHOP = 0xF7F490;
    private final static int INITIAL_BATTERY_LIFE_COUNT_RTM = 0x895440;
    private static final double MAX = 0x20;
    private RtmUnit[] units = new RtmUnit[4];
    private byte[] radioAddress = new byte[6];
    private double qos = 0;
    private double shortLifeCounter = 0;
    private OperatingMode operationMode = null;
    private ApplicationStatus applicationStatus = null;
    private ProfileType profileType = null;

    public ProfileType getProfileType() {
        return profileType;
    }

    public double getQos() {
        return qos;
    }

    public OperatingMode getOperationMode() {
        return operationMode;
    }

    public double getShortLifeCounter() {
        return shortLifeCounter;
    }

    public GenericHeader(byte[] radioAddress) {
        this.radioAddress = radioAddress;
    }

    public int getIntervalStatus(int port) {
        int status = IntervalStateBits.OK;
        if ((applicationStatus.getStatus() & 0x01) == 0x01) {
            status = status | IntervalStateBits.BATTERY_LOW;
        }
        if (profileType.isPulse()) {
            switch (port) {
                case 0:
                    if ((applicationStatus.getStatus() & 0x02) == 0x02) {
                        status = status | IntervalStateBits.CORRUPTED;
                    }
                    break;
                case 1:
                    if ((applicationStatus.getStatus() & 0x04) == 0x04) {
                        status = status | IntervalStateBits.CORRUPTED;
                    }
                    break;
                case 2:
                    if ((applicationStatus.getStatus() & 0x08) == 0x08) {
                        status = status | IntervalStateBits.CORRUPTED;
                    }
                    break;
                case 3:
                    if ((applicationStatus.getStatus() & 0x10) == 0x10) {
                        status = status | IntervalStateBits.CORRUPTED;
                    }
                    break;
            }
        } else if (profileType.isEncoder()) {
            switch (port) {
                case 0:
                    if (((applicationStatus.getStatus() & 0x02) == 0x02) || ((applicationStatus.getStatus() & 0x08) == 0x08)) {
                        status = status | IntervalStateBits.CORRUPTED;
                    }
                    break;
                case 1:
                    if (((applicationStatus.getStatus() & 0x04) == 0x04) || ((applicationStatus.getStatus() & 0x10) == 0x10)) {
                        status = status | IntervalStateBits.CORRUPTED;
                    }
                    break;
            }
        }
        return status;
    }

    public ApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public GenericHeader() {
    }

    private static boolean isRtm(byte[] radioAddress) {
        return (radioAddress[1] & 0xFF) == 0x50;
    }

    private static boolean isSRTM(byte[] radioAddress) {
        return (radioAddress[1] & 0xFF) == 0x51;
    }

    private static boolean isEvoHop(byte[] radioAddress) {
        return (radioAddress[1] & 0xFF) == 0x56;
    }

    public static int getInitialBatteryCount(byte[] radioAddress) {
        if (isRtm(radioAddress)) {
            return INITIAL_BATTERY_LIFE_COUNT_RTM;
        }
        if (isSRTM(radioAddress) || isEvoHop(radioAddress)) {
            return INITIAL_BATTERY_LIFE_COUNT_SRTM_AND_EVOHOP;
        }
        return INITIAL_BATTERY_LIFE_COUNT_SRTM_AND_EVOHOP;
    }

    /**
     * The unit for the port index
     *
     * @param port: zero based port number
     * @return unit
     */
    public Unit getUnit(int port) {
        if (units[port] == null) {
            return Unit.get("");
        }
        return units[port].getUnit();
    }

    public void parse(byte[] data) throws IOException {

        operationMode = new OperatingMode(new RTM(), ProtocolTools.getIntFromBytes(data, 1, 2));
        applicationStatus = new ApplicationStatus(new RTM(), data[3] & 0xFF);

        qos = ProtocolTools.getUnsignedIntFromBytes(data, 12, 1);
        qos = (qos / MAX) * 100;

        shortLifeCounter = ProtocolTools.getUnsignedIntFromBytes(data, 13, 2) << 8;
        shortLifeCounter = 100 - (((getInitialBatteryCount(radioAddress) * 100) - (shortLifeCounter * 100)) / getInitialBatteryCount(radioAddress));


        byte[] meterEncoderData = ProtocolTools.getSubArray(data, 15, 23);
        profileType = new ProfileType(new RTM());
        profileType.parse(data);

        if (profileType.isPulse()) {
            PulseWeight pulseWeight;
            for (int port = 0; port < 4; port++) {
                pulseWeight = new PulseWeight(new RTM(), port + 1);
                pulseWeight.parse(new byte[]{meterEncoderData[port]});
                units[port] = pulseWeight;
            }
        } else if (profileType.isEncoder()) {
            EncoderUnit encoderUnit;
            for (int port = 0; port < 2; port++) {
                encoderUnit = new EncoderUnit(new RTM(), port + 1);
                encoderUnit.parse(new byte[]{meterEncoderData[(2 * port) + 1], meterEncoderData[(2 * port)]});
                units[port] = encoderUnit;
            }
        }
    }

    public RtmUnit getRtmUnit(int port) {
        if (units[port] == null) {
            return new RtmUnit(new RTM());
        }
        return units[port];
    }
}