package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr40.messages.Dsmr40MessageExecutor;

import java.io.IOException;
import java.util.Calendar;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Messsage executor implementation for DSMR 5.0
 * <p/>
 * Mostly reuses the DSMR4.0 functionality, but changes a few things.<br/>
 * <b>Important:</b> for DSMR5.0, the new keys (message to change AK and/or EK) are used immediately, instead of only at the start of the next message!
 * Also, when changing the encryption key, the framecounter is restarted.
 *
 * @author sva
 * @since 6/01/2015 - 16:52
 */
public class Dsmr50MessageExecutor extends Dsmr40MessageExecutor {

    private static final String RESUME = "resume";

    public Dsmr50MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }


    @Override
    protected boolean isResume(OfflineDeviceMessage pendingMessage) {
        return (pendingMessage.getTrackingId() != null) && (pendingMessage.getTrackingId().toLowerCase().contains(RESUME));
    }

    @Override
    protected void changeAuthenticationKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String newAuthenticationKey = getDeviceMessageAttributeValue(pendingMessage, newAuthenticationKeyAttributeName);
        String newWrappedAuthenticationKey = getDeviceMessageAttributeValue(pendingMessage, newWrappedAuthenticationKeyAttributeName);
        byte[] authenticationKeysBytes = ProtocolTools.getBytesFromHexString(newWrappedAuthenticationKey);

        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(authenticationKeysBytes));
        authenticationKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(ProtocolTools.getBytesFromHexString(newAuthenticationKey, ""));
    }

    @Override
    protected void changeEncryptionKeyAndUseNewKey(OfflineDeviceMessage pendingMessage) throws IOException {
        String newEncrytionKey = getDeviceMessageAttributeValue(pendingMessage, newEncryptionKeyAttributeName);
        String newWrappedEncryptionKey = getDeviceMessageAttributeValue(pendingMessage, newWrappedEncryptionKeyAttributeName);
        byte[] encryptionKeysBytes = ProtocolTools.getBytesFromHexString(newWrappedEncryptionKey);

        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(encryptionKeysBytes));
        encryptionKeyArray.addDataType(keyData);

        SecuritySetup ss = getCosemObjectFactory().getSecuritySetup();
        ss.transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        getProtocol().getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(ProtocolTools.getBytesFromHexString(newEncrytionKey, ""));
    }

    /**
     * Convert the given epoch activation date to a proper DateTimeArray<br/>
     * The conversion is slightly different then the DSMR4.0 implementation:
     * <ul>
     *     <li>Day of week should be masked 0xFF</li>
     *     <li>Milliseconds should be masked 0xFF</li>
     * </ul>
     */
    @Override
    protected Array convertActivationDateEpochToDateTimeArray(String strDate) {
        Calendar cal = Calendar.getInstance(getProtocol().getTimeZone());
        cal.setTimeInMillis(Long.parseLong(strDate));
        byte[] dateBytes = new byte[5];
        dateBytes[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        dateBytes[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        dateBytes[2] = (byte) ((cal.get(Calendar.MONTH) & 0xFF) + 1);
        dateBytes[3] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0xFF);
        dateBytes[4] = (byte) 0xFF;
        OctetString date = OctetString.fromByteArray(dateBytes);
        byte[] timeBytes = new byte[4];
        timeBytes[0] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        timeBytes[1] = (byte) cal.get(Calendar.MINUTE);
        timeBytes[2] = (byte) 0x00;
        timeBytes[3] = (byte) 0xFF;
        OctetString time = OctetString.fromByteArray(timeBytes);

        Array dateTimeArray = new Array();
        Structure strDateTime = new Structure();
        strDateTime.addDataType(time);
        strDateTime.addDataType(date);
        dateTimeArray.addDataType(strDateTime);
        return dateTimeArray;
    }
}