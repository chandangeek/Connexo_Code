/*
 * Trimeran.java
 *
 * Created on 19 juni 2006, 16:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran;

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
import com.energyict.protocolimpl.edf.trimaran.core.DataFactory;
import com.energyict.protocolimpl.edf.trimaran.core.SPDUFactory;
import com.energyict.protocolimpl.edf.trimaran.registermapping.Register;
import com.energyict.protocolimpl.edf.trimaran.registermapping.RegisterFactory;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 *@beginchanges
KV|04012007|Bugfix to correct the year transition behaviour in the load profile data
 *@endchanges
 */
public class Trimaran extends AbstractProtocol {

    private TrimeranConnection trimeranConnection=null;
    private SPDUFactory sPDUFactory=null;
    private DataFactory dataFactory=null;
    private TrimaranProfile trimeranProfile=null;
    private RegisterFactory registerFactory=null;
    private int interKarTimeout;
    private int ackTimeout;
    private int commandTimeout;
    private int flushTimeout;

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
        return getTrimeranProfile().getProfileData();
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 600;
    }

    @Override
    protected void validateDeviceId() throws IOException {
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("HalfDuplex", PropertyTranslationKeys.EDF_HALF_DUPLEX, false));
        propertySpecs.add(this.integerSpec("ACKTimeoutTL", PropertyTranslationKeys.EDF_ACK_TIMEOUT_TL, false));
        propertySpecs.add(this.integerSpec("InterCharTimeout", PropertyTranslationKeys.EDF_INTER_CHAR_TIMEOUT, false));
        propertySpecs.add(this.integerSpec("CommandTimeout", PropertyTranslationKeys.EDF_COMMAND_TIMEOUT, false));
        propertySpecs.add(this.integerSpec("FlushTimeout", PropertyTranslationKeys.EDF_FLUSH_TIMEOUT, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty("ForcedDelay","300").trim())); // TE
        setInfoTypeHalfDuplex(Integer.parseInt(properties.getTypedProperty("HalfDuplex","50").trim())); // TC

        // KV_DEBUG
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(),"22000").trim())); // TSE (session layer)
        setAckTimeout(Integer.parseInt(properties.getTypedProperty("ACKTimeoutTL","5000").trim())); // TL (datalink layer)
        setInterKarTimeout(Integer.parseInt(properties.getTypedProperty("InterCharTimeout","400").trim())); //

        setCommandTimeout(Integer.parseInt(properties.getTypedProperty("CommandTimeout","3000").trim())); // Command retry timeout
        setFlushTimeout(Integer.parseInt(properties.getTypedProperty("FlushTimeout","500").trim())); // Timeout to wait befor sending a new command for receiving duplicate frames send by meter
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 1;
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setSPDUFactory(new SPDUFactory(this));
        setDataFactory(new DataFactory(this));
        trimeranProfile=new TrimaranProfile(this);
        setRegisterFactory(new RegisterFactory(this));
        setTrimeranConnection(new TrimeranConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getInterKarTimeout(),getAckTimeout(),getCommandTimeout(),getFlushTimeout()));
        return getTrimeranConnection();
    }

    @Override
    public Date getTime() throws IOException {
        return getDataFactory().getMeterStatusTable().getTimestamp();
    }

    @Override
    public void setTime() throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:39 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "TARIF="+getDataFactory().getMeterStatusTable().getTarif()+
               ", MODETA="+getDataFactory().getMeterStatusTable().getModeta()+
               ", SOMMOD="+getDataFactory().getMeterStatusTable().getSommod()+
               ", ERRFAT="+getDataFactory().getMeterStatusTable().getErrFat()+
               ", ERRSES="+getDataFactory().getMeterStatusTable().getErrSes();
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(this);
        return ocm.getRegisterValue(obisCode);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder builder = new StringBuilder();
        List registers = getRegisterFactory().getRegisters();
        for (Object register : registers) {
            Register r = (Register) register;
            builder.append(r).append("\n");
        }
        return builder.toString();
    }

    public TrimeranConnection getTrimeranConnection() {
        return trimeranConnection;
    }

    private void setTrimeranConnection(TrimeranConnection trimeranConnection) {
        this.trimeranConnection = trimeranConnection;
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

    private void setDataFactory(DataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    public TrimaranProfile getTrimeranProfile() {
        return trimeranProfile;
    }

    private void setTrimeranProfile(TrimaranProfile trimeranProfile) {
        this.trimeranProfile = trimeranProfile;
    }

    public RegisterFactory getRegisterFactory() {
        return registerFactory;
    }

    private void setRegisterFactory(RegisterFactory registerFactory) {
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

}