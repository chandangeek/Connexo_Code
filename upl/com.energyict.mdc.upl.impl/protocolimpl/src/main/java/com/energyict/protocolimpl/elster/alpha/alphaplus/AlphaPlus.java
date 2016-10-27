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

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
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
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

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
public class AlphaPlus extends AbstractProtocol implements Alpha, SerialNumberSupport {

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

    /** Creates a new instance of AlphaPlus */
    public AlphaPlus() {
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

    public int getProfileInterval() throws UnsupportedException, IOException {
        int pi = getClassFactory().getClass14LoadProfileConfiguration().getLoadProfileInterval();
        return pi==0?getInfoTypeProfileInterval():pi;
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
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
        return "$Date: 2015-11-26 15:25:59 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        try {
           return getClassFactory().getClass8FirmwareConfiguration().getFirmwareVersion();
        }
        catch(IOException e) {
            return "ERROR, unable to get the firmware version of the meter, "+e.toString();
        }
    }

    public String getSerialNumber() {
        try {
            return Long.toString(getClassFactory().getClass7MeteringFunctionBlock().getXMTRSN());
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        List passwords = discoverInfo.getPasswords();

        if (DEBUG>=1) System.out.println("alphaPlus, getSerialNumber, "+discoverInfo);

        if (passwords==null)
            passwords = new ArrayList();

        if (passwords.size()==0)
            passwords.add("00000000");

        int retries=0;
        for (int i=0;i<passwords.size();i++) {
            String password = (String)passwords.get(i);
//            while(true) {
                try {
                    Properties properties = new Properties();
                    properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), password);
                    setProperties(properties);
                    init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
                    connect();
                    //getCommandFactory().getFunctionWithDataCommand().whoAreYou(0);
                    String serialNumber =  Long.toString(getClassFactory().getSerialNumber()); //getSerialNumber();
                   // disconnect(); // no disconnect because the meter will hangup the link... disconnect contains an EZ7 protocol command to the meter that hangup the link!
                    if (DEBUG>=1) System.out.println("alphaPlus, getSerialNumber, serialNumber="+serialNumber+" size="+serialNumber.length());
                    return serialNumber;
                }
                catch(IOException ex) {

                    ex.printStackTrace();
    //                try {
    //                   Thread.sleep(500);
                       disconnect();
    //                }
    //                catch(Exception e) {
    //                    // absorb
    //                }

                    if (i==(passwords.size()-1))
                        throw ex;
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
        StringBuffer strBuff = new StringBuffer();

        strBuff.append("************************ CLASSES READ ************************\n");
        strBuff.append(getClassFactory().getClass0ComputationalConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass2IdentificationAndDemandData()+"\n");
        strBuff.append(getClassFactory().getClass31ModemBillingCallConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass32ModemAlarmCallConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass33ModemConfigurationInfo()+"\n");
        strBuff.append(getClassFactory().getClass6MeteringFunctionBlock()+"\n");
        strBuff.append(getClassFactory().getClass7MeteringFunctionBlock()+"\n");
        strBuff.append(getClassFactory().getClass8FirmwareConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass14LoadProfileConfiguration()+"\n");
        strBuff.append(getClassFactory().getClass9Status1()+"\n");
        strBuff.append(getClassFactory().getClass10Status2()+"\n");

        strBuff.append("************************ CLASS11 Current billing registers ************************\n");
        Iterator it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.CURRENT_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()+", "+description+"\n");
        }
        strBuff.append("************************ CLASS12 Previous month billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_MONTH_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()+", "+description+"\n");
        }
        strBuff.append("************************ CLASS13 Previous season billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_SEASON_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()+", "+description+"\n");
        }
        return strBuff.toString();
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
        Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
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
