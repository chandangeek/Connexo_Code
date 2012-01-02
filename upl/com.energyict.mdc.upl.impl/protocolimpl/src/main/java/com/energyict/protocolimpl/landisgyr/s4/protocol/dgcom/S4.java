/*
 * S4.java
 *
 * Created on 22 mei 2006, 14:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom;

import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.Dialer;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.CommandFactory;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.registermappping.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
/**
 *
 * @author Koen
 */
public class S4 extends AbstractProtocol {
    
    private DGCOMConnection dgcomConnection;
    private CommandFactory commandFactory;
    S4Profile s4Profile;
    private RegisterMapperFactory registerMapperFactory;
    String modemPassword;
    
    /** Creates a new instance of S4 */
    public S4() {
    } 

    
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters. 
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
    }
    
    protected void doConnect() throws IOException {
        // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters. 
        if (commChannel!=null) {
            commChannel.setBaudrate(9600);
            commChannel.getSerialPort().setDTR(getDtrBehaviour()==1);
        }
        else getDgcomConnection().signon();
        
        if (modemPassword!=null)
            getCommandFactory().modemUnlock(modemPassword);
        
        
    }
    
    
    protected void doDisConnect() throws IOException {
        getCommandFactory().logoff();
    } 

    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return s4Profile.getProfileData(from, to, includeEvents);
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException { 
        return getCommandFactory().getDemandIntervalCommand().getProfileInterval()*60;
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
        String sn = getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
        
    }       
    
    protected void validateDeviceId() throws IOException {
        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) return;
        String devId = getCommandFactory().getDeviceIDExtendedCommand().getDeviceID();
        if (devId.compareTo(getInfoTypeDeviceID()) == 0) return;
        throw new IOException("Device ID mismatch! meter devId="+devId+", configured devId="+getInfoTypeDeviceID());
    }       
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));  
        setInfoTypeSecurityLevel(Integer.parseInt(properties.getProperty("SecurityLevel","0").trim()));
        modemPassword = properties.getProperty("ModemPassword");
    }
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("ModemPassword");
        return result;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand().getNrOfActiveChannels();
    }   
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setDgcomConnection(new DGCOMConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel()));
        setCommandFactory(new CommandFactory(this));
        s4Profile = new S4Profile(this);
        registerMapperFactory = new RegisterMapperFactory(this);
        return getDgcomConnection();
    }
    
    public Date getTime() throws IOException {
        return getCommandFactory().getTime();
    }
    
    public void setTime() throws IOException {
        getCommandFactory().setTime();
    }
    
    public String getProtocolVersion() {
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "ProductFamily: "+getCommandFactory().getFirmwareVersionCommand().getProductFamily()+"\nFirmware version: "+getCommandFactory().getFirmwareVersionCommand().getFirmwareVersion()+"\nDGCOM version: "+getCommandFactory().getFirmwareVersionCommand().getDgcomVersion()+"\nDSP revision: "+getCommandFactory().getSerialNumberCommand().getDspRevision();
    }
    
    public String getSerialNumber() throws IOException {
        return ""+getCommandFactory().getSerialNumberCommand().getSerialNumber();
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
        StringBuffer strbuff = new StringBuffer();
//        strbuff.append(getCommandFactory().getLoadProfileMetricSelectionRXCommand());
//        strbuff.append(getCommandFactory().getThirdMetricValuesCommand());
//        strbuff.append(getCommandFactory().getMeasurementUnitsCommand());
        strbuff.append(getRegisterMapperFactory().getRegisterMapper().getRegisterInfo());
        return strbuff.toString();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        S4 s4 = new S4();
        Dialer dialer=null;
        try {
            
// direct rs232 connection
            //dialer =DialerFactory.getDirectDialer().newDialer();
            dialer =DialerFactory.getOpticalDialer().newDialer();
            dialer.init("COM1");
            dialer.connect("",60000); 
            
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)s4).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            //properties.setProperty("SecurityLevel","2");
            properties.setProperty(MeterProtocol.PASSWORD,"000000");
            
            //properties.setProperty(MeterProtocol.ADDRESS,"RETAILR");
            properties.setProperty("DTRBehaviour","0");
                    
            
            properties.setProperty("ProfileInterval", "900");
            //properties.setProperty(MeterProtocol.NODEID,"1234");
//            properties.setProperty("SerialNumber","204006174"); // multidrop + serial number check...
//            properties.setProperty("HalfDuplex", "50");
            //properties.setProperty("Retries", "0");
            
// transfer the properties to the protocol
            s4.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            s4.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)s4).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            s4.connect();
            
            //System.out.println(s4.readRegister(ObisCode.fromString("1.1.9.16.0.0")));
//            System.out.println(s4.getSerialNumber());
            System.out.println(s4.getFirmwareVersion());
            System.out.println(s4.getCommandFactory().getS4Configuration());
    

System.out.println("*************************************************** ALL VERSIONS SCALE & K-FACTOR ***************************************************");            
System.out.println(s4.getCommandFactory().getKFactorCommand());
System.out.println(s4.getCommandFactory().getScaleFactorCommand());
System.out.println(s4.getCommandFactory().getSelfReadConfigurationCommand());
            
