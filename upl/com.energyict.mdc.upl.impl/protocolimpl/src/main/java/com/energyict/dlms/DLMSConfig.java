/*
 * DLMSConfig.java
 *
 * Created on 4 april 2003, 14:57
 */

package com.energyict.dlms;

import java.io.*;


/**
 *
 * @author  Koen
 *
 * DLMS configuration data, only 1 instance of DLMSConfig should exist!
 */

public class DLMSConfig {
    
//    SL7000 hardcoded object references in DLMSCOSEMGlobals
//    final byte[] ASSOC_LN_OBJECT_LN={0,0,40,0,0,(byte)255};
//    final byte[] SAP_OBJECT_LN={0,0,41,0,0,(byte)255};
//    final byte[] CLOCK_OBJECT_LN={0,0,1,0,0,(byte)255};
//    final byte[] HISTORIC_VALUES_OBJECT_LN={0,0,98,1,0,126};
//    final byte[] LOAD_PROFILE_LN={0,0,99,1,0,(byte)255};
    
    
    final static private DLMSConfig clock =  new DLMSConfig("",8,0,0,1,0,-1,255);
    final static private DLMSConfig profile =  new DLMSConfig("",7,1,-1,99,1,-1,-1);
    
    // 18082004
    final static private DLMSConfig eventLog =  new DLMSConfig("",7,1,-1,99,98,-1,-1);
    final static private DLMSConfig historicValues =  new DLMSConfig("",7,0,-1,98,1,-1,126);
    final static private DLMSConfig resetCounter =  new DLMSConfig("",3,1,0,0,1,0,255);
    
    final static private DLMSConfig[] configchange = {
            new DLMSConfig("LGZ",3,0,0,96,2,0,255),
            new DLMSConfig("EIT",3,0,0,96,2,0,255),
            new DLMSConfig("ISK",1,0,0,96,2,0,255),
            new DLMSConfig("EMO",1,1,0,0,2,1,255),
            new DLMSConfig("SLB",4,0,0,96,2,0,255)
    };
    
    final static private DLMSConfig[] version = {
            new DLMSConfig("LGZ",1,1,0,0,2,0,255),
            new DLMSConfig("EIT",1,1,0,0,2,0,255),
            new DLMSConfig("EMO",1,0,0,96,1,2,255),      
            new DLMSConfig("SLB",1,0,0,142,1,1,255)
    };
    
    final static private DLMSConfig[] serialNumber = {
            new DLMSConfig("LGZ",1,1,0,0,0,0,255),
            new DLMSConfig("EMO",1,1,0,0,0,0,255),
            new DLMSConfig("SLB",1,0,0,96,1,255,255),    
            new DLMSConfig("ISK",1,0,0,96,1,0,255)
    };
     
    final static private DLMSConfig[] meterReading = {
            new DLMSConfig("LGZ",3,1,1,1,8,0,255),
            new DLMSConfig("LGZ",3,1,1,5,8,0,255),
            new DLMSConfig("LGZ",3,1,1,8,8,0,255),
            new DLMSConfig("LGZ",3,1,1,2,8,0,255),
            new DLMSConfig("LGZ",3,1,1,7,8,0,255),
            new DLMSConfig("LGZ",3,1,1,6,8,0,255),      
            new DLMSConfig("LGZ",3,1,1,10,8,0,255),      

            new DLMSConfig("EIT",3,1,1,82,8,0,255),
            new DLMSConfig("EIT",3,1,2,82,8,0,255),
            new DLMSConfig("EIT",3,1,3,82,8,0,255),
            new DLMSConfig("EIT",3,1,4,82,8,0,255),
            new DLMSConfig("EIT",3,1,5,82,8,0,255),
            new DLMSConfig("EIT",3,1,6,82,8,0,255),      
            new DLMSConfig("EIT",3,1,7,82,8,0,255),      
            new DLMSConfig("EIT",3,1,8,82,8,0,255),
            new DLMSConfig("EIT",3,1,9,82,8,0,255),
            new DLMSConfig("EIT",3,1,10,82,8,0,255),
            new DLMSConfig("EIT",3,1,11,82,8,0,255),
            new DLMSConfig("EIT",3,1,12,82,8,0,255),
            new DLMSConfig("EIT",3,1,13,82,8,0,255),      
            new DLMSConfig("EIT",3,1,14,82,8,0,255),      
            new DLMSConfig("EIT",3,1,15,82,8,0,255),
            new DLMSConfig("EIT",3,1,16,82,8,0,255),
            new DLMSConfig("EIT",3,1,17,82,8,0,255),
            new DLMSConfig("EIT",3,1,18,82,8,0,255),
            new DLMSConfig("EIT",3,1,19,82,8,0,255),
            new DLMSConfig("EIT",3,1,20,82,8,0,255),      
            new DLMSConfig("EIT",3,1,21,82,8,0,255),      
            new DLMSConfig("EIT",3,1,22,82,8,0,255),
            new DLMSConfig("EIT",3,1,23,82,8,0,255),
            new DLMSConfig("EIT",3,1,24,82,8,0,255),
            new DLMSConfig("EIT",3,1,25,82,8,0,255),
            new DLMSConfig("EIT",3,1,26,82,8,0,255),
            new DLMSConfig("EIT",3,1,27,82,8,0,255),      
            new DLMSConfig("EIT",3,1,28,82,8,0,255),      
            new DLMSConfig("EIT",3,1,29,82,8,0,255),      
            new DLMSConfig("EIT",3,1,30,82,8,0,255),      
            new DLMSConfig("EIT",3,1,31,82,8,0,255),      
            new DLMSConfig("EIT",3,1,32,82,8,0,255),      
            
            new DLMSConfig("EMO",4,1,1,1,8,1,255),
            new DLMSConfig("EMO",4,1,1,1,8,2,255),
            new DLMSConfig("EMO",4,1,1,2,8,1,255),
            new DLMSConfig("EMO",4,1,1,2,8,2,255),
            new DLMSConfig("EMO",4,1,1,1,6,1,255),
            new DLMSConfig("EMO",4,1,1,2,6,1,255),
            new DLMSConfig("EMO",4,1,1,3,6,1,255),      
            new DLMSConfig("EMO",4,1,1,4,6,1,255)      
    };
    
