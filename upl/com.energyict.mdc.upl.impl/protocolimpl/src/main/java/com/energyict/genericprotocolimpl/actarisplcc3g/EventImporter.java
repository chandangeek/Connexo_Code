package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.edf.messages.*;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.*;
import com.energyict.protocol.*;
import java.io.*;
import java.sql.SQLException;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.eisimport.core.AbstractStreamImporter;
import java.text.*;
import java.util.*;

// AbstractStreamImporter

public class EventImporter extends AbstractStreamImporter { //AbstractImporter {

    final int DEBUG=1;
    
    protected void importStream() throws BusinessException, SQLException, IOException { 

        File file  = getFileImport().getFile();
        String fileName = file.getName();
        
        if (fileName.startsWith("AL_")) {
            getLogger().warning("Alarm file ("+fileName+") imported, process...");
            byte[] data = new byte[(int)file.length()];
            getStream().read(data);
            if (DEBUG==2) 
                System.out.println( AXDRDecoder.decode(data));
            
            Date date = getEventFileDate(fileName);
            if (DEBUG==1) 
                System.out.println(date);
            Rtu device = findDevice(getConcentratorSerialNumber(fileName));
            AlarmFile alarmFile = new AlarmFile(data, device.getTimeZone());
            Iterator it = alarmFile.toAlarmEntries().iterator();
            while(it.hasNext()) {
                
                AlarmEntry alarmEntry = (AlarmEntry)it.next();
                if (DEBUG==1)
                    System.out.println(alarmEntry);
                
                createEventForDevice(alarmEntry);
                
                if (alarmEntry.isALARM_BEGIN_OF_DISCOVER()) {
                    
                }
                else if (alarmEntry.isALARM_END_OF_DISCOVER()) {
                    
                }
                else if (alarmEntry.isALARM_END_OF_LOAD_PROFILE_REQUEST()) {
                    
                }
                else if (alarmEntry.isALARM_NEW_METER_FOUND()) {
                    sendDiscoverMessage(alarmEntry);                   
                }
                else if (alarmEntry.isALARM_TAMPER()) {
                    
                }
            }
        }
        else {
            getLogger().severe("Unknow file ("+fileName+") imported, no processing takes place...");
        }
        
    } // protected void importStream() throws BusinessException, SQLException, IOException
    
    private void createEventForDevice(AlarmEntry alarmEntry) throws IOException,BusinessException,SQLException {
        Rtu device = findDevice(alarmEntry.getSerialNumber());
        RtuEventShadow rtuEventShadow = new RtuEventShadow();
        rtuEventShadow.setCode(mapMeterEvent(alarmEntry));
        rtuEventShadow.setDate(alarmEntry.getDatetime());
        rtuEventShadow.setDeviceCode(alarmEntry.getId());
        rtuEventShadow.setMessage(alarmEntry.getAlarmDescription());
        rtuEventShadow.setRtuId(device.getId());
        device.addEvent(rtuEventShadow);
    }
    
    private int mapMeterEvent(AlarmEntry alarmEntry) {
        if (alarmEntry.isALARM_BEGIN_OF_DISCOVER()) {
            return MeterEvent.OTHER;
        }
        else if (alarmEntry.isALARM_END_OF_DISCOVER()) {
            return MeterEvent.OTHER;
        }
        else if (alarmEntry.isALARM_END_OF_LOAD_PROFILE_REQUEST()) {
            return MeterEvent.OTHER;
        }
        else if (alarmEntry.isALARM_NEW_METER_FOUND()) {
            return MeterEvent.OTHER;
        }
        else if (alarmEntry.isALARM_TAMPER()) {
            return MeterEvent.METER_ALARM;
        }
        else return MeterEvent.OTHER;
    }
    
    private void sendDiscoverMessage(AlarmEntry alarmEntry) throws IOException,BusinessException,SQLException {
        Rtu device = findDevice(alarmEntry.getSerialNumber());
        MessageDiscoverMeters mr = new MessageDiscoverMeters();
        mr.setOrdinal(0);
        mr.setScriptId(MessageDiscoverMeters.READMETERLIST);
        createMessage( device, mr);
        startReadingNow(device);
    }
    
    private void startReadingNow(Rtu device) throws BusinessException,SQLException {
        List schedulers = device.getCommunicationSchedulers();
        for (Iterator jt = schedulers.iterator(); jt.hasNext();){
            CommunicationScheduler scheduler = (CommunicationScheduler) jt.next();
            if (scheduler.getCommunicationProfile().getSendRtuMessage()){
                    scheduler.startReadingNow();
            }
        }
    }
    
    private void createMessage( Rtu concentrator, MessageContent content) throws BusinessException,SQLException {
        RtuMessageShadow shadow = new RtuMessageShadow(concentrator.getId());
        shadow.setReleaseDate( new Date() );
        shadow.setContents( content.xmlEncode() );
        concentrator.createMessage(shadow);
    }
    
    private Date getEventFileDate(String fileName) throws IOException {
        String[] strs = fileName.split("_");
        if (strs.length != 3) {
            throw new IOException("Error extracting date from filename "+fileName);
        }
        DateFormat format = new java.text.SimpleDateFormat("yyyyMMdd-HHmmss");
        try {
            return format.parse(strs[2]);
        }
        catch(ParseException e) {
            throw new IOException("ParseException error extracting date from filename "+fileName+", "+e.toString());
        }
        
    }
    
    private String getConcentratorSerialNumber(String fileName) throws IOException { 
        String[] strs = fileName.split("_");
        if (strs.length != 3) {
            throw new IOException("Error extracting concentrator serial number from filename "+fileName);
        }
        return strs[1]; 
    }
    
    private Rtu findDevice(String serialNumber) throws IOException {
        RtuFactory rtuFactory = MeteringWarehouse.getCurrent().getRtuFactory();    
        List found = rtuFactory.findBySerialNumber(serialNumber);
        if (found.size()==1) {
            return (Rtu)found.get(0);
        }
        else if (found.size()==0) {
            throw new IOException("No device found for serial number "+serialNumber); 
        }
        else { // if (found.size()>1) {
            throw new IOException("More devices found for serial number "+serialNumber); 
        }
        
    }
    
}
