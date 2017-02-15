/*
 * GEKV.java
 *
 * Created on 17 oktober 2005, 8:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv;

import com.energyict.mdc.upl.UnsupportedException;
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
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.ansi.c12.AbstractResponse;
import com.energyict.protocolimpl.ansi.c12.C12Layer2;
import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.PSEMServiceFactory;
import com.energyict.protocolimpl.ansi.c12.ResponseIOException;
import com.energyict.protocolimpl.ansi.c12.procedures.StandardProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.tables.LoadProfileSet;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.ge.kv.tables.ManufacturerTableFactory;
import com.energyict.protocolimpl.meteridentification.AbstractManufacturer;
import com.energyict.protocolimpl.meteridentification.KV;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;

/**
 *
 * @author  Koen
 * @beginchanges
KV|04012007|Fix to allow unitless channels
KV|04012007|reengineered protocol to allow Eiserver 7 new channel properties
 * @endchanges
 */
public class GEKV extends AbstractProtocol implements C12ProtocolLink, SerialNumberSupport {

    private C12Layer2 c12Layer2;
    private PSEMServiceFactory psemServiceFactory;
    private StandardTableFactory standardTableFactory;
    private ManufacturerTableFactory manufacturerTableFactory;
    private StandardProcedureFactory standardProcedureFactory;

    private KV kv = new KV();
    private GEKVLoadProfile gekvLoadProfile;
    private ObisCodeInfoFactory obisCodeInfoFactory=null;

    private String c12User;
    private int c12UserId;

    private long timeStampAtLogon;
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;

    public GEKV(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading,new Date(),includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return gekvLoadProfile.getProfileData(from,to,includeEvents);
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        throw new IOException("Not implemented!");
    }

    @Override
    public AbstractManufacturer getManufacturer() {
        return kv;
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
    }

    @Override
    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        setTimeStampAtLogon(System.currentTimeMillis());
        if (commChannel!=null) {
            commChannel.setParams(9600,
                                  SerialCommunicationChannel.DATABITS_8,
                                  SerialCommunicationChannel.PARITY_NONE,
                                  SerialCommunicationChannel.STOPBITS_1);
            if (getDtrBehaviour() == 0) {
                commChannel.setDTR(false);
            } else if (getDtrBehaviour() == 1) {
                commChannel.setDTR(true);
            }
        }
        getPSEMServiceFactory().logOn(c12UserId,c12User,getInfoTypePassword(),getInfoTypeSecurityLevel(),PSEMServiceFactory.PASSWORD_BINARY);
    }

    @Override
    protected void doDisconnect() throws IOException {
        getPSEMServiceFactory().logOff();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec("C12User", PropertyTranslationKeys.GE_C12_USER, false));
        propertySpecs.add(this.integerSpec("C12UserId", PropertyTranslationKeys.GE_C12_USERID, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        setForcedDelay(Integer.parseInt(properties.getTypedProperty(PROP_FORCED_DELAY, "10").trim()));
        setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "64"));
        c12User = properties.getTypedProperty("C12User","");
        c12UserId = Integer.parseInt(properties.getTypedProperty("C12UserId", "0").trim());
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController);
        c12Layer2.initStates();
        psemServiceFactory = new PSEMServiceFactory(this);
        standardTableFactory = new StandardTableFactory(this);
        manufacturerTableFactory = new ManufacturerTableFactory(this);
        standardProcedureFactory = new StandardProcedureFactory(this);
        gekvLoadProfile = new GEKVLoadProfile(this);
        return c12Layer2;
    }

    @Override
    public void setTime() throws IOException {
        getStandardProcedureFactory().setDateTime();
    }

    @Override
    public Date getTime() throws IOException {
        try {
            return getStandardTableFactory().getTime();
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) {
                getLogger().warning("No clock table available, use system clock. Probably a demand only meter!");
            } else {
               throw e;
            }
        }
        catch(IOException e) {
            getLogger().warning(e.toString());
        }
        return new Date();

    }

    public int getNumberOfChannels() throws IOException {
        try {
            LoadProfileSet lps = getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet();
            if (lps!=null) {
                return lps.getNrOfChannelsSet()[0];
            } else {
                return 0;
            }
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) {
                getLogger().warning("No profile channels available. Probably a demand only meter!");
            } else {
                throw e;
            }
        }
        return 0;
    }

    @Override
    public String getSerialNumber() {
        try {
            return getStandardTableFactory().getManufacturerIdentificationTable().getManufacturerSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:23:40 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getStandardTableFactory().getManufacturerIdentificationTable().getManufacturer()+", "+
               getStandardTableFactory().getManufacturerIdentificationTable().getModel()+", "+
               "Firmware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getFwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getFwRevision()+", "+
               "Hardware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getHwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getHwRevision();
    }

    @Override
    public void setRegister(String name, String value) throws IOException {

    }

    @Override
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
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
        int skip=0;
        StringBuilder builder = new StringBuilder();
        builder.append("----------------------------------------------STANDARD TABLES--------------------------------------------------\n");
        while(true) {
            try {
                if (skip<=0) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getManufacturerIdentificationTable());}
                if (skip<=1) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConfigurationTable());}
                if (skip<=2) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEndDeviceModeAndStatusTable());}
                if (skip<=3) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDeviceIdentificationTable());}
                if (skip<=4) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualSourcesLimitingTable());}
                if (skip<=5) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDemandControlTable());}
                if (skip<=6) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataControlTable());}
                if (skip<=7) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConstantsTable());}
                if (skip<=8) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSourceDefinitionTable());}
                if (skip<=9) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualRegisterTable());}
                if (skip<=10) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataSelectionTable());}
                if (skip<=11) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCurrentRegisterDataTable());}
                if (skip<=12) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousSeasonDataTable());}
                if (skip<=13) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousDemandResetDataTable());}
                if (skip<=14) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSelfReadDataTable());}
                if (skip<=15) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPresentRegisterDataTable());}
                if (skip<=16) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualTimeAndTOUTable());}
                if (skip<=17) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getTimeOffsetTable());}
                if (skip<=18) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCalendarTable());}
                if (skip<=19) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getClockTable());}
                if (skip<=20) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLoadProfileTable());}
                if (skip<=21) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileControlTable());}
                if (skip<=22) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileStatusTable());}
                if (skip<=23) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLogTable());}
                if (skip<=24) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventsIdentificationTable());}
                if (skip<=25) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogControlTable());}
