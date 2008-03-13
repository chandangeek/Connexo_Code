package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.cbo.*;
import com.energyict.edf.messages.*;
import com.energyict.edf.messages.objects.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.PLCCMeterLoadProfileEnergy;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.mdw.amr.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocol.MeterReadingData;
import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Transaction;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.PLCCMeterListBlocData;
import com.energyict.mdw.core.*;
import com.energyict.dlms.cosem.DataAccessResultException;

public class HandleMeterTransaction implements Transaction {
    
    private HandleMeter handleMeter;
    Rtu device = null; 
    List rtuRegisters=null;
    
    HandleMeterTransaction(HandleMeter handleMeter) {
        this.handleMeter = handleMeter;
    }
    
    public Object doExecute() throws BusinessException, SQLException {
        device = findOrCreateMeter(handleMeter.getMeterInfo());
        handleMeter.getConcentrator().setCurrentSelectedDevice(device);
        
//        if( device.getGateway() == null ) 
//            device.updateGateway( handleMeter.getConcentratorDevice() );
        
        try {

            if (handleMeter.getConcentratorScheduler().getCommunicationProfile().getWriteClock() ||
                handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadDemandValues() ||    
                handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadMeterEvents() ||    
                handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadMeterReadings() ||
                (handleMeter.getConcentratorScheduler().getCommunicationProfile().getSendRtuMessage()&&(device.getPendingMessages().size()>0))) {    
                
                handleMeter.getConcentrator().selectMeter(handleMeter.getMeterInfo());
                
                handleMeter.getConcentrator().getLogger().info("***********************************************************************************************************");
                handleMeter.getConcentrator().getLogger().warning("Access meter with serialnumber "+device.getSerialNumber()+"");
                readMeterAndStore();
                verifyAndSetTime();
                handleMessages();
            }
        }
        catch(IOException e) {
            throw new BusinessException("Error reading the meter with serial "+handleMeter.getMeterInfo().getSerialNumber()+"\n"+com.energyict.cbo.Utils.stack2string(e),e);
        }
        
        
        return null;
    }

    private void verifyAndSetTime() throws IOException {
    
        /* KV_TO_DO Don't worry about clock sets over interval boundaries for the moment */
        
        Date cTime = handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterCurrentDateTime().getDate();
        Date now = new Date();
        
        handleMeter.getConcentrator().getLogger().info("Metertime "+cTime+", systemtime "+now); 
        
        long sDiff = ( now.getTime() - cTime.getTime() ) / 1000;
        long sAbsDiff = Math.abs( sDiff );
        
        
        if (handleMeter.getConcentratorScheduler().getCommunicationProfile().getWriteClock()) {
            handleMeter.getConcentrator().getLogger().info( "Difference between metertime and systemtime is " 
                         + sDiff * 1000 + " ms");
            
            long max = handleMeter.getConcentratorScheduler().getCommunicationProfile().getMaximumClockDifference();
            long min = handleMeter.getConcentratorScheduler().getCommunicationProfile().getMinimumClockDifference();

            if( ( sAbsDiff < max ) && ( sAbsDiff > min ) ) { 
                handleMeter.getConcentrator().getLogger().severe("Adjust meter "+handleMeter.getMeterInfo().getSerialNumber()+" time to system time");
                handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterCurrentDateTime().setDate(new Date());
            }
            else if (( sAbsDiff > min ) && (max>=1000000)) {  // tricky, to force timeset!
                handleMeter.getConcentrator().getLogger().severe("Adjust meter "+handleMeter.getMeterInfo().getSerialNumber()+" time to system time (forced timeset when maxdiff > 1000000)");
                handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterCurrentDateTime().setDate(new Date());            
            }
        }
        else {
            handleMeter.getConcentrator().getLogger().info( "Difference between metertime and systemtime is " 
                         + sDiff * 1000 + " ms (timeset is disabled!)");
            
        }
    }    
    
