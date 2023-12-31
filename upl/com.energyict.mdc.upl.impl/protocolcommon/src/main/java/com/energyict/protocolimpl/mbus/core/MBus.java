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

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.mbus.core.connection.MBusConnection;
import com.energyict.protocolimpl.mbus.core.connection.MBusException;
import com.energyict.protocolimpl.mbus.core.connection.iec870.IEC870ConnectionException;
import com.energyict.protocolimpl.mbus.core.discover.SecondaryAddressDiscover;
import com.energyict.protocolimpl.properties.nls.PropertyTranslationKeys;
import org.omg.CORBA.TIMEOUT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;

/**
 *
 * @author kvds
 */
public abstract class MBus extends AbstractProtocol {

    final int DEBUG=1;

    public MBus(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    protected abstract void doTheConnect() throws IOException;
    protected abstract void doTheDisConnect() throws IOException;
    protected abstract void initRegisterFactory();

    private MBusConnection mBusConnection=null;
    private AbstractRegisterFactory registerFactory=null;
    private CIField72h cIField72h=null;

    private int secondaryAddressing;
    private boolean useZeroBased;

    private int headerManufacturerCode;
    private int headerVersion;
    private int headerMedium;

    @Override
    public void doConnect() throws IOException {
        try {
            if ((getInfoTypeDeviceID()==null) || (getInfoTypeDeviceID().compareTo("")==0)) {
                getMBusConnection().setRTUAddress(253);
            } else {
                getMBusConnection().setRTUAddress(Integer.parseInt(getInfoTypeDeviceID()));
            }
            doTheConnect();
        }
        catch(IEC870ConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void doDisconnect() throws IOException {
        try {
            doTheDisConnect();
        }
        catch(IEC870ConnectionException e) {
            getLogger().severe("MBus, doDisConnect() error, "+e.getMessage());
        }
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.integerSpec("SecondaryAddressing", PropertyTranslationKeys.MBUS_SECONDARY_ADDRESSING, false));
        propertySpecs.add(this.integerSpec("VirtualLoadProfile", PropertyTranslationKeys.MBUS_VIRTUAL_LOAD_PROFILE, false));
        propertySpecs.add(this.integerSpec("DataQuantitiesAreZeroBased", PropertyTranslationKeys.MBUS_DATA_QUANTITIES_ARE_ZERO_BASED, false));
        propertySpecs.add(this.stringSpec("HeaderManufacturerCode", PropertyTranslationKeys.MBUS_HEADER_MANUFACTURER_CODE, false));
        propertySpecs.add(this.integerSpec("HeaderMedium", PropertyTranslationKeys.MBUS_HEADER_MEDIUM, false));
        propertySpecs.add(this.integerSpec("HeaderVersion", PropertyTranslationKeys.MBUS_HEADER_VERSION, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(Property.TIMEOUT.getName(), "3000").trim()));
        setInfoTypeProtocolRetriesProperty(properties.getTypedProperty(RETRIES.getName(), 2));
        setForcedDelay(properties.getTypedProperty(PROP_FORCED_DELAY, 0));
        setSecondaryAddressing(properties.getTypedProperty("SecondaryAddressing", 0));
        setUseZeroBased(properties.getTypedProperty("DataQuantitiesAreZeroBased", 0) == 1);

        String manufCode = properties.getTypedProperty("HeaderManufacturerCode");
        if (manufCode == null) {
            setHeaderManufacturerCode(0xFFFF);
        } else {
        	try {
        		setHeaderManufacturerCode(Integer.parseInt(manufCode,16));
        	}
        	catch(NumberFormatException e) {
        		// probably ASCII...
        		setHeaderManufacturerCode(CIField72h.getManufacturerCode(manufCode));
        	}
        }
        setHeaderMedium(properties.getTypedProperty("HeaderMedium", 15));
        setHeaderVersion(properties.getTypedProperty("HeaderVersion", 15));
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0; // KV_TO_DO
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setMBusConnection(new MBusConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, getTimeZone(), halfDuplexController));
        return getMBusConnection();
    }

    @Override
    public Date getTime() throws IOException {
        return new Date();
    }

    @Override
    public void setTime() {
    }

    @Override
    public String getFirmwareVersion() {
        return "YET UNKNOWN";
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        obisCode=new ObisCode(0,0,96,99,obisCode.getE(),obisCode.getF());
        return getRegisterFactory().findRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        obisCode=new ObisCode(0,0,96,99,obisCode.getE(),obisCode.getF());
        return new RegisterInfo(obisCode.toString());
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();
        //getRegisterFactory()
        Iterator it = getRegisterFactory().getRegisterValues().iterator();
        while(it.hasNext()) {
            RegisterValue registerValue = (RegisterValue)it.next();
            builder.append(registerValue.toString()).append("\n");
        }
        if (getCIField72h()!=null) {
            builder.append(getCIField72h().header());
        }
        return builder.toString();
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
        return o.getCIField72hs();
    }

    public CIField72h getCIField72h() throws IOException {
        if (cIField72h==null) {
        	ApplicationData frame = getMBusConnection().sendREQ_UD2().getASDU();
        	if (frame == null) {
                throw new MBusException("MBus, Framing error!");
            }
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