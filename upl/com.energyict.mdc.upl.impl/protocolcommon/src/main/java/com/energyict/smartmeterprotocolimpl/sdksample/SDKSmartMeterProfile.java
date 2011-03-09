package com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.cbo.TimeDuration;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.*;

/**
 * For the <b>Kamstrup case</b> we support 1 Master and 2 Slaves.<br>
 * We need to configure 4 LoadProfiles:
 * <ul>
 * <li> 15Min values (2channels)   ->  ObisCode : 1.0.99.1.0.255
 * <ol>
 * <li> Active Import  (1.0.1.8.0.255)
 * <li> Active Export  (1.0.2.8.0.255)
 * </ol>
 * <li> Daily values (3channels)   ->  ObisCode : 1.0.99.2.0.255
 * <ol>
 * <li> Active Import  (1.0.1.8.1.255)
 * <li> Active Export  (1.0.1.8.2.255)
 * <li> Active Import  (1.0.2.8.1.255)
 * <li> Active Export  (1.0.2.8.2.255)
 * <li> Gas Flow       (0.x.24.2.0.255)  -> will have a gasChannel for each slave, in our case <b>2</b>
 * </ol>
 * <p/>
 * <li> Monthly values (3channels) ->  ObisCode : 0.0.98.1.0.255
 * <ol>
 * <li> Active Import  (1.0.1.8.1.255)
 * <li> Active Export  (1.0.1.8.2.255)
 * <li> Active Import  (1.0.2.8.1.255)
 * <li> Active Export  (1.0.2.8.2.255)
 * <li> Gas Flow       (0.x.24.2.0.255)  -> will have a gasChannel for each slave, in our case <b>2</b>
 * </ol>
 * <li> Hourly values (1channels)  ->  ObisCode : 0.x.24.3.0.255
 * <ol>
 * <li> Gas Flow       (0.x.24.2.0.255)  -> will have a gasChannel for each slave, in our case <b>2</b>
 * </ol>
 * </ul>
 * <pre>
 *  _________
 * |         |   -> Ch1 - 1.0.1.8.0.255 - kWh - interval 15min  (LoadProfile : 1.0.99.1.0.255)
 * | Master  |   -> Ch2 - 1.0.2.8.0.255 - kWh - interval 15min  (LoadProfile : 1.0.99.1.0.255)
 * |10channel|   -> Ch3 - 1.0.1.8.1.255 - kWh - interval Daily  (LoadProfile : 1.0.99.2.0.255)
 * |         |   -> Ch4 - 1.0.1.8.2.255 - kWh - interval Daily  (LoadProfile : 1.0.99.2.0.255)
 * |         |   -> Ch5 - 1.0.2.8.1.255 - kWh - interval Daily  (LoadProfile : 1.0.99.2.0.255)
 * |         |   -> Ch6 - 1.0.2.8.2.255 - kWh - interval Daily  (LoadProfile : 1.0.99.2.0.255)
 * |         |   -> Ch7 - 1.0.1.8.1.255 - kWh - interval Monthly (LoadProfile : 0.0.98.1.0.255)
 * |         |   -> Ch8 - 1.0.1.8.2.255 - kWh - interval Monthly (LoadProfile : 0.0.98.1.0.255)
 * |         |   -> Ch9 - 1.0.2.8.1.255 - kWh - interval Monthly (LoadProfile : 0.0.98.1.0.255)
 * |_________|   -> Ch10 - 1.0.2.8.2.255 - kWh - interval Monthly (LoadProfile : 0.0.98.1.0.255)
 *      |      _________
 *      |---->|         |  -> Ch1 - 0.x.24.2.0.255 - m3 - interval 1hour (LoadProfile : 0.x.24.3.0.255)
 *      |     | Slave1  |  -> Ch2 - 0.x.24.2.0.255 - m3 - interval Daily (LoadProfile : 1.0.99.2.0.255)
 *      |     |3channel |  -> Ch3 - 0.x.24.2.0.255 - m3 - interval Monthly (LoadProfile : 0.0.98.1.0.255)
 *      |     |_________|
 *      |
 *      |      _________
 *      |---->|         |  -> Ch1 - 0.x.24.2.0.255 - m3 - interval 1hour (LoadProfile : 0.x.24.3.0.255)
 *            | Slave2  |  -> Ch2 - 0.x.24.2.0.255 - m3 - interval Daily (LoadProfile : 1.0.99.2.0.255)
 *            |3channel |  -> Ch3 - 0.x.24.2.0.255 - m3 - interval Monthly (LoadProfile : 0.0.98.1.0.255)
 *            |_________|
 * </pre>
 */
