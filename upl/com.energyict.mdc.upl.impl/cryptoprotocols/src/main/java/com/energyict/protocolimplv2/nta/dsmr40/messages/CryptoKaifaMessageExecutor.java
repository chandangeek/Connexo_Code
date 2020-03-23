package com.energyict.protocolimplv2.nta.dsmr40.messages;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.MBusClient;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 22/04/13
 * Time: 12:13
 * Author: khe
 */
public class CryptoKaifaMessageExecutor extends CryptoDSMR40MessageExecutor {
    public CryptoKaifaMessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(protocol, collectedDataFactory, issueFactory, keyAccessorTypeExtractor);
    }

    /**
     * The IBM Kaifa meter only accepts value 0x01 as boolean TRUE.
     */
    protected int getBooleanValue() {
        return 0x01;
    }


    @Override
    protected void mbusReset(OfflineDeviceMessage pendingMessage) throws IOException {
        //Find the MBus channel based on the given MBus serial number
        String mbusSerialNumber = pendingMessage.getDeviceSerialNumber();
        int channel = 0;
        for (com.energyict.protocolimplv2.common.topology.DeviceMapping deviceMapping : ((com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol)getProtocol()).getMeterTopology().getMbusMeterMap()) {
            if (deviceMapping.getSerialNumber().equals(mbusSerialNumber)) {
                channel = deviceMapping.getPhysicalAddress();
                break;
            }
        }
        if (channel == 0) {
            throw new IOException("No MBus slave meter with serial number '" + mbusSerialNumber + "' is installed on this e-meter");
        }

        ObisCode mbusClientObisCode = ProtocolTools.setObisCodeField(MBUS_CLIENT_OBISCODE, 1, (byte) channel);
        MBusClient mbusClient = getProtocol().getDlmsSession().getCosemObjectFactory().getMbusClient(mbusClientObisCode, MbusClientAttributes.VERSION10);
        try{
            mbusClient.setIdentificationNumber(new Unsigned32(0));
            mbusClient.setManufacturerID(new Unsigned16(0));
            mbusClient.setVersion(0);
            mbusClient.setDeviceType(0);
        }catch(ProtocolException e){
        }
    }
}
