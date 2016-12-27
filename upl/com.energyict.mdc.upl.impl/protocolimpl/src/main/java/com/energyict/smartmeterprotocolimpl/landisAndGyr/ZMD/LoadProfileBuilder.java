package com.energyict.smartmeterprotocolimpl.landisAndGyr.ZMD;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 14/12/11
 * Time: 9:13
 */
public class LoadProfileBuilder {

    protected static final ObisCode CLOCK_OBISCODE = ObisCode.fromString("0.0.1.0.0.255");
    protected static final ObisCode STATUS_OBISCODE = ObisCode.fromString("0.0.96.240.12.255");

    private ZMD meterProtocol;

    /**
     * The list of LoadProfileReaders which are expected to be fetched
     */
    private List<LoadProfileReader> expectedLoadProfileReaders;

    /**
     * The list of <CODE>LoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the {@link #expectedLoadProfileReaders}
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<>();

    private Map<LoadProfileReader, int[]> channelMaskMap = new HashMap<>();

    public LoadProfileBuilder(ZMD meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>LoadProfileConfiguration</CODE> objects which are in the device
     * @throws java.io.IOException when error occurred during dataFetching or -Parsing
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) throws IOException {
        expectedLoadProfileReaders = loadProfileReaders;
        loadProfileConfigurationList = new ArrayList<>();

        for (LoadProfileReader lpr : expectedLoadProfileReaders) {
            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), meterProtocol.getMeterSerialNumber());

            try {
                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), lpr.getProfileObisCode());
                ProfileGeneric pg = new ProfileGeneric(meterProtocol, new ObjectReference(uo.getBaseName()));

                List<ChannelInfo> channelInfos = constructChannelInfos(pg, lpr);
                lpc.setChannelInfos(channelInfos);
                lpc.setProfileInterval(pg.getCapturePeriod());

                if (! channelInfoMap.containsKey(lpr)){
                    channelInfoMap.put(lpr, channelInfos);
                }
            } catch (NestedIOException e) {
                if (ProtocolTools.getRootCause(e) instanceof ConnectionException || ProtocolTools.getRootCause(e) instanceof DLMSConnectionException) {
                    throw e;    // In case of a connection exception (of which we cannot recover), do throw the error.
                }
                lpc.setSupportedByMeter(false);
            } catch (IOException | NullPointerException e) {
                lpc.setSupportedByMeter(false);
            }
            loadProfileConfigurationList.add(lpc);
        }
        return loadProfileConfigurationList;
    }

    /**
     * Construct a list of <CODE>ChannelInfos</CODE>.
     */
    private List<ChannelInfo> constructChannelInfos(ProfileGeneric profileGeneric, LoadProfileReader lpr) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        List<CapturedObject> captureObjects = profileGeneric.getCaptureObjects();
        List<Register> registerList = new ArrayList<>();
        String channelMask = new String();
        int clockMask = -1;
        int statusMask = -1;

        int i = 1;
        for (CapturedObject capturedObject : captureObjects) {
            if (capturedObject.getLogicalName().toString().equals(CLOCK_OBISCODE.toString())) {
                channelMask = "0" + channelMask;
                clockMask = i;
                // DO nothing
            } else if (capturedObject.getLogicalName().toString().equals(STATUS_OBISCODE.toString())) {
                channelMask = "0" + channelMask;
                statusMask = i;
                // DO nothing
            } else {
                channelMask = "1" + channelMask;
                String registerObisCodeString = capturedObject.getLogicalName().toString();
                // Add register to the list
                registerList.add(new Register(-1, ObisCode.fromString(registerObisCodeString), meterProtocol.getMeterSerialNumber()));
            }
            i ++;
        }

        channelMaskMap.put(lpr, new int[] {clockMask, statusMask, Integer.parseInt(channelMask, 2)});
        List<RegisterValue> registerValues = meterProtocol.readRegisters(registerList);
        for (RegisterValue channelInformation : registerValues) {
            if (channelInformation.getQuantity().getBaseUnit().getDlmsCode() != 0) {
                ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), channelInformation.getObisCode().toString(), channelInformation.getQuantity().getUnit(), channelInformation.getSerialNumber(), ParseUtils.isObisCodeCumulative(channelInformation.getObisCode()));
                if (channelInformation.getQuantity().getUnit().isUndefined()) {
                    channelInfo.setMultiplier(new BigDecimal(new BigInteger("1"), -channelInformation.getQuantity().getUnit().getScale()));
                }
                channelInfos.add(channelInfo);
            } else {
                ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), channelInformation.getObisCode().toString(), Unit.getUndefined(), channelInformation.getSerialNumber(), true);
                channelInfos.add(channelInfo);
            }
        }
        return channelInfos;
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
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
     * @throws java.io.IOException if a communication or parsing error occurred
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileDataList = new ArrayList<>();
        ProfileGeneric profile;
        ProfileData profileData;
        for (LoadProfileReader lpr : loadProfiles) {
            ObisCode lpObisCode = lpr.getProfileObisCode();
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));

                Calendar fromCalendar = Calendar.getInstance(meterProtocol.getTimeZone());
                fromCalendar.setTime(lpr.getStartReadingTime());
                Calendar toCalendar = Calendar.getInstance(meterProtocol.getTimeZone());
                toCalendar.setTime(lpr.getEndReadingTime());

                int[] channelMask = channelMaskMap.get(lpr);
                if (channelMask == null) {
                    throw new IOException("Failed to build the load profile data: Invalid ChannelMask!");
                }
                DLMSProfileIntervals intervals = new DLMSProfileIntervals(profile.getBufferData(fromCalendar, toCalendar), channelMask[0], channelMask[1], channelMask[2], new ZMDProfileIntervalStatusBits());
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval(), meterProtocol.getTimeZone()));
                profileDataList.add(profileData);
            }
        }
        return profileDataList;
    }

    /**
     * Look for the <CODE>LoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>LoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    private LoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getMeterSerialNumber().equalsIgnoreCase(lpc.getMeterSerialNumber())) {
                return lpc;
            }
        }
        return null;
    }
}