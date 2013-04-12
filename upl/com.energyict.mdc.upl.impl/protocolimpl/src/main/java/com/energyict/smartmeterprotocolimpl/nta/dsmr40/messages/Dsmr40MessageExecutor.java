package com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.ActivityCalendar;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.Limiter;
import com.energyict.dlms.cosem.ScriptTable;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.messages.ActivityCalendarMessage;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.mdw.core.Code;
import com.energyict.mdw.core.UserFile;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5-sep-2011
 * Time: 8:40:01
 */
public class Dsmr40MessageExecutor extends Dsmr23MessageExecutor {

    private static final ObisCode OBISCODE_CONFIGURATION_OBJECT = ObisCode.fromString("0.1.94.31.3.255");
    private static final ObisCode OBISCODE_PUSH_SCRIPT = ObisCode.fromString("0.0.10.0.108.255");
    private static final ObisCode OBISCODE_GLOBAL_RESET = ObisCode.fromString("0.1.94.31.5.255");
    private static final String TIMEOUT = "timeout";

    public Dsmr40MessageExecutor(final AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    @Override
    protected void activateSms() throws IOException {
        log(Level.INFO, "Enabling SMS wakeup");
        getCosemObjectFactory().getSMSWakeupConfiguration().writeListeningWindow(new Array());
    }

    protected void doFirmwareUpgrade(MessageHandler messageHandler) throws IOException, InterruptedException {
        log(Level.INFO, "Handling message Firmware upgrade");

        String userFileID = messageHandler.getUserFileId();
        if (!ParseUtils.isInteger(userFileID)) {
            String str = "Not a valid entry for the userFile.";
            throw new IOException(str);
        }
        UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileID));
        if (uf == null) {
            String str = "Not a valid entry for the userfileID " + userFileID;
            throw new IOException(str);
        }

        byte[] imageData = uf.loadFileInByteArray();
        ImageTransfer it = getCosemObjectFactory().getImageTransfer();
        it.setUsePollingVerifyAndActivate(true);    //Poll verification
        it.setPollingDelay(5000);
        it.setPollingRetries(15);
        it.setDelayBeforeSendingBlocks(5000);
        it.upgrade(imageData, false);
        if (messageHandler.getActivationDate().equalsIgnoreCase("")) { // Do an execute now
            try {
                it.imageActivation();
            } catch (IOException e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains(TIMEOUT)) {
                    log(Level.INFO, "Polling for activation status timed out, meter is rebooting.");
                    //Move on, meter is rebooting
                } else {
                    throw e;
                }
            }

