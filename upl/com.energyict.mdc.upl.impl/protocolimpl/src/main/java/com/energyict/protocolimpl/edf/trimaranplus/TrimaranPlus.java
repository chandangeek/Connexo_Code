/*
 * TrimaranPlus.java
 *
 * Created on 25 januari 2007, 13:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.DLMSPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.APSEParameters;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.Connection62056;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.ProtocolLink;
import com.energyict.protocolimpl.edf.trimaranplus.core.TrimaranObjectFactory;
import com.energyict.protocolimpl.edf.trimaranplus.core.VDEType;

/**
 *@beginchanges
	KV|23052007|Bugfix to avoid looping in CourbeCharge object when retrieving more then the bufferspace in the meter
	GNA|19052008|Added a delayAfterConnect parameter to avoid gms failures
	GNA|22012009|Added a customizable safetyTimeout for the transportlayer.
				Default this was 300s so every failing communication took 5min.
 *@endchanges
 */

public class TrimaranPlus extends AbstractProtocol implements ProtocolLink {  
    
    
    private int t1Timeout;
    private int safetyTimeout;
    private Connection62056 connection62056;
    private APSEPDUFactory aPSEFactory;
    private DLMSPDUFactory dLMSPDUFactory;
    private int sourceTransportAddress;
    private int destinationTransportAddress;
    private int delayAfterConnect;
    private APSEParameters aPSEParameters;
    private VDEType vDEType = new VDEType(); // default set as
    private TrimaranObjectFactory trimaranObjectFactory;
    TrimaranPlusProfile trimaranPlusProfile=null;
    private RegisterFactory registerFactory=null;
    
    /** Creates a new instance of TrimaranPlus */
    public TrimaranPlus() {
    }
    
    protected void doConnect() throws IOException {
        getAPSEFactory().getAuthenticationReqAPSE();
        getDLMSPDUFactory().getInitiateRequest();
        getLogger().info(getDLMSPDUFactory().getStatusResponse().toString());
        if (getDLMSPDUFactory().getStatusResponse().getVDEType() == 770)
            getVDEType().setVDEType(VDEType.getVDEBASE());
        if (getDLMSPDUFactory().getStatusResponse().getVDEType() == 771)
            getVDEType().setVDEType(VDEType.getVDEEJP());
        if (getDLMSPDUFactory().getStatusResponse().getVDEType() == 772)
            getVDEType().setVDEType(VDEType.getVDEMODULABLE());
    }
    protected void doDisConnect() throws IOException {
        
    }
    
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return trimaranPlusProfile.getProfileData(lastReading);
    }
    
    protected void validateSerialNumber() throws IOException {
        
        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = getDLMSPDUFactory().getStatusResponse().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
        
    }    
    
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setT1Timeout(Integer.parseInt(properties.getProperty("T1Timeout","5000").trim())); // T1 (datalink layer)
        setSourceTransportAddress(Integer.parseInt(properties.getProperty("STSAP","0").trim()));
        setDestinationTransportAddress(Integer.parseInt(properties.getProperty("DTSAP","2").trim()));
                
        //setInfoTypeHalfDuplex(Integer.parseInt(properties.getProperty("HalfDuplex","50").trim()));
        
        setAPSEParameters(new APSEParameters());
        getAPSEParameters().setClientType(Integer.parseInt(properties.getProperty("ClientType","40967").trim())); // 0xA007
        getAPSEParameters().setCallingPhysicalAddress(properties.getProperty("CallingPhysicalAddress","30")); // APSE calling physical address, enter as string of even length, containing HEX karakters, default 0x30
        getAPSEParameters().setProposedAppCtxName(Integer.parseInt(properties.getProperty("ProposedAppCtxName","0").trim())); // APSE proposed App context name, default 0
        setInfoTypePassword(properties.getProperty(MeterProtocol.PASSWORD,"0000000000000000"));
        
        this.safetyTimeout = Integer.parseInt(properties.getProperty("SafetyTimeOut", "300000")); // Safety timeout in the transport layer
        
        if(Integer.parseInt(properties.getProperty("DelayAfterConnect", "0")) == 1)
        	delayAfterConnect = 6000;
        else 
        	delayAfterConnect = Integer.parseInt(properties.getProperty("DelayAfterConnect", "0").trim());
        
        try {
            getAPSEParameters().setKey(ProtocolUtils.convert2ascii(getInfoTypePassword().getBytes()));
        }
        catch(IOException e) {
            throw new InvalidPropertyException(e.toString());
        }
        
        //setVDEType(new VDEType(Integer.parseInt(properties.getProperty("VDEType","0").trim())));
        
    }
    protected List doGetOptionalKeys() {
        List list = new ArrayList(7);
        list.add("T1Timeout");
        list.add("STSAP");
        list.add("DTSAP");
        list.add("ClientType");
        list.add("CallingPhysicalAddress");
        list.add("ProposedAppCtxName");
        list.add("DelayAfterConnect");
        list.add("SafetyTimeOut");
        //list.add("VDEType");
        return list;
    }
    
    protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
        
        setAPSEFactory(new APSEPDUFactory(this,getAPSEParameters()));
        setDLMSPDUFactory(new DLMSPDUFactory(this));
        setTrimaranObjectFactory(new TrimaranObjectFactory(this));
