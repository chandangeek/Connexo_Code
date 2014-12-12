package com.energyict.protocolimplv2.nta.dsmr23;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ExceptionResponseException;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:39:32
 */
public class ComposedMeterInfo extends ComposedCosemObject {

    public static final DLMSAttribute SERIALNR = DLMSAttribute.fromString("1:0.0.96.1.0.255:2");
    public static final DLMSAttribute EQUIPMENT_IDENTIFIER = DLMSAttribute.fromString("1:0.0.96.1.1.255:2");
    public static final DLMSAttribute FIRMWARE_VERSION = DLMSAttribute.fromString("1:1.0.0.2.0.255:2");
    public static final DLMSAttribute CONFIG_NUMBER = DLMSAttribute.fromString("1:0.0.96.2.0.255:2");

    public ComposedMeterInfo(final ProtocolLink dlmsSession, final boolean bulkRequest) {
        super(dlmsSession, bulkRequest, getDlmsAttributes());
    }

    private static DLMSAttribute[] getDlmsAttributes() {
        return new DLMSAttribute[]{
                SERIALNR,
                EQUIPMENT_IDENTIFIER,
                FIRMWARE_VERSION,
                CONFIG_NUMBER
        };
    }

    public String getFirmwareVersion() {
        AbstractDataType attribute = getAttribute(FIRMWARE_VERSION);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, ioException);
        }
    }

    public String getSerialNr() {
        AbstractDataType attribute = getAttribute(SERIALNR);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, ioException);
        }
    }

    public int getConfigurationChanges() {
        return getAttribute(CONFIG_NUMBER).intValue();
    }

    public AbstractDataType getAttribute(DLMSAttribute dlmsAttribute) {
        try {
            return super.getAttribute(dlmsAttribute);
        } catch (DataAccessResultException | ProtocolException | ExceptionResponseException e) {
            throw new CommunicationException(MessageSeeds.GENERAL_PARSE_ERROR, e);
            //Received error code from the meter, instead of the expected value
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.NUMBER_OF_RETRIES_REACHED, getDLMSConnection().getMaxTries());
        }
    }

    public String getEquipmentIdentifier() {
        AbstractDataType attribute = getAttribute(EQUIPMENT_IDENTIFIER);
        if (attribute instanceof OctetString) {
            return attribute.getOctetString().stringValue();
        } else {
            IOException ioException = new IOException("Expected OctetString but was " + attribute.getClass().getSimpleName());
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, ioException);
        }
    }
}
