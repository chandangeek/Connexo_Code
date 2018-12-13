/*
 * IEC1107IdFactory.java
 *
 * Created on 15 april 2005, 11:44
 */

package com.energyict.protocolimpl.meteridentification;

import com.energyict.protocol.meteridentification.IdentificationFactory;
import com.energyict.protocol.meteridentification.MeterId;
import com.energyict.protocol.meteridentification.MeterType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author  Koen
 */
public class IdentificationFactoryImpl implements IdentificationFactory {
    
    public static final int DEBUG=0;
    
    static Map map = new HashMap();
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
        map.put("WebRTU", new DukePower());
        map.put("SLB", new SLB());
        map.put("GEC", new GEC());
        map.put("AS230", new AS230());

//        map.put("EMO", new EMO());
//        map.put("ECT", new EictVDEW());
//        map.put("ISK", new ISK());
//        map.put("LGZ", new LGZ());
    }    

    /** Creates a new instance of IEC1107IdFactory */
    public IdentificationFactoryImpl() {
    }
    
    
    
    public AbstractManufacturer getAbstractManufacturer(String className,int dummy) throws IOException {
        Iterator it = map.values().iterator();
        while(it.hasNext()) {
            AbstractManufacturer value = (AbstractManufacturer)it.next();
            if (value.getMeterProtocolClass().compareTo(className)==0) {
                if (DEBUG>=1) System.out.println("KV_DEBUG> IdentificationFactoryImpl, getAbstractManufacturer("+className+")="+value);
                return value;
            }
        }
        throw new IOException("IdentificationFactoryImpl, no meter found for className "+className);
    }
    
    public AbstractManufacturer getAbstractManufacturer(String iResponse) throws IOException {
        Iterator it = map.keySet().iterator();
        while(it.hasNext()) {
            String key = (String)it.next();
            if (iResponse.indexOf(key) >= 0) {
                AbstractManufacturer abm = (AbstractManufacturer)map.get(key);
                if (DEBUG>=1) System.out.println("KV_DEBUG> IdentificationFactoryImpl, getAbstractManufacturer ("+key+"),"+abm); 
                return (AbstractManufacturer)map.get(key);
            }
        }
        throw new IOException("IdentificationFactoryImpl, no meterid found for iResponse "+iResponse);
    }
    
    private AbstractManufacturer getAbstractManufacturer(MeterId meterId) throws IOException {
        AbstractManufacturer am = (AbstractManufacturer)map.get(meterId.getMeter3letterId()); 
        if (am != null) {
           am.setSignOnString(meterId.getIdentification()); 
           return am;
        }
        else
           throw new IOException("IdentificationFactoryImpl, unknown meterid "+meterId);
    }
    
    private AbstractManufacturer getAbstractManufacturer(MeterType meterType) throws IOException {
        AbstractManufacturer am = (AbstractManufacturer)map.get(meterType.getMeter3letterId()); 
        if (am != null) {
           am.setSignOnString(meterType.getReceivedIdent()); 
           return am;
        }
        else
           throw new IOException("IdentificationFactoryImpl, unknown meterType "+meterType);
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
    
    
 
    static public void main(String[] args) {
        try {
           IdentificationFactoryImpl ifi = new IdentificationFactoryImpl();
//           MeterType meterType;
//           meterType = new MeterType("/SLB7MINICOR200");
//           System.out.println(meterType);
////           System.out.println(ifi.getManufacturer(meterType.getMeter3letterId()));
//           System.out.println(ifi.getMeterProtocolClass(meterType));
//           meterType = new MeterType("/LGZ4\2ZMD4054407.B14");
//           System.out.println(meterType);
////           System.out.println(ifi.getManufacturer(meterType.getMeter3letterId()));
//           System.out.println(ifi.getMeterProtocolClass(meterType));
//           
//           String iResponse = "WebRTU\r\nAZ3446YYHGF";
//           AbstractManufacturer am = ifi.getAbstractManufacturer(iResponse);
//           System.out.println(am.getMeterProtocolClass());
//           
           String iResponse = "sdcwsdABB ALPHA,01    swcswdcsd";
           AbstractManufacturer am = ifi.getAbstractManufacturer(iResponse);
           System.out.println(am.getMeterProtocolClass());
           System.out.println(am.getResourceName());
           
           
           
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        
    }
    
}
