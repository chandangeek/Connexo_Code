/*
 * Trimeran.java
 *
 * Created on 19 juni 2006, 16:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.core.TrimeranConnection;
import com.energyict.protocolimpl.edf.trimarancje.core.DataFactory;
import com.energyict.protocolimpl.edf.trimarancje.core.SPDUFactory;
import com.energyict.protocolimpl.edf.trimarancje.registermapping.Register;
import com.energyict.protocolimpl.edf.trimarancje.registermapping.RegisterFactory;

/**
 *@beginchanges
	KV|04012007|Bugfix to correct the year transition behaviour in the load profile data
	GN|20052008|Made a copy of the CVE(trimaran) class to make the CJE protocol
 *@endchanges
 */
public class Trimaran extends AbstractProtocol {
    
    private TrimeranConnection trimeranConnection=null;
    private SPDUFactory sPDUFactory=null;
    private DataFactory dataFactory=null;
    private TrimaranProfile trimeranProfile=null;
    private RegisterFactory registerFactory=null;
    private int interKarTimeout;
    private int ackTimeout;
    private int commandTimeout;
    private int flushTimeout;
    
    /** Creates a new instance of Trimeran */
    public Trimaran() {
    }
    
    
    protected void doConnect() throws IOException {
        getSPDUFactory().logon();
    }
    
    
    protected void doDisConnect() throws IOException {
        getSPDUFactory().logoff();
    } 

    
    // KV_TO_DO extend framework to implement different hhu optical handshake mechanisms for US meters. 
//    SerialCommunicationChannel commChannel;
//    public void enableHHUSignOn(SerialCommunicationChannel commChannel,boolean datareadout) throws ConnectionException {
//        this.commChannel=commChannel;
//    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return getTrimeranProfile().getProfileData();
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException { 
//        return 600;
    	return 300;
    }
    
   /*  
     *  Method must be overridden by the subclass to verify the property 'SerialNumber'
     *  against the serialnumber read from the meter.
     *  Use code below as example to implement the method.
     *  This code has been taken from a real protocol implementation.
     */
    protected void validateSerialNumber() throws IOException {
//        boolean check = true;
//        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
//        String sn = getSerialNumber();
//        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
//        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }       
    
    protected void validateDeviceId() throws IOException {
//        if ((getInfoTypeDeviceID() == null) || ("".compareTo(getInfoTypeDeviceID())==0)) return;
//        String devId = getCommandFactory().getDeviceIDExtendedCommand().getDeviceID();
//        if (devId.compareTo(getInfoTypeDeviceID()) == 0) return;
//        throw new IOException("Device ID mismatch! meter devId="+devId+", configured devId="+getInfoTypeDeviceID());
    }       
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","300").trim())); // TE
        setInfoTypeHalfDuplex(Integer.parseInt(properties.getProperty("HalfDuplex","50").trim())); // TC  
        //setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","6000").trim())); // TL
        
        // KV_DEBUG
        setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","22000").trim())); // TSE (session layer)
        setAckTimeout(Integer.parseInt(properties.getProperty("ACKTimeoutTL","5000").trim())); // TL (datalink layer)
        setInterKarTimeout(Integer.parseInt(properties.getProperty("InterCharTimeout","400").trim())); // 
        
        setCommandTimeout(Integer.parseInt(properties.getProperty("CommandTimeout","3000").trim())); // Command retry timeout
        setFlushTimeout(Integer.parseInt(properties.getProperty("FlushTimeout","500").trim())); // Timeout to wait before sending a new command for receiving duplicate frames send by meter
    }
    
    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("InterCharTimeout");
        result.add("ACKTimeoutTL");
        result.add("CommandTimeout");
        result.add("FlushTimeout");
        
