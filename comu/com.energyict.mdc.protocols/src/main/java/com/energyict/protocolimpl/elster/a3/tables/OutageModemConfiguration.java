/*
 * OptionBoardScratchPad.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class OutageModemConfiguration extends AbstractTable {

    /*
    Memory storag:e EEPROM
    Total table size: (bytes) 62
    Read access: 1
    Write access: 3

    This table is written to the outage modem as part of modem initialization. For the first release
    of the A3 ALPHA meter, the outage modem will not be changed and the format must match
    ALPHA Plus meter Class 28. Data is downloaded to the outage modem using the IIC bus.
    There are 4 parts to the initialization of the outage modem:
    1 Write MT-88 (ALPHA Plus Class 28)
    2 Write a 14-byte account number to MT-89 storage location in the outage modem.
    3 Write a 5-byte serial number to MT-89 storage location in the outage modem.
    4 Write the time (hh:mm:ss) to MT-89 storage location in the outage modem.
    The initial call delay and the retry delay for the outage calls is calculated as follows:
      Initial call delay = PFDLY + rand(AVGDLY   2)
      Delay between retries = CALLDLY + rand(AVGDLY   2)
    Only the fields marked with  (*FW)  are used by the meter firmware. The other fields are
    All retry parameters in MT-88 are only used for outage calls. The retry strategy for
    restoration, alarm, and billing calls is specified in MT-90.
    */

    private String outageModemDialString; // 36 bytes The phone number for outage calls.
    private int powerFailDelay; // 1 byte Qualification time for outage and restoration calls. 0 - 255 seconds. When the outage exceeds this time, the outage modem can attempt to make an outage call (if enabled) and ModemCommunication sets the power_failure flag in ST-3 which can then trigger a restoration call (if enabled.)
    private int averageDelayTime; // 1 byte Average delay time (0-255 seconds.) Used by the internal modem (in conjunction with PFDLY and CALLDLY) to cause a random average delay before attempting to place an outage call.
                          // The internal modem randomizes the value of AVGDLY times 2, then adds the result to PFDLY (initial call attempt) or CALLDLY (subsequent call attempts) to use as a delay before making an outage call.
                                                             // Initial call delay = PFDLY + RAND(AVGDLY * 2)
                                                             // Subsequent call delay = CALLDLY + RAND(AVGDLY * 2)
    private int callRetryDelay; // 1 byte Call retry delay. 0 - 255 in 5 second increments. Used by the internal modem in conjunction with AVGDLY when an outage call is not successful. A value of zero is interpreted as 2 seconds.
    private int answerDelay; // 1 byte The maximum length of time (in seconds) the outage modem remains off-hook in an attempt to place an outage call. If the call is not successful within this length of time, the modem hangs up and schedules another call. When power is restored, the meter waits for this period of time before forcing the modem to release the phone line and attempting to place a restoration call.
    private int powerUpDelay; // 1 byte Minimum time period (in seconds) between power outages required to trigger an outage call by the internal modem. A value of zero causes the internal modem to make an outage call for every outage.
    private int maxOutageCallRetries; // 1 byte The max number of times to attempt an outage call. Restoration call retries are controlled by MT-90.
    private int outageFlags; // 1 byte
                                              // b0: Outage Flag
                     //      1 = place a call to report power outages
                                              //      0 = don t call to report power outages
                                              // b1: RXC_IS_ROUTED (OMM only)
                                              //    	 0 = RXC is driven such that U1-C is connected to Cx
                                              //      1 = RXC is driven based on the WAN routing algorithm
                                              // b2: Intrusion Detection
                                              //      1 = disable off-hook and intrusion detection
                                              //      0 = enable off-hook and intrusion detection
                                              // b3-4: Outage Call Baud Rate
                                              //	    0 = 2400 1 = 1200 2 = 300
                                              //      3 = no outage calls
                                              // b5 UseMT88Constants. Defines which diode constants are used to calculate thresholds for intrusion and off hook detection.
                                              //      This bit is only used for rev 51 of PIC firmware and higher.
                                              //      Revisions 40, 49, and 47 of PIC firmware consider this bit a  don t care  bit.
                                              //      0 = for rev 51 and higher, use constants defined in alternate field. This is MT-89 offset 64.
                                              //      1 = for rev 51 and higher, use constants defined by MT-88.EX_TIME_CONST.
                                              // b6 TXC_IS_AyBy (OMM only)
                                              //      0 = TXC is driven such that U1-A is connected to Ax and U1-B is connected to Bx. Echo SPHS2 to P5:17. (A3 ALPHA meter s RX/TX2 is connected to Option Board #2).
                                              //      1 = TXC is driven such that U1-A is connected to Ay and U1-B is connected to By. Echo RXC to P5:17.
                                              // b7: Outage Protocol. This is for future use with a new internal modem in development at Elster Electricity. Existing internal modems only support Elster Electricity protocol for outage calls.
                                              //      1 = ANSI C12.21 (A3 ALPHA meter)
                                              //      0 = Elster Electricity (ALPHA Plus meter)
    private int callTimeout; // 1 byte The number of minutes the internal modem remains off-hook during a non-outage call (any call initiated by the meter or by a remote modem) when no data is being sent or received.
    private int initialWait; // 1 byte Unconditional time delay before sending the modem initialization string (INITSTR) over the remote port. Default value of zero means no wait. This field is primarily intended for communication with devices other than a modem. For example, a radio transmitter might require time to charge up after a power outage before it can transmit.
    // RESERVED 16 bytes
    private int checkSum; // 1 byte One s complement checksum for MT-88.

    /** Creates a new instance of OptionBoardScratchPad */
    public OutageModemConfiguration(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(88,true));
    }

    public String toString() {
        return "OutageModemConfiguration:\n" +
                "   answerDelay=" + getAnswerDelay() + " s\n" +
                "   averageDelayTime=" + getAverageDelayTime() + " s\n" +
                "   callRetryDelay=" + getCallRetryDelay() + " (nr of 5 sec delays)\n" +
                "   callTimeout=" + getCallTimeout() + " min\n" +
                "   initialWait=" + getInitialWait() + "\n" +
                "   maxOutageCallRetries=" + getMaxOutageCallRetries() + "\n" +
                "   outageFlags=0x" + Integer.toHexString(getOutageFlags()) + "\n" +
                "   outageModemDialString=" + getOutageModemDialString() + "\n" +
                "   powerFailDelay=" + getPowerFailDelay() + " s\n" +
                "   powerUpDelay=" + getPowerUpDelay() + " s\n" +
                "   checkSum=0x" + Integer.toHexString(getCheckSum()) + "\n";
    }
    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset = 0;
        outageModemDialString = new String(ProtocolUtils.getSubArray2(tableData,offset, 36)); offset+=36;
        powerFailDelay = C12ParseUtils.getInt(tableData,offset); offset++;
        averageDelayTime = C12ParseUtils.getInt(tableData,offset); offset++;
        callRetryDelay = C12ParseUtils.getInt(tableData,offset); offset++;
        answerDelay = C12ParseUtils.getInt(tableData,offset); offset++;
        powerUpDelay = C12ParseUtils.getInt(tableData,offset); offset++;
        maxOutageCallRetries = C12ParseUtils.getInt(tableData,offset); offset++;
        outageFlags = C12ParseUtils.getInt(tableData,offset); offset++;
        callTimeout = C12ParseUtils.getInt(tableData,offset); offset++;
        initialWait = C12ParseUtils.getInt(tableData,offset); offset++;
        // RESERVED 16 bytes
        offset+=16;
        checkSum = C12ParseUtils.getInt(tableData,offset); offset++;
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }

    public String getOutageModemDialString() {
        return outageModemDialString;
    }

    public void setOutageModemDialString(String outageModemDialString) {
        this.outageModemDialString = outageModemDialString;
    }

    public int getPowerFailDelay() {
        return powerFailDelay;
    }

    public void setPowerFailDelay(int powerFailDelay) {
        this.powerFailDelay = powerFailDelay;
    }

    public int getAverageDelayTime() {
        return averageDelayTime;
    }

    public void setAverageDelayTime(int averageDelayTime) {
        this.averageDelayTime = averageDelayTime;
    }

    public int getCallRetryDelay() {
        return callRetryDelay;
    }

    public void setCallRetryDelay(int callRetryDelay) {
        this.callRetryDelay = callRetryDelay;
    }

    public int getAnswerDelay() {
        return answerDelay;
    }

    public void setAnswerDelay(int answerDelay) {
        this.answerDelay = answerDelay;
    }

    public int getPowerUpDelay() {
        return powerUpDelay;
    }

    public void setPowerUpDelay(int powerUpDelay) {
        this.powerUpDelay = powerUpDelay;
    }

    public int getMaxOutageCallRetries() {
        return maxOutageCallRetries;
    }

    public void setMaxOutageCallRetries(int maxOutageCallRetries) {
        this.maxOutageCallRetries = maxOutageCallRetries;
    }

    public int getOutageFlags() {
        return outageFlags;
    }

    public void setOutageFlags(int outageFlags) {
        this.outageFlags = outageFlags;
    }

    public int getCallTimeout() {
        return callTimeout;
    }

    public void setCallTimeout(int callTimeout) {
        this.callTimeout = callTimeout;
    }

    public int getInitialWait() {
        return initialWait;
    }

    public void setInitialWait(int initialWait) {
        this.initialWait = initialWait;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }


}