    private ProfileData readProfile(boolean intervalData, boolean logBook) throws IOException {
        return readProfile(intervalData,logBook,null,null,null,null);
    }
    private ProfileData readProfile(boolean intervalData, boolean logBook, Date fromLoadProfile, Date toLoadProfile, Date fromLogbook, Date toLogbook) throws IOException {
        
        toLoadProfile = toLoadProfile==null?new Date():toLoadProfile;
        toLogbook = toLogbook==null?new Date():toLogbook;
        
        // KV_TO_DO
        // remove workaround if meterbug is solved... but keep check for after
        if (fromLoadProfile==null) {
            if ((device.getLastReading()==null) || (device.getLastReading().after(toLoadProfile)))
                fromLoadProfile = new Date(toLoadProfile.getTime()-3600000);
            else
                fromLoadProfile = new Date(device.getLastReading().getTime()-3600000);
        }
        
        // KV_TO_DO
        // remove workaround if meterbug is solved... but keep check for after
        if (fromLogbook==null) {
            if ((device.getLastLogbook()==null) || (device.getLastLogbook().after(toLogbook)))
                fromLogbook = new Date(toLogbook.getTime()-3600000);
            else
                fromLogbook = new Date(device.getLastLogbook().getTime()-3600000);
        }
            
        handleMeter.getLogger().info("Retrieve profiledata from "+fromLoadProfile+" to "+toLoadProfile+" and logbook from "+fromLogbook+" to "+toLogbook);
        
        ProfileData profileData = handleMeter.getConcentrator().getConcentratorProfile().getProfileData(fromLoadProfile,toLoadProfile,fromLogbook,toLogbook, intervalData, logBook);
        doLogMeterDataCollection(profileData);
        return profileData;
    }
    
    
    private List readBillingRegisters(Date fromLoadProfile, Date toLoadProfile) throws IOException {
        
        toLoadProfile = toLoadProfile==null?new Date():toLoadProfile;
        
        // KV_TO_DO
        // remove workaround if meterbug is solved... but keep check for after
        if (fromLoadProfile==null) {
            if ((device.getLastReading()==null) || (device.getLastReading().after(toLoadProfile)))
                fromLoadProfile = new Date(toLoadProfile.getTime()-3600000);
            else
                fromLoadProfile = new Date(device.getLastReading().getTime()-3600000);
        }
            
        handleMeter.getLogger().info("Retrieve daily billing profiledata from "+fromLoadProfile+" to "+toLoadProfile);
        List registerValues = new ArrayList();
        PLCCMeterDailyEnergyValueProfile o = handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterDailyEnergyValueProfile(fromLoadProfile,toLoadProfile);
        List dailyBillingentries = o.getDailyBillingEntries();

        for (int fField=0; fField<dailyBillingentries.size();fField++) {
            
           DailyBillingEntry dbe = (DailyBillingEntry)dailyBillingentries.get(fField);
           handleMeter.getLogger().finer(dbe.toString());
           
           for (int eField=0; eField<dbe.getValues().length;eField++) {
               ObisCode obisCode = ObisCode.fromString("1.0.1.8."+(eField+1)+".VZ");    
//               registerValues.add(new RegisterValue(obisCode,new Quantity(dbe.getValues()[eField],Unit.get("Wh")),null,dbe.getCalendar().getTime()));
               
               // construct readTime and add seconds to avoid primary key violation
               Calendar readCalendar = Calendar.getInstance();
               readCalendar.set( Calendar.MILLISECOND, 0);
               readCalendar.add(Calendar.SECOND, fField);
               registerValues.add(new RegisterValue(obisCode,new Quantity(dbe.getValues()[eField],Unit.get("Wh")), null, null, dbe.getCalendar().getTime(), readCalendar.getTime() ));
               
           }
        }
        
        return registerValues;
    }    
    
    
    
