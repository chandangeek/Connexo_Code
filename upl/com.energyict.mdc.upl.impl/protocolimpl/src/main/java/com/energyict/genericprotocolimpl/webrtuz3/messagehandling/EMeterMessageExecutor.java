package com.energyict.genericprotocolimpl.webrtuz3.messagehandling;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.ActivityCalendarMessage;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.webrtuz3.EMeter;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gna
 * @beginChanges GNA |15042009| The dataField in the TestMessage has a '0x' prefix for better visualization.
 * GNA |04062009| Activate now is not a message value of the FirmwareUpgrade anymore, functionality is changes as it works with the disconnector
 * if the activation date isn't filled in, then the activation takes place immediately
 * @endChanges
 */
public class EMeterMessageExecutor extends GenericMessageExecutor {

    public static final ObisCode DISCONNECTOR_OBIS = ObisCode.fromString("0.0.96.3.10.255");
    public static final ObisCode DISCONNECTOR_SCRIPT_TABLE_OBIS = ObisCode.fromString("0.0.10.0.106.255");
    public static final ObisCode DISCONNECTOR_CTR_SCHEDULE_OBIS = ObisCode.fromString("0.0.15.0.1.255");

    private EMeter eMeter;

    public EMeterMessageExecutor(EMeter webRTU) {
        this.eMeter = webRTU;
    }

    private EMeter getEmeter() {
        return this.eMeter;
    }

    public void doMessage(OldDeviceMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        String content = rtuMessage.getContents();
        MessageHandler messageHandler = new MessageHandler();
        try {
            importMessage(content, messageHandler);

            boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
            boolean disconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
            boolean connectMode = messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
            boolean touCalendar = messageHandler.getType().equals(RtuMessageConstant.TOU_ACTIVITY_CAL);
            boolean touSpecialDays = messageHandler.getType().equals(RtuMessageConstant.TOU_SPECIAL_DAYS);

            if (connect) {

                getLogger().log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Connect");

                if (!messageHandler.getConnectDate().equals("") && !messageHandler.getConnectDate().equals("0") ) {    // use the disconnectControlScheduler

                    Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
                    SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(DISCONNECTOR_CTR_SCHEDULE_OBIS));

                    Structure scriptStruct = new Structure();
                    scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(DISCONNECTOR_SCRIPT_TABLE_OBIS).getLN()));
                    scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

                    sasConnect.writeExecutedScript(scriptStruct);
                    sasConnect.writeExecutionTime(executionTimeArray);

                } else {    // immediate connect
                    Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                    connector.remoteReconnect();
                }

