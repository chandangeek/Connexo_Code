/*
 * IndigoPlus.java
 *
 * Created on 5 juli 2004, 14:56
 */

package com.energyict.protocolimpl.iec1107.indigo;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.DialerMarker;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.iec1107.AbstractIEC1107Protocol;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;


// KV TO_DO
/*
 * Scaling of registers and profiledata using MeterDefinition and HistoricalData
 * Interpretation of profile daily flags and channel status flags 
 * Determine difference between gaps and zero-consumption
 * Behaviour of whole current meters
 * Implementation and mapping of registers
 * Test with IMServ production meters
 * Set time!!!
 * Password encryption level 2 and 3?
 */

/**
 *
 * @author  Koen
 * @beginchanges
KV|05092004|Add infotype property StatusFlagChannel
KV|26112004|Set default nodeaddress to 001!
KV|01122004|Ignore negative values...
||Change statuschannel mechanism...
KV|23032005|Changed header to be compatible with protocol version tool
KV|30032005|Handle StringOutOfBoundException in IEC1107 connection layer
KV|18052005|Workaround to avoid exception when meter does not respond correct with channel status flag data
KV|27072005|Use CTVT logical address instead of current historical value logical address
KV|15122005|Test if protocol retrieved new interval data, intervals retrieved between from and to
 * @endchanges
 */
public class IndigoPlus extends AbstractIEC1107Protocol {
    
    private static final int DEBUG=0;
    
    LogicalAddressFactory logicalAddressFactory;
    IndigoProfile indigoProfile;
    int statusFlagChannel,readCurrentDay;
    int emptyNodeAddress;
    
    /** Creates a new instance of IndigoPlus */
    public IndigoPlus() {
        super(false,new Encryption());
    }

