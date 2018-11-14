package com.energyict.smartmeterprotocolimpl.nta.esmr50.common.events;


import com.energyict.dlms.DataContainer;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eventhandling.PowerFailureLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.DSMR40EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.FraudDetectionLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.MbusEventLog;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.eventhandling.StandardEventLog;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50Properties;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.ESMR50Protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Deprecated
public class ESMR50EventProfile extends DSMR40EventProfile {
    public static final String MBUS_EVENT_LOG = "0.x.99.98.3.255";

    ESMR50Protocol  protocol;

    public ESMR50EventProfile(AbstractSmartNtaProtocol protocol) {
        super(protocol);
        this.protocol = (ESMR50Protocol) protocol;
    }

    private Calendar convertDate(Date date) throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        if (date == null) {
            date = ProtocolUtils.getClearLastMonthDate(getTimeZone());
        }
        calendar.setTime(date);

        return calendar;
    }

    @Override
    public List<MeterEvent> getEvents(Date fromDate) throws IOException {
        List<MeterEvent> eventList = new ArrayList<MeterEvent>();
        Logger  logger = protocol.getLogger();

        Calendar fromCal = convertDate(fromDate);

        logger.log(Level.INFO, "Reading EVENTS from meter with serialnumber " + protocol.getMeterSerialNumber() + " from " + fromDate.toString());

        readStandardEventLog(eventList, logger, fromCal);

        readPowerFailureLog(eventList, logger, fromCal);

        readFraudDetectionLog(eventList, logger, fromCal);

        readCommunicationSessionEventLog(eventList, logger, fromCal);

        //inherited from DSMR4.0
//        readVoltageQualityEventLog(eventList, fromCal); todo add function back?

        checkFrameCounterEvents(eventList, logger);

        return eventList;
    }

    private void checkFrameCounterEvents(List<MeterEvent> eventList, Logger logger) {
        SecurityContext securityContext = protocol.getDlmsSession().getAso().getSecurityContext();

        generateFrameCounterLimitEvent(securityContext.getFrameCounter(), "Frame Counter", 900, eventList, logger);
        //todo Create isResponseFrameCounterInitialized method?
        if (securityContext.getResponseFrameCounter() != 0) {
            generateFrameCounterLimitEvent(securityContext.getResponseFrameCounter(),"Response Frame Counter", 901, eventList, logger);
        } else {
            logger.info("Response frame counter not initialized.");
        }

    }

    private void generateFrameCounterLimitEvent(long frameCounter, String name, int eventId, List<MeterEvent> eventList, Logger logger) {
        try {
            int frameCounterLimit = ((ESMR50Properties)protocol.getProperties()).getFrameCounterLimit();

            if (frameCounterLimit==0){
                logger.info("Frame counter threshold not configured. FYI the current "+name+" is "+frameCounter);
            } else if (frameCounter>frameCounterLimit) {
                logger.info(name+": " + frameCounter + " is above the threshold ("+frameCounterLimit+") - will create an event");
                MeterEvent frameCounterEvent = new MeterEvent(new Date(), 0, eventId, name+" above threshold: " + frameCounter);
                eventList.add(frameCounterEvent);
            } else {
                logger.info(name + " below configured threshold: "+frameCounter+" < "+frameCounterLimit);
            }
        }catch (Exception e) {
            logger.log(Level.WARNING, "Error getting  "+name+e.getMessage(), e);
        }
    }


    public List<MeterEvent> getSlaveEvents(String slaveSerialNumber, Date lastLogbookDate) throws IOException {
        ObisCode    workingObisCode;
        Logger  logger = protocol.getLogger();
        Calendar fromCal = convertDate(lastLogbookDate);

        List<MeterEvent> eventList = new ArrayList<MeterEvent>();

        logger.info("Getting the events for slave device: "+slaveSerialNumber);

        for (DeviceMapping mbusDevice : this.protocol.getMeterTopology().getMbusMeterMap()){
            if (slaveSerialNumber.equals(mbusDevice.getSerialNumber())) {
                try {
                    int mBusChannel = mbusDevice.getPhysicalAddress();
                    workingObisCode = ObisCode.fromString(MBUS_EVENT_LOG.replace("x", "" + mBusChannel));
                    logger.info(" - reading M-Bus channel " + mBusChannel + " event log from : " + workingObisCode.toString());
                    DataContainer dcMbusEventLogChannel = getCosemObjectFactory().getProfileGeneric(workingObisCode, true).getBuffer(fromCal, getToCalendar());
                    MbusEventLog esmr50MbusEventLog = new ESMR50MbusEventLog(dcMbusEventLogChannel, this.protocol.getDateTimeDeviationType());
                    eventList.addAll(esmr50MbusEventLog.getMeterEvents());
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
        }


        for (MeterEvent event : eventList){
//            event.overrideRtuSerialNumber(slaveSerialNumber); todo Create method? or replace
        }

        return eventList;
    }



    private void readCommunicationSessionEventLog(List<MeterEvent> eventList, Logger logger, Calendar fromCal) {
        ObisCode readCommunicationSessionEventLogObis;
        try {
            readCommunicationSessionEventLogObis = getMeterConfig().getControlLogObject().getObisCode();// todo is Control log the same as Communication log?
            logger.info(" - reading communication session event log: "+readCommunicationSessionEventLogObis.toString());
            DataContainer dcCommunicationSessionEventLog = getCosemObjectFactory().getProfileGeneric(readCommunicationSessionEventLogObis, true).getBuffer(fromCal, getToCalendar());
            ESMR50CommunicationSessionLog mbusCommunicationSession = new ESMR50CommunicationSessionLog(dcCommunicationSessionEventLog, this.protocol.getDateTimeDeviationType());
            eventList.addAll(mbusCommunicationSession.getMeterEvents());
        } catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void readFraudDetectionLog(List<MeterEvent> eventList, Logger logger, Calendar fromCal) {
        ObisCode fraudDetectionLogObis;
        try{
            fraudDetectionLogObis = getMeterConfig().getFraudDetectionLogObject().getObisCode();
            logger.info(" - reading fraud detection log: "+fraudDetectionLogObis.toString());
            DataContainer dcFraudDetection = getCosemObjectFactory().getProfileGeneric(fraudDetectionLogObis, true).getBuffer(fromCal, getToCalendar());
            FraudDetectionLog fraudDetectionEvents = new ESMR50FraudDetectionLog(dcFraudDetection, this.protocol.getDateTimeDeviationType());
            eventList.addAll(fraudDetectionEvents.getMeterEvents());
        } catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void readPowerFailureLog(List<MeterEvent> eventList, Logger logger, Calendar fromCal) {
        ObisCode powerFailureLogObis;
        try {
            powerFailureLogObis = getMeterConfig().getPowerFailureLogObject().getObisCode();
            logger.info(" - reading power failure log: "+powerFailureLogObis.toString());
            DataContainer dcPowerFailure = getCosemObjectFactory().getProfileGeneric(powerFailureLogObis, true).getBuffer(fromCal, getToCalendar());
            PowerFailureLog powerFailure = new PowerFailureLog(dcPowerFailure, this.protocol.getDateTimeDeviationType());
            eventList.addAll(powerFailure.getMeterEvents());
        } catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void readStandardEventLog(List<MeterEvent> eventList, Logger logger, Calendar fromCal) {
        ObisCode standardEventLogObis;
        try{
            standardEventLogObis = getMeterConfig().getEventLogObject().getObisCode(); // todo is Standard log the same as Eventlog?
            logger.info(" - reading standard event log: "+ standardEventLogObis.toString());
            DataContainer dcEvent = getCosemObjectFactory().getProfileGeneric(standardEventLogObis, true).getBuffer(fromCal, getToCalendar());
            StandardEventLog standardEvents = new ESMR50StandardEventLog(dcEvent, this.protocol.getDateTimeDeviationType());
            eventList.addAll(standardEvents.getMeterEvents());
        } catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

}
