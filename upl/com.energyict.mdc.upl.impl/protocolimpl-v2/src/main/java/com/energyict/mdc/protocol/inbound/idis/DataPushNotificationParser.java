package com.energyict.mdc.protocol.inbound.idis;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.EventPushNotificationConfig;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.CollectedRegisterList;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.InboundDiscoveryContext;
import com.energyict.mdc.protocol.inbound.g3.EventPushNotificationParser;
import com.energyict.mdw.core.TimeZoneInUse;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130Properties;
import com.energyict.protocolimplv2.identifiers.DialHomeIdDeviceIdentifier;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.DEFAULT_TIMEZONE;
import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;
import static com.energyict.protocolimplv2.MdcManager.getComServerExceptionFactory;

/**
 * Parser class for the IDIS DataPush<br/>
 * This parser class can parse/handle a pushed DLMS Data-notification message and extract the relevant information
 * - being the deviceIdentifier and some register data - out of it.
 *
 * @author sva
 * @since 13/04/2015 - 16:45
 */
public class DataPushNotificationParser extends EventPushNotificationParser {

    CollectedRegisterList collectedRegisters;

    public DataPushNotificationParser(ComChannel comChannel, InboundDiscoveryContext context) {
        super(comChannel, context);
    }

    @Override
    protected DeviceIdentifier getDeviceIdentifierBasedOnSystemTitle(byte[] systemTitle) {
        String serverSystemTitle = ProtocolTools.getHexStringFromBytes(systemTitle, "");
        serverSystemTitle = serverSystemTitle.replace("454C53", "ELS-");      // Replace HEX 454C53 by its ASCII 'ELS'
        return new DialHomeIdDeviceIdentifier(serverSystemTitle);
    }

    @Override
    protected DlmsProperties getNewInstanceOfProperties() {
        return new AM130Properties();
    }

    @Override
    protected byte getCosemNotificationAPDUTag() {
        return DLMSCOSEMGlobals.COSEM_DATA_NOTIFICATION;
    }

    @Override
    protected void parseAPDU(ByteBuffer inboundFrame) {
        // 1. long-invoke-id-and-priority
        byte[] invokeIdAndPriority = new byte[4];   // 32-bits long format used
        inboundFrame.get(invokeIdAndPriority);

        //2. date-time
        int dateTimeAxdrLength = DLMSUtils.getAXDRLength(inboundFrame.array(), inboundFrame.position());
        inboundFrame.get(new byte[DLMSUtils.getAXDRLengthOffset(dateTimeAxdrLength)]); // Increment ByteBuffer position

        byte[] octetString = new byte[dateTimeAxdrLength];
        inboundFrame.get(octetString);
        Date dateTime = parseDateTime(new OctetString(octetString));

        //3. notification-body
        Structure structure;
        try {
            structure = AXDRDecoder.decode(inboundFrame.array(), inboundFrame.position(), Structure.class);
        } catch (ProtocolException e) {
            throw getComServerExceptionFactory().createProtocolParseException(e);
        }

        AbstractDataType dataType = structure.getNextDataType();
        if (dataType instanceof OctetString) {
            ObisCode obisCode = ObisCode.fromByteArray(((OctetString) dataType).getOctetStr());
            if (!obisCode.equalsIgnoreBChannel(EventPushNotificationConfig.getDefaultObisCode())) {
                throw getComServerExceptionFactory().createProtocolParseException(new ProtocolException("The first element of the Data-notification body should contain the obiscode of the Push Setup IC, but was unexpected obis '" + obisCode.toString() + "'"));
            }
        } else {
            throw getComServerExceptionFactory().createProtocolParseException(new ProtocolException("The first element of the Data-notification body should contain the obiscode of the Push Setup IC, but was an element of type '" + dataType.getClass().getSimpleName() + "'"));
        }

        parseRegisters(structure);
    }

