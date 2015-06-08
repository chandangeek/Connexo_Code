package test.com.energyict.protocolimplv2.coronis.waveflow.waveflowV2;

import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.tasks.support.DeviceLoadProfileSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierById;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.DailyConsumption;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.ExtendedDataloggingTable;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.ExtendedIndexReading;

import java.math.BigDecimal;
import java.util.*;

public class ProfileDataReader implements DeviceLoadProfileSupport {

    private static final int HOURLY = 60 * 60;
    private static final int DAILY = HOURLY * 24;
    private static final int WEEKLY = DAILY * 7;
    private static final int MONTHLY = (WEEKLY * 4) - 1;
    private static final int STEPS = 29;                   //Max number of profile data entries in 1 frame!
    private WaveFlow waveFlowV2;
    private int inputsUsed = 0;
    private int interval = 0;

    public ProfileDataReader(WaveFlow waveFlowV2) {
        this.waveFlowV2 = waveFlowV2;
    }

    /**
     * Only one profile is supported: 0.0.99.1.0.255
     */
    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        List<CollectedLoadProfileConfiguration> result = new ArrayList<>();
        CollectedLoadProfileConfiguration loadProfileConfiguration;
        for (LoadProfileReader loadProfileReader : loadProfilesToRead) {
            ObisCode profileObisCode = loadProfileReader.getProfileObisCode();
            loadProfileConfiguration = MdcManager.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, waveFlowV2.getOfflineDevice().getSerialNumber());
            if (!profileObisCode.equals(DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE)) {                        //Only one LP is supported
                loadProfileConfiguration.setSupportedByMeter(false);
            }
            result.add(loadProfileConfiguration);
        }
        return result;
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        LoadProfileReader loadProfileReader = loadProfiles.get(0);
        Date from = loadProfileReader.getStartReadingTime();
        Date to = loadProfileReader.getEndReadingTime();
        ProfileData profileData = getProfileData(from, to);

        CollectedLoadProfile collectedLoadProfile = MdcManager.getCollectedDataFactory().createCollectedLoadProfile(new LoadProfileIdentifierById(loadProfileReader.getLoadProfileId(), loadProfileReader.getProfileObisCode()));
        collectedLoadProfile.setCollectedIntervalData(profileData.getIntervalDatas(), profileData.getChannelInfos());

        return Arrays.asList(collectedLoadProfile);
    }

    private Date getTimeStampOfNewestRecordMonthly(Date toDate, Date lastLoggedValue) {
        Calendar lastLogged = Calendar.getInstance(waveFlowV2.getTimeZone());
        lastLogged.setTime(lastLoggedValue);
        lastLogged.setLenient(true);

        //Go back month by month until you have the date closest to the toDate.
        while (lastLogged.getTime().after(toDate)) {
            lastLogged.add(Calendar.MONTH, -1);
        }
        return lastLogged.getTime();
    }

    final ProfileData getProfileData(Date lastReading, Date toDate) {
        if (toDate == null || toDate.after(new Date())) {
            toDate = new Date();
        }
        boolean monthly = (getProfileIntervalInSeconds() >= MONTHLY);

        int nrOfIntervals = 4;
        if (!waveFlowV2.getWaveFlowProperties().usesInitialRFCommand()) {
            nrOfIntervals = getNrOfIntervals(lastReading, toDate, monthly);
        }

        Date lastLoggedValue = new Date();
        List<Long[]> rawValues = new ArrayList<>();

        long startOffset = -1;
        long initialOffset = -1;
        int indexFirst = 0;
        boolean daily = false;
        DailyConsumption dailyConsumption = null;

        if (waveFlowV2.getWaveFlowProperties().getInitialRFCommand() == AbstractRadioCommand.RadioCommandId.ExtendedIndexReading.getCommandId()) {
            ExtendedIndexReading extendedIndexReading = waveFlowV2.getRadioCommandFactory().readExtendedIndexConfiguration();
            lastLoggedValue = extendedIndexReading.getDateOfLastLoggedValue();
            rawValues = extendedIndexReading.getLast4LoggedIndexes();
            initialOffset = 0;
        } else if (waveFlowV2.getWaveFlowProperties().getInitialRFCommand() == AbstractRadioCommand.RadioCommandId.DailyConsumption.getCommandId()) {
            daily = true;
            dailyConsumption = waveFlowV2.getRadioCommandFactory().readDailyConsumption();
            lastLoggedValue = dailyConsumption.getLastLoggedReading();
            initialOffset = 0;
        } else {
            int initialNrOfIntervals = nrOfIntervals;

            //Get the profile data for all selected input channels, in case of periodic/weekly/monthly measuring.
            for (int i = 0; i < getNumberOfInputsUsed(); i++) {
                nrOfIntervals = initialNrOfIntervals;
                int counter = 0;
                Long[] values = new Long[0];
                while (nrOfIntervals > 0) {
                    if (startOffset == -1) {
                        ExtendedDataloggingTable table = waveFlowV2.getRadioCommandFactory().readExtendedDataloggingTable(i + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), toDate);
                        table.setMonthly(monthly);
                        values = ProtocolTools.concatLongArrays(values, table.getReadingsInputs());
                        startOffset = table.getOffset();
                        initialOffset = startOffset;
                        lastLoggedValue = table.getMostRecentRecordTimeStamp();
                        indexFirst = table.getNumberOfFirstIndex();
                        if (table.getNrOfReadings()[i] < getSteps(nrOfIntervals)) {
                            break;   //To avoid invalid offsets in the next iteration
                        }
                    } else {
                        long offset;
                        if (startOffset == 0) {
                            offset = indexFirst - (startOffset + getSteps(nrOfIntervals) * counter);
                        } else {
                            offset = (startOffset - getSteps(nrOfIntervals) * counter);
                        }
                        offset = (offset == indexFirst ? 0 : offset);   //Offset = 0 represents the highest record number
                        if (offset < 0) {
                            break;
                        }
                        ExtendedDataloggingTable table = waveFlowV2.getRadioCommandFactory().readExtendedDataloggingTable(i + 1, (nrOfIntervals < getSteps(nrOfIntervals) ? nrOfIntervals : getSteps(nrOfIntervals)), toDate, offset);
                        values = ProtocolTools.concatLongArrays(values, table.getReadingsInputs());
                        if (table.getNrOfReadings()[i] < getSteps(nrOfIntervals)) {
                            break;   //To avoid invalid offsets in the next iteration
                        }
                    }
                    counter++;
                    nrOfIntervals -= getSteps(nrOfIntervals);
                }
                rawValues.add(values);
            }
        }
        return parseProfileData(!daily, daily, monthly, lastLoggedValue, initialOffset, indexFirst, dailyConsumption, lastReading, toDate, rawValues);
    }

    //The parsing of the values.
    //This method can be used after a request or for a bubble up frame containing daily profile data.

    public ProfileData parseProfileData(boolean requestsAllowed, boolean daily, boolean monthly, Date lastLoggedValue, long initialOffset, int indexFirst, DailyConsumption dailyConsumption, Date lastReading, Date toDate, List<Long[]> rawValues) {
        ProfileData profileData = new ProfileData();
        List<ChannelInfo> channelInfos = new ArrayList<>();
        TimeZone timeZone = waveFlowV2.getTimeZone();
        if (timeZone == null) {
            timeZone = TimeZone.getDefault();
        }

        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setLenient(true);

        int channelId = 0;
        for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
            Unit unit = waveFlowV2.getPulseWeight(inputId, requestsAllowed).getUnit();
            ChannelInfo channelInfo = new ChannelInfo(channelId++, String.valueOf(inputId + 1), unit);       //Channel name: 1, 2, 3 or 4
            channelInfo.setCumulative();
            channelInfos.add(channelInfo);
        }
        profileData.setChannelInfos(channelInfos);

        // initialize calendar
        if (waveFlowV2.getWaveFlowProperties().usesInitialRFCommand()) {
            calendar.setTime(lastLoggedValue);
        } else {
            if (!daily & !monthly) {
                calendar.setTime(getTimeStampOfNewestRecord(lastLoggedValue, (initialOffset == 0 ? 0 : indexFirst - initialOffset)));
            } else if (daily) {
                calendar.setTime(dailyConsumption.getLastLoggedReading());
            } else if (monthly) {
                calendar.setTime(getTimeStampOfNewestRecordMonthly(toDate, lastLoggedValue));
            }
        }

        calendar = roundTimeStamps(monthly, calendar, getProfileIntervalInSeconds());
        int flags = checkBadTime(calendar);

        int nrOfReadings;
        if (!daily) {
            nrOfReadings = rawValues.get(0).length;
        } else {
            nrOfReadings = getNumberOfDailyValues();  //a fixed amount for the daily values table, see documentation
        }

        List<IntervalData> intervalDatas = new ArrayList<>();
        for (int index = 0; index < nrOfReadings; index++) {
            List<IntervalValue> intervalValues = new ArrayList<>();

            if (!daily) {
                for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
                    int weight = waveFlowV2.getPulseWeight(inputId, requestsAllowed).getWeight();
                    Long value = rawValues.get(inputId)[index];
                    if (value != -1) {  //Value 0xFFFFFFFF (-1) is not a valid value
                        BigDecimal bd = new BigDecimal(weight * value);
                        intervalValues.add(new IntervalValue(bd, 0, flags));    //The module doesn't send any information about the value's status..
                    }
                }
            } else {
                for (int inputId = 0; inputId < getNumberOfInputsUsed(); inputId++) {
                    int weight = waveFlowV2.getPulseWeight(inputId, requestsAllowed).getWeight();
                    long value = dailyConsumption.getReceivedValues()[inputId][index];
                    if (value != -1) {  //Value 0xFFFFFFFF (-1) is not a valid value
                        BigDecimal bd = new BigDecimal(weight * value);
                        intervalValues.add(new IntervalValue(bd, 0, flags));
                    }
                }
            }
            //Don't add the record if it doesn't belong in the requested interval
            if ((daily && !requestsAllowed) || (calendar.getTime().before(toDate) && calendar.getTime().after(lastReading))) {
                if (!intervalValues.isEmpty()) {
                    intervalDatas.add(new IntervalData(calendar.getTime(), 0, 0, 0, intervalValues));
                }
            }

            if (!monthly) {
                calendar.add(Calendar.SECOND, -1 * getProfileIntervalInSeconds() * (daily ? getNumberOfInputsUsed() : 1));   //Go back 4 intervals in case of daily consumption & 4 ports
            } else {
                calendar.add(Calendar.MONTH, -1);
            }
        }
        profileData.setIntervalDatas(intervalDatas);
        return profileData;
    }

    /**
     * If the timestamp of the newest LP interval deviates more than X minutes (X = profile interval) from the current time, indicate the LP entries as 'bad time'.
     * Also add an event in this case.
     */
    private int checkBadTime(Calendar calendar) {
        Calendar now = Calendar.getInstance(calendar.getTimeZone());
        if (Math.abs(calendar.getTimeInMillis() - now.getTimeInMillis()) > (getProfileIntervalInSeconds() * 1000)) {
            return IntervalStateBits.BADTIME;
        }
        return IntervalStateBits.OK;
    }

    private Calendar roundTimeStamps(boolean monthly, Calendar calendar, int profileIntervalInSeconds) {
        if (waveFlowV2.getWaveFlowProperties().isRoundDownToNearestInterval()) {
            if (monthly || profileIntervalInSeconds == WEEKLY || profileIntervalInSeconds == DAILY) {
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
            } else {
                if (!ParseUtils.isOnIntervalBoundary(calendar, profileIntervalInSeconds)) {
                    ParseUtils.roundDown2nearestInterval(calendar, profileIntervalInSeconds);
                }
            }
        }
        return calendar;
    }

    /**
     * If multiFrame mode is enabled, the number of values that can be requested in one time is not limited.
     * If it's disabled (due to the use of repeaters), data has to be requested in steps.
     */
    private int getSteps(int nrOfIntervals) {
        return waveFlowV2.getWaveFlowProperties().isEnableMultiFrameMode() ? nrOfIntervals : STEPS;
    }

    /**
     * Returns the number of DAILY values stored in the module.
     * If there's only 1 input channel used, this number is 24.
     * For 2 input channels, there's only 12 values each, etc.
     */
    private int getNumberOfDailyValues() {
        return 24 / getNumberOfInputsUsed();
    }

    /**
     * Only request the number of channels (it's included in the operation mode) if it is not known yet
     */
    private int getNumberOfInputsUsed() {
        if (inputsUsed == 0) {
            inputsUsed = waveFlowV2.getParameterFactory().readOperatingMode().getNumberOfInputsUsed();
        }
        return inputsUsed;
    }

    public void setNumberOfInputsUsed(int value) {
        inputsUsed = value;
    }

    private int getProfileIntervalInSeconds() {
        if (interval == 0) {
            interval = waveFlowV2.getProfileInterval();
        }
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    private Date getTimeStampOfNewestRecord(Date lastLoggedValue, long offset) {
        long timeStamp = lastLoggedValue.getTime();
        timeStamp -= 1000 * (offset * getProfileIntervalInSeconds());
        return new Date(timeStamp);
    }

    /**
     * Getter for the number of profile data entries that should be read.
     */
    private int getNrOfIntervals(Date lastReading, Date toDate, boolean monthly) {

        //The monthly logging doesn't have a fixed time interval.
        if (monthly) {
            Calendar checkDate = new GregorianCalendar(waveFlowV2.getTimeZone());
            checkDate.setTime(toDate);
            checkDate.setLenient(true);
            int numberOfIntervals = 0;
            while (checkDate.getTime().after(lastReading)) {
                checkDate.add(Calendar.MONTH, -1);
                numberOfIntervals++;
            }
            return numberOfIntervals;
        }

        //In case of periodic or weekly logging, calculate the number based on the interval in seconds.
        return (int) (((toDate.getTime() - lastReading.getTime()) / 1000) / getProfileIntervalInSeconds()) + 1;
    }

    @Override
    public Date getTime() {
        return waveFlowV2.getTime();
    }
}