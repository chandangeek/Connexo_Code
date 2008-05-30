/*
 * TrimeranProfile.java
 *
 * Created on 27 juni 2006, 9:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.axis.utils.ByteArray;

import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edf.trimarancje.core.DemandData;
import com.energyict.protocolimpl.edf.trimarancje.core.DemandValues;

/**
 *
 * @author Koen
 */
public class TrimaranProfile {
    
    Trimaran trimaran;
    
    private int pointer = 0;
    private DemandData[] demandData = {null, null, null, null,
    									null, null, null, null,
    									null, null, null, null,
    									null, null, null, null};
    private ProfileData profileData = null;
    
    /** Creates a new instance of TrimeranProfile */
    public TrimaranProfile(Trimaran trimaran) {
        this.trimaran=trimaran;
    }
    
    
    public ProfileData getProfileData() throws IOException {

    	if( profileData == null){
    		profileData = new ProfileData();
    		while(true){
    			if(getDemandData() == null)
    				setDemandData(trimaran.getDataFactory().getDemandData(pointer));
    			if (pointer == 1){
    				if(Arrays.equals(getDemandData(0).getData(), getDemandData(1).getData())){
    					break;          
    				}
    			}
    			incrementPointer();
    			if (pointer >= 16)
    				break;
    		}
    		if(pointer >= 16){
    			
    			profileData.setChannelInfos(getDemandData(0).getChannelInfos(1));
    			List allIntervals = new ArrayList();
    			for(int i = 0; i < pointer; i++){
    				allIntervals.add(getDemandData(i).getIntervalDatas());
    			}
    			profileData.setIntervalDatas(allIntervals);
    		}
    		else{
    			profileData.setChannelInfos(getDemandData(0).getChannelInfos());
    			profileData.setIntervalDatas(getDemandData(0).getIntervalDatas());
    		}
    	}
        
        return profileData;
    }
    
    protected void incrementPointer() {
		pointer++;
	}


	private DemandData getDemandData(int p){
    	return this.demandData[p];
    }
    
    protected DemandData getDemandData(){
    	return this.demandData[pointer];
    }
    
    protected void setDemandData(DemandData demandData){
    	this.demandData[pointer] = demandData;
    }
    
    public int getProfileInterval() throws IOException{
    	if(getDemandData() == null)
    		setDemandData(trimaran.getDataFactory().getDemandData());
    	return getDemandData().getProfileInterval();
    }


	/**
	 * @return the pointer
	 */
	protected int getPointer() {
		return pointer;
	}


	/**
	 * @param pointer the pointer to set
	 */
	protected void setPointer(int pointer) {
		this.pointer = pointer;
	}
    
}
