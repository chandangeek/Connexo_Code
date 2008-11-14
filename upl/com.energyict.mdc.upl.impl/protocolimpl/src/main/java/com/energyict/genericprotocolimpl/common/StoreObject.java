package com.energyict.genericprotocolimpl.common;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amrimpl.RtuRegisterImpl;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.coreimpl.ChannelImpl;
import com.energyict.mdw.coreimpl.RtuImpl;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

public class StoreObject implements Transaction {
	private HashMap storeObjects;
	
	public StoreObject(){
		this.storeObjects = new HashMap();
	}
	
	public Object doExecute() throws BusinessException, SQLException {
		
		Iterator keyit = storeObjects.entrySet().iterator();
		while(keyit.hasNext()){
			Map.Entry entry = (Map.Entry)keyit.next();
			Object key = entry.getKey();
			if(key instanceof RtuImpl){
				((Rtu) key).store((ProfileData) entry.getValue(), false);
			} else if(key instanceof ChannelImpl){
				(((Channel)key).getRtu()).store((ProfileData) entry.getValue(), false);
			} else if(key instanceof RtuRegisterImpl){
				((RtuRegister) key).store((RegisterValue) entry.getValue());
			}
		}
		
		return null;
	}
	
	/**
	 * It is possible to store three different objects:
	 * If you want to store:
	 * 	- profileData -> use the RTU as the key
	 * 	- channelData -> use the Channel as the key
	 *  - registerData -> use the RtuRegister as the key
	 * @param key
	 * @param value
	 */
	public void add(Object key, Object value){
		storeObjects.put(key, value);
	}
}
