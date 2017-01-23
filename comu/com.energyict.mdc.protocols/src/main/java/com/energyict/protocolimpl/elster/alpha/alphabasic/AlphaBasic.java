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
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.AlphaBasicProfile;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.ObisCodeMapper;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.BillingDataRegister;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.BillingDataRegisterFactoryImpl;
import com.energyict.protocolimpl.elster.alpha.alphabasic.core.classes.ClassFactory;
import com.energyict.protocolimpl.elster.alpha.core.Alpha;
import com.energyict.protocolimpl.elster.alpha.core.classes.BillingDataRegisterFactory;
import com.energyict.protocolimpl.elster.alpha.core.connection.AlphaConnection;
import com.energyict.protocolimpl.elster.alpha.core.connection.CommandFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author  Koen
 * @beginchanges
 * @endchanges
 */
public class AlphaBasic extends AbstractProtocol implements Alpha {

    @Override
    public String getProtocolDescription() {
        return "Elster Alpha Basic";
    }

    public static final int DEBUG=0;

    private AlphaConnection alphaConnection;
    private CommandFactory commandFactory;
    private ClassFactory classFactory;
    private AlphaBasicProfile alphaBasicProfile;

    // lazy initializing
    private BillingDataRegisterFactoryImpl billingDataRegisterFactory=null;
    long whoAreYouTimeout;
    private int totalRegisterRate;

    @Inject
    public AlphaBasic(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getAlphaBasicProfile().getProfileData(lastReading,includeEvents);
    }

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
        getAlphaConnection().setOptical(commChannel!=null);
    }

    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        if (commChannel!=null) {
            commandFactory.opticalHandshake(commChannel, getInfoTypePassword(), getDtrBehaviour());
        }
        else {
            commandFactory.signOn(getInfoTypeNodeAddressNumber(), getInfoTypePassword());
        }

        // set packetsize so that all Multiple (lenh lenl) packets behave corect (lenh bit 7 last packet)
        getCommandFactory().getFunctionWithDataCommand().PacketSize(4);
    }


    protected void doDisConnect() throws IOException {
        try {
            if (commChannel==null) {
                 commandFactory.getFunctionWithoutDataCommand().sendBillingReadComplete();
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
    }
    protected List<String> doGetOptionalKeys() {
        return Arrays.asList("WhoAreYouTimeout", "TotalRegisterRate");
    }

    public int getProfileInterval() throws IOException {
        int pi = getClassFactory().getClass14LoadProfileConfiguration().getLoadProfileInterval();
        return pi==0?getInfoTypeProfileInterval():pi;
    }

    public int getNumberOfChannels() throws IOException {
        return getClassFactory().getClass14LoadProfileConfiguration().getNrOfChannels();
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream, int timeout, int maxRetries, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        alphaConnection = new AlphaConnection(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, whoAreYouTimeout);
        commandFactory = new CommandFactory(alphaConnection);
        classFactory = new ClassFactory(this);
        alphaBasicProfile = new AlphaBasicProfile(this);
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
           return getClassFactory().getClass8FirmwareConfiguration().getFirmwareVersion()+" "+getClassFactory().getClass8FirmwareConfiguration().getMeterType();
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
            try {
                Properties properties = new Properties();
                properties.setProperty(MeterProtocol.PASSWORD,password);
                setProperties(properties);
                init(commChannel.getInputStream(),commChannel.getOutputStream(),null,null);
                connect();
                return Long.toString(getClassFactory().getSerialNumber());
            }
            catch(IOException ex) {
                disconnect();
                if (i==(passwords.size()-1)) {
                    throw ex;
                }
            }
        }
        throw new IOException("AlphaBasic, getSerialNumber(), Error discovering serialnumber!");
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
        StringBuilder strBuff = new StringBuilder();

        strBuff.append("************************ CLASSES READ ************************\n");
        strBuff.append(getClassFactory().getClass0ComputationalConfiguration()).append("\n");
        strBuff.append(getClassFactory().getClass2IdentificationAndDemandData()).append("\n");
        strBuff.append(getClassFactory().getClass33ModemConfigurationInfo()).append("\n");
        strBuff.append(getClassFactory().getClass6MeteringFunctionBlock()).append("\n");
        strBuff.append(getClassFactory().getClass7MeteringFunctionBlock()).append("\n");
        strBuff.append(getClassFactory().getClass8FirmwareConfiguration()).append("\n");
        strBuff.append(getClassFactory().getClass9Status1()).append("\n");
        strBuff.append(getClassFactory().getClass10Status2()).append("\n");
        strBuff.append(getClassFactory().getClass14LoadProfileConfiguration()).append("\n");
        strBuff.append(getClassFactory().getClass16LoadProfileHistory()).append("\n");

        strBuff.append("************************ CLASS11 Current billing registers ************************\n");
        Iterator it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.CURRENT_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        strBuff.append("************************ CLASS12 Previous month billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_MONTH_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        strBuff.append("************************ CLASS13 Previous season billing registers ************************\n");
        it = getBillingDataRegisterFactory().getBillingDataRegisters(BillingDataRegisterFactoryImpl.PREVIOUS_SEASON_BILLING_REGISTERS).iterator();
        while(it.hasNext()) {
            BillingDataRegister bdr = (BillingDataRegister)it.next();
            String description = (bdr.getDescription() != null?bdr.getDescription():"")+", "+bdr.getObisCode().getDescription();
            strBuff.append(bdr.getRegisterValue().toString()).append(", ").append(description).append("\n");
        }
        return strBuff.toString();
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
    public AlphaConnection getAlphaConnection() {
        return alphaConnection;
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

    public AlphaBasicProfile getAlphaBasicProfile() {
        return alphaBasicProfile;
    }
    public void setDialinScheduleTime(Date date) throws IOException {
        getCommandFactory().getFunctionWithDataCommand().billingReadDialin(date,getTimeZone());
    }

    public int getTotalRegisterRate() {
        return totalRegisterRate;
    }

    private void setTotalRegisterRate(int totalRegisterRate) {
        this.totalRegisterRate = totalRegisterRate;
    }

}