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

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.inbound.DiscoverInfo;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
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
import com.energyict.protocolimpl.ge.kv2.procedures.ManufacturerProcedureFactory;
import com.energyict.protocolimpl.ge.kv2.tables.ManufacturerTableFactory;
import com.energyict.protocolimpl.meteridentification.AbstractManufacturer;
import com.energyict.protocolimpl.meteridentification.KV2;

import javax.inject.Inject;
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
   KV|04012007|reengineered protocol to allow Eiserver 7 new channel properties
 */
public class GEKV2 extends AbstractProtocol implements C12ProtocolLink {

    @Override
    public String getProtocolDescription() {
        return "General Electric KV2 ANSI";
    }

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
    private int useSnapshotProcedure;

    @Inject
    public GEKV2(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }


    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading,new Date(),includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
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

    protected void validateSerialNumber() throws IOException {
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
            return;
        }
        String sn = getStandardTableFactory().getManufacturerIdentificationTable().getManufacturerSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
            return;
        }
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
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
            if (getDtrBehaviour() == 0) {
                commChannel.setDTR(false);
            }
            else if (getDtrBehaviour() == 1) {
                commChannel.setDTR(true);
            }
        }
        getPSEMServiceFactory().logOn(c12UserId,c12User,getInfoTypePassword(),getInfoTypeSecurityLevel(),PSEMServiceFactory.PASSWORD_BINARY);

        // only for the KV2c meter... Because the KV2c meter continues collecting data during a communication session...
        if (getUseSnapshotProcedure() == 1) {
            try {
                getManufacturerProcedureFactory().snapShotData();
                getLogger().info("KV2c meter");
            }
            catch(ResponseIOException e) {
                if (e.getReason()==AbstractResponse.ONP) { // operation not possible
                    getLogger().info("Snapshot procedure not possible, KV2 meter or KV meter!");
                }
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
    }

    protected List<String> doGetOptionalKeys() {
        return Arrays.asList("C12User", "C12UserId", "UseSnapshotProcedure");
    }

    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController);
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
            if (e.getReason()==AbstractResponse.IAR) {// table does not exist!
               getLogger().warning("No clock table available, use system clock. Probably a demand only meter!");
            } else {
               throw e;
             //   getLogger().warning(e.toString());
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
            }
            else {
                return 0;
            }
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) { // table does not exist!
                getLogger().warning("No profile channels available. Probably a demand only meter!");
            }
            else {
                throw e;
            }
        }
        return 0;
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
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
        StringBuilder builder = new StringBuilder();
        builder.append("----------------------------------------------STANDARD TABLES--------------------------------------------------\n");
        while(true) {
            try {
                if (skip<=0) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getManufacturerIdentificationTable());}
                if (skip<=1) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getConfigurationTable());}
                if (skip<=2) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getEndDeviceModeAndStatusTable());}
                if (skip<=3) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getDeviceIdentificationTable());}
                if (skip<=4) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getActualSourcesLimitingTable());}
                if (skip<=5) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getUnitOfMeasureEntryTable());}

                if (skip<=6) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getDemandControlTable());}
                if (skip<=7) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getDataControlTable());}
                if (skip<=8) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getConstantsTable());}
                if (skip<=9) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getSourceDefinitionTable());}
                if (skip<=10) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getActualRegisterTable());}
                if (skip<=11) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getDataSelectionTable());}
                if (skip<=12) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getCurrentRegisterDataTable());}
                if (skip<=13) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getPreviousSeasonDataTable());}
                if (skip<=14) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getPreviousDemandResetDataTable());}
                if (skip<=15) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getSelfReadDataTable());}
                if (skip<=16) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getPresentRegisterDataTable());}
                if (skip<=17) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getActualTimeAndTOUTable());}
                if (skip<=18) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getTimeOffsetTable());}
                if (skip<=19) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getCalendarTable());}
                if (skip<=20) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getClockTable());}
                if (skip<=21) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getActualLoadProfileTable());}
                if (skip<=22) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getLoadProfileControlTable());}
                if (skip<=23) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getLoadProfileStatusTable());}
                if (skip<=24) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getActualLogTable());}
                if (skip<=25) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getEventsIdentificationTable());}
                if (skip<=26) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getHistoryLogControlTable());}
if (skip<=27) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getHistoryLogDataTable());}
                if (skip<=28) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getEventLogControlTable());}
if (skip<=29) { skip+=2;builder.append("------------------------------------------------------------------------------------------------\n" + getStandardTableFactory().getEventLogDataTableHeader());}
//if (skip<=30) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableEventEntries(145, 10));}
                if (skip<=31) { skip++;builder.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n" + getManufacturerTableFactory()
                        .getGEDeviceTable());}
                if (skip<=32) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getManufacturerTableFactory().getMeterProgramConstants1());}
                if (skip<=33) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getManufacturerTableFactory().getMeterProgramConstants2());}
                if (skip<=34) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getManufacturerTableFactory().getDisplayConfigurationTable());}
                if (skip<=35) { skip+=3;builder.append("------------------------------------------------------------------------------------------------\n" + getManufacturerTableFactory().getScaleFactorTable());}
                //if (skip<=36) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getElectricalServiceConfiguration());}
                //if (skip<=37) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getElectricalServiceStatus());}
                if (skip<=38) { skip++;builder.append("------------------------------------------------------------------------------------------------\n" + getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
//e.printStackTrace();       // KV_DEBUG
                builder.append("Table not supported! " + e.toString() + "\n");
            }
        }
        return builder.toString();
    }


    /****************************************************************************************************************
     * Implementing C12ProtocolLink interface
     ****************************************************************************************************************/

    public C12Layer2 getC12Layer2() {
        return c12Layer2;
    }

    public int getProfileInterval() throws IOException {
        try {
            LoadProfileSet lps = getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet();
            if (lps!=null) {
                return lps.getProfileIntervalSet()[0] * 60;
            }
            else {
                return getInfoTypeProfileInterval();
            }
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) // table does not exist!
            {
                getLogger().warning("No profileinterval available. Probably a demand only meter!");
            }
            else {
                throw e;
            }
        }
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

    public ObisCodeInfoFactory getObisCodeInfoFactory() throws IOException {
        if (obisCodeInfoFactory == null) {
            obisCodeInfoFactory = new ObisCodeInfoFactory(this);
        }
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
