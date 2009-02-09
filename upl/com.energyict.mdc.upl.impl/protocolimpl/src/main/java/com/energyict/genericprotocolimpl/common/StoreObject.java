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

	/**
 	 * It is possible to store three different objects:
	 * If you want to store:
	 * 	- profileData -> use the RTU as the key
	 * 	- channelData -> use the Channel as the key
	 *  - registerData -> use the RtuRegister as the key
	 *  Changes:
	 *  GNA|09022009| You can use profileData as the key because you can only have the same key once. If you store the 15min values with the RTU and the daily/monthly values witht the
	 * 	RTU then you overwrite the previous value of it. ProfileData will be unique
	 * @author gna
	 *
	 */
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
			} else if(key instanceof ProfileData){
				((Rtu)entry.getValue()).store((ProfileData)key, false);
			}
		}
		
		return null;
	}
	
	/**
	 * @param key
	 * @param value
	 */
	public void add(Object key, Object value){
		storeObjects.put(key, value);
	}
	
	public void add(Rtu rtu, ProfileData pd){
		storeObjects.put(rtu, pd);
	}
	
	public void add(Channel channel, ProfileData pd){
		storeObjects.put(channel, pd);
	}
	
	public void add(RtuRegisterImpl registerImpl, RegisterValue registerValue){
		storeObjects.put(registerImpl, registerValue);
	}
	
	public void add(ProfileData pd, Rtu rtu){
		storeObjects.put(pd, rtu);
	}
}
