/*
 * Modbus.java
 *
 * Created on 20 september 2005, 9:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;

import java.io.*;
import java.math.*;
import java.util.*;
import com.energyict.protocol.discover.Discover;
/**
 *
 * @author Koen
 */
abstract public class Modbus extends AbstractProtocol implements Discover {
    
    abstract protected void doTheConnect() throws IOException;
    abstract protected void doTheDisConnect() throws IOException;
    abstract protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;
    abstract protected List doTheGetOptionalKeys();
    abstract protected void initRegisterFactory();
    
    ModbusConnection modbusConnection;
    private AbstractRegisterFactory registerFactory=null;
    private int  interframeTimeout;
            
    private String networkId;
    private boolean virtualLoadProfile;
    int responseTimeout;
    int physicalLayer;
            
    private int registerOrderFixedPoint;
    private int registerOrderFloatingPoint;
    
    /** Creates a new instance of Modbus */
    public Modbus() {
    }
    
    protected void doConnect() throws IOException {
        doTheConnect();
    }
    
    protected void doDisConnect() throws IOException {
        doTheDisConnect();
    }
    
    
    public void setTime() throws IOException {
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    public BigDecimal getRegisterMultiplier(int address) throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }

    
    protected void setInfoTypePhysicalLayer(int physicalLayer) {
    	this.physicalLayer=physicalLayer;
    }
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","10").trim()));
        setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","15").trim()));
        setNetworkId(properties.getProperty("NetworkId",""));
        setVirtualLoadProfile(Integer.parseInt(properties.getProperty("VirtualLoadProfile","0").trim())==1);
        
        physicalLayer = Integer.parseInt(properties.getProperty("PhysicalLayer","0").trim());
        responseTimeout = Integer.parseInt(properties.getProperty("ResponseTimeout","200").trim());
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","2000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty("Retries","2").trim()));
        
        setRegisterOrderFixedPoint(Integer.parseInt(properties.getProperty("RegisterOrderFixedPoint","1").trim()));
        setRegisterOrderFloatingPoint(Integer.parseInt(properties.getProperty("RegisterOrderFloatingPoint","1").trim()));
        
        
        doTheValidateProperties(properties);
    }
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("InterframeTimeout");
        result.add("ResponseTimeout");
        result.add("PhysicalLayer");
        result.add("RegisterOrderFixedPoint");
        result.add("RegisterOrderFloatingPoint");
        
        return result;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        modbusConnection = new ModbusConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInterframeTimeout(), responseTimeout, physicalLayer);
        return modbusConnection;
    }
    
    public Date getTime() throws IOException {
        return new Date(); // KV_TO_DO
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0; // KV_TO_DO
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.10 $";
    }
    
    /*
     * Override this method if the subclass wants to set a specific register 
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        
    }
    
    /*
     * Override this method if the subclass wants to get a specific register 
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        
        StringTokenizer strTok = new StringTokenizer(name,",");
        int functioncode=getTokVal(strTok.nextToken());
        int[] vals = new int[strTok.countTokens()];
        int i=0;
        while(strTok.countTokens()>0)
            vals[i++]=getTokVal(strTok.nextToken());
        return getRegisterFactory().getFunctionCodeFactory().getRequest(functioncode, vals).toString();
    }
    
    private int getTokVal(String tok) {
        if (tok.indexOf("0x")>=0) 
            return Integer.parseInt(tok.substring(2),16);
        else
            return Integer.parseInt(tok); 
    }
    
    
    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return new RegisterValue(obisCode,getRegisterFactory().findRegister(obisCode).quantityValue());
        }
        catch(ModbusException e) {
            if ((e.getExceptionCode()==0x02) && (e.getFunctionErrorCode()==0x83))
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
            else
                throw e;
        }
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        
        //return new RegisterInfo(obisCode.getDescription());
        
        return new RegisterInfo(getRegisterFactory().findRegister(obisCode).getName());
    }    
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        if (extendedLogging==0) {
            StringBuffer strBuff = new StringBuffer();
            Iterator it = getRegisterFactory().getRegisters().iterator();
            while (it.hasNext()) {
                AbstractRegister ar = (AbstractRegister)it.next();
//System.out.println("KV_DEBUG> "+ar.getObisCode());                
                if (ar.getObisCode()==null)
                    strBuff.append(ar.getReg()+"("+ar.getRange()+"), "+ar.getName()+"\n");
                else
                    strBuff.append(ar.getObisCode()+", "+ar.getReg()+"("+ar.getRange()+"), "+ar.getName()+"\n");
            }
            return strBuff.toString();
        }
        else if (extendedLogging==1) {
            StringBuffer strBuff = new StringBuffer();
            Iterator it = getRegisterFactory().getRegisters().iterator();
            while (it.hasNext()) {
                AbstractRegister ar = (AbstractRegister)it.next();
                //System.out.println(ar.getObisCode());
                if (ar.getObisCode()!=null)
                    strBuff.append(readRegister(ar.getObisCode())+"\n");
            }
            return strBuff.toString();
        }
        else return "";
    }
    
    
    /****************************************************************************************************************
     * Implementing Modbus interface
     ****************************************************************************************************************/    
    public ModbusConnection getModbusConnection() {
        return modbusConnection;
    }
    public TimeZone gettimeZone() {
        return super.getTimeZone();
    }

    public AbstractRegisterFactory getRegisterFactory() {
        if (registerFactory==null)
            initRegisterFactory();
        return registerFactory;
    }

    public void setRegisterFactory(AbstractRegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

    public int getInterframeTimeout() {
        return interframeTimeout;
    }

    public void setInfoTypeInterframeTimeout(int interframeTimeout) {
        this.interframeTimeout = interframeTimeout;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public boolean isVirtualLoadProfile() {
        return virtualLoadProfile;
    }

    public void setVirtualLoadProfile(boolean virtualLoadProfile) {
        this.virtualLoadProfile = virtualLoadProfile;
    }
    public int getRegisterOrderFixedPoint() {
        return registerOrderFixedPoint;
    }

    private void setRegisterOrderFixedPoint(int registerOrderFixedPoint) {
        this.registerOrderFixedPoint = registerOrderFixedPoint;
    }

    public int getRegisterOrderFloatingPoint() {
        return registerOrderFloatingPoint;
    }

    private void setRegisterOrderFloatingPoint(int registerOrderFloatingPoint) {
        this.registerOrderFloatingPoint = registerOrderFloatingPoint;
    }
    
}
