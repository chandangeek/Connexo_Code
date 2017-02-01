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

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.elster.alpha.BillingDataRegister;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.AlphaPlusProfile;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.ObisCodeMapper;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.BillingDataRegisterFactoryImpl;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.BillingDataRegisterImpl;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class31ModemBillingCallConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.Class32ModemAlarmCallConfiguration;
import com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes.ClassFactory;
import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

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
    private long whoAreYouTimeout;
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    private SerialCommunicationChannel commChannel;
    private int totalRegisterRate;

    public AlphaPlus(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAlphaPlusProfile().getProfileData(lastReading,includeEvents);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
        getAlphaConnection().setOptical(commChannel!=null);
    }

    @Override
    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        if (commChannel!=null) {
            commandFactory.opticalHandshake(commChannel, getInfoTypePassword(), getDtrBehaviour());
        } else {
            if (opticalHandshakeOverModemport==1) {
                commandFactory.opticalHandshakeOverModemport(getInfoTypePassword());
            } else {
                commandFactory.signOn(getInfoTypeNodeAddressNumber(), getInfoTypePassword());
            }
        }

        // set packetsize so that all Multiple (lenh lenl) packets behave corect (lenh bit 7 last packet)
        getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
    }

    @Override
    protected void doDisconnect() throws IOException {
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

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("WhoAreYouTimeout", false));
        propertySpecs.add(this.integerSpec("TotalRegisterRate", false));
        propertySpecs.add(this.integerSpec("OpticalHandshakeOverModemport", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "0").trim()));
        whoAreYouTimeout = Integer.parseInt(properties.getTypedProperty("WhoAreYouTimeout", "300").trim());
        totalRegisterRate = Integer.parseInt(properties.getTypedProperty("TotalRegisterRate", "1").trim());
        opticalHandshakeOverModemport =  Integer.parseInt(properties.getTypedProperty("OpticalHandshakeOverModemport", "0").trim());
    }

    @Override
    public int getProfileInterval() throws IOException {
        int pi = getClassFactory().getClass14LoadProfileConfiguration().getLoadProfileInterval();
        if (pi == 0) {
            return getInfoTypeProfileInterval();
        } else {
            return pi;
        }
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getClassFactory().getClass14LoadProfileConfiguration().getNrOfChannels();
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeout,int maxRetries,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        alphaConnection = new AlphaConnection(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, whoAreYouTimeout);
        commandFactory = new CommandFactory(alphaConnection);
        classFactory = new ClassFactory(this);
        alphaPlusProfile = new AlphaPlusProfile(this);
        return alphaConnection;
    }

    @Override
    public Date getTime() throws IOException {
        return getClassFactory().getClass9Status1().getTD();
    }

    @Override
    public void setTime() throws IOException {
        getCommandFactory().getFunctionWithDataCommand().syncTime(getInfoTypeRoundtripCorrection(), getTimeZone());
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
           return getClassFactory().getClass8FirmwareConfiguration().getFirmwareVersion();
        }
        catch(IOException e) {
            return "ERROR, unable to get the firmware version of the meter, "+e.toString();
        }
    }

    @Override
    public String getSerialNumber() {
        try {
            return Long.toString(getClassFactory().getClass7MeteringFunctionBlock().getXMTRSN());
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        SerialCommunicationChannel commChannel = discoverInfo.getCommChannel();
        List<String> passwords = discoverInfo.getPasswords();

        if (DEBUG>=1) {
            System.out.println("alphaPlus, getSerialNumber, " + discoverInfo);
        }

        if (passwords==null) {
            passwords = new ArrayList<>();
        }

        if (passwords.isEmpty()) {
            passwords.add("00000000");
        }

        for (int i=0;i<passwords.size();i++) {
            String password = passwords.get(i);
//            while(true) {
                try {
                    TypedProperties properties = com.energyict.cpo.TypedProperties.empty();
                    properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), password);
                    setUPLProperties(properties);
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
    //                try {
    //                   Thread.sleep(500);
                       disconnect();
    //                }
    //                catch(Exception e) {
    //                    // absorb
    //                }

                    if (i==(passwords.size()-1)) {
                        throw ex;
                    }
                }
//            }
        }
        throw new IOException("AlphaPlus, getSerialNumber(), Error discovering serialnumber!");
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
            BillingDataRegister bdr = (BillingDataRegisterImpl)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().toString();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        builder.append("************************ CLASS12 Previous month billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_MONTH_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegisterImpl)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().toString();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        builder.append("************************ CLASS13 Previous season billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_SEASON_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegisterImpl)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().toString();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        return builder.toString();
    }

    @Override
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public ClassFactory getClassFactory() {
        return classFactory;
    }

    @Override
    public BillingDataRegisterFactory getBillingDataRegisterFactory() throws IOException {
        if (billingDataRegisterFactory==null) {
           billingDataRegisterFactory = new BillingDataRegisterFactoryImpl(getClassFactory());
        }
        return billingDataRegisterFactory;

    }

    public AlphaPlusProfile getAlphaPlusProfile() {
        return alphaPlusProfile;
    }

    @Override
    public AlphaConnection getAlphaConnection() {
        return alphaConnection;
    }

    //This used to implement the DialinScheduleProtocol interface
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

    //This used to implement the DialinScheduleProtocol interface
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


    @Override
    public int getTotalRegisterRate() {
        return totalRegisterRate;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return super.getProtocolChannelMap();
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

}