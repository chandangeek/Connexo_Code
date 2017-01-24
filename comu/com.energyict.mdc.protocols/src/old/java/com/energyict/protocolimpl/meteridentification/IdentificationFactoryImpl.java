/*
 * IEC1107IdFactory.java
 *
 * Created on 15 april 2005, 11:44
 */

package com.energyict.protocolimpl.meteridentification;

import com.energyict.mdc.protocol.api.inbound.IdentificationFactory;
import com.energyict.mdc.protocol.api.inbound.MeterId;
import com.energyict.mdc.protocol.api.inbound.MeterType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author  Koen
 */
public class IdentificationFactoryImpl implements IdentificationFactory {

    public static final int DEBUG=0;

    static Map<String, AbstractManufacturer> map = new HashMap<>();
    static {
        /* Response to II
         * "EMS50,69A043-x.xx"  Mark V - EMS50 or 60, 4 channel meter
         * "EMS50,69A064-x.xx"  Mark V - EMS50 or 60, 8 channel meter
         * "EMS75,69A055-x.x"   Mark V - EMS75, dual circuit meter
         */
        map.put("EMS50,69A0", new TransdataMarkV()); // recorder
        map.put("EMS75,69A0", new TransdataMarkV());

        map.put("ABB ALPHA,01", new ElsterAlphaPlus());

        map.put("EMON-IDR-08A", new EMONIDR08A());
        map.put("EMO", new EMO());
        map.put("WebRTU", new DukePower());
        map.put("ECT", new EictVDEW());
        map.put("SLB", new SLB());
        map.put("GEC", new GEC());
        map.put("ISK", new ISK());
        map.put("LGZ", new LGZ());
        map.put("AS230", new AS230());

    }

    public AbstractManufacturer getAbstractManufacturer(String className,int dummy) throws IOException {
        for (AbstractManufacturer value : map.values()) {
            if (value.getMeterProtocolClass().compareTo(className) == 0) {
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG> IdentificationFactoryImpl, getAbstractManufacturer(" + className + ")=" + value);
                }
                return value;
            }
        }
        throw new IOException("IdentificationFactoryImpl, no meter found for className "+className);
    }

    public AbstractManufacturer getAbstractManufacturer(String iResponse) throws IOException {
        for (String key : map.keySet()) {
            if (iResponse.contains(key)) {
                AbstractManufacturer abm = map.get(key);
                if (DEBUG >= 1) {
                    System.out.println("KV_DEBUG> IdentificationFactoryImpl, getAbstractManufacturer (" + key + ")," + abm);
                }
                return map.get(key);
            }
        }
        throw new IOException("IdentificationFactoryImpl, no meterid found for iResponse "+iResponse);
    }

    private AbstractManufacturer getAbstractManufacturer(MeterId meterId) throws IOException {
        AbstractManufacturer am = map.get(meterId.getMeter3letterId());
        if (am != null) {
           am.setSignOnString(meterId.getIdentification());
           return am;
        }
        else {
            throw new IOException("IdentificationFactoryImpl, unknown meterid " + meterId);
        }
    }

    private AbstractManufacturer getAbstractManufacturer(MeterType meterType) throws IOException {
        AbstractManufacturer am = map.get(meterType.getMeter3letterId());
        if (am != null) {
           am.setSignOnString(meterType.getReceivedIdent());
           return am;
        }
        else {
            throw new IOException("IdentificationFactoryImpl, unknown meterType " + meterType);
        }
    }

    public String getManufacturer(MeterId meterId) throws IOException {
        return getAbstractManufacturer(meterId).getManufacturer();
    }
    public String getManufacturer(String iResponse) throws IOException {
        return getAbstractManufacturer(iResponse).getManufacturer();
    }

    public String getMeterProtocolClass(MeterId meterId) throws IOException {
        return getAbstractManufacturer(meterId).getMeterProtocolClass();
    }
    public String getMeterProtocolClass(MeterType meterType) throws IOException {
        return getAbstractManufacturer(meterType).getMeterProtocolClass();
    }
    public String getMeterProtocolClass(String iResponse) throws IOException {
        return getAbstractManufacturer(iResponse).getMeterProtocolClass();
    }

    public String getResourceName(MeterId meterId) throws IOException {
        return getAbstractManufacturer(meterId).getResourceName();
    }
    public String getResourceName(MeterType meterType) throws IOException {
        return getAbstractManufacturer(meterType).getResourceName();
    }
    public String getResourceName(String iResponse) throws IOException {
        return getAbstractManufacturer(iResponse).getResourceName();
    }
    public String getResourceName(String className,int dummy) throws IOException {
        return getAbstractManufacturer(className,0).getResourceName();
    }

    public String[] getMeterSerialNumberRegisters(MeterId meterId) throws IOException {
        return getAbstractManufacturer(meterId).getMeterSerialNumberRegisters();
    }
    public String[] getMeterSerialNumberRegisters(MeterType meterType) throws IOException {
        return getAbstractManufacturer(meterType).getMeterSerialNumberRegisters();
    }

    public String getMeterDescription(MeterId meterId) throws IOException {
        return getAbstractManufacturer(meterId).getMeterDescription();
    }
    public String getMeterDescription(String iResponse) throws IOException {
        return getAbstractManufacturer(iResponse).getMeterDescription();
    }

}
