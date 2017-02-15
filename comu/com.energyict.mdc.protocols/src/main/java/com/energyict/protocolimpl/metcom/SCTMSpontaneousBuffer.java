/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SCTMSpontaneousBuffer.java
 *
 * Created on 14 december 2004, 15:44
 */

package com.energyict.protocolimpl.metcom;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import com.energyict.protocolimpl.siemens7ED62.SCTMEvent;
import com.energyict.protocolimpl.siemens7ED62.SCTMTimeData;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTM;
import com.energyict.protocolimpl.siemens7ED62.SiemensSCTMException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
/**
 *
 * @author  Koen
 */
public class SCTMSpontaneousBuffer {
    SiemensSCTM siemensSCTMConnection;
    TimeZone timeZone;
    Metcom metcom;
    /** Creates a new instance of SCTMSpontaneousBuffer */
//    public SCTMSpontaneousBuffer(SiemensSCTM siemensSCTMConnection,TimeZone timeZone) {
//        this.siemensSCTMConnection=siemensSCTMConnection;
//        this.timeZone=timeZone;
//    }

    public SCTMSpontaneousBuffer(Metcom metcom) {
        this.metcom=metcom;
        this.siemensSCTMConnection=metcom.getSCTMConnection();
        this.timeZone=metcom.getTimeZone();
    }


    protected void getEvents(Calendar calendarFrom,Calendar calendarTo,ProfileData profileData) throws IOException {
       SCTMTimeData from = new SCTMTimeData(calendarFrom);
       SCTMTimeData to = new SCTMTimeData(calendarTo);
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       baos.write(SiemensSCTM.SPONTANEOUSBUFFERS);
       baos.write(from.getBUFENQData());
       if (metcom.getLogbookReadCommand()[1] == '6')
           baos.write(to.getBUFENQData());
       List sctmEvents = doGetEvents(metcom.getLogbookReadCommand(),baos.toByteArray());
       addToProfile(sctmEvents,profileData);
    }

    private List doGetEvents(byte[] command, byte[] data) throws IOException {
        try {
           List sctmEvents = new ArrayList();
           byte[] received;

           while(true) {
               received = siemensSCTMConnection.sendRequest(command, data);
               if (received == null) break;
               SCTMEvent sctmEvent = new SCTMEvent(received);
               sctmEvents.add(sctmEvent);
               command = SiemensSCTM.NEXT;
               data = SiemensSCTM.SPONTANEOUSBUFFERS;
           }

           return sctmEvents;
        }
        catch(SiemensSCTMException e) {
          throw new IOException("Siemens7ED62, doGetEvents, SiemensSCTMException, "+e.getMessage());
        }
    }

    // METCOM3
    private static final String SYSTEM_RESTART = "01/01";
    private static final String COLDSTART = "01/02";
    private static final String PROGRAMFAULT = "02/01";
    private static final String PARAMETERFAULT = "02/02";
    private static final String DATAFAULT = "02/03";
    private static final String POWERFAILURE = "03/01";
    private static final String OVERFLOWDATA = "05/09";
    private static final String OVERFLOW = "05/";  // inserted myself to have a wildcard to clear & delete events
    private static final String CLEAR_DATA = "06/";  // inserted myself to have a wildcard to clear & delete events
    private static final String PROFILEBUFFERCLEARED = "06/01";
    private static final String EVENTBUFFERCLEARED = "06/0A";
    private static final String DSTBEGIN = "07/03";
    private static final String DSTEND = "07/04";
    private static final String SETTIME = "07/05";
    private static final String HARDWARE_ERROR = "08/"; // inserted myself to have a wildcard to faulty events
    private static final String TASKTABLEERROR = "08/32";
    private static final String TARIFFTABLEERROR = "08/33";
    private static final String INPUTALARMXX = "0A/"; // format 0A/XX (with XX the CS interface)
    private static final String STATUSALARMXX = "0B/"; // format 0B/XX (with XX the input)
    private static final String CONFIGURATIONCHANGE= "0F/"; // inserted myself to have a wildcard to configchange events
    private static final String PARAMETERNEW = "0F/01";

