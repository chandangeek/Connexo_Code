/*
 * HandleConcentratorTransaction.java
 *
 * Created on 18 december 2007, 16:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.ProcessingException;
import com.energyict.cpo.Transaction;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.PLCCMeterListBlocData;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.edf.messages.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 *
 * @author kvds
 */
public class HandleConcentratorTransaction implements Transaction {
    
    HandleConcentrator handleConcentrator;
    List rtuRegisters=null;
    
    /** Creates a new instance of HandleConcentratorTransaction */
    public HandleConcentratorTransaction(HandleConcentrator handleConcentrator) {
        this.handleConcentrator=handleConcentrator;
    }
    
    public Object doExecute() throws BusinessException, SQLException {
        try {
//            readMeterAndStore();
            
            
            
            handleMessages();
        }
        catch(IOException e) {
            throw new ProcessingException("Error reading the concentrator\n"+com.energyict.cbo.Utils.stack2string(e),e);
        }
        
        
        return null;
    }


    
    private boolean isMessage(String message, String content) {
        return content != null && content.indexOf("<" + message + "/>") != -1;
    }
    
    private void handleMessages() throws BusinessException,SQLException,IOException {
       
        List messagePairs = new ArrayList();
                
        Iterator iter = handleConcentrator.getConcentratorDevice().getPendingMessages().iterator();
        
        while (iter.hasNext()) {
            
            RtuMessage msg = (RtuMessage) iter.next();
            String content = msg.getContents();
            messagePairs.add(new MessagePair(msg,MessageContentFactory.createMessageContent(msg.getContents())));
            
        } //  while (iter.hasNext())
        
        processMessagePairs(messagePairs);
        
    } // private void handleMessages(Rtu device) throws BusinessException, SQLException


    
    private void processMessagePairs(List messagePairs) throws BusinessException,SQLException,IOException {
        
        Collections.sort(messagePairs);
        
        Iterator it = messagePairs.iterator();
        
        while(it.hasNext()) {
            MessagePair messagePair = (MessagePair)it.next();
            try {

                MessageContent messageContent = messagePair.getMessageContent();
                if (messageContent instanceof MessageDiscoverMeters) {
                    MessageDiscoverMeters messageDiscoverMeters = (MessageDiscoverMeters)messageContent;
                    if (messageDiscoverMeters.isScriptIdREADMETERLIST())
                        updateMeters();
                    else if (messageDiscoverMeters.isScriptIdRESET())
                        handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCPLCEquipmentList().setAllNew();
                    else if (messageDiscoverMeters.isScriptIdDISCOVER())
                        handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCPLCEquipmentList().discover();
                    else if (messageDiscoverMeters.isScriptIdRESETANDREDISCOVER())
                        handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCPLCEquipmentList().initialDiscover();
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
                    readProfiles(messageReadLoadProfiles);
                }
                else if (messageContent instanceof MessageReadBillingValues) {
                    MessageReadBillingValues messageReadBillingValues = (MessageReadBillingValues)messageContent;
                    readBillingProfiles(messageReadBillingValues);
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
                handleConcentrator.getConcentrator().getLogger().severe(e.toString());
            }
            catch(NoSuchRegisterException e) {
                messagePair.getRtuMessage().setFailed();
                handleConcentrator.getConcentrator().getLogger().severe(e.toString());
            }
            catch(DataAccessResultException e) {
                messagePair.getRtuMessage().setFailed();
                handleConcentrator.getConcentrator().getLogger().severe(e.toString());
            }
//            catch (IOException e) {
//                messagePair.getRtuMessage().setFailed();
//                handleConcentrator.getConcentrator().getLogger().severe(e.toString());
//            }
            
        } // while(it.hasNext())
        
    } // private void processMessagePairs(List messagePairs) throws IOException

    private void updateMeters() throws BusinessException,SQLException,IOException {
        
        List meterList = handleConcentrator.getConcentrator().getAllMeters();
        
        if (meterList.size() == 0) {
            handleConcentrator.getConcentrator().getLogger().warning("HandleConcentratorTransaction, updateMeters, empty meterlist...");
        }
        else {
            Iterator it = meterList.iterator();
            while( it.hasNext() ) {
                findOrCreateMeter((PLCCMeterListBlocData)it.next());
            }
        }        
    }
    
    private MeterReadingData readRegisters(List registers) throws IOException {
        // KV_TO_DO for performance issues we should sort the registers here for oldest billing point first. See code for communicationschedulershadowbuilder in comserver...
        // read registers
        MeterReadingData meterReadingData = new MeterReadingData();
        Iterator it = registers.iterator();
        StringBuffer strBuff = null;
        while(it.hasNext()) {
            RtuRegister register = (RtuRegister) it.next();
            try {
                RegisterValue registerValue = handleConcentrator.getConcentrator().getConcentratorRegister().readRegister(register.getRtuRegisterSpec().getObisCode());
                registerValue.setRtuRegisterId(register.getId());
                meterReadingData.add(registerValue);
            }
            catch(NoSuchRegisterException e) {
                if (strBuff == null)
                    strBuff = new StringBuffer();
                strBuff.append(e.toString()+"\n");
                handleConcentrator.getConcentrator().getLogger().warning(e.toString());
            }
        }
        
        // KV_TO_DO Handle AMR journal???
//        if (strBuff != null) {
//            handleConcentrator.getAMRJournalManager().adjustCompletionCode(AmrJournalEntry.CC_CONFIGURATION);
//            handleConcentrator.getAMRJournalManager().journal(new AmrJournalEntry(AmrJournalEntry.NO_SUCH_REGISTER, "meter "+handleMeter.getMeterInfo().getSerialNumber()+": \n"+strBuff.toString()));
//        }
//        doLogMeterDataCollection(meterReadingData);
        
        return meterReadingData;
        
    } // private MeterReadingData readRegisters(Rtu device) throws IOException    
    
    private List getRtuRegisters() {
        // caching...
        if (rtuRegisters == null)
            rtuRegisters = handleConcentrator.getConcentratorDevice().getRegisters();
        return rtuRegisters;
    }
    
    private RtuRegister findInRtuRegister(ObisCode obisCode) {
        Iterator it = getRtuRegisters().iterator();
        while(it.hasNext()) {
            RtuRegister register = (RtuRegister) it.next();
            if (register.getRegisterMapping().getObisCode().equals(obisCode))
                return register;
        }
        
        handleConcentrator.getConcentrator().getLogger().severe("HandleConcentrator, findInRtuRegister, register with ObisCode "+obisCode+" not defined for the concentrator device "+handleConcentrator.getConcentratorDevice().getSerialNumber());
        return null;
    }    
    
    
    
    
    private void findOrCreateMeter(PLCCMeterListBlocData data) throws BusinessException,SQLException {
        
        String sNr = data.getSerialNumber();
        RtuFactory rtuFactory = MeteringWarehouse.getCurrent().getRtuFactory();
        
        List found = rtuFactory.findBySerialNumber(sNr);
        
        if( found.size() == 1 ) {
            // update reference
            Rtu device = (Rtu)found.get(0);
            if ((device.getGateway() == null) || (device.getGateway().getId() != handleConcentrator.getConcentratorDevice().getId()))
                device.updateGateway(handleConcentrator.getConcentratorDevice());
        }
        
        if( found.size() > 1 ) {
            String msg = "Found multiple devices with serial: \"" + sNr + "\"";
            throw new BusinessException(msg);
        }
    }
    
    private void executeAction(MessageExecuteAction messageExecuteAction) throws IOException {
        ObisCode obisCode = ObisCode.fromString(messageExecuteAction.getObisCode());
        
        if (obisCode.equals(ObisCode.fromString("0.0.98.139.0.255"))) {
            if (messageExecuteAction.getMethodId() == 1) {
                handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCPLCEquipmentList().initialDiscover();
            }
            else if (messageExecuteAction.getMethodId() == 132) {
                handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCPLCEquipmentList().discover();
            }
            else if (messageExecuteAction.getMethodId() == 136) {
                handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCPLCEquipmentList().setAllNew();
            }
            else throw new HandleMessageException("Invalid method ID "+messageExecuteAction.getMethodId()+" for action message to object "+messageExecuteAction.getObisCode());
        } 
        else
           throw new HandleMessageException("Invalid action message to object "+messageExecuteAction.getObisCode());
    }
    
    private void readIndexes(MessageReadIndexes messageReadIndexes) throws IOException, SQLException, BusinessException {
        throw new HandleMessageException("Invalid read indexes message for concentrator device "+handleConcentrator.getConcentratorDevice().getSerialNumber());
    }
    
    private void readLogbook(MessageReadLogBook messageReadLogBook) throws IOException, SQLException, BusinessException {
        throw new HandleMessageException("Invalid read logbook message for concentrator device "+handleConcentrator.getConcentratorDevice().getSerialNumber());
    }
    
    private void readProfiles(MessageReadLoadProfiles messageReadLoadProfiles) throws IOException, SQLException, BusinessException {
        throw new HandleMessageException("Invalid read load profile message for concentrator device "+handleConcentrator.getConcentratorDevice().getSerialNumber());
    }

    private void readBillingProfiles(MessageReadBillingValues messageReadBillingValues) throws IOException, SQLException, BusinessException {
        throw new HandleMessageException("Invalid read billing values message for concentrator device "+handleConcentrator.getConcentratorDevice().getSerialNumber());
    }
    
    private void readRegister(MessageReadRegister messageReadRegister) throws IOException, SQLException, BusinessException {
        List registers = new ArrayList();
        RtuRegister register = findInRtuRegister(ObisCode.fromString(messageReadRegister.getObisCode()));
        if (register != null) {
            registers.add(register);
            MeterReadingData meterReadingData = readRegisters(registers);
            handleConcentrator.getConcentratorDevice().store(meterReadingData);
        }
    }
    
    private void writeRegister(MessageWriteRegister messageWriteRegister) throws IOException {
        ObisCode obisCode = ObisCode.fromString(messageWriteRegister.getObisCode());
        
        if (obisCode.equals(ObisCode.fromString("0.0.25.5.0.255"))) {
            if (messageWriteRegister.getValue() instanceof String)
                throw new HandleMessageException("Write data for object "+messageWriteRegister.getObisCode()+" should not contain a String as data!");                    
            com.energyict.protocolimpl.edf.messages.objects.FtpServerId ftpServerId = (com.energyict.protocolimpl.edf.messages.objects.FtpServerId)messageWriteRegister.getValue();
            handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCFTPServerId().writeFtpServerId(ftpServerId);
        }
        else if (obisCode.equals(ObisCode.fromString("0.1.1.0.0.255"))) {
            if (messageWriteRegister.getValue() instanceof String)
                throw new HandleMessageException("Write data for object "+messageWriteRegister.getObisCode()+" should not contain a String as data!");                    
            com.energyict.protocolimpl.edf.messages.objects.MeterClock meterClock = (com.energyict.protocolimpl.edf.messages.objects.MeterClock)messageWriteRegister.getValue();
            
            if (handleConcentrator.getConcentratorScheduler().getCommunicationProfile().getWriteClock())
                 handleConcentrator.getConcentrator().getPLCCObjectFactory().getPLCCCurrentDateTime().writeMeterClock(meterClock);
        }
        else
           throw new HandleMessageException("Invalid write message to object "+messageWriteRegister.getObisCode()+" for concentrator "+handleConcentrator.getConcentratorDevice().getSerialNumber());
    }
    
}
