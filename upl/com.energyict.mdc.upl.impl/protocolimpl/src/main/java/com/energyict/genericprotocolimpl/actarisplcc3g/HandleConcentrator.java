/*
 * HandleConcentrator.java
 *
 * Created on 18 december 2007, 16:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.cpo.*;
import com.energyict.edf.messages.*;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.mdw.amr.*;
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
import java.util.logging.*;

/**
 *
 * @author kvds
 */
public class HandleConcentrator {
    
    private AMRJournalManager aMRJournalManager=null;
    private Concentrator concentrator;
    private Rtu concentratorDevice;
    private CommunicationScheduler concentratorScheduler;
    private Logger logger;
    private long connectTime=0;
    
    final int SUCCEEDED=0;
    final int FAILED=1;
    int state;
    
    /** Creates a new instance of HandleMeter */
    public HandleConcentrator(Concentrator concentrator, Rtu concentratorDevice, CommunicationScheduler concentratorScheduler, Logger logger) {
        
        this.setConcentrator(concentrator);
        this.setConcentratorDevice(concentratorDevice);
        this.setConcentratorScheduler(concentratorScheduler);
        this.setLogger(logger);
        state = SUCCEEDED;
        
        //setAMRJournalManager(new AMRJournalManager(concentratorDevice, concentratorScheduler));
    }
    
    public void verifyAndSetTime() throws IOException {
    
        /* Don't worry about clock sets over interval boundaries */
        
        Date cTime = getConcentrator().getPLCCObjectFactory().getPLCCCurrentDateTime().getDateTime().getTime();
        Date now = new Date();
        
        getConcentrator().getLogger().info("Concentrator "+cTime+", systemtime "+now); 
        
        long sDiff = ( now.getTime() - cTime.getTime() ) / 1000;
        long sAbsDiff = Math.abs( sDiff ); 
        
        if (getConcentratorScheduler().getCommunicationProfile().getWriteClock()) {
            getConcentrator().getLogger().info( "Difference between concentratortime and systemtime is " 
                         + sDiff * 1000 + " ms");
            long max = getConcentratorScheduler().getCommunicationProfile().getMaximumClockDifference();
            long min = getConcentratorScheduler().getCommunicationProfile().getMinimumClockDifference();

            if( ( sAbsDiff < max ) && ( sAbsDiff > min ) ) { 
                getLogger().severe("Adjust concentrator time to system time");
                getConcentrator().getPLCCObjectFactory().getPLCCCurrentDateTime().setDateTime();
            }
            else if (( sAbsDiff > min ) && (max>=1000000)) {  // tricky, to force timeset!
                getLogger().severe("Adjust concentrator time to system time (forced timeset when maxdiff > 1000000)");
                getConcentrator().getPLCCObjectFactory().getPLCCCurrentDateTime().setDateTime();     
            }            
        }
        else {
            getConcentrator().getLogger().info( "Difference between concentratortime and systemtime is " 
                         + sDiff * 1000 + " ms (timeset is disabled!)");
        }
    }        
    
    public void handleConcentratorTransaction() {
       try { 
            // KV_TO_DO should we do reading and uipdating in one same transaction?
            // read the meter and update the database
            Environment.getDefault().execute(
                    new HandleConcentratorTransaction( this ) ); 
//            if (getAMRJournalManager().getJournal().size()>0) {
//                adjustConnectionInfo();
//                getAMRJournalManager().updateLastCommunication();
//            }
       }
       catch(BusinessException e) {
            getLogger().log( Level.SEVERE, e.getMessage(), e );
            e.printStackTrace();
            state = FAILED;
//            getAMRJournalManager().setCompletionErrorString("meter "+meterInfo.getSerialNumber()+", "+e.toString());
//            getAMRJournalManager().adjustCompletionCode(AmrJournalEntry.CC_IOERROR, e.toString());                
//            adjustConnectionInfo();
//            try {
//                getAMRJournalManager().updateRetrials();
//            }
//            catch(SQLException ex) {
//                getLogger().log( Level.SEVERE, ex.getMessage(), ex );
//                ex.printStackTrace();
//                Environment.getDefault().closeConnection();
//            }
//            catch(BusinessException ex) {
//                getLogger().log( Level.SEVERE, ex.getMessage(), ex );
//                ex.printStackTrace();
//            }
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
    
    public Concentrator getConcentrator() {
        return concentrator;
    }

    private void setConcentrator(Concentrator concentrator) {
        this.concentrator = concentrator;
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

    public Rtu getConcentratorDevice() {
        return concentratorDevice;
    }

    private void setConcentratorDevice(Rtu concentratorDevice) {
        this.concentratorDevice = concentratorDevice;
    }
    
    
}
