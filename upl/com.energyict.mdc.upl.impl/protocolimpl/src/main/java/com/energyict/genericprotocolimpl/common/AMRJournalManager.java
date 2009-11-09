/*
 * AMRJournalManager.java
 *
 * Created on 13 december 2007, 13:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;

/**
 *
 * @author kvds
 */
public class AMRJournalManager {
    
    static final String NO_ERROR="no error";
    
    private int completionCode=AmrJournalEntry.CC_OK;
    private String completionErrorString=NO_ERROR;
    private List journal = new ArrayList();
    CommunicationScheduler communicationScheduler;
    Rtu device;
    String errorMessage="";
    
    /** Creates a new instance of AMRJournalManager */
    public AMRJournalManager(Rtu device,CommunicationScheduler communicationScheduler) {
        this.communicationScheduler=communicationScheduler;
        this.device=device;
        
    }
    // ***************************** AmrJournalEntry stuff *******************************
    // Only change the completion code if it was in the initial OK state
    // So, only the first completion code will be kept.
    public void adjustCompletionCode(int cc,String reason) {
        adjustCompletionCode(cc);
        this.setCompletionErrorString(reason);
    }
    
    public void adjustCompletionCode(int cc) {
        if (this.completionCode == AmrJournalEntry.CC_OK) {
			this.completionCode=cc;
		}       
    }
    
    public int getCompletionCode() {
    	return completionCode;
    }   

    public List getJournal() {
        return journal;
    }

    public void setJournal(List journal) {
        this.journal = journal;
    }
    
    public void journal(AmrJournalEntry entry) {
        journal.add(entry);
    }
    
    public java.lang.String getErrorMessage() {
        return errorMessage;
    }    
    
    public void setErrorMessage(java.lang.String errorMessage) {
        this.errorMessage=errorMessage;
    }    
    
    public void updateLastCommunication() throws SQLException, BusinessException {
        if (communicationScheduler != null) {
            communicationScheduler.journal(getJournal());
            communicationScheduler.logSuccess(new java.util.Date());
        }
    }
	    
    public void updateRetrials() throws SQLException, BusinessException {
        if (communicationScheduler != null) {
            communicationScheduler.journal(getJournal());
            communicationScheduler.logFailure(Calendar.getInstance(device.getDeviceTimeZone()).getTime(),getErrorMessage());
        }
    }    
    
    public void setAmrJournalCompletionCode() {
        if  (getCompletionErrorString().compareTo(NO_ERROR) != 0) {
            journal(new AmrJournalEntry(AmrJournalEntry.DETAIL,getCompletionErrorString()));
        }
        journal(new AmrJournalEntry(getCompletionCode()));
    }

    public String getCompletionErrorString() {
        return completionErrorString;
    }

    public void setCompletionErrorString(String completionErrorString) {
        this.completionErrorString = completionErrorString;
    }
    
}
