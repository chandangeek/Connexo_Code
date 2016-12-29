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

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;
/**
 *
 * @author  Koen
 * @author James Fox
 * @beginchanges
 * @endchanges
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

    static public void main(String[] args) {
        try {
            String[] phones = new String[]{"2093343019", "166.161.129.236:6100"};
            String[] passwords = new String[]{"READ", "READ"};
            String[] securityLevels = new String[]{"1", "1"};
            int select = 1;
            String dialInternational = "0001";

            // ********************** Dialer **********************
            //Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            //Dialer dialer = DialerFactory.getOpticalDialer().newDialer();

            Dialer dialer = DialerFactory.get("IPDIALER").newDialer();
            dialer.init("TCP01");
            dialer.connect(phones[select], 90000);

//            Dialer dialer = DialerFactory.getDefault().newDialer();
//            dialer.init("COM1"); //,"AT&FM0E0Q0V1&C1&D2");
//            dialer.getSerialCommunicationChannel().setParams(9600,
//                                                             SerialCommunicationChannel.DATABITS_8,
//                                                             SerialCommunicationChannel.PARITY_NONE,
//                                                             SerialCommunicationChannel.STOPBITS_1);
//            dialer.connect(dialInternational+phones[select],90000);
//

            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "900");
            //properties.setProperty(MeterProtocol.NODEID,"0");
            //properties.setProperty(MeterProtocol.ADDRESS,"1");
            properties.setProperty(MeterProtocol.PASSWORD, passwords[select]);
            properties.setProperty("SecurityLevel", securityLevels[select]);
            properties.setProperty("ChannelMap", "1,1");
            properties.setProperty("C12UserId", "2");

            // ********************** EictRtuModbus **********************
            Sentinel sentinel = new Sentinel();
            if (DialerMarker.hasOpticalMarker(dialer))
                sentinel.enableHHUSignOn(dialer.getSerialCommunicationChannel());

            sentinel.setHalfDuplexController(dialer.getHalfDuplexController());
            sentinel.setProperties(properties);
            sentinel.init(dialer.getInputStream(), dialer.getOutputStream(), TimeZone.getTimeZone("ECT"), Logger.getLogger("name"));
            sentinel.connect();


            System.out.println(sentinel.getDataReadFactory().getConstantsDataRead());


            System.out.println(sentinel.getStandardTableFactory().getDeviceIdentificationTable());

            System.out.println(sentinel.getStandardTableFactory().getManufacturerIdentificationTable());
            System.out.println(sentinel.getStandardTableFactory().getConfigurationTable());
            System.out.println(sentinel.getStandardTableFactory().getEndDeviceModeAndStatusTable());
//            System.out.println(sentinel.getManufacturerTableFactory().getGEDeviceTable());
            System.out.println(sentinel.getStandardTableFactory().getDeviceIdentificationTable());
//
            System.out.println(sentinel.getStandardTableFactory().getClockTable());

            //byte[] password = {(byte)0x5f,(byte)0x29,(byte)0x6e,(byte)0x00,(byte)0x29,(byte)0xfc,(byte)0x7c,(byte)0x90,(byte)0xce,(byte)0xef,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20};
            //byte[] password = {(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA};
            //byte[] password = {(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB};
//            byte[] password = {(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6};
//            sentinel.getPSEMServiceFactory().secure(password);

            //System.out.println(sentinel.getManufacturerTableFactory().getMeterProgramConstants1());
            try {
                System.out.println(sentinel.getStandardTableFactory().getActualLogTable());
            } catch (IOException e) {
                System.out.println("Table not supported! " + e.toString());
            }
            try {
                System.out.println(sentinel.getStandardTableFactory().getEventsIdentificationTable());
            } catch (IOException e) {
                System.out.println("Table not supported! " + e.toString());
            }

//            System.out.println(sentinel.getManufacturerTableFactory().getMeterProgramConstants2());
//            System.out.println(sentinel.getManufacturerTableFactory().getDisplayConfigurationTable());
//            System.out.println(sentinel.getManufacturerTableFactory().getScaleFactorTable());
//            System.out.println(sentinel.getManufacturerTableFactory().getElectricalServiceConfiguration());
//            System.out.println(sentinel.getManufacturerTableFactory().getElectricalServiceStatus());
//
            System.out.println(sentinel.getStandardTableFactory().getActualSourcesLimitingTable());
            System.out.println(sentinel.getStandardTableFactory().getDemandControlTable());
            System.out.println(sentinel.getStandardTableFactory().getDataControlTable());
            System.out.println(sentinel.getStandardTableFactory().getConstantsTable());
            System.out.println(sentinel.getStandardTableFactory().getSourceDefinitionTable());
            System.out.println(sentinel.getStandardTableFactory().getActualRegisterTable());
            System.out.println(sentinel.getStandardTableFactory().getDataSelectionTable());
            System.out.println(sentinel.getStandardTableFactory().getCurrentRegisterDataTable());
            System.out.println(sentinel.getStandardTableFactory().getPreviousSeasonDataTable());
            System.out.println(sentinel.getStandardTableFactory().getPreviousDemandResetDataTable());
            System.out.println(sentinel.getStandardTableFactory().getSelfReadDataTable());
            //System.out.println(sentinel.getStandardTableFactory().getPresentRegisterSelectionTable());
            System.out.println(sentinel.getStandardTableFactory().getPresentRegisterDataTable());
            System.out.println(sentinel.getStandardTableFactory().getActualTimeAndTOUTable());
            System.out.println(sentinel.getStandardTableFactory().getTimeOffsetTable());
            System.out.println(sentinel.getStandardTableFactory().getCalendarTable());


            // set time
            //sentinel.setTime();

//            System.out.println(sentinel.getStandardTableFactory().getClockStateTable());
//            System.out.println(sentinel.getStandardTableFactory().getActualLoadProfileTable());
//            System.out.println(sentinel.getStandardTableFactory().getLoadProfileControlTable());
//            System.out.println(sentinel.getStandardTableFactory().getLoadProfileStatusTable());


//            byte[] password = {(byte)0x5f,(byte)0x29,(byte)0x6e,(byte)0x00,(byte)0x29,(byte)0xfc,(byte)0x7c,(byte)0x90,(byte)0xce,(byte)0xef,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20};
            //byte[] password = {(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA};
            //byte[] password = {(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB};
//            byte[] password = {(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6};
//            sentinel.getPSEMServiceFactory().secure(password);


//System.out.println("KV_DEBUG> program manufacturer specific table");
//sentinel.getPSEMServiceFactory().fullWrite(66, new byte[]{0,0,(byte)(1667/256),(byte)(1667%256)});
//sentinel.getManufacturerTableFactory().getMeterProgramConstants1().setTableData(new byte[]{0,0,(byte)(1667/256),(byte)(1667%256)});
//sentinel.getManufacturerTableFactory().getMeterProgramConstants1().transfer();


//            Calendar cal = Calendar.getInstance();
//            cal.add(Calendar.DAY_OF_MONTH,-4);
//            System.out.println(sentinel.getProfileData(cal.getTime(),true));

            System.out.println(sentinel.getFirmwareVersion());


            sentinel.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

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

//        byte[] temp = new byte[10];
//        for (int i=0;i<temp.length;i++)
//            temp[i] = 0x20;
//        System.arraycopy(c12User.getBytes(), 0, temp, 0, c12User.getBytes().length);
//        String user = new String(temp);
//        return user.replace(' ', '\0');
    }

    public int reduceMaxNumberOfUomEntryBy() {
        return reduceMaxNumberOfUomEntryBy;
    }

    protected void doDisConnect() throws IOException {
        getPSEMServiceFactory().logOff();
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","10").trim()));
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"0"));
        c12User = properties.getProperty("C12User","");
        c12UserId = Integer.parseInt(properties.getProperty("C12UserId","0").trim());
        maxNrPackets = Integer.parseInt(properties.getProperty("MaxNrPackets", "1"), 16);
        readLoadProfilesChunked = Boolean.parseBoolean(properties.getProperty("ReadLoadProfilesChunked", "false"));
        chunkSize = Integer.parseInt(properties.getProperty("ChunkSize", "19"));
        convertRegisterReadsToKiloUnits = Boolean.parseBoolean(properties.getProperty("ConvertRegisterReadsToKiloUnits", "false"));
        readDemandsAndCoincidents = Boolean.parseBoolean(properties.getProperty("ReadDemandsAndCoincidents", "true"));
        readTiers = Boolean.parseBoolean(properties.getProperty("ReadTiers", "true"));
        limitRegisterReadSize = Boolean.parseBoolean(properties.getProperty("LimitRegisterReadSize", "false"));
        reduceMaxNumberOfUomEntryBy = Integer.parseInt(properties.getProperty("ReduceMaxNumberOfUomEntryBy", "0"));
        this.controlToggleBitMode = Integer.parseInt(properties.getProperty("FrameControlToggleBitMode", "1"));
        this.eventChunkSize = Integer.parseInt(properties.getProperty("EventChunkSize", "5"));
    }

    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("C12User");
        result.add("C12UserId");
        result.add("MaxNrPackets");
        result.add("ReadLoadProfilesChunked");
        result.add("ChunkSize");
        result.add("ConvertRegisterReadsToKiloUnits");
        result.add("ReadDemandsAndCoincidents");
        result.add("ReadTiers");
        result.add("LimitRegisterReadSize");
        result.add("ReduceMaxNumberOfUomEntryBy");
        result.add("FrameControlToggleBitMode");
        result.add("EventChunkSize");
        return result;
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
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
        return "$Date: 2016-12-29 12:41:11 +0100 (Th, 29 Dec 2016)$";
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
    public void setRegister(String name, String value) throws IOException, UnsupportedException {

    }

    /*
     * Override this method if the subclass wants to get a specific register
     */
    public String getRegister(String name) throws IOException, NoSuchRegisterException {
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
        StringBuffer strBuff = new StringBuffer();

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

        strBuff.append(getDataReadFactory().getConstantsDataRead()+"\n");
        strBuff.append(getDataReadFactory().getCapabilitiesDataRead()+"\n");

        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock())
            strBuff.append(getDataReadFactory().getClockRelatedDataRead()+"\n");

        strBuff.append(getDataReadFactory().getQuantityIdentificationDataRead()+"\n");

        strBuff.append(getDataReadFactory().getCurrentStateDataRead()+"\n");
        strBuff.append(getDataReadFactory().getCurrentEnergyDataRead()+"\n");
        strBuff.append(getDataReadFactory().getCurrentDemandDataRead()+"\n");
        strBuff.append(getDataReadFactory().getCurrentCumulativeDemandDataRead()+"\n");
        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock())
            strBuff.append(getDataReadFactory().getCurrentDemandTimeOfOccurenceDataRead()+"\n");

        strBuff.append(getDataReadFactory().getLastBillingPeriodStateDataRead()+"\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodEnergyDataRead()+"\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodDemandDataRead()+"\n");
        strBuff.append(getDataReadFactory().getLastBillingPeriodCumulativeDemandDataRead()+"\n");
        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock())
            strBuff.append(getDataReadFactory().getLastBillingPeriodDemandTimeOfOccurenceDataRead()+"\n");


        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfTOURates() > 0) {
            strBuff.append(getDataReadFactory().getLastSeasonStateDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLastSeasonEnergyDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLastSeasonDemandDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLastSeasonCumulativeDemandDataRead()+"\n");
            if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock())
                strBuff.append(getDataReadFactory().getLastSeasonDemandTimeOfOccurenceDataRead()+"\n");
        }

        if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasValidSelfReadData()) {
            strBuff.append(getDataReadFactory().getLastSelfReadStateDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLastSelfReadEnergyDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLastSelfReadDemandDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLastSelfReadCumulativeDemandDataRead()+"\n");
            if (getDataReadFactory().getCapabilitiesDataRead().isMeterHasAClock())
                strBuff.append(getDataReadFactory().getLastSelfReadDemandTimeOfOccurenceDataRead()+"\n");
        }

        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels() > 0) {
            strBuff.append(getDataReadFactory().getLoadProfileQuantitiesDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLoadProfilePulseWeightsDataRead()+"\n");
            strBuff.append(getDataReadFactory().getLoadProfilePreliminaryDataRead() + "\n");

        }

        //strBuff.append(getDataReadFactory().getMeterMultiplierDataRead()+"\n");

        strBuff.append("----------------------------------------------STANDARD TABLES--------------------------------------------------\n");

        while(true) {
            try {
                if (skip<=0) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDeviceIdentificationTable());}
                if (skip<=1) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConfigurationTable());}
                if (skip<=2) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEndDeviceModeAndStatusTable());}
                if (skip<=3) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getManufacturerIdentificationTable());}
                if (skip<=4) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualSourcesLimitingTable());}
                if (skip<=5) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getUnitOfMeasureEntryTable(true));}

                if (skip<=6) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDemandControlTable());}
                if (skip<=7) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataControlTable());}
                if (skip<=8) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConstantsTable(true));}
                if (skip<=9) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSourceDefinitionTable(true));}
                if (skip<=10) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualRegisterTable());}
                if (skip<=11) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataSelectionTable());}
                if (skip<=12) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCurrentRegisterDataTable(true));}
                if (skip<=13) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousSeasonDataTable(true));}
                if (skip<=14) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousDemandResetDataTable(true));}
                if (skip<=15) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSelfReadDataTable(true));}
                if (skip<=16) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPresentRegisterDataTable(true));}
                if (skip<=17) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualTimeAndTOUTable());}
                if (skip<=18) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getTimeOffsetTable());}
                if (skip<=19) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCalendarTable());}
                if (skip<=20) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getClockTable());}
                if (skip<=21) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLoadProfileTable());}
                if (skip<=22) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileControlTable());}
                if (skip<=23) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileStatusTable());}
                if (skip<=24) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLogTable());}
                if (skip<=25) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventsIdentificationTable());}
                if (skip<=26) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogControlTable());}
                if (skip<=27) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogDataTable());}
                if (skip<=28) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogControlTable());}
                if (skip<=29) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableHeader());}
//                if (skip<=31) { skip++;strBuff.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n"+getManufacturerTableFactory().getFeatureParameters());}
//                if (skip<=32) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getServiceTypeTable());}
//                if (skip<=33) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterFactors());}
//                if (skip<=34) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterStatus());}
//                if (skip<=35) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getS4Configuration());}
                if (skip<=36) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
//e.printStackTrace();       // KV_DEBUG
                strBuff.append("Table not supported! "+e.toString()+"\n");
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
        if (getDataReadFactory().getCapabilitiesDataRead().getNumberOfLoadProfileChannels()>0)
            return getDataReadFactory().getLoadProfilePreliminaryDataRead().getLoadProfileIntervalLength() * 60;
        else
            return getInfoTypeProfileInterval();
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
        if (obisCodeInfoFactory == null)
            obisCodeInfoFactory=new ObisCodeInfoFactory(this, convertRegisterReadsToKiloUnits, readDemandsAndCoincidents, readTiers, limitRegisterReadSize);
        return obisCodeInfoFactory;
    }

    public DataReadFactory getDataReadFactory() {
        return dataReadFactory;
    }

    public void setDataReadFactory(DataReadFactory dataReadFactory) {
        this.dataReadFactory = dataReadFactory;
    }
}