//        setConnection62056(new Connection62056(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getT1Timeout(), getSourceTransportAddress(), getDestinationTransportAddress(), getDelayAfterConnect()));
        setConnection62056(new Connection62056(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getT1Timeout(), getSourceTransportAddress(), getDestinationTransportAddress(), getDelayAfterConnect(), this.safetyTimeout));
        getConnection62056().initProtocolLayers();
        trimaranPlusProfile = new TrimaranPlusProfile(this);
        
        return getTrimaranPlusConnection();
    }
    
    public Date getTime() throws IOException {
        // KV_TO_DO datecourante
        return new Date();
    }
    public String getProtocolVersion() {
        return "$Revision: 1.10 $"; 
    }
    
    public void setTime() throws IOException {
        throw new UnsupportedException();
    }
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].toString();
    }

    public int getT1Timeout() {
        return t1Timeout;
    }

    public void setT1Timeout(int t1Timeout) {
        this.t1Timeout = t1Timeout;
    }

    public Connection62056 getTrimaranPlusConnection() {
        return getConnection62056();
    }

    public void setTrimaranPlusConnection(Connection62056 connection62056) {
        this.setConnection62056(connection62056);
    }


    public APSEPDUFactory getAPSEFactory() {
        return aPSEFactory;
    }

    public void setAPSEFactory(APSEPDUFactory aPSEFactory) {
        this.aPSEFactory = aPSEFactory;
    }

    public Connection62056 getConnection62056() {
        return connection62056;
    }

    public void setConnection62056(Connection62056 connection62056) {
        this.connection62056 = connection62056;
    }

    public int getSourceTransportAddress() {
        return sourceTransportAddress;
    }

    public void setSourceTransportAddress(int sourceTransportAddress) {
        this.sourceTransportAddress = sourceTransportAddress;
    }

    public int getDestinationTransportAddress() {
        return destinationTransportAddress;
    }

    public void setDestinationTransportAddress(int destinationTransportAddress) {
        this.destinationTransportAddress = destinationTransportAddress;
    }

    public APSEParameters getAPSEParameters() {
        return aPSEParameters;
    }

    public void setAPSEParameters(APSEParameters aPSEParameters) {
        this.aPSEParameters = aPSEParameters;
    }

    public DLMSPDUFactory getDLMSPDUFactory() {
        return dLMSPDUFactory;
    }

    public void setDLMSPDUFactory(DLMSPDUFactory dLMSPDUFactory) {
        this.dLMSPDUFactory = dLMSPDUFactory;
    }
    
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        
        strBuff.append(getTrimaranObjectFactory().readParametresPplus1());
        strBuff.append(getTrimaranObjectFactory().readParametresP());
        strBuff.append(getTrimaranObjectFactory().readParametresPmoins1());
        strBuff.append(getTrimaranObjectFactory().readParametresPmoins2());
        strBuff.append(getTrimaranObjectFactory().readAccessPartiel());
        strBuff.append(getTrimaranObjectFactory().readAsservissementClient());
        strBuff.append(getTrimaranObjectFactory().readEnergieIndex());
        strBuff.append(getTrimaranObjectFactory().readPmaxValues());
        strBuff.append(getTrimaranObjectFactory().readDureeDepassementValues());
        strBuff.append(getTrimaranObjectFactory().readDepassementQuadratiqueValues());
        strBuff.append(getTrimaranObjectFactory().readTempsFonctionnementValues());
        
        strBuff.append(getRegisterFactory().getRegisterInfo());
        
        return strBuff.toString();
    }        
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 1;
    }   
    
    public int getProfileInterval() throws UnsupportedException, IOException { 
        return 5*60*getTrimaranObjectFactory().readParametresP().getTCourbeCharge();
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
    
    static public void main(String args[]) {
        // TODO code application logic here
        TrimaranPlus trimaranPlus = new TrimaranPlus();
        Dialer dialer=null;
        
        String[] phones=new String[]{"0033231830806","0033297667306","0033241701921","0033381506179"};
        String[] clientPasswords=new String[]{"62696C636F62636C","525A5B5A5E5C535C","565c56565a575758","d6dcdededad7d7d8"};
        String preDial="T0";
        int index=3;
        
        try {
// direct rs232 connection
            //dialer =DialerFactory.getDirectDialer().newDialer();
            dialer =DialerFactory.getDefault().newDialer();
            //dialer.init("COM1","ATZX5&B0B4C0U4&C1&D1","AT&S0"); //,"AT+MS=1,1,1200,1200","AT&C1&D1&S0");
            dialer.init("COM1","AT&BB4&D2&D1&S1M0","AT&FV1E0QX4Z30S0=0S42=7");
            //dialer.init("COM1");
            dialer.getSerialCommunicationChannel().setParams(2400,
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
            //properties.setProperty("SerialNumber","0304072680850001");
            //properties.setProperty("DTSAP","3"); // VDE SUPERVISOR
// transfer the properties to the protocol
            trimaranPlus.setProperties(properties);    
            
// depending on the dialer, set the initial (pre-connect) communication parameters            
//            dialer.getSerialCommunicationChannel().setParamsAndFlush(1200,
//                                                                     SerialCommunicationChannel.DATABITS_8,
//                                                                     SerialCommunicationChannel.PARITY_NONE,
//                                                                     SerialCommunicationChannel.STOPBITS_1);
// initialize the protocol
            trimaranPlus.setHalfDuplexController(dialer.getHalfDuplexController());
            
            trimaranPlus.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"),Logger.getLogger("name")); 
            
            trimaranPlus.connect();  
            System.out.println("*********************** connect() ***********************");
            
            //System.out.println(trimaranPlus.getTrimaranObjectFactory().readDateCourante());
            
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readParametresPplus1());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readParametresP());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readParametresPmoins1());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readParametresPmoins2());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readAccessPartiel());
//            trimaranPlus.getTrimaranObjectFactory().writeAccessPartiel();
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readAccessPartiel());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readAsservissementClient());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readEnergieIndex());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readPmaxValues());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readDureeDepassementValues());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readDepassementQuadratiqueValues());
//            System.out.println(trimaranPlus.getTrimaranObjectFactory().readTempsFonctionnementValues());
            
            Calendar cal = ProtocolUtils.getCalendar(TimeZone.getTimeZone("ECT"));
            
            cal.add(Calendar.MONTH, -8);
            //cal.set(Calendar.HOUR_OF_DAY, 0);
            //cal.set(Calendar.MINUTE, 0);
            System.out.println("Load profile from "+cal.getTime());     
            System.out.println(trimaranPlus.getTrimaranObjectFactory().getCourbeCharge(cal.getTime()));
            
            
            //System.out.println(trimaranPlus.getTime());
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                trimaranPlus.disconnect();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }    

    public VDEType getVDEType() {
        return vDEType;
    }

    public void setVDEType(VDEType vDEType) {
        this.vDEType = vDEType;
    }

    public TrimaranObjectFactory getTrimaranObjectFactory() {
        return trimaranObjectFactory;
    }

    public void setTrimaranObjectFactory(TrimaranObjectFactory trimaranObjectFactory) {
        this.trimaranObjectFactory = trimaranObjectFactory;
    }

    public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory==null) 
           setRegisterFactory(new RegisterFactory(this));
        return registerFactory;
    }

    public void setRegisterFactory(RegisterFactory registerFactory) {
        this.registerFactory = registerFactory;
    }

	protected int getDelayAfterConnect() {
		return delayAfterConnect;
	}
}