    private MeterReadingData readRegisters(List registers, boolean onDemandRead) throws IOException {
        // KV_TO_DO for performance issues we should sort the registers here for oldest billing point first. See code for communicationschedulershadowbuilder in comserver...
        // read registers
        MeterReadingData meterReadingData = new MeterReadingData();
        Iterator it = registers.iterator();
        StringBuffer strBuff = null;
        while(it.hasNext()) {
            RtuRegister register = (RtuRegister) it.next();
            try {
                RegisterValue registerValue = handleMeter.getConcentrator().getConcentratorRegister().readRegister(register.getRtuRegisterSpec().getObisCode());
                registerValue.setRtuRegisterId(register.getId());
                meterReadingData.add(registerValue);
            }
            catch(NoSuchRegisterException e) {
                if (strBuff == null)
                    strBuff = new StringBuffer();
                strBuff.append(e.toString()+"\n");
                handleMeter.getConcentrator().getLogger().warning(e.toString());
                if (onDemandRead)
                    throw e;
            }
        }
        
        if (!onDemandRead && (strBuff != null)) {
            handleMeter.getAMRJournalManager().adjustCompletionCode(AmrJournalEntry.CC_CONFIGURATION);
            handleMeter.getAMRJournalManager().journal(new AmrJournalEntry(AmrJournalEntry.NO_SUCH_REGISTER, "meter "+handleMeter.getMeterInfo().getSerialNumber()+": \n"+strBuff.toString()));
        }
        
        doLogMeterDataCollection(meterReadingData);
        return meterReadingData;
        
    } // private MeterReadingData readRegisters(Rtu device) throws IOException
    
    private List getRtuRegisters() {
        // caching...
        if (rtuRegisters == null)
            rtuRegisters = device.getRegisters();
        return rtuRegisters;
    }
    
    private RtuRegister findInRtuRegister(ObisCode obisCode) {
        Iterator it = getRtuRegisters().iterator();
        while(it.hasNext()) {
            RtuRegister register = (RtuRegister) it.next();
            if (register.getRegisterMapping().getObisCode().equals(obisCode))
                return register;
        }
        
        handleMeter.getConcentrator().getLogger().severe("HandleMeter, findInRtuRegister, register with ObisCode "+obisCode+" not defined for the device "+handleMeter.getMeterInfo().getSerialNumber());
        return null;
    } 
    
    private Rtu findOrCreateMeter(PLCCMeterListBlocData data) throws BusinessException {
        
        String sNr = data.getSerialNumber();
        RtuFactory rtuFactory = MeteringWarehouse.getCurrent().getRtuFactory();
        
        List found = rtuFactory.findBySerialNumber(sNr);
        
        if( found.size() < 1 ) {
            String msg = "No device found with serial: \"" + sNr + "\"";
            throw new BusinessException(msg);
        }
        
        if( found.size() > 1 ) {
            String msg = "Found multiple devices with serial: \"" + sNr + "\"";
            throw new BusinessException(msg);
        }
        
        return (Rtu)found.get(0);
        
    }
    
    private boolean isMessage(String message, String content) {
        return content != null && content.indexOf("<" + message + "/>") != -1;
    }
    