public class SDKSmartMeterProfile implements MultipleLoadProfileSupport {

    private static final String MasterSerialNumber = "Master";
    private static final String Slave1SerialNumber = "Slave1";
    private static final String Slave2SerialNumber = "Slave2";

    private static final List<ChannelInfo> QuarterlyHourChannelInfos = new ArrayList<ChannelInfo>();
    private static final List<ChannelInfo> DailyChannelInfos = new ArrayList<ChannelInfo>();
    private static final List<ChannelInfo> MonthlyHourChannelInfos = new ArrayList<ChannelInfo>();
    private static final List<ChannelInfo> HourlyChannelInfosSlave1 = new ArrayList<ChannelInfo>();
    private static final List<ChannelInfo> HourlyChannelInfosSlave2 = new ArrayList<ChannelInfo>();

    static {
        QuarterlyHourChannelInfos.add(new ChannelInfo(0, "1.0.1.8.0.255", Unit.get("kWh"), MasterSerialNumber, true));
        QuarterlyHourChannelInfos.add(new ChannelInfo(1, "1.0.2.8.0.255", Unit.get("kWh"), MasterSerialNumber, true));

        DailyChannelInfos.add(new ChannelInfo(0, "1.0.1.8.1.255", Unit.get("kWh"), MasterSerialNumber, true));
        DailyChannelInfos.add(new ChannelInfo(1, "1.0.1.8.2.255", Unit.get("kWh"), MasterSerialNumber, true));
        DailyChannelInfos.add(new ChannelInfo(2, "1.0.2.8.1.255", Unit.get("kWh"), MasterSerialNumber, true));
        DailyChannelInfos.add(new ChannelInfo(3, "1.0.2.8.2.255", Unit.get("kWh"), MasterSerialNumber, true));
        DailyChannelInfos.add(new ChannelInfo(4, "0.x.24.2.0.255", Unit.get("m3"), Slave1SerialNumber, true));
        DailyChannelInfos.add(new ChannelInfo(5, "0.x.24.2.0.255", Unit.get("m3"), Slave2SerialNumber, true));

        MonthlyHourChannelInfos.add(new ChannelInfo(0, "1.0.1.8.1.255", Unit.get("kWh"), MasterSerialNumber, true));
        MonthlyHourChannelInfos.add(new ChannelInfo(1, "1.0.1.8.2.255", Unit.get("kWh"), MasterSerialNumber, true));
        MonthlyHourChannelInfos.add(new ChannelInfo(2, "1.0.2.8.1.255", Unit.get("kWh"), MasterSerialNumber, true));
        MonthlyHourChannelInfos.add(new ChannelInfo(3, "1.0.2.8.2.255", Unit.get("kWh"), MasterSerialNumber, true));
        MonthlyHourChannelInfos.add(new ChannelInfo(4, "0.x.24.2.0.255", Unit.get("m3"), Slave1SerialNumber, true));
        MonthlyHourChannelInfos.add(new ChannelInfo(5, "0.x.24.2.0.255", Unit.get("m3"), Slave2SerialNumber, true));

        HourlyChannelInfosSlave1.add(new ChannelInfo(0, "0.x.24.2.0.255", Unit.get("m3"), Slave1SerialNumber, true));
        HourlyChannelInfosSlave2.add(new ChannelInfo(0, "0.x.24.2.0.255", Unit.get("m3"), Slave2SerialNumber, true));
    }

    private static final ObisCode QuarterlyObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode DailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode MonthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode HourlyObisCode = ObisCode.fromString("0.x.24.3.0.255");

