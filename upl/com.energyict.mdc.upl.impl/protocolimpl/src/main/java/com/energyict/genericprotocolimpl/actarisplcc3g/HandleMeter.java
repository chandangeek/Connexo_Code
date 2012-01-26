/*
 * HandleMeter.java
 *
 * Created on 13 december 2007, 14:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.PLCCMeterListBlocData;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.mdw.core.*;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kvds
 */
public class HandleMeter {
    
    private AMRJournalManager aMRJournalManager=null;
    private Concentrator concentrator;
    private Rtu concentratorDevice;
    private PLCCMeterListBlocData meterInfo;
    private CommunicationScheduler concentratorScheduler;
    private Logger logger;
    private long connectTime=0;
    
    final int SUCCEEDED=0;
    final int FAILED=1;
    int state;
    
    /** Creates a new instance of HandleMeter */
    public HandleMeter(Concentrator concentrator, Rtu concentratorDevice, PLCCMeterListBlocData meterInfo, CommunicationScheduler concentratorScheduler, Logger logger) {
        
        this.setConcentrator(concentrator);
        this.setConcentratorDevice(concentratorDevice);
        this.setMeterInfo(meterInfo);
        this.setConcentratorScheduler(concentratorScheduler);
        this.setLogger(logger);
        state = SUCCEEDED;
        
        setAMRJournalManager(new AMRJournalManager(concentratorDevice, concentratorScheduler));
    }
    
    public void handleMeterTransaction() {
       try { 
           setConnectTime(System.currentTimeMillis());
            // KV_TO_DO should we do reading and uipdating in one same transaction?
            // read the meter and update the database
            Environment.getDefault().execute(
                    new HandleMeterTransaction( this ) );
            if (getAMRJournalManager().getJournal().size()>0) {
                adjustConnectionInfo();
                getAMRJournalManager().updateLastCommunication();
            }
       }
       catch(BusinessException e) {
            getLogger().log( Level.SEVERE, e.getMessage(), e );
            e.printStackTrace();
            state = FAILED;
            getAMRJournalManager().setCompletionErrorString("meter "+meterInfo.getSerialNumber()+", "+e.toString());
            getAMRJournalManager().adjustCompletionCode(AmrJournalEntry.CC_IOERROR, e.toString());                
            adjustConnectionInfo();
            try {
                getAMRJournalManager().updateRetrials();
            }
            catch(SQLException ex) {
                getLogger().log( Level.SEVERE, ex.getMessage(), ex );
                ex.printStackTrace();
                Environment.getDefault().closeConnection();
            }
            catch(BusinessException ex) {
                getLogger().log( Level.SEVERE, ex.getMessage(), ex );
                ex.printStackTrace();
            }
       }
       catch(SQLException e) {
            getLogger().log( Level.SEVERE, e.getMessage(), e );
            e.printStackTrace();
            Environment.getDefault().closeConnection();
            
       }
            
    }

    public boolean isFailed() {
        return state == FAILED;
    }
    
    private void adjustConnectionInfo() {
        if (getConnectTime() != 0) getAMRJournalManager().journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME,(System.currentTimeMillis() - getConnectTime())/1000));
        getAMRJournalManager().setAmrJournalCompletionCode();
    }
    
    public AMRJournalManager getAMRJournalManager() {
        return aMRJournalManager;
    }

    private void setAMRJournalManager(AMRJournalManager aMRJournalManager) {
        this.aMRJournalManager = aMRJournalManager;
    }

    public Concentrator getConcentrator() {
        return concentrator;
    }

    private void setConcentrator(Concentrator concentrator) {
        this.concentrator = concentrator;
    }

    public Rtu getConcentratorDevice() {
        return concentratorDevice;
    }

    private void setConcentratorDevice(Rtu concentratorDevice) {
        this.concentratorDevice = concentratorDevice;
    }

    public PLCCMeterListBlocData getMeterInfo() {
        return meterInfo;
    }

    private void setMeterInfo(PLCCMeterListBlocData meterInfo) {
        this.meterInfo = meterInfo;
    }

    public CommunicationScheduler getConcentratorScheduler() {
        return concentratorScheduler;
    }

    private void setConcentratorScheduler(CommunicationScheduler concentratorScheduler) {
        this.concentratorScheduler = concentratorScheduler;
    }

    public Logger getLogger() {
        return logger;
    }

    private void setLogger(Logger logger) {
        this.logger = logger;
    }

    private long getConnectTime() {
        return connectTime;
    }

    private void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }
    
}
