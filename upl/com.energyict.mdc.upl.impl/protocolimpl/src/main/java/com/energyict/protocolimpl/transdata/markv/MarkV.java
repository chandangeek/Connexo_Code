/*
 * MarkV.java
 *
 * Created on 8 augustus 2005, 10:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.energyict.protocol.HalfDuplexEnabler;    
import com.energyict.protocolimpl.base.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
//import com.energyict.protocolimpl.transdata.markv.core.*;
import com.energyict.protocolimpl.transdata.markv.core.connection.*;
import com.energyict.protocolimpl.transdata.markv.core.commands.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocolimpl.transdata.markv.core.MarkVProfile;
import com.energyict.dialer.connection.ConnectionException;
/**
 *
 * @author koen
 */
public class MarkV extends AbstractProtocol {
    
    MarkVConnection markVConnection=null;
    MarkVProfile markVProfile=null;
    CommandFactory commandFactory=null;
    int verifyTimeDelay;
    
    /** Creates a new instance of MarkV */
    public MarkV() {
    }
    
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters. 
    SerialCommunicationChannel commChannel;
    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
        this.commChannel=commChannel;
        markVConnection.setSerialCommunicationChannel(commChannel);
    }
    
    protected void doConnect() throws IOException {
    }
    
    
    protected void doDisConnect() throws IOException {
        getCommandFactory().issueLOCommand();
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new UnsupportedException();
    }
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        
        strBuff.append(getCommandFactory().getISCommand()+"\n");
        strBuff.append(getCommandFactory().getMICommand()+"\n");
        
        Iterator it = RegisterIdentification.getRegisterDataIds().iterator();
        while(it.hasNext()) {
            RegisterDataId rdi = (RegisterDataId)it.next();
            strBuff.append(rdi+"\n");
        }
        return strBuff.toString();
        
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
    
  /*  
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = getCommandFactory().getMICommand().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
        
    }        
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return markVProfile.getProfileData(lastReading,includeEvents);
    }
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        verifyTimeDelay=Integer.parseInt(properties.getProperty("VerifyTimeDelay","2000").trim());
    }
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("VerifyTimeDelay");
        return result;
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.6 $";
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getCommandFactory().getISCommand().getProfileInterval();
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getCommandFactory().getDCCommand().getProtocolChannelMap().getNrOfProtocolChannels();
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeout,int maxRetries,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        markVConnection = new MarkVConnection(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController);
        markVConnection.setDtrBehaviour(getDtrBehaviour());
        commandFactory = new CommandFactory(this);
        markVProfile = new MarkVProfile(this);
        //classFactory = new ClassFactory(this);
        //alphaPlusProfile = new AlphaPlusProfile(this);
        return markVConnection;
    }
    
    
    public Date getTime() throws IOException {
        return getCommandFactory().getGTCommand().getDate();
    }    
    
    public void setTime() throws IOException {
        getCommandFactory().issueTICommand();
        if (!verifySetTime(new Date(),getTime())) {
            getCommandFactory().issueTICommand();
            if (!verifySetTime(new Date(),getTime()))
                throw new IOException("MarkV, setTime(), after 2 tries, the meter time still differs more then "+verifyTimeDelay+" ms (metertime="+getTime()+", systemtime="+new Date()+")");
        }
        
        
    }
    
    
    public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("SecurityLevel", "0");
        if ((discoverInfo.getNodeId()!= null) && ("".compareTo(discoverInfo.getNodeId()) != 0))
            properties.setProperty(MeterProtocol.NODEID,discoverInfo.getNodeId());
        setProperties(properties);
        init(discoverInfo.getCommChannel().getInputStream(),discoverInfo.getCommChannel().getOutputStream(),null,null);
        connect();
        BufferedReader br = new BufferedReader(new StringReader(new String(markVConnection.receiveWithTimeout("ID"))));
        br.readLine(); // skip ID code 1
        br.readLine(); // skip ID code 2
        String serialNumber = br.readLine().trim();
        //String serialNumber =  getCommandFactory().getIDCommand().getSerialNr();
        //disconnect(); // KV 13102005 LO command does not react here...
        return serialNumber;
    }    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MarkV markV=null;
        Dialer dialer=null;
                         //  CRESCENT REAL ESTATE777     testmeter at TransData
        String[] phones={   "00018173363569,,,,,,1",    "00019724180460", "101"};
        String[] passwords={"74944122",                 "22222222", "22222222"};
        
        
        final int selection=2; 
        
        
        try {
// ********************************** DIALER ***********************************$            
// modem dialup connection
            dialer =DialerFactory.getDefault().newDialer();
            dialer.init("COM1"); //,"ATV1M1S7=120S6=6S10=50");
            //dialer.getSerialCommunicationChannel().setParams(2400,
            //                                                 SerialCommunicationChannel.DATABITS_7,
            //                                                 SerialCommunicationChannel.PARITY_EVEN,
             //                                                SerialCommunicationChannel.STOPBITS_1);
//            dialer.connect("phonenumber",60000);
            
// optical head connection
//            dialer =DialerFactory.getOpticalDialer().newDialer();
//            dialer.init("COM1");
//            dialer.connect("",60000); 
            
// direct rs232 connection
//            dialer =DialerFactory.getDirectDialer().newDialer();
//            dialer.init("COM4");
//            dialer.connect("",60000); 
            //00018173853675
            
            dialer.connect(phones[selection],90000);  
            
            
            
            
            //dialer.connect("4",60000); 
// *********************************** PROTOCOL ******************************************$            
            markV = new MarkV(); // instantiate the protocol

            System.out.println("Serial number = "+markV.getSerialNumber(new DiscoverInfo(dialer.getSerialCommunicationChannel(),null)));
            if (true)
                return;
            
            
//            System.out.println("Serial number = "+alphaPlus.getSerialNumber(new DiscoverInfo(dialer.getSerialCommunicationChannel(),null)));
//            if (true)
//                return;
//            
//            
//            System.out.println("Serial number = "+alphaPlus.getSerialNumber(new DiscoverInfo(dialer.getSerialCommunicationChannel(),null)));
//            if (true)
//                return;
          
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            //properties.setProperty("SecurityLevel","2");
            properties.setProperty(MeterProtocol.PASSWORD,passwords[selection]);
            properties.setProperty("ProfileInterval", "900");
            //properties.setProperty(MeterProtocol.NODEID,"0");
//            properties.setProperty("HalfDuplex", "50");
            //properties.setProperty("Retries", "0");
            
// transfer the properties to the protocol
            markV.setProperties(properties); 
            
//            ez7.setHalfDuplexController(dialer.getHalfDuplexController());
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
                                                                     SerialCommunicationChannel.DATABITS_8,
                                                                     SerialCommunicationChannel.PARITY_NONE,
                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            //markV.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("EST"),Logger.getLogger("name"));
            markV.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("CST"),Logger.getLogger("name"));
            
// if optical head dialer, enable the HHU signon mechanism
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)markV).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            System.out.println("*********************** connect() ***********************");
            
// connect to the meter            
            markV.connect();
            byte[] data=null;

            //System.out.println(markV.getCommandFactory().getIICommand());
            System.out.println(markV.getCommandFactory().getGTCommand());
            System.out.println(markV.getCommandFactory().getMICommand());
            //System.out.println(markV.getCommandFactory().getSCCommand());
            //System.out.println(markV.getCommandFactory().getHCCommand());
            //System.out.println(markV.getCommandFactory().getCCCommand());
            //System.out.println(markV.getCommandFactory().getRVCommand(768));
            //System.out.println(markV.getCommandFactory().getRCCommand(768));
            
            
            System.out.println("*********************** Meter information ***********************");
            System.out.println(markV.getNumberOfChannels());
            System.out.println(markV.getProtocolVersion());
            System.out.println(markV.getProfileInterval());
        
// get the meter profile data            
            System.out.println("*********************** getProfileData() ***********************");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE,-1);
            System.out.println(markV.getProfileData(calendar.getTime(),true));
// get the metertime            
            System.out.println("*********************** getTime() ***********************");
            Date date = markV.getTime();  
            System.out.println(date);
// set the metertime            
//            System.out.println("*********************** setTime() ***********************");
//            ez7.setTime();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                System.out.println("*********************** disconnect() ***********************");
                markV.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }        
    } // MarkV

    public MarkVConnection getMarkVConnection() {
        return markVConnection;
    }
    
    public CommandFactory getCommandFactory() {
        return commandFactory;
    }
    
    /*
     * Getter for the configured device TimeZone.
     * @return Value of the device TimeZone.
     */
    public TimeZone getTimeZone() {
        try {
            if (getCommandFactory().getISCommand().isDstEnabled())
                return super.getTimeZone();
            else
                return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
        }
        catch (IOException e) {
            getLogger().severe("getTimeZone(), Error requesting IS command!, use configured timezone, "+e.toString());
            return super.getTimeZone();
        }
    }    
    
    public void setDialinScheduleTime(Date nextDialin) throws IOException {
        Date nd = new Date(nextDialin.getTime() - nextDialin.getTime()%60000);
        getCommandFactory().issueTCCommand(nd);
        if (!verifySetTime(nd,getCommandFactory().getGCCommand().getDate())) {
            getCommandFactory().issueTCCommand(nd);
            if (!verifySetTime(nd,getCommandFactory().getGCCommand().getDate()))
                throw new IOException("MarkV, setDialinScheduleTime(), after 2 tries, the meter time still differs more then "+verifyTimeDelay+" ms (meter nextDialin="+getCommandFactory().getGCCommand().getDate()+", system nextDialin="+nd+")");
        }
    }
    
    
    /*
     * Because we are working in a terminal mode, we need to verify the time. verifyTimeDelay is a custom property 
     * we need to check the returned time against...
     */
    private boolean verifySetTime(Date src, Date dst) throws IOException {
        //Date system = new Date();
        //Date meter = getTime();
        if (Math.abs(src.getTime() - dst.getTime()) > verifyTimeDelay)
           return false;
        else
           return true;
    }    
}