if (skip<=26) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogDataTable());}
                if (skip<=27) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogControlTable());}
if (skip<=28) { skip+=2;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableHeader());}
//if (skip<=29) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableEventEntries(145, 10));}
                if (skip<=30) { skip++;builder.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n"+getManufacturerTableFactory().getGEDeviceTable());}
                if (skip<=31) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterProgramConstants1());}
                if (skip<=32) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterProgramConstants2());}
                if (skip<=33) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getDisplayConfigurationTable());}
                if (skip<=34) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getScaleFactorTable());}
                if (skip<=35) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getElectricalServiceConfiguration());}
                if (skip<=36) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getElectricalServiceStatus());}
                if (skip<=37) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
                builder.append("Table not supported! "+e.toString()+"\n");
            }
        }
        return builder.toString();
    }


    @Override
    public C12Layer2 getC12Layer2() {
        return c12Layer2;
    }

    @Override
    public int getProfileInterval() throws IOException {
        try {
            LoadProfileSet lps = getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet();
            if (lps!=null) {
                return lps.getProfileIntervalSet()[0] * 60;
            } else {
                return getInfoTypeProfileInterval();
            }
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) {
                getLogger().warning("No profileinterval available. Probably a demand only meter!");
            } else {
                throw e;
            }
        }
        return getInfoTypeProfileInterval();
    }

    public TimeZone gettimeZone() {
        return super.getTimeZone();
    }

    @Override
    public PSEMServiceFactory getPSEMServiceFactory() {
        return psemServiceFactory;
    }

    public ManufacturerTableFactory getManufacturerTableFactory() {
        return manufacturerTableFactory;
    }

    @Override
    public StandardTableFactory getStandardTableFactory() {
        return standardTableFactory;
    }

    private StandardProcedureFactory getStandardProcedureFactory() {
        return standardProcedureFactory;
    }

    public ObisCodeInfoFactory getObisCodeInfoFactory() throws IOException {
        if (obisCodeInfoFactory == null) {
            obisCodeInfoFactory = new ObisCodeInfoFactory(this);
        }
        return obisCodeInfoFactory;
    }
    public int getMeterConfig() throws IOException {
        return getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
    }

    long getTimeStampAtLogon() {
        return timeStampAtLogon;
    }

    private void setTimeStampAtLogon(long timeStampAtLogon) {
        this.timeStampAtLogon = timeStampAtLogon;
    }

}