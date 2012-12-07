package com.elster.genericprotocolimpl.dlms.ek280;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.amr.RegisterReadingShadow;
import com.energyict.protocol.*;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * <pre>
 * It is possible to store three different objects:
 * If you want to store:
 * 	- profileData -> use the RTU as the key
 * 	- channelData -> use the Channel as the key
 *  - registerData -> use the Register as the key
 *  Changes:
 *  GNA|09022009| You can use profileData as the key because you can only have the same key once.
 *  If you store the 15min values with the RTU and the daily/monthly values witht the
 * 	RTU then you overwrite the previous value of it. ProfileData will be unique
 * 	JME|Replaced map of storeObjects with a list of StoreObjectItem's. Now duplicate keys can be used,
 * 	without replacing earlier data that already was in the storeObject
 * </pre>
 *
 * @author gna
 */
public class StoreObject implements Transaction {

    private final List<StoreObjectItem> storeObjectItems;
    private Logger logger = null;

    public StoreObject() {
        this.storeObjectItems = new ArrayList<StoreObjectItem>();
    }

    public Object doExecute() throws BusinessException, SQLException {
        for (StoreObjectItem storeObjectItem : storeObjectItems) {
            Object key = storeObjectItem.getKey();
            if (key instanceof Device) {
                ((Device) key).store((ProfileData) storeObjectItem.getValue(), false);
            } else if (key instanceof Channel) {
                (((Channel) key).getRtu()).store((ProfileData) storeObjectItem.getValue(), false);
            } else if (key instanceof Register) {
                ((com.energyict.mdw.amr.Register) key).store((RegisterValue) storeObjectItem.getValue());
            } else if (key instanceof ProfileData) {
                ((Device) storeObjectItem.getValue()).store((ProfileData) key, false);
            } else if (key instanceof MeterReadingData) {
                store(((Device) storeObjectItem.getValue()), (MeterReadingData) key);
            } else {
                getLogger().severe("StoreObject cannot store item! Key/Value combination incorrect: key=" + key.getClass().getName() + ", value=" + storeObjectItem.getValue().getClass().getName());
            }
        }
        return null;
    }

    public void add(MeterReadingData meterReadingData, Device rtu) {
        storeObjectItems.add(new StoreObjectItem(meterReadingData, rtu));
    }

    public void add(Device rtu, ProfileData pd) {
        storeObjectItems.add(new StoreObjectItem(rtu, pd));
    }

    public void add(Channel channel, ProfileData pd) {
        storeObjectItems.add(new StoreObjectItem(channel, pd));
    }

    public void add(Register rtuRegister, RegisterValue registerValue) {
        storeObjectItems.add(new StoreObjectItem(rtuRegister, registerValue));
    }

    public void add(ProfileData pd, Device rtu) {
        storeObjectItems.add(new StoreObjectItem(pd, rtu));
    }

    public void addAll(Map map) {
        if (map != null) {
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                if ((key instanceof Device) && (value instanceof ProfileData)) {
                    add((Device) key, (ProfileData) value);
                } else if ((key instanceof Channel) && (value instanceof ProfileData)) {
                    add((Channel) key, (ProfileData) value);
                } else if ((key instanceof Register) && (value instanceof RegisterValue)) {
                    add((Register) key, (RegisterValue) value);
                } else if ((key instanceof ProfileData) && (value instanceof Device)) {
                    add((ProfileData) key, (Device) value);
                } else if ((key instanceof MeterReadingData) && (value instanceof Device)) {
                    add((MeterReadingData) key, (Device) value);
                } else {
                    getLogger().severe("StoreObject cannot store item! Key/Value combination incorrect: key=" + key.getClass().getName() + ", value=" + value.getClass().getName());
                }
            }
        }
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    private void store(Device rtu, MeterReadingData meterReadingData) throws SQLException, BusinessException {
        Map<Register, Date> lastReadings = new HashMap<Register, Date>();
        Map<Register, Date> lastCheckeds = new HashMap<Register, Date>();
        RegisterReadingStorer storer = new RegisterReadingStorer();
        for (RegisterValue registerValue : (List<RegisterValue>) meterReadingData.getRegisterValues()) {
            Register rtuRegister = getRtuRegister(rtu, registerValue.getRtuRegisterId());
            if (registerValue.isSupported()) {
                RegisterReadingShadow shadow = new RegisterReadingShadow();
                shadow.setToTime(registerValue.getToTime());
                shadow.setFromTime(registerValue.getFromTime());
                shadow.setEventTime(registerValue.getEventTime());
                shadow.setReadTime(registerValue.getReadTime());
                shadow.setText(registerValue.getText());
                if (registerValue.getQuantity() != null) {
                    Phenomenon phenomenon = rtuRegister.getRegisterSpec().getRegisterMapping().getProductSpec().getPhenomenon();
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
        for (Register rtuRegister : lastReadings.keySet()) {
            Date lastChecked = lastCheckeds.get(rtuRegister);
            if (lastChecked != null) {
                lastChecked = new Date(lastChecked.getTime() - 1000);
            }
            rtuRegister.updateLastCheckedAndLastReading(lastChecked, lastReadings.get(rtuRegister));
        }
    }


    private Register getRtuRegister(Device rtu, int id) throws SQLException, BusinessException {
        for (Register rtuRegister : rtu.getRegisters()) {
            if (rtuRegister.getId() == id) {
                return rtuRegister;
            }
        }
        throw new BusinessException("RtuImpl, getRtuRegister(id), no Register for id " + id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StoreObject{/n");
        for (StoreObjectItem storeObjectItem : storeObjectItems) {
            sb.append(" > ").append(storeObjectItem).append('\n');
        }
        sb.append("}/n");
        return sb.toString();
    }
}