    private int classid;
    private int a;
    private int b;
    private int c;
    private int d;
    private int e;
    private int f;
    private String manuf;
    
    static private DLMSConfig config=null;
    static public DLMSConfig getInstance() {
       if (config == null) {
          config = new DLMSConfig();   
       }
       return config;
    }
    
    private DLMSConfig() {
    }
    /** Creates a new instance of DLMSConfig */
    private DLMSConfig(String manuf,int classid, int a, int b, int c, int d, int e, int f) {
        this.a=a;
        this.b=b;
        this.c=c;
        this.d=d;
        this.e=e;
        this.f=f;
        this.classid=classid;
        this.manuf = manuf;
    }
    
    protected String getManuf() {
        return manuf;   
    }
    public int getClassID() {
        return classid;
    }
    public int getLNA() {
        return a;
    }
    public int getLNB() {
        return b;
    }
    public int getLNC() {
        return c;
    }
    public int getLND() {
        return d;
    }
    public int getLNE() {
        return e;
    }
    public int getLNF() {
        return f;
    }

    /*
     *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return DLMSConfig
     */
    protected DLMSConfig getConfig(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getConfig, objectlist empty!");
       for (int t=0;t<configchange.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (configchange[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(configchange[t])) return configchange[t];
           }
       }
       throw new IOException("DLMSConfig, getConfig, not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with a version DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return DLMSConfig
     */
    protected DLMSConfig getVersion(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getVersion, objectlist empty!");
       for (int t=0;t<version.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (version[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(version[t])) return version[t];
           }
       }
       throw new IOException("DLMSConfig, getVersion, not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return DLMSConfig
     */
    protected DLMSConfig getSerialNumber(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getSerialNumber, objectlist empty!");
       for (int t=0;t<serialNumber.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (serialNumber[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(serialNumber[t])) return serialNumber[t];
           }
       }
       throw new IOException("DLMSConfig, getSerialNumber, not found in objectlist (IOL)!");      
    }
    
    protected DLMSConfig getClock() {
        return clock;        
    }
    
    protected DLMSConfig getProfile() {
        return profile;
    }
    
    protected DLMSConfig getEventLog() {
        return eventLog;
    }
    
    protected DLMSConfig getHistoricValues() {
        return historicValues;
    }
    
    protected DLMSConfig getResetCounter() {
        return resetCounter;
    }
    /*
     *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getConfigSN(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getConfigSN, objectlist empty!");
       for (int t=0;t<configchange.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (configchange[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(configchange[t])) return objectList[i].getBaseName();
           }
       }
       return 0;
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with a version DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getVersionSN(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getVersionSN, objectlist empty!");
       for (int t=0;t<version.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (version[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(version[t])) return objectList[i].getBaseName();
           }
       }
       return 0;
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getSerialNumberSN(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getSerialNumberSN, objectlist empty!");
       for (int t=0;t<serialNumber.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (serialNumber[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(serialNumber[t])) return objectList[i].getBaseName();
           }
       }
       return 0;
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the clock DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getClockSN(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getClockSN, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(clock)) return objectList[i].getBaseName();
       }
       return 0;        
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the profile DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getProfileSN(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getProfileSN, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(profile)) return objectList[i].getBaseName();
       }
       return 0;        
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the eventLog DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getEventLogSN(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getEventLogSN, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(eventLog)) return objectList[i].getBaseName();
       }
       return 0;        
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the historicValues DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getHistoricValuesSN(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getHistoricValuesSN, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(historicValues)) return objectList[i].getBaseName();
       }
       return 0;        
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the resetCounter DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return int short name reference
     */
    protected int getResetCounterSN(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getResetCounterSN, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(resetCounter)) return objectList[i].getBaseName();
       }
       return 0;        
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with a configchange DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getConfigObject(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getConfigObject, objectlist empty!");
       for (int t=0;t<configchange.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (configchange[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               
//System.out.println("KV_DEBUG> "+objectList[i].toString()+" == "+ configchange[t].toString()+" ?");          
               if (objectList[i].equals(configchange[t])) return objectList[i];
           }
       }
       throw new IOException("DLMSConfig, getConfigObject, not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with a version DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getVersionObject(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getVersionObject, objectlist empty!");
       for (int t=0;t<version.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (version[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(version[t])) return objectList[i];
           }
       }
       throw new IOException("DLMSConfig, getVersionObject, not found in objectlist (IOL)!");      

    }
    /*
     *  Find in objectList a matching DLMSConfig object with a serialNumber DLMSConfig object
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getSerialNumberObject(UniversalObject[] objectList,String manuf) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getSerialNumberObject, objectlist empty!");
       for (int t=0;t<serialNumber.length;t++) {
           // if manuf != null, use it in the search for DLMSConfig object! 
           if ((manuf != null) && (serialNumber[t].getManuf().compareTo(manuf) != 0)) continue;
           for (int i=0;i<objectList.length;i++) {
               if (objectList[i].equals(serialNumber[t])) return objectList[i];
           }
       }
       throw new IOException("DLMSConfig, getSerialNumberObject, not found in objectlist (IOL)!");      

    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the clock DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getClockObject(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getClockObject, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(clock)) return objectList[i];
       }
       throw new IOException("DLMSConfig, getClockObject, not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the profile DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getProfileObject(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getProfileObject, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(profile)) return objectList[i];
       }
       throw new IOException("DLMSConfig, getProfileObject, not found in objectlist (IOL)!");      
    }

    
    /*
     *  Find in objectList a matching DLMSConfig object with the eventLog DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getEventLogObject(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getEventLogObject, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(eventLog)) return objectList[i];
       }
       throw new IOException("DLMSConfig, getEventLogObject, not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the historicValues DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getHistoricValuesObject(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getHistoricValuesObject, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(historicValues)) return objectList[i];
       }
       throw new IOException("DLMSConfig, getHistoricValuesObject, not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with the resetCounter DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @return UniversalObject the matching objectList
     */
    protected UniversalObject getResetcounterObject(UniversalObject[] objectList) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getResetcounterObject, objectlist empty!");
       for (int i=0;i<objectList.length;i++) {
           if (objectList[i].equals(resetCounter)) return objectList[i];
       }
       throw new IOException("DLMSConfig, getResetcounterObject, not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in meterReading DLMSConfig objects all the objects assigned to a specific deviceID
     *  @param String deviceId
     *  @return int nr of meterreading DLMSConfig objects
     */
    private int getNrOfMeterReadingObjects(String deviceId) {
        int count=0;
        for (int i=0;i<meterReading.length;i++) {
           if (meterReading[i].getManuf().compareTo(deviceId) == 0) count++;
        }
        return count;
    }
    
    /*
     *  Get the meterReading DLMSConfig object for a specific id
     *  @param String deviceId
     *  @param int id
     *  @return DLMSConfig
     */
    private DLMSConfig getMeterReadingDLMSConfigObject(int id,String deviceId) throws IOException {
        int count=0;
        for (int i=0;i<meterReading.length;i++) {
           if (meterReading[i].getManuf().compareTo(deviceId) == 0) {
               if (id == count) return meterReading[i];
               count++;
           }
        }
       throw new IOException("DLMSConfig, getMeterReadingDLMSConfigObject("+id+","+deviceId+"), not found in objectlist (IOL)!");      
    }
    
    /*
     *  Find in objectList a matching DLMSConfig object with a meterreading DLMSConfig objects
     *  @param UniversalObject[] objectList
     *  @param String deviceId
     *  @param int id
     *  @return UniversalObject the matching objectList
     */
   protected UniversalObject getMeterReadingObject(UniversalObject[] objectList,int id, String deviceId) throws IOException {
       if (objectList == null) throw new IOException("DLMSConfig, getMeterReadingObject, objectlist empty!");
       if (id >=getNrOfMeterReadingObjects(deviceId)) throw new IOException("DLMSConfig, getMeterReadingObject, meterreading id error!");
       //for (int t=0;t<version.length;t++) { // KV 17062003 removed
       for (int i=0;i<objectList.length;i++) {
           DLMSConfig dlmsConfig = getMeterReadingDLMSConfigObject(id,deviceId);
           //System.out.println(dlmsConfig.toString()+" == "+objectList[i].toString()+" ?");
           if (objectList[i].equals(dlmsConfig)) return objectList[i];
       }
       //}
       throw new IOException("DLMSConfig, getMeterReadingObject("+id+","+deviceId+"), not found in objectlist (IOL)!");      
    }
    
    public String toString() {
        return this.getLNA()+"."+
               this.getLNB()+"."+
               this.getLNC()+"."+
               this.getLND()+"."+
               this.getLNE()+"."+
               this.getLNF()+"."+
               this.getClassID();
    }
    
    public static void main(String[] args)
    {
        DLMSConfig config = DLMSConfig.getInstance();
        System.out.println("DLMS configuration");
        
        System.out.println(config.getClock().toString());
        
    }    
    
    
}
