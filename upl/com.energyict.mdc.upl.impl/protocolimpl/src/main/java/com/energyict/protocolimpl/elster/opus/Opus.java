package com.energyict.protocolimpl.elster.opus;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.genericprotocolimpl.iskrap2lpc.ProtocolChannelMap;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;

public class Opus extends AbstractProtocol{
	/**
	 * ---------------------------------------------------------------------------------<p>
	 * Protocol description:<p>
	 * The protocol consists out of layers<p>
	 * 1) Parsers object is the deepest layer handling serialization 
	 * 		and deserialization of matrixes and primitive types to byte arrays
	 * <p>
	 * 2) OpusBuildPacket inherits the Parsers object, this object is 
	 * 		responsible for the correct building and unpacking of the frames
	 * 		it is both used for putting data in the right frame as feeding
	 * 		it with a received frame as byte array to put it back into 
	 * 		a OpusBuildPacket object.  Deserializing the byte array allows
	 * 		the user to get the data array from the object.<p>
	 * 		TODO: checksum testing on two levels: wrong number vs. wrong data
	 * <p>
	 * 3) OpusCommandFactory deals with the commands.  The previously described
	 * 		OpusBuildPacket is only called from here. The factory has 3 internal 
	 * 		layers: command selection, command building, command executing. 
	 * 		Basically this factory is an interface called each time the same way.  
	 * 		A number of globals should be set in the factory, some should be 
	 * 		passed with the command method.  Settings to be passed to the factory,
	 * 		that don't have the property to change frequently are set by setters. Settings
	 * 		that might change frequently are: type of command, number of attempts to 
	 * 		execute the command, timeout and a calendar object containing data on the 
	 * 		time frame to retrieve data from.
	 * 		The command executing is done using 4 state machines, handling a total
	 * 		of almost 70 commands.  Commands not yet implemented either because
	 * 		they must not be used (setting special function registers in the meter) or
	 * 		because they are related to other types of meters but were described in 
	 * 		the datasheet are all commands ranging from 121 to 999<p>
	 * 		TODO proper checksum and error handling, parsing specific data into arrays 
	 * 		or objects
	 * <p>
	 *  4) Opus: this is the meter protocol, in the connect method some meter props are
	 *  	read.
	 *  <p>
	 *  Initial version:<p>
	 *  ----------------<p>
	 *  @Author: Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
	 *  @Version: 1.1 <p>
	 *  First edit date: 10/07/2008 PST<p>
	 *  Last edit date: 17/07/2008  PST<p>
	 *  Comments:<p>
	 *  Released for testing: not yet, still under construction
	 *  .<p>
	 *  Revisions<p>
	 *  ----------------<p>
	 *  Author: <p>
	 *  Version:(SET IN CODE:protocolVersion)<p>
	 *  Last edit date: <p>
	 *  Comments:<p>
	 *  released for testing:
	 * ---------------------------------------------------------------------------------<p>
	 *  
	 */
	private static final float protocolVersion=(float) 1.0;
	
	private ProtocolChannelMap channelMap;
	
	private final String oldPassword;
	private final String newPassword;
	private int outstationID;	
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private int timeOut=5000;			// timeout time in ms
	private OpusCommandFactory ocf; 	// command factory
	private int attempts=5;				// number of attempts
	private ObisCodeMapper ocm;

	// attributes to retrieve from the data
	private int numChan=-1;				// number of channels
	private int interval=-1;			// temp value
	private String firmwareVersion;
	
	
	public Opus(){
		this.oldPassword="--------";
		this.newPassword="--------";
		this.outstationID=7; // for testing purposes only!!!!!!!!!!
	}
	
	public Opus(String oldPassword, String newPassword, int outstationID){
		this.oldPassword=oldPassword;
		this.newPassword=newPassword;
		this.outstationID=outstationID;
	}

	public void connect() throws IOException {
		// download final information
		ArrayList<String[]> s;							// ArrayList to catch data from factory
		s=ocf.command(121,attempts,timeOut, null);		// factory command
		this.numChan=Integer.valueOf(s.get(0)[1]);					// set number of channels in this object
		this.interval=24*3600/Integer.valueOf(s.get(0)[0]);	// set interval in this object
		this.firmwareVersion=s.get(0)[6];
		// set factory globals (IMORTANT)
		ocf.setNumChan(this.numChan);
		// end of download
	}

	public void disconnect() throws IOException {
	}

	public Object fetchCache(int arg0) throws SQLException, BusinessException {
		return null;
	}

	public Object getCache() {
		return null;
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return this.firmwareVersion;
	}
	public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (this.numChan == -1)
            throw new IOException("getNumberOfChannels(), ChannelMap property not given. Cannot determine the nr of channels...");
		return this.numChan;
	}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
