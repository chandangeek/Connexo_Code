/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocols.util.ProtocolUtils;

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

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *@beginchanges
	KV|23052007|Bugfix to avoid looping in CourbeCharge object when retrieving more then the bufferspace in the meter
	GNA|19052008|Added a delayAfterConnect parameter to avoid gms failures
	GNA|22012009|Added a customizable safetyTimeout for the transportlayer.
				Default this was 300s so every failing communication took 5min.
 *@endchanges
 */

public class TrimaranPlus extends AbstractProtocol implements ProtocolLink {

    @Override
    public String getProtocolDescription() {
        return "EDF Trimaran+ ICE";
    }

    private int t1Timeout;
    private int safetyTimeout;
    private Connection62056 connection62056;
    private APSEPDUFactory aPSEFactory;
    private DLMSPDUFactory dLMSPDUFactory;
    private int sourceTransportAddress;
    private int destinationTransportAddress;
    private int delayAfterConnect;
    private APSEParameters aPSEParameters;
    private VDEType vDEType = new VDEType(); // default set as
    private TrimaranObjectFactory trimaranObjectFactory;
    TrimaranPlusProfile trimaranPlusProfile=null;
    private RegisterFactory registerFactory=null;

    @Inject
    public TrimaranPlus(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

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
    protected void doDisConnect() throws IOException {

    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return trimaranPlusProfile.getProfileData(lastReading);
    }

    protected void validateSerialNumber() throws IOException {

        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
			return;
		}
        String sn = getDLMSPDUFactory().getStatusResponse().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
			return;
		}
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());

    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setT1Timeout(Integer.parseInt(properties.getProperty("T1Timeout","5000").trim())); // T1 (datalink layer)
        setSourceTransportAddress(Integer.parseInt(properties.getProperty("STSAP","0").trim()));
        setDestinationTransportAddress(Integer.parseInt(properties.getProperty("DTSAP","2").trim()));

        //setInfoTypeHalfDuplex(Integer.parseInt(properties.getProperty("HalfDuplex","50").trim()));

        setAPSEParameters(new APSEParameters());
        getAPSEParameters().setClientType(Integer.parseInt(properties.getProperty("ClientType","40967").trim())); // 0xA007
        getAPSEParameters().setCallingPhysicalAddress(properties.getProperty("CallingPhysicalAddress","30")); // APSE calling physical address, enter as string of even length, containing HEX karakters, default 0x30
        getAPSEParameters().setProposedAppCtxName(Integer.parseInt(properties.getProperty("ProposedAppCtxName","0").trim())); // APSE proposed App context name, default 0
        setInfoTypePassword(properties.getProperty(MeterProtocol.PASSWORD,"0000000000000000"));

        this.safetyTimeout = Integer.parseInt(properties.getProperty("SafetyTimeOut", "300000")); // Safety timeout in the transport layer

        if(Integer.parseInt(properties.getProperty("DelayAfterConnect", "0")) == 1) {
			delayAfterConnect = 6000;
		} else {
			delayAfterConnect = Integer.parseInt(properties.getProperty("DelayAfterConnect", "0").trim());
		}

        try {
            getAPSEParameters().setKey(ProtocolUtils.convert2ascii(getInfoTypePassword().getBytes()));
        }
        catch(IOException e) {
            throw new InvalidPropertyException(e.toString());
        }

        //setVDEType(new VDEType(Integer.parseInt(properties.getProperty("VDEType","0").trim())));

    }
    protected List doGetOptionalKeys() {
        List list = new ArrayList(7);
        list.add("T1Timeout");
        list.add("STSAP");
        list.add("DTSAP");
        list.add("ClientType");
        list.add("CallingPhysicalAddress");
        list.add("ProposedAppCtxName");
        list.add("DelayAfterConnect");
        list.add("SafetyTimeOut");
        //list.add("VDEType");
        return list;
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {

        setAPSEFactory(new APSEPDUFactory(this,getAPSEParameters()));
        setDLMSPDUFactory(new DLMSPDUFactory(this));
        setTrimaranObjectFactory(new TrimaranObjectFactory(this));
//        setConnection62056(new Connection62056(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getT1Timeout(), getSourceTransportAddress(), getDestinationTransportAddress(), getDelayAfterConnect()));
        setConnection62056(new Connection62056(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getT1Timeout(), getSourceTransportAddress(), getDestinationTransportAddress(), getDelayAfterConnect(), this.safetyTimeout));
        getConnection62056().initProtocolLayers();
        trimaranPlusProfile = new TrimaranPlusProfile(this);

        return getTrimaranPlusConnection();
    }

    public Date getTime() throws IOException {
        // KV_TO_DO datecourante
        return new Date();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public void setTime() throws IOException {
        throw new UnsupportedException();
    }
    public String getFirmwareVersion() throws IOException, UnsupportedException {
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


    public APSEPDUFactory getAPSEFactory() {
        return aPSEFactory;
    }

    public void setAPSEFactory(APSEPDUFactory aPSEFactory) {
        this.aPSEFactory = aPSEFactory;
    }

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

    public DLMSPDUFactory getDLMSPDUFactory() {
        return dLMSPDUFactory;
    }

    public void setDLMSPDUFactory(DLMSPDUFactory dLMSPDUFactory) {
        this.dLMSPDUFactory = dLMSPDUFactory;
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();

        strBuff.append(getTrimaranObjectFactory().readParametresPplus1());
        strBuff.append(getTrimaranObjectFactory().readParametresP());
        strBuff.append(getTrimaranObjectFactory().readParametresPmoins1());
        strBuff.append(getTrimaranObjectFactory().readParametresPmoins2());
        strBuff.append(getTrimaranObjectFactory().readAccessPartiel());
        strBuff.append(getTrimaranObjectFactory().readAsservissementClient());
        strBuff.append(getTrimaranObjectFactory().readEnergieIndex());
        strBuff.append(getTrimaranObjectFactory().readPmaxValues());
        strBuff.append(getTrimaranObjectFactory().readDureeDepassementValues());
        strBuff.append(getTrimaranObjectFactory().readDepassementQuadratiqueValues());
        strBuff.append(getTrimaranObjectFactory().readTempsFonctionnementValues());

        strBuff.append(getRegisterFactory().getRegisterInfo());

        return strBuff.toString();
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 1;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return 5*60*getTrimaranObjectFactory().readParametresP().getTCourbeCharge();
    }

    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

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
