package com.energyict.protocolimpl.modbus.enerdis.recdigitcct;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.base.ProfileLimiter;
import com.energyict.protocolimpl.iec1107.Channel;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * RecDigit Cct meter is a pulse counter.
 */

public class RecDigitCct extends Modbus {

    private boolean debug = false;
    private boolean GO = true;

    /* Property Keys */
    private static final String PK_CHANNEL_MAP = "ChannelMap";
    private static final String PR_LIMIT_MAX_NR_OF_DAYS = "LimitMaxNrOfDays";

    /* Property Defaults */
    private static final String PD_CHANNEL_MAP = "1:1:1:1:1:1:1:1";

    private ChannelMap channelMap;

    private static final int INTERVAL = 1;
    private static final int POINTER = 2;
    private static final int UNIT_LOAD = 6;

    private int searchPointer[] = {0, 0, 0, 0, 0, 0, 0, 0};
    private int flagState = 0;
	private int interval;
    private int READ_STEP = 4;
    private int tempPointer = 0;
	private int addState = 0;

    private Calendar currentTime = null;
    private Calendar calendar = null;

	private Date firstDate;
	private Date date = new Date();

    private ByteArray parseDate = new ByteArray();
	private ByteArray[] previousFourByte = {new ByteArray( new byte[] { 0, 0, 0, 0} ), new ByteArray( new byte[] { 0, 0, 0, 0} ),
											new ByteArray( new byte[] { 0, 0, 0, 0} ), new ByteArray( new byte[] { 0, 0, 0, 0} ),
											new ByteArray( new byte[] { 0, 0, 0, 0} ), new ByteArray( new byte[] { 0, 0, 0, 0} ),
											new ByteArray( new byte[] { 0, 0, 0, 0} ), new ByteArray( new byte[] { 0, 0, 0, 0} )};

	private RegisterFactory rFactory;

    private IntervalData temp[] = { null, null, null, null, null };

    private VirtualMemory memChannel[] = new VirtualMemory[]
            { new VirtualMemory(this), new VirtualMemory(this), new VirtualMemory(this), new VirtualMemory(this),
    		new VirtualMemory(this), new VirtualMemory(this), new VirtualMemory(this), new VirtualMemory(this) };

    private int profileInterval = -1;
    private int nrChannels = -1;
    private int limitMaxNrOfDays;

