/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @version 2.0
 * @author Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the DukePowerLMS meter protocol.
 * <BR>
 * <B>@beginchanges</B><BR>
KV|24042002|Initial version
KV|09072002|Cast bug
KV|23102002|Adapt to MeterProtocol interface
KV|23032005|Changed header to be compatible with protocol version tool
 * @endchanges
 */
package com.energyict.protocolimpl.dukepower;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.SerialNumber;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.PluggableMeterProtocol;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;


public class DukePower extends PluggableMeterProtocol implements SerialNumber {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU DukePower";
    }

    private static final byte DUKEPOWERPROTOCOL_DEBUG = 0;

    private static final byte EV_POWER_DOWN = 0x01;
    private static final byte EV_POWER_UP = 0x02;
    private static final byte EV_CLOCK_SET_PREVIOUS_TIME = 0x03;
    private static final byte EV_CLOCK_SET_NEW_TIME = 0x04;
    private static final byte EV_RAM_ERROR_DETECTED = 0x07;
    private static final byte EV_SW_MALFUNCTION_DETECTED = 0x08;
    private static final byte EV_METER_INPUT1_OVF = 0x09;
    private static final byte EV_METER_INPUT2_OVF = 0x0A;
    private static final byte EV_METER_INPUT3_OVF = 0x0B;
    private static final byte EV_METER_INPUT4_OVF = 0x0C;
    private static final byte EV_ALTER_OPTIONS = 0x19;

    private String strID;
    private long lIDMV90;
    private String strPassword;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean boolAbort = false;

    private int[] crc_tab = {
            0x0000, 0x8005, 0x800F, 0x000A, 0x801B, 0x001E, 0x0014, 0x8011,
            0x8033, 0x0036, 0x003C, 0x8039, 0x0028, 0x802D, 0x8027, 0x0022,
            0x8063, 0x0066, 0x006C, 0x8069, 0x0078, 0x807D, 0x8077, 0x0072,
            0x0050, 0x8055, 0x805F, 0x005A, 0x804B, 0x004E, 0x0044, 0x8041,
            0x80C3, 0x00C6, 0x00CC, 0x80C9, 0x00D8, 0x80DD, 0x80D7, 0x00D2,
            0x00F0, 0x80F5, 0x80FF, 0x00FA, 0x80EB, 0x00EE, 0x00E4, 0x80E1,
            0x00A0, 0x80A5, 0x80AF, 0x00AA, 0x80BB, 0x00BE, 0x00B4, 0x80B1,
            0x8093, 0x0096, 0x009C, 0x8099, 0x0088, 0x808D, 0x8087, 0x0082,
            0x8183, 0x0186, 0x018C, 0x8189, 0x0198, 0x819D, 0x8197, 0x0192,
            0x01B0, 0x81B5, 0x81BF, 0x01BA, 0x81AB, 0x01AE, 0x01A4, 0x81A1,
            0x01E0, 0x81E5, 0x81EF, 0x01EA, 0x81FB, 0x01FE, 0x01F4, 0x81F1,
            0x81D3, 0x01D6, 0x01DC, 0x81D9, 0x01C8, 0x81CD, 0x81C7, 0x01C2,
            0x0140, 0x8145, 0x814F, 0x014A, 0x815B, 0x015E, 0x0154, 0x8151,
            0x8173, 0x0176, 0x017C, 0x8179, 0x0168, 0x816D, 0x8167, 0x0162,
            0x8123, 0x0126, 0x012C, 0x8129, 0x0138, 0x813D, 0x8137, 0x0132,
            0x0110, 0x8115, 0x811F, 0x011A, 0x810B, 0x010E, 0x0104, 0x8101,
            0x8303, 0x0306, 0x030C, 0x8309, 0x0318, 0x831D, 0x8317, 0x0312,
            0x0330, 0x8335, 0x833F, 0x033A, 0x832B, 0x032E, 0x0324, 0x8321,
            0x0360, 0x8365, 0x836F, 0x036A, 0x837B, 0x037E, 0x0374, 0x8371,
            0x8353, 0x0356, 0x035C, 0x8359, 0x0348, 0x834D, 0x8347, 0x0342,
            0x03C0, 0x83C5, 0x83CF, 0x03CA, 0x83DB, 0x03DE, 0x03D4, 0x83D1,
            0x83F3, 0x03F6, 0x03FC, 0x83F9, 0x03E8, 0x83ED, 0x83E7, 0x03E2,
            0x83A3, 0x03A6, 0x03AC, 0x83A9, 0x03B8, 0x83BD, 0x83B7, 0x03B2,
            0x0390, 0x8395, 0x839F, 0x039A, 0x838B, 0x038E, 0x0384, 0x8381,
            0x0280, 0x8285, 0x828F, 0x028A, 0x829B, 0x029E, 0x0294, 0x8291,
            0x82B3, 0x02B6, 0x02BC, 0x82B9, 0x02A8, 0x82AD, 0x82A7, 0x02A2,
            0x82E3, 0x02E6, 0x02EC, 0x82E9, 0x02F8, 0x82FD, 0x82F7, 0x02F2,
            0x02D0, 0x82D5, 0x82DF, 0x02DA, 0x82CB, 0x02CE, 0x02C4, 0x82C1,
            0x8243, 0x0246, 0x024C, 0x8249, 0x0258, 0x825D, 0x8257, 0x0252,
            0x0270, 0x8275, 0x827F, 0x027A, 0x826B, 0x026E, 0x0264, 0x8261,
            0x0220, 0x8225, 0x822F, 0x022A, 0x823B, 0x023E, 0x0234, 0x8231,
            0x8213, 0x0216, 0x021C, 0x8219, 0x0208, 0x820D, 0x8207, 0x0202
    };

    // Events for MV90Connection FSM

    // ********************** Duke power Master command frame *************************
    private static final byte CMD_READ_CLOCK = 'Q';

    private Calendar gcalendarMeter = null;
    private Calendar gcalendarTimeLastIntervalEnded = null;
    private Calendar gcalendarEarliestTimeIntervalRequested = null;
    private Calendar gcalendarRecordingTime = null;

    private static final byte CMD_WRITE_CLOCK = 'E';

    private static final byte CMD_READ_METER_VALUE = 'M';
    private long lMeterReading;

    private static final byte CMD_DUMP_INTERVAL_DATA = 'D';

    private static final byte CMD_REMOTE_STATUS = 'F';

    private static final byte CMD_GET_RECORDING_TIME = 'G';
    int iInterval = 0;

    private static final byte CMD_GET_CONFIGURATION_DATA = 'L';
    byte bMeterNROfChannels;
    private static final byte CHANNEL_8BIT_DATA = 0x00;
    private static final byte CHANNEL_12BIT_DATA = 0x01;
    private static final byte CHANNEL_16BIT_DATA = 0x02;
    private static final byte CHANNEL_1416BIT_DATA = 0x03;
    byte bChannelResolution;

    private static final byte CMD_VERIFY_TIME_WINDOW = 'V';
    //private byte bTimeWindowYear;

    private static final byte MASTER_CMD = 1;
    private static final byte MASTER_CMD_CRN = 2;
    private static final byte MASTER_CMD_CRCMSB = 13;
    private static final byte MASTER_CMD_CRCLSB = 14;

    private static final byte MASTER_CMD_NR_OF_DATA_BYTES = 6;
    private static final byte MASTER_CMD_DATA_OFFSET = 3;

    private static final byte MASTER_CMD_NR_OF_PASSWORD_BYTES = 4;
    private static final byte MASTER_CMD_PASSWORD_OFFSET = 9;

    private static final byte MASTER_CMD_FRAME_SIZE = 15;
    private byte[] MasterCommandBuffer = new byte[MASTER_CMD_FRAME_SIZE];
    private byte bCRN = 0;

    // ********************** Duke power Master DataBlock Acknowledgement frame *************************
    private static final byte MASTER_DBA_FRAME_SIZE = 5;
    private byte[] MasterDataBlockAckBuffer = new byte[MASTER_DBA_FRAME_SIZE];
    private static final byte MASTER_DBA_MSA = 1;
    private static final byte MASTER_DBA_MBN = 2;
    //private byte bMBN=0;

    // ********************** Duke power Remote Response frame *************************
    private static final byte REMOTE_SHORT_RESPONSE_FRAME_SIZE = 16;
    private static final int REMOTE_DATA_BLOCK_FRAME_SIZE = 267;
    private static final byte RSR_DATA6 = 8;
    private static final byte RSR_DATA5 = 9;
    private static final byte RSR_DATA4 = 10;
    private static final byte RSR_DATA3 = 11;
    private static final byte RSR_DATA2 = 12;
    private static final byte RSR_DATA1 = 13;
    private static final byte RSR_CRN = 7;
    private static final byte RSR_STA = 6;
    private static final byte RSR_RSA = 1;
    private static final byte RSR_ID_MSB = 2;

    private byte[] RemoteDataBlockBuffer = new byte[REMOTE_DATA_BLOCK_FRAME_SIZE];
    private byte[] RemoteShortResponseBuffer = new byte[REMOTE_SHORT_RESPONSE_FRAME_SIZE];
    private static final byte WAIT_FOR_FRAME_STX = 0;
    private static final byte WAIT_FOR_FRAME_DATA = 1;
    private byte bCurrentState;

    private static final int RDB_END = 266;
    private static final int RDB_CRCMSB = 264;
    private static final int RDB_CRCLSB = 265;
    private static final byte RDB_DATA = 8;
    private static final byte RDB_BKN = 7;
    private static final byte RDB_EVENT_DATA_LENGTH = 5;
    private static final byte RDB_ID_MSB = 2;
    private static final byte RDB_STA = 6;

    private static final byte NR_OF_EVENTS_IN_BLOCK = 51;

    // ********************** Duke power protocol general *************************
    private static final byte STX = 0x02;
    private static final byte ACK = 0x6;
    private static final byte NAK = 0x15;
    private static final byte FF = 0x0C;
    private static final byte EOT = 0x04;
    private static final byte ETB = 0x17;

    public final byte DUKE_SUCCESS = 0x00;
    public final byte DUKE_BADCRC = 0x01;
    public final byte DUKE_TIMEOUT = 0x02;
    public final byte DUKE_ABORT = 0x04;
    public final byte DUKE_RETRY = 0x08;
    public final byte DUKE_CRN_MISMATCH = 0x10;
    public final byte DUKE_NAK = 0x20;
    public final byte DUKE_FAILED = 0x40;
    public final byte DUKE_ERROR = (byte) 0x80;

    // 4Ah (74) = ABORT | RETRY | FAILED
    public final byte PSW = (byte) 0x80;
    // Onderstaande sta bits worden niet getest.
    public final byte CMD = 0x40;
    public final byte CRC = 0x20;
    public final byte ROM = 0x10;
    public final byte PGM = 0x8;
    public final byte OE = 0x4;
    public final byte FE = 0x2;
    public final byte RAM = 0x1;

    private int iProtocolTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iDelayAfterFailProperty;
    private byte bProtocolState;
    private int iRoundtripCorrection;


    private byte bChannelNR;
    private Number[] channelValues;

    private static final byte DEBUG = 0;

    //Logger logger=null;
    TimeZone timeZone = null;
    //Properties properties=null;

    @Inject
    public DukePower(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /**
     * initializes the receiver
     *
     * @param inputStream  <br>
     * @param outputStream <br>
     * @param timeZone     <br>
     * @param logger       <br>
     */
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.timeZone = timeZone;
        //this.logger = logger;

        bCRN = 0;
        boolAbort = false;
        bCurrentState = WAIT_FOR_FRAME_STX;
        lMeterReading = 0;
        iInterval = 0;

        gcalendarMeter = ProtocolUtils.getCalendar(timeZone);
        gcalendarRecordingTime = ProtocolUtils.getCalendar(timeZone);
        gcalendarTimeLastIntervalEnded = ProtocolUtils.getCalendar(timeZone);

        bProtocolState = 0;
        //bMBN=0;
        bMeterNROfChannels = 0;
        bChannelResolution = 0;
        //bTimeWindowYear=0;
        // Code for ID check 02052002
        lIDMV90 = (strID.charAt(0) - 48) * 1000000 +
                (strID.charAt(1) - 48) * 100000 +
                (strID.charAt(2) - 48) * 10000 +
                (strID.charAt(3) - 48) * 1000 +
                (strID.charAt(4) - 48) * 100 +
                (strID.charAt(5) - 48) * 10 +
                (strID.charAt(6) - 48) * 1;
    }

    private void buildFrameRecorderStatus(byte[] byteBuffer) {
        byteBuffer[0] = STX;
        byteBuffer[MASTER_CMD] = CMD_REMOTE_STATUS;
        byteBuffer[MASTER_CMD_CRN] = bCRN;
        clearData(byteBuffer);
        byteBuffer[MASTER_CMD_DATA_OFFSET] = 0;
        byteBuffer[MASTER_CMD_DATA_OFFSET + 1] = 0;
        byteBuffer[MASTER_CMD_DATA_OFFSET + 2] = (byte) (((byte) strID.getBytes()[0] - (byte) 48) | (byte) 0xF0);
        byteBuffer[MASTER_CMD_DATA_OFFSET + 3] = (byte) ((((byte) strID.getBytes()[1] - (byte) 48) << 4) | ((byte) strID.getBytes()[2] - (byte) 48));
        byteBuffer[MASTER_CMD_DATA_OFFSET + 4] = (byte) ((((byte) strID.getBytes()[3] - (byte) 48) << 4) | ((byte) strID.getBytes()[4] - (byte) 48));
        byteBuffer[MASTER_CMD_DATA_OFFSET + 5] = (byte) ((((byte) strID.getBytes()[5] - (byte) 48) << 4) | ((byte) strID.getBytes()[6] - (byte) 48));
        setPassword(byteBuffer);
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);

    } // private void buildFrameMeterReading(byte[] byteBuffer,int iChannelNR)

    private void buildFrameGetConfigurationData(byte[] byteBuffer) {
        byteBuffer[0] = STX;
        byteBuffer[MASTER_CMD] = this.CMD_GET_CONFIGURATION_DATA;
        byteBuffer[MASTER_CMD_CRN] = bCRN;
        clearData(byteBuffer);
        setPassword(byteBuffer);
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);

    } // private void buildFrameGetConfigurationData(byte[] byteBuffer,int iChannelNR)

    private void buildFrameGetRecorderTime(byte[] byteBuffer) {
        byteBuffer[0] = STX;
        byteBuffer[MASTER_CMD] = this.CMD_GET_RECORDING_TIME;
        byteBuffer[MASTER_CMD_CRN] = bCRN;
        clearData(byteBuffer);
        setPassword(byteBuffer);
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);

    } // private void buildFrameMeterReading(byte[] byteBuffer,int iChannelNR)

    /**
     * Method requests size of interval data memory in NR of 1024 byte pages.
     *
     * @return NR of 1024byte pages.
     * @throws IOException
     */
    private long getRecorderMemoryPage() throws IOException {
        buildFrameRecorderStatus(MasterCommandBuffer);

        if (!sendAndWaitForResponse(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteShortResponseBuffer, MasterCommandBuffer)) {
            ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
            ProtocolUtils.flushInputStream(inputStream);
            throw new ProtocolException("getRecorderMemoryPage Failed! reason=" + bProtocolState);
        } else {
            // KV 22072003
            byte bMemoryPage = RemoteShortResponseBuffer[RSR_DATA2];
            return (long) bMemoryPage & 0xFF;
        }

    } // private byte getRecorderMemoryPage() throws IOException


    /**
     * Method that requests the recorder interval in sec.
     *
     * @return Remote meter 'recorder interval' in sec.
     * @throws IOException
     */
    public int getProfileInterval() throws IOException, UnsupportedException {
        if (iInterval == 0) {
            buildFrameGetRecorderTime(MasterCommandBuffer);

            if (!sendAndWaitForResponse(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteShortResponseBuffer, MasterCommandBuffer)) {
                ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
                ProtocolUtils.flushInputStream(inputStream);
                throw new ProtocolException("getRecorderInterval Failed! reason=" + bProtocolState);
            } else {
                iInterval = (int) ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA2]) * 60;
                gcalendarRecordingTime.set(gcalendarRecordingTime.MONTH, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA6]) - 1);
                gcalendarRecordingTime.set(gcalendarRecordingTime.DAY_OF_MONTH, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA5]));
                gcalendarRecordingTime.set(gcalendarRecordingTime.HOUR_OF_DAY, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA4]));
                gcalendarRecordingTime.set(gcalendarRecordingTime.MINUTE, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA3]));
                return iInterval;
            }
        } else {
            return iInterval;
        }

    } // private int getProfileInterval() throws IOException,UnsupportedException

    /**
     * this implementation throws UnSupportedException. Subclasses may override
     *
     * @return the number of channels
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public int getNumberOfChannels() throws IOException, UnsupportedException {
        buildFrameGetConfigurationData(MasterCommandBuffer);

        if (!sendAndWaitForResponse(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteShortResponseBuffer, MasterCommandBuffer)) {
            ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
            ProtocolUtils.flushInputStream(inputStream);
            throw new ProtocolException("getNROfChannels Failed! reason=" + bProtocolState);
        } else {
            bMeterNROfChannels = (byte) ((((int) RemoteShortResponseBuffer[RSR_DATA5] & 0xff) / 16) + 1);

            // KV 22072003 Only for our dukepower implementation!
            // bit 7 of databyte 6 in the dukepower protocol is used to indicate nr of channels > 16!
            if ((((int) RemoteShortResponseBuffer[RSR_DATA6] & 0xff) & 0x80) != 0) {
                bMeterNROfChannels += 16;
            }

            bChannelResolution = (byte) (((int) RemoteShortResponseBuffer[RSR_DATA5] & 0xFF) % 16);


            if (bChannelResolution != this.CHANNEL_16BIT_DATA) {
                throw new ProtocolException("Only 16bit channel data allowed!");
            }
            return bMeterNROfChannels;
        }
    } // public int getNumberOfChannels()  throws IOException


    private void buildFrameReadClock(byte[] byteBuffer) {
        byteBuffer[0] = STX;
        byteBuffer[MASTER_CMD] = CMD_READ_CLOCK;
        byteBuffer[MASTER_CMD_CRN] = bCRN;

        clearData(byteBuffer);
        setPassword(byteBuffer);
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);
    }

    public Date getTime() throws IOException, ProtocolException {
        int i;
        byte bResult;

        buildFrameReadClock(MasterCommandBuffer);
        if (!sendAndWaitForResponse(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteShortResponseBuffer, MasterCommandBuffer)) {
            ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
            ProtocolUtils.flushInputStream(inputStream);
            throw new ProtocolException("getTime Failed! reason=" + bProtocolState);
        } else {
            gcalendarMeter.set(gcalendarMeter.MONTH, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA6]) - 1);
            gcalendarMeter.set(gcalendarMeter.DAY_OF_MONTH, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA5]));
            gcalendarMeter.set(gcalendarMeter.DAY_OF_WEEK, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA4]));
            gcalendarMeter.set(gcalendarMeter.HOUR_OF_DAY, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA3]));
            gcalendarMeter.set(gcalendarMeter.MINUTE, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA2]));
            gcalendarMeter.set(gcalendarMeter.SECOND, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA1]));
            gcalendarMeter.set(Calendar.MILLISECOND, 0);
            return new Date(gcalendarMeter.getTime().getTime() - iRoundtripCorrection);
        }
    }

    private void setCalendar(byte[] byteBuffer, Calendar calendar) {
        byteBuffer[MASTER_CMD_DATA_OFFSET + 0] = ProtocolUtils.hex2BCD(calendar.get(calendar.MONTH) + 1);
        byteBuffer[MASTER_CMD_DATA_OFFSET + 1] = ProtocolUtils.hex2BCD(calendar.get(calendar.DAY_OF_MONTH));
        byteBuffer[MASTER_CMD_DATA_OFFSET + 2] = ProtocolUtils.hex2BCD(calendar.get(calendar.DAY_OF_WEEK));
        byteBuffer[MASTER_CMD_DATA_OFFSET + 3] = ProtocolUtils.hex2BCD(calendar.get(calendar.HOUR_OF_DAY));
        byteBuffer[MASTER_CMD_DATA_OFFSET + 4] = ProtocolUtils.hex2BCD(calendar.get(calendar.MINUTE));
        byteBuffer[MASTER_CMD_DATA_OFFSET + 5] = ProtocolUtils.hex2BCD((calendar.get(calendar.YEAR) % 100));

    } // private setCalendar(Calendar calendar)

    private void buildFrameWriteClock(byte[] byteBuffer) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        // Add 1 minute.
        calendar.add(Calendar.MINUTE, 1);

        int iDelay = ((59 - calendar.get(Calendar.SECOND)) * 1000) - iRoundtripCorrection;
        while (iDelay > 0) {
            try {
                if (iDelay < 15000) {
                    Thread.sleep(iDelay);
                    break;
                } else {
                    Thread.sleep(15000);
                    getTime();
                    iDelay -= 15000;
                }
            } catch (InterruptedException e) {
                throw new NestedIOException(e);
            } catch (IOException e) {
                throw new IOException("DukePower, buildFrameWriteClock, IOException, " + e.getMessage());
            }

        } // while(true)

        byteBuffer[0] = STX;
        byteBuffer[MASTER_CMD] = CMD_WRITE_CLOCK;
        byteBuffer[MASTER_CMD_CRN] = bCRN;
        setCalendar(byteBuffer, calendar);
        setPassword(byteBuffer);
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);
    }

    public void setTime() throws IOException, ProtocolException {
        int i;
        byte bResult;
        buildFrameWriteClock(MasterCommandBuffer);
        if (!sendAndWaitForResponse(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteShortResponseBuffer, MasterCommandBuffer)) {
            ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
            ProtocolUtils.flushInputStream(inputStream);
            throw new ProtocolException("setTime Failed! reason=" + bProtocolState);
        }
    }

    private void buildFrameReadMeter(byte[] byteBuffer, int iChannelNR) {
        byteBuffer[0] = STX;
        byteBuffer[MASTER_CMD] = CMD_READ_METER_VALUE;
        byteBuffer[MASTER_CMD_CRN] = bCRN;
        clearData(byteBuffer);
        byteBuffer[MASTER_CMD_DATA_OFFSET] = (byte) iChannelNR;
        setPassword(byteBuffer);
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);

    } // private void buildFrameMeterReading(byte[] byteBuffer,int iChannelNR)

    public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
        return new Quantity(BigDecimal.valueOf(doGetMeterReading(channelId)), Unit.get(BaseUnit.COUNT));
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        throw new UnsupportedException("DukePower, getMeterReadingQuantity(DString name) is not supported!");
    }

    /**
     * This method gets the momental NR of pulses counted by the remote meter for a channel.
     *
     * @param iChannelNr Channel NR starting from 0.
     * @return NR of pulses counted by the remote meter for a channel.
     */
    private long doGetMeterReading(int iChannelNr) throws IOException {
        buildFrameReadMeter(MasterCommandBuffer, iChannelNr);

        if (!sendAndWaitForResponse(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteShortResponseBuffer, MasterCommandBuffer)) {
            ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
            ProtocolUtils.flushInputStream(inputStream);
            throw new ProtocolException("getMeterReading Failed! reason=" + bProtocolState);
        } else {
            lMeterReading = ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA2]) * 1 +
                    ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA3]) * 100 +
                    ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA4]) * 10000 +
                    ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA5]) * 1000000 +
                    ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA6]) * 100000000;
            return lMeterReading;
        }

    } // private long doGetMeterReading(int iChannelNr) throws MeterProtocolException

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Date lastReading = new Date(0);
        return doGetProfileData(lastReading, ProtocolUtils.getCalendar(timeZone).getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return doGetProfileData(lastReading, ProtocolUtils.getCalendar(timeZone).getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    private ProfileData doGetProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        byte bNROfChannels = (byte) getNumberOfChannels();
        long lNROfBlocks = getRecorderMemoryPage() * 4;

        long last = from.getTime() / 1000;
        long now = to.getTime() / 1000; //ProtocolUtils.getCalendar(timeZone).getTime().getTime()/1000; // Current time
        long lNROfBlocksToRetrieve = (((now - last) / (long) getProfileInterval()) * (long) bNROfChannels * 2);

        if ((lNROfBlocksToRetrieve % 256) != 0) {
            lNROfBlocksToRetrieve += 256;
        }
        lNROfBlocksToRetrieve /= 256;

        // Check for max available blocks
        if (lNROfBlocksToRetrieve > lNROfBlocks) {
            lNROfBlocksToRetrieve = lNROfBlocks;
        }
        // Add 2 blocks for the event data
        if (includeEvents) {
            lNROfBlocksToRetrieve += 2;
        }

        // beveiliging, steeds 1 block data opvragen!!
        if (lNROfBlocksToRetrieve < 2) {
            lNROfBlocksToRetrieve = 1;
        }

        return getDemandValues(lNROfBlocksToRetrieve, bNROfChannels, includeEvents);
    }

    private ProfileData getDemandValues(long lNROfBlocks, byte bNROfChannels, boolean includeEvents) throws IOException {
        ProfileData profileData;
        byte bYear = 0;
        long lMBN = 0, blockscount = 0;

        // 05082002 add to solve align bug

        bChannelNR = (byte) (bNROfChannels - (byte) 1);
        channelValues = new Number[bNROfChannels];
        profileData = new ProfileData();
        for (int i = 0; i < bNROfChannels; i++) {
            profileData.addChannel(new ChannelInfo(i, "dukepower_channel_" + i, Unit.get(BaseUnit.COUNT)));
        }

        if ((bNROfChannels <= 0) || (bNROfChannels > 32)) {
            throw new IOException("getDemandValues Failed, wrong nr of channels, must be >0 and <=32 !");
        }

        // Always include Event Buffers.
        buildFrameDumpIntervalData(MasterCommandBuffer, lNROfBlocks - 1, includeEvents);
        if (!sendAndWaitForResponse(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteShortResponseBuffer, MasterCommandBuffer)) {
            ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
            ProtocolUtils.flushInputStream(inputStream);
            throw new ProtocolException("getDemandValues Failed! reason=" + bProtocolState);
        } else {
            gcalendarTimeLastIntervalEnded.set(gcalendarTimeLastIntervalEnded.MONTH, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA6]) - 1);
            gcalendarTimeLastIntervalEnded.set(gcalendarTimeLastIntervalEnded.DAY_OF_MONTH, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA5]));
            gcalendarTimeLastIntervalEnded.set(gcalendarTimeLastIntervalEnded.DAY_OF_WEEK, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA4]));
            gcalendarTimeLastIntervalEnded.set(gcalendarTimeLastIntervalEnded.HOUR_OF_DAY, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA3]));
            gcalendarTimeLastIntervalEnded.set(gcalendarTimeLastIntervalEnded.MINUTE, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA2]));
            gcalendarTimeLastIntervalEnded.set(gcalendarTimeLastIntervalEnded.SECOND, ProtocolUtils.BCD2hex(RemoteShortResponseBuffer[RSR_DATA1]));
            gcalendarTimeLastIntervalEnded.set(Calendar.MILLISECOND, 0);
            gcalendarEarliestTimeIntervalRequested = (Calendar) gcalendarTimeLastIntervalEnded.clone();

            if (DUKEPOWERPROTOCOL_DEBUG >= 1) {
                System.out.println(gcalendarEarliestTimeIntervalRequested.getTime());
            }
        }

        lMBN = 0xFF;
        buildMasterDataBlockAck(MasterDataBlockAckBuffer, lMBN);

        blockscount = lNROfBlocks - 1;
        do {
//System.out.println("KV_DEBUG> blockscount "+blockscount);
            for (lMBN = (blockscount % 256); lMBN >= 0; lMBN--) {
//System.out.println("KV_DEBUG> lMBN "+lMBN);
                if (!sendAndWaitForDataBlock(iProtocolTimeoutProperty, iProtocolRetriesProperty, RemoteDataBlockBuffer, MasterDataBlockAckBuffer)) {
                    ProtocolUtils.delayProtocol(iDelayAfterFailProperty);
                    ProtocolUtils.flushInputStream(inputStream);
                    throw new ProtocolException("getDataBlock Failed! reason=" + bProtocolState);
                } else {
                    if (((long) RemoteDataBlockBuffer[RDB_BKN] & 0xff) != lMBN) {
                        throw new ProtocolException("getDataBlock Failed! Wrong BKN.");
                    }

                    // Bugfix KV 12042006
//                   if (((lMBN == (lNROfBlocks-1)) || (lMBN == (lNROfBlocks-2))) && (includeEvents)) {
                    if (((lMBN == (blockscount % 256)) || (lMBN == ((blockscount % 256) - 1))) && (includeEvents)) {

//System.out.println("KV_DEBUG> event block lMBN "+lMBN);
                        parseEvents(RemoteDataBlockBuffer, profileData, bYear);

                        if (lMBN == ((blockscount % 256) - 1)) {
                            includeEvents = false;
                        }
                    } else {
//System.out.println("KV_DEBUG> interval block lMBN "+lMBN);
                        parseIntervals(RemoteDataBlockBuffer, profileData, bNROfChannels, bYear);
                    }
                }
                // Send the Ack for the datablock.
                buildMasterDataBlockAck(MasterDataBlockAckBuffer, lMBN);

            } // for(bMBN=(byte)(bNROfBlocks-1);bMBN>0;bMBN--)
            blockscount = ((blockscount / 256) * 256) - 1;


        } while (blockscount > 0);

        sendFrame(MasterDataBlockAckBuffer);

        // Apply the events to the channel statusvalues
        profileData.applyEvents(getProfileInterval() / 60);

        return profileData;

    } // private ProfileData getDemandValues(...)

    private void parseEvents(byte[] byteReceiveBuffer, ProfileData profileData, byte bYear) throws IOException {
        for (int i = 0; i < NR_OF_EVENTS_IN_BLOCK; i++) {
            if (byteReceiveBuffer[RDB_DATA + RDB_EVENT_DATA_LENGTH * i] != 0) {
                Calendar calendar = ProtocolUtils.getCalendar(timeZone);
                calendar.set(calendar.MONTH, ProtocolUtils.BCD2hex(byteReceiveBuffer[RDB_DATA + 5 * i + 1]) - 1);
                calendar.set(calendar.DAY_OF_MONTH, ProtocolUtils.BCD2hex(byteReceiveBuffer[RDB_DATA + 5 * i + 2]));
                calendar.set(calendar.HOUR_OF_DAY, ProtocolUtils.BCD2hex(byteReceiveBuffer[RDB_DATA + 5 * i + 3]));
                calendar.set(calendar.MINUTE, ProtocolUtils.BCD2hex(byteReceiveBuffer[RDB_DATA + 5 * i + 4]));
                calendar.set(calendar.SECOND, 0);
                calendar.set(calendar.MILLISECOND, 0);

                // KV 22072003 if previousevent is
                if (calendar.getTime().after(new Date())) {
                    calendar.add(Calendar.YEAR, -1);
                }

                profileData.addEvent(new MeterEvent(new Date(calendar.getTime().getTime()), (int) mapLogCodes(byteReceiveBuffer[RDB_DATA + 5 * i]), (int) byteReceiveBuffer[RDB_DATA + 5 * i]));
            }

        } // for (i=0;i<NR_OF_EVENTS_IN_BLOCK;i++)

    } // private void parseEvents(...)

    private long mapLogCodes(long lLogCode) {
        switch ((byte) lLogCode) {
            case EV_POWER_DOWN:
                return (MeterEvent.POWERDOWN);
            case EV_POWER_UP:
                return (MeterEvent.POWERUP);
            case EV_CLOCK_SET_PREVIOUS_TIME:
                return (MeterEvent.SETCLOCK_BEFORE);
            case EV_CLOCK_SET_NEW_TIME:
                return (MeterEvent.SETCLOCK_AFTER);
            case EV_RAM_ERROR_DETECTED:
                return (MeterEvent.RAM_MEMORY_ERROR);
            case EV_SW_MALFUNCTION_DETECTED:
                return (MeterEvent.PROGRAM_FLOW_ERROR);
            case EV_METER_INPUT1_OVF:
                return (MeterEvent.REGISTER_OVERFLOW);
            case EV_METER_INPUT2_OVF:
                return (MeterEvent.REGISTER_OVERFLOW);
            case EV_METER_INPUT3_OVF:
                return (MeterEvent.REGISTER_OVERFLOW);
            case EV_METER_INPUT4_OVF:
                return (MeterEvent.REGISTER_OVERFLOW);
            case EV_ALTER_OPTIONS:
                return (MeterEvent.CONFIGURATIONCHANGE);
            default:
                return (MeterEvent.OTHER);
        } // switch(iLogCode)

    } // private void mapLogCodes(int iLogCode)

    private void parseIntervals(byte[] byteReceiveBuffer, ProfileData profileData, byte bNROfChannels, byte bYear) throws IOException {
        for (int i = 0; i < (int) (256 / 2); i++) // 1 block contains 128 interval values
        {
            channelValues[bChannelNR] = new Long(((long) byteReceiveBuffer[RDB_DATA + i * 2] & 0x000000FF) * 256 +
                    ((long) byteReceiveBuffer[RDB_DATA + i * 2 + 1] & 0x000000FF));
            if (bChannelNR-- <= 0) {
                // Fill profileData
                IntervalData intervalData = new IntervalData(new Date(gcalendarEarliestTimeIntervalRequested.getTime().getTime()));

                intervalData.addValues(channelValues);
                //for (int t=0;t<bNROfChannels;t++) intervalData.addValue(channelValues[t]);
                profileData.addInterval(intervalData);

                bChannelNR = (byte) (bNROfChannels - (byte) 1);
                gcalendarEarliestTimeIntervalRequested.add(gcalendarEarliestTimeIntervalRequested.SECOND, (getProfileInterval()) * (-1));
            }

        } // for (i=0;i<(byte)((256/2)/bNROfChannels);i++)

    } // private void parseIntervals(...)

    private void buildFrameDumpIntervalData(byte[] byteBuffer, long lNROfBlocks, boolean includeEvents) {
        byteBuffer[0] = STX;
        byteBuffer[MASTER_CMD] = CMD_DUMP_INTERVAL_DATA;

        byteBuffer[MASTER_CMD_CRN] = bCRN;
        clearData(byteBuffer);
        byteBuffer[MASTER_CMD_DATA_OFFSET] = (byte) (lNROfBlocks % 256);   // Nr of blocks%256
        byteBuffer[MASTER_CMD_DATA_OFFSET + 4] = (byte) (lNROfBlocks / 256);   // Nr of blocks/256
        byteBuffer[MASTER_CMD_DATA_OFFSET + 1] = (byte) 0xFF;   // Channelm nr (0..N-1); 0xFF = all channels
        if (includeEvents) {
            byteBuffer[MASTER_CMD_DATA_OFFSET + 2] = 0;
        } else {
            byteBuffer[MASTER_CMD_DATA_OFFSET + 2] = 'H';
        }
        setPassword(byteBuffer);
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);

    } // private void buildFrameDumpIntervalData(...)

    private void buildMasterDataBlockAck(byte[] byteBuffer, long lMBN) {
        byteBuffer[0] = STX;
        byteBuffer[MASTER_DBA_MSA] = ACK;
        byteBuffer[MASTER_DBA_MBN] = (byte) lMBN;
        clearCRC(byteBuffer);
        calcCRC(byteBuffer);
    }


    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    /**
     * this implementation calls <code> validateProperties </code>
     * and assigns the argument to the properties field
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
        //this.properties = properties;
    }

    /**
     * <p>validates the properties.</p><p>
     * The default implementation checks that all required parameters are present.
     * </p>
     *
     * @param properties <br>
     * @throws MissingPropertyException <br>
     * @throws InvalidPropertyException <br>
     */
    protected void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            if (strID.length() != 7) {
                throw new InvalidPropertyException("ID must be exact 7 characters.");
            }
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            if (strPassword.length() != 4) {
                throw new InvalidPropertyException("Password must be exact 4 characters.");
            }
            iProtocolTimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "10000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "1").trim());
            iDelayAfterFailProperty = Integer.parseInt(properties.getProperty("DelayAfterFail", "3000").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, " + e.getMessage());
        }
    }

    /**
     * this implementation of connect does nothing. Subclasses can override
     */
    public void connect() {
    }

    /**
     * this implementation of disconnect does nothing. Subclasses can override
     */
    public void disconnect() {
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name <br>
     * @return the register value
     * @throws IOException             <br>
     * @throws UnsupportedException    <br>
     * @throws NoSuchRegisterException <br>
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        throw new UnsupportedException();
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @param name  <br>
     * @param value <br>
     * @throws IOException             <br>
     * @throws NoSuchRegisterException <br>
     * @throws UnsupportedException    <br>
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        throw new UnsupportedException();
    }

    /**
     * this implementation throws UnsupportedException. Subclasses may override
     *
     * @throws IOException          <br>
     * @throws UnsupportedException <br>
     */
    public void initializeDevice() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    /**
     * the implementation returns both the address and password key
     *
     * @return a list of strings
     */
    public List<String> getRequiredKeys() {
        return Collections.emptyList();
    }

    /**
     * this implementation returns an empty list
     *
     * @return a list of strings
     */
    public List<String> getOptionalKeys() {
        return Arrays.asList("Timeout", "Retries", "DelayAfterFail");
    }

    /**
     * ************ Taken from DukePowerProtocol ****************
     */

    private void clearData(byte[] byteBuffer) {
        byte i;
        for (i = 0; i < MASTER_CMD_NR_OF_DATA_BYTES; i++) {
            byteBuffer[MASTER_CMD_DATA_OFFSET + i] = 0;
        }

    } // private void clearData()

    private void setPassword(byte[] byteBuffer) {
        byte i;
        for (i = 0; i < MASTER_CMD_NR_OF_PASSWORD_BYTES; i++) {
            byteBuffer[MASTER_CMD_PASSWORD_OFFSET + i] =
                    (byte) strPassword.charAt(i);
        }

    } // private void setPassword()

    private void clearCRC(byte[] byteBuffer) {
        byteBuffer[byteBuffer.length - 2] = 0;
        byteBuffer[byteBuffer.length - 1] = 0;
    }

    private void sendFrame(byte[] byteBuffer) throws IOException {
        outputStream.write(byteBuffer);
    }

    private int calcCRC(byte[] byteBuffer) {
        int iCRC;
        int i, Ichar, J0;

        iCRC = 0;
        for (i = 1; i < byteBuffer.length; i++) {
            Ichar = ((int) byteBuffer[i] & 0x000000ff);
            iCRC = crc_tab[(iCRC >> 8)] ^ (iCRC << 8) ^ Ichar;
            iCRC &= 0x0000FFFF;
        }

        byteBuffer[byteBuffer.length - 2] = (byte) ((iCRC >> 8) & 0x000000FF);
        byteBuffer[byteBuffer.length - 1] = (byte) ((iCRC) & 0x000000FF);
        return iCRC;

    } // private unsigned short CalcCRC()

    private int calcCRCDataBlock(byte[] byteBuffer) {
        int iCRC;
        int i, Ichar, J0;

        iCRC = 0;
        for (i = 1; i < byteBuffer.length - 1; i++) {
            Ichar = ((int) byteBuffer[i] & 0x000000ff);
            iCRC = crc_tab[(iCRC >> 8)] ^ (iCRC << 8) ^ Ichar;
            iCRC &= 0x0000FFFF;
        }

        byteBuffer[byteBuffer.length - 2] = (byte) ((iCRC >> 8) & 0x000000FF);
        byteBuffer[byteBuffer.length - 1] = (byte) ((iCRC) & 0x000000FF);
        return iCRC;

    } // private unsigned short calcCRCDataBlock()

    private byte waitForRemoteFrame(int iTimeout, byte[] byteReceiveBuffer, byte[] byteSendBuffer) throws IOException {
        long lMSTimeout;
        int inewKar;
        int iByteCount = 0;
        int iCRC = 0;

        lMSTimeout = System.currentTimeMillis() + iTimeout;
        iByteCount = 0;
        bCurrentState = WAIT_FOR_FRAME_STX;
        try {

            while (boolAbort == false) {
                if (inputStream.available() != 0) {
                    inewKar = inputStream.read();
                    switch (bCurrentState) {
                        case WAIT_FOR_FRAME_STX: {
                            switch ((byte) inewKar) {
                                case STX: {
                                    iByteCount = 1;
                                    byteReceiveBuffer[0] = (byte) inewKar;
                                    bCurrentState = WAIT_FOR_FRAME_DATA;
                                }
                                break;

                                default:
                                    break;

                            } // switch((byte)inewKar)
                        }
                        break; // case WAIT_FOR_STX

                        case WAIT_FOR_FRAME_DATA: {

                            byteReceiveBuffer[iByteCount] = (byte) inewKar;
                            if (++iByteCount >= byteReceiveBuffer.length) {
                                if (DEBUG == 1) {
                                    System.out.println();
                                }

                                if (byteReceiveBuffer[RSR_RSA] == NAK) {
                                    return DUKE_NAK;
                                }

                                if (iByteCount == this.REMOTE_DATA_BLOCK_FRAME_SIZE) {
                                    iCRC = 0;
                                    iCRC = ((int) byteReceiveBuffer[this.RDB_CRCMSB] << 8) & 0x0000FF00;
                                    iCRC |= (((int) byteReceiveBuffer[this.RDB_CRCLSB] & 0x000000FF));
                                    byteReceiveBuffer[this.RDB_CRCMSB] = 0;
                                    byteReceiveBuffer[this.RDB_CRCLSB] = 0;
                                    if (byteReceiveBuffer[this.RDB_END] == this.EOT) {


                                    } else if (byteReceiveBuffer[this.RDB_END] == this.ETB) {


                                    } else {
                                        return DUKE_ERROR;
                                    }

                                    if (calcCRCDataBlock(byteReceiveBuffer) == iCRC) {
                                        return DUKE_SUCCESS;
                                    } else {
                                        return DUKE_BADCRC;
                                    }

                                } else {
                                    iCRC = 0;
                                    iCRC = ((int) byteReceiveBuffer[byteReceiveBuffer.length - 2] << 8) & 0x0000FF00;
                                    iCRC |= (((int) byteReceiveBuffer[byteReceiveBuffer.length - 1] & 0x000000FF));
                                    byteReceiveBuffer[byteReceiveBuffer.length - 2] = 0;
                                    byteReceiveBuffer[byteReceiveBuffer.length - 1] = 0;
                                    if (calcCRC(byteReceiveBuffer) == iCRC) {
                                        return DUKE_SUCCESS;
                                    } else {
                                        return DUKE_BADCRC;
                                    }

                                }

                            }
                        }
                        break; // case WAIT_FOR_STX
                    } // switch (bCurrentState)

                } // if (inputStream.available() != 0)
                else {
                    Thread.sleep(100);
                }

                if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
                    return DUKE_TIMEOUT;
                }

            } // while(boolAbort==false)

            return 4;
        } catch (InterruptedException e) {
            throw new NestedIOException(e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException(e.getMessage());
        }

    } // private byte waitForRemoteFrame(int iTimeout,byte[] byteReceiveBuffer,byte[] byteSendBuffer) throws IOException


    private boolean sendAndWaitForResponse(int iTimeout,
                                           int iRetries,
                                           byte[] byteReceiveBuffer,
                                           byte[] byteSendBuffer) throws IOException {
        int i;
        byte bResult;
        long lReadID;

        boolAbort = false;
        bProtocolState = 0;
        for (i = 0; i < iRetries; i++) {
            sendFrame(byteSendBuffer);
            bResult = waitForRemoteFrame(iTimeout, byteReceiveBuffer, byteSendBuffer);

            if ((bResult == DUKE_SUCCESS) && (byteReceiveBuffer[RSR_CRN] != byteSendBuffer[MASTER_CMD_CRN])) {
                bResult = DUKE_CRN_MISMATCH;
            }

            if ((bResult == DUKE_SUCCESS) || (bResult == DUKE_ABORT)) {
                break;
            } else {
                bProtocolState |= bResult;
            }

        } // for (i=0;i<iRetries;i++)

        // Changed 10062002, was checked in the for loop...
        // ID Calculation 02052002

        // KV 05072002
        lReadID = getDukePowerID(byteReceiveBuffer, RSR_ID_MSB);
        if (lIDMV90 != lReadID) {
            throw new ProtocolException("MV90 ID wrong, master=" + lIDMV90 + " WebRTU=" + lReadID);
        }

        // Password check 02052002
        if ((byteReceiveBuffer[RSR_STA] & PSW) != 0) {
            throw new ProtocolException("MV90 PASSWORD wrong");
        }

        if (i != 0) {
            bProtocolState |= DUKE_RETRY;
        }
        if (i == iRetries) {
            bProtocolState |= DUKE_FAILED;
            return false;
        }

        bCRN++;
        return true;

    } // private boolean sendAndWaitForResponse(iProtocolTimeoutProperty,iProtocolRetriesProperty,RemoteShortResponseBuffer,MasterCommandBuffer) throws IOException

    // KV 05072002
    private long getDukePowerID(byte[] byteReceiveBuffer, int iOffset) {
        long lReadID;
        lReadID = (byteReceiveBuffer[iOffset + 3] & (byte) 0x0F) * 1 +
                ((byteReceiveBuffer[iOffset + 3] >> 4) & (byte) 0x0F) * 10 +
                (byteReceiveBuffer[iOffset + 2] & (byte) 0x0F) * 100 +
                ((byteReceiveBuffer[iOffset + 2] >> 4) & (byte) 0x0F) * 1000 +
                (byteReceiveBuffer[iOffset + 1] & (byte) 0x0F) * 10000 +
                ((byteReceiveBuffer[iOffset + 1] >> 4) & (byte) 0x0F) * 100000 +
                (byteReceiveBuffer[iOffset + 0] & (byte) 0x0F) * 1000000;
        return lReadID;
    }

    private boolean sendAndWaitForDataBlock(int iTimeout,
                                            int iRetries,
                                            byte[] byteReceiveBuffer,
                                            byte[] byteSendBuffer) throws ProtocolException, IOException {
        int i;
        byte bResult;
        long lReadID;

        boolAbort = false;
        bProtocolState = 0;
        for (i = 0; i < iRetries; i++) {
            sendFrame(byteSendBuffer);
            bResult = waitForRemoteFrame(iTimeout, byteReceiveBuffer, byteSendBuffer);

            // KV 05072002
            lReadID = getDukePowerID(byteReceiveBuffer, RDB_ID_MSB);
            if (lIDMV90 != lReadID) {
                throw new ProtocolException("MV90 ID wrong, master=" + lIDMV90 + " WebRTU=" + lReadID);
            }
            // Password check 02052002
            if ((byteReceiveBuffer[RDB_STA] & PSW) != 0) {
                throw new ProtocolException("MV90 PASSWORD wrong");
            }

            if ((bResult == DUKE_SUCCESS) || (bResult == DUKE_ABORT)) {
                break;
            } else {
                bProtocolState |= bResult;
            }
        }
        if (i != 0) {
            bProtocolState |= DUKE_RETRY;
        }
        if (i == iRetries) {
            bProtocolState |= DUKE_FAILED;
            return false;
        }

        return true;

    }

    public Object getCache() {
        return null;
    }

    public Object fetchCache(int rtuid) {
        return null;
    }

    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) {
    }

    public void release() throws IOException {
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        return getIResponse(commChannel);
    }

    public String getIResponse(SerialCommunicationChannel commChannel) throws IOException {
        String response = doGetIResponse(commChannel);
        if (response == null) {
            response = doGetIResponse(commChannel); // try again
        }

        if (response != null) {
            return response.substring(14);
        } else {
            return null;
        }
    }

    private String doGetIResponse(SerialCommunicationChannel commChannel) throws IOException {
        commChannel.getOutputStream().write('I');
        int count = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            if (commChannel.getInputStream().available() != 0) {
                baos.write(commChannel.getInputStream().read());
            } else {
                try {
                    Thread.sleep(100);
                    //if (count++>=(ProtocolMeterDiscover.IDISCOVER_WAIT*10))
                    if (count++ >= (5 * 10)) // KV tricky change because don't want to depend on core code changes...
                    {
                        break;
                    }
                } catch (InterruptedException e) {
                    // absorb
                }
            }
        }
        if (baos.size() == 0) {
            return null;
        } else {
            return new String(baos.toByteArray());
        }

    } // public String getIResponse()
}