    private void parseRegisters(Structure structure) {
        while (structure.hasMoreElements()) {
            AbstractDataType logicalName = structure.getNextDataType();
            if (!(logicalName instanceof OctetString)){
                throw getComServerExceptionFactory().createProtocolParseException(new ProtocolException("Failed to parse the register data from the Data-notification body: Expected an element of type OctetString (~ the logical name of the object), but was an element of type '" + logicalName.getClass().getSimpleName() + "'"));
            }
            AbstractDataType valueData = structure.getNextDataType();
            AbstractDataType scalerUnit = null;
            AbstractDataType eventDate = null;
            if (structure.hasMoreElements() && structure.peekAtNextDataType().isStructure()) {
                scalerUnit = structure.getNextDataType();

                if (structure.hasMoreElements()
                        && structure.peekAtNextDataType().isOctetString()
                        && structure.peekAtNextDataType().getOctetString().getOctetStr().length == AXDRDateTime.SIZE) {
                    eventDate = structure.getNextDataType();
                }
            }

            parseRegisterData(logicalName, valueData, scalerUnit, eventDate);
        }
    }

    private void parseRegisterData(AbstractDataType logicalNameData, AbstractDataType valueData, AbstractDataType scalerUnitData, AbstractDataType eventTimeData) {
        try {
            long value = 0;
            String text = null;
            ScalerUnit scalerUnit = null;
            Date eventTime = null;
            ObisCode obisCode = ObisCode.fromByteArray(((OctetString) logicalNameData).getOctetStr());

            if (valueData.isOctetString()) {
                text = valueData.getOctetString().stringValue();
            } else {
                value = DLMSUtils.parseValue2long(valueData.getBEREncodedByteArray());
            }

            if (scalerUnitData != null) {
                scalerUnit = new ScalerUnit(((Structure) scalerUnitData));
            }

            if (eventTimeData != null) {
                eventTime = parseDateTime((OctetString) eventTimeData);
            }

            addCollectedRegister(obisCode, value, scalerUnit, eventTime, text);
        } catch (IndexOutOfBoundsException | ProtocolException e) {
           throw getComServerExceptionFactory().createProtocolParseException(new ProtocolException(e, "Failed to parse the register data from the Data-notification body: " + e.getMessage()));
        }
    }

    private void addCollectedRegister(ObisCode obisCode, long value, ScalerUnit scalerUnit, Date eventTime, String text) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(
                new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier())
        );

        if (text == null) {
            deviceRegister.setCollectedData(new Quantity(value, scalerUnit != null ? scalerUnit.getEisUnit() : Unit.getUndefined()));
        } else {
            deviceRegister.setCollectedData(text);
        }

        deviceRegister.setCollectedTimeStamps(new Date(), null, new Date(), eventTime);
        getCollectedRegisters().addCollectedRegister(deviceRegister);
    }

    private Date parseDateTime(OctetString octetString) {
        try {
            return new AXDRDateTime(octetString.getBEREncodedByteArray(), 0, getDeviceTimeZone()).getValue().getTime(); // Make sure to pass device TimeZone, as deviation info is unspecified
        } catch (ProtocolException e) {
            throw getComServerExceptionFactory().createProtocolParseException(e);
        }
    }

    private TimeZone getDeviceTimeZone() {
        TypedProperties deviceProtocolProperties = getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());
        TimeZoneInUse timeZoneInUse = deviceProtocolProperties.getTypedProperty(TIMEZONE);
        if (timeZoneInUse == null || timeZoneInUse.getTimeZone() == null) {
            return TimeZone.getTimeZone(DEFAULT_TIMEZONE);
        } else {
            return timeZoneInUse.getTimeZone();
        }
    }

    public CollectedRegisterList getCollectedRegisters() {
        if (this.collectedRegisters == null)  {
            this.collectedRegisters = MdcManager.getCollectedDataFactory().createCollectedRegisterList(getDeviceIdentifier());
        }
        return this.collectedRegisters;
    }
}