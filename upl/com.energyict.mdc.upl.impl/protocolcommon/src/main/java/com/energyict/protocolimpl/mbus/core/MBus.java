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

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.Discover;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.mbus.core.connection.MBusConnection;
import com.energyict.protocolimpl.mbus.core.connection.MBusException;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.mbus.core.discover.SecondaryAddressDiscover;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author kvds
 */
abstract public class MBus extends AbstractProtocol implements Discover { 
    
    final int DEBUG=1;
    
    abstract protected void doTheConnect() throws IOException;
    abstract protected void doTheDisConnect() throws IOException;
    abstract protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;
    abstract protected List doTheGetOptionalKeys();
    abstract protected void initRegisterFactory();
    
    private MBusConnection mBusConnection=null;
    private AbstractRegisterFactory registerFactory=null;
    private CIField72h cIField72h=null;
    
    int secondaryAddressing;
    boolean useZeroBased;
    
    int headerManufacturerCode;
    int headerVersion;
    int headerMedium;
    
    /** Creates a new instance of MBus */
    public MBus() {
    }
   
    
    
    public void doConnect() throws IOException {
        try {
            
            if ((getInfoTypeDeviceID()==null) || (getInfoTypeDeviceID().compareTo("")==0))
                getMBusConnection().setRTUAddress(253);
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
        setSecondaryAddressing(Integer.parseInt(properties.getProperty("SecondaryAddressing","0")));
        setUseZeroBased(Integer.parseInt(properties.getProperty("DataQuantitiesAreZeroBased", "0")) == 1);
        
        String manufCode = properties.getProperty("HeaderManufacturerCode");
        if (manufCode == null)
        	setHeaderManufacturerCode(0xFFFF);
        else {
        	try {
        		setHeaderManufacturerCode(Integer.parseInt(manufCode,16));
        	}
        	catch(NumberFormatException e) {
        		// probably ASCII...
        		setHeaderManufacturerCode(CIField72h.getManufacturerCode(manufCode));
        	}
        }
        setHeaderMedium(Integer.parseInt(properties.getProperty("HeaderMedium","FF"),16));
        setHeaderVersion(Integer.parseInt(properties.getProperty("HeaderVersion","FF"),16));

        doTheValidateProperties(properties);
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0; // KV_TO_DO
    }
    
    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        list.add("SecondaryAddressing");
        list.add("DataQuantitiesAreZeroBased");
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

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "YET UNKNOWN";
    }

    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        obisCode=new ObisCode(0,0,96,99,obisCode.getE(),obisCode.getF());
        return getRegisterFactory().findRegisterValue(obisCode);
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        obisCode=new ObisCode(0,0,96,99,obisCode.getE(),obisCode.getF());
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
            CIField72h ciField72h = getCIField72h();    // Do this first - if this fails, the registerFactory should NOT be initialized but stay NULL
            initRegisterFactory();
            registerFactory.init(ciField72h); //.getDataRecords());
        }
        return registerFactory;
    }

    public void setRegisterFactory(AbstractRegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

    public List<CIField72h> discoverDeviceSerialNumbers() throws IOException {
    	
    	SecondaryAddressDiscover o = new SecondaryAddressDiscover(this);
    	o.discover();
    	List<CIField72h> cIField72hs = o.getCIField72hs();
    	
    	return cIField72hs;
    }
    
    private String buildSerialId(long id,int manuf,int version,int medium) {
    	return id+"_"+manuf+"_"+version+"_"+medium;
    }
    
    public CIField72h getCIField72h() throws IOException {
        if (cIField72h==null) {
        	ApplicationData frame = getMBusConnection().sendREQ_UD2().getASDU();
        	if (frame == null)
        		throw new MBusException("MBus, Framing error!");
            cIField72h = (CIField72h)frame.buildAbstractCIFieldObject(getTimeZone());
        }
        return cIField72h;
    }
	public int getInfoTypeSecondaryAddressing() {
		return secondaryAddressing;
	}
	private void setSecondaryAddressing(int secondaryAddressing) {
		this.secondaryAddressing = secondaryAddressing;
	}

    public boolean isUseZeroBased() {
        return useZeroBased;
    }

    public void setUseZeroBased(boolean useZeroBased) {
        this.useZeroBased = useZeroBased;
    }

	public int getInfoTypeHeaderManufacturerCode() {
		return headerManufacturerCode;
	}
	public void setHeaderManufacturerCode(int headerManufacturerCode) {
		this.headerManufacturerCode = headerManufacturerCode;
	}
	public int getInfoTypeHeaderVersion() {
		return headerVersion;
	}
	public void setHeaderVersion(int headerVersion) {
		this.headerVersion = headerVersion;
	}
	public int getInfoTypeHeaderMedium() {
		return headerMedium;
	}
	public void setHeaderMedium(int headerMedium) {
		this.headerMedium = headerMedium;
	}
}
