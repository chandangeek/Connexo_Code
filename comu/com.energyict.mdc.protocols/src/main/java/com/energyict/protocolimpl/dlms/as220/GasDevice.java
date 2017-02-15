/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.dlms.as220;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageCategorySpec;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimpl.base.ContactorController;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeter;
import com.energyict.protocolimpl.dlms.as220.gmeter.GMeterMessaging;
import com.energyict.protocolimpl.dlms.as220.gmeter.GasRegister;
import com.energyict.protocolimpl.dlms.as220.gmeter.GasValveController;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Basic implementation of a GasDevice
 *
 * @author jeroen.meulemeester
 *
 */
public class GasDevice extends AS220 implements MessageProtocol {

	@Override
	public String getProtocolDescription() {
		return "Elster AS220/AS1440 AM500 DLMS Mbus Slave";
	}

	private static final int MAX_MBUS_CHANNELS = 4;

    private String emeterSerialnumber;
    private String gmeterSerialnumber;
    private int gasMeterSlot = -1;
    private int mbusProfileInterval = -1;

    private final GMeter gMeter = new GMeter(this);
    private GMeterMessaging messaging;
    private int dif = -1;

	@Inject
	public GasDevice(PropertySpecService propertySpecService, CalendarService calendarService, DeviceMessageFileService deviceMessageFileService, OrmClient ormClient) {
		super(propertySpecService, calendarService, deviceMessageFileService, ormClient);
	}

	@Override
	public int getNumberOfChannels() throws IOException {
		return getgMeter().getNrOfChannels();
	}

    public GMeter getgMeter() {
		return gMeter;
	}

    public int getDif() {
        return dif;
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

	@Override
	protected void doConnect() {
		// search for the channel of the Mbus Device
		String tempSerial;
		for (int i = 0; i < MAX_MBUS_CHANNELS; i++) {
			try {
				tempSerial = getCosemObjectFactory().getData(getMeterConfig().getMbusSerialNumber(i).getObisCode()).getString();
				if (tempSerial.equalsIgnoreCase(gmeterSerialnumber)) {
					setGasSlotId(i + 1);
                    dif = getMBusDIF();
				}
			} catch (IOException e) {
				// fetch next
			}
		}

		if (getGasSlotId() == -1) {
			throw new NotFoundException("No MBus device found with serialNumber " + gmeterSerialnumber + " on the E-meter.");
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

	protected byte[] getSystemIdentifier(){
		return this.emeterSerialnumber.getBytes();
	}

    public int getProfileInterval() throws IOException {
        if (mbusProfileInterval == -1) {
        	mbusProfileInterval = getgMeter().getMbusProfile().getCapturePeriod();
        }
        return mbusProfileInterval;
    }

	@Override
	public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		validateProperties(properties);
		super.setProperties(properties);
	}

	private void validateProperties(Properties properties) {
		this.gmeterSerialnumber = properties.getProperty(MeterProtocol.SERIALNUMBER, "");
		this.emeterSerialnumber = properties.getProperty(MeterProtocol.NODEID, "");
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
			BreakerStatus cs = getgMeter().getGasValveController().getContactorState();
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
	public List<String> getRequiredKeys() {
		List<String> requiredKeys = new ArrayList<>();
		requiredKeys.addAll(super.getRequiredKeys());
		return requiredKeys;
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

	@Override
	public Optional<BreakerStatus> getBreakerStatus() throws IOException {
		ContactorController cc = new GasValveController(this);
		return Optional.of(cc.getContactorState());
	}
}
