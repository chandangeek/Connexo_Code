package com.energyict.protocolimplv2.nta.dsmr23;

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

    public static final DLMSAttribute SERIALNR = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");
    public static final DLMSAttribute EQUIPMENT_IDENTIFIER = DLMSAttribute.fromString("1:0.0.96.1.1.255:2");
    public static final DLMSAttribute CONFIG_NUMBER = DLMSAttribute.fromString("1:0.0.96.2.0.255:2");
    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:1.0.0.2.0.255:2");
    public static final DLMSAttribute CLOCK = DLMSAttribute.fromString("8:0.0.1.0.0.255:2");
    private final int roundTripCorrection;
    private final int retries;
    private Long timeDifference = null;

    public ComposedMeterInfo(final ProtocolLink dlmsSession, final boolean bulkRequest, final int roundTripCorrection, final int retries) {
        super(dlmsSession, bulkRequest, getDlmsAttributes());
        this.roundTripCorrection = roundTripCorrection;
        this.retries = retries;
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[]{
                SERIALNR,
                EQUIPMENT_IDENTIFIER,
                CONFIG_NUMBER,
                CLOCK,
                FIRMWARE_VERSION
        };
    }

    public String getFirmwareVersion() {
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw CommunicationException.unexpectedResponse(ioException);
        }
    }

    public Date getClock() {
        if (timeDifference == null) {
            AbstractDataType attribute = getAttribute(CLOCK);
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
        AbstractDataType attribute = getAttribute(SERIALNR);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw CommunicationException.unexpectedResponse(ioException);
        }
    }

    public int getConfigurationChanges() {
        return getAttribute(CONFIG_NUMBER).intValue();
    }

    public String getEquipmentIdentifier() {
        AbstractDataType attribute = getAttribute(EQUIPMENT_IDENTIFIER);
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
