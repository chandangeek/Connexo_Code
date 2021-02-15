package com.energyict.protocolimplv2.common.composedobjects;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.exceptionhandler.ExceptionResponseException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ConnectionCommunicationException;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:39:32
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    protected static final DLMSAttribute DEFAULT_SERIALNR = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");
    protected static final DLMSAttribute DEFAULT_EQUIPMENT_IDENTIFIER = DLMSAttribute.fromString("1:0.0.96.1.1.255:2");
    protected static final DLMSAttribute DEFAULT_CONFIG_NUMBER = DLMSAttribute.fromString("1:0.0.96.2.0.255:2");
    protected static final DLMSAttribute DEFAULT_FIRMWARE_VERSION = DLMSAttribute.fromString("1:1.0.0.2.0.255:2");
    protected static final DLMSAttribute DEFAULT_CLOCK = DLMSAttribute.fromString("8:0.0.1.0.0.255:2");

    private final DLMSAttribute serialnr;
    private final DLMSAttribute equipMentIdentitifer;
    private final DLMSAttribute configNumber;
    private final DLMSAttribute firmwareVersion;
    private final DLMSAttribute clock;

    private final int roundTripCorrection;
    private final int retries;
    private Long timeDifference = null;

    public ComposedMeterInfo(final ProtocolLink dlmsSession, final boolean bulkRequest, final int roundTripCorrection, final int retries, DLMSAttribute serialnr, DLMSAttribute equipMentIdentitifer, DLMSAttribute configNumber, DLMSAttribute firmwareVersion, DLMSAttribute clock) {
        super(dlmsSession, bulkRequest, serialnr, equipMentIdentitifer, configNumber, firmwareVersion, clock);
        this.serialnr = serialnr;
        this.equipMentIdentitifer = equipMentIdentitifer;
        this.configNumber = configNumber;
        this.firmwareVersion = firmwareVersion;
        this.clock = clock;
        this.roundTripCorrection = roundTripCorrection;
        this.retries = retries;
    }

    public ComposedMeterInfo(final ProtocolLink dlmsSession, final boolean bulkRequest, final int roundTripCorrection, final int retries, DLMSAttribute serialnr, DLMSAttribute clock) {
        super(dlmsSession, bulkRequest, serialnr,
                DEFAULT_EQUIPMENT_IDENTIFIER,
                DEFAULT_CONFIG_NUMBER,
                clock,
                DEFAULT_FIRMWARE_VERSION);
        this.serialnr = serialnr;
        this.equipMentIdentitifer = DEFAULT_EQUIPMENT_IDENTIFIER;
        this.configNumber = DEFAULT_CONFIG_NUMBER;
        this.firmwareVersion = DEFAULT_FIRMWARE_VERSION;
        this.clock = clock;
        this.roundTripCorrection = roundTripCorrection;
        this.retries = retries;
    }

    public ComposedMeterInfo(final ProtocolLink dlmsSession, final boolean bulkRequest, final int roundTripCorrection, final int retries) {
        super(dlmsSession, bulkRequest,
                DEFAULT_SERIALNR,
                DEFAULT_EQUIPMENT_IDENTIFIER,
                DEFAULT_CONFIG_NUMBER,
                DEFAULT_CLOCK,
                DEFAULT_FIRMWARE_VERSION
        );
        this.serialnr = DEFAULT_SERIALNR;
        this.equipMentIdentitifer = DEFAULT_EQUIPMENT_IDENTIFIER;
        this.configNumber = DEFAULT_CONFIG_NUMBER;
        this.firmwareVersion = DEFAULT_FIRMWARE_VERSION;
        this.clock = DEFAULT_CLOCK;
        this.roundTripCorrection = roundTripCorrection;
        this.retries = retries;
    }

    public String getFirmwareVersion() {
        AbstractDataType attribute = getAttribute(firmwareVersion);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw CommunicationException.unexpectedResponse(ioException);
        }
    }

    public Date getClock() {
        if (timeDifference == null) {
            AbstractDataType attribute = getAttribute(clock);
            try {
                Date meterTime = new Clock(getProtocolLink()).getDateTime(attribute.getBEREncodedByteArray(), roundTripCorrection);
                timeDifference = System.currentTimeMillis() - meterTime.getTime();
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, retries + 1);
            }
        }
        return new Date(System.currentTimeMillis() - timeDifference);
    }

    public String getSerialNr() {
        AbstractDataType attribute = getAttribute(serialnr);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw CommunicationException.unexpectedResponse(ioException);
        }
    }

    public int getConfigurationChanges() {
        return getAttribute(configNumber).intValue();
    }

    public String getEquipmentIdentifier() {
        AbstractDataType attribute = getAttribute(equipMentIdentitifer);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw CommunicationException.unexpectedResponse(ioException);
        }
    }

    public AbstractDataType getAttribute(DLMSAttribute dlmsAttribute) {
        try {
            return super.getAttribute(dlmsAttribute);
        } catch (DataAccessResultException | ProtocolException | ExceptionResponseException e) {
            throw CommunicationException.unexpectedResponse(e);   //Received error code from the meter, instead of the expected value
        } catch (IOException e) {
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getDLMSConnection().getMaxTries());
        }
    }
}
