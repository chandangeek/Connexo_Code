/*
 * TrimaranPlus.java
 *
 * Created on 25 januari 2007, 13:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.DLMSPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.APSEParameters;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.Connection62056;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.ProtocolLink;
import com.energyict.protocolimpl.edf.trimaranplus.core.TrimaranObjectFactory;
import com.energyict.protocolimpl.edf.trimaranplus.core.VDEType;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;

/**
 *@beginchanges
	KV|23052007|Bugfix to avoid looping in CourbeCharge object when retrieving more then the bufferspace in the meter
	GNA|19052008|Added a delayAfterConnect parameter to avoid gms failures
	GNA|22012009|Added a customizable safetyTimeout for the transportlayer.
				Default this was 300s so every failing communication took 5min.
 *@endchanges
 */

public class TrimaranPlus extends AbstractProtocol implements ProtocolLink, SerialNumberSupport {

    private int t1Timeout;
    private int safetyTimeout;
    private Connection62056 connection62056;
    private APSEPDUFactory aPSEFactory;
    private DLMSPDUFactory dLMSPDUFactory;
    private int sourceTransportAddress;
    private int destinationTransportAddress;
    private int delayAfterConnect;
    private APSEParameters aPSEParameters;
    private VDEType vDEType = new VDEType();
    private TrimaranObjectFactory trimaranObjectFactory;
    TrimaranPlusProfile trimaranPlusProfile=null;
    private RegisterFactory registerFactory=null;

