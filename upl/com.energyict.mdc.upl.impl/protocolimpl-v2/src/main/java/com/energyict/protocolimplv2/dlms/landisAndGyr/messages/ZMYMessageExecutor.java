package com.energyict.protocolimplv2.dlms.landisAndGyr.messages;

import com.energyict.cbo.Quantity;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.messages.ActivityCalendarDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PowerConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.AbstractMessageConverter;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.messages.convertor.utils.LoadProfileMessageUtils;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.CurrentRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.VoltageRatioNumeratorAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarCodeTableAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.enableDSTAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.fromDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.loadProfileAttributeName;

public class ZMYMessageExecutor extends AbstractMessageExecutor {

	public static final String SEPARATOR = ";";
	private static final ObisCode BILLING_SCRIPT_TABLE_OBIS_CODE = ObisCode.fromString("0.0.98.1.0.255");
	private static final ObisCode VTNumerator = ObisCode.fromString("1.0.0.4.3.255");
	private static final ObisCode CTNumerator = ObisCode.fromString("1.0.0.4.2.255");

	public static String ENABLE_DST = "EnableDST";

	public ZMYMessageExecutor(AbstractDlmsProtocol protocol) {
		super(protocol, protocol.getCollectedDataFactory(), protocol.getIssueFactory());
	}

	@Override
	public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
		CollectedMessageList result = getCollectedDataFactory().createCollectedMessageList(pendingMessages);

		List<OfflineDeviceMessage> masterMessages = getMessagesOfMaster(pendingMessages);

