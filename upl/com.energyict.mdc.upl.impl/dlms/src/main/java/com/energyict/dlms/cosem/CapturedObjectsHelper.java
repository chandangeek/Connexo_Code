package com.energyict.dlms.cosem;

import java.util.*;

import com.energyict.dlms.DLMSCOSEMGlobals;

public class CapturedObjectsHelper implements DLMSCOSEMGlobals {

	List<CapturedObject> capturedObjects;

	// lazy
	int nrOfChannels=-1;
	
	public CapturedObjectsHelper(List<CapturedObject> capturedObjects) {
		this.capturedObjects=capturedObjects;
	}
	
	
	public int getNrOfchannels() {
		
		if (nrOfChannels == -1) {
			Iterator<CapturedObject> it = capturedObjects.iterator();
			while(it.hasNext()) {
				CapturedObject co = it.next();
				
		          // Changed KV 22052003 to read also gas puls channels! 
		          if ((co.getLogicalName().getA()  != 0) && //== LN_A_ELECTRICITY_RELATED_OBJECTS) && 
		              (co.getLogicalName().getB() >= 0) && // was 1 (KV 06032007) 
		              (co.getLogicalName().getB() <= 64) && 
		              ((co.getClassId() == ICID_REGISTER) || (co.getClassId() == ICID_DEMAND_REGISTER))) {
		        	  nrOfChannels++;
		          }
		          // Changed GN 29022008 to add the extended register for the Iskra MBus meter
		          else if( ((co.getLogicalName().getA() == 0)||(co.getLogicalName().getA()) == 7) && 
		        		  (co.getLogicalName().getB() == 1) &&
		        		  (co.getLogicalName().getC() == (byte)0x80) &&
		        		  (co.getLogicalName().getD() == 50) &&
		        		  (co.getLogicalName().getE() >= 0) && (co.getLogicalName().getE() <= 3) &&
		        		  (co.getClassId() == ICID_EXTENDED_REGISTER)){
		        	  nrOfChannels++;
		          }				
				
			}
		}
		return nrOfChannels;
		
	} // public int getNrOfchannels()
	
	
}
