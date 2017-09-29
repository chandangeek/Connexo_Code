/*
 * S4.java
 *
 * Created on 17 oktober 2005, 8:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel;

import com.energyict.mdc.upl.SerialNumberSupport;
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
import com.energyict.protocolimpl.ansi.c12.C12Layer2;
import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.PSEMServiceFactory;
import com.energyict.protocolimpl.ansi.c12.procedures.StandardProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.itron.sentinel.logicalid.DataReadFactory;
import com.energyict.protocolimpl.itron.sentinel.procedures.ManufacturerProcedureFactory;
import com.energyict.protocolimpl.itron.sentinel.tables.ManufacturerTableFactory;
import com.energyict.protocolimpl.meteridentification.AbstractManufacturer;
import com.energyict.protocolimpl.meteridentification.SentinelItron;
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
 * @author James Fox
 */
public class Sentinel extends AbstractProtocol implements C12ProtocolLink, SerialNumberSupport {

    SentinelItron sentinelItron = new SentinelItron();
    SentinelLoadProfile sentinelLoadProfile;
    String c12User;
    int c12UserId;
    int maxNrPackets;
    boolean readLoadProfilesChunked = false;
    boolean convertRegisterReadsToKiloUnits = false;
    boolean readDemandsAndCoincidents = true;
    boolean readTiers = true;
    boolean limitRegisterReadSize = false;
    int reduceMaxNumberOfUomEntryBy;
    int chunkSize;
    int controlToggleBitMode;
    int eventChunkSize;

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    private SerialCommunicationChannel commChannel;
    private C12Layer2 c12Layer2;
    private PSEMServiceFactory psemServiceFactory;
    private StandardTableFactory standardTableFactory;
    private ManufacturerTableFactory manufacturerTableFactory;
    private StandardProcedureFactory standardProcedureFactory;
    private ManufacturerProcedureFactory manufacturerProcedureFactory;
    private DataReadFactory dataReadFactory=null;
    private ObisCodeInfoFactory obisCodeInfoFactory=null;

    public Sentinel(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return sentinelLoadProfile.getProfileData(from, to, includeEvents);
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        return getDataReadFactory().getConstantsDataRead().getCustomerSerialNumber();
    }

    @Override
    public AbstractManufacturer getManufacturer() {
        return sentinelItron;
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
    }

    @Override
    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
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

        if ((getInfoTypeSecurityLevel()!=2) && ((getInfoTypePassword()==null) || (getInfoTypePassword().compareTo("")==0))) {
            setInfoTypePassword(new String(new byte[]{0}));
        }

