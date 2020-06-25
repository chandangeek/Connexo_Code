package com.elster.us.protocolimplv2.sel;


import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.elster.us.protocolimplv2.sel.events.EventFormatter;
import com.elster.us.protocolimplv2.sel.frame.RequestFrame;
import com.elster.us.protocolimplv2.sel.frame.ResponseFrame;
import com.elster.us.protocolimplv2.sel.frame.data.BasicData;
import com.elster.us.protocolimplv2.sel.frame.data.RegisterReadResponseData;
import com.elster.us.protocolimplv2.sel.frame.data.TimeReadResponseData;
import com.elster.us.protocolimplv2.sel.profiles.LDPData;
import com.elster.us.protocolimplv2.sel.profiles.LDPParser;
import com.elster.us.protocolimplv2.sel.profiles.structure.SERData;
import com.elster.us.protocolimplv2.sel.registers.RegisterData;
import com.elster.us.protocolimplv2.sel.registers.RegisterParser;
import com.elster.us.protocolimplv2.sel.utility.ObisCodeMapper;
import com.elster.us.protocolimplv2.sel.utility.UnitMapper;
import com.elster.us.protocolimplv2.sel.utility.YModem;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.mdc.identifiers.RegisterIdentifierById;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.elster.us.protocolimplv2.sel.Consts.ACC1;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_ACC;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_EAC;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_LP;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_REG;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_SF;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_SN;
import static com.elster.us.protocolimplv2.sel.Consts.COMMAND_TIME;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_CRC;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.sel.Consts.CONTROL_STX;
import static com.elster.us.protocolimplv2.sel.Consts.EAC;
import static com.elster.us.protocolimplv2.sel.Consts.LDP_DATE_FORMAT;
import static com.elster.us.protocolimplv2.sel.Consts.LDP_YFILE_FORMAT;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_DELIVERED;
import static com.elster.us.protocolimplv2.sel.Consts.OBJECT_DIRECTION_RECEIVED;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.sel.utility.ByteArrayHelper.getString;

public class SELConnection {

    private final SELProperties properties;
    private Logger logger;
    private final CollectedDataFactory collectedDataFactory;
    private SerialPortComChannel comChannel;
    private boolean connected = false;
    private List<SERData> serCache;
    private Long[] registerValCache;
    private List<String> registerNameCache;

    private String lastCommandSent;

    public SELConnection(SerialPortComChannel comChannel, SELProperties properties, Logger logger, CollectedDataFactory collectedDataFactory) {
        this.comChannel = comChannel;
        this.properties = properties;
        this.logger = logger;
        this.collectedDataFactory = collectedDataFactory;
    }

    public List<ResponseFrame> sendAndReceiveFrames(RequestFrame toSend) {
        sendFrame(toSend);
        return receiveResponse();
    }

    public ResponseFrame sendAndReceiveFrame(RequestFrame toSend) {
        List<ResponseFrame> responseFrames = sendAndReceiveFrames(toSend);
        if (responseFrames.size() == 1) {
            return responseFrames.get(0);
        } else {
            throw CommunicationException.unexpectedResponse(new IOException("Unexpected number of response frames received: " + responseFrames.size() + ", expecting 1"));
        }
    }

    private ResponseFrame sendSignOn(String accessLevel) {
        // Construct the data
        String password = properties.getDevicePassword();
        if (accessLevel == COMMAND_EAC) {
            password = properties.getLevelEPassword();
        }
        BasicData data = new BasicData(password);
        // Construct the frame, including the data
        RequestFrame snFrame = new RequestFrame(data);
        // Keep a record of the last command we sent to the device
        lastCommandSent = COMMAND_SN;
        // Send the frame and handle the response
        sendFrame(snFrame);
        wait(2000);
        return receiveUnformattedResponse();
    }


    private ResponseFrame sendSignOff() {
        BasicData data = new BasicData(getBytes(COMMAND_SF));
        RequestFrame snFrame = new RequestFrame(data);
        lastCommandSent = COMMAND_SF;
        return sendAndReceiveFrame(snFrame);
    }

