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


import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.core.TrimeranConnection;
import com.energyict.protocolimpl.edf.trimaran.core.DataFactory;
import com.energyict.protocolimpl.edf.trimaran.core.SPDUFactory;
import com.energyict.protocolimpl.edf.trimaran.registermapping.Register;
import com.energyict.protocolimpl.edf.trimaran.registermapping.RegisterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *@beginchanges
KV|04012007|Bugfix to correct the year transition behaviour in the load profile data
 *@endchanges
 */
public class Trimaran extends AbstractProtocol {

    @Override
    public String getProtocolDescription() {
        return "EDF Trimaran CVE";
    }

    private TrimeranConnection trimeranConnection=null;
    private SPDUFactory sPDUFactory=null;
    private DataFactory dataFactory=null;
    private TrimaranProfile trimeranProfile=null;
    private RegisterFactory registerFactory=null;
    private int interKarTimeout;
    private int ackTimeout;
    private int commandTimeout;
    private int flushTimeout;

    @Inject
    public Trimaran(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected void doConnect() throws IOException {
        getSPDUFactory().logon();
    }


    protected void doDisConnect() throws IOException {
        getSPDUFactory().logoff();
    }


    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
//    SerialCommunicationChannel commChannel;
//    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
//        this.commChannel=commChannel;
//    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getTrimeranProfile().getProfileData();
    }

    public int getProfileInterval() throws IOException {
        return 600;
    }

   /*
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {
//        boolean check = true;
//        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
//        String sn = getSerialNumber();
//        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
//        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }

    protected void validateDeviceId() throws IOException {
//        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) return;
//        String devId = getCommandFactory().getDeviceIDExtendedCommand().getDeviceID();
//        if (devId.compareTo(getInfoTypeDeviceID()) == 0) return;
//        throw new IOException("Device ID mismatch! meter devId="+devId+", configured devId="+getInfoTypeDeviceID());
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","300").trim())); // TE
        setInfoTypeHalfDuplex(Integer.parseInt(properties.getProperty("HalfDuplex","50").trim())); // TC
        //setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","6000").trim())); // TL

        // KV_DEBUG
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","22000").trim())); // TSE (session layer)
        setAckTimeout(Integer.parseInt(properties.getProperty("ACKTimeoutTL","5000").trim())); // TL (datalink layer)
        setInterKarTimeout(Integer.parseInt(properties.getProperty("InterCharTimeout","400").trim())); //

        setCommandTimeout(Integer.parseInt(properties.getProperty("CommandTimeout","3000").trim())); // Command retry timeout
        setFlushTimeout(Integer.parseInt(properties.getProperty("FlushTimeout","500").trim())); // Timeout to wait befor sending a new command for receiving duplicate frames send by meter
    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList(
                    "InterCharTimeout",
                    "ACKTimeoutTL",
                    "CommandTimeout",
                    "FlushTimeout");
    }

    public int getNumberOfChannels() throws IOException {
        return 1;
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setSPDUFactory(new SPDUFactory(this));
        setDataFactory(new DataFactory(this));
        trimeranProfile=new TrimaranProfile(this);
        setRegisterFactory(new RegisterFactory(this));
        setTrimeranConnection(new TrimeranConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getInterKarTimeout(),getAckTimeout(),getCommandTimeout(),getFlushTimeout()));
        return getTrimeranConnection();
    }

    public Date getTime() throws IOException {
        return getDataFactory().getMeterStatusTable().getTimestamp();
    }

    public void setTime() throws IOException {
        throw new UnsupportedException();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return "TARIF="+getDataFactory().getMeterStatusTable().getTarif()+
               ", MODETA="+getDataFactory().getMeterStatusTable().getModeta()+
               ", SOMMOD="+getDataFactory().getMeterStatusTable().getSommod()+
               ", ERRFAT="+getDataFactory().getMeterStatusTable().getErrFat()+
               ", ERRSES="+getDataFactory().getMeterStatusTable().getErrSes();
    }

    public String getSerialNumber() throws IOException {
        return null;
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

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuilder strBuff = new StringBuilder();

        List registers = getRegisterFactory().getRegisters();
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register r = (Register)it.next();
            strBuff.append(r).append("\n");
        }

        return strBuff.toString();
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
