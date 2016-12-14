/*
 * GEKV2.java
 *
 * Created on 17 oktober 2005, 8:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
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
import com.energyict.protocolimpl.ge.kv2.procedures.ManufacturerProcedureFactory;
import com.energyict.protocolimpl.ge.kv2.tables.ManufacturerTableFactory;
import com.energyict.protocolimpl.meteridentification.AbstractManufacturer;
import com.energyict.protocolimpl.meteridentification.KV2;

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
 * @beginchanges
KV|04012007|reengineered protocol to allow Eiserver 7 new channel properties
 * @endchanges
 */
public class GEKV2 extends AbstractProtocol implements C12ProtocolLink, SerialNumberSupport {
    
    private C12Layer2 c12Layer2;
    private PSEMServiceFactory psemServiceFactory;
    private StandardTableFactory standardTableFactory;
    private ManufacturerTableFactory manufacturerTableFactory;
    private StandardProcedureFactory standardProcedureFactory;
    private ManufacturerProcedureFactory manufacturerProcedureFactory;
    KV2 kv2=new KV2();
    GEKV2LoadProfile gekv2LoadProfile;
    private ObisCodeInfoFactory obisCodeInfoFactory=null;
    
    String c12User;
    int c12UserId;
    boolean validateControlToggleBit;
    private int useSnapshotProcedure;
    