    public void sendFrame(RequestFrame frame) {
        comChannel.startWriting();
        comChannel.write(frame.toByteArray());
    }

    public void sendSELASCIICommand(String asciiCommand) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(getBytes(asciiCommand));
            outputStream.write(getBytes("\r"));
            comChannel.startWriting();
            comChannel.write(outputStream.toByteArray());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public List<ResponseFrame> receiveResponse() {
        List<ResponseFrame> receivedFrames = new ArrayList<ResponseFrame>();
        byte b = 0x00;
        comChannel.startReading();
        //Find the start of the sequence of frames
        for (int retries = 0; retries < 3; retries++) {
            wait(2000);
            while (b != CONTROL_STX && comChannel.available() > 0) {
                b = (byte) comChannel.read();
                if (b == -1) {
                    IOException ioe = new IOException("End of stream when reading from device");
                    throw ConnectionCommunicationException.unexpectedIOException(ioe);
                }
            }
            if (b == CONTROL_STX || retries == 3)
                break;
        }
        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        for (int retries = 0; retries < 3; retries++) {
            wait(2000);
            while (b != CONTROL_ETX && comChannel.available() > 0) {
                b = (byte) comChannel.read();
                if (b == -1) {
                    IOException ioe = new IOException("End of stream when reading from device");
                    throw ConnectionCommunicationException.unexpectedIOException(ioe);
                }
                if (b != CONTROL_ETX) {
                    receivedBytes.write(b);
                } else {
                    ResponseFrame response = new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent);
                    // add the frame to be returned
                    receivedFrames.add(response);
                }
            }
            if (b == CONTROL_ETX || retries >= 3)
                break;
        }
        return receivedFrames;
    }


    public List<ResponseFrame> receiveConnectResponse() {
        List<ResponseFrame> receivedFrames = new ArrayList<ResponseFrame>();
        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        byte b = 0x00;
        boolean transmit = false;
        // Find the start of the sequence of frames
        comChannel.startReading();
        while (comChannel.available() > 0) {
            b = (byte) comChannel.read();
            if (b == -1) {
                IOException ioe = new IOException("End of stream when reading from device");
                throw ConnectionCommunicationException.unexpectedIOException(ioe);
            }

            if (b == CONTROL_STX) { //start of msg
                transmit = true;
                receivedBytes = new ByteArrayOutputStream();
            } else {
                if (transmit) {
                    if (b != CONTROL_ETX) {
                        receivedBytes.write(b);
                    } else {
                        transmit = false;
                        ResponseFrame response = new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent);
                        receivedFrames.add(response);
                    }
                }
            }
        }
        return receivedFrames;
    }

    public ResponseFrame receiveUnformattedResponse() {
        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        byte b = 0x00;
        comChannel.startReading();
        while (comChannel.available() > 0) {
            b = (byte) comChannel.read();
            receivedBytes.write(b);
        }
        return new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent);
    }


    public void sendSpecialCommand(byte b) {
        comChannel.startWriting();
        comChannel.write(b);
    }


    public void doConnect() {
        lastCommandSent = COMMAND_SN;
        int numRetries = 0;
        try {
            while (!connected && numRetries < properties.getRetries()) {
                // Wait 1 second
                wait(1000);
                // verify established communication response
                receiveConnectResponse();
                // send ACC to enable Access Level 1
                sendSELASCIICommand(COMMAND_ACC);
                wait(2000);
                receiveResponse();
                // send password
                ResponseFrame signOnResponse = sendSignOn(COMMAND_ACC);
                if (getString(signOnResponse.getData().getCommandCode()).contains(ACC1)) {
                    connected = true;
                    break;
                } else {
                    numRetries++;
                    // Wait a second then go around again...
                    wait(1000);
                }
            }
        } catch (Exception ioe) {
            throw CommunicationException.protocolConnectFailed(ioe);
        }
    }

    private void wait(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ie) {
            throw CommunicationException.protocolConnectFailed(ie);
        }
    }

    public void doDisconnect() {
        ResponseFrame signOffResponse = sendSignOff();
        connected = false;
    }

    public List<CollectedLogBook> readEvents(List<LogBookReader> logBooks) {
        //all the meter events are returned in SER (sequential event report) that is returned in the file transfer along with LP data.
        List<CollectedLogBook> collectedLogBooks = new ArrayList<CollectedLogBook>();
        List<SERData> data;
        for (LogBookReader logBook : logBooks) {

            CollectedLogBook deviceLogBook = collectedDataFactory.createCollectedLogBook(logBook.getLogBookIdentifier());
            data = getSERCache(); //try to obtain the data from the cache first
            if (data == null)
                data = readLoadProfileData(new Date(), null, logBook.getMeterSerialNumber(), 0).getSerData();
            EventFormatter eventFormatter = new EventFormatter(data, this.properties);
            eventFormatter.addAllEventsFromSER(logBook, deviceLogBook);
            collectedLogBooks.add(deviceLogBook);
        }
        return collectedLogBooks;
    }

    public ResponseFrame readSingleRegisterValue(String command) {
        ByteArrayOutputStream requestParams = new ByteArrayOutputStream();

        try {
            requestParams.write(getBytes(command));
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        BasicData data = new BasicData(requestParams.toByteArray());

        RequestFrame toSend = new RequestFrame(data);
        lastCommandSent = command;
        return sendAndReceiveFrame(toSend);
    }

    public boolean changeAccessLevel(String accessLevel) {
        if (accessLevel == COMMAND_ACC) {
            sendSELASCIICommand(COMMAND_ACC);
            wait(2000);
            receiveResponse();
            return true;
        } else if (accessLevel == COMMAND_EAC) {
            sendSELASCIICommand(COMMAND_EAC);
            wait(2000);
            receiveResponse();
            // send password
            ResponseFrame signOnResponse = sendSignOn(COMMAND_EAC);
            if (getString(signOnResponse.getData().getCommandCode()).contains(EAC))
                return true;
        }
        return false;
    }

    public boolean setTime(Date date) {
        boolean returnValue = false;
        //go to level E access
        if (changeAccessLevel(COMMAND_EAC)) {
            // Get a calendar in the local timezone of the comserver
            Calendar cal = Calendar.getInstance(getTimeZone());
            // set the date into the calendar
            cal.setTime(date);
            // Set the calendar to the timezone of the device
            cal.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
            // Now get the time from the calendar
            int hours = cal.get(Calendar.HOUR_OF_DAY);
            int minutes = cal.get(Calendar.MINUTE);
            int seconds = cal.get(Calendar.SECOND);
            String curTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            //generate time sync command with params
            ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
            try {
                requestParams.write(getBytes(COMMAND_TIME + " " + curTime));
            } catch (IOException e) {
                // TODO
                e.printStackTrace();
            }
            BasicData data = new BasicData(requestParams.toByteArray());
            RequestFrame toSend = new RequestFrame(data);
            lastCommandSent = COMMAND_TIME;

            //send time sync to meter
            ResponseFrame response = sendAndReceiveFrame(toSend);
            String success = ((TimeReadResponseData) response.getData()).getValue();
            if (!success.contains("Invalid")) {
                returnValue = true;
            }
            changeAccessLevel(COMMAND_ACC);
        }
        return returnValue;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> list) {
        List<CollectedRegister> retVal = new ArrayList<>();
        Unit unit = null;
        String description = null;
        String description2 = null;
        String strUnit = null;
        String strDirection = null;
        ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
        try {
            requestParams.write(getBytes(COMMAND_REG));
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
        BasicData data = new BasicData(requestParams.toByteArray());

        RequestFrame toSend = new RequestFrame(data);
        lastCommandSent = COMMAND_REG;
        ResponseFrame response = sendAndReceiveFrame(toSend);
        String strResponse = ((RegisterReadResponseData) response.getData()).getValue();
        RegisterParser regParser = new RegisterParser();
        List<RegisterData> registerDatas = regParser.parse(strResponse);

        for (int i = 0; i < list.size(); i++) {
            OfflineRegister offlineRegister = list.get(i);
            ObisCode obisCode = offlineRegister.getObisCode();
            String obisCodeValue = obisCode.getValue();
            if (obisCodeValue.equals(ObisCodeMapper.OBIS_KWH_DELIVERED)) {
                description = "W DEL";
                description2 = "IN";
                strUnit = "Wh";
                strDirection = OBJECT_DIRECTION_DELIVERED;
            } else if (obisCodeValue.equals(ObisCodeMapper.OBIS_KVARH_DELIVERED)) {
                description = "Q DEL";
                description2 = "IN";
                strUnit = "VARh";
                strDirection = OBJECT_DIRECTION_DELIVERED;
            } else if (obisCodeValue.equals(ObisCodeMapper.OBIS_KVARH_RECEIVED)) {
                description = "OUT";
                strUnit = "VARh";
                strDirection = OBJECT_DIRECTION_RECEIVED;
            } else if (obisCodeValue.equals(ObisCodeMapper.OBIS_KWH_RECEIVED)) {
                description = "OUT";
                strUnit = "Wh";
                strDirection = OBJECT_DIRECTION_RECEIVED;
            } else {
                //TODO: unsupported register
            }

            //get register data that matches unit and direction
            RegisterData desiredRegister = null;
            for (RegisterData registerData : registerDatas) {
                if (registerData.getUnit().contains(strUnit) && (registerData.getDescription().equals(description) || registerData.getDescription().equals(description2)) && registerData.getDirection().equals(strDirection)) {
                    desiredRegister = registerData;
                    //UnitMapper.setEnergyUnits(registerData.getUnit(), logger);
                    unit = UnitMapper.getEnergyUnits(registerData.getUnit());
                    break;
                }
            }

            RegisterIdentifier registerIdentifier = new RegisterIdentifierById(offlineRegister.getRegisterId(), offlineRegister.getObisCode(), offlineRegister.getDeviceIdentifier());
            CollectedRegister register = collectedDataFactory.createDefaultCollectedRegister(registerIdentifier);
            retVal.add(register);

            if (desiredRegister != null) {
                Quantity quantity;
                if (registerValCache != null) { //pull from the cache, only works if read profiles communication task is before read registers
                    String channelName = ObisCodeMapper.getChannelNameByObis(obisCode);
                    int index = 0;
                    for (String name : registerNameCache) {
                        if (name.equals(channelName)) {
                            quantity = new Quantity(registerValCache[index], UnitMapper.getEnergyUnits(registerNameCache.get(index)));
                            register.setCollectedData(quantity);
                            register.setReadTime(desiredRegister.getTimestamp());
                            break;
                        }
                        index++;
                    }
                } else {
                    quantity = new Quantity(desiredRegister.getBucket("3P").getValue(), unit);
                    register.setCollectedData(quantity);
                    register.setReadTime(desiredRegister.getTimestamp());
                }
            }
        }
        return retVal;
    }

    public LDPData readLoadProfileData(Date start, Date end, String serialNumber, int intervalLength) {
        //FILE READ ldp_data.bin MM/DD/YYYY HH:MM:SS MM/DD/YYYY HH:MM:SS
        //TODO Refactor code duplication by code reusability
        Date startReadingTime = adjustStartTime(start, intervalLength);
        Date endReadingTime = end; //adjustEndTime(end);
        LDPData results = null;
        sendSELASCIICommand(getLoadProfileCommand(startReadingTime, endReadingTime));
        wait(2000);
        ResponseFrame response = receiveUnformattedResponse();
        if (getString(response.getData().getCommandCode()).contains("Ready to send file")) {
            sendSpecialCommand(CONTROL_CRC);
            DateFormat df = new SimpleDateFormat(LDP_YFILE_FORMAT);
            String yFile = "LDP_DATA_" + serialNumber + "_" + df.format(start) + ".BIN";
            YModem yModem = new YModem(comChannel);
            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(yFile)))) {
                yModem.receiveFileName();
                yModem.receive(yFile);
                LDPParser ldpParser = new LDPParser();
                results = ldpParser.parseYModemFile(inputStream);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw CommunicationException.unexpectedResponse(new IOException("Unexpected response: " + getString(response.getData().getCommandCode()) + ", expecting Ready to send file"));
        }
        cacheSERData(results.getSerData()); //cache serdata object in order to avoid repeating file transfer for ReadLogBooks (events)
        if (results.getMeterConfig().getRecorderNames().get(0).equalsIgnoreCase("EOI") && properties.getLPRecorder().equals("EOI")) { //cache register data if intervals are EOI/cumulative register values
            cacheRegisterData(results.getPresentValues(), results.getMeterConfig().channelNames);
        }
        return results;
    }

    private void cacheRegisterData(Long[] presentValues, List<String> channelNames) {
        registerValCache = null;
        registerNameCache = null;
        if (presentValues.length != channelNames.size()) {
            logger.warning("caching register data: # of channelNames not equal to # channel values");
            return;
        }
        registerValCache = presentValues;
        registerNameCache = channelNames;

    }

    private void cacheSERData(List<SERData> serData) {
        this.serCache = serData;
    }

    private List<SERData> getSERCache() {
        return this.serCache;
    }

    public LDPData readLoadProfileData(LoadProfileReader lpr, int intervalLength) {
        return readLoadProfileData(lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getMeterSerialNumber(), intervalLength);
    }

    public LDPData readLoadProfileConfig(String serialNumber) {
        LDPData results = null;
        sendSELASCIICommand("FILE READ ldp_data.bin");
        lastCommandSent = COMMAND_LP;
        wait(2000);
        ResponseFrame response = receiveUnformattedResponse();
        if (getString(response.getData().getCommandCode()).contains("Ready to send file")) {
            sendSpecialCommand(CONTROL_CRC);
            String yFile = "LDP_DATA_" + serialNumber + ".BIN";
            YModem yModem = new YModem(comChannel);
            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(new File(yFile)))) {
                yModem.receiveFileName();
                yModem.receive(yFile);
                LDPParser ldpParser = new LDPParser();
                results = ldpParser.parseYModemFile(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            throw CommunicationException.unexpectedResponse(new IOException("Unexpected response: " + getString(response.getData().getCommandCode()) + ", expecting Ready to send file"));
        }
        return results;
    }

    private String getLoadProfileCommand(Date start, Date end) {
        StringBuilder sb = new StringBuilder("FILE READ");
        sb.append(" ldp_data.bin ");
        sb.append(convertTimeZone(start));
        if (end != null) {
            sb.append(" " + convertTimeZone(end));
        }
        return sb.toString();
    }

    private String convertTimeZone(Date readingTime) {
        Calendar startTimeCal = Calendar.getInstance();
        startTimeCal.setTime(readingTime);
        startTimeCal.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
        SimpleDateFormat format = new SimpleDateFormat(LDP_DATE_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
        return format.format(readingTime);
    }

    private boolean isDateMoreThan1WeekOld(Date start) {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -properties.getMaxIntervalRetrievalInDays());
        if (now.getTime().before(start))
            return false;
        return true;
    }

    private Date adjustStartTime(Date readingTime, int intervalLength) {
        Calendar cal = Calendar.getInstance();
        if (readingTime == null || isDateMoreThan1WeekOld(readingTime)) {
            cal.add(Calendar.DATE, -properties.getMaxIntervalRetrievalInDays());
        } else {
            cal.setTime(readingTime);
        }
        return cal.getTime();
    }

    private Date adjustEndTime(Date readingTime) {
        if (readingTime == null)
            return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(readingTime);
        cal.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
        return cal.getTime();
    }

    private TimeZone getTimeZone() {
        if (properties.getTimezone() != null)
            return TimeZone.getTimeZone(properties.getTimezone());
        else
            return null;
    }


}
