package com.energyict.smartmeterprotocolimpl.nta.dsmr40.xemex.messages;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.messages.Dsmr23MbusMessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;

import java.io.IOException;
import java.time.Clock;
import java.util.logging.Level;

/**
 * @author sva
 * @since 26/02/13 - 10:03
 */
public class XemexMessageExecutor extends Dsmr40MessageExecutor {

    public static String ENABLE_DST = "EnableDST";

    private static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    private final ObisCode ALARM_FILTER = ObisCode.fromString("0.0.97.98.10.255");

    public XemexMessageExecutor(AbstractSmartNtaProtocol protocol, Clock clock, TopologyService topologyService, CalendarService calendarService) {
        super(protocol, clock, topologyService, calendarService);
    }

    @Override
    public MessageResult executeMessageEntry(MessageEntry msgEntry) throws ConnectionException, NestedIOException {
        if (!this.protocol.getSerialNumber().equalsIgnoreCase(msgEntry.getSerialNumber())) {
            Dsmr23MbusMessageExecutor mbusMessageExecutor = new Dsmr23MbusMessageExecutor(protocol, this.getClock());
            return mbusMessageExecutor.executeMessageEntry(msgEntry);
        } else {
            MessageResult msgResult = null;
            try {
                if (isItThisMessage(msgEntry, RtuMessageConstant.RESET_ERROR_REGISTER)) {
                    resetErrorRegister();
                } else if (isItThisMessage(msgEntry, RtuMessageConstant.ALARM_FILTER)) {
                    setAlarmFilter(msgEntry);
                } else if (isItThisMessage(msgEntry, ENABLE_DST)) {
                    enableDST(msgEntry);
                } else {
                    return super.executeMessageEntry(msgEntry);
                }

                // Some message create their own messageResult
                if (msgResult == null) {
                    msgResult = MessageResult.createSuccess(msgEntry);
                    log(Level.INFO, "Message has finished.");
                } else if (msgResult.isFailed()) {
                    log(Level.SEVERE, "Message failed : " + msgResult.getInfo());
                }

            } catch (ConnectionException e) {
                throw new ConnectionException(e.getMessage());
            } catch (NestedIOException e) {
                Throwable rootCause = getRootCause(e);
                if (rootCause.getClass().equals(ConnectionException.class)) {
                    throw new NestedIOException(rootCause);
                }
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            } catch (IOException e) {
                msgResult = MessageResult.createFailed(msgEntry, e.getMessage());
                log(Level.SEVERE, "Message failed : " + e.getMessage());
            }
            return msgResult;
        }
    }

    private void resetErrorRegister() throws IOException {
        log(Level.INFO, "Handling message Reset Error register.");
        getCosemObjectFactory().getData(ERROR_REGISTER).setValueAttr(new Unsigned32(0));
    }

    private void setAlarmFilter(MessageEntry entry) throws IOException {
        log(Level.INFO, "Handling message Set Alarm Filter.");
        try {
            String hexValue = getValueFromXML(RtuMessageConstant.ALARM_FILTER, entry.getContent());
            if (hexValue.startsWith("0x")) {
                hexValue = hexValue.substring(2);
            }
            long value = Long.parseLong(hexValue, 16);
            getCosemObjectFactory().getData(ALARM_FILTER).setValueAttr(new Unsigned32(value));
        } catch (NullPointerException e) {
            throw new IOException("Could not parse the given Alarm Filter value.");
        } catch (NumberFormatException e) {
            throw new IOException("Could not parse the given Alarm Filter value: " + e.getMessage());
        }
    }

    private void enableDST(MessageEntry entry) throws IOException {
        log(Level.INFO, "Handling message EnableDST message.");
        String mode = getValueFromXML(ENABLE_DST, entry.getContent());
        if (mode.equals("0")) {
            log(Level.INFO, "Disabling DST switching.");
        } else if (mode.equals("1")) {
            log(Level.INFO, "Enabling DST switching");
        } else {
            String messageToLog = "Failed to parse the message value.";
            log(Level.INFO, messageToLog);
            throw new IOException(messageToLog);
        }

        com.energyict.dlms.cosem.Clock clock = getCosemObjectFactory().getClock();
        clock.enableDisableDs(mode.equals("1") ? true : false);
    }

    private String getValueFromXML(String tag, String content) {
        int startIndex = content.indexOf("<" + tag);
        int endIndex = content.indexOf("</" + tag);
        return content.substring(startIndex + tag.length() + 2, endIndex);
    }

    /**
     * Checks if the given MessageEntry contains the corresponding MessageTag
     *
     * @param messageEntry the given messageEntry
     * @param messageTag   the tag to check
     * @return true if this is the message, false otherwise
     */
    protected boolean isItThisMessage(MessageEntry messageEntry, String messageTag) {
        return messageEntry.getContent().indexOf(messageTag) >= 0;
    }
}
