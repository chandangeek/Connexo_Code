/*
 * Sdc.java
 *
 * Created on 28 juli 2004, 10:28
 */

package com.energyict.protocolimpl.iec1107.sdc;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.customerconfig.RegisterConfig;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
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
abstract public class SdcBase extends AbstractProtocol implements SerialNumberSupport {

    IEC1107Connection iec1107Connection=null;
    DataReadingCommandFactory dataReadingCommandFactory=null;
    SdcLoadProfile sdcLoadProfile=null;
    ObisCodeMapper ocm = null;
	private int iSecurityLevelProperty;
	private int extendedLogging;
	private boolean software7E1;


    abstract protected RegisterConfig getRegs();

    /** Creates a new instance of Sdc */
    public SdcBase() {
        super(false); // true for datareadout;
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getSdcLoadProfile().getProfileData(lastReading, includeEvents);
    }

    protected void doConnect() throws IOException {
        dataReadingCommandFactory = new DataReadingCommandFactory(this);
//        if (extendedLogging >= 1)
//            getRegistersInfo();
    }

    private void getRegistersInfo() throws IOException{
//        StringBuffer strBuff = new StringBuffer();
////        if (getDataReadoutRequest()==1) {
////            strBuff.append("******************* ExtendedLogging *******************\n");
////            strBuff.append(new String(getDataReadout()));
////        }
////        else {
//            strBuff.append("******************* ExtendedLogging *******************\n");
//            strBuff.append("1.0.1.8.128.255: Active Energy tariff HV" + "\n");
//            strBuff.append("1.0.1.8.129.255: Active Energy tariff HP" + "\n");
//            strBuff.append("1.0.1.8.130.255: Active Energy tariff HC" + "\n");
//            strBuff.append("1.0.1.8.131.255: Active Energy tariff HSV" + "\n");
//            strBuff.append("1.0.3.8.128.255: Reactive Energy inductive tariff HV" + "\n");
//            strBuff.append("1.0.3.8.132.255: Reactive Energy inductive tariff HFV" + "\n");
//            strBuff.append("1.0.4.8.128.255: Reactive Energy capacitive tariff HV" + "\n");
//            strBuff.append("1.0.4.8.132.255: Reactive Energy capacitive tariff HFV" + "\n");
//            strBuff.append("1.0.1.6.128.255: Active Energy maximum demand tariff HV" + "\n");
//            strBuff.append("1.0.1.6.132.255: Active Energy maximum demand tariff HFV" + "\n");
//            strBuff.append("1.0.3.6.128.255: Reactive Energy maximum demand inductive tariff HV" + "\n");
//            strBuff.append("1.0.3.6.132.255: Reactive Energy maximum demand inductive tariff HFV" + "\n");
//            strBuff.append("1.0.4.6.128.255: Reactive Energy maximum demand capacitive tariff HV" + "\n");
//            strBuff.append("1.0.4.6.132.255: Reactive Energy maximum demand capacitive tariff HFV" + "\n");
//            strBuff.append("1.0.1.8.0.255: Active Energy total (all phases)" + "\n");
//            strBuff.append("1.0.3.8.0.255: Reactive Energy inductive total (all phases)" + "\n");
//            strBuff.append("1.0.4.8.0.255: Reactive Energy capacitive total (all phases)" + "\n");
//            strBuff.append("*******************************************************\n");
////        }
//        getLogger().info(strBuff.toString());
    }

    protected void doDisConnect() throws IOException {
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
    	StringBuffer strBuff = new StringBuffer();
    	strBuff.append("******************* ExtendedLogging *******************\n");
    	strBuff.append("1.0.1.8.128.255: Active Energy tariff HV" + "\n");
    	strBuff.append("1.0.1.8.129.255: Active Energy tariff HP" + "\n");
    	strBuff.append("1.0.1.8.130.255: Active Energy tariff HC" + "\n");
    	strBuff.append("1.0.1.8.131.255: Active Energy tariff HSV" + "\n");
    	strBuff.append("1.0.3.8.128.255: Reactive Energy inductive tariff HV" + "\n");
    	strBuff.append("1.0.3.8.132.255: Reactive Energy inductive tariff HFV" + "\n");
    	strBuff.append("1.0.4.8.128.255: Reactive Energy capacitive tariff HV" + "\n");
    	strBuff.append("1.0.4.8.132.255: Reactive Energy capacitive tariff HFV" + "\n");
    	strBuff.append("1.0.1.6.128.255: Active Energy maximum demand tariff HV" + "\n");
    	strBuff.append("1.0.1.6.132.255: Active Energy maximum demand tariff HFV" + "\n");
    	strBuff.append("1.0.3.6.128.255: Reactive Energy maximum demand inductive tariff HV" + "\n");
    	strBuff.append("1.0.3.6.132.255: Reactive Energy maximum demand inductive tariff HFV" + "\n");
    	strBuff.append("1.0.4.6.128.255: Reactive Energy maximum demand capacitive tariff HV" + "\n");
    	strBuff.append("1.0.4.6.132.255: Reactive Energy maximum demand capacitive tariff HFV" + "\n");
    	strBuff.append("1.0.1.8.0.255: Active Energy total (all phases)" + "\n");
    	strBuff.append("1.0.3.8.0.255: Reactive Energy inductive total (all phases)" + "\n");
    	strBuff.append("1.0.4.8.0.255: Reactive Energy capacitive total (all phases)" + "\n");
    	strBuff.append("*******************************************************\n");
    	return strBuff.toString();
    }


    public int getNumberOfChannels() throws UnsupportedException, IOException {
       return getSdcLoadProfile().getNrOfChannels();
    }

    protected List doGetOptionalKeys() {
        return null;
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
//    	properties.setProperty("SecurityLevel","0");
    	extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0").trim());
        this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
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

    static public final String COMMAND_CANNOT_BE_EXECUTED="([4])";
    static public final String ERROR_SIGNATURE="([";

    static Map exceptionInfoMap = new HashMap();
    static {
        exceptionInfoMap.put("([1])","General error, insufficient access rights");
        exceptionInfoMap.put("([2])","The nr of command parameters is not correct");
        exceptionInfoMap.put("([3])","The value of a command parameters is not valid");
        exceptionInfoMap.put(COMMAND_CANNOT_BE_EXECUTED,"The command is formally correct, but it cannot be executed in this context");
        exceptionInfoMap.put("([6])","EEPROM write error");
        exceptionInfoMap.put("([7])","Core communication error");
    }

    public String getExceptionInfo(String id) {

        String exceptionInfo = (String)exceptionInfoMap.get(id);
        if (exceptionInfo != null)
            return id+", "+exceptionInfo;
        else
            return "No meter specific exception info for "+id;
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

    	if (ocm == null)
    		ocm = new ObisCodeMapper(getDataReadingCommandFactory(),getTimeZone(),getRegs());

        return ocm.getRegisterValue(obisCode);
    }

    /**
     * Getter for property sdcLoadProfile.
     * @return Value of property sdcLoadProfile.
     */
    public com.energyict.protocolimpl.iec1107.sdc.SdcLoadProfile getSdcLoadProfile() {
        return sdcLoadProfile;
    }

    public String getSerialNumber(){
        try {
            ObisCode oc = new ObisCode(1,0,0,0,0,255);
            String str = readRegister(oc).getText();
            return str.substring(str.indexOf(",") + 2 );
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

} // class Sdc
