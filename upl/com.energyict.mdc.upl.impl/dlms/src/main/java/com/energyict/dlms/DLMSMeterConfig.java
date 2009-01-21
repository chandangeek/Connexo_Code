/*
 * DLMSMeterConfig.java
 *
 * Created on 4 april 2003, 15:52
 * Changes:
 * KV 14072004 DLMSMeterConfig made multithreaded! singleton pattern implementation removed!
 */

package com.energyict.dlms; 

import java.io.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocolimpl.dlms.*;
/**
 *
 * @author  Koen 
 */
public class DLMSMeterConfig {

    private UniversalObject[] IOL=null;
    private UniversalObject[] COL=null;
    String manuf;
    static private DLMSConfig config = DLMSConfig.getInstance();
    
    static public DLMSMeterConfig getInstance() {
        return getInstance(null);
    }
    
    static public DLMSMeterConfig getInstance(String manuf) {
        return new DLMSMeterConfig(manuf);
    }
    
    public void reportIOL() {
        for (int i=0;i<IOL.length;i++) {
            System.out.println(IOL[i].toString());
        }
    }
    
    public boolean isActarisPLCC() {
        if (manuf==null) 
            return false;
        else
            return "ActarisPLCC".compareTo(manuf)==0;
    }
    
    public boolean isIskra() {
        
        if (manuf==null) 
            return false;
        else
            return "ISK".compareTo(manuf)==0;
    }
    
    /** Creates a new instance of DLMSMeterConfig */
    private DLMSMeterConfig(String manuf) {
        this.manuf = manuf;
    }
    
    public int getSN(ObisCode obisCode) throws IOException {
       if (IOL == null) throw new IOException("DLMSMeterConfig, getSN, IOL empty!");
       for (int i=0;i<IOL.length;i++) {
           if (IOL[i].equals(obisCode)) return IOL[i].getBaseName();
       }
      throw new NoSuchRegisterException("DLMSMeterConfig, getSN, "+obisCode.toString()+" not found in meter's instantiated object list!");
    }
    
    public int getClassId(ObisCode obisCode) throws IOException {
       if (IOL == null) throw new IOException("DLMSMeterConfig, getSN, IOL empty!");
       for (int i=0;i<IOL.length;i++) {
           if (IOL[i].equals(obisCode)) {
               int classId = IOL[i].getClassID();
               return classId;
           }
       }
       throw new NoSuchRegisterException("DLMSMeterConfig, getClassId, "+obisCode.toString()+" not found in meter's instantiated object list!");
    }
    
    public int getProfileSN() throws IOException {
       return config.getProfileSN(IOL);    
    }
    
    public int getEventLogSN() throws IOException {
       return config.getEventLogSN(IOL);    
    }
    
    public int getHistoricValuesSN() throws IOException {
       return config.getHistoricValuesSN(IOL);    
    }
    
    public int getResetCounterSN() throws IOException {
       return config.getResetCounterSN(IOL);    
    }
    
    public int getClockSN() throws IOException {
       return config.getClockSN(IOL);    
    }

    public int getConfigSN() throws IOException {
       return config.getConfigSN(IOL,manuf);   
    }
    
    public int getVersionSN() throws IOException {
       return config.getVersionSN(IOL,manuf);    
    }
    public int getSerialNumberSN() throws IOException {
       return config.getSerialNumberSN(IOL,manuf);    
    }

	public int getIPv4SetupSN() throws IOException {
		return config.getIPv4SetupSN(IOL);
	}
	
	public int getP3ImageTransferSN() throws IOException {
		return config.getP3ImageTransferSN(IOL);
	}
    
    public UniversalObject getEventLogObject() throws IOException {
       return config.getEventLogObject(IOL);    
    }
    
    public UniversalObject getProfileObject() throws IOException {
       return config.getProfileObject(IOL);    
    }
    
    public UniversalObject getDailyProfileObject() throws IOException {
    	return config.getDailyProfileObject(IOL,manuf);
    }
    
    public UniversalObject getMonthlyProfileObject() throws IOException {
    	return config.getMonthlyProfileObject(IOL,manuf);
    }
    
    public UniversalObject getClockObject() throws IOException {
       return config.getClockObject(IOL);    
    }
    
    public UniversalObject getStatusObject() throws IOException {
    	return config.getStatusObject(IOL,manuf);
    }
    
    public UniversalObject getConfigObject() throws IOException {
       return config.getConfigObject(IOL,manuf);   
    }
    
    public UniversalObject getVersionObject() throws IOException {
       return config.getVersionObject(IOL,manuf);    
    }

