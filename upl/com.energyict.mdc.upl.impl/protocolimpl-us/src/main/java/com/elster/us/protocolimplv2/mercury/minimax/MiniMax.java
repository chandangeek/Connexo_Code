package com.elster.us.protocolimplv2.mercury.minimax;

import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.DeviceFunction;
import com.energyict.mdc.upl.DeviceProtocol;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.DeviceProtocolDialect;
import com.energyict.mdc.upl.ManufacturerInformation;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedCalendar;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedFirmwareVersion;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.upl.security.EncryptionDeviceAccessLevel;

import com.elster.us.protocolimplv2.mercury.minimax.frame.RequestFrame;
import com.elster.us.protocolimplv2.mercury.minimax.frame.ResponseFrame;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.AuditLogRecord;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.BasicResponseData;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.DMResponseData;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.ExtendedData;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.MultiReadResponseData;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.SingleReadResponseData;
import com.elster.us.protocolimplv2.mercury.minimax.utility.ObisCodeMapper;
import com.elster.us.protocolimplv2.mercury.minimax.utility.UnitMapper;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdw.amr.RegisterIdentifierById;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;
import com.energyict.protocolimplv2.security.NoOrPasswordSecuritySupport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.elster.us.protocolimplv2.mercury.minimax.Command.DM;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_STX;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.ERROR_NO_AUDIT_TRAIL_RECORDS_AVAILABLE;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_1;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_10;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_2;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_3;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_4;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_5;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_6;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_7;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_8;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_AUDIT_9;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_DATE;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_DATE_FORMAT;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_MAX_DAY_DATE;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_SERIAL_NUMBER;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_TIME;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_UOM_COR_VOL;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_UOM_DATE_FORMAT;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_UOM_ENERGY;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_UOM_PRESS;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_UOM_PRESS_DECIMALS;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_UOM_TEMP;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.OBJECT_UOM_UNC_VOL;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.RECORDS_PER_PACKET;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.arraysEqual;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ResponseValueHelper.getNumericValue;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ResponseValueHelper.isStringValue;

/**
 * The main class for the Mercury protocol
 * Currently supports MiniMax devices
 *
 * @author James Fox
 */
public class MiniMax implements DeviceProtocol {

    private MiniMaxConnection connection;
    private MiniMaxProperties properties;
    private final NoOrPasswordSecuritySupport securitySupport;
    private final PropertySpecService propertySpecService;

    private OfflineDevice offlineDevice;

    private List<ObisCode> channelObisCodes;

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private final CollectedDataFactory collectedDataFactory;

    public MiniMax(PropertySpecService propertySpecService, CollectedDataFactory collectedDataFactory) {
        this.propertySpecService = propertySpecService;
        this.properties = new MiniMaxProperties(propertySpecService);
        this.securitySupport = new NoOrPasswordSecuritySupport(propertySpecService);
        this.collectedDataFactory = collectedDataFactory;
    }

    public Logger getLogger() {
        return logger;
    }

    private void populateUnitMap() {
        List<String> registers = new ArrayList<>();
        registers.add(OBJECT_UOM_PRESS);
        registers.add(OBJECT_UOM_PRESS_DECIMALS);
        registers.add(OBJECT_UOM_TEMP);
        registers.add(OBJECT_UOM_COR_VOL);
        registers.add(OBJECT_UOM_UNC_VOL);
        registers.add(OBJECT_UOM_ENERGY);
        registers.add(OBJECT_UOM_DATE_FORMAT);

        ResponseFrame responses = getConnection().readMultipleRegisterValues(registers);
        MultiReadResponseData data = (MultiReadResponseData)responses.getData();
        for (int count = 0; count < registers.size(); count++) {
            String dataStr = data.getResponse(count);
            switch (count) {
                case 0:
                    UnitMapper.setPressureUnits(dataStr, getLogger());
                    break;
                case 1:
                    UnitMapper.setPressureDecimals(dataStr);
                    break;
                case 2:
                    UnitMapper.setTemperatureUnits(dataStr, getLogger());
                    break;
                case 3:
                    UnitMapper.setCorVolUnits(dataStr, getLogger());
                    break;
                case 4:
                    UnitMapper.setUncVolUnits(dataStr, getLogger());
                    break;
                case 5:
                    UnitMapper.setEnergyUnits(dataStr, getLogger());
                    break;
                case 6:
                    UnitMapper.setDateFormat(dataStr, getLogger());
                    break;
                default:
                    getLogger().warning("Failed to handle unit mapping: " + dataStr);
            }
        }
        UnitMapper.setupUnitMappings(getLogger());
    }