                success = true;
            } else if (disconnect) {

                getLogger().log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Disconnect");

                if (!messageHandler.getDisconnectDate().equals("") && !messageHandler.getDisconnectDate().equals("0")) { // use the disconnectControlScheduler

                    Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
                    SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(DISCONNECTOR_CTR_SCHEDULE_OBIS));

                    Structure scriptStruct = new Structure();
                    scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(DISCONNECTOR_SCRIPT_TABLE_OBIS).getLN()));
                    scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

                    sasDisconnect.writeExecutedScript(scriptStruct);
                    sasDisconnect.writeExecutionTime(executionTimeArray);

                } else {     // immediate disconnect
                    Disconnector disconnector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                    disconnector.remoteDisconnect();
                }

                success = true;
            } else if (touCalendar) {

                getLogger().log(Level.INFO, "Handling message: Set Activity calendar for E-meter");

                String name = messageHandler.getTOUCalendarName();
                String activateDate = messageHandler.getTOUActivationDate();
                String codeTable = messageHandler.getTOUCodeTable();
                String userFile = messageHandler.getTOUUserFile();

                boolean activateNow = (activateDate != null) && (activateDate.equalsIgnoreCase("0"));

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

                        ObisCode acObisCode = getCorrectedObisCode(getMeterConfig().getActivityCalendar().getObisCode());
                        ActivityCalendar ac = getCosemObjectFactory().getActivityCalendar(acObisCode);
                        ac.writeSeasonProfilePassive(acm.getSeasonProfile());
                        ac.writeWeekProfileTablePassive(acm.getWeekProfile());
                        ac.writeDayProfileTablePassive(acm.getDayProfile());

                        if (name != null) {
                            if (name.length() > 8) {
                                name = name.substring(0, 8);
                            }
                            ac.writeCalendarNamePassive(OctetString.fromString(name));
                        }

                        if (activateNow) {
                            ac.activateNow();
                        } else if (activateDate != null) {
                            ac.writeActivatePassiveCalendarTime(new OctetString(convertUnixToGMTDateTime(activateDate).getBEREncodedByteArray(), 0));
                        }

                    }

                } else if (userFile != null) {
                    throw new IOException("ActivityCalendar by userfile is not supported yet.");
                } else {
                    // should never get here
                    throw new IOException("CodeTable-ID AND UserFile-ID can not be both empty.");
                }

                success = true;

            } else if (touSpecialDays) {

                getLogger().log(Level.INFO, "Handling message: Set Special Days table for E-meter");

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

                        ObisCode sdtObis = getCorrectedObisCode(getMeterConfig().getSpecialDaysTable().getObisCode());
                        SpecialDaysTable sdt = getCosemObjectFactory().getSpecialDaysTable(sdtObis);

                        for (int i = 0; i < calendars.size(); i++) {
                            CodeCalendar cc = (CodeCalendar) calendars.get(i);
                            if (cc.getSeason() == 0) {
                                OctetString os = OctetString.fromByteArray(new byte[]{(byte) ((cc.getYear() == -1) ? 0xff : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xff : (cc.getYear()) & 0xFF),
                                        (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                                        (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek())});
                                Unsigned8 dayType = new Unsigned8(cc.getDayType().getId());
                                Structure struct = new Structure();
                                AXDRDateTime dt = new AXDRDateTime(new byte[]{(byte) 0x09, (byte) 0x0C, (byte) ((cc.getYear() == -1) ? 0x07 : ((cc.getYear() >> 8) & 0xFF)), (byte) ((cc.getYear() == -1) ? 0xB2 : (cc.getYear()) & 0xFF),
                                        (byte) ((cc.getMonth() == -1) ? 0xFF : cc.getMonth()), (byte) ((cc.getDay() == -1) ? 0xFF : cc.getDay()),
                                        (byte) ((cc.getDayOfWeek() == -1) ? 0xFF : cc.getDayOfWeek()), 0, 0, 0, 0, 0, 0, 0});
                                long days = dt.getValue().getTimeInMillis() / 1000 / 60 / 60 / 24;
                                struct.addDataType(new Unsigned16((int) days));
                                struct.addDataType(os);
                                struct.addDataType(dayType);
                                sdArray.addDataType(struct);
                            }
                        }

                        if (sdArray.nrOfDataTypes() != 0) {
                            sdt.writeSpecialDays(sdArray);
                        }

                        success = true;
                    }
                }
            } else if (connectMode) {

                getLogger().log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": ConnectControl mode");
                String mode = messageHandler.getConnectControlMode();

                if (mode != null) {
                    try {
                        int modeInt = Integer.parseInt(mode);

                        if ((modeInt >= 0) && (modeInt <= 6)) {
                            Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                            connectorMode.writeControlMode(new TypeEnum(modeInt));

                        } else {
                            throw new IOException("Mode is not a valid entry for message " + rtuMessage.displayString() + ", value must be between 0 and 6");
                        }

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        throw new IOException("Mode is not a valid entry for message " + rtuMessage.displayString());
                    }
                } else {
                    // should never get to the else, can't leave message empty
                    throw new IOException("Message " + rtuMessage.displayString() + " can not be empty");
                }

                success = true;
            } else {
                success = false;
            }

        } catch (BusinessException e) {
            e.printStackTrace();
            getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } catch (ConnectionException e) {
            e.printStackTrace();
            getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } finally {
            if (success) {
                rtuMessage.confirm();
                getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has finished.");
            } else {
                rtuMessage.setFailed();
            }
        }
    }

    private Logger getLogger() {
        return getEmeter().getLogger();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getEmeter().getCosemObjectFactory();
    }

    /**
     * Getter for the DLMSMeterConfig object
     * @return
     */
    private DLMSMeterConfig getMeterConfig() {
        return getEmeter().getMeterConfig();
    }

    /**
     * @return the current MeteringWarehouse
     */
    private MeteringWarehouse mw() {
        return CommonUtils.mw();
    }

    /**
     * Getter for the timeZone
     * @return
     */
    protected TimeZone getTimeZone() {
        return getEmeter().getTimeZone();
    }

    /**
     * Get the corrected obiscode to match the physical address in the B-field
     *
     * @param obisCode
     * @return
     */
    private ObisCode getCorrectedObisCode(ObisCode obisCode) {
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) getEmeter().getPhysicalAddress());
    }

}