    @Override
    public String getProtocolDescription() {
        return "Actaris Indigo+ IEC1107";
    }

    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendarFrom = ProtocolUtils.getCalendar(getTimeZone());
        calendarFrom.setTime(lastReading);
        Calendar calendarTo = ProtocolUtils.getCalendar(getTimeZone());
        calendarTo.setTime(new Date());
        return indigoProfile.getProfileData(calendarFrom.getTime(),calendarTo.getTime(),isStatusFlagChannel(),isReadCurrentDay());
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
       int nrOfChannels = getLogicalAddressFactory().getMeteringDefinition().getNrOfIntervalRecordingChannels();   
       if ((!(isStatusFlagChannel())) && (getLogicalAddressFactory().getMeteringDefinition().isChannelUnitsStatusFlagsChannel())) 
          return nrOfChannels-1;
       return nrOfChannels;
    }
    
    protected void doConnect() throws java.io.IOException {
        logicalAddressFactory = new LogicalAddressFactory((ProtocolLink)this,(MeterExceptionInfo)this);
        indigoProfile = new IndigoProfile((ProtocolLink)this,(MeterExceptionInfo)this,logicalAddressFactory);
    }    
    
    /*
     *  extendedLogging = 1 current set of logical addresses, extendedLogging = 2..17 historical set 1..16
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("************************* Extended Logging *************************\n");
        strBuff.append(getLogicalAddressFactory().getMeterIdentity().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getMeterStatus().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDefaultStatus().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDateTimeGMT().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDateTimeLocal().toString()+"\n");
        
        strBuff.append(getLogicalAddressFactory().getTotalRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getRateRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDemandRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getDefaultRegisters(extendedLogging-1).toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getHistoricalData(extendedLogging-1).toString()+"\n");
        
        strBuff.append(getLogicalAddressFactory().getMeteringDefinition().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getClockDefinition().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getBillingPeriodDefinition().toString()+"\n");
        strBuff.append(getLogicalAddressFactory().getGeneralMeterData().toString()+"\n");
        
        
        for (int i=-1;i<16;i++) {
            int billingPoint;
            if (i==-1) billingPoint = 255;
            else billingPoint = i;
            strBuff.append("Cumulative registers (total & tariff):\n");
            for(int obisC=1;obisC<=9;obisC++) {
                String code = "1.1."+obisC+".8.0."+billingPoint;
                strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
            }
            for(int obisC=1;obisC<=2;obisC++) {
                for(int obisE=1;obisE<=9;obisE++) {
                    String code = "1.1."+obisC+".8."+obisE+"."+billingPoint;
                    strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
                }
            }
            String defaultRegCode = "1.2.1.8.0."+billingPoint;
            strBuff.append(defaultRegCode+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))+" default register\n");
            defaultRegCode = "1.2.9.8.0."+billingPoint;
            strBuff.append(defaultRegCode+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))+" default register\n");
            defaultRegCode = "1.2.129.8.0."+billingPoint;
            strBuff.append(defaultRegCode+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(defaultRegCode))+" default register\n");

            strBuff.append("Cumulative maximum demand registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND,billingPoint));
            
            strBuff.append("Current average registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_RISING_DEMAND,billingPoint));
            
            strBuff.append("Maximum demand registers:\n");
            strBuff.append(buildDemandReg(0,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(1,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(2,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            strBuff.append(buildDemandReg(3,ObisCode.CODE_D_MAXIMUM_DEMAND,billingPoint));
            
            strBuff.append("General purpose registers:\n");
            if( billingPoint != 255) {
                String code = "1.1.0.1.2."+billingPoint;
                strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
            }
            if( billingPoint == 255) {
               String code = "1.1.0.1.0.255";
               strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
            }
        }
        return strBuff.toString();
    }
    
    private String buildDemandReg(int index, int obisD, int billingPoint) throws IOException {
       int obisC;
       if (obisD == ObisCode.CODE_D_RISING_DEMAND) 
         obisC = DemandRegisters.OBISCMAPPINGRISINGDEMAND[index];  
       else    
         obisC = getLogicalAddressFactory().getHistoricalData(billingPoint==255?0:billingPoint).getObisC(index);
       if (obisC != 255) {
          String code = "1.1."+obisC+"."+obisD+".0."+billingPoint;
          return code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n";
       }
       return "";
    }
    
    public Date getTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCleanCalendar(getTimeZone());
        calendar.setTime(getLogicalAddressFactory().getDateTimeGMT().getDate());
        return calendar.getTime();
    }
    
    public void setTime() throws IOException {
        Calendar calendar=null;
        calendar = ProtocolUtils.getCalendar(getTimeZone());
        calendar.add(Calendar.MILLISECOND,getRoundtripCorrection());
        setTime(calendar.getTime()); 
    }
    
    private void setTime(Date date) throws IOException {
        try {
           getLogicalAddressFactory().setDateTimeGMT(date); 
        }
        catch(FlagIEC1107ConnectionException e) {
            throw new IOException("setTime() error, possibly wrong password level! "+e.toString());
        }
    }
    
    protected java.util.List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("StatusFlagChannel");
        result.add("ReadCurrentDay");
        result.add("EmptyNodeAddress");
        return result;
    }
    
    protected void doValidateProperties(java.util.Properties properties) throws com.energyict.protocol.MissingPropertyException, com.energyict.protocol.InvalidPropertyException {
        statusFlagChannel = Integer.parseInt(properties.getProperty("StatusFlagChannel","0"));
        readCurrentDay = Integer.parseInt(properties.getProperty("ReadCurrentDay","0"));
        emptyNodeAddress = Integer.parseInt(properties.getProperty("EmptyNodeAddress","0"));
        setNodeId(properties.getProperty(MeterProtocol.NODEID,(emptyNodeAddress==0?"001":"")));
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return getLogicalAddressFactory().getMeterIdentity().getSoftwareVersionNumber();
        
    }
    
    public String getSerialNumber() throws IOException {
       return getLogicalAddressFactory().getMeterIdentity().getMeterId();
    }
    
    /*  
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {
        
         boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = getSerialNumber().trim();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
        
    }
    
    // ********************************************************************************************************
    // main
    public static void main(String[] args) {
        Dialer dialer=null;
        IndigoPlus indigoPlus=null;
        try {
//            dialer =DialerFactory.getDefault().newDialer();
//            dialer.init("COM1","AT+MS=2,0,2400,2400");
//            dialer.getSerialCommunicationChannel().setParams(2400,
//                                                             SerialCommunicationChannel.DATABITS_7,
//                                                             SerialCommunicationChannel.PARITY_EVEN,
//                                                             SerialCommunicationChannel.STOPBITS_1);
//            dialer.connect("0,00441908607743",60000);
            dialer =DialerFactory.getOpticalDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("",60000); 
            InputStream is = dialer.getInputStream();
            OutputStream os = dialer.getOutputStream();
            indigoPlus = new IndigoPlus();
            Properties properties = new Properties();
            properties.setProperty("SecurityLevel","2");
            properties.setProperty(MeterProtocol.PASSWORD,"ABCDEF"); //13579B"); //"123456");
            properties.setProperty("ProfileInterval", "1800");
            if (!(DialerMarker.hasOpticalMarker(dialer)))
              properties.setProperty(MeterProtocol.NODEID,"002");
            else    
              properties.setProperty(MeterProtocol.NODEID,"");
            indigoPlus.setProperties(properties);
            if (!(DialerMarker.hasOpticalMarker(dialer)))
                dialer.getSerialCommunicationChannel().setParamsAndFlush(2400,
                                                                         SerialCommunicationChannel.DATABITS_7,
                                                                         SerialCommunicationChannel.PARITY_EVEN,
                                                                         SerialCommunicationChannel.STOPBITS_1);
            else
                dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                         SerialCommunicationChannel.DATABITS_7,
                                                                         SerialCommunicationChannel.PARITY_EVEN,
                                                                         SerialCommunicationChannel.STOPBITS_1);
                
            // initialize
            indigoPlus.init(is,os,TimeZone.getTimeZone("GMT"),Logger.getLogger("name"));
            
            // KV 18092003
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)indigoPlus).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("Start session");
            
            indigoPlus.connect();
             
            System.out.println(indigoPlus.getLogicalAddressFactory().getMeterIdentity().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getMeterStatus().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDefaultStatus().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDateTimeGMT().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDateTimeLocal().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getTotalRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getRateRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDemandRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getDefaultRegisters().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getHistoricalData().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getMeteringDefinition().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getClockDefinition().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getBillingPeriodDefinition().toString());
            System.out.println(indigoPlus.getLogicalAddressFactory().getGeneralMeterData().toString());
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,-10);
            System.out.println(indigoPlus.getProfileData(calendar.getTime(),true).toString());
            Date date = indigoPlus.getTime();
            System.out.println(date);
            //indigoPlus.setTime();
            System.out.println("End session");
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally{
            try {
                indigoPlus.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Getter for property logicalAddressFactory.
     * @return Value of property logicalAddressFactory.
     */
    public com.energyict.protocolimpl.iec1107.indigo.LogicalAddressFactory getLogicalAddressFactory() {
        return logicalAddressFactory;
    }    
    
    /*******************************************************************************************
     M e t e r E x c e p t i o n I n f o  i n t e r f a c e 
     *******************************************************************************************/
    /*
     *  This method must be overridden by the subclass to implement meter specific error
     *  messages. Us sample code of a static map with error codes below as a sample and 
     *  use code in method as a sample of how to retrieve the error code.  
     *  This code has been taken from a real protocol implementation.
     */

    static Map exceptionInfoMap = new HashMap();
    static {
           exceptionInfoMap.put("ERRDAT","Error setting the time");
           exceptionInfoMap.put("ERRADD","Protocol error");
    }
 
    public String getExceptionInfo(String id) {
        
        String exceptionInfo = (String)exceptionInfoMap.get(id);
        if (exceptionInfo != null)
           return id+", "+exceptionInfo;
        else
           return "No meter specific exception info for "+id; 
    }        
    
    /*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e 
     *******************************************************************************************/
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(getLogicalAddressFactory());
        return ocm.getRegisterValue(obisCode);
    }
    
    /**
     * Getter for property statusFlagChannel.
     * @return Value of property statusFlagChannel.
     */
    public boolean isStatusFlagChannel() {
        return statusFlagChannel==1;
    }
    
    public boolean isReadCurrentDay() {
        return readCurrentDay==1;
    }
 }
