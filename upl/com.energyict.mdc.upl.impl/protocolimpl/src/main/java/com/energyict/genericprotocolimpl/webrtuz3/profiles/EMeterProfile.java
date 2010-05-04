package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class EMeterProfile {

	private static final ObisCode STATUS_OBISCODE = ObisCode.fromString("0.0.96.10.1.255");

    private EDevice eDevice;

    /**
     * Default constructor
     */
    public EMeterProfile(){

	}

    /**
     * Constructor to pass an EDevice
     * @param eDevice
     */
    public EMeterProfile(final EDevice eDevice){
		this.eDevice = eDevice;
	}

    /**
     * Read and parse the profileData from the eMeter, do not read the events
     * @param obisCode
     * @return
     * @throws IOException
     * @throws SQLException
     * @throws BusinessException
     */
    public ProfileData getProfile(final ObisCode obisCode) throws IOException, SQLException, BusinessException {
		return getProfile(obisCode, false);
	}

    /**
     * Read and parse the profileData from the eMeter
     * @param electricityProfile
     * @param events
     * @return
     * @throws IOException
     * @throws SQLException
     * @throws BusinessException
     */
    public ProfileData getProfile(final ObisCode electricityProfile, final boolean events) throws IOException, SQLException, BusinessException{
		final ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;

		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(electricityProfile);
			final List<ChannelInfo> channelInfos = getChannelInfos(genericProfile);

			if(channelInfos.size() != 0){
				eDevice.getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMeter().getSerialNumber());
				verifyProfileInterval(genericProfile, channelInfos);

				profileData.setChannelInfos(channelInfos);
				Calendar fromCalendar = null;
				Calendar channelCalendar = null;

				for (int i = 0; i < getMeter().getChannels().size(); i++) {
					final Channel chn = getMeter().getChannel(i);

					//TODO this does not work with the 7.5 version

					if(!(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS) &&
							!(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS)){
						channelCalendar = getFromCalendar(getMeter().getChannel(i));
						if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
							fromCalendar = channelCalendar;
						}
					}
				}

                /*
                eDevice.getLogger().log(Level.INFO, "Retrieving profiledata from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
                final DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
                 */
                eDevice.getLogger().log(Level.INFO, "Retrieving profiledata from " + fromCalendar.getTime());
                final DataContainer dc = genericProfile.getBuffer(fromCalendar);

                buildProfileData(dc, profileData, genericProfile);
				ParseUtils.validateProfileData(profileData, getToCalendar().getTime());
				profileData.sort();

//				if(webrtu.getMarkedAsBadTime()){
//					profileData.markIntervalsAsBadTime();
//				}
				// We save the profileData to a tempObject so we can store everything at the end of the communication
//				webrtu.getStoreObject().add(getMeter(), profileData);
				return profileData;

			}

		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}
		return profileData;
	}

    /**
     * Check if the profileInterval from the meter matches the profileInterval configured in EiServer
     * @param genericProfile
     * @param channelInfos
     * @throws IOException
     */
    private void verifyProfileInterval(final ProfileGeneric genericProfile, final List<ChannelInfo> channelInfos) throws IOException{
		final Iterator<ChannelInfo> it = channelInfos.iterator();
		while(it.hasNext()){
			final ChannelInfo ci = it.next();
            Channel eMeterChannel = getMeter().getChannel(ci.getId());
            if(eMeterChannel.getIntervalInSeconds() != genericProfile.getCapturePeriod()){
				throw new IOException("Interval mismatch, EIServer: " + getMeter().getIntervalInSeconds() + "s - Meter: " + genericProfile.getCapturePeriod() + "s.");
			}
		}
	}

    /**
     * Build a List of ChannelInfo's from the capturedObjectList from a given ProfileGeneric
     * @param profile
     * @return
     * @throws IOException
     */
    private List<ChannelInfo> getChannelInfos(final ProfileGeneric profile) throws IOException {
        final List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        ChannelInfo ci = null;
        int index = 0;
        int channelIndex = -1;
        try {
            List<CapturedObject> captureObjects = profile.getCaptureObjects();
            for (int i = 0; i < captureObjects.size(); i++) {

                CapturedObject capturedObject = captureObjects.get(i);
                ObisCode obisCode = capturedObject.getLogicalName().getObisCode();

                if (isValidChannelObisCode(obisCode) && !isProfileStatusObisCode(obisCode)) { // make a channel out of it
                    final CapturedObject co = ((CapturedObject) captureObjects.get(i));
                    final ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());
                    channelIndex = getProfileChannelNumber(index + 1);
                    if (channelIndex != -1) {
                        if ((su != null) && (su.getUnitCode() != 0)) {
                            ci = new ChannelInfo(index, channelIndex, "WebRtuKP_" + index, su.getUnit());
                        } else {
                            ci = new ChannelInfo(index, channelIndex, "WebRtuKP_" + index, Unit.get(BaseUnit.UNITLESS));
                        }

                        index++;
                        if (com.energyict.dlms.ParseUtils.isObisCodeCumulative(co.getLogicalName().getObisCode())) {
                            //TODO need to check the wrapValue
                            ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
                        }
                        channelInfos.add(ci);
                    }
                }

            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to build the channelInfos." + e);
        }
        return channelInfos;
    }

	/**
	 * Read the given object and return the scalerUnit.
	 * If the unit is 0(not a valid value) then return a unitLess scalerUnit.
	 * If you can not read the scalerUnit, then return a unitLess scalerUnit.
	 * @param oc
	 * @return
	 * @throws IOException
	 */
    private ScalerUnit getMeterDemandRegisterScalerUnit(final ObisCode oc) throws IOException {
        try {
            ScalerUnit su = getCosemObjectFactory().getCosemObject(oc).getScalerUnit();
            if (su != null) {
                if (su.getUnitCode() == 0) {
                    su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
                }

            } else {
                su = new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
            }
            return su;
        } catch (final IOException e) {
            e.printStackTrace();
            eDevice.getLogger().log(Level.INFO, "Could not get the scalerunit from object '" + oc + "'.");
        }
        return new ScalerUnit(Unit.get(BaseUnit.UNITLESS));
    }

    /**
     * Search the correct channel (with a matching channelInterval) 
     * @param index
     * @return
     */
    private int getProfileChannelNumber(final int index) {
        int channelIndex = 0;
        for (int i = 0; i < getMeter().getChannels().size(); i++) {

            //TODO does not work with the 7.5 version, only in the 8.X

            Channel meterChannel = getMeter().getChannel(i);
            int intervalTimeUnitCode = meterChannel.getInterval().getTimeUnitCode();
            if (!(intervalTimeUnitCode == TimeDuration.DAYS) && !(intervalTimeUnitCode == TimeDuration.MONTHS)) {
                channelIndex++;
                if (channelIndex == index) {
                    return meterChannel.getLoadProfileIndex() - 1;
                }
            }
        }
        return -1;
    }

    /**
     * Build up the real profileData from the data given in a DataContainer
     * @param dc
     * @param pd
     * @param pg
     * @throws IOException
     */
    private void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg) throws IOException{
		Calendar cal = null;
		IntervalData currentInterval = null;
		int profileStatus = 0;
		if(dc.getRoot().getElements().length != 0){

			for(int i = 0; i < dc.getRoot().getElements().length; i++){

				if(dc.getRoot().getStructure(i).isOctetString(0)){
//					cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
					cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, eDevice.getMeter().getIntervalInSeconds());
					}
				}
				if(cal != null){

                    if(getProfileStatusChannelIndex(pg) != -1){
						profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
					} else {
						profileStatus = 0;
					}

                    /*
                     Because the WebRTUZ3 is reporting an 'I am in DST' flag,
                     the profile in EiServer is messed up with 'OTHER' events.
                     We filter this flag out here.
                    */

                    profileStatus &= 0xFFFFFFF7;

					currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
					if(currentInterval != null){
						pd.addInterval(currentInterval);
					}
				}
			}
		} else {
			eDevice.getLogger().info("No entries in LoadProfile");
		}
	}

    /**
     * Check if a given obisCode is a status obisCode
     * @param oc
     * @return
     * @throws IOException
     */
    private boolean isProfileStatusObisCode(final ObisCode oc) throws IOException{
        return getCorrectedObisCode(oc).equals(getCorrectedObisCode(STATUS_OBISCODE));
	}

    /**
     * Read the intervalData elements from a given DataContainer, including the EiStatus flags
     * @param ds
     * @param cal
     * @param status
     * @param pg
     * @param channelInfos
     * @return
     * @throws IOException
     */
    private IntervalData getIntervalData(final DataStructure ds, final Calendar cal, final int status, final ProfileGeneric pg, final List channelInfos) throws IOException {

        final IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
        int index = 0;

        try {
            List<CapturedObject> captureObjects = pg.getCaptureObjects();
            for (int i = 0; i < captureObjects.size(); i++) {
                if (index < channelInfos.size()) {
                    CapturedObject capturedObject = captureObjects.get(i);
                    ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                    if (isValidChannelObisCode(obisCode) && !isProfileStatusObisCode(obisCode)) {
                        id.addValue(new Integer(ds.getInteger(i)));
                        index++;
                    }
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
        }

        return id;
    }

    /**
     * Check if it is a valid channel Obiscode
     * TODO it is the same method as the one from the {@link EMeterProfile}, maybe extract an abstract profile class for both ...
     *
     * @param obisCode - the {@link ObisCode} to check
     * @return true if you know it is a valid channelData oc, false otherwise
     */
    private boolean isValidChannelObisCode(final ObisCode obisCode){
		ObisCode oc = getCorrectedObisCode(obisCode);
        if ((oc.getA() == 1) && (((oc.getB() >= 0) && (oc.getB() <= 64)) || (oc.getB() == 128)) ) {	// Energy channels - Pulse channels (C == 82)
			return true;
		} else if(oc.getC() == 96){	// Temperature and Humidity
			if((oc.getA() == 0) && ((oc.getB() == 0) || (oc.getB() == 1)) && (oc.getD() == 9) && ((oc.getE() == 0) || (oc.getE() == 2))){
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

    /**
     * 
     * @param pg
     * @return
     * @throws IOException
     */
    private int getProfileStatusChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
                CapturedObject capturedObject = pg.getCaptureObjects().get(i);
                ObisCode obisCode = getCorrectedObisCode(capturedObject.getLogicalName().getObisCode());
                ObisCode statusObisCode = getCorrectedObisCode(STATUS_OBISCODE);
                if (obisCode.equals(statusObisCode)) {
                    return i;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's status attribute.");
		}
		return -1;
	}

    /**
     * Get from an given ProfileGeneric the index of the clockChannel by comparing the obisCodes in the
     * capturedObjectList with the obisCode of the ClockObject friomo the Meter objectList
     * 
     * @param pg
     * @return
     * @throws IOException
     */
    private int getProfileClockChannelIndex(final ProfileGeneric pg) throws IOException{
		try {
            List<CapturedObject> captureObjects = pg.getCaptureObjects();
            for(int i = 0; i < captureObjects.size(); i++){
                CapturedObject capturedObject = captureObjects.get(i);
                ObisCode obisCode = getCorrectedObisCode(capturedObject.getLogicalName().getObisCode());
                ObisCode clockObjectObisCode = getCorrectedObisCode(getMeterConfig().getClockObject().getObisCode());
                if(obisCode.equals(clockObjectObisCode)){
					return i;
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}

    /**
     * Getter for the cosemObjectFactory of the eMeter
     * @return
     */
    private CosemObjectFactory getCosemObjectFactory(){
		return this.eDevice.getCosemObjectFactory();
	}

    /**
     * Get the webRTUZ3 from the eDevice
     * @return
     */
    private Rtu getMeter(){
		return this.eDevice.getMeter();
	}

    /**
     * Get the toCalendar from the EDevice
     * @return
     */
    private Calendar getToCalendar(){
		return this.eDevice.getToCalendar();
	}

    /**
     * Get the fromCalendar from the EDevice
     * @param channel
     * @return
     */
    private Calendar getFromCalendar(final Channel channel){
		return this.eDevice.getFromCalendar(channel);
	}

    /**
     * Get the DLMSMeterConfig from the EDevice
     * @return
     */
    private DLMSMeterConfig getMeterConfig(){
		return this.eDevice.getMeterConfig();
	}

    /**
     *
     * @param baseObisCode
     * @return
     */
    private ObisCode getCorrectedObisCode(ObisCode baseObisCode) {
		return ProtocolTools.setObisCodeField(baseObisCode, 1, (byte) eDevice.getPhysicalAddress());
	}

}
