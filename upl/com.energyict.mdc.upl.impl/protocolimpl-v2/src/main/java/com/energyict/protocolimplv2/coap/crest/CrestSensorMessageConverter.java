package com.energyict.protocolimplv2.coap.crest;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.KeyAccessorType;

import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.AbstractMessageConverter;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SimpleEIWebMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.XMLAttributeDeviceMessageEntry;
import com.google.common.collect.ImmutableMap;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Represents a MessageConverter that maps the Crest Sensor payload to legacy XML
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */

public class CrestSensorMessageConverter extends AbstractMessageConverter {

    private final KeyAccessorTypeExtractor keyAccessorTypeExtractor;

    public CrestSensorMessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(propertySpecService, nlsService, converter);
        this.keyAccessorTypeExtractor = keyAccessorTypeExtractor;
    }


    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        switch (propertySpec.getName()) {
            case DeviceMessageConstants.SetDLMSPasswordAttributeName:
            case DeviceMessageConstants.SetISP1PasswordAttributeName:
            case DeviceMessageConstants.SetISP2PasswordAttributeName:
            case DeviceMessageConstants.SetPOPPasswordAttributeName:
            case DeviceMessageConstants.SetOpusPasswordAttributeName:
            case DeviceMessageConstants.SetEIWebPasswordAttributeName:
            case DeviceMessageConstants.SetDukePowerPasswordAttributeName:
                return this.keyAccessorTypeExtractor.passiveValueContent((KeyAccessorType) messageAttribute);
            case DeviceMessageConstants.AdminPassword:
                return marshallActualPassiveValueOfKeyAccessorType((KeyAccessorType) messageAttribute);
            default:
                return messageAttribute.toString();
        }
    }

    private String marshallActualPassiveValueOfKeyAccessorType(KeyAccessorType messageAttribute) {
        String[] keys = new String[]{this.keyAccessorTypeExtractor.actualValueContent(messageAttribute), this.keyAccessorTypeExtractor.passiveValueContent(messageAttribute)};
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            new ObjectOutputStream(out).writeObject(keys);
            return DatatypeConverter.printHexBinary(out.toByteArray());
        } catch (IOException e) {
            throw DataParseException.generalParseException(e);
        }
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // General Parameters
                .put(messageSpec(GeneralDeviceMessage.SEND_XML_MESSAGE), new XMLAttributeDeviceMessageEntry())
                .put(messageSpec(FirmwareDeviceMessage.UPGRADE_FIRMWARE_WITH_USER_FILE), new SimpleEIWebMessageEntry())
                .build();
    }
}
