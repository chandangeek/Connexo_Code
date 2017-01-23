package com.energyict.protocolimpl.elster.opus;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnection;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

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
 *
 * <p>
 *  4) Opus: this is the meter protocol, in the connect method some meter props are
 *  	read.
 *  <p>
 *  Initial version:<p>
 *  ----------------<p>
 *  Author: Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
 *  Version: 1.0 <p>
 *  First edit date: 9/07/2008 PST<p>
 *  Last edit date: 31/07/2008  PST<p>
 *  Comments: Beta ready for testing<p>
 *  Released for testing: 31/07/2008<p>
 *  <p>
 *  Revisions<p>
 *  ----------------<p>
 *  @author Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
 *  @version 1.01<p>
 *  First edit date:13/08/2008 <p>
 *  Last edit date:13/08/2008 <p>
 *  Comments:changes in command factory: empty matrix handling and offset calculation <p>
 *  	debug, firmware problem solved with error throw<p>
 *  released for testing:13/08/2008
 *
 *  Author: <p>
 *  Version:<p>
 *  First edit date: <p>
 *  Last edit date: <p>
 *  Comments:<p>
 *  released for testing:<p>
 * ---------------------------------------------------------------------------------<p>
 *
 */
public class Opus extends AbstractProtocol {

	@Override
	public String getProtocolDescription() {
		return "Elster OPUS";
	}

	private ProtocolChannelMap channelMap=null;

	private String oldPassword;
	private String newPassword;
	private int outstationID;

	private InputStream inputStream;
	private OutputStream outputStream;
	private OpusCommandFactory ocf; 	// command factory
	private ObisCodeMapper ocm;
	private int timeOut=5000;			// timeout time in ms
	private int attempts=3;				// number of attempts

	// attributes to retrieve from the data
	private int numChan=-1;				// number of channels (derived from channel map)
	private int interval=-1;			// temp value
	private String firmwareVersion;

	private TimeZone timezone;

	@Inject
	public Opus(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	public void connect() throws IOException {
		// download final information
		ArrayList s;								// ArrayList to catch data from factory
		try {
			s=ocf.command(121,attempts,timeOut, null);			// factory command
		} catch (IOException e) {
			throw new IOException(e.getMessage()+ ". Interframe timeout probably caused because no node address "+this.outstationID+" is found");
		}
		String[] st;
		st=(String[]) s.get(0);
		this.numChan=Integer.parseInt(st[1]);			// set number of channels in this object
		this.interval=24*3600/Integer.parseInt(st[0]);	// set interval in this object
		this.firmwareVersion=st[6];
		// set factory globals (IMORTANT)
		ocf.setNumChan(this.numChan);
		if(numChan<channelMap.getNrOfProtocolChannels()){throw new IOException("You defined more channels ("+channelMap.getNrOfProtocolChannels()+") than channels available on the instrument("+numChan+")");}
		// make channelmap with all channels enabled
		// end of download
		// change numChan to the real numChan derived from the channelMap

	}

	public void disconnect() throws IOException {
	}

	public Object fetchCache(int arg0) {
		return null;
	}

	public Object getCache() {
		return null;
	}

	public String getFirmwareVersion() throws IOException {
		return this.firmwareVersion;
	}
	public int getNumberOfChannels() throws IOException {
        if (this.channelMap.getNrOfProtocolChannels() == -1) {
	        throw new IOException("getNumberOfChannels(), ChannelMap property not given. Cannot determine the nr of channels...");
        }
		return this.channelMap.getNrOfProtocolChannels();
	}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
//		return getProfileData(, includeEvents);
		return null;
	}

	public ProfileData getProfileData(Date fromTime, boolean includeEvents)
			throws IOException {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		Calendar cal=Calendar.getInstance(tz);
		return getProfileData(fromTime, cal.getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date fromTime, Date toTime, boolean event) throws IOException {
		Calendar now= Calendar.getInstance(timezone);
		Calendar endtime=Calendar.getInstance(timezone);
		endtime.setTime(toTime);
		long millis=endtime.getTimeInMillis();
		if (now.getTimeInMillis()-1000*getProfileInterval()<millis) {
			endtime.setTimeInMillis(millis-1000*getProfileInterval());
			toTime=endtime.getTime();
		}
		OpusGetProfileData ogpd=new OpusGetProfileData();
		ogpd.setTimeZone(timezone);
		return ogpd.getProfileData(fromTime, toTime, event, this.channelMap, this.ocf, this.numChan, getProfileInterval(), this.attempts, this.timeOut);
	}

	public int getProfileInterval() throws IOException {
		return this.interval;
	}

    public String getProtocolVersion() {
		return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
	}

    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e
    *******************************************************************************************/
   public RegisterValue readRegister(ObisCode obisCode) throws IOException {
	   if(ocm == null)
		   ocm = new ObisCodeMapper(this);
       return ocm.getRegisterValue(obisCode);
   }

   public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
	   RegisterInfo registerInfo = new RegisterInfo("");
	   return registerInfo;
   }

   public Date getTime() throws IOException  {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		Calendar cal=Calendar.getInstance(tz);
		ArrayList d=ocf.command(102, attempts, timeOut, null);
		String[] s= (String[]) d.get(d.size()-1); // last index
		cal.set(Integer.parseInt(s[5])+2000,
				Integer.parseInt(s[4])-1, // correction for java date
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

	public void initializeDevice() throws IOException {
	}

	public void release() throws IOException {
	}

	public void setCache(Object arg0) {
	}

	public void setProperties(Properties properties) throws InvalidPropertyException,	MissingPropertyException {
		try {
			this.outstationID = Integer.parseInt(properties.getProperty("NodeAddress"));
		} catch (NumberFormatException e) {
			throw new NumberFormatException("The node address field has not been filled in");
		}
		this.channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap","1"));
		this.timeOut=Integer.parseInt(properties.getProperty("TimeOut","5000"));
		this.attempts=Integer.parseInt(properties.getProperty("Retry", "3"));
		this.oldPassword=properties.getProperty("Password");
		this.newPassword=this.oldPassword;

	}

	public void updateCache(int arg0, Object arg1) {
	}

	public List<String> getOptionalKeys() {
		return Arrays.asList("ChannelMap", "TimeOut");
	}

	public List<String> getRequiredKeys() {
		return Collections.singletonList("Password");
	}

	public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2,
			Logger arg3) throws IOException {
		// set streams
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.timezone=arg2;

        // build command factory
		this.ocf=new OpusCommandFactory(this.outstationID,this.oldPassword,this.newPassword,this.inputStream,this.outputStream);
        this.ocf.setTimeZone(this.timezone);

		if(this.channelMap==null){ // if no setProperties has been called
			String cs="1";
			for(int i=1;i<this.numChan; i++){
				cs+=":1";
			}
			channelMap=new ProtocolChannelMap(cs);
		}

		this.ocf.setChannelMap(this.channelMap); // set the channel map in the factory
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
	// additional getters and setters
	public OpusCommandFactory getOcf() {
		return ocf;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}


}
