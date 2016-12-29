package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

import com.energyict.protocolimpl.coronis.amco.rtm.RTM;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.OperatingMode;
import com.energyict.protocolimpl.coronis.amco.rtm.core.parameter.SamplingPeriod;
import com.energyict.protocolimpl.coronis.core.TimeDateRTCParser;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 8-apr-2011
 * Time: 13:25:54
 */
public class ExtendedDataloggingTable extends AbstractRadioCommand {

    private int portMask = 0x01;
    private int offset = -1;
    private Date toDate;
    private int numberOfRequestedReadings = 24;

    private int numberOfReceivedReadings = 0;
    private Date lastLoggedTimeStamp;

    //Used for parsing data received upon a user request
    private List<Integer> profileData = new ArrayList<Integer>();

    //Used for parsing data received via the bubble up mechanism
    private List<List<Integer[]>> profileDataForAllPorts = new ArrayList<List<Integer[]>>();
    private int profileInterval;

    public int getProfileInterval() {
        return profileInterval;
    }

    public ExtendedDataloggingTable(RTM rtm) {
        super(propertySpecService, rtm);
    }

    public List<List<Integer[]>> getProfileDataForAllPorts() {
        return profileDataForAllPorts;
    }

    protected ExtendedDataloggingTable(RTM rtm, int port, int numberOfRequestedReadings, int offset) {
        super(propertySpecService, rtm);
        this.portMask = (int) Math.pow(2, port - 1);
        this.numberOfRequestedReadings = numberOfRequestedReadings;
        this.offset = offset;
    }

    protected ExtendedDataloggingTable(RTM rtm, int port, int numberOfRequestedReadings, Date toDate) throws WaveFlowException {
        super(propertySpecService, rtm);
        this.portMask = (int) Math.pow(2, port - 1);
        this.numberOfRequestedReadings = numberOfRequestedReadings;
        this.toDate = toDate;
    }

    protected ExtendedDataloggingTable(RTM rtm, int portMask, int numberOfReadings) throws WaveFlowException {
        super(propertySpecService, rtm);
        this.portMask = portMask;
        this.numberOfRequestedReadings = numberOfReadings;
        this.offset = 0;
    }

    public List<Integer> getProfileData() {
        return profileData;
    }

    public int getOffset() throws IOException {
        if (offset == -1) {
            calcOffset(toDate);
        }
        if (offset < 0) {
            throw new WaveFlowException("Error calculating the offset to fetch the profile data");
        }
        return offset;
    }

    public Date getLastLoggedTimeStamp() {
        return lastLoggedTimeStamp;
    }

    private void calcOffset(Date toDate) throws IOException {
        Calendar now = new GregorianCalendar(getRTM().getTimeZone());
        now.set(Calendar.SECOND, 0);
        now.set(Calendar.MILLISECOND, 0);
        Calendar toCal = new GregorianCalendar(getRTM().getTimeZone());
        toCal.setTime(toDate);
        toCal.set(Calendar.SECOND, 0);
        toCal.set(Calendar.MILLISECOND, 0);

        if (now.getTime().equals(toCal.getTime())) {
            offset = 0;
            return;
        }
        if (toCal.getTime().after(now.getTime())) {
            offset = 0;
        } else {
            OperatingMode operatingMode = getRTM().getParameterFactory().readOperatingMode();
            if (operatingMode.isMonthlyLogging()) {
                getMostRecentRecord();
                Calendar lastLogged = Calendar.getInstance(getRTM().getTimeZone());
                lastLogged.setTime(getLastLoggedTimeStamp());
                lastLogged.setLenient(true);                                      //TODO test
                offset = 0;

                //Go back month by month until you have the date closest to the toDate.
                while (lastLogged.getTime().after(toDate)) {
                    lastLogged.add(Calendar.MONTH, -1);
                    offset++;
                }
            } else {
                getMostRecentRecord();
                long timeDiff = (getLastLoggedTimeStamp().getTime() - toCal.getTimeInMillis()) / 1000;
                offset = (int) (timeDiff / getRTM().getProfileInterval());
                offset = offset < 0 ? 0 : offset;           //Not allowed to be negative
            }
        }
    }

