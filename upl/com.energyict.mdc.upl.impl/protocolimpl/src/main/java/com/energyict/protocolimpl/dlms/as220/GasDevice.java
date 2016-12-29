/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeterMessaging;
import com.energyict.protocolimpl.dlms.as220.gmeter.GasRegister;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;

/**
 * Basic implementation of a GasDevice
 *
 * @author jeroen.meulemeester
 *
 */
public class GasDevice extends AS220 implements MessageProtocol, SerialNumberSupport {

	private static final int MAX_MBUS_CHANNELS = 4;

    private String emeterSerialnumber;
    private String gmeterSerialnumber;
    private int gasMeterSlot = -1;
    private int mbusProfileInterval = -1;

    private final GMeter gMeter = new GMeter(this);
    private GMeterMessaging messaging;
    private int dif = -1;

	public GasDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, Extractor extractor) {
		super(propertySpecService, calendarFinder, extractor);
	}

	@Override
	public int getNumberOfChannels() throws IOException {
		return getgMeter().getNrOfChannels();
	}

	@Override
    public GMeter getgMeter() {
		return gMeter;
	}

    public int getDif() {
        return dif;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:58 +0200 (Thu, 26 Nov 2015)$";
    }

	@Override
	protected void doConnect() {
		// search for the channel of the Mbus Device
		for(int i = 0; i < MAX_MBUS_CHANNELS; i++){
            String tempSerial;
			try {
				tempSerial = getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(i).getObisCode()).getString();
				if(tempSerial.equalsIgnoreCase(gmeterSerialnumber)){
					setGasSlotId(i + 1);
                    dif = getMBusDIF();
				}
			} catch (IOException e) {
				// fetch next
			}
		}

        if (getGasSlotId() == -1) {
            throw new IllegalArgumentException("No MBus device found with serialNumber " + gmeterSerialnumber + " on the E-meter.");
        }
    }

    public int getGasSlotId(){
    	return gasMeterSlot;
    }

    public void setGasSlotId(int slotId){
    	this.gasMeterSlot = slotId;
    }

    public int getPhysicalAddress(){
    	return getGasSlotId() - 1;
    }

	@Override
	public String getSerialNumber() {
        try {
            return getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(getPhysicalAddress()).getObisCode()).getString();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getProtocolRetries() + 1);
        }
    }

	@Override
	protected byte[] getSystemIdentifier(){
		return this.emeterSerialnumber.getBytes();
	}

	@Override
    public int getProfileInterval() throws IOException {
        if (mbusProfileInterval == -1) {
        	mbusProfileInterval = getgMeter().getMbusProfile().getCapturePeriod();
        }
        return mbusProfileInterval;
    }

	@Override
	public void setProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
		validateProperties(properties);
		super.setProperties(properties);
	}

	private void validateProperties(TypedProperties properties) {
		this.gmeterSerialnumber = properties.getTypedProperty(SERIALNUMBER.getName(), "");
		this.emeterSerialnumber = properties.getTypedProperty(NODEID.getName(), "");

	}

	@Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		if (to == null) {
			to = ProtocolUtils.getCalendar(getTimeZone()).getTime();
			getLogger().info("getProfileData: toDate was 'null'. Reading profiledate up to: " + to);
		}

        ProfileLimiter limiter = new ProfileLimiter(from, to, getLimitMaxNrOfDays());
        from = limiter.getFromDate();
        to = limiter.getToDate();

		if (validateFromToDates(from, to)) {
			return new ProfileData();
		}

		getLogger().info("Starting to read profileData [from=" + from + ", to=" + to + ", includeEvents=" + includeEvents + "]");
		return getgMeter().getProfileData(from, to, includeEvents);
	}

	/**
	 * The gasMeter has normally one register ( 0.x.24.2.0.255 )
	 */
	@Override
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		if (obisCode.equals(ObisCode.fromString("0.0.24.2.0.255"))) {
			ObisCode oc = getCorrectedChannelObisCode(obisCode);
			GasRegister gasRegister = new GasRegister(this);
			RegisterValue registerValue = gasRegister.getRegisterValue(oc);
			return ProtocolTools.setRegisterValueObisCode(registerValue, obisCode);
		} else if(obisCode.equals(ObisCode.fromString("0.0.24.4.129.255"))) {
            ContactorController.ContactorState cs = getgMeter().getGasValveController().getContactorState();
            return new RegisterValue(obisCode,null, null, null, null, new Date(), 0, cs.name());
        }
        else {
			throw new NoSuchRegisterException(obisCode.toString() + " is not supported.");
		}
	}

	/**
     * Getter for the DIF value of the MBus capture_definition
     *
     * @return DIF value, or -1 if unknown
     * @throws IOException
     */
    private int getMBusDIF() throws IOException {
        Array captureDefinitionArray = getgMeter().getGasInstallController().getMbusClient().getCaptureDefiniton();
        if (captureDefinitionArray != null) {
            AbstractDataType abstractCaptureDefinition = captureDefinitionArray.getDataType(0);
            Structure captureDefinition = abstractCaptureDefinition.getStructure();
            if (captureDefinition != null) {
                AbstractDataType abstractDib = captureDefinition.getDataType(0);
                OctetString dib = abstractDib.getOctetString();
                if (dib != null) {
                    return dib.getOctetStr()[0] & 0xFF; //Return DIF (first byte of the DIB)
                }
            }
        }
        return -1;
	}

	/**
	 * Construct the ObisCode with the correct channelField filled in
	 * @param oc - the ObisCode to change the B field
	 * @return the corrected ObisCode
	 */
	public ObisCode getCorrectedChannelObisCode(ObisCode oc){
		return ProtocolTools.setObisCodeField(oc, 1, (byte) getGasSlotId());
	}

	@Override
	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return getMessaging().queryMessage(messageEntry);
	}

	@Override
	public List<MessageCategorySpec> getMessageCategories() {
		return getMessaging().getMessageCategories();
	}

	@Override
	public String writeMessage(Message msg) {
		return getMessaging().writeMessage(msg);
	}

	@Override
	public String writeTag(MessageTag msgTag) {
		return getMessaging().writeTag(msgTag);
    }

	@Override
	public String writeValue(MessageValue msgValue) {
		return msgValue.getValue();
	}

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

}