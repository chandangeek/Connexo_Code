package com.energyict.genericprotocolimpl.common;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterReadingStorer;
import com.energyict.mdw.amrimpl.RtuRegisterImpl;
import com.energyict.mdw.core.*;
import com.energyict.mdw.coreimpl.ChannelImpl;
import com.energyict.mdw.coreimpl.RtuImpl;
import com.energyict.mdw.shadow.amr.RtuRegisterReadingShadow;
import com.energyict.protocol.*;

import java.sql.SQLException;
import java.util.*;

	/**
	 * <pre>
 	 * It is possible to store three different objects:
	 * If you want to store:
	 * 	- profileData -> use the RTU as the key
	 * 	- channelData -> use the Channel as the key
	 *  - registerData -> use the RtuRegister as the key
	 *  Changes:
	 *  GNA|09022009| You can use profileData as the key because you can only have the same key once. 
	 *  If you store the 15min values with the RTU and the daily/monthly values witht the
	 * 	RTU then you overwrite the previous value of it. ProfileData will be unique
	 * </pre>
	 * 
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
			} else if(key instanceof MeterReadingData){
                store(((Rtu)entry.getValue()), (MeterReadingData)key );
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
	
	public void addAll(Map map){
		storeObjects.putAll(map);
	}
	
	public HashMap getMap(){
		return this.storeObjects;
	}

    private void store(Rtu rtu, MeterReadingData meterReadingData) throws SQLException, BusinessException {
        Map<RtuRegister, Date> lastReadings = new HashMap<RtuRegister, Date>();
        Map<RtuRegister, Date> lastCheckeds = new HashMap<RtuRegister, Date>();
        RtuRegisterReadingStorer storer = new RtuRegisterReadingStorer();
        for (RegisterValue registerValue : (List<RegisterValue>) meterReadingData.getRegisterValues()) {
             RtuRegister rtuRegister = getRtuRegister(rtu, registerValue.getRtuRegisterId());
            if (registerValue.isSupported()) {
                RtuRegisterReadingShadow shadow = new RtuRegisterReadingShadow();
                shadow.setToTime(registerValue.getToTime());
                shadow.setFromTime(registerValue.getFromTime());
                shadow.setEventTime(registerValue.getEventTime());
                shadow.setReadTime(registerValue.getReadTime());
                shadow.setText(registerValue.getText());
                if (registerValue.getQuantity() != null) {
                    Phenomenon phenomenon = rtuRegister.getRtuRegisterSpec().getRegisterMapping().getProductSpec().getPhenomenon();
                    Quantity reading = registerValue.getQuantity();
                    try {
                        registerValue.setQuantity(reading.convertTo(phenomenon.getUnit(), true));
                    } catch (ArithmeticException ex) {
                        throw new BusinessException(ex);
                    }
                    shadow.setValue(registerValue.getQuantity().getAmount());
                }

                storer.add(rtuRegister, shadow);

                Date lastReading = lastReadings.get(rtuRegister);
                Date lastChecked = lastCheckeds.get(rtuRegister);
                Date toTime = registerValue.getToTime();
                if (lastReading == null || toTime.after(lastReading)) {
                    lastReadings.put(rtuRegister, toTime);
                }
                if (lastChecked == null || toTime.before(lastChecked)) {
                    lastCheckeds.put(rtuRegister, toTime);
                }
            }
        }
        MeteringWarehouse.getCurrent().execute(storer);
        for (RtuRegister rtuRegister : lastReadings.keySet()) {
            Date lastChecked = lastCheckeds.get(rtuRegister);
            if (lastChecked != null) {
                lastChecked = new Date(lastChecked.getTime() - 1000);
            }
            rtuRegister.updateLastCheckedAndLastReading(lastChecked, lastReadings.get(rtuRegister));
        }
    }


    private RtuRegister getRtuRegister(Rtu rtu, int id) throws SQLException, BusinessException {
        for (RtuRegister rtuRegister : rtu.getRegisters()) {
            if (rtuRegister.getId() == id) {
                return rtuRegister;
            }
        }
        throw new BusinessException("RtuImpl, getRtuRegister(id), no RtuRegister for id " + id);
    }
}
