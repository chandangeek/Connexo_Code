/*
 * RtuPlusSettings.java
 *
 * Created on 19 februari 2004, 15:08
 */

package com.energyict.protocolimpl.rtuplusbus;

import java.io.*;
import java.util.*;
import com.energyict.cbo.LittleEndianInputStream;
import com.energyict.cbo.NestedIOException;
/**
 *
 * @author  Koen
 */
public class RtuPlusSettings {
    
    private static final int DEBUG=0;
    
    String name;
    long password1=-1,password2=-1;
    String modemInit;
    String modemInitAd1,modemInitAd2;
    int master=-1;
    int profileInterval=-1;
    int instantInterval=-1;
    int produktcode=-1;
    int nodeId=-1;
    int tariffMoment=-1;
    int serieNr=-1;
    
    private static final int VERSION_LENGTH_131=131;
    private static final int VERSION_LENGTH_132=132;
    private static final int VERSION_LENGTH_133=133; // extra byte (CopyIoTimeOut) na dummy
    
    /** Creates a new instance of RtuPlusSettings */
    public RtuPlusSettings() {
    }
    
    public void parse(byte[] rtuPlusSettingsFrame) {
        if (DEBUG>=1) {
            System.out.println("KV_DEBUG> versionLength="+rtuPlusSettingsFrame.length);
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(rtuPlusSettingsFrame);
        fillFields( new LittleEndianInputStream(bais));
    }
    
    private void fillFields(LittleEndianInputStream littleEndianInputStream) {
        try {
            String name = littleEndianInputStream.readString(18);
            setName(( name.indexOf( 0 ) == -1 ) ? name : name.substring( 0, name.indexOf( 0 ) ));
            setPassword1(littleEndianInputStream.readLEInt());
            setPassword2(littleEndianInputStream.readLEInt());
            setModemInit(littleEndianInputStream.readString(50));
            setModemInitAd1(littleEndianInputStream.readString(20));
            setModemInitAd2(littleEndianInputStream.readString(20));
            
            
/*            
            setMaster((int)littleEndianInputStream.readByte()&0xFF); 
            if (versionLength == VERSION_LENGTH_131) {
               setProfileInterval(((int)littleEndianInputStream.readByte()&0xFF)*60);
               littleEndianInputStream.readByte(); // read dummy byte
            }
            else if ((versionLength == VERSION_LENGTH_132) || (versionLength == VERSION_LENGTH_133)) {
               setProfileInterval(littleEndianInputStream.readLEShort());
               setInstantInterval(littleEndianInputStream.readByte());
            }
            setProduktcode(littleEndianInputStream.readByte());
            setNodeId(littleEndianInputStream.readByte());
            setTariffMoment(littleEndianInputStream.readLEShort()); // is dummy in case of VERSION_LENGTH_131
            setSerieNr(littleEndianInputStream.readLEShort());
            // in VERSION_LENGTH_133 there is an extra byte CopyIoTimeOut which we do not use
 */
        }
        catch(IOException e) {
            return;
        }
    }
    
    public String toString() {
       return "name: "+getName()+"\n"+
              "password1: "+getPassword1()+"\n"+
              "password2: "+getPassword2()+"\n"+
              "modemInit: "+getModemInit()+"\n"+
              "modemInitAd1: "+getModemInitAd1()+"\n"+
              "modemInitAd2: "+getModemInitAd2()+"\n"+
              "master: "+getMaster()+"\n"+
              "profile interval: "+getProfileInterval()+"\n"+
              "instant interval: "+getInstantInterval()+"\n"+
              "produkt code: "+getProduktcode()+"\n"+
              "node id: "+getNodeId()+"\n"+
              "tariffmoment: "+getTariffMoment()+"\n"+
              "serie nr: "+getSerieNr()+"\n";
              
        
    }
    
    
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public java.lang.String getName() {
        return name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /** Getter for property password1.
     * @return Value of property password1.
     *
     */
    public long getPassword1() {
        return password1;
    }
    
    /** Setter for property password1.
     * @param password1 New value of property password1.
     *
     */
    public void setPassword1(long password1) {
        this.password1 = password1;
    }
    
    /** Getter for property password2.
     * @return Value of property password2.
     *
     */
    public long getPassword2() {
        return password2;
    }
    
    /** Setter for property password2.
     * @param password2 New value of property password2.
     *
     */
    public void setPassword2(long password2) {
        this.password2 = password2;
    }
    
    /** Getter for property modemInit.
     * @return Value of property modemInit.
     *
     */
    public java.lang.String getModemInit() {
        return modemInit;
    }
    
    /** Setter for property modemInit.
     * @param modemInit New value of property modemInit.
     *
     */
    public void setModemInit(java.lang.String modemInit) {
        this.modemInit = modemInit;
    }
    
    /** Getter for property modemInitAd1.
     * @return Value of property modemInitAd1.
     *
     */
    public java.lang.String getModemInitAd1() {
        return modemInitAd1;
    }
    
    /** Setter for property modemInitAd1.
     * @param modemInitAd1 New value of property modemInitAd1.
     *
     */
    public void setModemInitAd1(java.lang.String modemInitAd1) {
        this.modemInitAd1 = modemInitAd1;
    }
    
    /** Getter for property modemInitAd2.
     * @return Value of property modemInitAd2.
     *
     */
    public java.lang.String getModemInitAd2() {
        return modemInitAd2;
    }
    
    /** Setter for property modemInitAd2.
     * @param modemInitAd2 New value of property modemInitAd2.
     *
     */
    public void setModemInitAd2(java.lang.String modemInitAd2) {
        this.modemInitAd2 = modemInitAd2;
    }
    
    /** Getter for property master.
     * @return Value of property master.
     *
     */
    public int getMaster() {
        return master;
    }
    
    /** Setter for property master.
     * @param master New value of property master.
     *
     */
    public void setMaster(int master) {
        this.master = master;
    }
    
    /** Getter for property profileInterval.
     * @return Value of property profileInterval.
     *
     */
    public int getProfileInterval() {
        return profileInterval;
    }
    
    /** Setter for property profileInterval.
     * @param profileInterval New value of property profileInterval.
     *
     */
    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }
    
    /** Getter for property instantInterval.
     * @return Value of property instantInterval.
     *
     */
    public int getInstantInterval() {
        return instantInterval;
    }
    
    /** Setter for property instantInterval.
     * @param instantInterval New value of property instantInterval.
     *
     */
    public void setInstantInterval(int instantInterval) {
        this.instantInterval = instantInterval;
    }
    
    /** Getter for property produktcode.
     * @return Value of property produktcode.
     *
     */
    public int getProduktcode() {
        return produktcode;
    }
    
    /** Setter for property produktcode.
     * @param produktcode New value of property produktcode.
     *
     */
    public void setProduktcode(int produktcode) {
        this.produktcode = produktcode;
    }
    
    /** Getter for property nodeId.
     * @return Value of property nodeId.
     *
     */
    public int getNodeId() {
        return nodeId;
    }
    
    /** Setter for property nodeId.
     * @param nodeId New value of property nodeId.
     *
     */
    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
    
    /** Getter for property tariffMoment.
     * @return Value of property tariffMoment.
     *
     */
    public int getTariffMoment() {
        return tariffMoment;
    }
    
    /** Setter for property tariffMoment.
     * @param tariffMoment New value of property tariffMoment.
     *
     */
    public void setTariffMoment(int tariffMoment) {
        this.tariffMoment = tariffMoment;
    }
    
    /** Getter for property serieNr.
     * @return Value of property serieNr.
     *
     */
    public int getSerieNr() {
        return serieNr;
    }
    
    /** Setter for property serieNr.
     * @param serieNr New value of property serieNr.
     *
     */
    public void setSerieNr(int serieNr) {
        this.serieNr = serieNr;
    }

    static public void main(String[] args) {
        byte[] data = {  
            0x52,0x74,0x75,0x50,0x6C,0x75,0x73,0x33,0x32,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x00
            ,0x53,0x04,(byte)0xFF,0x47,(byte)0xB2,(byte)0xA6,0x22,0x38
            ,0x41,0x54,0x53,0x30,0x3D,0x31,0x53,0x33,0x30,0x3D
            ,0x36,0x53,0x33,0x36,0x3D,0x37,0x53,0x34,0x36,0x3D
            ,0x31,0x33,0x36,0x53,0x34,0x38,0x3D,0x31,0x32,0x38
            ,0x26,0x51,0x35,0x45,0x30,0x00,0x00,0x00,0x00,0x00
            ,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
            ,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
            ,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01
            ,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
            ,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
            ,0x00
            ,0x58,0x02
            ,0x1E,0x00
            ,0x03,0x00,0x00,0x07,0x00
            ,0x00,0x00,0x00,0x00,0x02,0x00,0x00};  
            
            RtuPlusSettings rtuPlusSettings = new RtuPlusSettings();
            rtuPlusSettings.parse(data);
            System.out.println(rtuPlusSettings.toString());
    }
}
