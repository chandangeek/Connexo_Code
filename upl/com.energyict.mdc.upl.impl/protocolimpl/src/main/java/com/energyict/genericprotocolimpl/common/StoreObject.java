package com.energyict.genericprotocolimpl.common;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cpo.Transaction;
import com.energyict.mdw.amr.Register;
import com.energyict.mdw.amr.RegisterReadingStorer;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Phenomenon;
import com.energyict.mdw.shadow.amr.RegisterReadingShadow;
import com.energyict.protocol.MeterData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<StoreObjectItem> getStoreObjectItems() {
        return storeObjectItems;
    }

    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Object doExecute() throws BusinessException, SQLException {
        for (StoreObjectItem storeObjectItem : getStoreObjectItems()) {
            Object key = storeObjectItem.getKey();
            if (key instanceof Device) {
                Device rtu = (Device) key;
                ProfileData profileData = (ProfileData) storeObjectItem.getValue();
                store(rtu, profileData);
            } else if (key instanceof Channel) {
                Channel channel = (Channel) key;
                ProfileData profileData = (ProfileData) storeObjectItem.getValue();
                store(channel, profileData);
            } else if (key instanceof Register) {
                Register rtuRegister = (Register) key;
                RegisterValue registerValue = (RegisterValue) storeObjectItem.getValue();
                store(rtuRegister, registerValue);
            } else if (key instanceof ProfileData) {
                Device rtu = (Device) storeObjectItem.getValue();
                ProfileData profileData = (ProfileData) key;
                store(rtu, profileData);
            } else if (key instanceof MeterReadingData) {
                Device rtu = (Device) storeObjectItem.getValue();
                MeterReadingData meterReadingData = (MeterReadingData) key;
                store(rtu, meterReadingData);
            } else if (key instanceof MeterData) {
                Device rtu = (Device) storeObjectItem.getValue();
                MeterData meterReadingData = (MeterData) key;
                store(meterReadingData, rtu);
            } else {
                getLogger().severe("StoreObject cannot store item! Key/Value combination incorrect: key=" + key.getClass().getName() + ", value=" + storeObjectItem.getValue().getClass().getName());
            }
        }
        return null;
    }

    public void add(MeterReadingData meterReadingData, Device rtu) {
        getStoreObjectItems().add(new StoreObjectItem(meterReadingData, rtu));
    }

    public void add(Device rtu, ProfileData pd) {
        getStoreObjectItems().add(new StoreObjectItem(rtu, pd));
    }

    public void add(Device rtu, List<MeterEvent> meterEvents) {
        final ProfileData eventProfile = new ProfileData();
        eventProfile.setMeterEvents(meterEvents);
        getStoreObjectItems().add(new StoreObjectItem(rtu, eventProfile));
    }

    public void add(Channel channel, ProfileData pd) {
        getStoreObjectItems().add(new StoreObjectItem(channel, pd));
    }

    public void add(Register rtuRegister, RegisterValue registerValue) {
        getStoreObjectItems().add(new StoreObjectItem(rtuRegister, registerValue));
    }

    public void add(ProfileData pd, Device rtu) {
        getStoreObjectItems().add(new StoreObjectItem(pd, rtu));
    }

    public void add(MeterData meterdata, Device rtu){
        getStoreObjectItems().add(new StoreObjectItem(meterdata, rtu));
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

    protected void store(Device rtu, MeterReadingData meterReadingData) throws SQLException, BusinessException {
        Map<Register, Date> lastReadings = new HashMap<Register, Date>();
        Map<Register, Date> lastCheckeds = new HashMap<Register, Date>();
        RegisterReadingStorer storer = new RegisterReadingStorer();
        for (RegisterValue registerValue : meterReadingData.getRegisterValues()) {
            Register rtuRegister = getRtuRegister(rtu, registerValue.getRtuRegisterId());
            if (registerValue.isSupported()) {
                RegisterReadingShadow shadow = new RegisterReadingShadow();
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
        throw new BusinessException("DeviceImpl, getRtuRegister(id), no Register for id " + id);
    }

    protected void store(Device rtu, ProfileData profileData) throws BusinessException, SQLException {
        rtu.store(profileData, false);
    }

    protected void store(MeterData meterData, Device rtu) throws BusinessException, SQLException {
        rtu.store(meterData, false);
    }

    protected void store(Register rtuRegister, RegisterValue registerValue) throws SQLException, BusinessException {
        rtuRegister.store(registerValue);
    }

    protected void store(Channel channel, ProfileData profileData) throws BusinessException, SQLException {
        channel.getRtu().store(profileData, false);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StoreObject{/n");
        for (StoreObjectItem storeObjectItem : getStoreObjectItems()) {
            sb.append(" > ").append(storeObjectItem).append('\n');
        }
        sb.append("}/n");
        return sb.toString();
    }
}