    private TimeZone getTimeZone() {
        return TimeZone.getTimeZone(properties.getTimezone());
    }

    /**
     * Gets the firmware version from the device
     * @return a String representing the firmware version
     * @throws IOException
     */
    /*
    public String getFirmwareVersion() throws IOException {
        ResponseFrame responseFrame = getConnection().readSingleRegisterValue(OBJECT_FIRMWARE_VERSION);
        BasicResponseData data = responseFrame.getData();
        if (data instanceof SingleReadResponseData) {
            return ((SingleReadResponseData)data).getValue();
        } else {
            getLogger().warning("Failed to read firmware version " + data.getError());
            return "";
        }
    }
    */

    /**
     * Gets the time from the device
     * @return a {@link Date} representation of the current time of the device
     * @throws IOException
     */
    @Override
    public Date getTime() {
        String[] registerArray = {OBJECT_TIME, OBJECT_DATE, OBJECT_DATE_FORMAT};
        ResponseFrame timeResponse = getConnection().readMultipleRegisterValues(Arrays.asList(registerArray));
        BasicResponseData data = timeResponse.getData();
        if (data instanceof MultiReadResponseData) {
            MultiReadResponseData mrrd = (MultiReadResponseData)data;
            // Get the time, date and date format strings from the response
            String timeStr = mrrd.getResponse(0);
            String dateStr = mrrd.getResponse(1);
            // Date format seems to have nothing in it. use the standard one
            //String dateFormatStr = mrrd.getResponse(2);
            // Create a date from the date string and the date format
            Date d = getDate(dateStr, timeStr);
            // Create a calendar, and then set the date and time into it

            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyyhhmmss");
            String str = format.format(d);

            format.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));

