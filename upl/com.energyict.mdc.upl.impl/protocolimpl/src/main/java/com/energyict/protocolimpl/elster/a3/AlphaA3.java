/*
 * AlphaA3.java
 *
 * Created on 10 februari 2006, 14:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.ansi.c12.AbstractResponse;
import com.energyict.protocolimpl.ansi.c12.C1222Buffer;
import com.energyict.protocolimpl.ansi.c12.C1222Layer;
import com.energyict.protocolimpl.ansi.c12.C1222Layer.SecurityModeEnum;
import com.energyict.protocolimpl.ansi.c12.C12Layer2;
import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.PSEMServiceFactory;
import com.energyict.protocolimpl.ansi.c12.ResponseIOException;
import com.energyict.protocolimpl.ansi.c12.procedures.StandardProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.tables.LoadProfileSet;
import com.energyict.protocolimpl.ansi.c12.tables.StandardTableFactory;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.elster.a3.procedures.ManufacturerProcedureFactory;
import com.energyict.protocolimpl.elster.a3.tables.ManufacturerTableFactory;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.meteridentification.A3;
import com.energyict.protocolimpl.meteridentification.AbstractManufacturer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;

/**
 * @version  2.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements Elster A3 ANSI C12 meter.
 * <BR>
 * <B>@beginchanges</B><BR>
 * KV|04072007|Add additional multipliers
 * KV|24072007|Allow null password
 * KV|26072007|Add UOM codes 4 & 5
 * KV|08102007|Bugfix load profile interval end time
 * @endchanges
 */

// changed
public class AlphaA3 extends AbstractProtocol implements C12ProtocolLink, SerialNumberSupport {

    private static String SECURITY_MODE = "SecurityMode";
	private static String CALLED_AP_TITLE = "CalledAPTitle";
    private static String SECURITY_KEY = "SecurityKey";

	protected C12Layer2 c12Layer2;
    protected PSEMServiceFactory psemServiceFactory;
    protected StandardTableFactory standardTableFactory;
    protected ManufacturerTableFactory manufacturerTableFactory;
    protected StandardProcedureFactory standardProcedureFactory;
    protected ManufacturerProcedureFactory manufacturerProcedureFactory;
    private A3 a3=new A3();
    private AlphaA3LoadProfile alphaA3LoadProfile;
    private ObisCodeInfoFactory obisCodeInfoFactory=null;
    protected int passwordBinary;
    private int retrieveExtraIntervals;

    protected String c12User;
    protected int c12UserId;
    private boolean c1222 = false;
    private String securityMode;
    private String calledAPTitle;
    private String securityKey;
    private SerialCommunicationChannel commChannel;

    public AlphaA3(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading,new Date(),includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return alphaA3LoadProfile.getProfileData(from,to,includeEvents);
    }

