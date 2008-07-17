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
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.base.ProtocolConnection;

public class Opus implements MeterProtocol{
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
	 *  @Version: 1.0 <p>
	 *  First edit date: 10/07/2008 PST<p>
	 *  Last edit date: 16/07/2008  PST<p>
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
	
	private final String oldPassword;
	private final String newPassword;
	private final int outstationID;	
	
	private InputStream inputStream;
	private OutputStream outputStream;
	private int timeOut=5000;			// timeout time in ms
	private OpusCommandFactory ocf; 	// command factory
	private int attempts=5;				// number of attempts

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
		// set factory globals
		ocf.setNumChan(this.numChan);
		// end of download
		
		// testing routines, to be removed
		System.out.println("number of channels:     "+this.numChan);
		System.out.println("interval:               "+this.interval);
		System.out.println("firmwareVersion:        "+this.firmwareVersion);
		DateFormat d=DateFormat.getDateInstance();
		Date date=getTime();
		System.out.println("get time:               "+d.format(date)+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds());
		setTime();
		System.out.println("get time after setting: "+d.format(date)+" "+date.getHours()+":"+date.getMinutes()+":"+date.getSeconds());
//		Calendar cal=Calendar.getInstance();
//		cal.set(Calendar.HOUR_OF_DAY, 12);
//		getProfileData(cal.getTime(),Calendar.getInstance().getTime(),true);
		// s=ocf.command(1 ,attempts,timeOut, null);
		// end of testing routines
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
	public Quantity getMeterReading(int arg0) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Quantity getMeterReading(String arg0) throws UnsupportedException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
        if (this.numChan == -1)
            throw new IOException("getNumberOfChannels(), ChannelMap property not given. Cannot determine the nr of channels...");
		return this.numChan;
	}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
//		TODO return getProfileData(, includeEvents);
		return null;
	}

	public ProfileData getProfileData(Date fromTime, boolean includeEvents)
			throws IOException {
		return getProfileData(fromTime, Calendar.getInstance().getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date fromTime, Date toTime, boolean arg2)	throws IOException, UnsupportedException {
		ProfileData pd = new ProfileData();
		int[][] datamatrix;
		IntervalData id= new IntervalData();
		ArrayList<String[]> data= new ArrayList<String[]>();
		Calendar cal1 = Calendar.getInstance(); // removed getTimeZone()
		Calendar tempcal=Calendar.getInstance(); // to store data in for the interval data
		int command=10; // 10 is the value of today...69 of 60 days ago
		long millis=0,temp=0;
		// build object
		for(int i=0; i<this.numChan;i++ ){
			pd.addChannel(new ChannelInfo(i,i, "Elster Opus channel "+(i+1), Unit.get(BaseUnit.UNITLESS)));
		}
		
        // set timers
		// set to start of day
		cal1.setTime(fromTime);
		cal1.set(Calendar.HOUR_OF_DAY, 0);	// reset hour
		cal1.set(Calendar.MINUTE, 0);		// reset minutes
		cal1.set(Calendar.SECOND, 0);		// reset seconds
		cal1.set(Calendar.MILLISECOND, 0);
		
		// check properties
        if (getProfileInterval()<=0)
            throw new IOException("load profile interval must be > 0 sec. (is "+getProfileInterval()+")");
        ParseUtils.roundDown2nearestInterval(cal1,getProfileInterval());
        // start downloading
        setTime(); // synchronize
        while(cal1.getTime().before(toTime)) {
        	command=getCommandnr(cal1.getTime()); // download the specified day
        	if(command>69)
        		throw new IOException("the requested data is no longer available. (maximum is 60 days)");
        	
        	// get the data
        	data=ocf.command(command, attempts, timeOut, null);
        	// put the data in a 2D matrix
        	datamatrix=processIntervalData(data);
        	// make the calendar object for that date
        	tempcal.setTime(cal1.getTime()); 		// set date and hour
    		tempcal.set(Calendar.HOUR_OF_DAY, 0);	// reset hour
    		tempcal.set(Calendar.MINUTE, 0);		// reset minutes
    		tempcal.set(Calendar.SECOND, 0);		// reset seconds
    		tempcal.set(Calendar.MILLISECOND, 0);
        	millis=tempcal.getTimeInMillis();
        	
        	for(int i=0; i<(3600*24/getProfileInterval()); i++){// 0->47
        		tempcal.setTimeInMillis(millis);
        		millis+=(getProfileInterval()*1000);
        		id=new IntervalData(tempcal.getTime());
        		for(int ii=0;ii<getNumberOfChannels();ii++){// 0->12
        			id.addValue(datamatrix[i][ii]);
        		}
        		if(tempcal.getTime().after(fromTime) && tempcal.getTime().before(toTime)){
            		pd.addInterval(id);        			
        		}
        	}
        	temp=cal1.getTimeInMillis()+(3600*24*1000);
        	cal1.setTimeInMillis(temp);// date change
        }
        
        //pd.addEvent(new MeterEvent(now,MeterEvent.APPLICATION_ALERT_START, "SDK Sample"));

		return pd;
	}

	protected int[][] processIntervalData(ArrayList<String[]> data) throws UnsupportedException, IOException {
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

	protected int getCommandnr(Date cal1) {
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
		return "$Revision "+Opus.protocolVersion+"$";
	}

	public String getRegister(String arg0) throws IOException,
			UnsupportedException, NoSuchRegisterException {
		// TODO Auto-generated method stub
		return null;
	}

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

	public void setProperties(Properties arg0) throws InvalidPropertyException,
			MissingPropertyException {
		// TODO Auto-generated method stub
	}

	public void updateCache(int arg0, Object arg1) throws SQLException,
			BusinessException {
	}

	public List getOptionalKeys() {
		ArrayList list = new ArrayList();
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

	public void setRegister(String arg0, String arg1) throws IOException,
			NoSuchRegisterException, UnsupportedException {
	}
	
}
