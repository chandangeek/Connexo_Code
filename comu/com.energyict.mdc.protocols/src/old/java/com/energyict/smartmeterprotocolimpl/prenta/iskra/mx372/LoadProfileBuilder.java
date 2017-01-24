package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372;

import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 23/01/12
 * Time: 15:54
 */
public class LoadProfileBuilder {

    protected static final ObisCode CLOCK_OBISCODE = ObisCode.fromString("0.0.1.0.0.255");
    protected static final ObisCode STATUS_OBISCODE = ObisCode.fromString("1.0.96.240.0.255");

    private final IskraMx372 meterProtocol;
    private final MdcReadingTypeUtilService readingTypeUtilService;

    /**
     * The list of <CODE>LoadProfileConfiguration</CODE> objects which are build from the information from the actual device, based on the expectedLoadProfileReaders.
     */
    private List<LoadProfileConfiguration> loadProfileConfigurationList;

    /**
     * Keeps track of the list of <CODE>ChannelInfo</CODE> objects for all the LoadProfiles
     */
    private Map<LoadProfileReader, List<ChannelInfo>> channelInfoMap = new HashMap<>();

    public LoadProfileBuilder(IskraMx372 meterProtocol, MdcReadingTypeUtilService readingTypeUtilService) {
        this.meterProtocol = meterProtocol;
        this.readingTypeUtilService = readingTypeUtilService;
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles for the {@link #meterProtocol}
     *
     * @param loadProfileReaders a list of definitions of expected loadProfiles to read
     * @return the list of <CODE>LoadProfileConfiguration</CODE> objects which are in the device
     * @throws java.io.IOException when error occurred during dataFetching or -Parsing
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) throws IOException {
        loadProfileConfigurationList = new ArrayList<>();
        for (LoadProfileReader lpr : loadProfileReaders) {
            this.meterProtocol.getLogger().log(Level.INFO, "Reading configuration from LoadProfile " + lpr);
            LoadProfileConfiguration lpc = new LoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getDeviceIdentifier());  // meterSerialNumber = serialNumber of the master

            try {
                ObisCode profileObisCode = lpr.getProfileObisCode();
                // LoadProfiles capturing only channels of slave devices will have 'x' as B field in the ObisCode.
                // The 'x' can be (hard-coded) replaced by 0, as the profileObisCode is fixed - regardless of the number of the slaves connected.
                if (lpr.getProfileObisCode().getB() == -1) {
                    profileObisCode = ProtocolTools.setObisCodeField(profileObisCode, 1, (byte) 0);
                }

                ProfileGeneric pg = meterProtocol.getCosemObjectFactory().getProfileGeneric(profileObisCode);
                List<ChannelInfo> channelInfos = constructChannelInfos(pg, lpr);
                lpc.setChannelInfos(channelInfos);
                lpc.setProfileInterval(pg.getCapturePeriod());
                /** The Captured period will be 0 for billing load profiles
                 *  In this case we should filter the retrieved load profile intervals
                 *  eg.: when the device is reset, a billing interval is created. As the values are cumulative, this interval should be filtered out.
                 */

                if (!channelInfoMap.containsKey(lpr)) {
                    channelInfoMap.put(lpr, channelInfos);
                }
            } catch (IOException | NullPointerException e) {
                lpc.setSupportedByMeter(false);
            }
            loadProfileConfigurationList.add(lpc);
        }
        return loadProfileConfigurationList;
    }