    // FBC
    private static final String MOD1_SYSREM_RESTART = "01/31";
    private static final String MOD1_COLD_START = "01/32";
    private static final String MOD1_PROGRAMFAULT = "02/31";
    private static final String MOD1_PARAMETERFAULT = "02/32";
    private static final String MOD1_DATAFAULT = "02/33";
    private static final String BATTERY_ERROR = "04/01";
    private static final String DATACARD_BAT_ERROR = "04/41";
    private static final String COM1_OVERFLOW51 = "05/3A";
    private static final String DATACARD_OVERFLOW01 = "05/41";
    private static final String DATACARD_OVERFLOW02 = "05/42";
    private static final String DATACARD_OVERFLOW51 = "05/4A";
    private static final String MOD1_CLEARED_01 = "06/31";
    private static final String MOD1_CLEARED_02 = "06/32";
    private static final String MOD1_CLEARED_51 = "06/3A";
    private static final String DATACARD_CLEARED_01 = "06/41";
    private static final String DATACARD_CLEARED_02 = "06/42";
    private static final String DATACARD_CLEARED_51 = "06/4A";
    private static final String CLOCK_SYNC_ERROR = "07/02";
    private static final String MASTER_ERROR = "08/01";
    private static final String CARD_ERROR = "08/11";
    private static final String NO_CARD = "08/12";
    private static final String BAD_CARD = "08/13";
    private static final String WRONG_CARD_TYPE = "08/14";
    private static final String CARD_SIZE = "08/15";
    private static final String MOD1_ERROR = "08/31";
    private static final String DATACARD_ERROR = "08/41";
    private static final String PULSE_ALARMXX = "09/"; // XX pulse
    private static final String OVERFLOW_OUTPUTXX = "0C/";
    private static final String NUMERIC_OVERFLOWXX = "OE/";

    // FCL
    private static final String INPUTALARM100 = "100"; // method isFCLInputAlarm() used
    // range of 16
    private static final String INPUTALARM115 = "115"; // method isFCLInputAlarm() used

    private static final String BATTERYALARM = "130";
    private static final String RAMERROR = "131";
    private static final String EPROMERROR = "132";
    private static final String ESYNALARM = "170";


    // FAF10, FAF20
    private static final String FAF_SYSTEM_RESTART = "#100";
    private static final String FAF_COLDSTART = "#101";
    private static final String FAF_PARAMETER_FAULT = "#102";
    private static final String FAF_PROGRAM_FAULT = "#103";
    private static final String FAF_DATA_FAULT = "#104";
    private static final String FAF_DATA_CLEARED = "#105";
    private static final String FAF_BATTERY_FAULT = "#106";
    private static final String FAF_NUMERIC_OVERFLOW ="#107";
    private static final String COM_COLDSTART = "#111";
    private static final String COM_PROGRAM_FAULT = "#113";
    private static final String COM_DATA_FAULT = "#114";
    private static final String COM_BUFFER01_CLEARED = "#115";
    private static final String COM_BUFFER02_CLEARED = "#116";
    private static final String CARD_COLDSTART = "#121";
    private static final String CARD_PROGRAM_FAULT = "#123";
    private static final String CARD_DATA_FAULT = "#124";
    private static final String CARD_BUFFER01_OVERFLOW = "#125";
    private static final String CARD_BUFFER02_OVERFLOW = "#126";
    private static final String CARD_BUFFER51_OVERFLOW = "#127";
    private static final String CARD_BATTERY_FAULT = "#128";

    private static final String PULSE_ALARM_E00 = "#130"; // method isFAFInputPulseAlarm() used
    // range 24
    private static final String PULSE_ALARM_E23 = "#153"; // method isFAFInputPulseAlarm() used

