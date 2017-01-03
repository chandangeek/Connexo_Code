package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.CollectedMessageAcknowledgement;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocolEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.LoadProfileBuilder;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.SmsObisCodeMapper;
import com.energyict.protocolimplv2.elster.ctr.MTU155.events.CTRMeterEvent;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Function;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.profile.ProfileChannelForSms;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.AbstractTableQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.AckStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayEventsQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.NackStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECFQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierByDeviceAndProtocolInfoParts;
import com.energyict.protocolimplv2.identifiers.LoadProfileIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.identifiers.LogBookIdentifierByObisCodeAndDevice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 20-sep-2010
 * Time: 9:58:20
 */
public class SmsHandler {

    private static final ObisCode GENERIC_LOGBOOK_TYPE_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");

    private final String SMS_IDENTIFICATION_NUMBER = "SMS identification number";

    /**
     * The {@link DeviceIdentifier} uniquely identifying the device for which the SMSFrame should be parsed
     */
    private DeviceIdentifier deviceIdentifier;

    /**
     * The {@link TypedProperties} containing all device protocol properties
     */
    private TypedProperties allProperties;

    /**
     * The {@link MTU155Properties}
     */
    private MTU155Properties mtu155Properties;

    /**
     * The list in which all parsed {@link CollectedData} will be stored
     */
    private List<CollectedData> collectedDataList;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;

    public SmsHandler(DeviceIdentifier deviceIdentifier, TypedProperties typedProperties, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.collectedDataList = new ArrayList<>();
        this.deviceIdentifier = deviceIdentifier;
        this.allProperties = typedProperties;
        this.mtu155Properties = new MTU155Properties(typedProperties);
    }

    /**
     * Parse the (unencrypted) sms frame and extract all data (registers/loadprofile/logbook/message ack/nack) out of it.
     *
     * @param smsFrame the unencrypted {@link SMSFrame}, containing the data
     */
    public void parseSMSFrame(SMSFrame smsFrame) throws CTRException {
        smsFrame.doParse();

        if (smsFrame.getData() instanceof ArrayEventsQueryResponseStructure) {
            ArrayEventsQueryResponseStructure data = (ArrayEventsQueryResponseStructure) smsFrame.getData();
            verifyCallHomeID(data.getPdr());
            parseEventArrayData(data);
        } else if (smsFrame.getData() instanceof Trace_CQueryResponseStructure) {
            Trace_CQueryResponseStructure data = (Trace_CQueryResponseStructure) smsFrame.getData();
            verifyCallHomeID(data.getPdr());
            parseTrace_CData(data);
        } else if (smsFrame.getData() instanceof TableDECFQueryResponseStructure) {
            TableDECFQueryResponseStructure data = (TableDECFQueryResponseStructure) smsFrame.getData();
            verifyCallHomeID(data.getPdr());
            parseDECFTableData(data);
        } else if (smsFrame.getData() instanceof TableDECQueryResponseStructure) {
            TableDECQueryResponseStructure data = (TableDECQueryResponseStructure) smsFrame.getData();
            verifyCallHomeID(data.getPdr());
            parseDECTableData(data);
        } else if (smsFrame.getData() instanceof AckStructure) {
            AckStructure data = (AckStructure) smsFrame.getData();
            parseMessageData(data);
        } else if (smsFrame.getData() instanceof NackStructure) {
            NackStructure data = (NackStructure) smsFrame.getData();
            parseMessageData(data);
        } else {
            throw new CTRException("Unrecognized data structure in SMS. Expected array of event records, trace_C response, tableDEC(F), ACK or NACK response.");
        }
    }