    private static final Map<String, Map<ObisCode, List<ChannelInfo>>> LoadProfileSerialNumberChannelInfoMap = new HashMap<String, Map<ObisCode, List<ChannelInfo>>>();
    private static final Map<ObisCode, List<ChannelInfo>> ChannelInfoMapMaster = new HashMap<ObisCode, List<ChannelInfo>>();
    private static final Map<ObisCode, List<ChannelInfo>> ChannelInfoMapSlave1 = new HashMap<ObisCode, List<ChannelInfo>>();
    private static final Map<ObisCode, List<ChannelInfo>> ChannelInfoMapSlave2 = new HashMap<ObisCode, List<ChannelInfo>>();

    static {
        ChannelInfoMapMaster.put(QuarterlyObisCode, QuarterlyHourChannelInfos);
        ChannelInfoMapMaster.put(DailyObisCode, DailyChannelInfos);
        ChannelInfoMapMaster.put(MonthlyObisCode, MonthlyHourChannelInfos);

        ChannelInfoMapSlave1.put(HourlyObisCode, HourlyChannelInfosSlave1);
        ChannelInfoMapSlave1.put(DailyObisCode, DailyChannelInfos);
        ChannelInfoMapSlave1.put(MonthlyObisCode, MonthlyHourChannelInfos);

        ChannelInfoMapSlave2.put(HourlyObisCode, HourlyChannelInfosSlave2);
        ChannelInfoMapSlave2.put(DailyObisCode, DailyChannelInfos);
        ChannelInfoMapSlave2.put(MonthlyObisCode, MonthlyHourChannelInfos);

        LoadProfileSerialNumberChannelInfoMap.put(MasterSerialNumber, ChannelInfoMapMaster);
        LoadProfileSerialNumberChannelInfoMap.put(Slave1SerialNumber, ChannelInfoMapSlave1);
        LoadProfileSerialNumberChannelInfoMap.put(Slave2SerialNumber, ChannelInfoMapSlave2);
    }

    private static final Map<ObisCode, Integer> LoadProfileIntervalMap = new HashMap<ObisCode, Integer>();

    static {
        LoadProfileIntervalMap.put(QuarterlyObisCode, 900);
        LoadProfileIntervalMap.put(DailyObisCode, 86400);
        LoadProfileIntervalMap.put(MonthlyObisCode, 0);
        LoadProfileIntervalMap.put(HourlyObisCode, 3600);
    }

    private final SDKSmartMeterProtocol protocol;

    private int steps;