    private static final String OUTPUT_STORE_A0_OVERFLOW = "#170";
    private static final String OUTPUT_STORE_A1_OVERFLOW = "#171";
    private static final String OUTPUT_STORE_A2_OVERFLOW = "#172";
    private static final String OUTPUT_STORE_A3_OVERFLOW = "#173";
    private static final String OUTPUT_STORE_A4_OVERFLOW = "#174";
    private static final String OUTPUT_STORE_A5_OVERFLOW = "#175";
    private static final String OUTPUT_STORE_A6_OVERFLOW = "#176";
    private static final String OUTPUT_STORE_A7_OVERFLOW = "#177";
    private static final String PRINTER_OUT_OF_PAPER = "#186";
    private static final String PRINTER_FAULT = "#187";
    private static final String COM_MODULE_FAULT = "#192";
    private static final String DATACARD_MODULE_FAULT = "#195";
    private static final String DATACARD_FAULT = "#197";
    private static final String CLOCK_SYN_FAULT = "#162";
    private static final String STATUS_ALARM_ES02 = "#164";
    private static final String STATUS_ALARM_ES03 = "#165";
    private static final String STATUS_ALARM_ES04 = "#166";
    private static final String STATUS_ALARM_ES05 = "#167";
    private static final String STATUS_ALARM_ES06 = "#168";
    private static final String STATUS_ALARM_ES07 = "#169";

    // FAG
    private static final String SYSTEM_RESTART_INPUT_MODULE_M1 = "01/41";
    private static final String COLD_START_INPUT_MODULE_M1 = "01/42";
    private static final String SYSTEM_RESTART_INPUT_MODULE_M2 = "01/51";
    private static final String COLD_START_INPUT_MODULE_M2 = "01/52";
    private static final String SYSTEM_RESTART_INPUT_MODULE_M3 = "01/61";
    private static final String COLD_START_INPUT_MODULE_M3 = "01/62";
    private static final String SYSTEM_RESTART_INPUT_MODULE_M4 = "01/71";
    private static final String COLD_START_INPUT_MODULE_M4 = "01/72";
    private static final String PROGRAMFAULT_INPUT_MODULE_M1 = "02/41";
    private static final String PARAMETERFAULT_INPUT_MODULE_M1 = "02/42";
    private static final String DATAFAULT_INPUT_MODULE_M1 = "02/43";
    private static final String PROGRAMFAULT_INPUT_MODULE_M2 = "02/51";
    private static final String PARAMETERFAULT_INPUT_MODULE_M2 = "02/52";
    private static final String DATAFAULT_INPUT_MODULE_M2 = "02/53";
    private static final String PROGRAMFAULT_INPUT_MODULE_M3 = "02/61";
    private static final String PARAMETERFAULT_INPUT_MODULE_M3 = "02/62";
    private static final String DATAFAULT_INPUT_MODULE_M3 = "02/63";
    private static final String PROGRAMFAULT_INPUT_MODULE_M4 = "02/71";
    private static final String PARAMETERFAULT_INPUT_MODULE_M4 = "02/72";
    private static final String DATAFAULT_INPUT_MODULE_M4 = "02/73";
    private static final String VOLTAGE_DIP = "03/02";
    private static final String BATTERY_FAULT_MV_BUFFER = "04/02";
    private static final String BATTERY_FAULT_DATACARD = "04/40";
    private static final String PP1_DATA_OVERFLOW_DATACARD = "05/11";
    private static final String PP2_DATA_OVERFLOW_DATACARD = "05/12";
    private static final String PP3_DATA_OVERFLOW_DATACARD = "05/13";
    private static final String PP4_DATA_OVERFLOW_DATACARD = "05/14";
    private static final String PP5_DATA_OVERFLOW_DATACARD = "05/15";
    private static final String PP6_DATA_OVERFLOW_DATACARD = "05/16";
    private static final String PS51_DATA_OVERFLOW_DATACARD = "05/1A";
    private static final String PP2_DATA_DELETED_COM_BUFFER1 = "06/02";
    private static final String PP3_DATA_DELETED_COM_BUFFER1 = "06/03";
    private static final String PP4_DATA_DELETED_COM_BUFFER1 = "06/04";
    private static final String PP5_DATA_DELETED_COM_BUFFER1 = "06/05";
    private static final String PP6_DATA_DELETED_COM_BUFFER1 = "06/06";

