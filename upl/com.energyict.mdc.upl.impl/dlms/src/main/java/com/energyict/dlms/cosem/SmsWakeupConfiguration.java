package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.SMSWakeupConfigurationAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 12:00
 * Author: khe
 */
public class SmsWakeupConfiguration extends AbstractCosemObject {

    public static byte[] LN = new byte[]{0, 0, 2, 3, 0, (byte) 255};

    /**
     * Creates a new instance of AbstractCosemObject
     *
     * @param protocolLink
     * @param objectReference
     */
    public SmsWakeupConfiguration(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public SmsWakeupConfiguration(ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(LN));
    }

    public ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.SMS_WAKEUP_CONFIGURATION.getClassId();
    }

    /**
     * Write the start and the end time of the listening window
     */
    public void writeListeningWindow(OctetString start, OctetString end) throws IOException {
        Structure window = new Structure();
        window.addDataType(start);
        window.addDataType(end);
        Array windows = new Array();
        windows.addDataType(window);
        writeListeningWindow(windows);
    }

    public void writeListeningWindow(Array window) throws IOException {
        write(SMSWakeupConfigurationAttributes.LISTENING_WINDOW, window);
    }

    /**
     * Write the list of allowed senders
     */
    public void writeAllowedSendersAndActions(List<Structure> senders) throws IOException {
        Array sendersArray = new Array();
        for (Structure sender : senders) {
            sendersArray.addDataType(sender);
        }
        write(SMSWakeupConfigurationAttributes.SENDERS_AND_ACTIONS, sendersArray);
    }
}