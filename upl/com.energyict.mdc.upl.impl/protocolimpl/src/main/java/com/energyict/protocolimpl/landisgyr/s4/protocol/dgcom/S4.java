/*
 * S4.java
 *
 * Created on 22 mei 2006, 14:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.CommandFactory;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.registermappping.RegisterMapperFactory;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;

/**
 *
 * @author Koen
 */
public class S4 extends AbstractProtocol implements SerialNumberSupport {

    private DGCOMConnection dgcomConnection;
    private CommandFactory commandFactory;
    private S4Profile s4Profile;
    private RegisterMapperFactory registerMapperFactory;
    private String modemPassword;
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    private SerialCommunicationChannel commChannel;

    public S4(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
    }

    @Override
    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        if (commChannel!=null) {
            commChannel.setBaudrate(9600);
            commChannel.getSerialPort().setDTR(getDtrBehaviour()==1);
        } else {
            getDgcomConnection().signon();
        }

        if (modemPassword!=null) {
            getCommandFactory().modemUnlock(modemPassword);
        }
    }

    @Override
    protected void doDisconnect() throws IOException {
        getCommandFactory().logoff();
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return s4Profile.getProfileData(from, to, includeEvents);
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getCommandFactory().getDemandIntervalCommand().getProfileInterval()*60;
    }

    @Override
    protected void validateDeviceId() throws IOException {
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) {
            return;
        }
        String devId = getCommandFactory().getDeviceIDExtendedCommand().getDeviceID();
        if (devId.compareTo(getInfoTypeDeviceID()) == 0) {
            return;
        }
        throw new IOException("Device ID mismatch! meter devId="+devId+", configured devId="+getInfoTypeDeviceID());
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec("ModemPassword", PropertyTranslationKeys.S4_MODEM_PASSWORD, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "0").trim()));
        setInfoTypeSecurityLevel(Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "0").trim()));
        modemPassword = properties.getTypedProperty("ModemPassword");
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels();
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setDgcomConnection(new DGCOMConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel()));
        setCommandFactory(new CommandFactory(this));
        s4Profile = new S4Profile(this);
        registerMapperFactory = new RegisterMapperFactory(this);
        return getDgcomConnection();
    }

    @Override
    public Date getTime() throws IOException {
        return getCommandFactory().getTime();
    }

    @Override
    public void setTime() throws IOException {
        getCommandFactory().setTime();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:01 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "ProductFamily: "+getCommandFactory().getFirmwareVersionCommand().getProductFamily()+"\nFirmware version: "+getCommandFactory().getFirmwareVersionCommand().getFirmwareVersion()+"\nDGCOM version: "+getCommandFactory().getFirmwareVersionCommand().getDgcomVersion()+"\nDSP revision: "+getCommandFactory().getSerialNumberCommand().getDspRevision();
    }

    @Override
    public String getSerialNumber(){
        try {
            return String.valueOf(getCommandFactory().getSerialNumberCommand().getSerialNumber());
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
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
        return getRegisterMapperFactory().getRegisterMapper().getRegisterInfo();
    }

    public DGCOMConnection getDgcomConnection() {
        return dgcomConnection;
    }

    private void setDgcomConnection(DGCOMConnection dgcomConnection) {
        this.dgcomConnection = dgcomConnection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    RegisterMapperFactory getRegisterMapperFactory() {
        return registerMapperFactory;
    }

    @Override
    public int getInfoTypeRoundtripCorrection() {
        return super.getInfoTypeRoundtripCorrection();
    }

}