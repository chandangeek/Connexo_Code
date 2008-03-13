/*
 * S200.java
 *
 * Created on 18 juli 2006, 13:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200;

import com.energyict.dialer.core.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.landisgyr.sentry.s200.core.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author Koen
 |09052007&Change to ForceStatusCommand.java|
 */

public class S200 extends AbstractProtocol {
    
    private S200Connection s200Connection=null;
    private CommandFactory commandFactory = null;
    private S200Profile s200Profile=null;
    private int crnInitialValue;
    private int modeOfOperation;
    
    /** Creates a new instance of S200 */
    public S200() {
    }
    
    protected void doConnect() throws IOException {
        getCommandFactory().getForceStatusCommand();
    }
    
    protected void doDisConnect() throws IOException {
        getCommandFactory().hangup();
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getS200Profile().getProfileData(lastReading,includeEvents); 
    }     
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"0000000"));
        setInfoTypePassword(properties.getProperty(MeterProtocol.PASSWORD,"0000"));
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0")));
        setCrnInitialValue(Integer.parseInt(properties.getProperty("CRNInitialValue","-1")));
        setModeOfOperation(Integer.parseInt(properties.getProperty("ModeOfOperation","0"),16));
    }
    
    protected List doGetOptionalKeys() {
        List list = new ArrayList();
        list.add("CRNInitialValue");
        list.add("ModeOfOperation");
        return list;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return getCommandFactory().getLookAtCommand().getNrOfInputs();
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        return getCommandFactory().getBeginRecordTimeCommand().getProfileInterval()*60;
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "Version="+getCommandFactory().getVerifyCommand().getSoftwareVersion()+", Revision="+getCommandFactory().getRevisionLevelCommand().getRev();
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        
        s200Connection = new S200Connection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getCrnInitialValue());
        commandFactory = new CommandFactory(this);
        setS200Profile(new S200Profile(this));
        
        return s200Connection;
    }
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        
        for (int channelNr=0;channelNr<getNumberOfChannels();channelNr++) {
            ObisCode obisCode = ObisCode.fromString("1."+channelNr+".82.8.0.255");
            strBuff.append(obisCode+", "+ObisCodeMapper.getRegisterInfo(obisCode)+"\n");
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
    

    
    public Date getTime() throws IOException {
        return getCommandFactory().getQueryTimeCommand().getTime();
    }
    
    public void setTime() throws IOException {
        getCommandFactory().getEnterTimeCommand();
    }
    
    public String getProtocolVersion() {
        return "$Revision: 1.8 $";
    }
    
    
    static public void main(String[] args) {
        try {
            
        String[] phones=new String[]{"0018036281317"};
        String[] clientPasswords=new String[]{"0000"};
        String preDial="0";
        int index=0;
        
            // ********************** Dialer **********************
            Dialer dialer = DialerFactory.getDefault().newDialer();
            
            dialer.init("COM1");
            dialer.getSerialCommunicationChannel().setParams(9600,
                                                            SerialCommunicationChannel.DATABITS_8,
                                                            SerialCommunicationChannel.PARITY_NONE,
                                                            SerialCommunicationChannel.STOPBITS_1);            
            //dialer.getSerialCommunicationChannel().setDTR(false);
            dialer.connect(preDial+phones[index],60000); 
            
            //dialer.connect();
            
            // ********************** Properties **********************
            Properties properties = new Properties();
            properties.setProperty("ProfileInterval", "900");
            properties.setProperty(MeterProtocol.PASSWORD,clientPasswords[index]);
            properties.setProperty(MeterProtocol.NODEID,"1000000");
            // ********************** EictRtuModbus **********************
            S200 s200 = new S200();
            if (DialerMarker.hasOpticalMarker(dialer))
                ((HHUEnabler)s200).enableHHUSignOn(dialer.getSerialCommunicationChannel());
            
            s200.setHalfDuplexController(dialer.getHalfDuplexController());
            s200.setProperties(properties);
            s200.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("GMT-5"),Logger.getLogger("name"));
            s200.connect();
            
            System.out.println(s200.getCommandFactory().getForceStatusCommand());
            System.out.println(s200.getCommandFactory().getLookAtCommand());
            for (int i=0;i<s200.getCommandFactory().getLookAtCommand().getNrOfInputs();i++) {
                System.out.println(s200.getCommandFactory().getMeterDataCommand(i));
            }
            System.out.println(s200.getCommandFactory().getRevisionLevelCommand());
            System.out.println(s200.getCommandFactory().getVerifyCommand());
            

            System.out.println(new Date());
            System.out.println(s200.getTime());
            //s200.setTime();
            //System.out.println(s200.getTime());
            
            
            System.out.println(s200.getFirmwareVersion());
            System.out.println(s200.getProfileInterval());
            
//            DataDumpFactory ddf = new DataDumpFactory(s200.getCommandFactory());
//            ProtocolUtils.printResponseData(ddf.collectHistoryLogDataBlocks());
//            System.out.println("-----------------------------------");
//            ProtocolUtils.printResponseData(ddf.collectLoadProfileDataBlocks(10));
            

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH,-4);
            System.out.println(s200.getProfileData(cal.getTime(),true));
            
            
            s200.disconnect();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }    

    public S200Connection getS200Connection() {
        return s200Connection;
    }

    private void setS200Connection(S200Connection s200Connection) {
        this.s200Connection = s200Connection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public S200Profile getS200Profile() {
        return s200Profile;
    }

    private void setS200Profile(S200Profile s200Profile) {
        this.s200Profile = s200Profile;
    }

    public int getCrnInitialValue() {
        return crnInitialValue;
    }

    public void setCrnInitialValue(int crnInitialValue) {
        this.crnInitialValue = crnInitialValue;
    }

    public int getModeOfOperation() {
        return modeOfOperation;
    }

    public void setModeOfOperation(int modeOfOperation) {
        this.modeOfOperation = modeOfOperation;
    }
    
} // S200