    /** Creates a new instance of GEKV */
    public GEKV2() {
    }
    
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading,new Date(),includeEvents);
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return gekv2LoadProfile.getProfileData(from,to,includeEvents);
    }
    
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
/*
        Properties properties = new Properties();
        properties.setProperty("SecurityLevel","0");
        properties.setProperty(MeterProtocol.NODEID,nodeId);
        properties.setProperty("IEC1107Compatible","1");
        setProperties(properties);
        init(discoverInfo.getCommChannel().getInputStream(),discoverInfo.getCommChannel().getOutputStream(),null,null);
        enableHHUSignOn(commChannel);
        connect();
        String serialNumber =  getRegister("SerialNumber");
        disconnect();
        return serialNumber;
*/        
        throw new IOException("Not implemented!");
    }
    
    public AbstractManufacturer getManufacturer() {
        return kv2;
    }
    
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters. 
    SerialCommunicationChannel commChannel;
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
        getPSEMServiceFactory().logOn(c12UserId,c12User,getInfoTypePassword(),getInfoTypeSecurityLevel(),PSEMServiceFactory.PASSWORD_BINARY);
        
        // only for the KV2c meter... Because the KV2c meter continues collecting data during a communication session...
        if (getUseSnapshotProcedure() == 1) {
            try {
                getManufacturerProcedureFactory().snapShotData();
                getLogger().info("KV2c meter");
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.ONP) // operation not possible
                   getLogger().info("Snapshot procedure not possible, KV2 meter or KV meter!");
                if (e.getReason()==AbstractResponse.SNAPSHOT_ERROR) { 
                   getLogger().info("It appears that some KV2 meters are not happy with the use of the snapshot command. therefor, set the 'UseSnapshotProcedure' custom property to 0!");
                   throw e;
                }
                else { 
                   throw e;
                }
            }        
        }
    }
    
    protected void doDisConnect() throws IOException {
        getPSEMServiceFactory().logOff();        
    }    
    
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","10").trim()));
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"64"));
        c12User = properties.getProperty("C12User","");
        c12UserId = Integer.parseInt(properties.getProperty("C12UserId","0").trim());
        setUseSnapshotProcedure(Integer.parseInt(properties.getProperty("UseSnapshotProcedure","1").trim()));
        this.validateControlToggleBit = Integer.parseInt(properties.getProperty("ValidateFrameControlToggleBit", "0")) == 1;
    }
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        
        result.add("C12User");
        result.add("C12UserId");
        result.add("UseSnapshotProcedure");
        result.add("ValidateFrameControlToggleBit");
        return result;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getLogger(), validateControlToggleBit);
        c12Layer2.initStates();
        psemServiceFactory = new PSEMServiceFactory(this);
        standardTableFactory = new StandardTableFactory(this);
        manufacturerTableFactory = new ManufacturerTableFactory(this);
        standardProcedureFactory = new StandardProcedureFactory(this);
        manufacturerProcedureFactory = new ManufacturerProcedureFactory(this);
        
        gekv2LoadProfile = new GEKV2LoadProfile(this);
        return c12Layer2;
    }
    
    public void setTime() throws IOException {
        getStandardProcedureFactory().setDateTime();
    }    
    
    public Date getTime() throws IOException {
        try {
            return getStandardTableFactory().getTime();
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) // table does not exist!
               getLogger().warning("No clock table available, use system clock. Probably a demand only meter!");
            else { 
               throw e;
             //   getLogger().warning(e.toString());
            }
        }
        catch(IOException e) {
            getLogger().warning(e.toString());
        }
        return new Date();
        
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        try {
            LoadProfileSet lps = getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet();
            if (lps!=null)
                return lps.getNrOfChannelsSet()[0];
            else
                return 0;
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) // table does not exist!
               getLogger().warning("No profile channels available. Probably a demand only meter!");
            else 
               throw e;
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

    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:13 +0200 (Thu, 26 Nov 2015)$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return getStandardTableFactory().getManufacturerIdentificationTable().getManufacturer()+", "+
               getStandardTableFactory().getManufacturerIdentificationTable().getModel()+", "+
               "Firmware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getFwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getFwRevision()+", "+
               "Hardware version.revision="+getStandardTableFactory().getManufacturerIdentificationTable().getHwVersion()+"."+getStandardTableFactory().getManufacturerIdentificationTable().getHwRevision();
    }    
    
    /*
     * Override this method if the subclass wants to set a specific register 
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        
    }
    
    /*
     * Override this method if the subclass wants to get a specific register 
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
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
        strBuff.append("----------------------------------------------STANDARD TABLES--------------------------------------------------\n");
        while(true) {
            try {
                if (skip<=0) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getManufacturerIdentificationTable());}
                if (skip<=1) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConfigurationTable());}
                if (skip<=2) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEndDeviceModeAndStatusTable());}
                if (skip<=3) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDeviceIdentificationTable());}
                if (skip<=4) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualSourcesLimitingTable());}
                if (skip<=5) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getUnitOfMeasureEntryTable());}
                
                if (skip<=6) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDemandControlTable());}
                if (skip<=7) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataControlTable());}
                if (skip<=8) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConstantsTable());}
                if (skip<=9) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSourceDefinitionTable());}
                if (skip<=10) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualRegisterTable());}
                if (skip<=11) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataSelectionTable());}
                if (skip<=12) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCurrentRegisterDataTable());}
                if (skip<=13) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousSeasonDataTable());}
                if (skip<=14) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousDemandResetDataTable());}
                if (skip<=15) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSelfReadDataTable());}
                if (skip<=16) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPresentRegisterDataTable());}
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
if (skip<=29) { skip+=2;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableHeader());}
//if (skip<=30) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableEventEntries(145, 10));}
                if (skip<=31) { skip++;strBuff.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n"+getManufacturerTableFactory().getGEDeviceTable());}
                if (skip<=32) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterProgramConstants1());}
                if (skip<=33) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getMeterProgramConstants2());}
                if (skip<=34) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getDisplayConfigurationTable());}
                if (skip<=35) { skip+=3;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getScaleFactorTable());}
                //if (skip<=36) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getElectricalServiceConfiguration());}
                //if (skip<=37) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getElectricalServiceStatus());}
                if (skip<=38) { skip++;strBuff.append("------------------------------------------------------------------------------------------------\n"+getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
//e.printStackTrace();       // KV_DEBUG          
                strBuff.append("Table not supported! "+e.toString()+"\n");
            }
        }
        
        
        
        return strBuff.toString();
    }
    
    
    /****************************************************************************************************************
     * Implementing C12ProtocolLink interface
     ****************************************************************************************************************/    
    
    public C12Layer2 getC12Layer2() {
        return c12Layer2;
    }
    
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        try {
            LoadProfileSet lps = getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet();
            if (lps!=null)
                return lps.getProfileIntervalSet()[0]*60;
            else
                return getInfoTypeProfileInterval();
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) // table does not exist!
               getLogger().warning("No profileinterval available. Probably a demand only meter!");
            else 
               throw e;
        }
        return getInfoTypeProfileInterval();
    }
    
    public TimeZone gettimeZone() {
        return super.getTimeZone();
    }
    
    static public void main(String[] args) {
        try {
            // ********************** Dialer **********************
            //Dialer dialer = DialerFactory.getDirectDialer().newDialer();
            Dialer dialer = DialerFactory.getOpticalDialer().newDialer();
            dialer.init("COM1");
            dialer.getSerialCommunicationChannel().setParams(9600,
                                                             SerialCommunicationChannel.DATABITS_8,
                                                             SerialCommunicationChannel.PARITY_NONE,
                                                             SerialCommunicationChannel.STOPBITS_1);
            dialer.connect();
            
            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "900");
            properties.setProperty(MeterProtocol.NODEID,"0");
            properties.setProperty(MeterProtocol.ADDRESS,"1");
            properties.setProperty(MeterProtocol.PASSWORD,"A6A6A6A6A6A6A6A6A6A6A6A6A6A6A6A6A6A6A6A6");
            properties.setProperty("ChannelMap","1,1");
            //properties.setProperty("HalfDuplex", "10");
            
            // ********************** EictRtuModbus **********************
            GEKV2 gekv2 = new GEKV2();
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)gekv2).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            gekv2.setHalfDuplexController(dialer.getHalfDuplexController());
            gekv2.setProperties(properties);
            gekv2.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            gekv2.connect();
            
            
//            System.out.println(gekv2.getStandardTableFactory().getManufacturerIdentificationTable());
//            System.out.println(gekv2.getStandardTableFactory().getConfigurationTable());
//            System.out.println(gekv2.getStandardTableFactory().getEndDeviceModeAndStatusTable());
//            System.out.println(gekv2.getManufacturerTableFactory().getGEDeviceTable());
//            System.out.println(gekv2.getStandardTableFactory().getDeviceIdentificationTable());
//            
//            System.out.println(gekv2.getStandardTableFactory().getClockTable());
            
            //byte[] password = {(byte)0x5f,(byte)0x29,(byte)0x6e,(byte)0x00,(byte)0x29,(byte)0xfc,(byte)0x7c,(byte)0x90,(byte)0xce,(byte)0xef,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20};
            //byte[] password = {(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA};
            //byte[] password = {(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB};