    /**
     * parse the event data (received via sms)
     *
     * @param data the {@link ArrayEventsQueryResponseStructure} to parse
     */
    protected void parseEventArrayData(ArrayEventsQueryResponseStructure data) {
        CTRMeterEvent ctrMeterEvent = new CTRMeterEvent(getTimeZone());
        List<MeterProtocolEvent> meterProtocolEvents = MeterEvent.mapMeterEventsToMeterProtocolEvents(
                ctrMeterEvent.convertToMeterEvents(Arrays.asList(data.getEvento_Short())));
        CollectedLogBook collectedLogBook = this.collectedDataFactory.createCollectedLogBook(
                new LogBookIdentifierByObisCodeAndDevice(getDeviceIdentifier(), GENERIC_LOGBOOK_TYPE_OBISCODE));
        collectedLogBook.setCollectedMeterEvents(meterProtocolEvents);
        this.collectedDataList.add(collectedLogBook);
    }

    /**
     * parse the trace_c data (received via sms)
     *
     * @param data: the Trace_CQueryResponseStructure sent by the meter via sms
     * @throws CTRException
     */
    protected void parseTrace_CData(Trace_CQueryResponseStructure data) throws CTRException {
        ProfileChannelForSms profileForSms = new ProfileChannelForSms(getDeviceSerialNumber(), getMtu155Properties(), data, collectedDataFactory, issueFactory);
        ProfileData pd = profileForSms.getProfileData();
        this.collectedDataList.add(convertToCollectedData(data, pd));
    }

    private CollectedData convertToCollectedData(Trace_CQueryResponseStructure data, ProfileData profileData) throws CTRException {
        CollectedLoadProfile collectedProfile = this.collectedDataFactory.createCollectedLoadProfile(new LoadProfileIdentifierByObisCodeAndDevice(getProfileObisCode(data), getDeviceIdentifier()));
        collectedProfile.setCollectedIntervalData(profileData.getIntervalDatas(), profileData.getChannelInfos());
        collectedProfile.setDoStoreOlderValues(true);
        return collectedProfile;
    }

    private ObisCode getProfileObisCode(Trace_CQueryResponseStructure data) throws CTRException {
        if (data.getPeriod().isHourly() || data.getPeriod().isHourlyFistPart() || data.getPeriod().isHourlySecondPart()) {
            return LoadProfileBuilder.FLOW_MEASUREMENT_PROFILE;
        } else if (data.getPeriod().isDaily()) {
            if (LoadProfileBuilder.VOLUME_MEASUREMENT_PROFILE.toString().equals(data.getId().toString())) {
                return LoadProfileBuilder.VOLUME_MEASUREMENT_PROFILE;
            } else if (Arrays.asList(LoadProfileBuilder.TOTALIZERS_OBJECT_IDS).contains(data.getId().toString())) {
                return LoadProfileBuilder.TOTALIZERS_PROFILE;
            }
        }

        throw new CTRException("SMS contained profile data, but failed to map the data to a device load profile.");
    }

    /**
     * Parse the data in the decf table (received via sms)
     *
     * @param data: the {@link TableDECFQueryResponseStructure} to parse
     */
    protected void parseDECFTableData(TableDECFQueryResponseStructure data) {
        this.collectedDataList.addAll(doReadRegisters(data));
    }

    /**
     * Parse the data in the dec table (received via sms)
     *
     * @param data: the {@link TableDECQueryResponseStructure} to parse
     */
    protected void parseDECTableData(TableDECQueryResponseStructure data) {
        this.collectedDataList.addAll(doReadRegisters(data));
    }

    /**
     * Read the register data from a received DEC(F) table
     *
     * @param response: the table containing register data
     * @throws CTRException
     * @return: register values
     */
    protected List<CollectedRegister> doReadRegisters(AbstractTableQueryResponseStructure response) {
        SmsObisCodeMapper obisCodeMapper = getSmsObisCodeMapper();
        List<AbstractCTRObject> objects = response.getObjects();
        return obisCodeMapper.readRegisters(objects);
    }

    private SmsObisCodeMapper getSmsObisCodeMapper() {
        return new SmsObisCodeMapper(getDeviceIdentifier(), this.collectedDataFactory, this.issueFactory);
    }

