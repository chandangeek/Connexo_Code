package com.energyict.smartmeterprotocolimpl.sdksample;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
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
 * <li> Active Import  (1.0.1.8.0.255)
 * <li> Active Export  (1.0.2.8.0.255)
 * <li> Gas Flow       (0.x.24.2.0.255)  -> will have a gasChannel for each slave, in our case <b>2</b>
 * </ol>
 * <p/>
 * <li> Monthly values (3channels) ->  ObisCode : 1.0.98.1.0.255
 * <ol>
 * <li> Active Import  (1.0.1.8.0.255)
 * <li> Active Export  (1.0.2.8.0.255)
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
 * |6channel |   -> Ch3 - 1.0.1.8.0.255 - kWh - interval Daily  (LoadProfile : 1.0.99.2.0.255)
 * |         |   -> Ch4 - 1.0.2.8.0.255 - kWh - interval Daily  (LoadProfile : 1.0.99.1.0.255)
 * |         |   -> Ch5 - 1.0.1.8.0.255 - kWh - interval Monthly (LoadProfile : 0.0.98.1.0.255)
 * |_________|   -> Ch6 - 1.0.2.8.0.255 - kWh - interval Monthly (LoadProfile : 0.0.98.1.0.255)
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
        QuarterlyHourChannelInfos.add(new ChannelInfo(0, "1.0.1.8.0.255", Unit.get("kWh"), MasterSerialNumber));
        QuarterlyHourChannelInfos.add(new ChannelInfo(1, "1.0.2.8.0.255", Unit.get("kWh"), MasterSerialNumber));

        DailyChannelInfos.add(new ChannelInfo(0, "1.0.1.8.0.255", Unit.get("kWh"), MasterSerialNumber));
        DailyChannelInfos.add(new ChannelInfo(1, "1.0.2.8.0.255", Unit.get("kWh"), MasterSerialNumber));
        DailyChannelInfos.add(new ChannelInfo(2, "0.x.24.2.0.255", Unit.get("m3"), Slave1SerialNumber));
        DailyChannelInfos.add(new ChannelInfo(3, "0.x.24.2.0.255", Unit.get("m3"), Slave2SerialNumber));

        MonthlyHourChannelInfos.add(new ChannelInfo(0, "1.0.1.8.0.255", Unit.get("kWh"), MasterSerialNumber));
        MonthlyHourChannelInfos.add(new ChannelInfo(1, "1.0.2.8.0.255", Unit.get("kWh"), MasterSerialNumber));
        MonthlyHourChannelInfos.add(new ChannelInfo(2, "0.x.24.2.0.255", Unit.get("m3"), Slave1SerialNumber));
        MonthlyHourChannelInfos.add(new ChannelInfo(3, "0.x.24.2.0.255", Unit.get("m3"), Slave2SerialNumber));

        HourlyChannelInfosSlave1.add(new ChannelInfo(0, "0.x.24.2.0.255", Unit.get("m3"), Slave1SerialNumber));
        HourlyChannelInfosSlave2.add(new ChannelInfo(0, "0.x.24.2.0.255", Unit.get("m3"), Slave2SerialNumber));
    }

    private static final ObisCode QuarterlyObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode DailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    private static final ObisCode MonthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode HourlyObisCode = ObisCode.fromString("0.x.24.3.0.255");

    private static Map<String, Map<ObisCode, List<ChannelInfo>>> LoadProfileSerialNumberChannelInfoMap = new HashMap<String, Map<ObisCode, List<ChannelInfo>>>();
    private static Map<ObisCode, List<ChannelInfo>> ChannelInfoMapMaster = new HashMap<ObisCode, List<ChannelInfo>>();
    private static Map<ObisCode, List<ChannelInfo>> ChannelInfoMapSlave1 = new HashMap<ObisCode, List<ChannelInfo>>();
    private static Map<ObisCode, List<ChannelInfo>> ChannelInfoMapSlave2 = new HashMap<ObisCode, List<ChannelInfo>>();

    static {
        ChannelInfoMapMaster.put(QuarterlyObisCode, QuarterlyHourChannelInfos);
        ChannelInfoMapMaster.put(DailyObisCode, DailyChannelInfos);
        ChannelInfoMapMaster.put(MonthlyObisCode, MonthlyHourChannelInfos);

        ChannelInfoMapSlave1.put(HourlyObisCode, HourlyChannelInfosSlave2);
        ChannelInfoMapSlave1.put(DailyObisCode, DailyChannelInfos);
        ChannelInfoMapSlave1.put(MonthlyObisCode, MonthlyHourChannelInfos);

        ChannelInfoMapSlave2.put(HourlyObisCode, HourlyChannelInfosSlave2);
        ChannelInfoMapSlave2.put(DailyObisCode, DailyChannelInfos);
        ChannelInfoMapSlave2.put(MonthlyObisCode, MonthlyHourChannelInfos);

        LoadProfileSerialNumberChannelInfoMap.put(MasterSerialNumber, ChannelInfoMapMaster);
        LoadProfileSerialNumberChannelInfoMap.put(Slave1SerialNumber, ChannelInfoMapSlave1);
        LoadProfileSerialNumberChannelInfoMap.put(Slave2SerialNumber, ChannelInfoMapSlave2);
    }

    private static Map<ObisCode, Integer> LoadProfileIntervalMap = new HashMap<ObisCode, Integer>();

    static {
        LoadProfileIntervalMap.put(QuarterlyObisCode, 900);
        LoadProfileIntervalMap.put(DailyObisCode, 86400);
        LoadProfileIntervalMap.put(MonthlyObisCode, 0);
        LoadProfileIntervalMap.put(HourlyObisCode, 3600);
    }

    private final SDKSmartMeterProtocol protocol;

    /**
     * Contains a list of <CODE>LoadProfileConfiguration</CODE> objects which corresponds with the LoadProfiles in the METER
     */
    private ArrayList<LoadProfileConfiguration> loadProfileConfigurationList;

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
            lpc.setChannelInfos(tempMap.get(lpr.getProfileObisCode()));
            lpc.setProfileInterval(LoadProfileIntervalMap.get(lpr.getProfileObisCode()));
            loadProfileConfigurationList.add(lpc);
        }
        return loadProfileConfigurationList;
    }

    /**
     * @param loadProfiles
     * @return
     * @throws IOException
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<ProfileData>();
        for (LoadProfileReader loadProfile : loadProfiles) {
            profileDataList.add(getRawProfileData(loadProfile));
        }
        return profileDataList;
    }

    /**
     * Search for the {@link com.energyict.protocol.LoadProfileConfiguration} object which is linked to the given {@link com.energyict.protocol.LoadProfileReader}.
     * The link is made using the {@link com.energyict.obis.ObisCode} from both objects.
     *
     * @param lpro the {@link com.energyict.protocol.LoadProfileReader}
     * @return the requested {@link com.energyict.protocol.LoadProfileConfiguration}
     * @throws LoadProfileConfigurationException
     *          if no corresponding object is found
     */
    private LoadProfileConfiguration getLoadProfileConfigurationForGivenReadObject(LoadProfileReader lpro) throws LoadProfileConfigurationException {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (lpc.getObisCode().equals(lpro.getProfileObisCode())) {
                return lpc;
            }
        }
        throw new LoadProfileConfigurationException("Could not find the configurationObject to read.");
    }

    /**
     * Do the actual reading of the loadProfile data from the Meter
     *
     * @param lpro the identification of which LoadProfile to read
     * @return a {@link com.energyict.protocol.ProfileData} object with the necessary intervals filled in.
     * @throws IOException
     */
    private ProfileData getRawProfileData(LoadProfileReader lpro) throws IOException {

        LoadProfileConfiguration lpc = getLoadProfileConfigurationForGivenReadObject(lpro);
        int timeInterval = Calendar.SECOND;
        int timeDuration = lpc.getProfileInterval();

        ProfileData pd = new ProfileData(lpro.getLoadProfileId());
        pd.setChannelInfos(LoadProfileSerialNumberChannelInfoMap.get(lpro.getMeterSerialNumber()).get(lpro.getProfileObisCode()));

        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTime(lpro.getStartReadingTime());

        if (timeDuration == 0) { //monthly
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            timeInterval = Calendar.MONTH;
            timeDuration = 1;
        } else if (timeDuration == 86400){
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        } else {
            ParseUtils.roundDown2nearestInterval(cal, lpc.getProfileInterval());
        }

        Calendar currentCal = Calendar.getInstance();

        String outputData = "";
        while (cal.getTime().before(currentCal.getTime())) {
            IntervalData id = new IntervalData(cal.getTime());

            for (int i = 0; i < lpc.getNumberOfChannels(); i++) {
                id.addValue(new BigDecimal(10 * 10 ^ i + Math.round(Math.random() * 10 * i)));
            }

            pd.addInterval(id);
            cal.add(timeInterval, timeDuration);

//            if (getProtocol().isSimulateRealCommunication()) {
//                ProtocolTools.delay(1);
//                String second = String.valueOf(System.currentTimeMillis() / 500);
//                second = second.substring(second.length() - 1);
//                if (!outputData.equalsIgnoreCase(second)) {
//                    outputData = second;
//                    doGenerateCommunication(1, outputData);
//                }
//            }
        }
        return pd;
    }

    public SDKSmartMeterProtocol getProtocol() {
        return protocol;
    }
}
