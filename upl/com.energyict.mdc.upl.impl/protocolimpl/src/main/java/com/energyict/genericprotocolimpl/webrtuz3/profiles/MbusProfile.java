package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.webrtuz3.MbusDevice;
import com.energyict.genericprotocolimpl.webrtuz3.WebRTUZ3;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public class MbusProfile extends AbstractDLMSProfile {

    private static final int MIN_DEVICEID = WebRTUZ3.MBUS_DEVICES.getFrom();
    private static final int MAX_DEVICEID = WebRTUZ3.MBUS_DEVICES.getTo();
    private static final ObisCode MBUS_STATUS_OBIS = ObisCode.fromString("0.0.96.10.3.255");

    private MbusDevice mbusDevice;

    public MbusProfile(){
	}

	public MbusProfile(MbusDevice mbusDevice){
		this.mbusDevice = mbusDevice;
	}

	public ProfileData getProfile(ObisCode obisCode) throws IOException, SQLException, BusinessException {
		return getProfile(obisCode, false);
	}

	public ProfileData getProfile(ObisCode mbusProfile, boolean events) throws IOException, SQLException, BusinessException{
		ProfileData profileData = new ProfileData( );
		ProfileGeneric genericProfile;


		try {
			genericProfile = getCosemObjectFactory().getProfileGeneric(mbusDevice.getProfileObisCode());
			List<ChannelInfo> channelInfos = getMbusChannelInfos(genericProfile);

			if(channelInfos.size() != 0){

				profileData.setChannelInfos(channelInfos);
				Calendar fromCalendar = null;
				Calendar channelCalendar = null;
				Calendar toCalendar = getToCalendar();

				for (int i = 0; i < getMeter().getChannels().size(); i++) {
					Channel chn = getMeter().getChannel(i);

					// TODO does not work with the 7.5

					if(!(chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS) &&
							!(chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS)){
						channelCalendar = getFromCalendar(getMeter().getChannel(i));
						if((fromCalendar == null) || (channelCalendar.before(fromCalendar))){
							fromCalendar = channelCalendar;
						}
					}
				}
				this.mbusDevice.getLogger().log(Level.INFO, "Retrieving profiledata from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
				DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
				buildProfileData(dc, profileData, genericProfile);
				ParseUtils.validateProfileData(profileData, toCalendar.getTime());
				profileData.sort();

				return profileData;
			}

		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		return profileData;
	}

	private List<ChannelInfo> getMbusChannelInfos(ProfileGeneric profile) throws IOException {
		List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
		try{
			for(int i = 0; i < profile.getCaptureObjects().size(); i++){

				// Normally the mbusData is in a separate profile
				if(isMbusRegisterObisCode(profile.getCaptureObjects().get(i).getLogicalName().getObisCode())){
                    int index = 0;
                    int channelIndex = -1;

					channelIndex = getProfileChannelNumber(index+1);
                    if (channelIndex != -1) {
                        CapturedObject co = ((CapturedObject) profile.getCaptureObjects().get(i));
                        Unit unit = getUnit(co.getLogicalName().getObisCode());
                        ChannelInfo ci = new ChannelInfo(index, channelIndex, "WebRtuKP_MBus_" + index, unit);
                        index++;
                        // We do not do the check because we know it is a cumulative value
                        //TODO need to check the wrapValue
                        ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
                        channelInfos.add(ci);
                    }
                }

			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to build the channelInfos." + e);
		}
		return channelInfos;
	}

    private boolean isMbusRegisterObisCode(ObisCode oc) {
        if (oc.getC() != 24) {
            return false;
        } else if (oc.getD() != 2) {
            return false;
        } else if ((oc.getB() < MIN_DEVICEID) || (oc.getB() > MAX_DEVICEID)) {
            return false;
        } else if ((oc.getE() < MIN_DEVICEID) || (oc.getE() > MAX_DEVICEID)) {
            return false;
        }
        return true;
    }


    private int getProfileChannelNumber(int index){
		int channelIndex = 0;
		for(int i = 0; i < getMeter().getChannels().size(); i++){

			//TODO does not work with the 7.5, only in the 8.X

		if(!(getMeter().getChannel(i).getInterval().getTimeUnitCode() == TimeDuration.DAYS) &&
				!(getMeter().getChannel(i).getInterval().getTimeUnitCode() == TimeDuration.MONTHS)){
			channelIndex++;
			if(channelIndex == index){
				return getMeter().getChannel(i).getLoadProfileIndex() -1;
			}
		}
	}
		return -1;
	}

	private void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg) throws IOException{

		if(dc.getRoot().getElements().length != 0){

			for(int i = 0; i < dc.getRoot().getElements().length; i++){
                Calendar cal = null;
				if(dc.getRoot().getStructure(i).isOctetString(0)){
					cal = new AXDRDateTime(OctetString.fromByteArray(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
				} else {
					if(cal != null){
						cal.add(Calendar.SECOND, mbusDevice.getMbus().getIntervalInSeconds());
					}
				}
				if(cal != null){

                    int profileStatus = 0;
					if(getProfileStatusChannelIndex(pg) != -1){
						profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
					}

					IntervalData currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
					if(currentInterval != null){
						pd.addInterval(currentInterval);
					}
				}
			}
		} else {
			mbusDevice.getLogger().info("No entries in MbusLoadProfile");
		}
	}

	private IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg, List channelInfos)throws IOException{

		IntervalData id = new IntervalData(cal.getTime(), status);
		int index = 0;

		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(index < channelInfos.size()){
					if(isMbusRegisterObisCode(pg.getCaptureObjects().get(i).getLogicalName().getObisCode())){
						id.addValue(new Integer(ds.getInteger(i)));
						index++;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
		}

		return id;
	}

	private int getProfileClockChannelIndex(ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjects().size(); i++){
				if(pg.getCaptureObjects().get(i).getLogicalName().getObisCode().equals(getMeterConfig().getClockObject().getObisCode())){
					return i;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
		}
		return -1;
	}

	private int getProfileStatusChannelIndex(ProfileGeneric pg) throws IOException{
		try {
			for(int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++){
                ObisCode mbusStatOC = getCorrectedObisCode(MBUS_STATUS_OBIS);
				if(pg.getCaptureObjects().get(i).getLogicalName().getObisCode().equals(mbusStatOC)){
					return i;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not retrieve the index of the profileData's status attribute.");
		}
		return -1;
	}

	protected CosemObjectFactory getCosemObjectFactory(){
		return this.mbusDevice.getWebRTU().getCosemObjectFactory();
	}

    @Override
    protected ObisCode getCorrectedObisCode(ObisCode baseObisCode) {
        return ProtocolTools.setObisCodeField(baseObisCode, 1, (byte) this.mbusDevice.getPhysicalAddress());
    }

    private Device getMeter(){
		return this.mbusDevice.getMbus();
	}

	private Calendar getToCalendar(){
		return this.mbusDevice.getWebRTU().getToCalendar();
	}

	private Calendar getFromCalendar(Channel channel){
		return this.mbusDevice.getWebRTU().getFromCalendar(channel);
	}

	private DLMSMeterConfig getMeterConfig(){
		return this.mbusDevice.getWebRTU().getMeterConfig();
	}

}
