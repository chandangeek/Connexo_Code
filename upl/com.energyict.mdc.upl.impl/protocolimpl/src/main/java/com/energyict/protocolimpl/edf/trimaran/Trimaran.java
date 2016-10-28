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
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
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

    private TrimeranConnection trimeranConnection=null;
    private SPDUFactory sPDUFactory=null;
    private DataFactory dataFactory=null;
    private TrimaranProfile trimeranProfile=null;
    private RegisterFactory registerFactory=null;
    private int interKarTimeout;
    private int ackTimeout;
    private int commandTimeout;
    private int flushTimeout;

    /** Creates a new instance of Trimeran */
    public Trimaran() {
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

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return getTrimeranProfile().getProfileData();
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return 600;
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

    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("InterCharTimeout");
        result.add("ACKTimeoutTL");
        result.add("CommandTimeout");
        result.add("FlushTimeout");

        return result;
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
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
        Date date = getDataFactory().getMeterStatusTable().getTimestamp();
        return date;
    }

    public void setTime() throws IOException {
        throw new UnsupportedException();
    }

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:39 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "TARIF="+getDataFactory().getMeterStatusTable().getTarif()+
               ", MODETA="+getDataFactory().getMeterStatusTable().getModeta()+
               ", SOMMOD="+getDataFactory().getMeterStatusTable().getSommod()+
               ", ERRFAT="+getDataFactory().getMeterStatusTable().getErrFat()+
               ", ERRSES="+getDataFactory().getMeterStatusTable().getErrSes();
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
        StringBuffer strBuff = new StringBuffer();

        List registers = getRegisterFactory().getRegisters();
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register r = (Register)it.next();
            strBuff.append(r+"\n");
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
