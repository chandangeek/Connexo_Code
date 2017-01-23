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


import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.core.TrimeranConnection;
import com.energyict.protocolimpl.edf.trimarancje.core.DataFactory;
import com.energyict.protocolimpl.edf.trimarancje.core.SPDUFactory;
import com.energyict.protocolimpl.edf.trimarancje.registermapping.Register;
import com.energyict.protocolimpl.edf.trimarancje.registermapping.RegisterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 *@beginchanges
	KV|04012007|Bugfix to correct the year transition behaviour in the load profile data
	GN|20052008|Made a copy of the CVE(trimaran) class to make the CJE protocol
 *@endchanges
 */
public class Trimaran extends AbstractProtocol {

    @Override
    public String getProtocolDescription() {
        return "EDF Trimaran CJE";
    }

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
        return getTrimaranProfile().getProfileData();
    }

    public int getProfileInterval() throws IOException {
    	getTrimaranProfile().getProfileData();
    	return getTrimaranProfile().getProfileInterval();
    }

    public int getMeterProfileInterval(){
    	this.getLogger().log(Level.INFO, "** Could not retreive profileInterval from the meter **");
    	this.getLogger().log(Level.INFO, "** Make sure the interval on the meter is correct. **");
    	return Integer.parseInt(MeterProtocol.PROFILEINTERVAL);
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

        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","22000").trim())); // TSE (session layer)
        setAckTimeout(Integer.parseInt(properties.getProperty("ACKTimeoutTL","5000").trim())); // TL (datalink layer)
        setInterKarTimeout(Integer.parseInt(properties.getProperty("InterCharTimeout","400").trim())); //

        setCommandTimeout(Integer.parseInt(properties.getProperty("CommandTimeout","3000").trim())); // Command retry timeout
        setFlushTimeout(Integer.parseInt(properties.getProperty("FlushTimeout","500").trim())); // Timeout to wait before sending a new command for receiving duplicate frames send by meter

        setMeterVersion(properties.getProperty("MeterVersion", "V1")); // Select the meterVersion, V2 is NOT TESTED YET!

    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList(
                    "InterCharTimeout",
                    "ACKTimeoutTL",
                    "CommandTimeout",
                    "FlushTimeout",
                    "MeterVersion");
    }

    public int getNumberOfChannels() throws IOException {
        return 1;
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setSPDUFactory(new SPDUFactory(this));
        setDataFactory(new DataFactory(this));
        trimaranProfile=new TrimaranProfile(this);
        setRegisterFactory(new RegisterFactory(this));
        setTrimaranConnection(new TrimeranConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getInterKarTimeout(),getAckTimeout(),getCommandTimeout(),getFlushTimeout()));
        return getTrimaranConnection();
    }

    public Date getTime() throws IOException {
//        Date date = getDataFactory().getMeterStatusTable().getTimestamp();
//        return date;
    	return new Date(System.currentTimeMillis());
    }

    public void setTime() throws IOException {
        throw new UnsupportedException();
    }

    public String getProtocolVersion() {
//        return "$Revision$" ;
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
//    	getDataFactory().getPreviousPeriodTable();
//    	getDataFactory().getCurrentPeriodTable();
//        return "TARIF="+getDataFactory().getMeterStatusTable().getTarif()+
//               ", MODETA="+getDataFactory().getMeterStatusTable().getModeta()+
//               ", SOMMOD="+getDataFactory().getMeterStatusTable().getSommod()+
//               ", ERRFAT="+getDataFactory().getMeterStatusTable().getErrFat()+
//               ", ERRSES="+getDataFactory().getMeterStatusTable().getErrSes();

    	return "UNSUPPORTED";
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
            strBuff.append(r+"\n");
        }

        return strBuff.toString();
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
