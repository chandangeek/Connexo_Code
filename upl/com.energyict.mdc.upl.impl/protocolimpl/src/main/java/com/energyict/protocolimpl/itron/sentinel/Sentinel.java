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

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

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
    int chunkSize;

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
    SerialCommunicationChannel commChannel;
    private C12Layer2 c12Layer2;
    private PSEMServiceFactory psemServiceFactory;
    private StandardTableFactory standardTableFactory;
    private ManufacturerTableFactory manufacturerTableFactory;
    private StandardProcedureFactory standardProcedureFactory;
    private ManufacturerProcedureFactory manufacturerProcedureFactory;
    private DataReadFactory dataReadFactory=null;
    private ObisCodeInfoFactory obisCodeInfoFactory=null;

    /**
     * Creates a new instance of Sentinel
     */
    public Sentinel() {
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return sentinelLoadProfile.getProfileData(from, to, includeEvents);
    }

    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        return getDataReadFactory().getConstantsDataRead().getCustomerSerialNumber();

    }

    public AbstractManufacturer getManufacturer() {
        return sentinelItron;
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
    }

    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
        if (commChannel!=null) {

            commChannel.setParams(9600,
                    SerialCommunicationChannel.DATABITS_8,
                    SerialCommunicationChannel.PARITY_NONE,
                    SerialCommunicationChannel.STOPBITS_1);
            if (getDtrBehaviour() == 0)
                commChannel.setDTR(false);
            else if (getDtrBehaviour() == 1)
                commChannel.setDTR(true);
        }

        if ((getInfoTypeSecurityLevel()!=2) && ((getInfoTypePassword()==null) || (getInfoTypePassword().compareTo("")==0)))
            setInfoTypePassword(new String(new byte[]{0}));

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

    protected void doDisConnect() throws IOException {
        getPSEMServiceFactory().logOff();
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","10").trim()));
        setInfoTypeNodeAddress(properties.getProperty(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(),"0"));
        c12User = properties.getProperty("C12User","");
        c12UserId = Integer.parseInt(properties.getProperty("C12UserId","0").trim());
        maxNrPackets = Integer.parseInt(properties.getProperty("MaxNrPackets", "1"), 16);
        readLoadProfilesChunked = Boolean.parseBoolean(properties.getProperty("ReadLoadProfilesChunked", "false"));
        chunkSize = Integer.parseInt(properties.getProperty("ChunkSize", "19"));
        convertRegisterReadsToKiloUnits = Boolean.parseBoolean(properties.getProperty("ConvertRegisterReadsToKiloUnits", "false"));
    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList(
                "C12User",
                "C12UserId",
                "MaxNrPackets",
                "ReadLoadProfilesChunked",
                "ChunkSize",
                "ConvertRegisterReadsToKiloUnits");
    }

    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController);
        c12Layer2.initStates();
        psemServiceFactory = new PSEMServiceFactory(this);
        standardTableFactory = new StandardTableFactory(this);
        manufacturerTableFactory = new ManufacturerTableFactory(this);
        standardProcedureFactory = new StandardProcedureFactory(this);
        manufacturerProcedureFactory = new ManufacturerProcedureFactory(this);
        setDataReadFactory(new DataReadFactory(manufacturerTableFactory));
        sentinelLoadProfile = new SentinelLoadProfile(this, readLoadProfilesChunked, chunkSize);
        return c12Layer2;
    }

    public void setTime() throws IOException {
        getStandardProcedureFactory().setDateTime();
//        throw new UnsupportedException("NOT IMPLEMENTED YET!");
    }

    public Date getTime() throws IOException {
        return getStandardTableFactory().getTime();
        //return getDataReadFactory().getCurrentStateDataRead().getCurrentTimeDate();
    }

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

    public String getProtocolVersion() {
        return "$Date: 2016-09-08 09:36:35 +0300 (Thu, 08 Sep 2016)$";
    }

    public String getFirmwareVersion() throws IOException {
        return getStandardTableFactory().getManufacturerIdentificationTable().getManufacturer()+", "+
                getStandardTableFactory().getManufacturerIdentificationTable().getModel()+", "+
                "Firmware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getFwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getFwRevision()+", "+
                "Hardware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getHwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getHwRevision();
    }

    /*
     * Override this method if the subclass wants to set a specific register
     */
    public void setRegister(String name, String value) throws IOException {

    }

    /*
     * Override this method if the subclass wants to get a specific register
     */
    public String getRegister(String name) throws IOException {
        throw new UnsupportedException();
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
        int skip=0;
        StringBuilder strBuff = new StringBuilder();

/*
        strBuff.append(getStandardTableFactory().getConfigurationTable());
        strBuff.append(getStandardTableFactory().getActualLoadProfileTable());
        strBuff.append(getStandardTableFactory().getLoadProfileControlTable());
        strBuff.append(getStandardTableFactory().getLoadProfileStatusTable());
//        strBuff.append(getStandardTableFactory().getLoadProfileDataSetTable(0,0));
    	boolean skipIt=true;
    	if (skipIt) return strBuff.toString();
*/


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

        //strBuff.append(getDataReadFactory().getMeterMultiplierDataRead()+"\n");

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
//                if (skip<=31) { skip++;strBuff.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n"+getManufacturerTableFactory().getFeatureParameters());}
//                if (skip<=32) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getServiceTypeTable());}
//                if (skip<=33) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterFactors());}
//                if (skip<=34) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterStatus());}
//                if (skip<=35) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getS4Configuration());}
                if (skip<=36) { skip++;
                    strBuff.append("------------------------------------------------------------------------------------------------\n").append(getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
//e.printStackTrace();       // KV_DEBUG
                strBuff.append("Table not supported! ").append(e.toString()).append("\n");
            }
        }




        //System.out.println(strBuff.toString());
        return strBuff.toString();
    }

    /****************************************************************************************************************
     * Implementing C12ProtocolLink interface
     ****************************************************************************************************************/

    public C12Layer2 getC12Layer2() {
        return c12Layer2;
    }

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

    public PSEMServiceFactory getPSEMServiceFactory() {
        return psemServiceFactory;
    }

    public ManufacturerTableFactory getManufacturerTableFactory() {
        return manufacturerTableFactory;
    }

    public StandardTableFactory getStandardTableFactory() {
        return standardTableFactory;
    }

    public StandardProcedureFactory getStandardProcedureFactory() {
        return standardProcedureFactory;
    }

    public ManufacturerProcedureFactory getManufacturerProcedureFactory() {
        return manufacturerProcedureFactory;
    }

    public int getMeterConfig() throws IOException {
        return -1; //getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
    }

    public ObisCodeInfoFactory getObisCodeInfoFactory() throws IOException {
        if (obisCodeInfoFactory == null) {
            obisCodeInfoFactory = new ObisCodeInfoFactory(this, convertRegisterReadsToKiloUnits);
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
