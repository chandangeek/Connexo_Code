/*
 * AlphaBasic.java
 *
 * Created on 5 juli 2005, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphabasic;

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
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.elster.alpha.BillingDataRegister;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.AlphaBasicProfile;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.ObisCodeMapper;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.BillingDataRegisterFactoryImpl;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.ClassFactory;
import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 * @beginchanges
 * @endchanges
 */
public class AlphaBasic extends AbstractProtocol implements Alpha, SerialNumberSupport {

    public static final int DEBUG=0;

    private AlphaConnection alphaConnection;
    private CommandFactory commandFactory;
    private ClassFactory classFactory;
    private AlphaBasicProfile alphaBasicProfile;

    // lazy initializing
    private BillingDataRegisterFactoryImpl billingDataRegisterFactory=null;
    private long whoAreYouTimeout;
    private int totalRegisterRate;

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;

    public AlphaBasic(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAlphaBasicProfile().getProfileData(lastReading,includeEvents);
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
            commandFactory.signOn(getInfoTypeNodeAddressNumber(), getInfoTypePassword());
        }

        // set packetsize so that all Multiple (lenh lenl) packets behave corect (lenh bit 7 last packet)
        getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
    }

    @Override
    protected void doDisconnect() throws IOException {
        try {
            if (commChannel==null) {
                 commandFactory.getFunctionWithoutDataCommand().sendBillingReadComplete();
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
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "0").trim()));
        whoAreYouTimeout = Integer.parseInt(properties.getTypedProperty("WhoAreYouTimeout","300").trim());
        totalRegisterRate = Integer.parseInt(properties.getTypedProperty("TotalRegisterRate","1").trim());
    }

    @Override
    public int getProfileInterval() throws IOException {
        int pi = getClassFactory().getClass14LoadProfileConfiguration().getLoadProfileInterval();
        return pi==0?getInfoTypeProfileInterval():pi;
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
        alphaBasicProfile = new AlphaBasicProfile(this);
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
           return getClassFactory().getClass8FirmwareConfiguration().getFirmwareVersion()+" "+getClassFactory().getClass8FirmwareConfiguration().getMeterType();
        }
        catch(IOException e) {
            return "ERROR, unable to get the firmware version of the meter, "+e.toString();
        }
    }

    @Override
    public String getSerialNumber(){
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

        if (passwords == null) {
            passwords = new ArrayList<>();
        }

        if (passwords.isEmpty()) {
            passwords.add("00000000");
        }

        for (int i=0;i<passwords.size();i++) {
            String password = passwords.get(i);
            try {
                TypedProperties properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
                properties.setProperty(com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD.getName(), password);
                setUPLProperties(properties);
                init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
                connect();
                return Long.toString(getClassFactory().getSerialNumber());
            }
            catch (IOException ex) {
                disconnect();
                if (i==(passwords.size()-1)) {
                    throw ex;
                }
            }
        }
        throw new IOException("AlphaBasic, getSerialNumber(), Error discovering serialnumber!");
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
        builder.append(getClassFactory().getClass33ModemConfigurationInfo()).append("\n");
        builder.append(getClassFactory().getClass6MeteringFunctionBlock()).append("\n");
        builder.append(getClassFactory().getClass7MeteringFunctionBlock()).append("\n");
        builder.append(getClassFactory().getClass8FirmwareConfiguration()).append("\n");
        builder.append(getClassFactory().getClass9Status1()).append("\n");
        builder.append(getClassFactory().getClass10Status2()).append("\n");
        builder.append(getClassFactory().getClass14LoadProfileConfiguration()).append("\n");
        builder.append(getClassFactory().getClass16LoadProfileHistory()).append("\n");

        builder.append("************************ CLASS11 Current billing registers ************************\n");
        Iterator it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.CURRENT_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().toString();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        builder.append("************************ CLASS12 Previous month billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_MONTH_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().toString();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        builder.append("************************ CLASS13 Previous season billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_SEASON_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().toString();
            builder.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        return builder.toString();
    }

    @Override
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    @Override
    public AlphaConnection getAlphaConnection() {
        return alphaConnection;
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

    public AlphaBasicProfile getAlphaBasicProfile() {
        return alphaBasicProfile;
    }

    //This used to implement the DialinScheduleProtocol interface
    public void setDialinScheduleTime(Date date) throws IOException {
        getCommandFactory().getFunctionWithDataCommand().billingReadDialin(date,getTimeZone());
    }

    @Override
    public int getTotalRegisterRate() {
        return totalRegisterRate;
    }

}