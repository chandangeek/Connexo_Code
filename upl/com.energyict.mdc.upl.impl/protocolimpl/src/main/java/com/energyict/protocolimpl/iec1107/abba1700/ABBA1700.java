package com.energyict.protocolimpl.iec1107.abba1700;

import com.energyict.cbo.NestedIOException;
import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.meteridentification.DiscoverInfo;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.*;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
/**
 *
 * @author  Koen
 */
public class ABBA1700 implements MeterProtocol,ProtocolLink,HHUEnabler,SerialNumber,MeterExceptionInfo,RegisterProtocol,DemandResetProtocol  { // KV 19012004

	private static final int			BREAK_DELAY				= 500;
	private static final int			BREAK_BAUDRATE			= 9600;

	private static final String[] ABB1700_REGISTERCONFIG = {
		"CummMainImport", "CummMainExport", "CummMainQ1", "CummMainQ2",
		"CummMainQ3", "CummMainQ4", "CummMainVA", "CummMainCustDef1",
		"CummMainCustDef2", "CummMainCustDef3", "ExternalInput1",
		"ExternalInput2", "ExternalInput3", "ExternalInput4"
	};

    // KV 19012004 implementation of MeterExceptionInfo
    private static final Map<String, String> EXCEPTIONINFOMAP = new HashMap<String, String>();
    static {
           EXCEPTIONINFOMAP.put("ERR1","Invalid Command/Function type e.g. other than W1, R1 etc");
           EXCEPTIONINFOMAP.put("ERR2","Invalid Data Identity Number e.g. Data id does not exist in the meter");
           EXCEPTIONINFOMAP.put("ERR3","Invalid Packet Number");
           EXCEPTIONINFOMAP.put("ERR5","Data Identity is locked - password timeout");
           EXCEPTIONINFOMAP.put("ERR6","General Comms error");
    }

	private boolean								soft7E1					= false;
	private boolean								breakBeforeConnect		= false;
	private String								nodeId					= null;
	private String								strID					= null;
	private String								strPassword				= null;
	private String								serialNumber			= null;
	private TimeZone							timeZone				= null;
	private Logger								logger					= null;
	private FlagIEC1107Connection				connection				= null;
	private ABBA1700RegisterFactory				abba1700RegisterFactory	= null;
	private ABBA1700Profile						abba1700Profile			= null;
	private ABBA1700MeterType					abba1700MeterType		= null;
	private SerialCommunicationChannel			commChannel				= null;

    private int iTimeout;
    private int iProtocolRetries;
    private int iRoundtripCorrection;
    private int iSecurityLevel;
    private int iEchoCancelling;
    private int iIEC1107Compatible;
    private int extendedLogging;
    private int forcedDelay;

    /** Creates a new instance of ABBA1700 */
    public ABBA1700() {
    }