//            byte[] password = {(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6};
//            gekv2.getPSEMServiceFactory().secure(password);

            //System.out.println(gekv2.getManufacturerTableFactory().getMeterProgramConstants1());
            try {
               System.out.println(gekv2.getStandardTableFactory().getActualLogTable());
            }
            catch(IOException e) {
                System.out.println("Table not supported! "+e.toString());
            }
            try {
               System.out.println(gekv2.getStandardTableFactory().getEventsIdentificationTable());
            }
            catch(IOException e) {
                System.out.println("Table not supported! "+e.toString());
            }
            
//            System.out.println(gekv2.getManufacturerTableFactory().getMeterProgramConstants2());
//            System.out.println(gekv2.getManufacturerTableFactory().getDisplayConfigurationTable());
//            System.out.println(gekv2.getManufacturerTableFactory().getScaleFactorTable());
//            System.out.println(gekv2.getManufacturerTableFactory().getElectricalServiceConfiguration());
//            System.out.println(gekv2.getManufacturerTableFactory().getElectricalServiceStatus());
//            
//            System.out.println(gekv2.getStandardTableFactory().getActualSourcesLimitingTable());
//            System.out.println(gekv2.getStandardTableFactory().getDemandControlTable());
//            System.out.println(gekv2.getStandardTableFactory().getDataControlTable());
//            System.out.println(gekv2.getStandardTableFactory().getConstantsTable());
//            System.out.println(gekv2.getStandardTableFactory().getSourceDefinitionTable());
//            System.out.println(gekv2.getStandardTableFactory().getActualRegisterTable());
//            System.out.println(gekv2.getStandardTableFactory().getDataSelectionTable());
//            System.out.println(gekv2.getStandardTableFactory().getCurrentRegisterDataTable());
//            System.out.println(gekv2.getStandardTableFactory().getPreviousSeasonDataTable());
//            System.out.println(gekv2.getStandardTableFactory().getPreviousDemandResetDataTable());
//            System.out.println(gekv2.getStandardTableFactory().getSelfReadDataTable());
            //System.out.println(gekv2.getStandardTableFactory().getPresentRegisterSelectionTable());
//            System.out.println(gekv2.getStandardTableFactory().getPresentRegisterDataTable());
//            System.out.println(gekv2.getStandardTableFactory().getActualTimeAndTOUTable());
//            System.out.println(gekv2.getStandardTableFactory().getTimeOffsetTable());
//            System.out.println(gekv2.getStandardTableFactory().getCalendarTable());
            
            // set time
            //gekv2.setTime();
            
//            System.out.println(gekv2.getStandardTableFactory().getClockStateTable());
//            System.out.println(gekv2.getStandardTableFactory().getActualLoadProfileTable());
//            System.out.println(gekv2.getStandardTableFactory().getLoadProfileControlTable());
//            System.out.println(gekv2.getStandardTableFactory().getLoadProfileStatusTable());

            
//            byte[] password = {(byte)0x5f,(byte)0x29,(byte)0x6e,(byte)0x00,(byte)0x29,(byte)0xfc,(byte)0x7c,(byte)0x90,(byte)0xce,(byte)0xef,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20,(byte)0x20};
            //byte[] password = {(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA};
            //byte[] password = {(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB,(byte)0xBB};
//            byte[] password = {(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6,(byte)0xA6};
//            gekv2.getPSEMServiceFactory().secure(password);
            
            
//System.out.println("KV_DEBUG> program manufacturer specific table");        
//gekv2.getPSEMServiceFactory().fullWrite(66, new byte[]{0,0,(byte)(1667/256),(byte)(1667%256)});           
//gekv2.getManufacturerTableFactory().getMeterProgramConstants1().setTableData(new byte[]{0,0,(byte)(1667/256),(byte)(1667%256)});          
//gekv2.getManufacturerTableFactory().getMeterProgramConstants1().transfer();



//            Calendar cal = Calendar.getInstance();
//            cal.add(Calendar.DAY_OF_MONTH,-4);
//            System.out.println(gekv2.getProfileData(cal.getTime(),true));
            
            System.out.println(gekv2.getFirmwareVersion());
            
            
            gekv2.disconnect();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
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

    public ObisCodeInfoFactory getObisCodeInfoFactory() throws IOException {
        if (obisCodeInfoFactory == null)
            obisCodeInfoFactory=new ObisCodeInfoFactory(this);
        return obisCodeInfoFactory;
    }
    public int getMeterConfig() throws IOException {
        return getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
    }

    public int getUseSnapshotProcedure() {
        return useSnapshotProcedure;
    }

    public void setUseSnapshotProcedure(int useSnapshotProcedure) {
        this.useSnapshotProcedure = useSnapshotProcedure;
    }
}