    public TrimaranPlus(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected void doConnect() throws IOException {
        getAPSEFactory().getAuthenticationReqAPSE();
        getDLMSPDUFactory().getInitiateRequest();
        getLogger().info(getDLMSPDUFactory().getStatusResponse().toString());
        if (getDLMSPDUFactory().getStatusResponse().getVDEType() == 770){
            getVDEType().setVDEType(VDEType.getVDEBASE());
        }
        if (getDLMSPDUFactory().getStatusResponse().getVDEType() == 771) {
			getVDEType().setVDEType(VDEType.getVDEEJP());
		}
        if (getDLMSPDUFactory().getStatusResponse().getVDEType() == 772) {
			getVDEType().setVDEType(VDEType.getVDEMODULABLE());
		}
    }

    @Override
    protected void doDisconnect() throws IOException {
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return trimaranPlusProfile.getProfileData(lastReading);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("T1Timeout", false));
        propertySpecs.add(this.integerSpec("SafetyTimeOut", false));
        propertySpecs.add(this.integerSpec("STSAP", false));
        propertySpecs.add(this.integerSpec("DTSAP", false));
        propertySpecs.add(this.integerSpec("ClientType", false));
        propertySpecs.add(this.stringSpec("CallingPhysicalAddress", false));
        propertySpecs.add(this.integerSpec("ProposedAppCtxName", false));
        propertySpecs.add(this.integerSpec("DelayAfterConnect", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setT1Timeout(Integer.parseInt(properties.getTypedProperty("T1Timeout", "5000").trim())); // T1 (datalink layer)
        setSourceTransportAddress(Integer.parseInt(properties.getTypedProperty("STSAP", "0").trim()));
        setDestinationTransportAddress(Integer.parseInt(properties.getTypedProperty("DTSAP", "2").trim()));

        setAPSEParameters(new APSEParameters());
        getAPSEParameters().setClientType(Integer.parseInt(properties.getTypedProperty("ClientType", "40967").trim())); // 0xA007
        getAPSEParameters().setCallingPhysicalAddress(properties.getTypedProperty("CallingPhysicalAddress", "30")); // APSE calling physical address, enter as string of even length, containing HEX karakters, default 0x30
        getAPSEParameters().setProposedAppCtxName(Integer.parseInt(properties.getTypedProperty("ProposedAppCtxName", "0").trim())); // APSE proposed App context name, default 0
        setInfoTypePassword(properties.getTypedProperty(PASSWORD.getName(), "0000000000000000"));

        this.safetyTimeout = Integer.parseInt(properties.getTypedProperty("SafetyTimeOut", "300000")); // Safety timeout in the transport layer

        if (Integer.parseInt(properties.getTypedProperty("DelayAfterConnect", "0")) == 1) {
			delayAfterConnect = 6000;
		} else {
			delayAfterConnect = Integer.parseInt(properties.getTypedProperty("DelayAfterConnect", "0").trim());
		}

        try {
            getAPSEParameters().setKey(ProtocolUtils.convert2ascii(getInfoTypePassword().getBytes()));
        }
        catch(IOException e) {
            throw new InvalidPropertyException(e.toString());
        }
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setAPSEFactory(new APSEPDUFactory(this,getAPSEParameters()));
        setDLMSPDUFactory(new DLMSPDUFactory(this));
        setTrimaranObjectFactory(new TrimaranObjectFactory(this));
        setConnection62056(new Connection62056(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getT1Timeout(), getSourceTransportAddress(), getDestinationTransportAddress(), getDelayAfterConnect(), this.safetyTimeout));
        getConnection62056().initProtocolLayers();
        trimaranPlusProfile = new TrimaranPlusProfile(this);
        return getTrimaranPlusConnection();
    }

    @Override
    public Date getTime() throws IOException {
        // KV_TO_DO datecourante
        return new Date();
    }

    @Override
    public String getSerialNumber() {
        try {
            return getDLMSPDUFactory().getStatusResponse().getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:26 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public void setTime() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].toString();
    }

    public int getT1Timeout() {
        return t1Timeout;
    }

    public void setT1Timeout(int t1Timeout) {
        this.t1Timeout = t1Timeout;
    }

    public Connection62056 getTrimaranPlusConnection() {
        return getConnection62056();
    }

    public void setTrimaranPlusConnection(Connection62056 connection62056) {
        this.setConnection62056(connection62056);
    }

    @Override
    public APSEPDUFactory getAPSEFactory() {
        return aPSEFactory;
    }

    public void setAPSEFactory(APSEPDUFactory aPSEFactory) {
        this.aPSEFactory = aPSEFactory;
    }

    @Override
    public Connection62056 getConnection62056() {
        return connection62056;
    }

    public void setConnection62056(Connection62056 connection62056) {
        this.connection62056 = connection62056;
    }

    public int getSourceTransportAddress() {
        return sourceTransportAddress;
    }

    public void setSourceTransportAddress(int sourceTransportAddress) {
        this.sourceTransportAddress = sourceTransportAddress;
    }

    public int getDestinationTransportAddress() {
        return destinationTransportAddress;
    }

    public void setDestinationTransportAddress(int destinationTransportAddress) {
        this.destinationTransportAddress = destinationTransportAddress;
    }

    public APSEParameters getAPSEParameters() {
        return aPSEParameters;
    }

    public void setAPSEParameters(APSEParameters aPSEParameters) {
        this.aPSEParameters = aPSEParameters;
    }

    @Override
    public DLMSPDUFactory getDLMSPDUFactory() {
        return dLMSPDUFactory;
    }

    public void setDLMSPDUFactory(DLMSPDUFactory dLMSPDUFactory) {
        this.dLMSPDUFactory = dLMSPDUFactory;
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return String.valueOf(getTrimaranObjectFactory().readParametresPplus1()) +
                getTrimaranObjectFactory().readParametresP() +
                getTrimaranObjectFactory().readParametresPmoins1() +
                getTrimaranObjectFactory().readParametresPmoins2() +
                getTrimaranObjectFactory().readAccessPartiel() +
                getTrimaranObjectFactory().readAsservissementClient() +
                getTrimaranObjectFactory().readEnergieIndex() +
                getTrimaranObjectFactory().readPmaxValues() +
                getTrimaranObjectFactory().readDureeDepassementValues() +
                getTrimaranObjectFactory().readDepassementQuadratiqueValues() +
                getTrimaranObjectFactory().readTempsFonctionnementValues() +
                getRegisterFactory().getRegisterInfo();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 1;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 5*60*getTrimaranObjectFactory().readParametresP().getTCourbeCharge();
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public VDEType getVDEType() {
        return vDEType;
    }

    public void setVDEType(VDEType vDEType) {
        this.vDEType = vDEType;
    }

    public TrimaranObjectFactory getTrimaranObjectFactory() {
        return trimaranObjectFactory;
    }

    public void setTrimaranObjectFactory(TrimaranObjectFactory trimaranObjectFactory) {
        this.trimaranObjectFactory = trimaranObjectFactory;
    }

    public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory==null) {
			setRegisterFactory(new RegisterFactory(this));
		}
        return registerFactory;
    }

    public void setRegisterFactory(RegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

	protected int getDelayAfterConnect() {
		return delayAfterConnect;
	}

}