        return result;
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 1;
    }   
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        setSPDUFactory(new SPDUFactory(this));
        setDataFactory(new DataFactory(this));
        trimeranProfile=new TrimaranProfile(this);
        setRegisterFactory(new RegisterFactory(this));
        setTrimeranConnection(new TrimeranConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getInterKarTimeout(),getAckTimeout(),getCommandTimeout(),getFlushTimeout()));
        return getTrimeranConnection();
    }
    
    public Date getTime() throws IOException {
//        Date date = getDataFactory().getMeterStatusTable().getTimestamp();
//        return date;
    	return new Date(System.currentTimeMillis());
    }
    
    public void setTime() throws IOException {
        throw new UnsupportedException();
    }
    
    public String getProtocolVersion() {
//        return "$Revision$" ; 
        return "$Date$";
    }
    
    public String getFirmwareVersion() throws IOException, UnsupportedException {
//    	getDataFactory().getCurrentPeriodTable();
//        return "TARIF="+getDataFactory().getMeterStatusTable().getTarif()+
//               ", MODETA="+getDataFactory().getMeterStatusTable().getModeta()+ 
//               ", SOMMOD="+getDataFactory().getMeterStatusTable().getSommod()+
//               ", ERRFAT="+getDataFactory().getMeterStatusTable().getErrFat()+
//               ", ERRSES="+getDataFactory().getMeterStatusTable().getErrSes();
    	
    	return "Currently not implemented";
    }
    
    public String getSerialNumber() throws IOException {
        return null;
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
        StringBuffer strBuff = new StringBuffer();
        
        List registers = getRegisterFactory().getRegisters();
        Iterator it = registers.iterator();
        while(it.hasNext()) {
            Register r = (Register)it.next();
            strBuff.append(r+"\n");
        }
        
        return strBuff.toString();
    }    

    static public void main(String args[]) {
        // TODO code application logic here
        Trimaran trimeran = new Trimaran();
        Dialer dialer=null;
        
        String[] phones=new String[]{"033681497551","0033493746195","0033164498288","0033254601214","0033555877984","0033296550926","0033299834824","0033321853985","0033493746195","0033490868582","0033324564410","0033381506179","0033139811766","0033299825952","0033298531729","0033468082801","0033557519694","0033164498288"};
        String[] clientPasswords=new String[]{"08402","2494","4398","24398","57017","8870","37542","2801","2494","4488","24776","31158","65427","63668","13576","53840","791","4398"};
        String preDial="T0";
        int index=0;
        
        try {
////            // ,(byte)0x09,(byte)0x01,(byte)0x0f,(byte)0x00,(byte)0x00,(byte)0x88,(byte)0x11,(byte)0x6a,(byte)0xdc
////            byte[] data = new byte[]{(byte)0x09,(byte)0x01,(byte)0x0f,(byte)0x00,(byte)0x00,(byte)0x93,(byte)0xff,(byte)0xe0,(byte)0x60};//,(byte)0xcb};
//            byte[] data = new byte[]{(byte)0x09,(byte)0x02,(byte)0x0f,(byte)0x00,(byte)0x00,(byte)0x93,(byte)0xff};//,(byte)0xe0,(byte)0x53};//,(byte)0xcb};
////            byte[] data = new byte[]{0x09,0x01,0x0f,0x00,0,(byte)0x88,0x11,(byte)0x6a,(byte)0xdc};
//            int crc = CRCGenerator.calcCRCFull(data);
//            System.out.println(Integer.toHexString(crc));      
//            //data = new byte[]{0x04,0x62,0,0}; //,0x0,(byte)0x0};
//            crc = CRCGenerator.calcCRC(data); //,data.length-2);
//            System.out.println(Integer.toHexString(crc));                  
//            if (true) return;
            
// direct rs232 connection
            //dialer =DialerFactory.getDirectDialer().newDialer();
            dialer =DialerFactory.getDefault().newDialer();
            //dialer.init("COM1","ATZX5&B0B4C0U4&C1&D1","AT&S0"); //,"AT+MS=1,1,1200,1200","AT&C1&D1&S0");
            dialer.init("COM1","ATZ30X5&B0B4C0U4&C1&D1&S0","ATL0M0");
            //dialer.init("COM1");
            dialer.getSerialCommunicationChannel().setParams(1200,
                                                            SerialCommunicationChannel.DATABITS_8,
                                                            SerialCommunicationChannel.PARITY_NONE,
                                                            SerialCommunicationChannel.STOPBITS_1);            
            //dialer.getSerialCommunicationChannel().setDTR(false);
            
            
            
            dialer.connect(preDial+phones[index],90000); 
            
//dialer.getSerialCommunicationChannel().setDTR(false); // KV_DEBUG
// setup the properties (see AbstractProtocol for default properties)
// protocol specific properties can be added by implementing doValidateProperties(..)
            Properties properties = new Properties();
            properties.setProperty(MeterProtocol.PASSWORD,clientPasswords[index]);
            properties.setProperty("ProfileInterval", "600");
// transfer the properties to the protocol
            trimeran.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
//            dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
//                                                                     SerialCommunicationChannel.DATABITS_8,
//                                                                     SerialCommunicationChannel.PARITY_NONE,
//                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            trimeran.setHalfDuplexController(dialer.getHalfDuplexController());
            
            trimeran.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name"));
 
            
            
            trimeran.connect();  
            
            
            System.out.println("*********************** connect() ***********************");
            
            //System.out.println(trimeran.getDataFactory().getCurrentMonthInfoTable());
            //System.out.println(trimeran.getDataFactory().getPreviousMonthInfoTable());
            //System.out.println(trimeran.getDataFactory().getMeterStatusTable()); 
            //System.out.println(trimeran.getProfileData(null,null,false));
            
                   System.out.println(trimeran.getTime());
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                trimeran.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public TrimeranConnection getTrimeranConnection() {
        return trimeranConnection;
    }

    private void setTrimeranConnection(TrimeranConnection trimeranConnection) {
        this.trimeranConnection = trimeranConnection;
    }

    public SPDUFactory getSPDUFactory() {
        return sPDUFactory;
    }

    private void setSPDUFactory(SPDUFactory sPDUFactory) {
        this.sPDUFactory = sPDUFactory;
    }

    public DataFactory getDataFactory() {
        return dataFactory;
    }

    private void setDataFactory(DataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    public TrimaranProfile getTrimeranProfile() {
        return trimeranProfile;
    }

    private void setTrimeranProfile(TrimaranProfile trimeranProfile) {
        this.trimeranProfile = trimeranProfile;
    }

    public RegisterFactory getRegisterFactory() {
        return registerFactory;
    }

    private void setRegisterFactory(RegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

    public int getInterKarTimeout() {
        return interKarTimeout;
    }

    public void setInterKarTimeout(int interKarTimeout) {
        this.interKarTimeout = interKarTimeout;
    }

    public int getAckTimeout() {
        return ackTimeout;
    }

    public void setAckTimeout(int ackTimeout) {
        this.ackTimeout = ackTimeout;
    }

    public int getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(int commandTimeout) {
        this.commandTimeout = commandTimeout;
    }

    public int getFlushTimeout() {
        return flushTimeout;
    }

    public void setFlushTimeout(int flushTimeout) {
        this.flushTimeout = flushTimeout;
    }

}
