/*
 * Trimeran.java
 *
 * Created on 19 juni 2006, 16:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje;


import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.core.TrimeranConnection;
import com.energyict.protocolimpl.edf.trimarancje.core.DataFactory;
import com.energyict.protocolimpl.edf.trimarancje.core.SPDUFactory;
import com.energyict.protocolimpl.edf.trimarancje.registermapping.Register;
import com.energyict.protocolimpl.edf.trimarancje.registermapping.RegisterFactory;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 *@beginchanges
	KV|04012007|Bugfix to correct the year transition behaviour in the load profile data
	GN|20052008|Made a copy of the CVE(trimaran) class to make the CJE protocol
 *@endchanges
 */
public class Trimaran extends AbstractProtocol {

    private TrimeranConnection trimaranConnection=null;
    private SPDUFactory sPDUFactory=null;
    private DataFactory dataFactory=null;
    private TrimaranProfile trimaranProfile=null;
    private RegisterFactory registerFactory=null;
    private int interKarTimeout;
    private int ackTimeout;
    private int commandTimeout;
    private int flushTimeout;
    private String meterVersion;

    public Trimaran(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void doConnect() throws IOException {
        getSPDUFactory().logon();
    }

    @Override
    protected void doDisconnect() throws IOException {
        getSPDUFactory().logoff();
    }
    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getTrimaranProfile().getProfileData();
    }

    @Override
    public int getProfileInterval() throws IOException {
    	getTrimaranProfile().getProfileData();
    	return getTrimaranProfile().getProfileInterval();
    }

    public int getMeterProfileInterval(){
    	this.getLogger().log(Level.INFO, "** Could not retreive profileInterval from the meter **");
    	this.getLogger().log(Level.INFO, "** Make sure the interval on the meter is correct. **");
    	return Integer.parseInt(com.energyict.mdc.upl.MeterProtocol.Property.PROFILEINTERVAL.getName());
    }

    @Override
    protected void validateDeviceId() throws IOException {
//        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) return;
//        String devId = getCommandFactory().getDeviceIDExtendedCommand().getDeviceID();
//        if (devId.compareTo(getInfoTypeDeviceID()) == 0) return;
//        throw new IOException("Device ID mismatch! meter devId="+devId+", configured devId="+getInfoTypeDeviceID());
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("ACKTimeoutTL", PropertyTranslationKeys.EDF_ACK_TIMEOUT_TL, false));
        propertySpecs.add(this.integerSpec("InterCharTimeout", PropertyTranslationKeys.EDF_INTER_CHAR_TIMEOUT, false));
        propertySpecs.add(this.integerSpec("CommandTimeout", PropertyTranslationKeys.EDF_COMMAND_TIMEOUT, false));
        propertySpecs.add(this.integerSpec("FlushTimeout", PropertyTranslationKeys.EDF_FLUSH_TIMEOUT, false));
        propertySpecs.add(this.stringSpec("MeterVersion", PropertyTranslationKeys.EDF_METER_VERSION, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "300").trim())); // TE
        setInfoTypeHalfDuplex(Integer.parseInt(properties.getTypedProperty(PROP_HALF_DUPLEX, "50").trim())); // TC

        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "22000").trim())); // TSE (session layer)
        setAckTimeout(Integer.parseInt(properties.getTypedProperty("ACKTimeoutTL", "5000").trim())); // TL (datalink layer)
        setInterKarTimeout(Integer.parseInt(properties.getTypedProperty("InterCharTimeout", "400").trim())); //

        setCommandTimeout(Integer.parseInt(properties.getTypedProperty("CommandTimeout", "3000").trim())); // Command retry timeout
        setFlushTimeout(Integer.parseInt(properties.getTypedProperty("FlushTimeout", "500").trim())); // Timeout to wait before sending a new command for receiving duplicate frames send by meter

        setMeterVersion(properties.getTypedProperty("MeterVersion", "V1")); // Select the meterVersion, V2 is NOT TESTED YET!

    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 1;
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setSPDUFactory(new SPDUFactory(this));
        setDataFactory(new DataFactory(this));
        trimaranProfile=new TrimaranProfile(this);
        setRegisterFactory(new RegisterFactory(this));
        setTrimaranConnection(new TrimeranConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getInterKarTimeout(),getAckTimeout(),getCommandTimeout(),getFlushTimeout()));
        return getTrimaranConnection();
    }

    @Override
    public Date getTime() {
    	return new Date(System.currentTimeMillis());
    }

    @Override
    public void setTime() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:39 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() {
    	return "UNSUPPORTED";
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

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();
        List<Register> registers = getRegisterFactory().getRegisters();
        for (Register r : registers) {
            builder.append(r).append("\n");
        }
        return builder.toString();
    }

    public TrimeranConnection getTrimaranConnection() {
        return trimaranConnection;
    }

    private void setTrimaranConnection(TrimeranConnection trimaranConnection) {
        this.trimaranConnection = trimaranConnection;
    }

    public SPDUFactory getSPDUFactory() {
        return sPDUFactory;
    }

    private void setSPDUFactory(SPDUFactory sPDUFactory) {
        this.sPDUFactory = sPDUFactory;
    }

    public DataFactory getDataFactory() {
        return dataFactory;
    }

    protected void setDataFactory(DataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    public TrimaranProfile getTrimaranProfile() {
        return trimaranProfile;
    }

    protected void setTrimaranProfile(TrimaranProfile trimaranProfile) {
        this.trimaranProfile = trimaranProfile;
    }

    public RegisterFactory getRegisterFactory() {
        return registerFactory;
    }

    protected void setRegisterFactory(RegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

    public int getInterKarTimeout() {
        return interKarTimeout;
    }

    public void setInterKarTimeout(int interKarTimeout) {
        this.interKarTimeout = interKarTimeout;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public int getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(int commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public int getFlushTimeout() {
        return flushTimeout;
    }

    public void setFlushTimeout(int flushTimeout) {
        this.flushTimeout = flushTimeout;
    }


	protected String getMeterVersion() {
		return meterVersion;
	}


	protected void setMeterVersion(String meterVersion) {
		this.meterVersion = meterVersion;
	}

}