    @Override
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        throw new IOException("Not implemented!");
    }

    @Override
    public AbstractManufacturer getManufacturer() {
        return a3;
    }

    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters.
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

        if (passwordBinary==0) {
            if ((getInfoTypeSecurityLevel()!=2) && ((getInfoTypePassword()==null) || (getInfoTypePassword().compareTo("")==0))) {
                setInfoTypePassword(new String(new byte[]{0}));
            }
            String pw=null;
            if (getInfoTypePassword()!=null) {
                pw = new String(ParseUtils.extendWithChar0(getInfoTypePassword().getBytes(), 20));
            }
            getPSEMServiceFactory().logOn(c12UserId,c12User,pw,getInfoTypeSecurityLevel(),PSEMServiceFactory.PASSWORD_ASCII);
        }
        else {
            if ((getInfoTypeSecurityLevel()!=2) && ((getInfoTypePassword()==null) || (getInfoTypePassword().compareTo("")==0))) {
                setInfoTypePassword(new String(new byte[]{0, 0}));
            }
            String pw=null;
            if (getInfoTypePassword()!=null) {
                pw = new String(ParseUtils.extendWithBinary0(getInfoTypePassword().getBytes(), 20));
            }
            getPSEMServiceFactory().logOn(c12UserId,c12User,pw,getInfoTypeSecurityLevel(),PSEMServiceFactory.PASSWORD_BINARY);
        }
    }

    @Override
    protected void doDisconnect() throws IOException {
    	if (c1222) {
            getPSEMServiceFactory().terminate();
        } else {
            getPSEMServiceFactory().logOff();
        }
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.stringSpec("C12User", false));
        propertySpecs.add(this.integerSpec("C12UserId", false));
        propertySpecs.add(this.integerSpec("PasswordBinary", false));
        propertySpecs.add(this.integerSpec("RetrieveExtraIntervals", false));
        propertySpecs.add(this.stringSpec(CALLED_AP_TITLE, false));
        propertySpecs.add(this.stringSpec(SECURITY_KEY, false));
        propertySpecs.add(this.stringSpec(SECURITY_MODE, false));
        return propertySpecs;
    }

    @Override
    public void setProperties(TypedProperties properties) throws PropertyValidationException {
        super.setProperties(properties);
        setForcedDelay(Integer.parseInt(properties.getTypedProperty("ForcedDelay", "10").trim()));
        setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "0"));
        c12User = properties.getTypedProperty("C12User", "");
        c12UserId = Integer.parseInt(properties.getTypedProperty("C12UserId", "0").trim());
        passwordBinary = Integer.parseInt(properties.getTypedProperty("PasswordBinary", "0").trim());
        setRetrieveExtraIntervals(Integer.parseInt(properties.getTypedProperty("RetrieveExtraIntervals", "0").trim()));
        calledAPTitle = properties.getTypedProperty(CALLED_AP_TITLE, "");
    	securityKey = properties.getTypedProperty(SECURITY_KEY, "");
    	securityMode = properties.getTypedProperty(SECURITY_MODE, "");

        if (getInfoTypePassword() != null) {
            if (passwordBinary == 0 && getInfoTypePassword().length() > 20) {
                throw new InvalidPropertyException("Length of password cannot be higher than 20 ASCII characters. Please correct first.");
            } else if (passwordBinary == 1 && getInfoTypePassword().length() > 40) {
                throw new InvalidPropertyException("Length of password cannot be higher than 40 binary values. Please correct first.");
            }
        }
    }

    private C1222Buffer checkForC1222() throws IOException {
        C1222Buffer result = null;

        if (securityMode != null) {
            // C1222Authenticate
            if (securityMode.compareToIgnoreCase("1") == 0) {
                result = new C1222Buffer();
                result.setSecurityMode(SecurityModeEnum.SecurityClearTextWithAuthentication);
            // C1222Encrypt
            } else if (securityMode.compareToIgnoreCase("2") == 0) {
                result = new C1222Buffer();
                result.setSecurityMode(SecurityModeEnum.SecurityCipherTextWithAuthentication);
            }
        }

        if (result != null) {
            c1222 = true;
            result.setCalledApTitle(calledAPTitle);
            result.setSecurityKey(securityKey);
            byte[] passwordBytes = passwordBinary == 0 ? getInfoTypePassword().getBytes() : ProtocolUtils.convert2ascii(getInfoTypePassword().getBytes());
            String password = new String(ParseUtils.extendWithWhiteSpace(passwordBytes, 20));
            result.setPassword(password);
        }

        return result;
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        psemServiceFactory = new PSEMServiceFactory(this);

        C1222Buffer c1222Buffer = checkForC1222();
    	if (c1222Buffer != null) {
    		c12Layer2 = new C1222Layer(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController);
            ((C1222Layer) c12Layer2).setC1222Buffer(c1222Buffer);
            psemServiceFactory.setC1222(c1222);
            psemServiceFactory.setC1222Buffer(c1222Buffer);
    	} else{
            c12Layer2 = new C12Layer2(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController);
        }

        c12Layer2.initStates();
        standardTableFactory = new StandardTableFactory(this);
        manufacturerTableFactory = new ManufacturerTableFactory(this);
        standardProcedureFactory = new StandardProcedureFactory(this);
        manufacturerProcedureFactory = new ManufacturerProcedureFactory(this);
        alphaA3LoadProfile = new AlphaA3LoadProfile(this);
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
                getLogger().warning("No clock table available. Probably a demand only meter!");
            } else {
                throw e;
            }
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
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeProtocolRetriesProperty() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:59 +0200 (Thu, 26 Nov 2015)$";
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
                if (skip<=5) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getUnitOfMeasureEntryTable());}

                if (skip<=6) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDemandControlTable());}
                if (skip<=7) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataControlTable());}
                if (skip<=8) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getConstantsTable());}
                if (skip<=9) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSourceDefinitionTable());}
                if (skip<=10) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualRegisterTable());}
                if (skip<=11) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getDataSelectionTable());}
                if (skip<=12) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCurrentRegisterDataTable());}
                if (skip<=13) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousSeasonDataTable());}
                if (skip<=14) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPreviousDemandResetDataTable());}
                if (skip<=15) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getSelfReadDataTable());}
                if (skip<=16) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getPresentRegisterDataTable());}
                if (skip<=17) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualTimeAndTOUTable());}
                if (skip<=18) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getTimeOffsetTable());}
                if (skip<=19) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getCalendarTable());}
                if (skip<=20) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getClockTable());}
                if (skip<=21) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLoadProfileTable());}
                if (skip<=22) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileControlTable());}
                if (skip<=23) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getLoadProfileStatusTable());}
                if (skip<=24) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getActualLogTable());}
                if (skip<=25) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventsIdentificationTable());}
                if (skip<=26) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogControlTable());}