      //identify with node 1 before you can address other nodes
        if (c12Layer2.getIdentity()!=1) {
        	int targetIdentity = c12Layer2.getIdentity();
            c12Layer2.setIdentity(1);
            getPSEMServiceFactory().getIdentificationResponse().getIdentificationFeature0();
        	getPSEMServiceFactory().terminate();
        	c12Layer2.setIdentity(targetIdentity);
        }
        getPSEMServiceFactory().logOn(c12UserId,replaceSpaces(c12User),getInfoTypePassword(),getInfoTypeSecurityLevel(),PSEMServiceFactory.PASSWORD_ASCII, 128, maxNrPackets);
    }

    private String replaceSpaces(String c12User) {
        return c12User;
    }

    public int reduceMaxNumberOfUomEntryBy() {
        return reduceMaxNumberOfUomEntryBy;
    }

    @Override
    protected void doDisconnect() throws IOException {
        getPSEMServiceFactory().logOff();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec("C12User", PropertyTranslationKeys.ITRON_C12_USER, false));
        propertySpecs.add(this.integerSpec("C12UserId", PropertyTranslationKeys.ITRON_C12_USERID, false));
        propertySpecs.add(this.integerSpec("MaxNrPackets", PropertyTranslationKeys.ITRON_MAX_NR_PACKETS, false));
        propertySpecs.add(this.stringSpec("ReadLoadProfilesChunked", PropertyTranslationKeys.ITRON_READ_LOAD_PROFILE_CHUNKED, false));
        propertySpecs.add(this.integerSpec("ChunkSize", PropertyTranslationKeys.ITRON_CHUNK_SIZE, false));
        propertySpecs.add(this.stringSpec("ConvertRegisterReadsToKiloUnits", PropertyTranslationKeys.ITRON_CONVERT_REGISTER_READS_TO_KILO_UNITS, false));
        propertySpecs.add(this.booleanSpec("ReadDemandsAndCoincidents", PropertyTranslationKeys.ITRON_READ_DEMANDS_AND_COINCIDENTS, false));
        propertySpecs.add(this.booleanSpec("ReadTiers", PropertyTranslationKeys.ITRON_READ_TIERS, false));
        propertySpecs.add(this.booleanSpec("LimitRegisterReadSize", PropertyTranslationKeys.ITRON_LIMIT_REGISTER_READ_SIZE, false));
        propertySpecs.add(this.integerSpec("ReduceMaxNumberOfUomEntryBy", PropertyTranslationKeys.ITRON_REDUCE_MAX_NUMBER_OF_UOM_ENTRY_BY, false));
        propertySpecs.add(this.integerSpec("FrameControlToggleBitMode", PropertyTranslationKeys.ELSTER_FRAME_CONTROL_TOGGLE_BIT_MODE, false));
        propertySpecs.add(this.integerSpec("EventChunkSize", PropertyTranslationKeys.ITRON_EVENT_CHUNK_SIZE, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        setForcedDelay(properties.getTypedProperty(PROP_FORCED_DELAY, 10));
        setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "0"));
        c12User = properties.getTypedProperty("C12User", "");
        c12UserId = properties.getTypedProperty("C12UserId", 0);
        maxNrPackets = properties.getTypedProperty("MaxNrPackets", 1);
        readLoadProfilesChunked = Boolean.parseBoolean(properties.getTypedProperty("ReadLoadProfilesChunked", "false"));
        chunkSize = properties.getTypedProperty("ChunkSize", 19);
        convertRegisterReadsToKiloUnits = Boolean.parseBoolean(properties.getTypedProperty("ConvertRegisterReadsToKiloUnits", "false"));
        readDemandsAndCoincidents = Boolean.parseBoolean(properties.getTypedProperty("ReadDemandsAndCoincidents", "true"));
        readTiers = Boolean.parseBoolean(properties.getTypedProperty("ReadTiers", "true"));
        limitRegisterReadSize = Boolean.parseBoolean(properties.getTypedProperty("LimitRegisterReadSize", "false"));
        this.reduceMaxNumberOfUomEntryBy = properties.getTypedProperty("ReduceMaxNumberOfUomEntryBy", 0);
        this.controlToggleBitMode = properties.getTypedProperty("FrameControlToggleBitMode", 1);
        this.eventChunkSize = properties.getTypedProperty("EventChunkSize", 5);
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getLogger(), controlToggleBitMode);
        c12Layer2.initStates();
        psemServiceFactory = new PSEMServiceFactory(this);
        standardTableFactory = new StandardTableFactory(this);
        manufacturerTableFactory = new ManufacturerTableFactory(this);
        standardProcedureFactory = new StandardProcedureFactory(this);
        manufacturerProcedureFactory = new ManufacturerProcedureFactory(this);
        setDataReadFactory(new DataReadFactory(manufacturerTableFactory));
        sentinelLoadProfile = new SentinelLoadProfile(this, readLoadProfilesChunked, chunkSize, eventChunkSize);
        return c12Layer2;
    }

    @Override
    public void setTime() throws IOException {
        getStandardProcedureFactory().setDateTime();
    }

    @Override
    public Date getTime() throws IOException {
        return getStandardTableFactory().getTime();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels();
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
    public String getProtocolDescription() {
        return "Itron/Schlumberger Sentinel ANSI";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-12-29 12:41:11 +0100 (Th, 29 Dec 2016)$";
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
        StringBuilder strBuff = new StringBuilder();

        strBuff.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n");

        strBuff.append(getDataReadFactory().getConstantsDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCapabilitiesDataRead()).append("\n");

        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
            strBuff.append(getDataReadFactory().getClockRelatedDataRead()).append("\n");
        }

        strBuff.append(getDataReadFactory().getQuantityIdentificationDataRead()).append("\n");

        strBuff.append(getDataReadFactory().getCurrentStateDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCurrentEnergyDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCurrentDemandDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getCurrentCumulativeDemandDataRead()).append("\n");
        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
            strBuff.append(getDataReadFactory().getCurrentDemandTimeOfOccurenceDataRead()).append("\n");
        }

        strBuff.append(getDataReadFactory().getLastBillingPeriodStateDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodEnergyDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodDemandDataRead()).append("\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodCumulativeDemandDataRead()).append("\n");
        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
            strBuff.append(getDataReadFactory().getLastBillingPeriodDemandTimeOfOccurenceDataRead()).append("\n");
        }


        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates() > 0) {
            strBuff.append(getDataReadFactory().getLastSeasonStateDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSeasonEnergyDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSeasonDemandDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSeasonCumulativeDemandDataRead()).append("\n");
            if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
                strBuff.append(getDataReadFactory().getLastSeasonDemandTimeOfOccurenceDataRead()).append("\n");
            }
        }

        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasValidSelfReadData()) {
            strBuff.append(getDataReadFactory().getLastSelfReadStateDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSelfReadEnergyDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSelfReadDemandDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLastSelfReadCumulativeDemandDataRead()).append("\n");
            if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock()) {
                strBuff.append(getDataReadFactory().getLastSelfReadDemandTimeOfOccurenceDataRead()).append("\n");
            }
        }

        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels() > 0) {
            strBuff.append(getDataReadFactory().getLoadProfileQuantitiesDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLoadProfilePulseWeightsDataRead()).append("\n");
            strBuff.append(getDataReadFactory().getLoadProfilePreliminaryDataRead()).append("\n");

        }

        strBuff.append("----------------------------------------------STANDARD TABLES--------------------------------------------------\n");

        while(true) {
            try {
                if (skip<=0) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getDeviceIdentificationTable());}
                if (skip<=1) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getConfigurationTable());}
                if (skip<=2) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getEndDeviceModeAndStatusTable());}
                if (skip<=3) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getManufacturerIdentificationTable());}
                if (skip<=4) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getActualSourcesLimitingTable());}
                if (skip<=5) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getUnitOfMeasureEntryTable(true));}

                if (skip<=6) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getDemandControlTable());}
                if (skip<=7) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getDataControlTable());}
                if (skip<=8) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getConstantsTable(true));}
                if (skip<=9) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getSourceDefinitionTable(true));}
                if (skip<=10) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getActualRegisterTable());}
                if (skip<=11) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getDataSelectionTable());}
                if (skip<=12) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getCurrentRegisterDataTable(true));}
                if (skip<=13) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getPreviousSeasonDataTable(true));}
                if (skip<=14) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getPreviousDemandResetDataTable(true));}
                if (skip<=15) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getSelfReadDataTable(true));}
                if (skip<=16) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getPresentRegisterDataTable(true));}
                if (skip<=17) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getActualTimeAndTOUTable());}
                if (skip<=18) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getTimeOffsetTable());}
                if (skip<=19) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getCalendarTable());}
                if (skip<=20) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getClockTable());}
                if (skip<=21) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getActualLoadProfileTable());}
                if (skip<=22) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getLoadProfileControlTable());}
                if (skip<=23) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getLoadProfileStatusTable());}
                if (skip<=24) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getActualLogTable());}
                if (skip<=25) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n")
                            .append(getStandardTableFactory().getEventsIdentificationTable());}
                if (skip<=26) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getHistoryLogControlTable());}
                if (skip<=27) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getHistoryLogDataTable());}
                if (skip<=28) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getEventLogControlTable());}
                if (skip<=29) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getStandardTableFactory().getEventLogDataTableHeader());}
                if (skip<=36) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
                strBuff.append("Table not supported! ").append(e.toString()).append("\n");
            }
        }
        return strBuff.toString();
    }

    @Override
    public C12Layer2 getC12Layer2() {
        return c12Layer2;
    }

    @Override
    public int getProfileInterval() throws IOException {
        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()>0) {
            return getDataReadFactory().getLoadProfilePreliminaryDataRead().getLoadProfileIntervalLength() * 60;
        } else {
            return getInfoTypeProfileInterval();
        }
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

    public StandardProcedureFactory getStandardProcedureFactory() {
        return standardProcedureFactory;
    }

    public ManufacturerProcedureFactory getManufacturerProcedureFactory() {
        return manufacturerProcedureFactory;
    }

    @Override
    public int getMeterConfig() throws IOException {
        return -1; //getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
    }

    public ObisCodeInfoFactory getObisCodeInfoFactory() throws IOException {
        if (obisCodeInfoFactory == null) {
            obisCodeInfoFactory=new ObisCodeInfoFactory(this, convertRegisterReadsToKiloUnits, readDemandsAndCoincidents, readTiers, limitRegisterReadSize);
        }
        return obisCodeInfoFactory;
    }

    public DataReadFactory getDataReadFactory() {
        return dataReadFactory;
    }

    public void setDataReadFactory(DataReadFactory dataReadFactory) {
        this.dataReadFactory = dataReadFactory;
    }

}