    private static final String PP3_DATA_DELETED_COM_BUFFER2 = "06/43";
    private static final String PP4_DATA_DELETED_COM_BUFFER2 = "06/44";
    private static final String PP5_DATA_DELETED_COM_BUFFER2 = "06/45";
    private static final String PP6_DATA_DELETED_COM_BUFFER2 = "06/46";

    private static final String PP1_DATA_DELETED_COM_BUFFER3 = "06/51";
    private static final String PP2_DATA_DELETED_COM_BUFFER3 = "06/52";
    private static final String PP3_DATA_DELETED_COM_BUFFER3 = "06/53";
    private static final String PP4_DATA_DELETED_COM_BUFFER3 = "06/54";
    private static final String PP5_DATA_DELETED_COM_BUFFER3 = "06/55";
    private static final String PP6_DATA_DELETED_COM_BUFFER3 = "06/56";

    private static final String PP1_DATA_DELETED_COM_BUFFER4 = "06/61";
    private static final String PP2_DATA_DELETED_COM_BUFFER4 = "06/62";
    private static final String PP3_DATA_DELETED_COM_BUFFER4 = "06/63";
    private static final String PP4_DATA_DELETED_COM_BUFFER4 = "06/64";
    private static final String PP5_DATA_DELETED_COM_BUFFER4 = "06/65";
    private static final String PP6_DATA_DELETED_COM_BUFFER4 = "06/66";

    private static final String BAD_RECEPTION = "07/01";
    private static final String SYSTEM_TIME_EX_CLOCK_DEV_TO_HIGH = "07/06";
    private static final String SYSTEM_TIME_HW_CLOCK_DEV_TO_HIGH = "07/07";
    private static final String NO_RECEPTION = "07/08";

    private static final String DATA_LOSS_DATACARD = "08/16";
    //private static final String FAULT_IN_INPUT_MODULE_M1 = "08/41";
    private static final String NO_CONNECTION_TO_INPUT_MODULE_M1 = "08/42";
    private static final String CONNECTOR_CARD_M1A_WRONG = "08/43";
    private static final String CONNECTOR_CARD_M1B_WRONG = "08/44";

    private static final String FAULT_IN_INPUT_MODULE_M2 = "08/51";
    private static final String NO_CONNECTION_TO_INPUT_MODULE_M2 = "08/52";
    private static final String CONNECTOR_CARD_M2A_WRONG = "08/53";
    private static final String CONNECTOR_CARD_M2B_WRONG = "08/54";

    private static final String FAULT_IN_INPUT_MODULE_M3 = "08/61";
    private static final String NO_CONNECTION_TO_INPUT_MODULE_M3 = "08/62";
    private static final String CONNECTOR_CARD_M3A_WRONG = "08/63";
    private static final String CONNECTOR_CARD_M3B_WRONG = "08/64";

    private static final String FAULT_IN_INPUT_MODULE_M4 = "08/71";
    private static final String NO_CONNECTION_TO_INPUT_MODULE_M4 = "08/72";
    private static final String CONNECTOR_CARD_M4A_WRONG = "08/73";
    private static final String CONNECTOR_CARD_M4B_WRONG = "08/74";

    private static final String MAIN_CHECK_COMPARXX = "0D/";

    private static final String PARAMETER_CHANGE_INPUT_MODULE_M1 = "0F/41";
    private static final String PARAMETER_CHANGE_INPUT_MODULE_M2 = "0F/51";
    private static final String PARAMETER_CHANGE_INPUT_MODULE_M3 = "0F/61";
    private static final String PARAMETER_CHANGE_INPUT_MODULE_M4 = "0F/71";

    private static final String MV_BUFFER_FAULTXX = "14/";
    private static final String METER_EXCHANGEXX = "15/";

