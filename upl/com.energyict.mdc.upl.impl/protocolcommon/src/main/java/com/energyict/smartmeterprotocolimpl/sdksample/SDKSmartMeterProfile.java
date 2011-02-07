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
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 13:32:13
 */
public class SDKSmartMeterProfile implements MultipleLoadProfileSupport {

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
     * @param loadProfileObisCodes the list of LoadProfile ObisCodes
     * @return a list of {@link com.energyict.protocol.LoadProfileConfiguration} objects corresponding with the meter
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileObisCodes) {

        loadProfileConfigurationList = new ArrayList<LoadProfileConfiguration>();

        LoadProfileConfiguration lpc = new LoadProfileConfiguration(loadProfileObisCodes.get(0).getProfileObisCode(), loadProfileObisCodes.get(0).getMeterSerialNumber());
        Map<ObisCode, Unit> channelUnitMap = new HashMap<ObisCode, Unit>();
        channelUnitMap.put(ObisCode.fromString("1.0.1.8.1.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("1.0.1.8.2.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("1.0.2.8.1.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("1.0.2.8.2.255"), Unit.get("kWh"));
        channelUnitMap.put(ObisCode.fromString("0.x.24.2.1.255"), Unit.get("m3"));
        channelUnitMap.put(ObisCode.fromString("0.x.24.2.1.255"), Unit.get("m3"));
        lpc.setChannelUnits(channelUnitMap);
        lpc.setProfileInterval(86400);
        loadProfileConfigurationList.add(lpc);

        lpc = new LoadProfileConfiguration(loadProfileObisCodes.get(1).getProfileObisCode(), loadProfileObisCodes.get(1).getMeterSerialNumber());
        lpc.setProfileInterval(0); //we set 0 as interval because the monthly profile has an asynchronous capture period

        // we use the same channelMap ...
        lpc.setChannelUnits(channelUnitMap);
        loadProfileConfigurationList.add(lpc);

        return loadProfileConfigurationList;
    }

    /**
     * 
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

        List<ChannelInfo> channelInfoList = new ArrayList<ChannelInfo>();
        int channelCounter = 0;
        for (Map.Entry<ObisCode, Unit> entry : lpc.getChannelUnits().entrySet()) {
            channelInfoList.add(new ChannelInfo(channelCounter, channelCounter, entry.getKey().toString(), entry.getValue()));
            channelCounter++;
        }

        ProfileData pd = new ProfileData(lpro.getLoadProfileId());
        pd.setChannelInfos(channelInfoList);

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

            if (getProtocol().getProtocolProperties().isSimulateRealCommunication()) {
                ProtocolTools.delay(1);
                String second = String.valueOf(System.currentTimeMillis() / 500);
                second = second.substring(second.length() - 1);
                if (!outputData.equalsIgnoreCase(second)) {
                    outputData = second;
                    getProtocol().doGenerateCommunication(1, outputData);
                }
            }
        }
        return pd;
    }

    public SDKSmartMeterProtocol getProtocol() {
        return protocol;
    }
}