    public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
        return (Quantity)getABBA1700RegisterFactory().getRegister(name);
    }

    public Quantity getMeterReading(int channelID) throws UnsupportedException, IOException {
        return (Quantity)getABBA1700RegisterFactory().getRegister(ABB1700_REGISTERCONFIG[getABBA1700Profile().getChannelIndex(channelID)]);
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return abba1700Profile.getProfileData();
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        return abba1700Profile.getProfileData(lastReading,calendar.getTime());
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,UnsupportedException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }


	public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
		String regName = name;
		int billingPoint = -1;
		String current = getABBA1700RegisterFactory().getRegister(regName, billingPoint).toString();
		if (name.indexOf("_") != -1) {
			String[] strings = name.split("_");
			regName = strings[0];
			billingPoint = Integer.parseInt(strings[1]);
			String hist = ((HistoricalValues) getABBA1700RegisterFactory().getRegister("HistoricalValues", billingPoint)).getHistoricalValueSetInfo().toString();
			return current + ", " + hist;
		} else {
			return current;
		}
	}

	public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
		getABBA1700RegisterFactory().setRegister(name, value);
	}

	public void resetDemand() throws IOException {
		getABBA1700RegisterFactory().invokeRegister("BillingReset");
	}

	public Date getTime() throws IOException {
		return (Date) getABBA1700RegisterFactory().getRegister("TimeDate");
	}

	public void setTime() throws IOException {
		Calendar calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
		getFlagIEC1107Connection().authenticate();
		getABBA1700RegisterFactory().setRegister("TimeDate", calendar.getTime());
	}

	public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		validateProperties(properties);
	}

    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
					throw new MissingPropertyException(key + " key missing");
				}
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD,"");
            iSecurityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","2").trim());
            if (iSecurityLevel != 0) {
                if ("".compareTo(strPassword)==0) {
                    throw new InvalidPropertyException("Password field is empty! correct first!");
                }
                if (strPassword.length()!=8) {
                    throw new InvalidPropertyException("Password must have a length of 8 characters!, correct first!");
                }
            }
            iTimeout=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetries=Integer.parseInt(properties.getProperty("Retries","5").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iSecurityLevel=Integer.parseInt(properties.getProperty("SecurityLevel","2").trim());
            nodeId=properties.getProperty(MeterProtocol.NODEID,"");
            iEchoCancelling=Integer.parseInt(properties.getProperty("EchoCancelling","0").trim());
            iIEC1107Compatible=Integer.parseInt(properties.getProperty("IEC1107Compatible","0").trim());
            extendedLogging=Integer.parseInt(properties.getProperty("ExtendedLogging","0").trim());
            // 15122003 get the serialNumber
            serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);

            // 0 = 16 TOU registers type (most in the UK)
            // 1 = 32 TOU registers type (Portugal, etc...)
            // -1 = use identification string from signon later...
            abba1700MeterType = new ABBA1700MeterType(Integer.parseInt(properties.getProperty("MeterType","-1").trim()));
            forcedDelay = Integer.parseInt(properties.getProperty("ForcedDelay","300").trim());
            this.soft7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
            this.breakBeforeConnect = !properties.getProperty("BreakBeforeConnect", "0").equalsIgnoreCase("0");

        }
        catch (NumberFormatException e) {
            throw new InvalidPropertyException("ABBA1700, validateProperties, NumberFormatException, "+e.getMessage());
        }
    }

    public List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    public List getOptionalKeys() {
        List<String> result = new ArrayList<String>();
        result.add("Timeout");
        result.add("Retries");
        result.add("SecurityLevel");
        result.add("EchoCancelling");
        result.add("IEC1107Compatible");
        result.add("ExtendedLogging");
        result.add("MeterType");
        result.add("ForcedDelay");
        result.add("Software7E1");
        result.add("BreakBeforeConnect");
        return result;
    }

    public String getProtocolVersion() {
        return "$Date$";
    }

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		if ((strID != null) && (strID.length() > 5)) {
			return strID.substring(5, strID.length());
		} else {
			return "Unknown";
		}
	}

	public void initializeDevice() throws IOException, UnsupportedException {
		throw new UnsupportedException();
	}

	public void init(InputStream in, OutputStream out, TimeZone tz, Logger log) {
		this.timeZone = tz;
		this.logger = log;

		try {
			connection = new FlagIEC1107Connection(in, out, iTimeout, iProtocolRetries, forcedDelay, iEchoCancelling, iIEC1107Compatible, new CAI700(), soft7E1);
		} catch (ConnectionException e) {
			log.severe("ABBA1500: init(...), " + e.getMessage());
		}
	}

    /**
     * @throws IOException  */

    public void connect() throws IOException {
        connect(0);
    }

    public void connect(int baudrate) throws IOException {
        MeterType meterType;

		if (isBreakBeforeConnect()) {
			switchBaudRate(BREAK_BAUDRATE);
			getFlagIEC1107Connection().sendBreak();
			getFlagIEC1107Connection().delayAndFlush(BREAK_DELAY);
		}

		try {
			meterType = getFlagIEC1107Connection().connectMAC(strID, strPassword, iSecurityLevel, nodeId, baudrate);
			if (!abba1700MeterType.isAssigned()) {
				abba1700MeterType.updateWith(meterType);
			}
			abba1700RegisterFactory = new ABBA1700RegisterFactory((ProtocolLink) this, (MeterExceptionInfo) this, abba1700MeterType); // KV 19012004
			abba1700Profile = new ABBA1700Profile(this, getABBA1700RegisterFactory());
		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException(e.getMessage());
		} catch (IOException e) {
			disconnect();
			throw e;
		}

		try {
			validateSerialNumber(); // KV 15122003
		} catch (FlagIEC1107ConnectionException e) {
			disconnect();
			throw new IOException(e.getMessage());
		}

		if (extendedLogging >= 1) {
			getRegistersInfo();
		}
    }

    private void getRegistersInfo() throws IOException {
        StringBuffer strBuff = new StringBuffer();
        String code;
        logger.info("************************* Extended Logging *************************");

        for (int billingPoint=-1;billingPoint<12;billingPoint++) {

            TariffSources ts;
            if (billingPoint == -1) {
				ts = (TariffSources)getABBA1700RegisterFactory().getRegister("TariffSources");
			} else {
                HistoricalDisplayScalings hds = (HistoricalDisplayScalings)abba1700RegisterFactory.getRegister("HistoricalDisplayScalings",billingPoint);
                HistoricalDisplayScalingSet hdss = hds.getHistoricalDisplayScalingSet();
                ts = hdss.getTariffSources();
            }

            strBuff.append("Cumulative registers (total & tariff):\n");
            List list = EnergyTypeCode.getEnergyTypeCodes();
            Iterator it = list.iterator();
            while(it.hasNext()) {
               EnergyTypeCode etc = (EnergyTypeCode)it.next();
               code = "1.1."+etc.getObisC()+".8.0."+(billingPoint==-1?255:billingPoint);
               strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
               for (int i=0;i<ts.getRegSource().length;i++) {
                   if (ts.getRegSource()[i] == etc.getRegSource()) {
                       code = "1.1."+etc.getObisC()+".8."+(i+1)+"."+(billingPoint==-1?255:billingPoint);
                       strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+"\n");
                   }
               }
            }

            strBuff.append("Maximum demand registers:\n");
            for (int i=0;i<ABBA1700RegisterFactory.MAX_MD_REGS;i+=3) {
               try {
                   //MaximumDemand md = (MaximumDemand)abba1700RegisterFactory.getRegister("MaximumDemand"+(i+obisCode.getB()-1),billingPoint);
                   List mds = new ArrayList();
                   for (int j=0;j<3;j++) {
					mds.add((MaximumDemand)getABBA1700RegisterFactory().getRegister("MaximumDemand"+(i+j),billingPoint));
				}
                   // sort in accending datetime
                   MaximumDemand.sortOnQuantity(mds);
                   // energytype code match with the maximumdemand register with the most
                   // recent datetime stamp.

                   for (int bField=1;bField<=3;bField++) {
                       MaximumDemand md = (MaximumDemand)mds.get(3 - bField); // B=1 => get(2), B=2 => get(1), B=3 => get(0)

    //                   if (unit != null) // in case of customer defined registers unit is defined earlier
    //                       md.setQuantity(new Quantity(md.getQuantity().getAmount(),unit.getFlowUnit()));
    //

                       // We suppose that the rate 1 mostly covers the continue schedule (=total). So, we map rate 1 also to 0 to
                       // be backwards compatible with previous implementation
                       if (i==0) {
                           code = "1."+bField+"."+EnergyTypeCode.getObisCFromRegSource(md.getRegSource(),false)+".6.0."+(billingPoint==-1?255:billingPoint);
                           // KV_DEBUG
                           strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+", "+md+"\n");

                           code = "1."+bField+"."+EnergyTypeCode.getObisCFromRegSource(md.getRegSource(),false)+".6.1."+(billingPoint==-1?255:billingPoint);
                           // KV_DEBUG
                           strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+", "+md+"\n");
                       }
                       else {
                           code = "1."+bField+"."+EnergyTypeCode.getObisCFromRegSource(md.getRegSource(),false)+".6."+((i/3)+1)+"."+(billingPoint==-1?255:billingPoint);
                           // KV_DEBUG
                           strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+", "+md+"\n");
                       }


                   }
               }
               catch(NoSuchRegisterException e) {
                   //strBuff.append("KV_DEBUG> Unknown Code...");
                   // absorb...
               }

            } // for (int i=0;i<ABBA1700RegisterFactory.MAX_MD_REGS;i+=3)

