package com.energyict.protocolimplv2.elster.ctr.MTU155.messaging;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.CTRObjectFactory;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 3/07/12
 * Time: 10:25
 */
public class IPSetupMessage extends AbstractMTU155Message {

    private static final CTRObjectID OBJECT_ID = new CTRObjectID("E.3.2");
    private byte[] ipAddressBytes = new byte[4];
    private byte[] tcpPortBytes = new byte[2];

    public IPSetupMessage(Messaging messaging, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(messaging, collectedDataFactory, issueFactory);
    }

    @Override
    public boolean canExecuteThisMessage(OfflineDeviceMessage message) {
        return message.getSpecification().getId() == NetworkConnectivityMessage.CHANGE_GPRS_IP_ADDRESS_AND_PORT.id();
    }

    @Override
    protected CollectedMessage doExecuteMessage(OfflineDeviceMessage message) throws CTRException {
        String ipAddress = getDeviceMessageAttribute(message, DeviceMessageConstants.ipAddressAttributeName).getValue();
        String tcpPort = getDeviceMessageAttribute(message, DeviceMessageConstants.portNumberAttributeName).getValue();

        validateIPSetupParameters(ipAddress, tcpPort);
        writeIpAddressAndPortNumberSetup();
        return null;
    }

    private void validateIPSetupParameters(String ipAddress, String tcpPort) throws CTRException {
        if (ipAddress.split("\\.").length != 4) {
            String msg = "Parameter IP_Address contains no valid IP address";
            throw new CTRException(msg);
        }

        String[] split = ipAddress.split("\\.");
        try {
            for (int i = 0; i < 4; i++) {
                ipAddressBytes[i] = (byte) Integer.parseInt(split[i]);
            }
        } catch (NumberFormatException e) {
            String msg = "Parameter IP_Address contains no valid IP address";
            throw new CTRException(msg);
        }

        try {
            int port = Integer.parseInt(tcpPort);
            if (port < 0 || port > 65535) {
                String msg = "Parameter TCP_Port should be an integer number range 0-65535.";
                throw new CTRException(msg);

            } else {
                tcpPortBytes = ProtocolTools.getBytesFromInt(port, 2);
            }
        } catch (NumberFormatException e) {
            String msg = "Parameter TCP_Port should be an integer number range 0-65535.";
            throw new CTRException(msg);
        }
    }

    private void writeIpAddressAndPortNumberSetup() throws CTRException {
        ReferenceDate validityDate = new ReferenceDate().parse(new Date(), getFactory().getTimeZone());
        WriteDataBlock wdb = new WriteDataBlock((int) (100 * Math.random()));
        P_Session p_session = new P_Session(0x00);
        AttributeType attributeType = AttributeType.getValueOnly();
        attributeType.setHasIdentifier(true);
        byte[] rawData;

        if (!getFactory().isEK155Protocol() && firmwareBelow200()) {
            byte[] paddingBytes = new byte[8];
            rawData = ProtocolTools.concatByteArrays(OBJECT_ID.getBytes(), new byte[]{0x04}, ipAddressBytes, tcpPortBytes, paddingBytes);
        } else {
            byte[] paddingBytes = new byte[8];
            rawData = ProtocolTools.concatByteArrays(OBJECT_ID.getBytes(), new byte[]{0x04}, new byte[]{(byte) 0x81}, ipAddressBytes, tcpPortBytes, paddingBytes);
        }

        CTRObjectFactory objectFactory = new CTRObjectFactory();
        AbstractCTRObject object = objectFactory.parse(rawData, 0, attributeType);
        getFactory().writeRegister(validityDate, wdb, p_session, attributeType, object);
    }

    /**
     * Test is the MTU155 firmware version is below 0200.
     *
     * @return true if the version is below 0200
     *         false if the version is equal or higher than 0200
     */
    private Boolean firmwareBelow200() {
        IdentificationResponseStructure structure = getFactory().getIdentificationStructure();
        if (structure != null) {
            AbstractCTRObject vf = structure.getVf();
            try {
                return (Integer.parseInt(vf.getValue(0).getStringValue().substring(3, 6)) < 200);
            } catch (NumberFormatException e) {
            }
        }
        return true;    //By default we set true
    }
}