    private void handleMessages() throws BusinessException,SQLException,IOException {
       
        List messagePairs = new ArrayList();
                
        Iterator iter = device.getPendingMessages().iterator();
        
        while (iter.hasNext()) {
            
            RtuMessage msg = (RtuMessage) iter.next();
            String content = msg.getContents();
            
            try {
                if (isMessage(RtuMessageConstant.CONNECT_LOAD, content)) {
                    doConnect();
                    msg.confirm();
                }
                else if (isMessage(RtuMessageConstant.DISCONNECT_LOAD, content)) {
                    doDisconnect();
                    msg.confirm();
                }
                else if (isMessage(RtuMessageConstant.READ_ON_DEMAND, content)) {
                    readMeterAndStore();
                    msg.confirm();
                }
                else {
                    messagePairs.add(new MessagePair(msg,MessageContentFactory.createMessageContent(msg.getContents())));
                }
            } 
            catch(DataAccessResultException e) {
                msg.setFailed();
                handleMeter.getConcentrator().getLogger().severe(e.toString());
            }
//            catch (IOException e) {
//                msg.setFailed();
//                handleMeter.getConcentrator().getLogger().severe(e.toString());
//            }
            
        } //  while (iter.hasNext())
        
        processMessagePairs(messagePairs);
        
    } // private void handleMessages(Rtu device) throws BusinessException, SQLException


    
    private void processMessagePairs(List messagePairs) throws BusinessException,SQLException,IOException {
        
        Collections.sort(messagePairs);
        
        Iterator it = messagePairs.iterator();
        
        while(it.hasNext()) {
            MessagePair messagePair = (MessagePair)it.next();
            handleMeter.getConcentrator().getLogger().info(messagePair.toString());
            try {

                MessageContent messageContent = messagePair.getMessageContent();
                if (messageContent instanceof MessageDiscoverMeters) {
                    MessageDiscoverMeters messageDiscoverMeters = (MessageDiscoverMeters)messageContent;
                    discoverMeters(messageDiscoverMeters);
                }
                else if (messageContent instanceof MessageExecuteAction) {
                    MessageExecuteAction messageExecuteAction = (MessageExecuteAction)messageContent;
                    executeAction(messageExecuteAction);
                }
                else if (messageContent instanceof MessageReadIndexes) {
                    MessageReadIndexes messageReadIndexes = (MessageReadIndexes)messageContent;
                    readIndexes(messageReadIndexes);
                }
                else if (messageContent instanceof MessageReadLogBook) {
                    MessageReadLogBook messageReadLogBook = (MessageReadLogBook)messageContent;
                    readLogbook(messageReadLogBook);
                }
                else if (messageContent instanceof MessageReadLoadProfiles) {
                    MessageReadLoadProfiles messageReadLoadProfiles = (MessageReadLoadProfiles)messageContent;
                    readProfile(messageReadLoadProfiles);
                }
                else if (messageContent instanceof MessageReadBillingValues) {
                    MessageReadBillingValues messageReadBillingValues = (MessageReadBillingValues)messageContent;
                    readBillingRegisters(messageReadBillingValues);
                }
                else if (messageContent instanceof MessageReadRegister) {
                    MessageReadRegister messageReadRegister = (MessageReadRegister)messageContent;
                    readRegister(messageReadRegister);
                }
                else if (messageContent instanceof MessageWriteRegister) {
                    MessageWriteRegister messageWriteRegister = (MessageWriteRegister)messageContent;
                    writeRegister(messageWriteRegister);
                }

                messagePair.getRtuMessage().confirm();
            } 
            catch(HandleMessageException e) {
                messagePair.getRtuMessage().setFailed();
                handleMeter.getConcentrator().getLogger().severe(e.toString());
            }
            catch(NoSuchRegisterException e) {
                messagePair.getRtuMessage().setFailed();
                handleMeter.getConcentrator().getLogger().severe(e.toString());
            }
            catch(DataAccessResultException e) {
                messagePair.getRtuMessage().setFailed();
                handleMeter.getConcentrator().getLogger().severe(e.toString());
            }
//            catch (IOException e) {
//                messagePair.getRtuMessage().setFailed();
//                handleMeter.getConcentrator().getLogger().severe(e.toString());
//            }
            
        } // while(it.hasNext())
        
    } // private void processMessagePairs(List messagePairs) throws IOException
    