    public void getMostRecentRecord() throws IOException {
        ExtendedDataloggingTable mostRecentRecord = getRTM().getRadioCommandFactory().getMostRecentRecord();
        lastLoggedTimeStamp = mostRecentRecord.getLastLoggedTimeStamp();
    }

    public void parseBubbleUpData(byte[] data, byte[] radioAddress) throws IOException {
        if (data.length == 1 && ((data[0] & 0xFF) == 0xFF)) {
            throw new WaveFlowException("Error reading the Extended data logging table, returned 0xFF");
        }
        getGenericHeader().setRadioAddress(radioAddress);
        getGenericHeader().parse(data);
        OperatingMode operatingMode = getGenericHeader().getOperationMode();
        int offset = 23;    //Skip the rest of the generic header

        SamplingPeriod period = new SamplingPeriod(getRTM());
        period.parse(ProtocolTools.getSubArray(data, offset, offset + 1));
        int multiplier = data[offset + 2] & 0xFF;
        profileInterval = period.getSamplingPeriodInSeconds() * multiplier;
        offset += 7;        //Skip data logging parameters in the first frame

        lastLoggedTimeStamp = TimeDateRTCParser.parse(data, offset, 7, TimeZone.getDefault()).getTime();
        offset += 7;
        offset++;           //Skip the frame counter, in case of bubble up there is only one frame possible

        int[] numberOfValuesPerPort = new int[4];
        for (int port = 0; port < 4; port++) {
            numberOfValuesPerPort[port] = data[offset++] & 0xFF;
        }

        for (int port = 0; port < operatingMode.readNumberOfPorts(); port++) {
            List<Integer[]> profileDataPerPort = new ArrayList<Integer[]>();
            for (int number = 0; number < numberOfValuesPerPort[port]; number++) {
                profileDataPerPort.add(new Integer[]{ProtocolTools.getIntFromBytes(data, offset, 4), getGenericHeader().getIntervalStatus(port), getGenericHeader().getApplicationStatus().getStatus()});
                offset += 4;
            }
            profileDataForAllPorts.add(profileDataPerPort);
        }
    }

    public void parseBubbleUpData(byte[] data) throws IOException {
        parseBubbleUpData(data, null);
    }

    @Override
    public void parse(byte[] data) throws IOException {
        if (getRTM().usesInitialRFCommand()) {
            parseBubbleUpData(data);
            return;
        }

        int frameCounter = 0;
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("No profile data available yet");
        }
        getGenericHeader().setRadioAddress(null);
        getGenericHeader().parse(data);
        int offset = 23;    //Skip generic header in the first frame

        do {
            if (frameCounter == 0) {
                offset += 7;        //Skip data logging parameters in the first frame
                lastLoggedTimeStamp = TimeDateRTCParser.parse(data, offset, 7, getRTM().getTimeZone()).getTime();
                offset += 7;
            }

            frameCounter = data[offset++] & 0xFF;

            numberOfReceivedReadings += (data[offset++] & 0xFF);
            numberOfReceivedReadings += (data[offset++] & 0xFF);
            numberOfReceivedReadings += (data[offset++] & 0xFF);
            numberOfReceivedReadings += (data[offset++] & 0xFF);         //sum of received values for all channels.

            for (int i = 0; i < numberOfReceivedReadings; i++) {
                profileData.add(ProtocolTools.getIntFromBytes(data, offset, 4));
                offset += 4;
            }

        } while (frameCounter != 1);           //Frame counter = 1 indicates the last frame
    }

    @Override
    protected byte[] prepare() throws IOException {
        byte[] portBytes = new byte[]{(byte) portMask};
        byte[] numberOfReadingsBytes = ProtocolTools.getBytesFromInt(numberOfRequestedReadings, 2);
        byte[] offsetBytes = ProtocolTools.getBytesFromInt(getOffset(), 2);
        return ProtocolTools.concatByteArrays(portBytes, numberOfReadingsBytes, offsetBytes);
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedDatalogginTable;
    }
}