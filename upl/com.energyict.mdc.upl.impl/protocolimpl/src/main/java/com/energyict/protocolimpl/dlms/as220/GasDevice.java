/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.energyict.cbo.BusinessException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeterMessaging;
import com.energyict.protocolimpl.dlms.as220.gmeter.GasRegister;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Basic implementation of a GasDevice
 *
 * @author jeroen.meulemeester
 *
 */
public class GasDevice extends AS220 implements MessageProtocol{

	private static final int MAX_MBUS_CHANNELS = 4;

	private String 	emeterSerialnumber;
	private String  gmeterSerialnumber;
	private int 	gasMeterSlot = -1;
	private int		mbusProfileInterval = -1;

	private final GMeter	gMeter	= new GMeter(this);
	private GMeterMessaging messaging;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getNumberOfChannels() throws IOException {
		return getgMeter().getNrOfChannels();
	}

	/**
	 * {@inheritDoc}
	 */
    public GMeter getgMeter() {
		return gMeter;
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProtocolVersion() {
		String rev = "$Revision$" + " - " + "$Date$";
		String manipulated = "Revision " + rev.substring(rev.indexOf("$Revision: ") + "$Revision: ".length(), rev.indexOf("$ -")) + "at "
				+ rev.substring(rev.indexOf("$Date: ") + "$Date: ".length(), rev.indexOf("$Date: ") + "$Date: ".length() + 19);
    	return manipulated;
    }

	/**
	 * {@inheritDoc}
	 * @throws BusinessException
	 */
	@Override
	protected void doConnect() throws BusinessException {
		// search for the channel of the Mbus Device
		String tempSerial;
		for(int i = 0; i < MAX_MBUS_CHANNELS; i++){
			tempSerial = "";
			try {
				tempSerial = getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(i).getObisCode()).getString();
				if(tempSerial.equalsIgnoreCase(gmeterSerialnumber)){
					setGasSlotId(i + 1);
				}
			} catch (IOException e) {
				// fetch next
			}
		}

		if(getGasSlotId() == -1){
			throw new BusinessException("No MBus device found with serialNumber " + gmeterSerialnumber + " on the E-meter.");
		}
	}

    /**
     * Getter for the SlotId
     *
     * @return the slotId
     */
    public int getGasSlotId(){
    	return gasMeterSlot;
    }

    /**
     * Setter for the slotId
     */
    public void setGasSlotId(int slotId){
    	this.gasMeterSlot = slotId;
    }

    /**
     * Getter for the physical address. Start counting from zero
     *
     * @return physical address (normally the slotId minus 1)
     */
    public int getPhysicalAddress(){
    	return getGasSlotId() - 1;
    }

    /**
	 * Read the serialNumber from the Gas Device
	 *
	 * @return the serial number from the device as {@link String}
	 * @throws IOException
	 */
	public String getSerialNumber() throws IOException {
		return getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(getPhysicalAddress()).getObisCode()).getString();
	}

	/**
	 * {@inheritDoc}
	 */
	protected byte[] getSystemIdentifier(){
		return this.emeterSerialnumber.getBytes();
	}

	/**
	 * {@inheritDoc}
	 */
    public int getProfileInterval() throws IOException {
        if (mbusProfileInterval == -1) {
        	mbusProfileInterval = getgMeter().getMbusProfile().getCapturePeriod();
        }
        return mbusProfileInterval;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		validateProperties(properties);
		super.setProperties(properties);
	}

	/**
	 * {@inheritDoc}
	 */
	private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		this.gmeterSerialnumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
		this.emeterSerialnumber = properties.getProperty(MeterProtocol.NODEID, "");

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		if (to == null) {
			to = ProtocolUtils.getCalendar(getTimeZone()).getTime();
			getLogger().info("getProfileData: toDate was 'null'. Reading profildate up to: " + to);
		}
		if (validateFromToDates(from, to)) {
			return new ProfileData();
		}

		getLogger().info("Starting to read profileData [from=" + from + ", to=" + to + ", includeEvents=" + includeEvents + "]");
		return getgMeter().getProfileData(from, to, includeEvents);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The gasMeter has normally one register ( 0.x.24.2.0.255 )
	 */
	@Override
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		if (obisCode.equals(ObisCode.fromString("0.0.24.2.0.255"))) {
			ObisCode oc = getCorrectedChannelObisCode(obisCode);
			GasRegister gasRegister = new GasRegister(this);
			RegisterValue registerValue = gasRegister.getRegisterValue(oc);
			return ProtocolTools.setRegisterValueObisCode(registerValue, obisCode);
		} else {
			throw new NoSuchRegisterException(obisCode.toString() + " is not supported.");
		}
	}

	/**
	 * Construct the ObisCode with the correct channelField filled in
	 * @param oc - the ObisCode to change the B field
	 * @return the corrected ObisCode
	 */
	public ObisCode getCorrectedChannelObisCode(ObisCode oc){
		return ProtocolTools.setObisCodeField(oc, 1, (byte) getGasSlotId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<String> getRequiredKeys() {
		List<String> requiredKeys = new ArrayList<String>();
		requiredKeys.addAll(super.getRequiredKeys());
		return requiredKeys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return getMessaging().queryMessage(messageEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MessageCategorySpec> getMessageCategories() {
		return getMessaging().getMessageCategories();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String writeMessage(Message msg) {
		return getMessaging().writeMessage(msg);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String writeTag(MessageTag msgTag) {
		return getMessaging().writeTag(msgTag);
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void applyMessages(List messageEntries) throws IOException {
		getMessaging().applyMessages(messageEntries);
	}

	/**
	 * Return the {@link GMeterMessaging} object
	 */
	private GMeterMessaging getMessaging(){
		if(this.messaging == null){
			this.messaging = new GMeterMessaging(this);
		}
		return this.messaging;
	}

	/**
	 * {@inheritDoc}
	 *
	 * FirmwareUpgrade is not supported but it's there because we inherit from AS220 ...
	 */
	public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
	    return null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Currently URL's are not supported
	 */
	public boolean supportsUrls() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * We don't have database access so we don't need references
	 */
	public boolean supportsUserFileReferences() {
	    return false;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Userfiles are not supported for upgrades
	 */
	public boolean supportsUserFilesForFirmwareUpdate() {
	    return false;
	}
}