            //Below is a solution for not immediately activating the image so the current connection isn't lost
//					Calendar cal = Calendar.getInstance();
//					cal.add(Calendar.MINUTE, 2);
//					SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
//					String strDate = Long.toString(cal.getTimeInMillis()/1000);
//					Array dateArray = convertUnixToDateTimeArray(strDate);
//
//					sas.writeExecutionTime(dateArray);


        } else if (!messageHandler.getActivationDate().equalsIgnoreCase("")) {
            SingleActionSchedule sas = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getImageActivationSchedule().getObisCode());
            String strDate = messageHandler.getActivationDate();
            Array dateArray = convertUnixToDateTimeArray(strDate);
            sas.writeExecutionTime(dateArray);
        }
    }

    @Override
    protected void deactivateSms() throws IOException {
        log(Level.INFO, "Disabling SMS wakeup");
        AXDRDateTime axdrDateTime = convertUnixToDateTime(String.valueOf(946684800), getTimeZone());        //Jan 1st, 2000
        OctetString time = new OctetString(axdrDateTime.getBEREncodedByteArray(), 0);
        getCosemObjectFactory().getSMSWakeupConfiguration().writeListeningWindow(time, time);               //Closed window, no SMSes are allowed
    }

    @Override
    protected void setWakeUpWhiteList(MessageHandler messageHandler) throws IOException {
        List<Structure> senders = new ArrayList<Structure>();
        senders.add(createSenderAndAction(messageHandler.getNr1()));
        senders.add(createSenderAndAction(messageHandler.getNr2()));
        senders.add(createSenderAndAction(messageHandler.getNr3()));
        senders.add(createSenderAndAction(messageHandler.getNr4()));
        senders.add(createSenderAndAction(messageHandler.getNr5()));

        getCosemObjectFactory().getSMSWakeupConfiguration().writeAllowedSendersAndActions(senders);
    }

    private Structure createSenderAndAction(String telephoneNumber) {
        Structure senderAndAction = new Structure();
        Structure action = new Structure();
        action.addDataType(OctetString.fromObisCode(OBISCODE_PUSH_SCRIPT));
        action.addDataType(new Unsigned16(3));  //3rd script contains the SMS wakeup actions

        senderAndAction.addDataType(OctetString.fromString(telephoneNumber));
        senderAndAction.addDataType(action);
        return senderAndAction;
    }

    @Override
    protected void restoreFactorySettings() throws IOException {
        log(Level.INFO, "Handling message Restore Factory Settings.");
        ScriptTable globalResetST = getCosemObjectFactory().getScriptTable(OBISCODE_GLOBAL_RESET);
        globalResetST.execute(0);
    }

    @Override
    protected void clearLoadLimiting(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Clear LoadLimit configuration");
        Limiter clearLLimiter = getCosemObjectFactory().getLimiter();
        Structure emptyStruct = new Structure();
        emptyStruct.addDataType(new Unsigned16(0));
        emptyStruct.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString("999901010601000000FFC400", "")));   //set date far in the future...
        emptyStruct.addDataType(new Unsigned32(0));
        clearLLimiter.writeEmergencyProfile(clearLLimiter.new EmergencyProfile(emptyStruct.getBEREncodedByteArray(), 0, 0));
    }

    /**
     * DSMR 4.0 implementation differs from 2.3, override.
     * Order is now: write day profiles, write week profiles, write season profiles.
     */
    @Override
    protected void upgradeCalendar(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set Activity calendar");

        String name = messageHandler.getTOUCalendarName();
        String activateDate = messageHandler.getTOUActivationDate();
        String codeTable = messageHandler.getTOUCodeTable();
        String userFile = messageHandler.getTOUUserFile();

        if ((codeTable == null) && (userFile == null)) {
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
        } else if ((codeTable != null) && (userFile != null)) {
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both filled in.");
        }

        if (codeTable != null) {

            Code ct = mw().getCodeFactory().find(Integer.parseInt(codeTable));
            if (ct == null) {
                throw new IOException("No CodeTable defined with id '" + codeTable + "'");
            } else {

                ActivityCalendarMessage acm = new ActivityCalendarMessage(ct, getMeterConfig());
                acm.parse();

                ActivityCalendar ac = getCosemObjectFactory().getActivityCalendar(getMeterConfig().getActivityCalendar().getObisCode());
                ac.writeDayProfileTablePassive(acm.getDayProfile());
                ac.writeWeekProfileTablePassive(acm.getWeekProfile());
                ac.writeSeasonProfilePassive(acm.getSeasonProfile());

                if (name != null) {
                    if (name.length() > 8) {
                        name = name.substring(0, 8);
                    }
                    ac.writeCalendarNamePassive(OctetString.fromString(name));
                }
                if ((activateDate != null) && (activateDate.length() != 0)) {
                    ac.writeActivatePassiveCalendarTime(new OctetString(convertUnixToDateTime(activateDate, getTimeZone()).getBEREncodedByteArray(), 0));
                } else {
                    ac.activateNow();
                }
            }
        } else if (userFile != null) {
            throw new IOException("ActivityCalendar by userfile is not supported yet.");
        } else {
            // should never get here
            throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
        }
    }

    @Override
    protected MessageResult changeAuthenticationLevel(MessageEntry msgEntry, MessageHandler messageHandler, int type, boolean enable) throws IOException {
        int newAuthLevel = messageHandler.getAuthenticationLevel();
        if (newAuthLevel != -1) {
            Data config = getCosemObjectFactory().getData(OBISCODE_CONFIGURATION_OBJECT);
            Structure value;
            BitString flags;
            try {
                value = (Structure) config.getValueAttr();
                try {
                    AbstractDataType dataType = value.getDataType(1);
                    flags = (BitString) dataType;
                } catch (IndexOutOfBoundsException e) {
                    return MessageResult.createFailed(msgEntry, "Couldn't write configuration. Expected structure value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to have 2 elements.");
                } catch (ClassCastException e) {
                    return MessageResult.createFailed(msgEntry, "Couldn't write configuration. Expected second element of structure to be of type 'Bitstring', but was of type '" + value.getDataType(1).getClass().getSimpleName() + "'.");
                }

                flags.set(4 - type + messageHandler.getAuthenticationLevel(), enable);    //HLS5_P0 = bit9, HLS4_P0 = bit8, HLS3_P0 = bit7, HLS5_P3 = bit6, HLS4_P3 = bit5, HLS3_P3 = bit4
                config.setValueAttr(value);
                return MessageResult.createSuccess(msgEntry);
            } catch (ClassCastException e) {
                return MessageResult.createFailed(msgEntry, "Couldn't write configuration. Expected value of [" + OBISCODE_CONFIGURATION_OBJECT.toString() + "] to be of type 'Structure', but was of type '" + config.getValueAttr().getClass().getSimpleName() + "'.");
            }
        } else {
            return MessageResult.createFailed(msgEntry, "Message contained an invalid authenticationLevel.");
        }
    }

    /**
     * Adds timezone information in the activation timestamp
     */
    @Override
    protected void loadLimitConfiguration(MessageHandler messageHandler) throws IOException {
        log(Level.INFO, "Handling message Set LoadLimit configuration");

        byte theMonitoredAttributeType = -1;
        Limiter loadLimiter = getCosemObjectFactory().getLimiter();

        if (theMonitoredAttributeType == -1) {    // check for the type of the monitored value
            Limiter.ValueDefinitionType valueDefinitionType = loadLimiter.getMonitoredValue();
            if (valueDefinitionType.getClassId().getValue() == 0) {
                setMonitoredValue(loadLimiter);
                valueDefinitionType = loadLimiter.readMonitoredValue();
            }
            theMonitoredAttributeType = getMonitoredAttributeType(valueDefinitionType);
        }

        // Write the normalThreshold
        if (messageHandler.getNormalThreshold() != null) {
            try {
                loadLimiter.writeThresholdNormal(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getNormalThreshold()));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the normalThreshold value to an integer.");
                throw new IOException("Could not pars the normalThreshold value to an integer." + e.getMessage());
            }
        }

        // Write the emergencyThreshold
        if (messageHandler.getEmergencyThreshold() != null) {
            try {
                loadLimiter.writeThresholdEmergency(convertToMonitoredType(theMonitoredAttributeType, messageHandler.getEmergencyThreshold()));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergencyThreshold value to an integer.");
                throw new IOException("Could not pars the emergencyThreshold value to an integer." + e.getMessage());
            }
        }

        // Write the minimumOverThresholdDuration
        if (messageHandler.getOverThresholdDurtion() != null) {
            try {
                loadLimiter.writeMinOverThresholdDuration(new Unsigned32(Integer.parseInt(messageHandler.getOverThresholdDurtion())));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the minimum over threshold duration value to an integer.");
                throw new IOException("Could not pars the minimum over threshold duration value to an integer." + e.getMessage());
            }
        }

        // Construct the emergencyProfile
        Structure emergencyProfile = new Structure();
        if (messageHandler.getEpProfileId() != null) {    // The EmergencyProfileID
            try {
                emergencyProfile.addDataType(new Unsigned16(Integer.parseInt(messageHandler.getEpProfileId())));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergency profile id value to an integer.");
                throw new IOException("Could not pars the emergency profile id value to an integer." + e.getMessage());
            }
        }
        if (messageHandler.getEpActivationTime() != null) {    // The EmergencyProfileActivationTime
            try {
                emergencyProfile.addDataType(new OctetString(convertUnixToDateTime(messageHandler.getEpActivationTime(), getTimeZone()).getBEREncodedByteArray(), 0, true));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergency profile activationTime value to a valid date.");
                throw new IOException("Could not pars the emergency profile activationTime value to a valid date." + e.getMessage());
            }
        }
        if (messageHandler.getEpDuration() != null) {        // The EmergencyProfileDuration
            try {
                emergencyProfile.addDataType(new Unsigned32(Integer.parseInt(messageHandler.getEpDuration())));
            } catch (NumberFormatException e) {
                log(Level.INFO, "Could not pars the emergency profile duration value to an integer.");
                throw new IOException("Could not pars the emergency profile duration value to an integer." + e.getMessage());
            }
        }
        if ((emergencyProfile.nrOfDataTypes() > 0) && (emergencyProfile.nrOfDataTypes() != 3)) {    // If all three elements are correct, then send it, otherwise throw error
            throw new IOException("The complete emergecy profile must be filled in before sending it to the meter.");
        } else {
            if (emergencyProfile.nrOfDataTypes() > 0) {
                loadLimiter.writeEmergencyProfile(emergencyProfile.getBEREncodedByteArray());
            }
        }
    }

}