    private List<ChannelInfo> constructChannelInfos(final ProfileGeneric profile, LoadProfileReader lpr) throws IOException {
        final List<ChannelInfo> channelInfos = new ArrayList<>();
        List<Register> registerList = new ArrayList<>();

        try {
            for (CapturedObject capturedObject : profile.getCaptureObjects()) {
                String registerObisCodeString = capturedObject.getLogicalName().toString();
                if (capturedObject.getLogicalName().toString().equals(CLOCK_OBISCODE.toString()) || capturedObject.getLogicalName().toString().equals(STATUS_OBISCODE.toString())) {
                    // DO nothing
                } else {
                    Register register;
                    register = constructRegister(ObisCode.fromString(registerObisCodeString), lpr.getChannelInfos());
                    if (register != null) {
                        registerList.add(register);
                    }
                }
            }

            List<RegisterValue> registerValues = meterProtocol.readRegisters(registerList);
            for (RegisterValue registerValue : registerValues) {
                if (registerValue.getQuantity().getBaseUnit().getDlmsCode() != 0) {
                    ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), getLprChannelObisCode(registerValue, lpr), registerValue.getQuantity().getUnit(), registerValue.getSerialNumber(), this.readingTypeUtilService.getReadingTypeFrom(registerValue.getObisCode(), registerValue.getQuantity().getUnit()));
                    channelInfos.add(channelInfo);
                } else {
                    ChannelInfo channelInfo = new ChannelInfo(channelInfos.size(), getLprChannelObisCode(registerValue, lpr), Unit.getUndefined(), registerValue.getSerialNumber(), this.readingTypeUtilService.getReadingTypeFrom(registerValue.getObisCode(), registerValue.getQuantity().getUnit()));
                    channelInfos.add(channelInfo);
                }
            }
            return channelInfos;
        } catch (final IOException e) {
            throw new IOException("Failed to build the channelInfos.", e);
        }
    }

    private Register constructRegister(ObisCode registerObisCode,  List<ChannelInfo> channelInfos) throws IOException {
        // 1. Exact obiscode match (for obiscodes of E-meter)
        for (ChannelInfo each : channelInfos) {
            if (each.getChannelObisCode().equals(registerObisCode)) {
                return new Register(-1, registerObisCode, each.getMeterIdentifier());
            }
        }
        //2. No match found --> this obiscode belongs to a Mbus slave meter. Based on the physical address, we can retrieve the serial number of the slave.
        int physicalAddress = registerObisCode.getB() - 1;
        String serialNumberFromPhysicalAddress = meterProtocol.getSerialNumberFromPhysicalAddress(physicalAddress);
        if (serialNumberFromPhysicalAddress != null) {
            for (ChannelInfo channelInfo : channelInfos) {
                if (channelInfo.getMeterIdentifier().equals(serialNumberFromPhysicalAddress)) {
                return new Register(-1, registerObisCode, serialNumberFromPhysicalAddress); // Construct register with obiscode B-field 'x'.
                }
            }
        }
        return null;
    }

    private String getLprChannelObisCode(RegisterValue registerValue, LoadProfileReader lpr) throws IOException {
        for (ChannelInfo channelInfo : lpr.getChannelInfos()) {
            if (channelInfo.getChannelObisCode().equalsIgnoreBChannel(registerValue.getObisCode()) && (channelInfo.getMeterIdentifier().equals(registerValue.getSerialNumber()))) {
                return channelInfo.getChannelObisCode().toString();
            }
        }
        throw new IOException("Obiscode " + registerValue.getObisCode() + " not found in the LoadProfileReader " + lpr);
    }

    /**
     * <p>
     * Fetches one or more LoadProfiles from the device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels(LoadProfileReader#channelInfos) to read. If it is possible then only these channels should be read,
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
            // LoadProfiles capturing only channels of slave devices will have 'x' as B field in the ObisCode.
            // The 'x' can be (hard-coded) replaced by 0, as the profileObisCode is fixed - regardless of the number of the slaves connected.
            if (lpr.getProfileObisCode().getB() == -1) {
                lpObisCode = ProtocolTools.setObisCodeField(lpObisCode, 1, (byte) 0);
            }
            LoadProfileConfiguration lpc = getLoadProfileConfiguration(lpr);
            if (this.channelInfoMap.containsKey(lpr) && lpc != null) { // otherwise it is not supported by the meter
                profile = this.meterProtocol.getDlmsSession().getCosemObjectFactory().getProfileGeneric(lpObisCode);
                profileData = new ProfileData(lpr.getLoadProfileId());
                profileData.setChannelInfos(this.channelInfoMap.get(lpr));

                /** Retrieve the Clock, Status and Channel mask
                 *  This check is always needed, as some load profiles have both clock and status as captured objects.
                 *  Other load profiles only contain the clock.
                 **/
                List<CapturedObject> captureObjects = meterProtocol.getCosemObjectFactory().getProfileGeneric(lpObisCode).getCaptureObjects();

                int clockMask = constructClockMask(captureObjects);
                int statusMask = constructStatusMask(captureObjects);
                int channelMask = constructChannelMask(captureObjects, lpc);

                Calendar fromCalendar = Calendar.getInstance(meterProtocol.getTimeZone());
                fromCalendar.setTimeInMillis(lpr.getStartReadingTime().toEpochMilli());
                Calendar toCalendar = Calendar.getInstance(meterProtocol.getTimeZone());
                toCalendar.setTimeInMillis(lpr.getEndReadingTime().toEpochMilli());

                DLMSProfileIntervals intervals = new DLMSProfileIntervals( profile.getBufferData(fromCalendar,  toCalendar), clockMask, statusMask, channelMask, new IskraMx372ProfileIntervalStatusBits());
                profileData.setIntervalDatas(intervals.parseIntervals(lpc.getProfileInterval(), meterProtocol.getTimeZone()));

                if (lpc.getProfileInterval() == 0) {
                    profileData = filterBillingProfileData(profileData);
                }
                profileDataList.add(profileData);
            }
        }
        return profileDataList;
    }

    /**
     * Filters the billing profile data
     * eg.: each time the device is reset, a billing interval is created.
     * These intervals will be filtered out by this method.
     * @param profileData
     * @return
     */
    private ProfileData filterBillingProfileData(ProfileData profileData) {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(profileData.getChannelInfos());
        pd.setLoadProfileId(profileData.getLoadProfileId());
        List<IntervalData> list = new ArrayList<>();
        pd.setIntervalDatas(list);

        for (IntervalData intervalData : profileData.getIntervalDatas()) {
            if(checkDailyBillingTime(intervalData.getEndTime())) {
					pd.addInterval(intervalData);
            }
        }
        return pd;
    }

    /**
	 * Checks if the given date is a date at midnight
	 * @param date
	 * @return true or false
	 */
	private boolean checkDailyBillingTime(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
        return cal.get(Calendar.HOUR) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0 && cal.get(Calendar.MILLISECOND) == 0;
    }

    private int constructClockMask(List<CapturedObject> captureObjects) throws IOException {
        for (int i= 0; i < captureObjects.size() ; i++) {
            if (captureObjects.get(i).getLogicalName().toString().equals(CLOCK_OBISCODE.toString())) {
                return (int) Math.pow(2, i);
            }
        }
        throw new IOException("The Captured Object list doesn't contain the Clock object.");
    }

    private int constructStatusMask(List<CapturedObject> captureObjects) {
        for (int i = 0; i < captureObjects.size(); i++) {
            if (captureObjects.get(i).getLogicalName().toString().equals(STATUS_OBISCODE.toString())) {
                return (int) Math.pow(2, i);
            }
        }
        return 0;   // Captured Objects doesn't contain the status.
    }

    private int constructChannelMask(List<CapturedObject> captureObjects, LoadProfileConfiguration lpc) throws IOException {
        int channelMask = 0;
        Map<ObisCode,Integer> mp=new HashMap<>();
        for ( CapturedObject capturedObject : captureObjects) {
                mp.put(capturedObject.getLogicalName().getObisCode(), new Integer(mp.size()));
        }

        for (ChannelInfo channelInfo : lpc.getChannelInfos()) {
            // If the channelInfo ObisCode contians 'x' as B field - replace the 'X'. Based on the serial number, we can lookup the physical address
            if (channelInfo.getChannelObisCode().getB() == -1) {
                int fieldB = meterProtocol.getPhysicalAddressFromSerialNumber(channelInfo.getMeterIdentifier()) + 1;
                ObisCode obis = ProtocolTools.setObisCodeField(channelInfo.getChannelObisCode(), 1, (byte) fieldB);
                channelMask += (int) Math.pow(2, mp.get(obis));
            } else {
                channelMask += (int) Math.pow(2, mp.get(channelInfo.getChannelObisCode()));
            }
        }
        return channelMask;
    }

    /**
     * Look for the <CODE>LoadProfileConfiguration</CODE> in the previously build up list
     *
     * @param loadProfileReader the reader linking to the <CODE>LoadProfileConfiguration</CODE>
     * @return requested configuration
     */
    private LoadProfileConfiguration getLoadProfileConfiguration(LoadProfileReader loadProfileReader) {
        for (LoadProfileConfiguration lpc : this.loadProfileConfigurationList) {
            if (loadProfileReader.getProfileObisCode().equals(lpc.getObisCode()) && loadProfileReader.getDeviceIdentifier().equals(lpc.getDeviceIdentifier())) {
                return lpc;
            }
        }
        return null;
    }
}