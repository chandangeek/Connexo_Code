/*
 * Sdc.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// com.energyict.protocolimpl.iec1107.sdc.Sdc
/**
 *
 * @author gna
 * <B>@beginchanges</B><BR>
 * GN|29012008| Adjusted the readRegisters according to the spec
 *@endchanges
 *
 */
abstract class SdcBase extends AbstractProtocol implements SerialNumberSupport {

    private IEC1107Connection iec1107Connection = null;
    private DataReadingCommandFactory dataReadingCommandFactory = null;
    private SdcLoadProfile sdcLoadProfile = null;
    private ObisCodeMapper ocm = null;
	private int extendedLogging;
	private boolean software7E1;

    protected abstract RegisterConfig getRegs();

    SdcBase(PropertySpecService propertySpecService) {
        super(false, propertySpecService); // true for datareadout;
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getSdcLoadProfile().getProfileData(lastReading, includeEvents);
    }

    @Override
    protected void doConnect() throws IOException {
        dataReadingCommandFactory = new DataReadingCommandFactory(this);
    }

    @Override
    protected void doDisconnect() throws IOException {
    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return "******************* ExtendedLogging *******************\n" +
                "1.0.1.8.128.255: Active Energy tariff HV" + "\n" +
                "1.0.1.8.129.255: Active Energy tariff HP" + "\n" +
                "1.0.1.8.130.255: Active Energy tariff HC" + "\n" +
                "1.0.1.8.131.255: Active Energy tariff HSV" + "\n" +
                "1.0.3.8.128.255: Reactive Energy inductive tariff HV" + "\n" +
                "1.0.3.8.132.255: Reactive Energy inductive tariff HFV" + "\n" +
                "1.0.4.8.128.255: Reactive Energy capacitive tariff HV" + "\n" +
                "1.0.4.8.132.255: Reactive Energy capacitive tariff HFV" + "\n" +
                "1.0.1.6.128.255: Active Energy maximum demand tariff HV" + "\n" +
                "1.0.1.6.132.255: Active Energy maximum demand tariff HFV" + "\n" +
                "1.0.3.6.128.255: Reactive Energy maximum demand inductive tariff HV" + "\n" +
                "1.0.3.6.132.255: Reactive Energy maximum demand inductive tariff HFV" + "\n" +
                "1.0.4.6.128.255: Reactive Energy maximum demand capacitive tariff HV" + "\n" +
                "1.0.4.6.132.255: Reactive Energy maximum demand capacitive tariff HFV" + "\n" +
                "1.0.1.8.0.255: Active Energy total (all phases)" + "\n" +
                "1.0.3.8.0.255: Reactive Energy inductive total (all phases)" + "\n" +
                "1.0.4.8.0.255: Reactive Energy capacitive total (all phases)" + "\n" +
                "*******************************************************\n";
    }

    public int getNumberOfChannels() throws IOException {
       return getSdcLoadProfile().getNrOfChannels();
    }

    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        iec1107Connection = new IEC1107Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,ERROR_SIGNATURE, software7E1);
        sdcLoadProfile = new SdcLoadProfile(this);
        iec1107Connection.setChecksumMethod(1);
        return iec1107Connection;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec("Software7E1", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
    	extendedLogging=Integer.parseInt(properties.getTypedProperty(PROP_EXTENDED_LOGGING, "0").trim());
        this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "Unknown";
    }

    @Override
    public Date getTime() throws IOException {
        // KV_DEBUG
//        TimeZone tz = getDataReadingCommandFactory().getTimeZoneRead();
//        System.out.println(tz.getRawOffset());
//        System.out.println(tz.getDisplayName());
//        System.out.println(tz);
        return getDataReadingCommandFactory().getDateTimeGmt();
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND,getInfoTypeRoundtripCorrection());
        getDataReadingCommandFactory().setDateTimeGmt(calendar.getTime());
    }


    com.energyict.protocolimpl.iec1107.IEC1107Connection getIec1107Connection() {
        return iec1107Connection;
    }

    static final String COMMAND_CANNOT_BE_EXECUTED="([4])";
    private static final String ERROR_SIGNATURE="([";

    private  static final Map<String, String> EXCEPTION_INFO_MAP = new HashMap<>();
    static {
        EXCEPTION_INFO_MAP.put("([1])","General error, insufficient access rights");
        EXCEPTION_INFO_MAP.put("([2])","The nr of command parameters is not correct");
        EXCEPTION_INFO_MAP.put("([3])","The value of a command parameters is not valid");
        EXCEPTION_INFO_MAP.put(COMMAND_CANNOT_BE_EXECUTED,"The command is formally correct, but it cannot be executed in this context");
        EXCEPTION_INFO_MAP.put("([6])","EEPROM write error");
        EXCEPTION_INFO_MAP.put("([7])","Core communication error");
    }

    @Override
    public String getExceptionInfo(String id) {
        String exceptionInfo = EXCEPTION_INFO_MAP.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        } else {
            return "No meter specific exception info for " + id;
        }
    }

    public com.energyict.protocolimpl.iec1107.sdc.DataReadingCommandFactory getDataReadingCommandFactory() {
        return dataReadingCommandFactory;
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	if (ocm == null) {
            ocm = new ObisCodeMapper(getDataReadingCommandFactory(), getRegs());
        }
        return ocm.getRegisterValue(obisCode);
    }

    private com.energyict.protocolimpl.iec1107.sdc.SdcLoadProfile getSdcLoadProfile() {
        return sdcLoadProfile;
    }

    @Override
    public String getSerialNumber(){
        try {
            ObisCode oc = new ObisCode(1,0,0,0,0,255);
            String str = readRegister(oc).getText();
            return str.substring(str.indexOf(",") + 2 );
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public ProtocolChannelMap getProtocolChannelMap() {
        return super.getProtocolChannelMap();
    }

}