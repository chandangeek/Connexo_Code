/*
 * AlphaPlus.java
 *
 * Created on 5 juli 2005, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.AlphaPlusProfile;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.ObisCodeMapper;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.BillingDataRegister;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.BillingDataRegisterFactoryImpl;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class31ModemBillingCallConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class32ModemAlarmCallConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.ClassFactory;
import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author  Koen
 * @beginchanges
 KV|12062007|Bugfix timeout large profiledata request
 KV|13072007|changes for Metersmart for A+ without quadrant info
 * @endchanges
 */
public class AlphaPlus extends AbstractProtocol implements Alpha {

    @Override
    public String getProtocolDescription() {
        return "Elster Alpha Plus";
    }

    private static final int DEBUG=0;
    private AlphaConnection alphaConnection;
    private CommandFactory commandFactory;
    private ClassFactory classFactory;
    private AlphaPlusProfile alphaPlusProfile;
    private int opticalHandshakeOverModemport;

    // lazy initializing
    private BillingDataRegisterFactoryImpl billingDataRegisterFactory=null;
    long whoAreYouTimeout;
    private int totalRegisterRate;

    @Inject
    public AlphaPlus(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAlphaPlusProfile().getProfileData(lastReading,includeEvents);
    }

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
        getAlphaConnection().setOptical(commChannel!=null);
    }

    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        if (commChannel!=null)
            commandFactory.opticalHandshake(commChannel, getInfoTypePassword(), getDtrBehaviour());
        else {
            if (opticalHandshakeOverModemport==1)
                commandFactory.opticalHandshakeOverModemport(getInfoTypePassword());
            else
                commandFactory.signOn(getInfoTypeNodeAddressNumber(),getInfoTypePassword());
        }

        // set packetsize so that all Multiple (lenh lenl) packets behave corect (lenh bit 7 last packet)
        getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
    }
    protected void doDisConnect() throws IOException {
        try {
            if (commChannel==null) {
                 commandFactory.getFunctionWithoutDataCommand().sendBillingReadComplete();
                 commandFactory.getFunctionWithoutDataCommand().sendAlarmReadComplete(); // KV 27062007
            }
        }
        finally {
           commandFactory.getShortFormatCommand().terminateSession();
        }
    }
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
        whoAreYouTimeout = Integer.parseInt(properties.getProperty("WhoAreYouTimeout","300").trim());
        totalRegisterRate = Integer.parseInt(properties.getProperty("TotalRegisterRate","1").trim());
        opticalHandshakeOverModemport =  Integer.parseInt(properties.getProperty("OpticalHandshakeOverModemport","0").trim());
    }
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("WhoAreYouTimeout");
        result.add("TotalRegisterRate");
        result.add("OpticalHandshakeOverModemport");
        return result;
    }

    public int getProfileInterval() throws IOException {
        int pi = getClassFactory().getClass14LoadProfileConfiguration().getLoadProfileInterval();
        return pi==0?getInfoTypeProfileInterval():pi;
    }

    public int getNumberOfChannels() throws IOException {
        return getClassFactory().getClass14LoadProfileConfiguration().getNrOfChannels();
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeout,int maxRetries,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        alphaConnection = new AlphaConnection(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, whoAreYouTimeout);
        commandFactory = new CommandFactory(alphaConnection);
        classFactory = new ClassFactory(this);
        alphaPlusProfile = new AlphaPlusProfile(this);
        return alphaConnection;
    }
    public Date getTime() throws IOException {
        return getClassFactory().getClass9Status1().getTD();
    }

    public void setTime() throws IOException {
        getCommandFactory().getFunctionWithDataCommand().syncTime(getInfoTypeRoundtripCorrection(), getTimeZone());
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        try {
           return getClassFactory().getClass8FirmwareConfiguration().getFirmwareVersion();
        }
        catch(IOException e) {
            return "ERROR, unable to get the firmware version of the meter, "+e.toString();
        }
    }

    public String getSerialNumber() throws IOException {
        //return Long.toString(getClassFactory().getClass2IdentificationAndDemandData().getUMTRSN());
        return Long.toString(getClassFactory().getClass7MeteringFunctionBlock().getXMTRSN());
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        List passwords = discoverInfo.getPasswords();

        if (DEBUG>=1) {
            System.out.println("alphaPlus, getSerialNumber, " + discoverInfo);
        }

        if (passwords==null) {
            passwords = new ArrayList();
        }

        if (passwords.isEmpty()) {
            passwords.add("00000000");
        }

        for (int i=0;i<passwords.size();i++) {
            String password = (String)passwords.get(i);
//            while(true) {
                try {
                    Properties properties = new Properties();
                    properties.setProperty(MeterProtocol.PASSWORD,password);
                    setProperties(properties);
                    init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
                    connect();
                    //getCommandFactory().getFunctionWithDataCommand().whoAreYou(0);
                    String serialNumber =  Long.toString(getClassFactory().getSerialNumber()); //getSerialNumber();
                   // disconnect(); // no disconnect because the meter will hangup the link... disconnect contains an EZ7 protocol command to the meter that hangup the link!
                    if (DEBUG>=1) {
                        System.out.println("alphaPlus, getSerialNumber, serialNumber=" + serialNumber + " size=" + serialNumber.length());
                    }
                    return serialNumber;
                }
                catch(IOException ex) {

                    ex.printStackTrace();
                    disconnect();
                    if (i==(passwords.size()-1)) {
                        throw ex;
                    }
                }
//            }
        }
        throw new IOException("AlphaPlus, getSerialNumber(), Error discovering serialnumber!");
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
        getBillingDataRegisterFactory().buildAll();
        StringBuilder builder = new StringBuilder();

        builder.append("************************ CLASSES READ ************************\n");
        builder.append(getClassFactory().getClass0ComputationalConfiguration()).append("\n");
        builder.append(getClassFactory().getClass2IdentificationAndDemandData()).append("\n");
        builder.append(getClassFactory().getClass31ModemBillingCallConfiguration()).append("\n");
        builder.append(getClassFactory().getClass32ModemAlarmCallConfiguration()).append("\n");
        builder.append(getClassFactory().getClass33ModemConfigurationInfo()).append("\n");
        builder.append(getClassFactory().getClass6MeteringFunctionBlock()).append("\n");
        builder.append(getClassFactory().getClass7MeteringFunctionBlock()).append("\n");
        builder.append(getClassFactory().getClass8FirmwareConfiguration()).append("\n");
        builder.append(getClassFactory().getClass14LoadProfileConfiguration()).append("\n");
        builder.append(getClassFactory().getClass9Status1()).append("\n");
        builder.append(getClassFactory().getClass10Status2()).append("\n");

        builder.append("************************ CLASS11 Current billing registers ************************\n");
        Iterator it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.CURRENT_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        builder.append("************************ CLASS12 Previous month billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_MONTH_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        builder.append("************************ CLASS13 Previous season billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_SEASON_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        return builder.toString();
    }


   /*
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
            return;
        }
        String sn = getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());

    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public ClassFactory getClassFactory() {
        return classFactory;
    }

    public BillingDataRegisterFactory getBillingDataRegisterFactory() throws IOException {
        if (billingDataRegisterFactory==null) {
           billingDataRegisterFactory = new BillingDataRegisterFactoryImpl(getClassFactory());
        }
        return billingDataRegisterFactory;

    }

    public AlphaPlusProfile getAlphaPlusProfile() {
        return alphaPlusProfile;
    }
    public AlphaConnection getAlphaConnection() {
        return alphaConnection;
    }

    public void setDialinScheduleTime(Date date) throws IOException {

        // Programm class31
        Calendar cal = this.getCleanCalendar(getTimeZone());
        cal.setTime(date);
        Class31ModemBillingCallConfiguration o = getClassFactory().getClass31ModemBillingCallConfiguration();
        if (o.getTimingWindowFrom() != cal.get(Calendar.HOUR_OF_DAY)) {
            o.setTimingWindowFrom(cal.get(Calendar.HOUR_OF_DAY));
            o.setTimingWindowTo(cal.get(Calendar.HOUR_OF_DAY));
            o.write();
        }

        getCommandFactory().getFunctionWithDataCommand().billingReadDialin(date,getTimeZone());
    }

    public void setPhoneNr(String phoneNr) throws IOException {

        Class31ModemBillingCallConfiguration o = getClassFactory().getClass31ModemBillingCallConfiguration();
        if (changePhoneNr(o.getDialString().trim(), phoneNr.trim()))  {
            o.setDialString(phoneNr);
            o.write();
        }

        Class32ModemAlarmCallConfiguration o2 = getClassFactory().getClass32ModemAlarmCallConfiguration();
        if (changePhoneNr(o2.getDialString().trim(), phoneNr.trim()))  {
            o2.setDialString(phoneNr);
            o2.write();
        }
    }

    private boolean changePhoneNr(String programmedPhoneNr, String newPhoneNr) {
        return ((newPhoneNr != null) && // if the phone nr != null
                (newPhoneNr.compareTo("") != 0) && // if the phone nr != ""
                (programmedPhoneNr.compareTo(newPhoneNr.trim()) != 0) && // phone nr != programmed phone nr
                (!((programmedPhoneNr.compareTo("") == 0) && (newPhoneNr.compareTo("remove")==0)))); // NOT phone nr == remove AND programmed phone nr empty
    }


    public int getTotalRegisterRate() {
        return totalRegisterRate;
    }

    private void setTotalRegisterRate(int totalRegisterRate) {
        this.totalRegisterRate = totalRegisterRate;
    }

}