//		return getProfileData(, includeEvents);
		return null;
	}

	public ProfileData getProfileData(Date fromTime, boolean includeEvents)
			throws IOException {
		return getProfileData(fromTime, Calendar.getInstance().getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date fromTime, Date toTime, boolean event)	throws IOException, UnsupportedException {
		ProfileData pd = new ProfileData();
		int[][] datamatrix;
		IntervalData id= new IntervalData();
		IntervalData previd= new IntervalData();
		MeterEvent mev;
		ArrayList<String[]> data= new ArrayList<String[]>();
		Calendar cal1 = Calendar.getInstance(); // removed getTimeZone()
		Calendar tempcal=Calendar.getInstance(); // to store data in for the interval data
		int command=10; // 10 is the value of today...69 of 60 days ago
		long millis=0,temp=0;
		boolean eventflag=false,powDownFlag=false, firstchan=true;

		// build object
		for(int i=0; i<this.numChan;i++ ){
			pd.addChannel(new ChannelInfo(i,i, "Elster Opus channel "+(i+1), Unit.get(BaseUnit.UNITLESS)));
		}
		
        // set timers
		// set to start of day
		cal1.setTime(fromTime);
		temp=cal1.getTimeInMillis()-getProfileInterval()*1500;// go back 1.5 interval
		cal1.setTimeInMillis(temp); //PD/PU midnight problem solution
		cal1.set(Calendar.HOUR_OF_DAY, 0);	// reset hour
		cal1.set(Calendar.MINUTE, 0);		// reset minutes
		cal1.set(Calendar.SECOND, 0);		// reset seconds
		cal1.set(Calendar.MILLISECOND, 0);
		
		// check properties
        if (getProfileInterval()<=0)
            throw new IOException("load profile interval must be > 0 sec. (is "+getProfileInterval()+")");
        ParseUtils.roundDown2nearestInterval(cal1,getProfileInterval());
        // start downloading
        // setTime(); // synchronize
        // id set
        while(cal1.getTime().before(toTime)) {
        	command=getCommandnr(cal1.getTime()); // download the specified day        	
        	// get the data
        	data=ocf.command(command, attempts, timeOut, null);
        	// put the data in a 2D matrix
        	if(data.size()>0){// data available test
        		// dump data in 2D matrix
        		datamatrix=processIntervalData(data);
        		// make the calendar object for that date
        		tempcal.setTime(cal1.getTime()); 		// set date
        		tempcal.set(Calendar.HOUR_OF_DAY, 0);	// reset hour
        		tempcal.set(Calendar.MINUTE, 0);		// reset minutes
        		tempcal.set(Calendar.SECOND, 0);		// reset seconds
        		tempcal.set(Calendar.MILLISECOND, 0);	// reset milliseconds
        		millis=tempcal.getTimeInMillis(); // start at 0:0:0h (interval 47 of previous day)
        	
        		for(int i=0; i<(3600*24/getProfileInterval()); i++){// 0->47
        		
        			// save previous data
        			previd=new IntervalData(tempcal.getTime());
        			previd.setIntervalValues(id.getIntervalValues());
        			previd.setEiStatus(id.getEiStatus());
        		
        			// 	generate stepclock
        			millis+=(getProfileInterval()*1000); 	// now time correction        		
        			tempcal.setTimeInMillis(millis);		// set to now
        			id=new IntervalData(tempcal.getTime());
        			firstchan=true;
        			for(int ii=0;ii<getNumberOfChannels();ii++){// 0->12
        				// check value
        				if(datamatrix[i][ii]>999990){
        					// value should be zero, because is false
        					id.addValue(0);
        					// event flagging
        					eventflag=true;    					
        					switch(datamatrix[i][ii]){ // special values
           						case 999996:
           							id.addEiStatus(IntervalStateBits.CORRUPTED);            					
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Data Overflow");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
           						case 999997:
           							id.addEiStatus(IntervalStateBits.CORRUPTED);
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Fuse Failure Delay");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
           						case 999998:
           							id.addEiStatus(IntervalStateBits.CORRUPTED);
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Lost Pulse");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
           						case 999999:
           							id.addEiStatus(IntervalStateBits.MISSING);
           							if(!powDownFlag){
           								long pdtemp=millis-getProfileInterval()*1000;
           								Calendar cal=Calendar.getInstance();
           								cal.setTimeInMillis(pdtemp); // set time one interval back
           								mev=new MeterEvent(cal.getTime(), MeterEvent.POWERDOWN);
           								if(tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           								powDownFlag=true;                					
           							}
           							break;
           						default:
           							// code 0 to 5 not implemented? No information
           							id.addEiStatus(IntervalStateBits.OTHER);
           							mev=new MeterEvent(tempcal.getTime(), MeterEvent.OTHER,"Data Overflow");
           							if(firstchan && tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
           							break;
        					}
        					firstchan=false;// don't tag other channel recordings in pd
        				}else{
        					if(powDownFlag && eventflag){
        						powDownFlag=false;
        						mev=new MeterEvent(tempcal.getTime(), MeterEvent.POWERUP);
        						if(tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){pd.addEvent(mev);}
        					}
        					eventflag=false;
        					// value is real value
        					id.addValue(datamatrix[i][ii]);
        				}
        			}
        			if(tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){        			
        				pd.addInterval(id);        			
        			}
        		}
        	}// end data available test
        	// next day
        	temp=cal1.getTimeInMillis()+(3600*24*1000);
        	cal1.setTimeInMillis(temp);// date change        	
        }
        
       // pd.getMeterEvents().addAll(mev);
        //pd.addEvent(new MeterEvent(now,MeterEvent.APPLICATION_ALERT_START, "SDK Sample"));

		return pd;
	}

	private int[][] processIntervalData(ArrayList<String[]> data) throws UnsupportedException, IOException {
		int tel=-1;
		int channelBody=0;
		int[][] matrix=new int[(int) (3600*24/getProfileInterval())][getNumberOfChannels()];
		String[] s;
		for(int index=1; index<data.size(); index++){
			s=data.get(index);
			if(s[0].charAt(0)=='S' && s[0].charAt(4)=='C'){
				// this is a channel header
				tel++;
				channelBody=0;
			}else{
				// channel body
				for(int i=0; i<8; i++){
					matrix[i+channelBody][tel]=Integer.parseInt(s[i]);
				}
				channelBody+=8;
			}			
		}
		return matrix;
	}

	private int getCommandnr(Date cal1) {
		int command=10;
		long now, then;
		Calendar calthen=Calendar.getInstance();
		calthen.setTime(cal1);
		Calendar calnow=Calendar.getInstance();
		calnow.set(Calendar.HOUR_OF_DAY, 0);
		calnow.set(Calendar.MINUTE, 0);
		calnow.set(Calendar.SECOND, 0);
		calnow.set(Calendar.MILLISECOND, 0);
		calthen.set(Calendar.HOUR_OF_DAY, 0);
		calthen.set(Calendar.MINUTE, 0);
		calthen.set(Calendar.SECOND, 0);
		calthen.set(Calendar.MILLISECOND, 0);
		now=calnow.getTimeInMillis(); 	// current date
		then=calthen.getTimeInMillis();	// requested date
		while(then<now){
			then+=3600*24*1000;
			command++;
		}
		return command;
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
		return this.interval;
	}

	public String getProtocolVersion() {
		return "$Date$";
	}

    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e 
    *******************************************************************************************/
//   public RegisterValue readRegister(ObisCode obisCode) throws IOException {
       //TODO
//	   if(ocm == null)
//		   ocm = new ObisCodeMapper(this);
//       return ocm.getRegisterValue(obisCode);
//   }
   
   //TODO
//   public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
//       return ObisCodeMapper.getRegisterInfo(obisCode);
//   }   

	public Date getTime() throws IOException  {
		Calendar cal=Calendar.getInstance();
		ArrayList<String[]> d=ocf.command(102, attempts, timeOut, null);
		String[] s=d.get(d.size()-1); // last index
		cal.set(Integer.parseInt(s[5])+2000,
				Integer.parseInt(s[4]),
				Integer.parseInt(s[3]),
				Integer.parseInt(s[0]),
				Integer.parseInt(s[1]),
				Integer.parseInt(s[2]));		
		return cal.getTime();
	}
	public void setTime() throws IOException {
		// time and date are read in the factory
		ocf.command(101, attempts, timeOut, null);
	}

	public void initializeDevice() throws IOException, UnsupportedException {
	}

	public void release() throws IOException {
	}

	public void setCache(Object arg0) {
	}

	public void setProperties(Properties properties) throws InvalidPropertyException,	MissingPropertyException {
		outstationID = Integer.parseInt(properties.getProperty("NodeAddress", "000"));
		channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", null));
	}

	public void updateCache(int arg0, Object arg1) throws SQLException,
			BusinessException {
	}

	public List getOptionalKeys() {
		ArrayList list = new ArrayList();
		list.add("ChannelMap");
		return list;
	}

	public List getRequiredKeys() {
		ArrayList list = new ArrayList();
		return list;
	}

	public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2,
			Logger arg3) throws IOException {
		// set streams
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        // build command factory
		this.ocf=new OpusCommandFactory(this.outstationID,this.oldPassword,this.newPassword,this.inputStream,this.outputStream);
	}

	protected void doConnect() throws IOException {
	}

	protected void doDisConnect() throws IOException {
	}

	protected List doGetOptionalKeys() {
		ArrayList list = new ArrayList();
		return list;
	}

	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		return null;
	}

	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		
	}
	
}
