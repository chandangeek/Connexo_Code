package com.energyict.protocolimpl.dlms.g3.registers.mapping;


import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.ProtocolException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.BeaconEventPushNotificationConfig;
import com.energyict.dlms.cosem.CosemObjectFactory;

import java.io.IOException;

/**
 * Created by cisac on 1/5/2016.
 */
public class BeaconPushEventNotificationAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 4;

    public BeaconPushEventNotificationAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return BeaconEventPushNotificationConfig.getDefaultObisCode().equalsIgnoreBChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final BeaconEventPushNotificationConfig eventPushConfig = getCosemObjectFactory().getBeaconEventPushNotificationConfig();
        return parse(obisCode, readAttribute(obisCode, eventPushConfig));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, BeaconEventPushNotificationConfig eventPushConfig) throws IOException {

        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return OctetString.fromObisCode(BeaconEventPushNotificationConfig.getDefaultObisCode());
            // Structure containing the Transport type, Destination address and Message type
            case 2:
                return eventPushConfig.readDestinationAndMethod();
            case 4:
                return eventPushConfig.readIsPushEventEnabled();
            default:
                throw new NoSuchRegisterException("BeaconEventPushNotificationConfig attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {


        switch (obisCode.getB()) {

            // Logical name
            case 1:
                return new RegisterValue(obisCode, BeaconEventPushNotificationConfig.getDefaultObisCode().toString());

            case 2:
                if (!abstractDataType.isStructure() || abstractDataType.getStructure().nrOfDataTypes() != 3) {
                    throw new ProtocolException("Cannot parse the beacon event push notification attributes. Should be a structure of with 3 elements");
                } else {
                    //Special parsing for this structure
//                    final String transportType = abstractDataType.getStructure().getDataType(0).isTypeEnum() ? AlarmConfigurationMessage.TransportType.getStringValue(abstractDataType.getStructure().getDataType(0).getTypeEnum().intValue()) : "Incompatible";
//                    final String destinationAddress = abstractDataType.getStructure().getDataType(1).getOctetString().stringValue();
//                    final String messageType = abstractDataType.getStructure().getDataType(2).isTypeEnum() ? AlarmConfigurationMessage.MessageType.getStringValue(abstractDataType.getStructure().getDataType(2).getTypeEnum().intValue()) : "Incompatible";
                    //TODO don't do messages for the moment
                    final String transportType = "Incompatible";
                    final String destinationAddress = abstractDataType.getStructure().getDataType(1).getOctetString().stringValue();
                    final String messageType = "Incompatible";
                    return new RegisterValue(obisCode,
                            "Transport type: " + transportType + ", Destination address: " + destinationAddress + ", Message type: " + messageType);
                }
            case 4:
                final Boolean isEnabled = abstractDataType.getBooleanObject().getState();
                return new RegisterValue(obisCode,
                        "Event notification enabled: " + isEnabled);
            default:
                throw new NoSuchRegisterException("BeaconEventPushNotificationConfig attribute [" + obisCode.getB() + "] not supported!");

        }
    }
}