    private static final String STATUS_MESSAGE_RESTART = "01/00";
    private static final String STATUS_MESSAGE_POWER_FAILURE = "03/00";
    private static final String STATUS_MESSAGE_TIMESHIFT = "07/00";
    private static final String STATUS_MESSAGE_METERING_VALUE_DIFF = "0D/00";
    private static final String STATUS_MESSAGE_PARAMETER_CHANGE = "0F/00";
    private static final String STATUS_MESSAGE_MANUAL_INPUT = "11/00";
    private static final String STATUS_MESSAGE_WARNING = "12/00";
    private static final String STATUS_MESSAGE_ERROR = "13/00";

    private boolean isFCLInputAlarm(String str) {
        for (int i = 100; i < 115; i++) {
            String inputAlarm = String.valueOf(i);
            if (str.compareTo(inputAlarm) == 0)
                return true;
        }
        return false;
    }

    private boolean isFAFInputPulseAlarm(String str) {
        for (int i = 130; i <= 153; i++) {
            String inputAlarm = "#"+String.valueOf(i);
            if (str.compareTo(inputAlarm) == 0)
                return true;
        }
        return false;
    }

    private void addToProfile(List sctmEvents,ProfileData profileData) {

        Iterator iterator = sctmEvents.iterator();
        while(iterator.hasNext()) {
           SCTMEvent sctmEvent = (SCTMEvent)iterator.next();

           switch(sctmEvent.getType()) {
               case 0xA1: {
                    MeterEvent meterEvent=null;
                    String message = sctmEvent.getSubAddress()+", "+sctmEvent.getAdat()+" -> "+sctmEvent.getEdat();

                    // interprete the metcom3 MTTT3A events as in document of Siemens MTT3A
                    if ((sctmEvent.getAdat().indexOf(SYSTEM_RESTART)>=0) || (sctmEvent.getEdat().indexOf(SYSTEM_RESTART)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));

                    else if ((sctmEvent.getAdat().indexOf(COLDSTART)>=0) || (sctmEvent.getEdat().indexOf(COLDSTART)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.POWERUP,sctmEvent.getType(),message));

                    else if ((sctmEvent.getAdat().indexOf(PROGRAMFAULT)>=0) || (sctmEvent.getEdat().indexOf(PROGRAMFAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PARAMETERFAULT)>=0) || (sctmEvent.getEdat().indexOf(PARAMETERFAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CONFIGURATIONCHANGE,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATAFAULT)>=0) || (sctmEvent.getEdat().indexOf(DATAFAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));



                    else if (sctmEvent.getAdat().indexOf(POWERFAILURE)>=0)
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.POWERUP,sctmEvent.getType(),message+" (POWERUP)"));
                    else if (sctmEvent.getEdat().indexOf(POWERFAILURE)>=0)
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.POWERDOWN,sctmEvent.getType(),message+" (POWERDOWN)"));


                    else if ((sctmEvent.getAdat().indexOf(OVERFLOWDATA)>=0) || (sctmEvent.getEdat().indexOf(OVERFLOWDATA)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.REGISTER_OVERFLOW,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(OVERFLOW)>=0) || (sctmEvent.getEdat().indexOf(OVERFLOW)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.REGISTER_OVERFLOW,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PROFILEBUFFERCLEARED)>=0) || (sctmEvent.getEdat().indexOf(PROFILEBUFFERCLEARED)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(EVENTBUFFERCLEARED)>=0) || (sctmEvent.getEdat().indexOf(EVENTBUFFERCLEARED)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DSTBEGIN)>=0) || (sctmEvent.getEdat().indexOf(DSTBEGIN)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DSTEND)>=0) || (sctmEvent.getEdat().indexOf(DSTEND)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));

                    else if ((sctmEvent.getAdat().indexOf(SETTIME)>=0) || (sctmEvent.getEdat().indexOf(SETTIME)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.SETCLOCK,sctmEvent.getType(),message+" (SETCLOCK)"));

                    else if ((sctmEvent.getAdat().indexOf(TASKTABLEERROR)>=0) || (sctmEvent.getEdat().indexOf(TASKTABLEERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(TARIFFTABLEERROR)>=0) || (sctmEvent.getEdat().indexOf(TARIFFTABLEERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(INPUTALARMXX)>=0) || (sctmEvent.getEdat().indexOf(INPUTALARMXX)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUSALARMXX)>=0) || (sctmEvent.getEdat().indexOf(STATUSALARMXX)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PARAMETERNEW)>=0) || (sctmEvent.getEdat().indexOf(PARAMETERNEW)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CONFIGURATIONCHANGE,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(INPUTALARMXX)>=0) || (sctmEvent.getEdat().indexOf(INPUTALARMXX)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUSALARMXX)>=0) || (sctmEvent.getEdat().indexOf(STATUSALARMXX)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));

                    // FBC
                    else if ((sctmEvent.getAdat().indexOf(MOD1_PROGRAMFAULT)>=0) || (sctmEvent.getEdat().indexOf(MOD1_PROGRAMFAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(MOD1_DATAFAULT)>=0) || (sctmEvent.getEdat().indexOf(MOD1_DATAFAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(BATTERY_ERROR)>=0) || (sctmEvent.getEdat().indexOf(BATTERY_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATACARD_BAT_ERROR)>=0) || (sctmEvent.getEdat().indexOf(DATACARD_BAT_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(MOD1_CLEARED_01)>=0) || (sctmEvent.getEdat().indexOf(MOD1_CLEARED_01)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(MOD1_CLEARED_02)>=0) || (sctmEvent.getEdat().indexOf(MOD1_CLEARED_02)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(MOD1_CLEARED_51)>=0) || (sctmEvent.getEdat().indexOf(MOD1_CLEARED_51)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATACARD_CLEARED_01)>=0) || (sctmEvent.getEdat().indexOf(DATACARD_CLEARED_01)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATACARD_CLEARED_02)>=0) || (sctmEvent.getEdat().indexOf(DATACARD_CLEARED_02)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATACARD_CLEARED_51)>=0) || (sctmEvent.getEdat().indexOf(DATACARD_CLEARED_51)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(CLOCK_SYNC_ERROR)>=0) || (sctmEvent.getEdat().indexOf(CLOCK_SYNC_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(MASTER_ERROR)>=0) || (sctmEvent.getEdat().indexOf(MASTER_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.FATAL_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(CARD_ERROR)>=0) || (sctmEvent.getEdat().indexOf(CARD_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(MOD1_ERROR)>=0) || (sctmEvent.getEdat().indexOf(MOD1_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR ,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATACARD_ERROR)>=0) || (sctmEvent.getEdat().indexOf(DATACARD_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PULSE_ALARMXX)>=0) || (sctmEvent.getEdat().indexOf(PULSE_ALARMXX)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(OVERFLOW_OUTPUTXX)>=0) || (sctmEvent.getEdat().indexOf(OVERFLOW_OUTPUTXX)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.REGISTER_OVERFLOW,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(NUMERIC_OVERFLOWXX)>=0) || (sctmEvent.getEdat().indexOf(NUMERIC_OVERFLOWXX)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.REGISTER_OVERFLOW,sctmEvent.getType(),message));

                    // FCL
                    else if (isFCLInputAlarm(sctmEvent.getAdat()) || isFCLInputAlarm(sctmEvent.getEdat()))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(BATTERYALARM)>=0) || (sctmEvent.getEdat().indexOf(BATTERYALARM)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(ESYNALARM)>=0) || (sctmEvent.getEdat().indexOf(ESYNALARM)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(RAMERROR)>=0) || (sctmEvent.getEdat().indexOf(RAMERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(EPROMERROR)>=0) || (sctmEvent.getEdat().indexOf(EPROMERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.ROM_MEMORY_ERROR,sctmEvent.getType(),message));


                    // FAF
                    else if ((sctmEvent.getAdat().indexOf(FAF_COLDSTART)>=0) || (sctmEvent.getEdat().indexOf(FAF_COLDSTART)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.POWERUP,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(FAF_PARAMETER_FAULT)>=0) || (sctmEvent.getEdat().indexOf(FAF_PARAMETER_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(FAF_PROGRAM_FAULT)>=0) || (sctmEvent.getEdat().indexOf(FAF_PROGRAM_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(FAF_DATA_FAULT)>=0) || (sctmEvent.getEdat().indexOf(FAF_DATA_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(FAF_DATA_CLEARED)>=0) || (sctmEvent.getEdat().indexOf(FAF_DATA_CLEARED)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(FAF_BATTERY_FAULT)>=0) || (sctmEvent.getEdat().indexOf(FAF_BATTERY_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(FAF_NUMERIC_OVERFLOW)>=0) || (sctmEvent.getEdat().indexOf(FAF_NUMERIC_OVERFLOW)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.REGISTER_OVERFLOW,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(COM_PROGRAM_FAULT)>=0) || (sctmEvent.getEdat().indexOf(COM_PROGRAM_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(COM_DATA_FAULT)>=0) || (sctmEvent.getEdat().indexOf(COM_DATA_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(COM_BUFFER01_CLEARED)>=0) || (sctmEvent.getEdat().indexOf(COM_BUFFER01_CLEARED)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(COM_BUFFER02_CLEARED)>=0) || (sctmEvent.getEdat().indexOf(COM_BUFFER02_CLEARED)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(CARD_PROGRAM_FAULT)>=0) || (sctmEvent.getEdat().indexOf(CARD_PROGRAM_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(CARD_DATA_FAULT)>=0) || (sctmEvent.getEdat().indexOf(CARD_DATA_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(CARD_BATTERY_FAULT)>=0) || (sctmEvent.getEdat().indexOf(CARD_BATTERY_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));

                    else if ((isFAFInputPulseAlarm(sctmEvent.getAdat())) || (isFAFInputPulseAlarm(sctmEvent.getEdat())))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));

                    else if ((sctmEvent.getAdat().indexOf(PRINTER_FAULT)>=0) || (sctmEvent.getEdat().indexOf(PRINTER_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(COM_MODULE_FAULT)>=0) || (sctmEvent.getEdat().indexOf(COM_MODULE_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATACARD_MODULE_FAULT)>=0) || (sctmEvent.getEdat().indexOf(DATACARD_MODULE_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATACARD_FAULT)>=0) || (sctmEvent.getEdat().indexOf(DATACARD_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(CLOCK_SYN_FAULT)>=0) || (sctmEvent.getEdat().indexOf(CLOCK_SYN_FAULT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_ALARM_ES02)>=0) || (sctmEvent.getEdat().indexOf(STATUS_ALARM_ES02)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_ALARM_ES03)>=0) || (sctmEvent.getEdat().indexOf(STATUS_ALARM_ES03)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_ALARM_ES04)>=0) || (sctmEvent.getEdat().indexOf(STATUS_ALARM_ES04)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_ALARM_ES05)>=0) || (sctmEvent.getEdat().indexOf(STATUS_ALARM_ES05)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_ALARM_ES06)>=0) || (sctmEvent.getEdat().indexOf(STATUS_ALARM_ES06)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_ALARM_ES07)>=0) || (sctmEvent.getEdat().indexOf(STATUS_ALARM_ES07)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));


                    // FAG
                    else if ((sctmEvent.getAdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M1)>=0) || (sctmEvent.getEdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M1)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M1)>=0) || (sctmEvent.getEdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M1)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATAFAULT_INPUT_MODULE_M1)>=0) || (sctmEvent.getEdat().indexOf(DATAFAULT_INPUT_MODULE_M1)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(VOLTAGE_DIP)>=0) || (sctmEvent.getEdat().indexOf(VOLTAGE_DIP)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(BATTERY_FAULT_MV_BUFFER)>=0) || (sctmEvent.getEdat().indexOf(BATTERY_FAULT_MV_BUFFER)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(BATTERY_FAULT_DATACARD)>=0) || (sctmEvent.getEdat().indexOf(BATTERY_FAULT_DATACARD)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));

                    else if ((sctmEvent.getAdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M2)>=0) || (sctmEvent.getEdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M2)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M2)>=0) || (sctmEvent.getEdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M2)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATAFAULT_INPUT_MODULE_M2)>=0) || (sctmEvent.getEdat().indexOf(DATAFAULT_INPUT_MODULE_M2)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M3)>=0) || (sctmEvent.getEdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M3)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M3)>=0) || (sctmEvent.getEdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M3)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATAFAULT_INPUT_MODULE_M3)>=0) || (sctmEvent.getEdat().indexOf(DATAFAULT_INPUT_MODULE_M3)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M4)>=0) || (sctmEvent.getEdat().indexOf(PROGRAMFAULT_INPUT_MODULE_M4)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.PROGRAM_FLOW_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M4)>=0) || (sctmEvent.getEdat().indexOf(PARAMETERFAULT_INPUT_MODULE_M4)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.RAM_MEMORY_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(DATAFAULT_INPUT_MODULE_M4)>=0) || (sctmEvent.getEdat().indexOf(DATAFAULT_INPUT_MODULE_M4)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.METER_ALARM,sctmEvent.getType(),message));

                    else if ((sctmEvent.getAdat().indexOf(CLEAR_DATA)>=0) || (sctmEvent.getEdat().indexOf(CLEAR_DATA)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CLEAR_DATA,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(HARDWARE_ERROR)>=0) || (sctmEvent.getEdat().indexOf(HARDWARE_ERROR)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.HARDWARE_ERROR,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(CONFIGURATIONCHANGE)>=0) || (sctmEvent.getEdat().indexOf(CONFIGURATIONCHANGE)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CONFIGURATIONCHANGE,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_MESSAGE_POWER_FAILURE)>=0) || (sctmEvent.getEdat().indexOf(STATUS_MESSAGE_POWER_FAILURE)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.POWERUP|MeterEvent.POWERDOWN,sctmEvent.getType(),message));
                    else if ((sctmEvent.getAdat().indexOf(STATUS_MESSAGE_TIMESHIFT)>=0) || (sctmEvent.getEdat().indexOf(STATUS_MESSAGE_TIMESHIFT)>=0))
                        meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.SETCLOCK,sctmEvent.getType(),message));

                    else meterEvent = (new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));


                    // 05300 means clear alarm...
                    if (meterEvent != null) {
                        if (sctmEvent.getSubAddress().compareTo("05300") == 0)  {
//System.out.println("05300 received") ;
                            meterEvent = new MeterEvent(meterEvent.getTime(),meterEvent.OTHER,meterEvent.getProtocolCode(),message);
                        }
                        profileData.addEvent(meterEvent);
                    }

               } break;

               case 0xA2: {
                    String message = sctmEvent.getSubAddress()+", "+sctmEvent.getAdat()+" -> "+sctmEvent.getEdat();
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.OTHER,sctmEvent.getType(),message));
               } break;

               case 0xC1:
               case 0xC2: {
                    String message = sctmEvent.getSubAddress()+", "+sctmEvent.getAdat()+" -> "+sctmEvent.getEdat();
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.CONFIGURATIONCHANGE,sctmEvent.getType(),message));
               } break;

               case 0xA3:
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.POWERDOWN,sctmEvent.getType()));
                    profileData.addEvent(new MeterEvent(sctmEvent.getTo().getDate(timeZone),MeterEvent.POWERUP,sctmEvent.getType()));
                    break;

               case 0xD1:
               case 0xD2:
               case 0xD3:
               case 0xD4:
                    profileData.addEvent(new MeterEvent(sctmEvent.getFrom().getDate(timeZone),MeterEvent.SETCLOCK_BEFORE,sctmEvent.getType()));
                    profileData.addEvent(new MeterEvent(sctmEvent.getTo().getDate(timeZone),MeterEvent.SETCLOCK_AFTER,sctmEvent.getType()));
                    break;

               default:
                    profileData.addEvent(new MeterEvent(new Date(),MeterEvent.OTHER,sctmEvent.getType()));
                    break;

           } // switch(sctmEvent.type)
        }
    } // private void addToProfile(List sctmEvents,ProfileData profileData)

        // FCL


}