    /**
     * Contains a list of <CODE>LoadProfileConfiguration</CODE> objects which corresponds with the LoadProfiles in the METER
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    public SDKSmartMeterProfile(SDKSmartMeterProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles from the meter.
     * Build up a list of {@link com.energyict.protocol.LoadProfileConfiguration} objects and return them so the
     * framework can validate them to the configuration in EIServer
     *
     * @param loadProfilesToRead the list of LoadProfile ObisCodes
     * @return a list of {@link com.energyict.protocol.LoadProfileConfiguration} objects corresponding with the meter
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {

        loadProfileConfigurationList = new ArrayList<LoadProfileConfiguration>();

        for (LoadProfileReader lpr : loadProfilesToRead) {
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            Map<ObisCode, List<ChannelInfo>> tempMap = LoadProfileSerialNumberChannelInfoMap.get(lpr.getMeterSerialNumber());
            if (tempMap.containsKey(lpr.getProfileObisCode())) {
                lpc.setChannelInfos(tempMap.get(lpr.getProfileObisCode()));
                lpc.setProfileInterval(LoadProfileIntervalMap.get(lpr.getProfileObisCode()));
                loadProfileConfigurationList.add(lpc);
            } else {
                this.protocol.getLogger().info("LoadProfile " + lpr.getProfileObisCode() + " is not supported.");
            }
        }
        return loadProfileConfigurationList;
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned in the <CODE>ProfileData</CODE>.
     * </p>
     * <p>
     * <b>Implementors should throw an exception if all data since {@link LoadProfileReader#getStartReadingTime()} can NOT be fetched</b>,
     * as the collecting system will update its lastReading setting based on the returned ProfileData
     * </p>
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>ProfileData</CODE> objects containing interval records
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        for (LoadProfileReader loadProfile : loadProfiles) {
            ProfileData pd = getRawProfileData(loadProfile);
            if(pd != null){
                profileDataList.add(pd);
            }
        }
        return profileDataList;
    }

    /**
     * Search for the {@link com.energyict.protocol.LoadProfileConfiguration} object which is linked to the given {@link com.energyict.protocol.LoadProfileReader}.
     * The link is made using the {@link com.energyict.obis.ObisCode} from both objects.
     *
     * @param lpro the {@link com.energyict.protocol.LoadProfileReader}
     * @return the requested {@link com.energyict.protocol.LoadProfileConfiguration}
     */
    private LoadProfileConfiguration getLoadProfileConfigurationForGivenReadObject(LoadProfileReader lpro)  {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (lpc.getObisCode().equals(lpro.getProfileObisCode()) && lpc.getMeterSerialNumber().equalsIgnoreCase(lpro.getMeterSerialNumber())) {
                return lpc;
            }
        }
        this.protocol.getLogger().info("Could not find the configurationObject to read for " + lpro.getProfileObisCode());
        return null;
    }

    /**
     * Do the actual reading of the loadProfile data from the Meter
     *
     * @param lpro the identification of which LoadProfile to read
     * @return a {@link com.energyict.protocol.ProfileData} object with the necessary intervals filled in.
     * @throws IOException when a error happens during parsing
     */
    private ProfileData getRawProfileData(LoadProfileReader lpro) throws IOException {

        LoadProfileConfiguration lpc = getLoadProfileConfigurationForGivenReadObject(lpro);
        if(lpc != null){
            int timeInterval = Calendar.SECOND;
            int timeDuration = lpc.getProfileInterval();

            ProfileData pd = new ProfileData(lpro.getLoadProfileId());
            pd.setChannelInfos(LoadProfileSerialNumberChannelInfoMap.get(lpro.getMeterSerialNumber()).get(lpro.getProfileObisCode()));

            Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
            cal.setTime(lpro.getStartReadingTime());

            if (timeDuration == 0) { //monthly
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                timeInterval = Calendar.MONTH;
                timeDuration = 1;
            } else if (timeDuration == 86400) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            } else {
                ParseUtils.roundDown2nearestInterval(cal, lpc.getProfileInterval());
            }

            Calendar currentCal = Calendar.getInstance();
            long count = 1;
            switch (new TimeDuration(timeDuration, timeInterval).getSeconds()) {
                case 900:
                    steps = 86400 / 900;
                    count = (cal.getTimeInMillis() / 900000) - (cal.getTimeInMillis() / 86400000) * steps;
                    break;
                case 3600:
                    steps = 86400 / 3600;
                    count = (cal.getTimeInMillis() / 3600000) - (cal.getTimeInMillis() / 86400000) * steps;
                    break;
                case 86400:
                    steps = 31;
                    count = (cal.getTimeInMillis() / 86400000) - (cal.getTimeInMillis() / ((long) 31 * 86400000)) * steps;
                    break;
                case (3600 * 24 * 31):
                    steps = 12;
                    count = (cal.getTimeInMillis() / ((long) 3600 * 24 * 31 * 1000)) - (cal.getTimeInMillis() / ((long) 12 * 3600 * 24 * 31 * 1000)) * steps;
                    break;
            }
            count++;
            double multiplier = (2 * Math.PI) / steps;
            while (cal.getTime().before(currentCal.getTime())) {
                IntervalData id = new IntervalData(cal.getTime());

                for (int i = 1; i <= lpc.getNumberOfChannels(); i++) {
                    id.addValue(Math.sin(count * multiplier) * i);
                }
                if (count++ >= steps) {
                    count = 1;
                }
                pd.addInterval(id);
                cal.add(timeInterval, timeDuration);
            }
            return pd;
        }
        return null;
    }

    public SDKSmartMeterProtocol getProtocol() {
        return protocol;
    }
}
