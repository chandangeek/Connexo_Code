/**
 * 
 */
package com.energyict.genericprotocolimpl.actarisace4000;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationScheduler;

/**
 * @author gna
 *
 * This class fills in the journal for the UPD schedule
 * Currently no push schedule is defined in EIServer so we have to do it this was
 * Class can be deleted if push scheduling is added to the framework
 * 
 */
public class AMRLogging {
	
	private List<AmrJournalEntry> journal;
	private ActarisACE4000 aace;

	/**
	 * @param actarisACE4000 
	 * 
	 */
	public AMRLogging(ActarisACE4000 actarisACE4000) {
		journal = new ArrayList<AmrJournalEntry>();
		journal.add(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, System.currentTimeMillis()/1000));
		this.aace = actarisACE4000;
	}
	
	public void journal(AmrJournalEntry entry){
		journal.add(entry);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ActarisACE4000 aace = new ActarisACE4000();
		AMRLogging amrl = new AMRLogging(aace);
		
		CommunicationScheduler cs = aace.mw().getCommunicationSchedulerFactory().find(5387);
		
		aace.setMasterMeter(cs.getRtu());
		try {
			amrl.addSuccessFullLogging();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void addSuccessFullLogging() throws SQLException, BusinessException {
		try {
			journal(new AmrJournalEntry(AmrJournalEntry.CC_OK));
			List csList = getAace().getMeter().getCommunicationSchedulers();
			Iterator it = csList.iterator();
			while(it.hasNext()){
				CommunicationScheduler cs = (CommunicationScheduler)it.next();
				if( !cs.getActive() ){
					System.out.println(cs);
					cs.startCommunication();
					cs.journal(journal);
					cs.logSuccess(new Date());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException("Could not add to AMR journal", e);
		}
		
//		CommunicationScheduler commScheduler = 
	}

	protected ActarisACE4000 getAace() {
		return aace;
	}

	protected void setAace(ActarisACE4000 aace) {
		this.aace = aace;
	}

	public void addFailureLogging(StringBuilder errorString) throws SQLException, BusinessException {
		try {
			journal(new AmrJournalEntry(AmrJournalEntry.CC_IOERROR, "IOError - " + errorString));
			List csList = getAace().getMeter().getCommunicationSchedulers();
			Iterator it = csList.iterator();
			while(it.hasNext()){
				CommunicationScheduler cs = (CommunicationScheduler)it.next();
				if( !cs.getActive() ){
					System.out.println(cs);
					cs.startCommunication();
					cs.journal(journal);
					cs.logSuccess(new Date());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		} catch (BusinessException e) {
			e.printStackTrace();
			throw new BusinessException("Could not add to AMR journal", e);
		}
	}

}