if (skip<=27) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getHistoryLogDataTable());}
                if (skip<=28) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogControlTable());}
if (skip<=29) { skip+=2;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableHeader());}
//if (skip<=30) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getStandardTableFactory().getEventLogDataTableEventEntries(145, 10));}
                if (skip<=31) { skip++;builder.append("----------------------------------------------MANUFACTURER TABLES--------------------------------------------------\n"+getManufacturerTableFactory().getElectricitySpecificProductSpec());}
                if (skip<=32) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getFactoryDefaultMeteringInformation());}
                if (skip<=33) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getPrimaryMeteringInformation());}
                if (skip<=34) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getSourceDefinitionTable());}
                if (skip<=35) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getStatusTable());}
                if (skip<=36) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getSummationSnapshotTable());}
                if (skip<=37) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getPreviousIntervalDemand());}
                if (skip<=38) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getAnswerParametersTableForRemotePorts());}
                if (skip<=39) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getCallPurposeTableForRemotePorts());}
                if (skip<=40) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getCallStatusTableForRemotePorts());}
                if (skip<=41) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getGlobalParametersTablesForRemotePorts());}
                if (skip<=42) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getOptionBoardScratchPad());}
                if (skip<=43) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getOriginateParametersTableForRemotePorts());}
                if (skip<=44) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getOriginateSchedulingTablesforRemotePorts());}
                if (skip<=45) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getOutageModemConfiguration());}
                if (skip<=46) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getOutageModemStatus());}
                if (skip<=47) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getRemoteCommunicationStatus());}
                if (skip<=48) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getManufacturerTableFactory().getRemoteConfigurationConfiguration());}

                if (skip<=49) { skip++;builder.append("------------------------------------------------------------------------------------------------\n"+getObisCodeInfoFactory().toString());}
                break;
            }
            catch(IOException e) {
//e.printStackTrace();       // KV_DEBUG
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
                return 0;
            }
        }
        catch(ResponseIOException e) {
            if (e.getReason()==AbstractResponse.IAR) {
                getLogger().warning("No profileinterval available. Probably a demand only meter!");
            } else {
                throw e;
            }
        }
        return 0;
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
        return 0; //getManufacturerTableFactory().getGEDeviceTable().getMeterMode();
    }

    public ObisCodeInfoFactory getObisCodeInfoFactory() throws IOException {
        if (obisCodeInfoFactory == null) {
            obisCodeInfoFactory = new ObisCodeInfoFactory(this);
        }
        return obisCodeInfoFactory;
    }

    public int getRetrieveExtraIntervals() {
        return retrieveExtraIntervals;
    }

    public void setRetrieveExtraIntervals(int retrieveExtraIntervals) {
        this.retrieveExtraIntervals = retrieveExtraIntervals;
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return super.getProtocolChannelMap();
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

    @Override
    public int getInfoTypeRoundtripCorrection() {
        return super.getInfoTypeRoundtripCorrection();
    }

}