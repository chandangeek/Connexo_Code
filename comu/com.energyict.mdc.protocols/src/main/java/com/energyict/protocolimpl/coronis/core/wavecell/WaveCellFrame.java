package com.energyict.protocolimpl.coronis.core.wavecell;

import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 27/04/12
 * Time: 17:21
 */

public class WaveCellFrame {

    private static final String INI = "INI";
    private static final String RDY = "RDY";
    private static final String ANS = "ANS";
    private static final String ERR = "ERR";
    private static final String ACK = "ACK";
    private static final int ALARM_OP_CODE = 0x20;
    private static final int POWERDOWN_OP_CODE = 0x21;

    private int opCode = -1;
    private int opCount = -1;
    private String waveCellID = "";
    private String command = "";
    private int crc = -1;
    private ApplicativeData applicativeData = null;

    public WaveCellFrame(String command, String waveCellID) {
        this.waveCellID = waveCellID;
        this.command = command;
    }

    public String getWaveCellID() {
        return waveCellID;
    }

    public int getOpCount() {
        return opCount;
    }

    public void setOpCount(int opCount) {
        this.opCount = opCount;
    }

    public int getOpCode() {
        return opCode;
    }

    public void setOpCode(int opCode) {
        this.opCode = opCode;
    }

    public boolean isAlarm() {
        return ((opCode == ALARM_OP_CODE) || (opCode == POWERDOWN_OP_CODE)) && (opCount == 0);
    }

    public boolean isPowerDownAlarmFrame() {
        return (opCode == POWERDOWN_OP_CODE) && (opCount == 0);
    }

    public ApplicativeData getApplicativeData() {
        return applicativeData;
    }

    public void setApplicativeData(byte[] applicativeDataBytes) {
        this.applicativeData = new ApplicativeData(applicativeDataBytes);
    }

    public class ApplicativeData {

        byte[] wavenisFrame = new byte[0];
        int length = 0;
        int waveFlowId = 0;

        public ApplicativeData(byte[] applicativeData) {
            if ((applicativeData.length == 1) && isPowerDownAlarmFrame()) {
                wavenisFrame = applicativeData;
                //No other information included
            } else {
                waveFlowId = ProtocolTools.getUnsignedIntFromBytes(applicativeData, 1, 2);
                length = applicativeData[3] & 0xFF;
                wavenisFrame = ProtocolTools.getSubArray(applicativeData, 4, 4 + getLength());
            }
        }

        public byte[] getWavenisFrame() {
            return wavenisFrame;
        }

        public int getLength() {
            return length;
        }

        public int getWaveFlowId() {
            return waveFlowId;
        }
    }

    public boolean isPowerUp() {
        byte[] wavenisFrame = getApplicativeData().getWavenisFrame();
        return (wavenisFrame.length == 1) && ((wavenisFrame[0] & 0xFF) == 0x01);
    }

    public boolean isPowerDown() {
        byte[] wavenisFrame = getApplicativeData().getWavenisFrame();
        return (wavenisFrame.length == 1) && ((wavenisFrame[0] & 0xFF) == 0x00);
    }

    public boolean isReadyCommand() {
        return RDY.equalsIgnoreCase(command);
    }

    public boolean isIniCommand() {
        return INI.equalsIgnoreCase(command);
    }

    public boolean isAckCommand() {
        return ACK.equalsIgnoreCase(command);
    }

    public boolean isAnsCommand() {
        return ANS.equalsIgnoreCase(command);
    }

    public boolean isError() {
        return ERR.equalsIgnoreCase(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCrc(int crc) {
        this.crc = crc;
    }

    public int getCrc() {
        return crc;
    }
}