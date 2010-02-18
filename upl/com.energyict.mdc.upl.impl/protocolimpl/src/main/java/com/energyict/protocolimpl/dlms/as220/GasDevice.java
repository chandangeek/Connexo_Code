/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeterMessaging;

/**
 * @author jeroen.meulemeester
 *
 */
public class GasDevice extends AS220{
	
	private final static int EMETERSERIAL = 0;
	private final static int SLOTID = 1;
	
	private String 	emeterSerialnumber;
	private int 	gasMeterSlot = -1;
	private int		mbusProfileInterval = -1;

	private final GMeter	gMeter	= new GMeter(this);


	/**
	 * {@inheritDoc}
	 */
    public GMeter getgMeter() {
		return gMeter;
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
		// TOTEST Test the serialNumber
//		return new String(getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(getPhysicalAddress()).getObisCode()).getData());
		return "35016036";
	}
	
//	protected String getCorrectIdentificationNumber(byte[] rawBytes){
//		
//		StringBuilder strBuilder = new StringBuilder();
//		
//		for(int i = 0; i < rawBytes.length; i++){
//			strBuilder.append(ProtocolUtils.hex2String(rawBytes[rawBytes.length-i]));
//		}
//		
//		return strBuilder.toString();
//	}
	
	/**
	 * {@inheritDoc}
	 */
    public int getProfileInterval() throws UnsupportedException, IOException {
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

		String nodeId = properties.getProperty(MeterProtocol.NODEID);
		if (nodeId == null) {
			throw new MissingPropertyException("Excpected a value for " + MeterProtocol.NODEID + " but was null");
		}

		String[] nd = nodeId.split(":");
		this.emeterSerialnumber = nd[EMETERSERIAL];
		if(nd.length == 1){
			throw new InvalidPropertyException("NodeId should contain an MBus SlotID");
		}
		this.gasMeterSlot = Integer.parseInt(nd[SLOTID]);
		for (int i = 0; i < nd.length; i++) {
			System.out.println("[" + i + "] " + nd[i]);
		}

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
		oc = new ObisCode(oc.getA(), getGasSlotId(), oc.getC(), oc.getD(), oc.getE(), oc.getF());
		return oc;
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