		for (OfflineDeviceMessage pendingMessage : masterMessages) {
			CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
			collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
			try {
				if (pendingMessage.getSpecification().equals(DeviceActionMessage.BILLING_RESET)) {
					doBillingReset();
				} else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.EnableOrDisableDST)) {
					doEnableDST(pendingMessage);
				} else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SetStartOfDST)) {
					doSetDSTTime(pendingMessage, true);
				} else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SetEndOfDST)) {
					doSetDSTTime(pendingMessage, false);
				} else if (pendingMessage.getSpecification().equals(PowerConfigurationDeviceMessage.SetVoltageRatioNumerator)) {
					SetVoltageRatioEnumerator(pendingMessage);
				} else if (pendingMessage.getSpecification().equals(PowerConfigurationDeviceMessage.SetCurrentRatioNumerator)) {
					SetCurrentRatioEnumerator(pendingMessage);
				} else if (pendingMessage.getSpecification().equals(ActivityCalendarDeviceMessage.ACTIVITY_CALENDER_FULL_CALENDAR_WITH_DATETIME)) {
					activityCalendarWithActivationDate(pendingMessage);
				} else if (pendingMessage.getSpecification().equals(DeviceActionMessage.ReadDLMSAttribute)) {
					collectedMessage = this.readDlmsAttribute(collectedMessage, pendingMessage);
				} else {   //Unsupported message
					collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
					collectedMessage.setFailureInformation(ResultType.NotSupported, createUnsupportedWarning(pendingMessage));
					collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
				}
			} catch (IOException e) {
				if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSessionProperties().getRetries() + 1)) {
					collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
					collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
					collectedMessage.setDeviceProtocolInformation(e.getMessage());
				}
			}
			result.addCollectedMessage(collectedMessage);
		}
		return result;
	}

	private CollectedMessage readDlmsAttribute(CollectedMessage collectedMessage, OfflineDeviceMessage pendingMessage) {
		String obisCodeString = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.obisCode).getValue();
		int attributeId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.attributeId).getValue());
		int classId = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.classId).getValue());

		obisCodeString = obisCodeString.replace(":", ".").replace("-", ".").replace(" ", "");
		ObisCode obisCode = ObisCode.fromString(obisCodeString);

		DLMSAttribute dlmsAttribute = new DLMSAttribute(obisCode, attributeId, classId);

		try {
			ComposedCosemObject composeObject = getCosemObjectFactory().getComposedCosemObject(dlmsAttribute);
			AbstractDataType abstractDataType = composeObject.getAttribute(dlmsAttribute);
			collectedMessage.setDeviceProtocolInformation(abstractDataType.toString());
		} catch (IOException e) {
			collectedMessage.setDeviceProtocolInformation(e.toString());
		}

		return collectedMessage;
	}

	/**
	 * Substracts 5 seconds from the startReadingTime and adds 5 seconds to the endReadingTime
	 *
	 * @param loadProfileReader the reader
	 * @return the reader with the adjested times
	 */
	protected LoadProfileReader constructDateTimeCorrectdLoadProfileReader(final LoadProfileReader loadProfileReader) {
		Date from = new Date(loadProfileReader.getStartReadingTime().getTime() - 5000);
		Date to = new Date(loadProfileReader.getEndReadingTime().getTime() + 5000);
		return new LoadProfileReader(loadProfileReader.getProfileObisCode(), from, to, loadProfileReader.getLoadProfileId(), loadProfileReader.getMeterSerialNumber(), loadProfileReader.getChannelInfos());
	}

	protected CollectedMessage loadProfileRegisterRequest(OfflineDeviceMessage pendingMessage) throws IOException {
		String loadProfileContent = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, loadProfileAttributeName).getValue();
		String fromDateEpoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, fromDateAttributeName).getValue();

		String fullLoadProfileContent = LoadProfileMessageUtils.createLoadProfileRegisterMessage(
				"LoadProfileRegister",
				getDefaultDateFormatter().format(new Date(Long.parseLong(fromDateEpoch))),
				loadProfileContent
		);
		Date fromDate = new Date(Long.valueOf(fromDateEpoch));
		try {
			LegacyLoadProfileRegisterMessageBuilder builder = new LegacyLoadProfileRegisterMessageBuilder();
			builder = (LegacyLoadProfileRegisterMessageBuilder) builder.fromXml(fullLoadProfileContent);
			if (builder.getRegisters() == null || builder.getRegisters().isEmpty()) {
				CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
				collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
				collectedMessage.setFailureInformation(ResultType.ConfigurationMisMatch, createMessageFailedIssue(pendingMessage, "Unable to execute the message, there are no channels attached under LoadProfile " + builder.getProfileObisCode() + "!"));
			}

			LoadProfileReader lpr = checkLoadProfileReader(constructDateTimeCorrectdLoadProfileReader(builder.getLoadProfileReader()), builder.getMeterSerialNumber());
			LoadProfileReader fullLpr = new LoadProfileReader(lpr.getProfileObisCode(), fromDate, new Date(), lpr.getLoadProfileId(), lpr.getMeterSerialNumber(), lpr.getChannelInfos());

			List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = getProtocol().fetchLoadProfileConfiguration(Arrays.asList(fullLpr));
			for (CollectedLoadProfileConfiguration config : collectedLoadProfileConfigurations) {
				if (!config.isSupportedByMeter()) {   //LP not supported
					CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
					collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
					collectedMessage.setFailureInformation(ResultType.NotSupported, createMessageFailedIssue(pendingMessage, "Load profile with obiscode " + config.getObisCode() + " is not supported by the device"));
					return collectedMessage;
				}
			}

			List<CollectedLoadProfile> loadProfileData = getProtocol().getLoadProfileData(Arrays.asList(fullLpr));

			CollectedLoadProfile collectedLoadProfile = loadProfileData.get(0);
			IntervalData intervalDatas = null;
			for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
				if (intervalData.getEndTime().equals(builder.getStartReadingTime())) {
					intervalDatas = intervalData;
				}
			}

			if (intervalDatas == null) {
				CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
				collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
				collectedMessage.setFailureInformation(ResultType.DataIncomplete, createMessageFailedIssue(pendingMessage, "Didn't receive data for requested interval (" + builder.getStartReadingTime() + ")"));
				return collectedMessage;
			}

			com.energyict.protocol.Register previousRegister = null;
			List<CollectedRegister> collectedRegisters = new ArrayList<>();
			for (com.energyict.protocol.Register register : builder.getRegisters()) {
				if (register.equals(previousRegister)) {
					continue;    //Don't add the same intervals twice if there's 2 channels with the same obiscode
				}
				for (int i = 0; i < collectedLoadProfile.getChannelInfo().size(); i++) {
					final ChannelInfo channel = collectedLoadProfile.getChannelInfo().get(i);
					if (register.getObisCode().equalsIgnoreBChannel(ObisCode.fromString(channel.getName())) && register.getSerialNumber().equals(channel.getMeterIdentifier())) {
						final RegisterValue registerValue = new RegisterValue(register, new Quantity(intervalDatas.get(i), channel.getUnit()), intervalDatas.getEndTime(), null, intervalDatas.getEndTime(), new Date(), register
								.getRtuRegisterId());
						collectedRegisters.add(createCollectedRegister(registerValue, pendingMessage));
					}
				}
				previousRegister = register;
			}
			CollectedMessage collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, collectedRegisters);
			collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
			return collectedMessage;
		} catch (SAXException e) {
			CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
			collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
			collectedMessage.setFailureInformation(ResultType.Other, createMessageFailedIssue(pendingMessage, e));
			return collectedMessage;
		}
	}

	private DateFormat getDefaultDateFormatter() {
		return AbstractMessageConverter.dateTimeFormatWithTimeZone;
	}

	private LoadProfileReader checkLoadProfileReader(final LoadProfileReader lpr, String serialNumber) {
		if (lpr.getProfileObisCode().equalsIgnoreBChannel(ObisCode.fromString("0.x.24.3.0.255"))) {
			return new LoadProfileReader(lpr.getProfileObisCode(), lpr.getStartReadingTime(), lpr.getEndReadingTime(), lpr.getLoadProfileId(), serialNumber, lpr.getChannelInfos());
		} else {
			return lpr;
		}
	}

	private void doBillingReset() throws IOException {
		ScriptTable demandResetScriptTable = getCosemObjectFactory().getScriptTable(BILLING_SCRIPT_TABLE_OBIS_CODE);
		demandResetScriptTable.execute(1);
	}


	private void doEnableDST(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
		int mode = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, enableDSTAttributeName).getValue());

		Clock clock = getProtocol().getDlmsSession().getCosemObjectFactory().getClock();
		clock.enableDisableDs(mode == 1 ? true : false);
	}

	protected void activityCalendarWithActivationDate(OfflineDeviceMessage pendingMessage) throws IOException {
		String calendarName = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarNameAttributeName).getValue();
		String epoch = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarActivationDateAttributeName).getValue();
		String activityCalendarContents = MessageConverterTools.getDeviceMessageAttribute(pendingMessage, activityCalendarCodeTableAttributeName).getValue();
		if (calendarName.length() > 8) {
			calendarName = calendarName.substring(0, 8);
		}

		ActivityCalendarController activityCalendarController = new DLMSActivityCalendarController(getCosemObjectFactory(), getProtocol().getDlmsSession().getTimeZone(), false);
		activityCalendarController.parseContent(activityCalendarContents);
		activityCalendarController.writeCalendarName(calendarName);
		activityCalendarController.writeCalendar(); //Does not activate it yet
		Calendar activationCal = Calendar.getInstance(getProtocol().getTimeZone());
		activationCal.setTimeInMillis(Long.parseLong(epoch));
		activityCalendarController.writeCalendarActivationTime(activationCal);   //Activate now
	}

	protected List<OfflineDeviceMessage> getMessagesOfMaster(List<OfflineDeviceMessage> deviceMessages) {
		List<OfflineDeviceMessage> messages = new ArrayList<>();

		for (OfflineDeviceMessage pendingMessage : deviceMessages) {
			if (getProtocol().getSerialNumber().equals(pendingMessage.getDeviceSerialNumber())) {
				messages.add(pendingMessage);
			}
		}
		return messages;
	}

	private void doSetDSTTime(OfflineDeviceMessage offlineDeviceMessage, boolean startOfDST) throws IOException {
		int month = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.month).getValue());
		if (month < 1 || month > 12) {
			throw new IOException("Failed to parse the message content. " + month + " is not a valid month. Message will fail.");
		}

		int dayOfMonth = 0xFF;
		try {
			dayOfMonth = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.dayOfMonth).getValue());
			if (dayOfMonth == -1) {
				dayOfMonth = 0xFD;
			} else if (dayOfMonth == -2) {
				dayOfMonth = 0xFE;
			} else if (dayOfMonth < -2 || dayOfMonth > 31) {
				throw new IOException("Failed to parse the message content. " + dayOfMonth + " is not a valid Day of month. Message will fail.");
			}
		} catch (ProtocolException e) {
		}

		int dayOfWeek = 0xFF;
		try {
			dayOfWeek = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.dayOfWeek).getValue());
			if (dayOfWeek < 1 || dayOfWeek > 7) {
				throw new IOException("Failed to parse the message content. " + dayOfWeek + " is not a valid Day of week. Message will fail.");
			}
		} catch (ProtocolException e) {	}


		int hour = Integer.parseInt(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, DeviceMessageConstants.hour).getValue());
		if (hour < 0||hour > 23) {
			throw new IOException("Failed to parse the message content. " + hour + " is not a valid hour. Message will fail.");
		}

		byte[] dsDateTimeByteArray = new byte[] {
				(byte) 0xFF,
				(byte) 0xFF,
				(byte) month,
				(byte) dayOfMonth,
				(byte) dayOfWeek,
				(byte) hour,
				(byte) 0x00,
				(byte) 0xFF,
				(byte) 0xFF,
				(byte) 0x80,
				0,
				(byte) 0xFF
		};

		Clock clock = getProtocol().getDlmsSession().getCosemObjectFactory().getClock();
		if (startOfDST) {
			clock.setDsDateTimeBegin(new OctetString(dsDateTimeByteArray).getBEREncodedByteArray());
		} else {
			clock.setDsDateTimeEnd(new OctetString(dsDateTimeByteArray).getBEREncodedByteArray());
		}
	}

	private void SetCurrentRatioEnumerator(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
		int cr_numerator = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, CurrentRatioNumeratorAttributeName).getValue()).intValue();
		getCosemObjectFactory().writeObject(CTNumerator,  DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(cr_numerator).getBEREncodedByteArray());
	}

	private void SetVoltageRatioEnumerator(OfflineDeviceMessage offlineDeviceMessage) throws IOException {
		int vt_numerator = new BigDecimal(MessageConverterTools.getDeviceMessageAttribute(offlineDeviceMessage, VoltageRatioNumeratorAttributeName).getValue()).intValue();
		getCosemObjectFactory().writeObject(VTNumerator,  DLMSClassId.REGISTER.getClassId(), RegisterAttributes.VALUE.getAttributeNumber(), new Unsigned16(vt_numerator).getBEREncodedByteArray());
	}
}
