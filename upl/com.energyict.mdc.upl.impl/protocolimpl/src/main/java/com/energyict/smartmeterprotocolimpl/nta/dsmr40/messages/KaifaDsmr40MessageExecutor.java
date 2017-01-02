package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;

import java.io.IOException;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 10/04/13
 * Time: 16:14
 * Author: khe
 */
public class KaifaDsmr40MessageExecutor extends Dsmr40MessageExecutor {

    /**
     * The IBM Kaifa meter only accepts value 0x01 as boolean TRUE.
     */
    protected int getBooleanValue() {
        return 0x01;
    }

    public KaifaDsmr40MessageExecutor(AbstractSmartNtaProtocol protocol, TariffCalendarFinder calendarFinder, TariffCalendarExtractor extractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor) {
        super(protocol, calendarFinder, extractor, messageFileFinder, messageFileExtractor);
    }

    protected void resetMbusClient(MessageHandler messageHandler) throws IOException {

        //Find the MBus channel based on the given MBus serial number
        String mbusSerialNumber = messageHandler.getMbusSerialNumber();
        int channel = 0;
        for (DeviceMapping deviceMapping : getProtocol().getMeterTopology().getMbusMeterMap()) {
            if (deviceMapping.getSerialNumber().equals(mbusSerialNumber)) {
                channel = deviceMapping.getPhysicalAddress();
                break;
            }
        }
        if (channel == 0) {
            throw new IOException("No MBus slave meter with serial number '" + mbusSerialNumber + "' is installed on this e-meter");
        }

        ObisCode mbusClientObisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) channel);
        MBusClient mbusClient = getProtocol().getDlmsSession().getCosemObjectFactory().getMbusClient(mbusClientObisCode, MbusClientAttributes.VERSION9);
        try{
            mbusClient.setIdentificationNumber(new Unsigned32(0));
            mbusClient.setManufacturerID(new Unsigned16(0));
            mbusClient.setVersion(0);
            mbusClient.setDeviceType(0);
        }catch(ProtocolException e){
            log(Level.SEVERE,"Invalid short id value.");
        }
    }
}