	public RecDigitCct(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
    protected void doTheConnect() throws IOException { }

	@Override
    protected void doTheDisConnect() throws IOException {}

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec(PK_CHANNEL_MAP, false));
        propertySpecs.add(this.integerSpec(PR_LIMIT_MAX_NR_OF_DAYS, false));
        return propertySpecs;
    }

    @Override
	public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
		super.setUPLProperties(properties);
       	setInfoTypePhysicalLayer(Integer.parseInt(properties.getTypedProperty(PK_PHYSICAL_LAYER, "1").trim()));
    	setInfoTypeInterframeTimeout(Integer.parseInt(properties.getTypedProperty(PK_INTERFRAME_TIMEOUT, "100").trim()));

        String property = properties.getTypedProperty( PK_CHANNEL_MAP );
        if (property != null) {
			channelMap = new ChannelMap( property );
		} else {
			channelMap = new ChannelMap( PD_CHANNEL_MAP );
		}
        if (channelMap.getNrOfChannels() > 8) {
            String msg =
                "Nr of channels must be between 0 and 8 but is configured" +
                "to " + channelMap.getNrOfChannels();
            throw new InvalidPropertyException( msg );
        }

        this.limitMaxNrOfDays = Integer.parseInt(properties.getTypedProperty(PR_LIMIT_MAX_NR_OF_DAYS, "0"));
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "unknown";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2016-06-03 12:47:33 +0300 (Fri, 03 Jun 2016)$";
    }

    @Override
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfileWithLimiter(new ProfileLimiter(from, to, getLimitMaxNrOfDays()));
    }

    private ProfileData getProfileWithLimiter(ProfileLimiter limiter) throws IOException {
        Date from = limiter.getFromDate();
        Date to = limiter.getToDate();

        if (to.before(from)) {
            return new ProfileData();
        }

        this.interval   = this.getProfileInterval();
        this.rFactory   = this.getRecFactory();

        for (int i = 0; i < channelMap.getNrOfChannels(); i++) {

            Channel channel = channelMap.getChannel(i);
            if( ! "0".equals( channel.getRegister() ) ){
                memChannel[i].initMemory(i);
                searchPointer[i] = getPointer(i);

                if ( searchPointer[i] == 43690 ){
                    String newChannelmap = "";
                    Channel chan;
                    for( int j = 0; j < channelMap.getNrOfChannels(); j++){
                        chan = channelMap.getChannel(j);
                        if (i == j) {
                            newChannelmap = newChannelmap + "0";
                        } else {
                            newChannelmap = newChannelmap + chan.getRegister();
                        }

                        newChannelmap = newChannelmap + ":";
                    }
                    newChannelmap = newChannelmap.substring(0, newChannelmap.length()-1);
                    channelMap = new ChannelMap(newChannelmap);
                }
            }
        }


        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(newChannelInfo());

        ByteArray dataArray[] = { new ByteArray(), new ByteArray(), new ByteArray(), new ByteArray(),
                new ByteArray(), new ByteArray(), new ByteArray(), new ByteArray() };

        IntervalData currentId[];

        calendar = Calendar.getInstance( gettimeZone() );
        currentTime = Calendar.getInstance( gettimeZone() );

        if( to != null ) {
            calendar.setTime( round( to ) );
        } else {
            calendar.setTime( round( new Date() ) );
        }

        while( currentTime.getTime().after(from) & GO ){

            for ( int i = 0; i < channelMap.getNrOfChannels(); i++ ){

                Channel channel = channelMap.getChannel(i);
                if( (! "0".equals( channel.getRegister()) && (searchPointer[i] != 43690) ) ){
                    dataArray[i] 	= memChannel[i].read(searchPointer[i], READ_STEP);

                    searchPointer[i] = searchPointer[i] - ( READ_STEP/2 );
                    if ( searchPointer[i] < -2 ) {
                        searchPointer[i] = searchPointer[i] + (VirtualMemory.MEMORY_SIZE) ;
                    }
                } else {
                    dataArray[i] = null;
                }

            }

            currentId = parse(dataArray);

            if ( ( tempPointer >= 2 ) & ( flagState == 0 ) ){
                addState = 1;
            }

            if ( !currentTime.getTime().after(from) ) {
                addState = 1;
            }

            while( tempPointer > (0 + ( (GO)?1:0 ) + ( !currentTime.getTime().after(from)?-1:0 ) ) ){

                profileData.addInterval( currentId[0] );
                currentId = intervalShift(currentId);
            }
            addState = 0;
        }

        profileData = dubbelCheck(profileData);

        return profileData;
    }

    private ProfileData dubbelCheck(ProfileData profileData) {

    	for (int i = 0; i < profileData.getNumberOfIntervals(); i++){
    		for (int j = i+1; j < profileData.getNumberOfIntervals(); j++){
    			if(profileData.getIntervalData(i).getEndTime().equals( profileData.getIntervalData(j).getEndTime()) ){

    				IntervalData doubleInterval = new IntervalData(profileData.getIntervalData(i).getEndTime());

    				if(debug) {
						System.out.println("We got a double!");
					}

    				for (int k = 0; k < profileData.getNumberOfChannels(); k ++) {
						doubleInterval.addValue( ( (BigDecimal)profileData.getIntervalData(i).get(k) )
    							.add( (BigDecimal)profileData.getIntervalData(j).get(k) )
    							, 0, profileData.getIntervalData(i).getEiStatus(k) | profileData.getIntervalData(j).getEiStatus(k) );
					}

    				profileData.getIntervalDatas().remove(j);
    				profileData.getIntervalDatas().remove(i);

    				profileData.addInterval(doubleInterval);
    				i--; j--;
    				break;
    			}
    		}

    	    getLogger().fine("Next intervalTime: " + profileData.getIntervalData(i).getEndTime());

    	}

		return profileData;
	}

    @Override
	public void setTime() throws IOException {

    	if (debug) {
			System.out.println( "TESTING THE setTime!" );
		}


		Calendar instTime = Calendar.getInstance( gettimeZone() );
		byte[] currentInstantTime = new byte[8];

		currentInstantTime[0] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.MONTH) + 1) ,16)) ;
		currentInstantTime[1] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.YEAR) - 2000) ,16));
		currentInstantTime[2] = (byte) 0xff;
		currentInstantTime[3] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.DATE)) ,16));
		currentInstantTime[4] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.MINUTE)) ,16));
		currentInstantTime[5] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.HOUR_OF_DAY)) ,16));
		currentInstantTime[6] = (byte) 0xff;
		currentInstantTime[7] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.SECOND)) ,16));


		FunctionCodeFactory fcf = new FunctionCodeFactory(this);

		fcf.getWriteMultipleRegisters(0x0000, 4, currentInstantTime);

	}

	private IntervalData[] parse(ByteArray[] channel) throws IOException {

		ByteArray fourByte[] = { null, null, null, null, null, null, null, null };
		BigDecimal actualByte[] = { BigDecimal.valueOf( (long) 0 ), BigDecimal.valueOf( (long) 0 ), BigDecimal.valueOf( (long) 0 ),
				BigDecimal.valueOf( (long) 0 ), BigDecimal.valueOf( (long) 0 ), BigDecimal.valueOf( (long) 0 ),
				BigDecimal.valueOf( (long) 0 ), BigDecimal.valueOf( (long) 0 )},
				currentPercent, previousPercent;
		long intervalTime, timeDifference;
		int j = 0;
		intervalTime = interval*1000;

		for ( int i = 0; i < channel.length; i++ ){
			if (channel[i] != null){
				fourByte[j] = channel[i].sub(0,READ_STEP);
				j++;
			}
		}

        int tempPermission;
        if( !isEmpty(fourByte) ) {

			if( fourByte[0].getBytes()[2] == -1 ){

		   		addState = 0;
		   		tempPermission = 0;
				parseDate.add(fourByte[0].sub(0,READ_STEP));

				if (parseDate.size() > 4){

					parseDate = pivot(4, parseDate);
					date = rFactory.toPowerStreamDate(parseDate);
	                parseDate = new ByteArray();

	                if( firstDate == null ) {
	                    firstDate = date;
	                    currentTime.setTime( date );
	                }

	                else  {
	                	flagState = IntervalStateBits.POWERUP;
	                	tempPermission = 1;
		                fourByte = clearByteArray(fourByte);
	                	if ( firstDate.equals( date ) ){
	                		GO = false;
	                		flagState = 0;
	                		tempPermission = 0;
	                		addState = 1;
	                	}
	                }
				}
			}

			else {
				tempPermission = 1;
			}
		}

		else {

			if ( !isZero(previousFourByte) ){
				tempPermission = 1;
	            fourByte = clearByteArray(fourByte);
			}
			else {
				tempPermission = 0;
				jumpBack(currentTime);
			}

		}

		if ( tempPermission == 1 ){

			timeChecks(intervalTime);

			timeDifference = ( currentTime.getTimeInMillis() - ( currentTime.getTimeInMillis()%60000 ) )
			- ( calendar.getTimeInMillis() - ( calendar.getTimeInMillis()%60000 ) );

			if ( timeDifference >= intervalTime ){

				if ( tempPointer > 0 ){
					timeDifference = timeDifference - intervalTime;
					tempPointer--;
					calendar.add( Calendar.SECOND, getProfileInterval() );
					flagState = flagState | temp[tempPointer].getEiStatus(tempPointer);
					for (int i = 0; i < notNullCount(previousFourByte); i++ ) {
						actualByte[i] = (BigDecimal)temp[tempPointer].get(i);
					}
					temp[tempPointer] = new IntervalData( round( currentTime.getTime() ) );
				}
			}

			currentPercent = BigDecimal.valueOf(timeDifference).divide(BigDecimal.valueOf(intervalTime),5,BigDecimal.ROUND_HALF_UP);
			previousPercent = BigDecimal.valueOf( (long) 1 ).subtract(currentPercent);

			for ( int i = 0; i < notNullCount(fourByte); i++ ){
				if (notEmpty(fourByte[i])) {
					fourByte[i] = new ByteArray( new byte[] { 0, 0, 0, 0} );
				}

				actualByte[i] = actualByte[i].add( toBigDecimal( fourByte[i] ).multiply( currentPercent )
						.add( toBigDecimal( previousFourByte[i] ).multiply( previousPercent ) ) );
			}

			if (debug) {
				System.out.println(calendar.getTime());
			}

			previousFourByte = fourByte;

			if ( ( addState == 0 ) & ( currentTime.getTimeInMillis() != firstDate.getTime() ) ){

				if ( temp[tempPointer] == null ) {
					temp[tempPointer] = new IntervalData( round( currentTime.getTime() ) );
				}

				for ( int i = 0; i < notNullCount(fourByte); i++) {
					temp[tempPointer].addValue( actualByte[i], 0, flagState );
				}

		    	tempPointer++;

	            calendar.add( Calendar.SECOND, -getProfileInterval() );

				if ( ( flagState == IntervalStateBits.POWERDOWN ) | ( flagState == 3 ) ) {
					flagState = 0;
				}
			}

	        jumpBack(currentTime);

			if ( flagState == IntervalStateBits.POWERUP ){
				currentTime.setTime( date );
				flagState = IntervalStateBits.POWERDOWN;
			}
		}

		return temp;
	}

	private boolean notEmpty(ByteArray byteArray) {
        return
        byteArray.getBytes()[0] == -1 &&
        byteArray.getBytes()[1] == -1 &&
        byteArray.getBytes()[2] == -1 &&
        byteArray.getBytes()[3] == -1;
	}


	private int notNullCount(ByteArray byteArray[]){
		int count = 0;
		for (int i = 0; i < byteArray.length; i++) {
			if (byteArray[i] != null) {
				count++;
			}
		}
		return count;
	}

    ByteArray pivot(int pivotPoint, ByteArray victim) {
        return new ByteArray( )
        .add( victim.sub(pivotPoint) )
        .add( victim.sub(0, pivotPoint) );
	}

    private ByteArray[] clearByteArray(ByteArray[] bArray){

    	for (int i = 0; i < notNullCount(bArray); i++) {
			bArray[i] = new ByteArray( new byte[] { 0, 0, 0, 0} );
		}

    	return bArray;
    }

    private IntervalData[] intervalShift(IntervalData[] currentIntData){

    	for(int i = 0; i<4; i++){
    		currentIntData[i] = currentIntData[i+1];
    	}
    	temp = currentIntData;
    	tempPointer--;
    	return currentIntData;
    }

    private void timeChecks(long intervalTime) throws IOException{

		while (currentTime.getTime().before(calendar.getTime())){
        	calendar.add( Calendar.SECOND, -getProfileInterval() );
		}

		if ( ( currentTime.getTimeInMillis() - ( currentTime.getTimeInMillis()%60000 ) )
				- ( calendar.getTimeInMillis() - ( calendar.getTimeInMillis()%60000 ) ) > 2*intervalTime ){
			while( ( currentTime.getTimeInMillis() - ( currentTime.getTimeInMillis()%60000 ) )
					- ( calendar.getTimeInMillis() - ( calendar.getTimeInMillis()%60000 ) ) > intervalTime ){
				calendar.add( Calendar.SECOND, getProfileInterval() );
			}
		}

    }

	private boolean isEmpty(ByteArray[] byteArray) {
		boolean state = true;
		for ( int i = 0; i < notNullCount(byteArray); i++ ){
			if ( ( byteArray[i] != null ) & state ){
				state = byteArray[i].getBytes()[0] == -1 && byteArray[i].getBytes()[1] == -1 &&
						byteArray[i].getBytes()[2] == -1 && byteArray[i].getBytes()[3] == -1;
			}
		}
        return state;
    }

	private boolean isZero(ByteArray[] previousByteArray){
		boolean state = false;
		for ( int i = 0; i < notNullCount(previousByteArray); i++){
			if ( previousByteArray[i].getBytes()[0] == 0 &&	previousByteArray[i].getBytes()[1] == 0 &&
				previousByteArray[i].getBytes()[2] == 0 && previousByteArray[i].getBytes()[3] == 0 ) {
				state = true;
			}
		}
		return state;
	}

    private BigDecimal toBigDecimal(ByteArray byteArray) {
        BigDecimal bd = rFactory.toBigDecimal(Type.UNSIGNED_LONG, byteArray);
        return bd.movePointLeft(1);
    }

    private void jumpBack(Calendar currentTime) {
        currentTime.add(Calendar.SECOND, -interval);
    }

    private int getInterval(int cIndex) throws IOException {
        return
            readValue(VirtualMemory.MEMORY_BLOCKS[cIndex][INTERVAL], Type.UNSIGNED_SHORT)
                .intValue();
    }

    private int getPointer(int cIndex) throws IOException {
    	int pointer = readValue(VirtualMemory.MEMORY_BLOCKS[cIndex][POINTER], Type.UNSIGNED_SHORT).intValue();

    	pointer = pointer * 2;

        return pointer;

    }

    private Date round( Date date ) throws IOException {
        long msRest = date.getTime() % (getProfileInterval() * 1000);
        return new Date(date.getTime() - msRest);
    }

    private int[] readValue(int address, int length) throws IOException {
        HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();
    }

    private int getUnit(int uIndex) throws IOException{
        return readValue(VirtualMemory.MEMORY_BLOCKS[uIndex][UNIT_LOAD], Type.CHAR).intValue();
    }

    private List<ChannelInfo> newChannelInfo( ) throws IOException{
    	Unit u;
        List<ChannelInfo> result = new ArrayList<>();
        int count = 0;
		final ObisCode baseObisCode = ObisCode.fromString("0.0.128.0.0.255");
		for (int i = 0; i < channelMap.getNrOfChannels(); i++) {
			Channel channel = channelMap.getChannel(i);
			if (!"0".equals(channel.getRegister())) {
				int unitCode = getUnit(i);
				int unitScale = unitCode % 10;
				u = Unit.get(unitCode / 10, unitScale);
				result.add(new ChannelInfo(count++, ProtocolTools.setObisCodeField(baseObisCode, 1, (byte) (i + 1)).toString(), u));
			}
		}
		return result;
    }

    /* Could save a rountrip here .... */
    public int getProfileInterval() throws IOException {

        if( profileInterval == -1 ) {   /* lazy init */

            for (int j = 0; (j<channelMap.getNrOfChannels()) && profileInterval==-1; j++) {
                Channel channel = channelMap.getChannel(j);
                if( ! "0".equals( channel.getRegister() ) ) {
                    profileInterval = getInterval(j);
                }
            }
        }

        return profileInterval;

    }

    @Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return getRegisterFactory().toString();
    }

    @Override
    public int getNumberOfChannels()
        throws IOException {

        if( nrChannels == -1 ) {    /* lazy init */

            nrChannels = 0;
            for (int i = 0; i < channelMap.getNrOfChannels(); i++) {

               Channel channel = channelMap.getChannel(i);
               if( ! "0".equals( channel.getRegister() ) ) {
				nrChannels = nrChannels + 1;
			}

            }

        }
        return nrChannels;

    }

    @Override
    public Date getTime() throws IOException {
        return getRecFactory().toDate( readValue(0x0000, 4) );
    }

    private RegisterFactory getRecFactory() {
        return (RegisterFactory)getRegisterFactory();
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r  = getRegisterFactory().findRegister(obisCode);
        return new RegisterInfo( r.getName() );
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r = getRegisterFactory().findRegister(obisCode);
        String key = r.getName();
        try {
            return r.registerValue(key);
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }

    /**
     * @param address   offset
     * @param length    nr of words
     * @return          int[] 2 bytes per int
     */
    int[] readRawValue(int address, int length)  throws IOException {
        HoldingRegister r = new HoldingRegister(address, length);
        r.setRegisterFactory(getRegisterFactory());
        return r.getReadHoldingRegistersRequest().getRegisters();
    }

    private BigDecimal readValue(int address, Type type) throws IOException {
        int [] values = readRawValue( address, type.wordSize() );
        return getRecFactory().toBigDecimal(type, values);
    }

    void dbg( String msg ) {
        if( debug ) {
			System.out.println(msg);
		}
    }

    @Override
    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }

    public int getLimitMaxNrOfDays() {
        return limitMaxNrOfDays;
    }

}