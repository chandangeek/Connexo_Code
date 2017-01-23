/*
 * Sdc.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
// com.energyict.protocolimpl.iec1107.sdc.Sdc
/**
 *
 * @author gna
 * <B>@beginchanges</B><BR>
 * GN|29012008| Adjusted the readRegisters according to the spec
 *@endchanges
 *
 */
public abstract class SdcBase extends AbstractProtocol {

    IEC1107Connection iec1107Connection=null;
    DataReadingCommandFactory dataReadingCommandFactory=null;
    SdcLoadProfile sdcLoadProfile=null;
    ObisCodeMapper ocm = null;
    private boolean software7E1;


    protected abstract RegisterConfig getRegs();

    public SdcBase(PropertySpecService propertySpecService) {
        super(propertySpecService, false); // true for datareadout;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getSdcLoadProfile().getProfileData(lastReading, includeEvents);
    }

    protected void doConnect() throws IOException {
        dataReadingCommandFactory = new DataReadingCommandFactory(this);
    }

    protected void doDisConnect() throws IOException {
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        String strBuff = "******************* ExtendedLogging *******************\n" +
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
        return strBuff;
    }


    public int getNumberOfChannels() throws IOException {
       return getSdcLoadProfile().getNrOfChannels();
    }

    protected List<String> doGetOptionalKeys() {
        return Collections.emptyList();
    }

    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {

        iec1107Connection=new IEC1107Connection(inputStream,outputStream,timeoutProperty,protocolRetriesProperty,forcedDelay,echoCancelling,protocolCompatible,encryptor,ERROR_SIGNATURE, software7E1);
        sdcLoadProfile = new SdcLoadProfile(this);
        iec1107Connection.setChecksumMethod(1);


//        getSdcLoadProfile().setNrOfChannels(3);
        getSdcLoadProfile().setNrOfChannels(1);

        return iec1107Connection;
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        this.software7E1 = !"0".equals(properties.getProperty("Software7E1", "0"));
    }

    public String getFirmwareVersion() throws IOException {
        return "Unknown";
    }

    public Date getTime() throws IOException {
        // KV_DEBUG
//        TimeZone tz = getDataReadingCommandFactory().getTimeZoneRead();
//        System.out.println(tz.getRawOffset());
//        System.out.println(tz.getDisplayName());
//        System.out.println(tz);
        return getDataReadingCommandFactory().getDateTimeGmt();
    }

    public void setTime() throws IOException {
        //Calendar calendar = ProtocolUtils.getCalendar(TimeZoneManager.getTimeZone("GMT"));
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND,getInfoTypeRoundtripCorrection());
        getDataReadingCommandFactory().setDateTimeGmt(calendar.getTime());
    }


    /*******************************************************************************************
     * g e t t e r s  a n d  s e t t e r s
     *******************************************************************************************/

    /**
     * Getter for property iec1107Connection.
     * @return Value of property iec1107Connection.
     */
    public com.energyict.protocolimpl.iec1107.IEC1107Connection getIec1107Connection() {
        return iec1107Connection;
    }


    //    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
    //        enableHHUSignOn(commChannel,true);
    //    }

    /*******************************************************************************************
     * M e t e r E x c e p t i o n I n f o  i n t e r f a c e
     *******************************************************************************************/
    /*
     *  This method must be overridden by the subclass to implement meter specific error
     *  messages. Us sample code of a static map with error codes below as a sample and
     *  use code in method as a sample of how to retrieve the error code.
     *  This code has been taken from a real protocol implementation.
     */

    public static final String COMMAND_CANNOT_BE_EXECUTED="([4])";
    public static final String ERROR_SIGNATURE="([";

    static Map<String, String> exceptionInfoMap = new HashMap<>();
    static {
        exceptionInfoMap.put("([1])","General error, insufficient access rights");
        exceptionInfoMap.put("([2])","The nr of command parameters is not correct");
        exceptionInfoMap.put("([3])","The value of a command parameters is not valid");
        exceptionInfoMap.put(COMMAND_CANNOT_BE_EXECUTED,"The command is formally correct, but it cannot be executed in this context");
        exceptionInfoMap.put("([6])","EEPROM write error");
        exceptionInfoMap.put("([7])","Core communication error");
    }

    public String getExceptionInfo(String id) {

        String exceptionInfo = exceptionInfoMap.get(id);
        if (exceptionInfo != null) {
            return id + ", " + exceptionInfo;
        }
        else {
            return "No meter specific exception info for " + id;
        }
    }

    /*
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {

    	ObisCode oc = new ObisCode(1,0,0,0,0,255);
    	String str = readRegister(oc).getText();
    	str = str.substring(str.indexOf(",") + 2 );
    	if (!getInfoTypeNodeAddress().isEmpty()) {
	    	if ( str.compareTo(getInfoTypeNodeAddress().substring(getInfoTypeNodeAddress().indexOf(str.charAt(0)))) == -1 ) {
                throw new IOException("Incorrect node Address!");
            }
    	}
    	else {
            throw new IOException("Incorrect node Address!");
        }

    	return;
    }

    /**
     * Getter for property dataReadingCommandFactory.
     * @return Value of property dataReadingCommandFactory.
     */
    public com.energyict.protocolimpl.iec1107.sdc.DataReadingCommandFactory getDataReadingCommandFactory() {
        return dataReadingCommandFactory;
    }

    /*******************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {

    	if (ocm == null) {
            ocm = new ObisCodeMapper(getDataReadingCommandFactory(), getTimeZone(), getRegs());
        }

        return ocm.getRegisterValue(obisCode);
    }

    /**
     * Getter for property sdcLoadProfile.
     * @return Value of property sdcLoadProfile.
     */
    public com.energyict.protocolimpl.iec1107.sdc.SdcLoadProfile getSdcLoadProfile() {
        return sdcLoadProfile;
    }



}
