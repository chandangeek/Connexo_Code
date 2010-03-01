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
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeterMessaging;

/**
 * @author jeroen.meulemeester
 *
 */
public class GasDevice extends AS220 {

	private static final int EMETERSERIAL = 0;
	private static final int SLOTID = 1;
	private static final int MAX_MBUS_CHANNELS = 4;

	private String 	emeterSerialnumber;
	private String  gmeterSerialnumber;
	private int 	gasMeterSlot = -1;
	private int		mbusProfileInterval = -1;

	private final GMeter	gMeter	= new GMeter(this);

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
		properties.list(System.out);
		this.gmeterSerialnumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
		this.emeterSerialnumber = properties.getProperty(MeterProtocol.NODEID, "");

	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		return getgMeter().getProfileData(from, to, includeEvents);
	}

	/**
	 * Construct the ObisCode with the correct channelField filled in
	 *
	 * @param oc
	 * 			- the ObisCode to change the B field
	 *
	 * @return the corrected ObisCode
	 */
	public ObisCode getCorrectedChannelObisCode(ObisCode oc){
		ObisCode obisCode = new ObisCode(oc.getA(), getGasSlotId(), oc.getC(), oc.getD(), oc.getE(), oc.getF());
		return obisCode;
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
		return new GMeterMessaging(this).queryMessage(messageEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<MessageCategorySpec> getMessageCategories() {
		return new GMeterMessaging(this).getMessageCategories();
	}

	/**
	 * {@inheritDoc}
	 *
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
	 * Userfiles are supported for upgrades
	 */
	public boolean supportsUserFilesForFirmwareUpdate() {
	    return false;
	}
}
