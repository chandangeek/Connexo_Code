package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amrimpl.RegisterImpl;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.coreimpl.ChannelImpl;
import com.energyict.mdw.coreimpl.DeviceImpl;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

public class StoreObject implements Transaction{

	private HashMap storeObjects;
	
	public StoreObject(){
		this.storeObjects = new HashMap();
	}
	
	public Object doExecute() throws BusinessException, SQLException {
		
		Iterator keyit = storeObjects.entrySet().iterator();
		while(keyit.hasNext()){
			Map.Entry entry = (Map.Entry)keyit.next();
			Object key = entry.getKey();
			if(key instanceof DeviceImpl){
				((Device) key).store((ProfileData) entry.getValue(), false);
			} else if(key instanceof ChannelImpl){
				(((Channel)key).getRtu()).store((ProfileData) entry.getValue(), false);
			} else if(key instanceof RegisterImpl){
				((Register) key).store((RegisterValue) entry.getValue());
			}
		}
		
		return null;
	}
	
	public void add(Object key, Object value){
		storeObjects.put(key, value);
	}

}
