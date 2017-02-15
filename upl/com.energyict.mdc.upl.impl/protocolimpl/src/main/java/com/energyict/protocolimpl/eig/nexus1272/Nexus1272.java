package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.cbo.Unit;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.eig.nexus1272.command.AbstractCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.AuthenticationCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.command.ReadCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.SetTimeCommand;
import com.energyict.protocolimpl.eig.nexus1272.parse.LinePoint;
import com.energyict.protocolimpl.eig.nexus1272.parse.NexusDataParser;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySetting;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySettingFactory;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Nexus1272 extends AbstractProtocol implements SerialNumberSupport {

    private NexusProtocolConnection connection;
    private OutputStream outputStream;

    private List<LinePoint> masterlpMap = new ArrayList<>();
    private List<LinePoint> mtrlpMap = null;
    private List<LinePoint> chnlpMap = new ArrayList<>();
    private long start;
    private ScaledEnergySettingFactory sesf;
    private String channelMapping;
    private String password;
    private int intervalLength;
    private boolean isDeltaWired = false;

    public Nexus1272(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void doConnect() throws IOException {
        start = System.currentTimeMillis();
        try {
            authenticate(2);
        } catch (IOException ioe) {
            try {
                authenticate(1);
            } catch (IOException ioe2) {
                throw new IOException("Could not authenticate with meter, check password :: " + ioe2.getMessage());
            }
        }
    }

    @Override
    protected void doDisconnect() throws IOException {
    }

    @Override
    protected ProtocolConnection doInit(
            InputStream inputStream,
            OutputStream outputStream, int timeoutProperty,
            int protocolRetriesProperty, int forcedDelay, int echoCancelling,
            int protocolCompatible, Encryptor encryptor,
            HalfDuplexController halfDuplexController) throws IOException {
        connection = new NexusProtocolConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, protocolCompatible, encryptor, getLogger());
        this.outputStream = outputStream;
        intervalLength = getInfoTypeProfileInterval();
        return connection;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(new ChannelMappingPropertySpec("NexusChannelMapping", false, getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.EDMI_NEXUS_CHANNEL_MAPPING).format(), getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.EDMI_NEXUS_CHANNEL_MAPPING_DESCRIPTION).format()));
        propertySpecs.add(this.stringSpec("Delta Wired", PropertyTranslationKeys.EDMI_DELTA_WIRED, false));
        return propertySpecs;
    }

    @Override
    protected boolean passwordIsRequired() {
        return true;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        channelMapping = properties.getTypedProperty("NexusChannelMapping", "");
        if (channelMapping != null && !channelMapping.isEmpty()) {
            chnlpMap = ChannelMappingPropertySpec.parse(channelMapping);
        }

        if ("1".equals(properties.getTypedProperty("Delta Wired", "0"))) {
            isDeltaWired = true;
        }

        String str = getInfoTypePassword();
        StringBuilder passwordBuilder = new StringBuilder();
        passwordBuilder.append(str);
        int length = 10;
        if (length >= str.length()) {
            for (int i = 0; i < (length - str.length()); i++) {
                passwordBuilder.append(' ');
            }
        }
        password = passwordBuilder.toString();
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String fwVersion = "";
        Command command = NexusCommandFactory.getFactory().getCommBootVersionCommand();
        outputStream.write(command.build());
        baos.write(connection.receiveWriteResponse(command).toByteArray());

        command = NexusCommandFactory.getFactory().getCommRunVersionCommand();
        outputStream.write(command.build());
        baos.write(connection.receiveWriteResponse(command).toByteArray());

        command = NexusCommandFactory.getFactory().getDSPBootVersionCommand();
        outputStream.write(command.build());
        baos.write(connection.receiveWriteResponse(command).toByteArray());

        command = NexusCommandFactory.getFactory().getDSPRunVersionCommand();
        outputStream.write(command.build());
        baos.write(connection.receiveWriteResponse(command).toByteArray());

        NexusDataParser ndp = new NexusDataParser(baos.toByteArray());
        fwVersion += ndp.parseF2() + ".";
        fwVersion += ndp.parseF2() + ".";
        fwVersion += ndp.parseF2() + ".";
        fwVersion += ndp.parseF2();

        return fwVersion;
    }

    @Override
    public String getSerialNumber() {
        try {
            Command command = NexusCommandFactory.getFactory().getSerialNumberCommand();
            outputStream.write(command.build());
            byte[] data = connection.receiveWriteResponse(command).toByteArray();
            NexusDataParser ndp = new NexusDataParser(data);
            return ndp.parseSN();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
    }

    @Override
    public Date getTime() throws IOException {
        Command command = NexusCommandFactory.getFactory().getGetTimeCommand();
        outputStream.write(command.build());
        NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(command).toByteArray());
        return ndp.parseF3();
    }

    @Override
    public void setTime() throws IOException {
        authenticate(2);
        Command command = NexusCommandFactory.getFactory().getSetTimeCommand();
        ((SetTimeCommand) command).setTimeZone(getTimeZone());
        outputStream.write(command.build());
        connection.receiveWriteResponse(command);
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (mtrlpMap == null) {
            Command command = NexusCommandFactory.getFactory().getDataPointersCommand();
            outputStream.write(command.build());
            mtrlpMap = processPointers(connection.receiveWriteResponse(command).toByteArray());
        }
        int numChannels = mtrlpMap.size();
        return numChannels;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if ("".equals(channelMapping)) {
            throw new IOException("NexusChannelMapping custom property must be set to read profile data");
        }

        sesf = new ScaledEnergySettingFactory(outputStream, connection);

        ProfileData profileData = new ProfileData();
        buildChannelInfo(profileData);
        buildIntervalData(profileData, from, to);
        if (includeEvents) {
            buildEventLog(profileData, from);
            profileData.applyEvents(getProfileInterval() / 60);
        }

        profileData.sort();
        return profileData;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (sesf == null) {
            sesf = new ScaledEnergySettingFactory(outputStream, connection);
        }
        ObisCodeMapper ocm = new ObisCodeMapper(NexusCommandFactory.getFactory(), connection, outputStream, sesf, isDeltaWired);
        return ocm.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    private void buildEventLog(ProfileData profileData, Date from) throws IOException {
        LogReader lr = new SystemLogReader(outputStream, connection);
        byte[] byteArray = lr.readLog(from);
        lr.parseLog(byteArray, profileData, from, intervalLength);
        List<MeterEvent> meterEvents = ((SystemLogReader) lr).getMeterEvents();


        lr = new LimitTriggerLogReader(outputStream, connection);
        byteArray = lr.readLog(from);
        lr.parseLog(byteArray, profileData, from, intervalLength);
        meterEvents.addAll(((LimitTriggerLogReader) lr).getMeterEvents());

        //check sanity register
        ReadCommand c = (ReadCommand) NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
        c.setStartAddress(AbstractCommand.intToByteArray(0xD000));
        c.setNumRegisters(AbstractCommand.intToByteArray(1));
        outputStream.write(c.build());
        NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
        if (ndp.parseF51() != 0) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.METER_ALARM, "Sanity Register not 0"));
        }

        //check low battery register
        c = (ReadCommand) NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
        c.setStartAddress(AbstractCommand.intToByteArray(0x6039));
        c.setNumRegisters(AbstractCommand.intToByteArray(1));
        outputStream.write(c.build());
        ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
        if ((ndp.parseF51() & 0x8000) == 0x8000) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.METER_ALARM, "Battery Low"));
        }

        //check Nexus Comm operation indicator
        c = (ReadCommand) NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
        c.setStartAddress(AbstractCommand.intToByteArray(0xFF81));
        c.setNumRegisters(AbstractCommand.intToByteArray(1));
        outputStream.write(c.build());
        ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
        if ((ndp.parseF51() & 0x0001) == 0x0001) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.METER_ALARM, "RAM Failure"));
        }

        //check Nexus DSP operation indicator
        c = (ReadCommand) NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
        c.setStartAddress(AbstractCommand.intToByteArray(0xFF86));
        c.setNumRegisters(AbstractCommand.intToByteArray(1));
        outputStream.write(c.build());
        ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
        if ((ndp.parseF51() & 0x0001) == 0x0001) {
            meterEvents.add(new MeterEvent(new Date(), MeterEvent.METER_ALARM, "RAM Failure"));
        }

        profileData.setMeterEvents(meterEvents);
    }

    private void buildIntervalData(ProfileData profileData, Date from, Date to) throws IOException {
        LogReader lr = new Historical2LogReader(outputStream, connection, mtrlpMap, masterlpMap, sesf);
        byte[] ba = lr.readLog(from);
        lr.parseLog(ba, profileData, from, intervalLength);
    }

    private void buildChannelInfo(ProfileData profileData) throws IOException {
        if (mtrlpMap == null) {
            Command command = NexusCommandFactory.getFactory().getDataPointersCommand();
            outputStream.write(command.build());
            mtrlpMap = processPointers(connection.receiveWriteResponse(command).toByteArray());
        }

        buildMasterLPMap();

        for (LinePoint lp : masterlpMap) {
            ScaledEnergySetting ses = null;
            Unit unit = Unit.getUndefined();
            if (lp.isScaled()) {
                ses = sesf.getScaledEnergySetting(lp);
                unit = ses.getUnit();
            }
            int channel = lp.getChannel() - 1;
            ChannelInfo ci = new ChannelInfo(channel, "Nexus1272_channel_" + channel, unit);
            try {
                profileData.addChannel(ci);
            } catch (IndexOutOfBoundsException ioe) {
                throw new IndexOutOfBoundsException("Channel mapping custom property must start at 1 and not skip channels");
            }
        }

    }

    private void buildMasterLPMap() throws IOException {
        for (LinePoint lp : chnlpMap) {
            boolean notAdded = true;
            for (LinePoint lp2 : mtrlpMap) {
                if (lp.getLine() == lp2.getLine() && lp.getPoint() == lp2.getPoint()) {
                    notAdded = false;
                    masterlpMap.add(lp);
                    break;
                }
            }
            if (notAdded) {
                throw new IOException("Line " + lp.getLine() + " Point " + lp.getPoint() + " not found in the meter's log");
            }
        }

    }

    private List<LinePoint> processPointers(byte[] ba) throws IOException {
        int offset = 0;
        List<LinePoint> lpMap = new ArrayList<LinePoint>();
        while (offset <= ba.length - 4) {
            if (ba[offset] == -1 && ba[offset + 1] == -1) {
                offset += 4;
                continue;
            }
            int line = ProtocolUtils.getInt(ba, offset, 2);
            offset += 2;
            int point = ProtocolUtils.getInt(ba, offset, 1);
            offset += 2;
            lpMap.add(new LinePoint(line, point));
        }
        return lpMap;
    }

    private boolean authenticate(int level) throws IOException {

        Command command = NexusCommandFactory.getFactory().getAuthenticationCommand();
        ((AuthenticationCommand) command).setPassword(password.getBytes());
        outputStream.write(command.build());
        connection.receiveWriteResponse(command);

        command = NexusCommandFactory.getFactory().getVerifyAuthenticationCommand();
        outputStream.write(command.build());
        byte[] data = connection.receiveWriteResponse(command).toByteArray();


        switch (level) {
            case 1:
                if (data[data.length - 1] != 0x04 && data[data.length - 1] != 0x03) {
                    throw new IOException("Level 1 authentication failed");
                }
                break;
            case 2:
                if (data[data.length - 1] != 0x04) {
                    throw new IOException("Level 2 authentication failed");
                }
                break;
            default:
                if (data[data.length - 1] != 0x04) {
                    throw new IOException("Level 2 authentication failed (level requested invalid)");
                }
        }

        return true;
    }

}