    public UniversalObject getSerialNumberObject() throws IOException {
       return config.getSerialNumberObject(IOL,manuf);    
    }
    
    public UniversalObject getIPv4SetupObject() throws IOException {
    	return config.getIPv4SetupObject(IOL);
    }
    
    public UniversalObject getObject(DLMSObis dlmsObis) throws IOException {
       if (IOL == null) throw new IOException("DLMSMeterConfig, objectlist (IOL) empty!");
       for (int i=0;i<IOL.length;i++) {
           if (IOL[i].equals(dlmsObis)) return IOL[i];
       }
       throw new IOException("DLMSMeterConfig, dlmsObis "+dlmsObis+" not found in objectlist (IOL)!");    
    }
    
    public UniversalObject getMeterReadingObject(int id,String deviceId) throws IOException {
       return config.getMeterReadingObject(IOL,id,deviceId);  
    }
    
    public UniversalObject getChannelObject(int id) throws IOException {
       int count=0;
       for (int i=0;i<COL.length;i++) {
           if (COL[i].isCapturedObjectNotAbstract()) {
               if (id==count)
                    return COL[i];
               count++;
           }
       }
       throw new IOException("DLMSMeterConfig, getMeterDemandObject("+id+"), not found in objectlist (IOL)!");      
    }
    
    
    public UniversalObject getMeterDemandObject(int id) throws IOException {
       int count=0;
       for (int i=0;i<COL.length;i++) {
           // Changed KV 23022007 to allow also gas and water captured objects
           //if (COL[i].isCapturedObjectElectricity()) {
           if (COL[i].isCapturedObjectNotAbstract()) {
               if (id == count) {
                   for (int t=0;t<IOL.length;t++) {
                      if (IOL[t].equals(COL[i])) return IOL[t];
                   }
               }
               count++;
           }
       }
       throw new IOException("DLMSMeterConfig, getMeterDemandObject("+id+"), not found in objectlist (IOL)!");      
    }    
    
    public int getNumberOfChannels() throws IOException {
       int count=0;
       for (int i=0;i<COL.length;i++) {
           
           //System.out.println("KV_DEBUG> "+COL[i].toStringCo());
           
           
           // Changed KV 23022007 to allow also gas and water captured objects
           // if (COL[i].isCapturedObjectElectricity()) {
           if (COL[i].isCapturedObjectNotAbstract()) {
               count++;
           }
       }
       return count;
    }
    
    /**
     * Getter for property IOL.
     * @return Value of property IOL.
     */
    public UniversalObject[] getInstantiatedObjectList() {
        return this.IOL;
    }    
    
    public void setInstantiatedObjectList(UniversalObject[] IOL) {
        this.IOL=IOL;
    }    
    
    /**
     * Getter for property COL.
     * @return Value of property COL.
     */
    public UniversalObject[] getCapturedObjectList() {
        return this.COL;
    }    
    
    public void setCapturedObjectList(UniversalObject[] COL) {
        this.COL=COL;
    }  
    
    public static void main(String[] args)
    {
        try {
        DLMSMeterConfig meterConfig = DLMSMeterConfig.getInstance();
        System.out.println("DLMS meter configuration");
        
        System.out.println(meterConfig.getClockSN());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

	public UniversalObject getMbusDisconnectControl(int physicalAddress) throws IOException {
		return config.getMbusDisconnectControl(IOL, manuf, physicalAddress);
	}        
	
	public UniversalObject getMbusDisconnectControlState(int physicalAddress) throws IOException {
		return config.getMbusDisconnectControlState(IOL, manuf, physicalAddress);
	}

	public UniversalObject getMbusSerialNumber(int physicalAddress) throws IOException {
		return config.getMbusSerialNumber(IOL, manuf, physicalAddress);
	}

	public UniversalObject getMbusProfile(int physicalAddress) throws IOException {
		return config.getMbusProfile(IOL, manuf, physicalAddress);
	}

	public UniversalObject getXMLConfig() throws IOException {
	       return config.getXMLConfig(IOL,manuf);    
	}
	
	public UniversalObject getImageActivationSchedule() throws IOException{
		return config.getImageActivationSchedule(IOL);
	}

	public UniversalObject getMbusStatusObject(int physicalAddress) throws IOException{
		return config.getMbusStatusObject(IOL, manuf, physicalAddress);
	}
	
	public UniversalObject getP3ImageTransfer() throws IOException{
		return config.getP3ImageTransfer(IOL);
	}
	
}