    private <T extends Data> void verifyCallHomeID(AbstractCTRObject pdr) throws CTRException {
        String smsCallHomeId = (String) pdr.getValue(0).getValue();
        if (!smsCallHomeId.equals(getCallHomeID())) {
            String message = "Expected callHomeId " + getCallHomeID() + ", but the callHomeId in the sms was " + smsCallHomeId.toString();
            throw new CTRException(message);
        }
    }

    protected void parseMessageData(AckStructure data) throws CTRException {
        int wdb;
        if (data.getFunctionCode().getFunction().getFunctionCode() == Function.WRITE.getFunctionCode() ||
                data.getFunctionCode().getFunction().getFunctionCode() == Function.SECRET.getFunctionCode()) {
            wdb = data.getAdditionalData().getBytes()[0];
        } else if (data.getFunctionCode().getFunction().getFunctionCode() == Function.EXECUTE.getFunctionCode()) {
            wdb = data.getAdditionalData().getBytes()[2];
        } else {
            throw new CTRException("Unable to extract the Writing Data Block number from the AckStructure.");
        }

        MessageIdentifier messageIdentifier = new DeviceMessageIdentifierByDeviceAndProtocolInfoParts(getDeviceIdentifier(), SMS_IDENTIFICATION_NUMBER, Integer.toString(wdb));
        CollectedMessageAcknowledgement messageAcknowledgement = this.collectedDataFactory.createDeviceProtocolMessageAcknowledgementFromSms(messageIdentifier);
        messageAcknowledgement.setDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        messageAcknowledgement.setProtocolInfo("Message confirmed as successful by SMS.");

        this.collectedDataList.add(messageAcknowledgement);
    }

    protected void parseMessageData(NackStructure data) throws CTRException {
        int wdb = -1;
        switch (data.getReason().getReason()) {
            case 0x4A:
            case 0x4E:
                wdb = data.getAdditionalData().getBytes()[0];
                break;
            case 0x50:
            case 0x51:
            case 0x52:
                wdb = data.getAdditionalData().getBytes()[1];
                break;
            case 0x42:
                if (data.getFunctionCode().getFunctionCode() != Function.EXECUTE.getFunctionCode()) {
                    break;
                }
            case 0x43:
            case 0x47:
                if ((data.getFunctionCode().getFunctionCode() != Function.EXECUTE.getFunctionCode()) ||
                        ((data.getFunctionCode().getFunctionCode() != Function.WRITE.getFunctionCode()))) {
                    break;
                }
            case 0x4B:
            case 0x4F:
                wdb = data.getAdditionalData().getBytes()[2];
                break;
            default:
                break;
        }

        if (wdb == -1) {
            throw new CTRException("Unable to extract the Writing Data Block number from the NackStructure.");
        }

        MessageIdentifier messageIdentifier = new DeviceMessageIdentifierByDeviceAndProtocolInfoParts(getDeviceIdentifier(), SMS_IDENTIFICATION_NUMBER, Integer.toString(wdb));
        CollectedMessageAcknowledgement messageAcknowledgement = this.collectedDataFactory.createDeviceProtocolMessageAcknowledgement(messageIdentifier);
        messageAcknowledgement.setDeviceMessageStatus(DeviceMessageStatus.FAILED);
        messageAcknowledgement.setProtocolInfo("Message confirmed as failed by SMS - failure reason: " + data.getReason().getDescription());

        this.collectedDataList.add(messageAcknowledgement);
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public TypedProperties getAllProperties() {
        return allProperties;
    }

    public MTU155Properties getMtu155Properties() {
        return mtu155Properties;
    }

    /**
     * @return the meter's {@link java.util.TimeZone}
     */
    private TimeZone getTimeZone() {
        return getMtu155Properties().getTimeZone();
    }

    private String getDeviceSerialNumber() {
        return (String) getAllProperties().getProperty(com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER.getName(), "");
    }

    public String getCallHomeID() throws CTRException {
        String callHomeId = (String) getAllProperties().getProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "");
        if (callHomeId.isEmpty()) {
            throw new CTRException("Required property " + LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME + " is missing.");
        }
        return callHomeId;
    }

    public List<CollectedData> getCollectedDataList() {
        return collectedDataList;
    }
}