    private void discoverMeters(MessageDiscoverMeters messageDiscoverMeters) throws IOException {
        throw new HandleMessageException("Invalid discover meters message for device "+handleMeter.getMeterInfo().getSerialNumber());
    }
    private void executeAction(MessageExecuteAction messageExecuteAction) throws IOException {
        ObisCode obisCode = ObisCode.fromString(messageExecuteAction.getObisCode());
        
        // sync clock broadscast
        if (obisCode.equals(ObisCode.fromString("0.0.1.0.0.255"))) {
            if (messageExecuteAction.getMethodId() == 129) {
                handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterCurrentDateTime().syncTime();
            }
            else throw new HandleMessageException("Invalid method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode());
        }
        
        // moving peak
        else if (obisCode.equals(ObisCode.fromString("0.0.10.0.125.255"))) {
            if ((messageExecuteAction.getMethodId() == 1) || (messageExecuteAction.getMethodId() == 0xFC40)) {
                if (messageExecuteAction.getMethodData() instanceof ComplexCosemObject)
                    throw new HandleMessageException("Method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode()+" should not contain a ComplexCosemObject as data!");                    
                String messageData = (String)messageExecuteAction.getMethodData();
                try {
                    int scriptId = Integer.parseInt(messageData);
                    handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterMovingPeak().execute(scriptId);
                }
                catch(NumberFormatException e) {
                    throw new HandleMessageException("Invalid data "+messageData+" for method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode());
                }
            }
            else if (messageExecuteAction.getMethodId() == 130) {
                if (messageExecuteAction.getMethodData() instanceof ComplexCosemObject)
                    throw new HandleMessageException("Method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode()+" should not contain a ComplexCosemObject as data!");                    
                String messageData = (String)messageExecuteAction.getMethodData();
                try {
                    int scriptId = Integer.parseInt(messageData);
                    handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterMovingPeak().executeBroadcast(scriptId);
                }
                catch(NumberFormatException e) {
                    throw new HandleMessageException("Invalid data "+messageData+" for method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode());
                }
            }
            else throw new HandleMessageException("Invalid method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode());
        }
        
        // breaker unit
        else if (obisCode.equals(ObisCode.fromString("0.0.128.30.22.255"))) {
            if (messageExecuteAction.getMethodId() == 129) {
                if (messageExecuteAction.getMethodData() instanceof ComplexCosemObject)
                    throw new HandleMessageException("Method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode()+" should not contain a ComplexCosemObject as data!");                    
                String messageData = (String)messageExecuteAction.getMethodData();
                try {
                    int data = Integer.parseInt(messageData);
                    handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterContactorState().controlBreaker(data);
                }
                catch(NumberFormatException e) {
                    throw new HandleMessageException("Invalid data "+messageData+" for method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode());
                }
            }
            else throw new HandleMessageException("Invalid method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode());
        }
        else
           throw new HandleMessageException("Invalid action message to object "+messageExecuteAction.getObisCode());   
    }
    
    private void readIndexes(MessageReadIndexes messageReadIndexes) throws IOException, SQLException, BusinessException {
        List registers = new ArrayList();
        for (int e=0;e<=6;e++) {
            RtuRegister register = findInRtuRegister(ObisCode.fromString("1.0.1.8."+e+".255"));
            if (register != null) registers.add(register);
        }
        
        MeterReadingData meterReadingData = readRegisters(registers, true);
        device.store(meterReadingData);
    }
    
    
    private void readLogbook(MessageReadLogBook messageReadLogBook) throws IOException, SQLException, BusinessException {
        ProfileData profileData = readProfile(false,true, null,null, messageReadLogBook.getFrom(), messageReadLogBook.getTo());
        device.store(profileData);
    }
    
    private void readProfile(MessageReadLoadProfiles messageReadLoadProfiles) throws IOException, SQLException, BusinessException {
        ProfileData profileData = readProfile(true,false,messageReadLoadProfiles.getFrom(), messageReadLoadProfiles.getTo(), null,null);
        device.store(profileData);
    }
    
    private void readBillingRegisters(MessageReadBillingValues messageReadBillingValues) throws IOException, SQLException, BusinessException {
        MeterReadingData meterReadingData = new MeterReadingData();
        List registerValues = readBillingRegisters(messageReadBillingValues.getFrom(), messageReadBillingValues.getTo());
        for (int i=0;i<registerValues.size();i++) {
            
            RegisterValue registerValue = (RegisterValue)registerValues.get(i);
            handleMeter.getLogger().finer(registerValue.toString());
            RtuRegister register = findInRtuRegister(registerValue.getObisCode());
            if (register != null) {
                registerValue.setRtuRegisterId(register.getId());
                meterReadingData.add(registerValue);
            }
        }
        device.store(meterReadingData);        
    }
    
    private void readRegister(MessageReadRegister messageReadRegister) throws IOException, SQLException, BusinessException {
        List registers = new ArrayList();
        RtuRegister register = findInRtuRegister(ObisCode.fromString(messageReadRegister.getObisCode()));
        if (register != null) {
            registers.add(register);
            MeterReadingData meterReadingData = readRegisters(registers,true);
            device.store(meterReadingData);
        }
    }
    
    private void writeRegister(MessageWriteRegister messageWriteRegister) throws IOException {
        ObisCode obisCode = ObisCode.fromString(messageWriteRegister.getObisCode());
        
        // meter current date time
        if (obisCode.equals(ObisCode.fromString("0.0.1.0.0.255"))) {
            com.energyict.edf.messages.objects.MeterClock meterClock;
            if (messageWriteRegister.getValue() instanceof String) {
                Calendar cal = ProtocolUtils.getCalendar(handleMeter.getConcentrator().getTimeZone());
                
                meterClock = new com.energyict.edf.messages.objects.MeterClock(cal, handleMeter.getConcentrator().getTimeZone().inDaylightTime(cal.getTime()));
            }
            else {
                meterClock = (com.energyict.edf.messages.objects.MeterClock)messageWriteRegister.getValue();
            }
            
            if (handleMeter.getConcentratorScheduler().getCommunicationProfile().getWriteClock())
                 handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterCurrentDateTime().writeMeterClock(meterClock);
        }
        // activity calendar
        else if (obisCode.equals(ObisCode.fromString("0.0.13.0.0.255"))) {
            if (messageWriteRegister.getValue() instanceof String)
                throw new IOException("Write data for object "+messageWriteRegister.getObisCode()+" should not contain a String as data!");                    
            com.energyict.edf.messages.objects.ActivityCalendar activityCalendar = (com.energyict.edf.messages.objects.ActivityCalendar)messageWriteRegister.getValue();
            handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterActivityCalendar().writeActivityCalendar(activityCalendar);
        }
        // demand management
        else if (obisCode.equals(ObisCode.fromString("0.0.16.0.1.255"))) {
            if (messageWriteRegister.getValue() instanceof String)
                throw new HandleMessageException("Write data for object "+messageWriteRegister.getObisCode()+" should not contain a String as data!");                    
            com.energyict.edf.messages.objects.DemandManagement demandManagement = (com.energyict.edf.messages.objects.DemandManagement)messageWriteRegister.getValue();
            handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterDemandManagement().writeDemandManagement(demandManagement);
        }
        // capture period load profile
        else if (obisCode.equals(ObisCode.fromString("1.0.99.1.0.255"))) {
            if (messageWriteRegister.getValue() instanceof ComplexCosemObject)
                throw new HandleMessageException("Write data for object "+messageWriteRegister.getObisCode()+" should not contain a ComplexCosemObject as data!");                    
            String messageData = (String)messageWriteRegister.getValue();
            try {
                int capturePeriod = Integer.parseInt(messageData);
                handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterLoadProfileEnergy().writeCapturePeriod(capturePeriod);
            }
            catch(NumberFormatException e) {
                throw new HandleMessageException("Invalid write data "+messageData+" to object "+messageWriteRegister.getObisCode());
            }
        }
        // contactor state
        else if (obisCode.equals(ObisCode.fromString("0.0.128.30.22.255"))) {
            if (messageWriteRegister.getValue() instanceof ComplexCosemObject)
                throw new HandleMessageException("Write data for object "+messageWriteRegister.getObisCode()+" should not contain a ComplexCosemObject as data!");                    
            String messageData = (String)messageWriteRegister.getValue();
            try {
                int state = Integer.parseInt(messageData);
                handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterContactorState().writeState(state);
            }
            catch(NumberFormatException e) {
                throw new HandleMessageException("Invalid write data "+messageData+" to object "+messageWriteRegister.getObisCode());
            }
        }
        // TIC configuration
        else if (obisCode.equals(ObisCode.fromString("0.0.96.3.2.255"))) {
            if (messageWriteRegister.getValue() instanceof ComplexCosemObject)
                throw new HandleMessageException("Write data for object "+messageWriteRegister.getObisCode()+" should not contain a ComplexCosemObject as data!");                    
            String messageData = (String)messageWriteRegister.getValue();
            try {
                int mode = Integer.parseInt(messageData);
                handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterTICConfiguration().writeMode(mode);
            }
            catch(NumberFormatException e) {
                throw new HandleMessageException("Invalid write data "+messageData+" to object "+messageWriteRegister.getObisCode());
            }
        }
        else
            throw new HandleMessageException("Invalid write message to object "+messageWriteRegister.getObisCode()+" for meter "+handleMeter.getMeterInfo().getSerialNumber());
    }
    
    
    private void doConnect() throws IOException {
        handleMeter.getConcentrator().getLogger().warning("Close breaker of meter "+device.getSerialNumber()+"...");
        handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterContactorState().writeState(2);
    }
    
    private void doDisconnect() throws IOException {
        handleMeter.getConcentrator().getLogger().warning("Open breaker of meter "+device.getSerialNumber()+"...");
        handleMeter.getConcentrator().getPLCCObjectFactory().getPLCCMeterContactorState().writeState(0);
    }
    
    private void readMeterAndStore() throws IOException,SQLException,BusinessException {
        if (handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadDemandValues() || handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadMeterEvents()) {
            ProfileData profileData = readProfile(handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadDemandValues(),handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadMeterEvents());
            device.store(profileData);
        }
        if (handleMeter.getConcentratorScheduler().getCommunicationProfile().getReadMeterReadings()) {
            MeterReadingData meterReadingData = readRegisters(getRtuRegisters(),false);
            device.store(meterReadingData);
        }
    }    


    private void doLogMeterDataCollection(ProfileData profileData) {
        if (profileData==null) {
            handleMeter.getLogger().info("ProfileData is null");
            return;
        }
        int i,iNROfChannels=profileData.getNumberOfChannels();
        int t,iNROfIntervals=profileData.getNumberOfIntervals();
        int z,iNROfEvents=profileData.getNumberOfEvents();
        handleMeter.getLogger().info("Channels: " + iNROfChannels);
        handleMeter.getLogger().info("Intervals per channel: " + iNROfIntervals);
        for (t=0;t<iNROfIntervals;t++) {
            handleMeter.getLogger().finer(" Interval "+t+"\tendtime = "+profileData.getIntervalData(t).getEndTime());
            handleMeter.getLogger().finer("Channel\tvalue\tstatus\tunit\trtu channel");
            for (i=0;i<iNROfChannels;i++) {
                handleMeter.getLogger().finer(
                        i+"\t"+profileData.getIntervalData(t).get(i)+"\t"+profileData.getIntervalData(t).getEiStatusTranslation(i)+"\t"+profileData.getChannel(i).getUnit()+"\t"+profileData.getChannel(i).getChannelId());
            }
        }
        
        handleMeter.getLogger().info("Events in profiledata: " +iNROfEvents);
        handleMeter.getLogger().finer("Event\tEICode\t\tProtocolCode\t\ttime");
        handleMeter.getLogger().finer("\t(descr)\t(hex)\t(dec)\t(hex)\ttime");
        for (z=0;z<iNROfEvents;z++) {
            handleMeter.getLogger().finer(z+"\t"+profileData.getEvent(z).toString()+"\t0x"+Integer.toHexString(profileData.getEvent(z).getEiCode())+"\t"+profileData.getEvent(z).getProtocolCode()+"\t0x"+Integer.toHexString(profileData.getEvent(z).getProtocolCode())+"\t"+profileData.getEvent(z).getTime());
        }
        
    } // private void doLogMeterDataCollection(ProfileData profileData)  throws ProtocolReaderException
    
    private void doLogMeterDataCollection(MeterReadingData meterReadingData) {
        if (meterReadingData==null) {
            handleMeter.getLogger().info("meterReadingData is null, probably meterreadings not supported by meter.");
            return;
        } else {
            List registerValues = meterReadingData.getRegisterValues();
            Iterator it = registerValues.iterator();
            while(it.hasNext()) {
                RegisterValue each = (RegisterValue)it.next();
                handleMeter.getLogger().info(each.toString());
            }
        }
    } // private void doLogMeterDataCollection(List readingsData)  throws ProtocolReaderException    
    
} // public class HandleMeterTransaction implements Transaction