//            for (int i=0;i<ABBA1700RegisterFactory.MAX_MD_REGS;i++) {
//               try {
//
//                  MaximumDemand md = (MaximumDemand)getABBA1700RegisterFactory().getRegister("MaximumDemand"+i,billingPoint);
//                  code = "1."+((i%3)+1)+"."+EnergyTypeCode.getObisCFromRegSource(md.getRegSource(),false)+".6.0."+(billingPoint==-1?255:billingPoint);
//                  // KV_DEBUG
//                  strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+", "+md+"\n");
//               }
//               catch(NoSuchRegisterException e) {
//                   //strBuff.append("KV_DEBUG> Unknown Code...");
//                   // absorb...
//               }
//            }

            strBuff.append("cumulative maximum demand registers:\n");
            for (int i=0;i<ABBA1700RegisterFactory.MAX_CMD_REGS;i++) {
               try {
                   CumulativeMaximumDemand cmd = (CumulativeMaximumDemand)getABBA1700RegisterFactory().getRegister("CumulativeMaximumDemand"+i,billingPoint);
                   if (i==0) {
                       code = "1.1."+EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(),false)+".2.0."+(billingPoint==-1?255:billingPoint);
                       strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+", "+cmd+"\n");

                       code = "1.1."+EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(),false)+".2.1."+(billingPoint==-1?255:billingPoint);
                       strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+", "+cmd+"\n");
                   }
                   else {
                       code = "1.1."+EnergyTypeCode.getObisCFromRegSource(cmd.getRegSource(),false)+".2."+(i+1)+"."+(billingPoint==-1?255:billingPoint);
                       strBuff.append(code+", "+ObisCodeMapper.getRegisterInfo(ObisCode.fromString(code))+", "+cmd+"\n");
                   }
               }
               catch(NoSuchRegisterException e) {
                   //strBuff.append("KV_DEBUG> Unknown Code...");
                   // absorb...
               }
            }
        } // for (int billingPoint=-1;billingPoint<12;billingPoint++)

        logger.info(strBuff.toString());


        abba1700RegisterFactory.getProtocolLink().getFlagIEC1107Connection().authenticate();
    }

    // KV 15122003
    private void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) {
			return;
		}
        String sn = (String)getABBA1700RegisterFactory().getRegister("SerialNumber");
        if (sn.compareTo(serialNumber) == 0) {
			return;
		} else if (sn.replace('-', ' ').trim().compareTo(serialNumber.replace('-', ' ').trim()) == 0) {
			return;
		}
        throw new IOException("SerialNumber mismatch! meter sn="+sn.replace('-', ' ').trim()+", configured sn="+serialNumber.replace('-', ' ').trim());
    }

    public void disconnect() throws NestedIOException {
        try {
            getFlagIEC1107Connection().disconnectMAC();
        }
        catch(FlagIEC1107ConnectionException e) {
            logger.severe("disconnect() error, "+e.getMessage());
        }
    }


    public int getNumberOfChannels() throws UnsupportedException, IOException {
        long channelMask = getABBA1700Profile().getChannelMask();
        int nrOfChannels=0;
        for (long i=1; i!=0x10000 ; i<<=1) {
			if ((channelMask & i) != 0) {
				nrOfChannels++;
			}
		}
        return nrOfChannels;
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return ((Integer)getABBA1700RegisterFactory().getRegister("IntegrationPeriod")).intValue()*60;
    }

    private ABBA1700RegisterFactory getABBA1700RegisterFactory() {
        return abba1700RegisterFactory;
    }
    private ABBA1700Profile getABBA1700Profile() {
        return abba1700Profile;
    }


    // implementing ProtocolLink
    public FlagIEC1107Connection getFlagIEC1107Connection() {
        return connection;
    }
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public boolean isIEC1107Compatible() {
        return (iIEC1107Compatible == 1);
    }

    public String getPassword() {
        return strPassword;
    }

    public byte[] getDataReadout() {
        return null;
    }

    public Object getCache() {
        return null;
    }
    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }
    public void setCache(Object cacheObject) {
    }

    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
    }

    public void release() throws IOException {
    }

    public ProtocolChannelMap getProtocolChannelMap() {
        return null;
    }
    public ChannelMap getChannelMap() {
        return null;
    }

    // ********************************************************************************************************
    // implementation of the HHUEnabler interface
    public void enableHHUSignOn(SerialCommunicationChannel serialCommunicationChannel) throws ConnectionException {
        enableHHUSignOn(serialCommunicationChannel,false);
    }
    public void enableHHUSignOn(SerialCommunicationChannel serialCommunicationChannel,boolean datareadout) throws ConnectionException {
        this.commChannel = serialCommunicationChannel;

    	HHUSignOn hhuSignOn =
        (HHUSignOn)new IEC1107HHUConnection(serialCommunicationChannel,iTimeout,iProtocolRetries,300,iEchoCancelling);
        hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
        hhuSignOn.enableDataReadout(datareadout);
        getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
    }
    public byte[] getHHUDataReadout() {
        return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
    }

	public String getSerialNumber(DiscoverInfo discoverInfo) throws IOException {
		SerialCommunicationChannel serialCommunicationChannel = discoverInfo.getCommChannel();
		String nodeId = discoverInfo.getNodeId();
		int baudrate = discoverInfo.getBaudrate();
		Properties properties = new Properties();
		properties.setProperty("SecurityLevel", "0");
		properties.setProperty(MeterProtocol.NODEID, nodeId == null ? "" : nodeId);
		properties.setProperty("IEC1107Compatible", "1");
		setProperties(properties);
		init(serialCommunicationChannel.getInputStream(), serialCommunicationChannel.getOutputStream(), null, null);
		enableHHUSignOn(serialCommunicationChannel);
		connect(baudrate);
		String sn = getRegister("SerialNumber");
		disconnect();
		return sn;
	}

	public String getExceptionInfo(String id) {
		String exceptionInfo = (String) EXCEPTIONINFOMAP.get(id);
		if (exceptionInfo != null) {
			return id + ", " + exceptionInfo;
		} else {
			return "No meter specific exception info for " + id;
		}
	}

    public Logger getLogger() {
        return logger;
    }

	public RegisterValue readRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
		return getABBA1700RegisterFactory().readRegister(obisCode);
	}

	public RegisterInfo translateRegister(com.energyict.obis.ObisCode obisCode) throws IOException {
		return ObisCodeMapper.getRegisterInfo(obisCode);
	}

	public int getNrOfRetries() {
		return iProtocolRetries;
	}

	public boolean isRequestHeader() {
		return false;
	}

	/**
	 * @return
	 */
	public boolean isBreakBeforeConnect() {
		return breakBeforeConnect;
	}

    /**
     * @param baudRate
     * @throws IOException
     */
    private void switchBaudRate(int baudRate) throws IOException {
    	if (isBreakBeforeConnect() && (commChannel != null)) {
    		commChannel.setParams(baudRate, SerialCommunicationChannel.DATABITS_7, SerialCommunicationChannel.PARITY_EVEN, SerialCommunicationChannel.STOPBITS_1);
    	}
	}

}
