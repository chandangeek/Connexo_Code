/*
 * MBus.java
 *
 * Created on 2 oktober 2007, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.mbus.core;

import com.energyict.dialer.core.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocol.discover.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.iec870.*;
import com.energyict.protocolimpl.mbus.core.connection.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author kvds
 */
abstract public class MBus extends AbstractProtocol implements Discover {
    
    final int DEBUG=0;
    
    abstract protected void doTheConnect() throws IOException;
    abstract protected void doTheDisConnect() throws IOException;
    abstract protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;
    abstract protected List doTheGetOptionalKeys();
    abstract protected void initRegisterFactory();
    
    private MBusConnection mBusConnection=null;
    private AbstractRegisterFactory registerFactory=null;
    private CIField72h cIField72h=null;
    
    /** Creates a new instance of MBus */
    public MBus() {
    }
   
    
    
    public void doConnect() throws IOException {
        try {
            
            if ((getInfoTypeDeviceID()==null) || (getInfoTypeDeviceID().compareTo("")==0))
                getMBusConnection().setRTUAddress(254);
            else
                getMBusConnection().setRTUAddress(Integer.parseInt(getInfoTypeDeviceID()));
            doTheConnect();
        }
        catch(IEC870ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    public void doDisConnect() throws IOException {
        try {
            doTheDisConnect();
        }
        catch(IEC870ConnectionException e) {
            getLogger().severe("MBus, doDisConnect() error, "+e.getMessage());
        }
    }
    
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","3000").trim()));
        setInfoTypeProtocolRetriesProperty(Integer.parseInt(properties.getProperty("Retries","2").trim()));
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        doTheValidateProperties(properties);
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0; // KV_TO_DO
    }
    
    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        return list;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setMBusConnection(new MBusConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, getTimeZone(), halfDuplexController));
        return getMBusConnection();
    }
    public Date getTime() throws IOException {
        return new Date();
    }
    public void setTime() throws IOException {
        
    }
    public String getProtocolVersion() {
        return "$Revision: 1.3 $";
    }
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "YET UNKNOWN";
    }

    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        obisCode=new ObisCode(0,0,96,99,0,obisCode.getF());
        return getRegisterFactory().findRegisterValue(obisCode);
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        obisCode=new ObisCode(0,0,96,99,0,obisCode.getF());
        return new RegisterInfo(obisCode.getDescription());
    }    
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        //getRegisterFactory()
        Iterator it = getRegisterFactory().getRegisterValues().iterator();
        while(it.hasNext()) {
            RegisterValue registerValue = (RegisterValue)it.next();
            strBuff.append(registerValue.toString()+"\n");
        }
        if (getCIField72h()!=null)
            strBuff.append(getCIField72h().header());
        return strBuff.toString();
    }

    
    
    public MBusConnection getMBusConnection() {
        return mBusConnection;
    }

    public void setMBusConnection(MBusConnection mBusConnection) {
        this.mBusConnection = mBusConnection;
    }

    public AbstractRegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory==null) {
            initRegisterFactory();
            registerFactory.init(getCIField72h().getDataRecords());
        }
        return registerFactory;
    }

    public void setRegisterFactory(AbstractRegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

    public CIField72h getCIField72h() throws IOException {
        if (cIField72h==null)
            cIField72h = (CIField72h)getMBusConnection().sendREQ_UD2().getASDU().buildAbstractCIFieldObject(getTimeZone());
        return cIField72h;
    }
}
