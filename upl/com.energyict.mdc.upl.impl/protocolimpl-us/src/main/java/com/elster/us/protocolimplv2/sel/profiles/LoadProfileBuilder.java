package com.elster.us.protocolimplv2.sel.profiles;

import com.energyict.mdc.meterdata.DeviceLoadProfile;
import com.energyict.mdc.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.meterdata.identifiers.LoadProfileIdentifierById;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;

import com.elster.us.protocolimplv2.sel.SEL;
import com.elster.us.protocolimplv2.sel.utility.ObisCodeMapper;
import com.elster.us.protocolimplv2.sel.utility.UnitMapper;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;

import java.util.ArrayList;
import java.util.List;


public class LoadProfileBuilder /*implements DeviceLoadProfileSupport */ {

    private SEL meterProtocol;
    /**
     * The list of <CODE>DeviceLoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the expectedLoadProfileReaders
     */
    private List<CollectedLoadProfileConfiguration> loadProfileConfigurationList;
    List<ObisCode> channelObisCodes;

    public LoadProfileBuilder(SEL meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>DeviceLoadProfileConfiguration</CODE> objects which are in the device
     */
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        LDPData results = meterProtocol.getConnection().readLoadProfileConfig(loadProfileReaders.get(0).getMeterSerialNumber());
        UnitMapper.setupUnitMappings(meterProtocol.getLogger());
        //setUnits(results);

        List<Integer> registersToMap = new ArrayList<>();
        List<String> deviceChannelNames = new ArrayList<>();

        for (int count = 0; count < results.getMeterConfig().getNumberLDPChannelsEnabled(); count++) {
            //registersToMap.add(results.getMeterConfig().getChannelNames().get(count));
            deviceChannelNames.add(results.getMeterConfig().getChannelNames().get(count));
            registersToMap.add(count + 1);
        }

        // Get the obis codes corresponding to the channels defined in the device
        List<ObisCode> obisCodesFromDevice = ObisCodeMapper.mapDeviceChannels(registersToMap);

        this.channelObisCodes = obisCodesFromDevice;

        // Go through the list of load profiles provided from EiServer
        List<CollectedLoadProfileConfiguration> loadProfileConfigList = new ArrayList<>();
        for (LoadProfileReader lpReader : loadProfileReaders) {

            String serialNumber = lpReader.getMeterSerialNumber();
            ObisCode obisCode = lpReader.getProfileObisCode();

            // Create a LoadProfileConfiguration to return
            CollectedLoadProfileConfiguration config = new DeviceLoadProfileConfiguration(obisCode, serialNumber);
            List<ChannelInfo> channelInfosToReturn = new ArrayList<>();
            config.setChannelInfos(channelInfosToReturn);
            // 5 minute intervals should be 300
            config.setProfileInterval(results.getMeterConfig().getLdarSetting());
            // Get the channels for this load profile
            List<ChannelInfo> channelInfos = lpReader.getChannelInfos();
            for (ChannelInfo channelInfo : channelInfos) {
                ObisCode obisCodeFromChannelInfo = channelInfo.getChannelObisCode();

                // Check if the obis code for this channel exists in the device
                if (obisCodesFromDevice.contains(obisCodeFromChannelInfo)) {
                    // If the channel exists, we will return it with the unit defined for this channel in the device
                    int obisOrdinal = ObisCodeMapper.getObisKeyByValue(obisCodeFromChannelInfo);
                    if (obisOrdinal >= 1 && obisOrdinal <= deviceChannelNames.size()) {
                        channelInfo.setUnit(UnitMapper.getUnitForChannelName(deviceChannelNames.get(obisOrdinal - 1)));
                    }
                    channelInfosToReturn.add(channelInfo);
                }
            }
            loadProfileConfigList.add(config);
        }
        this.loadProfileConfigurationList = loadProfileConfigList;

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
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {

        List<CollectedLoadProfile> profileDataList = new ArrayList<>();

        for (LoadProfileReader lpr : loadProfiles) {

            LoadProfileIdentifier lpi = new LoadProfileIdentifierById(lpr.getLoadProfileId(), lpr.getProfileObisCode());
            CollectedLoadProfile profileData1 = new DeviceLoadProfile(lpi);
            profileDataList.add(profileData1);

            CollectedLoadProfileConfiguration clpc = getLoadProfileConfiguration(lpr);
            int intvlLength = clpc.getProfileInterval(); //returns interval in seconds

            // TODO: set start/end times correctly using timezone
            LDPData results = meterProtocol.getConnection().readLoadProfileData(lpr);
            LoadProfileEIServerFormatter formatter = new LoadProfileEIServerFormatter(results, meterProtocol.getProperties());

            // These are the channels we are interested in...
            List<ChannelInfo> channelInfosFromEiServer = lpr.getChannelInfos();
            List<Integer> interestedIn = new ArrayList<>();
            for (ChannelInfo channelInfo : channelInfosFromEiServer) {
                if ("EOI".equalsIgnoreCase(results.getMeterConfig().getRecorderNames().get(0))) {
                    channelInfo.setCumulative();
                }
                int index;
                ObisCode obis = channelInfo.getChannelObisCode();
                index = channelObisCodes.indexOf(obis);

                if (index != -1) {
                    interestedIn.add(index);
                }
            }

            List<IntervalData> intervalDatas = formatter.getIntervalData(interestedIn);
            profileData1.setCollectedIntervalData(intervalDatas, channelInfosFromEiServer);
        }
        return profileDataList;
    }


    /**
     * Look for the <CODE>DeviceLoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>DeviceLoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    private CollectedLoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (CollectedLoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                return lpc;
            }
        }
        return null;
    }

}
