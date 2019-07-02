package com.energyict.protocolimplv2.dlms.idis.hs3300.messages;

import com.energyict.common.CommonCryptoMessageExecutor;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.Data;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DeviceConfigurationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

import static com.energyict.dlms.aso.SecurityPolicy.REQUESTS_SIGNED_FLAG;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_KEK_RENEWAL_OBISCODE;
import static com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper.PSK_RENEWAL_OBISCODE;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.newPSKKEKAttributeName;

public class CryptoHS3300MessageExecutor extends HS3300MessageExecutor {

    private static final String SEPARATOR = ",";

    private final CommonCryptoMessageExecutor executor;

    public CryptoHS3300MessageExecutor(AbstractDlmsProtocol protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(protocol, collectedDataFactory, issueFactory);
        this.executor = new CommonCryptoMessageExecutor(protocol, collectedDataFactory, issueFactory);
    }

    private String changeKey(OfflineDeviceMessage pendingMessage, String keyAttributeName, ObisCode keyObis) throws IOException {
        String[] hsmKeyAndLabelAndSmartMeterKey = getDeviceMessageAttributeValue(pendingMessage, keyAttributeName).split(SEPARATOR);
        if (hsmKeyAndLabelAndSmartMeterKey.length != 2) {
            throw DeviceConfigurationException.unexpectedHsmKeyFormat();
        }

        final String newKey = hsmKeyAndLabelAndSmartMeterKey[0];
        final String newWrappedKey = hsmKeyAndLabelAndSmartMeterKey[1];
        byte[] keyBytes = ProtocolTools.getBytesFromHexString(newWrappedKey, "");

        Data keyRenewalObject = getProtocol().getDlmsSession().getCosemObjectFactory().getData(keyObis);
        try {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().setBit(REQUESTS_SIGNED_FLAG);
            keyRenewalObject.setValueAttr(OctetString.fromByteArray(keyBytes));
        } catch (ConnectionCommunicationException e) {
            //Swallow this exception. It is the Beacon that responds an error because the logical device of the meter no longer exists.
            //Indeed, this is expected behaviour because the meter disconnects immediately after the PSK is written.
        } finally {
            getProtocol().getDlmsSession().getAso().getSecurityContext().getSecurityPolicy().unsetBit(REQUESTS_SIGNED_FLAG);
        }

        return newKey;
    }

    @Override
    protected void changePSK(OfflineDeviceMessage pendingMessage) throws IOException {

        String hsmKeyAndLabel = changeKey(pendingMessage, newPSKAttributeName, PSK_RENEWAL_OBISCODE);

    }

    @Override
    protected void changePSKKEK(OfflineDeviceMessage pendingMessage) throws IOException {

        String hsmKeyAndLabel = changeKey(pendingMessage, newPSKKEKAttributeName, PSK_KEK_RENEWAL_OBISCODE);

    }

}
