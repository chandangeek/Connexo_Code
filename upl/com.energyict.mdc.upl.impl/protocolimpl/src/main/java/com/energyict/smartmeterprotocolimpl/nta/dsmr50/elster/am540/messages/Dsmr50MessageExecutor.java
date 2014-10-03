package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.SpecialDaysTable;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.mdw.core.CodeDayType;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.generic.messages.ActivityCalendarMessage;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Mostly reuses the DSMR4.0 functionality, but changes a few things.
 * Important: for DSMR5.0, the new keys (message to change AK and/or EK) are used immediately, instead of only at the start of the next message!
 * Also, when changing the encryption key, the framecounter is restarted.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 12/06/2014 - 15:20
 */
public class Dsmr50MessageExecutor extends Dsmr40MessageExecutor {

    private static final String RESUME = "resume";

    public Dsmr50MessageExecutor(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected boolean isResume(MessageEntry messageEntry) {
        return (messageEntry.getTrackingId() != null) && (messageEntry.getTrackingId().toLowerCase().contains(RESUME));
    }

    protected ActivityCalendarMessage getActivityCalendarParser(Code ct) {
        return new Dsmr50ActivityCalendarParser(ct, getMeterConfig());
    }

    @Override
    protected void upgradeSpecialDays(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Special Days table");

        String codeTable = messageHandler.getSpecialDaysCodeTable();

        if (codeTable == null) {
            throw new IOException("CodeTable-ID can not be empty.");
        } else {

            Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
            if (ct == null) {
                throw new IOException("No CodeTable defined with id '" + codeTable + "'");
            } else {

                List calendars = ct.getCalendars();
                Array sdArray = new Array();

                SpecialDaysTable specialDaysTable = getCosemObjectFactory().getSpecialDaysTable(getMeterConfig().getSpecialDaysTable().getObisCode());

                //Create day type IDs (incremental 0-based)
                Map<Integer, Integer> dayTypeIds = new HashMap<Integer, Integer>();  //Map the DB id's of the day types to a proper 0-based index that can be used in the AXDR array
                List<CodeDayType> dayTypes = ct.getDayTypes();
                for (int dayTypeIndex = 0; dayTypeIndex < dayTypes.size(); dayTypeIndex++) {
                    CodeDayType dayType = dayTypes.get(dayTypeIndex);
                    if (!dayTypeIds.containsKey(dayType.getId())) {
                        dayTypeIds.put(dayType.getId(), dayTypeIndex);
                    }
                }

                int dayIndex = 0;
                for (Object calendar : calendars) {
                    CodeCalendar codeCalendar = (CodeCalendar) calendar;
                    if (codeCalendar.getSeason() == 0) {
                        OctetString timeStamp = OctetString.fromByteArray(new byte[]{(byte) ((codeCalendar.getYear() == -1) ? 0xff : ((codeCalendar.getYear() >> 8) & 0xFF)), (byte) ((codeCalendar.getYear() == -1) ? 0xff : (codeCalendar.getYear()) & 0xFF),
                                (byte) ((codeCalendar.getMonth() == -1) ? 0xFF : codeCalendar.getMonth()), (byte) ((codeCalendar.getDay() == -1) ? 0xFF : codeCalendar.getDay()),
                                (byte) ((codeCalendar.getDayOfWeek() == -1) ? 0xFF : codeCalendar.getDayOfWeek())});
                        Unsigned8 dayType = new Unsigned8(dayTypeIds.get(codeCalendar.getDayType().getId()));
                        Structure specialDayStructure = new Structure();
                        specialDayStructure.addDataType(new Unsigned16(dayIndex));
                        specialDayStructure.addDataType(timeStamp);
                        specialDayStructure.addDataType(dayType);
                        sdArray.addDataType(specialDayStructure);
                        dayIndex++;
                    }
                }

                sdArray = sort(sdArray);

                if (sdArray.nrOfDataTypes() != 0) {
                    specialDaysTable.writeSpecialDays(sdArray);
                }
            }
        }
    }

    protected Array sort(Array specialDaysArray) {
        return specialDaysArray;    //No sorting needed, subclasses can override
    }

    @Override
    protected void changeAuthenticationKey(MessageHandler messageHandler) throws IOException {
        protocol.getLogger().info("Received [ChangeAuthenticationKeyMessage], wrapped key is '" + ProtocolTools.getHexStringFromBytes(messageHandler.getNewAuthenticationKey(), "") + "'");
        Array authenticationKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(2));    // 2 means keyType: authenticationKey
        keyData.addDataType(OctetString.fromByteArray(messageHandler.getNewAuthenticationKey()));
        authenticationKeyArray.addDataType(keyData);

        protocol.getDlmsSession().getCosemObjectFactory().getSecuritySetup().transferGlobalKey(authenticationKeyArray);

        //Update the key in the security provider, it is used instantly
        protocol.getDlmsSession().getProperties().getSecurityProvider().changeAuthenticationKey(messageHandler.getPlainAuthenticationKey());
    }

    @Override
    protected void changeGlobalKey(MessageHandler messageHandler) throws IOException {
        String oldGlobalKey = ProtocolTools.getHexStringFromBytes(protocol.getDlmsSession().getProperties().getSecurityProvider().getGlobalKey(), "");
        protocol.getLogger().info("Received [ChangeEncryptionKeyMessage], wrapped key is '" + ProtocolTools.getHexStringFromBytes(messageHandler.getNewEncryptionKey(), "") + "'");
        Array encryptionKeyArray = new Array();
        Structure keyData = new Structure();
        keyData.addDataType(new TypeEnum(0));    // 0 means keyType: encryptionKey (global key)
        keyData.addDataType(OctetString.fromByteArray(messageHandler.getNewEncryptionKey()));
        encryptionKeyArray.addDataType(keyData);

        protocol.getDlmsSession().getCosemObjectFactory().getSecuritySetup().transferGlobalKey(encryptionKeyArray);

        //Update the key in the security provider, it is used instantly
        protocol.getDlmsSession().getProperties().getSecurityProvider().changeEncryptionKey(messageHandler.getPlainEncryptionKey());

        //Reset frame counter, only if a different key has been written
        if (!oldGlobalKey.equalsIgnoreCase(ProtocolTools.getHexStringFromBytes(messageHandler.getPlainEncryptionKey(), ""))) {
            protocol.getDlmsSession().getAso().getSecurityContext().setFrameCounter(1);
        }
    }
}