System.out.println("*************************************************** ALL VERSIONS REGISTERS ***************************************************");            
System.out.println(s4.getCommandFactory().getDemandIntervalCommand());
System.out.println(s4.getCommandFactory().getHighestMaximumDemandsCommand());
System.out.println(s4.getCommandFactory().getMeasurementUnitsCommand());
System.out.println(s4.getCommandFactory().getNegativeEnergyCommand());
System.out.println(s4.getCommandFactory().getPresentDemandCommand());
System.out.println(s4.getCommandFactory().getPreviousIntervalDemandCommand());
System.out.println(s4.getCommandFactory().getPreviousSeasonDemandDataCommand());
System.out.println(s4.getCommandFactory().getThirdMetricValuesCommand());

System.out.println("*************************************************** DX METER ***************************************************");            
if (s4.getCommandFactory().getFirmwareVersionCommand().isDX()) {
    System.out.println(s4.getCommandFactory().getCurrentSeasonCumulativeDemandDataDXCommand());       
    System.out.println(s4.getCommandFactory().getCurrentSeasonLastResetValuesDXCommand());
    
    System.out.println(s4.getCommandFactory().getCurrentSeasonTOUDemandDataDXCommand());
    System.out.println(s4.getCommandFactory().getPreviousSeasonLastResetValuesDXCommand());
    
    System.out.println(s4.getCommandFactory().getPreviousSeasonTOUDataDXCommand());
    System.out.println(s4.getCommandFactory().getRateBinsAndTotalEnergyDXCommand());
    System.out.println(s4.getCommandFactory().getSelfReadDataDXCommand(0));
    
}            
System.out.println("*************************************************** RX METER ***************************************************");            
if (s4.getCommandFactory().getFirmwareVersionCommand().isRX()) {
    System.out.println(s4.getCommandFactory().getCurrentSeasonCumDemandAndLastResetRXCommand());            
    
    System.out.println(s4.getCommandFactory().getCurrentSeasonTOUDemandDataRXCommand());
    
    System.out.println(s4.getCommandFactory().getPreviousSeasonTOUDataRXCommand());
    System.out.println(s4.getCommandFactory().getRateBinsAndTotalEnergyRXCommand());
    System.out.println(s4.getCommandFactory().getSelfReadDataRXCommand(0));
}            
            
//            System.out.println(s4.getCommandFactory().getTOUAndLoadProfileOptions());
//            System.out.println(s4.getCommandFactory().getErrorCodesCommand());
//            System.out.println(s4.getCommandFactory().getDeviceIDExtendedCommand());
//            System.out.println(s4.getCommandFactory().getDemandIntervalCommand());
//            System.out.println(s4.getCommandFactory().getNegativeEnergyCommand());
//            System.out.println(s4.getCommandFactory().getPresentDemandCommand());
//            System.out.println(s4.getCommandFactory().getMeasurementUnitsCommand());
            
            
/*            System.out.println(s4.getCommandFactory().getHighestMaximumDemandsCommand());
            System.out.println(s4.getCommandFactory().getPreviousSeasonDemandDataCommand());
            System.out.println(s4.getCommandFactory().getPreviousSeasonTOUDataRXCommand());
             System.out.println(s4.getCommandFactory().getPreviousIntervalDemandCommand());
            System.out.println(s4.getCommandFactory().getCurrentSeasonCumDemandAndLastResetRXCommand());
            System.out.println(s4.getCommandFactory().getCurrentSeasonTOUDemandDataRXCommand());
             System.out.println(s4.getCommandFactory().getRateBinsAndTotalEnergyRXCommand());
             System.out.println(s4.getCommandFactory().getKFactorCommand());
             System.out.println(s4.getCommandFactory().getThermalKFactorCommand());
            System.out.println(s4.getCommandFactory().getKhValueCommand());
            System.out.println(s4.getCommandFactory().getScaleFactorCommand());
 **/
            System.out.println(s4.getCommandFactory().getSelfReadDataRXCommand(0));
//            System.out.println(s4.getCommandFactory().getSelfReadDataRXCommand(1));
//            System.out.println(s4.getCommandFactory().getSelfReadDataRXCommand(2));
            
            
//            System.out.println(s4.getCommandFactory().getLoadProfileAndSeasonChangeOptionsCommand());
//            System.out.println(s4.getCommandFactory().getLoadProfileMetricSelectionRXCommand());
            
            
            
//            System.out.println("Meter:  "+s4.getTime());
//            System.out.println("System: "+new Date());
            //s4.setTime();

//            Calendar from = Calendar.getInstance();
//            from.add(Calendar.DAY_OF_MONTH,-1);
//            System.out.println(s4.getProfileData(from.getTime(),null,true));

            s4.disconnect();
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        
    }    

    public DGCOMConnection getDgcomConnection() {
        return dgcomConnection;
    }

    private void setDgcomConnection(DGCOMConnection dgcomConnection) {
        this.dgcomConnection = dgcomConnection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public RegisterMapperFactory getRegisterMapperFactory() {
        return registerMapperFactory;
    }

    
}