            Date d1 = null;
            try {
                d1 = format.parse(str);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(properties.getDeviceTimezone()));
            // Get the timezone that we are running in
            cal.setTime(d1);
            TimeZone tz = getTimeZone();
            cal.setTimeZone(tz);
            return cal.getTime();
        } else {
            getLogger().warning("Failed to read time " + data.getError());
            return new Date();
        }
    }

    private Date getDate(String dateStr, String timeStr) {
        try {
            String dateFormatStr = UnitMapper.getDateFormat().toPattern();
            String dateFormatStrIncTime = dateFormatStr + "-" + "hh mm ss";
            return new SimpleDateFormat(dateFormatStrIncTime).parse(dateStr+"-"+timeStr);
        } catch (ParseException pe) {
            getLogger().warning("Failed to parse the date from the device: " + dateStr + ", returning current date");
        }
        return new Date();
    }

    /**
     * Update the device time
     * @param date a {@Date} representation of the time to set into the device
     * @throws IOException
     */
    @Override
    public void setTime(Date date) {
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
        String curTime = String.format("%02d %02d %02d", hours, minutes, seconds);
        ResponseFrame response = getConnection().writeSingleRegisterValue(Consts.OBJECT_TIME, curTime);
        try {
            if (!response.isOK()) {
                throw new IOException("Failed to set the device time: " + response.getError());
            }
        } catch (IOException ioe) {
            // TODO:
        }
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles from the meter.
     * Build up a list of <CODE>LoadProfileConfiguration</CODE> objects and return them so the
     * framework can validate them to the configuration in EIServer
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>LoadProfileConfiguration</CODE> objects corresponding with the meter
     */
    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {

        // Read what is setup in the meter
        List<String> registers = new ArrayList<>();
        registers.add(OBJECT_AUDIT_1);
        registers.add(OBJECT_AUDIT_2);
        registers.add(OBJECT_AUDIT_3);
        registers.add(OBJECT_AUDIT_4);
        registers.add(OBJECT_AUDIT_5);
        registers.add(OBJECT_AUDIT_6);
        registers.add(OBJECT_AUDIT_7);
        registers.add(OBJECT_AUDIT_8);
        registers.add(OBJECT_AUDIT_9);
        registers.add(OBJECT_AUDIT_10);

        ResponseFrame responseFrame = getConnection().readMultipleRegisterValues(registers);
        MultiReadResponseData data = (MultiReadResponseData)responseFrame.getData();

        List<String> registersToMap = new ArrayList<>();

        for (int count = 0; count < registers.size(); count++) {
            registersToMap.add(data.getResponse(count));
        }

        // Get the obis codes corresponding to the registers defined in the device
        List<ObisCode> obisCodesFromDevice = ObisCodeMapper.mapDeviceChannels(registersToMap);

        this.channelObisCodes = obisCodesFromDevice;

        // What we need to do here?
        // We have a list of channels that EiServer suspects we should support
        // And we have a list of register IDs from the device, which is what it actually supports
        // So, what we need to do is:
        // 1) Convert all of the register IDs from the device into obis codes
        // 2) Look up all of the units for each of the registers
        // 3) For each of the LoadProfileReader objects received from EiServer:
        //      a) Check whether the obis codes for each of the channels under it exists in the list from the device
        //      b) If it does, then return a channel with that Obis code and the unit from the device plus all other data populated


        // Go through the list of load profiles provided from EiServer
        List<CollectedLoadProfileConfiguration> loadProfileConfigList = new ArrayList<>();
        for (LoadProfileReader lpReader : loadProfilesToRead) {

            String serialNumber = lpReader.getMeterSerialNumber();
            ObisCode obisCode = lpReader.getProfileObisCode();

            // Create a LoadProfileConfiguration to return
            CollectedLoadProfileConfiguration config = this.collectedDataFactory.createCollectedLoadProfileConfiguration(obisCode, serialNumber);
            List<ChannelInfo> channelInfosToReturn = new ArrayList<>();
            config.setChannelInfos(channelInfosToReturn);
            // These devices are always 1 hour intervals
            config.setProfileInterval(3600);
            // Get the channels for this load profile
            List<ChannelInfo> channelInfos = lpReader.getChannelInfos();
            for (ChannelInfo channelInfo : channelInfos) {
                ObisCode obisCodeFromChannelInfo = channelInfo.getChannelObisCode();

                // Check if the obis code for this channel exists in the device
                if (obisCodesFromDevice.contains(obisCodeFromChannelInfo)) {
                    // If the channel exists, we will return it with the unit defined for this channel in the device
                    channelInfo.setUnit(UnitMapper.getUnitForObisCode(obisCodeFromChannelInfo));
                    channelInfosToReturn.add(channelInfo);
                }
            }
            loadProfileConfigList.add(config);
        }
        return loadProfileConfigList;
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>. If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     */
    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {

        List<CollectedLoadProfile> profileDataList = new ArrayList<>();

        for (LoadProfileReader lpr : loadProfiles) {

            LoadProfileIdentifier lpi = new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode());
            CollectedLoadProfile profileData1 = this.collectedDataFactory.createCollectedLoadProfile(lpi);
            profileDataList.add(profileData1);

            // These are the channels we are interested in...
            List<ChannelInfo> channelInfosFromEiServer = lpr.getChannelInfos();
            List<Integer> interestedIn = new ArrayList<>();

            for (ChannelInfo channelInfo : channelInfosFromEiServer) {
                ObisCode obis = channelInfo.getChannelObisCode();
                int index = channelObisCodes.indexOf(obis);

                if (index != -1) {
                    interestedIn.add(index);
                }
            }

            List<IntervalData> intervalDatas = new ArrayList<>();

            // Construct the date and time strings to send to the device
            Date startReadingTime = lpr.getStartReadingTime();

            getLogger().info("startReadingTime: " + startReadingTime);

            Calendar startTimeCal = Calendar.getInstance();
            startTimeCal.setTime(startReadingTime);
            startTimeCal.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));

            UnitMapper.getEventDateFormat().setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
            String startDateStr = UnitMapper.getEventDateFormat().format(startReadingTime);

            int startHour = startTimeCal.get(Calendar.HOUR_OF_DAY);
            int startMinute = startTimeCal.get(Calendar.MINUTE);
            int startSecond = startTimeCal.get(Calendar.SECOND);
            String startTimeStr = String.format("%02d%02d%02d", startHour, startMinute, startSecond);

            getLogger().info("Start date: " + startDateStr + ", start time: " + startTimeStr);

            Date endReadingTime = lpr.getEndReadingTime();

            getLogger().info("endReadingTime: " + endReadingTime);

            Calendar endTimeCal = Calendar.getInstance();
            endTimeCal.setTime(endReadingTime);
            endTimeCal.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));

            UnitMapper.getEventDateFormat().setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
            String endDateStr = UnitMapper.getEventDateFormat().format(endReadingTime);

            int endHour = endTimeCal.get(Calendar.HOUR_OF_DAY);
            int endMinute = endTimeCal.get(Calendar.MINUTE);
            int endSecond = endTimeCal.get(Calendar.SECOND);
            String endTimeStr = String.format("%02d%02d%02d", endHour, endMinute, endSecond);

            getLogger().info("end date: " + endDateStr + ", end time: " + endTimeStr);

            List<ResponseFrame> auditLogs = null;
            try {
                // Construct the command params
                ByteArrayOutputStream commandParams = new ByteArrayOutputStream();
                String recPerPacPadded = String.format("%03d", RECORDS_PER_PACKET);
                commandParams.write(getBytes(recPerPacPadded));

                commandParams.write(CONTROL_STX);
                commandParams.write('*');
                commandParams.write(',');

                commandParams.write(getBytes(startDateStr));
                commandParams.write(',');
                commandParams.write(getBytes(startTimeStr));
                commandParams.write(',');
                commandParams.write(getBytes(endDateStr));
                commandParams.write(',');
                commandParams.write(getBytes(endTimeStr));
                // Construct the data
                ExtendedData eData = new ExtendedData(getBytes(DM.name()), commandParams.toByteArray());
                // Construct the frame, including the data
                RequestFrame snFrame = new RequestFrame(eData);

                // Keep a record of the last command we sent to the device
                getConnection().setLastCommandSent(DM);

                auditLogs = getConnection().sendAndReceiveFrames(snFrame);
            } catch (IOException ioe) {
                // TODO: handle this
            }



            if (auditLogs != null && auditLogs.size() == 1 && !(auditLogs.get(0).getData() instanceof DMResponseData)) {
                BasicResponseData brd = auditLogs.get(0).getData();
                if (arraysEqual(brd.getErrorCode(), getBytes(RESPONSE_OK))) {
                    // Ignore this
                } else if (arraysEqual(brd.getErrorCode(), getBytes(ERROR_NO_AUDIT_TRAIL_RECORDS_AVAILABLE))){
                    // We can probably ignore and log
                    getLogger().warning(brd.getError());
                } else {
                    //throw new IOException(brd.getError());
                    // TODO: throw the correct exception
                }
            } else {
                for (ResponseFrame frame : auditLogs) {
                    DMResponseData dmData = (DMResponseData)frame.getData();
                    List<AuditLogRecord> records = dmData.getRecords();
                    for (AuditLogRecord record : records) {
                        if (record.isIntervalRecord()) {
                            int eiStatus = 0;
                            if (record.getAlarm(AuditLogRecord.ALARM_INDEX_BATT_LOW)) {
                                eiStatus += IntervalStateBits.BATTERY_LOW;
                            }
                            if (record.getAlarm(AuditLogRecord.ALARM_INDEX_PRESS_OUT_OF_RANGE)) {
                                eiStatus += IntervalStateBits.OTHER;
                            }

                            // This timestamp has the time from the meter, but the timezone will be whatever eiserver is running in
                            Date recordTimestamp = record.getTimestamp();

                            getLogger().info("Timestamp from meter is: " + recordTimestamp);

                            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            //isoFormat.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
                            //isoFormat.setTimeZone(getTimeZone());
                            String timeStrMeterTz = isoFormat.format(recordTimestamp);

                            getLogger().info("timeStrMeterTz is: " + timeStrMeterTz);

                            isoFormat.setTimeZone(TimeZone.getTimeZone(properties.getDeviceTimezone()));
                            Date dateInMeterTz = null;
                            try {
                                dateInMeterTz = isoFormat.parse(timeStrMeterTz);
                            } catch (ParseException pe) {
                                // this is not possible, we are parsing a string we just created in the same format
                            }

                            getLogger().info("dateInMeterTz is: " + dateInMeterTz);

                            IntervalData interval = new IntervalData(dateInMeterTz, eiStatus);
                            for (int i : interestedIn) {
                                BigDecimal value;
                                try {
                                    value = new BigDecimal(record.getStuff(i).trim());
                                } catch (Throwable t) {
                                    getLogger().warning("Exception when setting value into interval data: "
                                            + t + ", setting value to 0");
                                    value = new BigDecimal(0);
                                }
                                interval.addValue(value);

                            }
                            intervalDatas.add(interval);
                        } else {
                            getLogger().info("Not handling non-interval record: " + record.getAlarmsString());
                        }
                    }
                }
            }

            profileData1.setCollectedIntervalData(intervalDatas, channelInfosFromEiServer);
        }
        return profileDataList;
    }

    public MiniMaxConnection getConnection() {
        return connection;
    }

    @Override
    public String getVersion() {
        return "$Date: 2016-06-14 09:25:49 -0400 (Tue, 14 Jun 2016) $";
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        this.properties.setAllProperties(properties);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        connection = new MiniMaxConnection(comChannel, properties, logger);
    }

    @Override
    public void terminate() {

    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION, DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return this.properties.getPropertySpecs();
    }

    @Override
    public List<PropertySpec> getSecurityProperties() {
        return securitySupport.getSecurityProperties();
    }

    @Override
    public List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return this.securitySupport.getAuthenticationAccessLevels();
    }

    @Override
    public List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return this.securitySupport.getEncryptionAccessLevels();
    }

    @Override
    public Optional<PropertySpec> getSecurityPropertySpec(String s) {
        return this.securitySupport.getSecurityPropertySpec(s);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> retVal = new ArrayList<>();
        retVal.add(new OutboundTcpIpConnectionType(this.propertySpecService));
        return retVal;
    }

    @Override
    public void logOn() {
        getConnection().doConnect();
        populateUnitMap();
    }

    @Override
    public void daisyChainedLogOn() {
        // Not implemented
    }

    @Override
    public void logOff() {
        getConnection().doDisconnect();
    }

    @Override
    public void daisyChainedLogOff() {
        // Not implemented
    }

    @Override
    public String getSerialNumber() {
        ResponseFrame responseFrame = getConnection().readSingleRegisterValue(OBJECT_SERIAL_NUMBER);
        BasicResponseData data = responseFrame.getData();
        if (data instanceof SingleReadResponseData) {
            return ((SingleReadResponseData)data).getValue();
        } else {
            getLogger().warning("Failed to read serial number " + data.getError());
            return "";
        }
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        // Not implemented
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        // Not implemented
        return null;
    }

    @Override
    public String getProtocolDescription() {
        return "Mercury MiniMax";
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> list) {
        // Not implemented
        return null;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        // Not implemented
        return null;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> list) {
        // not implemented
        return null;
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> list) {
        // Not implemented - related to messages
        return null;
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, PropertySpec propertySpec, Object o) {
        return null;
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> dialect = new ArrayList<>();
        dialect.add(new MiniMaxTcpDeviceProtocolDialect(this.propertySpecService));
        return dialect;
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties typedProperties) {
        properties.setAllProperties(typedProperties);
    }

    /**
     * Read multiple register values from the device.
     *
     * @param list The List of registers to be read
     * @return The List of CollectedRegister
     */
    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> list) {
        try {
            List<String> registerIds = ObisCodeMapper.mapRegisters(list);
            ResponseFrame response = getConnection().readMultipleRegisterValues(registerIds);
            MultiReadResponseData data = (MultiReadResponseData) response.getData();
            List<CollectedRegister> retVal = new ArrayList<>();
            Unit unit = null;
            Date eventDate;
            for (int i = 0; i < list.size(); i++) {
                eventDate = null;
                String str = data.getResponse(i);
                String obisCode = list.get(i).getObisCode().getValue();
                switch (obisCode) {
                    case ObisCodeMapper.OBIS_BATTERY_READING:
                        unit = UnitMapper.getVoltageUnits();
                        break;
                    case ObisCodeMapper.OBIS_CORRECTED_VOLUME:  // Intentional fall-through
                    case ObisCodeMapper.OBIS_MAX_DAY_CORRECTED_VOLUME:
                        unit = UnitMapper.getCorVolUnits();
                        if (obisCode.equals(ObisCodeMapper.OBIS_MAX_DAY_CORRECTED_VOLUME)) {
                            // We also have to get the event time from a register in the device
                            String maxValDate = ((SingleReadResponseData) getConnection().readSingleRegisterValue(OBJECT_MAX_DAY_DATE).getData()).getValue();
                            try {
                                eventDate = UnitMapper.getDateFormat().parse(maxValDate);
                            } catch (ParseException e) {
                                getLogger().warning("Failed to parse the date for the max corrected volume day: " + maxValDate);
                                // TODO: throw the correct type of exception
                            }
                        }
                        break;
                    case ObisCodeMapper.OBIS_UNCORRECTED_VOLUME:
                        unit = UnitMapper.getUncVolUnits();
                        break;
                }

                RegisterIdentifier registerIdentifier = new RegisterIdentifierById((int) list.get(i).getRegisterId(), list.get(i).getObisCode());
                CollectedRegister register = this.collectedDataFactory.createDefaultCollectedRegister(registerIdentifier);
                retVal.add(register);

                if (isStringValue(str)) {
                    register.setCollectedData(str);
                } else {
                    Quantity quantity = new Quantity(getNumericValue(str), unit);
                    if (eventDate == null) {
                        register.setCollectedData(quantity);
                    } else {
                        register.setCollectedData(quantity);
                        register.setReadTime(eventDate);
                        //retVal.add(new RegisterValue(list.get(i), quantity, eventDate));
                    }
                }
            }
            return retVal;
        } catch (Throwable t) {
            t.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        properties.setAllProperties(deviceProtocolSecurityPropertySet.getSecurityProperties());
    }

    public OfflineDevice getOfflineDevice() { return offlineDevice; }

    @Override
    public CollectedTopology getDeviceTopology() {
        // Only master
        return this.collectedDataFactory.createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return DeviceFunction.NONE;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public CollectedCalendar getCollectedCalendar() {
        return null;
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        return null;
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        return null;
    }

}