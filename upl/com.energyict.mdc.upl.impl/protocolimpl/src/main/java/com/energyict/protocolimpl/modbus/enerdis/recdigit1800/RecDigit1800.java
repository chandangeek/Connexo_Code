package com.energyict.protocolimpl.modbus.enerdis.recdigit1800;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;


/**
 *
 * @author fbl
 * @author gna
 * 
 * @beginchanges
 * FBL|12092007| initial release 
 * 
 * @endchanges
 */

public class RecDigit1800 extends Modbus {

    private boolean debug = false;
    private boolean GO = true;
    
    private byte[] currentInstantTime = new byte[6];
    
    final static Unit kWh = Unit.get(BaseUnit.WATT);
    final static Unit kVAr = Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
    
    private final int ACTIVE        = 0;
    private final int REACTIVE      = 1;
    private final int READ_BYTES 	= 4;
    
    private int READ_STEP = 4;
    private int tempPointer = 0;
    private int profileInterval = -1;
	private int tempPermission = 0;
    private int searchPointer;
	private int addState = 0;
    private int flagState = 0;
    private int interval;

	private ByteArray previousFourAct = new ByteArray(), previousFourReact = new ByteArray();
    private ByteArray	parseDate = new ByteArray();

    private Calendar currentTime = null;
    private Calendar calendar = null;
    
	private Date date = new Date();
	private Date firstDate;
    
    private BigDecimal ku;
    private BigDecimal ki;
    private BigDecimal kp;
    private BigDecimal ctRatio;
    private BigDecimal ptRatio;
	private RegisterFactory rFactory;
    
    private VirtualMemory memChannel[] = new VirtualMemory[] { new VirtualMemory(this), new VirtualMemory(this) }; 

    private IntervalData temp[] = {null, null, null, null, null, null, null, null, null, null};

    public RecDigit1800() { }
    
    protected void doTheConnect() throws IOException { }
    protected void doTheDisConnect() throws IOException {}
    protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    	
    	setInfoTypePhysicalLayer(Integer.parseInt(properties.getProperty("PhysicalLayer","1").trim()));    	
    	setInfoTypeInterframeTimeout(Integer.parseInt(properties.getProperty("InterframeTimeout","100").trim()));
    	
    }
    
    public String getFirmwareVersion() 
        throws IOException, UnsupportedException {
        return "unknown";        
    }
    
    protected List doTheGetOptionalKeys() {
        return new ArrayList();
    }

    public String getProtocolVersion() {
        return "$Date$";
    }
    
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactory(this));
    } 
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) 
        throws IOException, UnsupportedException {
    	
    	
    	this.interval   = this.getProfileInterval();
    	this.rFactory   = this.getRecFactory();
    
    	memChannel[ACTIVE].initMemory(ACTIVE);		
    	memChannel[REACTIVE].initMemory(REACTIVE);	
    	
    	previousFourAct = new ByteArray( new byte[]{ 0, 0, 0, 0 } ); previousFourReact = new ByteArray( new byte[]{ 0, 0, 0, 0 } );
       
    	searchPointer = getPointer();
        
        ByteArray activeArray,reactiveArray = new ByteArray();
        
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(newChannelInfo());
        
        IntervalData currentId[] = null;
    
        calendar = Calendar.getInstance( gettimeZone() );
    	currentTime = Calendar.getInstance( gettimeZone() );
        
        if( to != null )
            calendar.setTime( round( to ) );
        else
            calendar.setTime( round( new Date() ) );
        
        getLogger().info( "getProfileData( from=" + from + ", to=" + to + " ) " );
        
    	while( currentTime.getTime().after(from) & GO ){
            
            activeArray 	= memChannel[ACTIVE].read(searchPointer, READ_STEP);
            reactiveArray 	= memChannel[REACTIVE].read(searchPointer, READ_STEP);   
            
            currentId = parse(activeArray, reactiveArray);       
            
            if ( !currentTime.getTime().after(from) ) 
            	addState = 1;
            
            searchPointer = searchPointer - READ_STEP/4;   
            if ( searchPointer < 0 ){
            	searchPointer = searchPointer + (memChannel[ACTIVE].MEMORY_SIZE)/4;
            }
            if ( ( addState == 1 ) & ( currentId[0] != null ) ){
            	
            	while( tempPointer > (0 + ( (GO)?1:0 ) + ( !currentTime.getTime().after(from)?-1:0 ) ) ){
            		
        			profileData.addInterval( currentId[0] );
    	            currentId = intervalShift(currentId);     
            	}
            	addState = 0;        	
            }
        }
    	
    	profileData = dubbelCheck(profileData);

        return profileData;
        
    }
    
    public void setTime() throws IOException {
    	
    	if (debug) System.out.println( "TESTING THE setTime!" ); 
 	
    	Calendar instTime = Calendar.getInstance( gettimeZone() );

    	currentInstantTime[0] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.MONTH) + 1) ,16)) ;
    	currentInstantTime[1] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.YEAR) - 2000) ,16));
    	currentInstantTime[2] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.HOUR_OF_DAY)) ,16));
    	currentInstantTime[3] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.DATE)) ,16));
    	currentInstantTime[4] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.SECOND)) ,16));
    	currentInstantTime[5] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.MINUTE)) ,16));
    	
    	FunctionCodeFactory fcf = new FunctionCodeFactory(this);

    	fcf.getWriteMultipleRegisters(0x19dc, 4, new byte[] { 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55 });
    	fcf.getWriteMultipleRegisters(0x0000, 3, currentInstantTime);
    	fcf.getWriteMultipleRegisters(0x19dc, 4, new byte[] { 0x63, 0x65, 0x69, 0x72, 0x75, 0x74, 0x65, 0x72 });
    	
    }
    
    private ProfileData dubbelCheck(ProfileData profileData) {
		
    	for (int i = 0; i < profileData.getNumberOfIntervals(); i++){
    		for (int j = i+1; j < profileData.getNumberOfIntervals(); j++){
    			if(profileData.getIntervalData(i).getEndTime().equals( profileData.getIntervalData(j).getEndTime()) ){
    				
    				IntervalData doubleInterval = new IntervalData(profileData.getIntervalData(i).getEndTime());
    				
    				if(debug)System.out.println("We got a double!");
    				
    				for (int k = 0; k < profileData.getNumberOfChannels(); k ++)
    					doubleInterval.addValue( ( (BigDecimal)profileData.getIntervalData(i).get(k) )
    							.add( (BigDecimal)profileData.getIntervalData(j).get(k) )
    							, 0, profileData.getIntervalData(i).getEiStatus(k) | profileData.getIntervalData(j).getEiStatus(k) );
    				
    				profileData.getIntervalDatas().remove(j);
    				profileData.getIntervalDatas().remove(i);
    				
    				profileData.addInterval(doubleInterval);
    				i--; j--;
    				break;
    			}
    		}
    		
    		System.out.println("Next intervalTime: " + profileData.getIntervalData(i).getEndTime() );
    		
    	}
    	
		return profileData;
	}
    
    IntervalData[] parse(ByteArray active, ByteArray reactive) throws UnsupportedException, IOException{
    	
		ByteArray fourByteAct = active.sub( 0, READ_BYTES ), fourByteReact = reactive.sub( 0, READ_BYTES );
		BigDecimal actualAct = null, actualReact = null, currentPercent, previousPercent;
		long intervalTime, timeDifference;
	
		intervalTime = 60*10*1000;
		
		if ( debug ) {
			System.out.println("The active value:" + fourByteAct.toHexaString(true));  
			System.out.println("The reactive value:" + fourByteReact.toHexaString(true));
		}
		
		if( !isEmpty(fourByteAct) ) {
			
			if( fourByteAct.getBytes()[2] == -1 ){
				
		   		addState = 0;
		   		tempPermission = 0;
				parseDate.add(fourByteAct);
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
    	                fourByteAct = new ByteArray( new byte[] { 0, 0, 0, 0} );
    	                fourByteReact = new ByteArray( new byte[] { 0, 0, 0, 0} ); 
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
			if ( !isZero(previousFourAct) ){
				tempPermission = 1;
                fourByteAct = new ByteArray( new byte[] { 0, 0, 0, 0} );
                fourByteReact = new ByteArray( new byte[] { 0, 0, 0, 0} ); 
			}
			else {
				tempPermission = 0;
				jumpBack(currentTime);			
			}
		}
		
		if ( tempPermission  == 1 ){	
			
			timeChecks(intervalTime);
			actualAct = BigDecimal.valueOf( (long) 0 );
			actualReact = BigDecimal.valueOf( (long) 0 );
			
			timeDifference = ( currentTime.getTimeInMillis() - ( currentTime.getTimeInMillis()%60000 ) ) 
			- ( calendar.getTimeInMillis() - ( calendar.getTimeInMillis()%60000 ) );

		if ( timeDifference >= intervalTime ){

			if ( tempPointer > 0 ){
				timeDifference = timeDifference - intervalTime;
				tempPointer--;
				calendar.add( Calendar.SECOND, getProfileInterval() );
				flagState = flagState + temp[tempPointer].getEiStatus(tempPointer);
				actualAct = (BigDecimal)temp[tempPointer].get(0);
				actualReact = (BigDecimal)temp[tempPointer].get(1);	
			}
				
		}
			
		currentPercent = BigDecimal.valueOf(timeDifference).divide(BigDecimal.valueOf(intervalTime),5,BigDecimal.ROUND_HALF_UP);
			previousPercent = BigDecimal.valueOf( (long) 1 ).subtract(currentPercent);

			actualAct = actualAct.add( toBigDecimal( fourByteAct ).multiply( currentPercent )
					.add( toBigDecimal( previousFourAct ).multiply( previousPercent ) ) );
			actualReact = actualReact.add( toBigDecimal( fourByteReact ).multiply( currentPercent )
					.add( toBigDecimal( previousFourReact ).multiply( previousPercent ) ) );
			
			previousFourAct = fourByteAct;
			previousFourReact = fourByteReact;
			
			if ( ( addState == 0 ) & ( currentTime.getTimeInMillis() != firstDate.getTime() ) ){
				
				temp[tempPointer] = new IntervalData( round( currentTime.getTime() ) );
				temp[tempPointer].addValue( actualAct, 0, flagState );
				temp[tempPointer].addValue( actualReact, 0, flagState );
	            calendar.add( Calendar.SECOND, -getProfileInterval() ); 
				tempPointer++;
				
				if ( ( tempPointer >= 2 ) & ( flagState == 0 ) ){
					addState = 1;
				}
				
				if ( ( flagState == IntervalStateBits.POWERDOWN ) | ( flagState == 3 ) )
					flagState = 0;
			}
			
            jumpBack(currentTime);  
            
			if ( flagState == IntervalStateBits.POWERUP ){
				currentTime.setTime( date ); 
				flagState = IntervalStateBits.POWERDOWN;
			}
		}
		
		return temp;
    }
    
    public ByteArray pivot(int pivotPoint, ByteArray victim) {
        return new ByteArray( )
        .add( victim.sub(pivotPoint) )
        .add( victim.sub(0, pivotPoint) );
	}

    private IntervalData[] intervalShift(IntervalData[] currentIntData){
    	
    	for(int i = 0; i<9; i++){
    		currentIntData[i] = currentIntData[i+1];
    	}
    	temp = currentIntData;
    	tempPointer--;
    	return currentIntData;
    }
    
    private void timeChecks(long intervalTime) throws UnsupportedException, IOException{
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
    
	private boolean isEmpty(ByteArray byteArray) {
        return 
            byteArray.getBytes()[0] == -1 && 
            byteArray.getBytes()[1] == -1 &&
            byteArray.getBytes()[2] == -1 && 
            byteArray.getBytes()[3] == -1;
    }
	
	private boolean isZero(ByteArray previousByteArray){
		return
			previousByteArray.getBytes()[0] == 0 &&
			previousByteArray.getBytes()[1] == 0 &&
			previousByteArray.getBytes()[2] == 0 &&
			previousByteArray.getBytes()[3] == 0;
	}
    
    private void jumpBack(Calendar currentTime) {
        currentTime.add(Calendar.SECOND, -interval);
    }
    
    private BigDecimal toBigDecimal(ByteArray byteArray) throws IOException {
        BigDecimal bd = rFactory.toBigDecimal(Type.LONG_WORD, byteArray);
        bd = bd.divide(getKP(), 3, BigDecimal.ROUND_HALF_UP);
        bd = bd.multiply(getCtRatio());
        bd = bd.multiply(getPtRatio());
        return bd;
    }
    
    private Date round( Date date ) throws UnsupportedException, IOException {
        long msRest = date.getTime() % (getProfileInterval() * 1000);
        return new Date(date.getTime() - msRest);
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
    
    BigDecimal readValue(int address, Type type) throws IOException {
        
        int [] values = readRawValue( address, type.wordSize() );
        return getRecFactory().toBigDecimal(type, values);
        
    }
    
    /* 
     * Construct a list of ChannelInfo's:
     *  - as far as the doc's explain, the unit is fixed.  Scaling is done
     *    using kp, ct and vt
     *  - to keep things simple both channels are _always_ read.
     * 
     */
    private List newChannelInfo( ){
        ArrayList result = new ArrayList();
        result.add( new ChannelInfo(0, "0.1.128.0.0.255", RegisterFactory.W) );     //Active Power Stream
        result.add( new ChannelInfo(1, "0.2.128.0.0.255", RegisterFactory.VAr) ); //Reactive Power Stream
        return result;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        if( profileInterval == -1 ) {
            profileInterval = readValue( 0x19F4, Type.WORD).intValue();
        }
        return profileInterval;
    }
    
    private int getPointer() throws UnsupportedException, IOException {
        return readValue(0x03FC, Type.WORD).intValue();
    }
        
    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return getRecFactory().toString();
    }
    
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 2; 
    }
    
    public Date getTime() throws IOException {
        return (Date)getRecFactory().toDate( readRawValue(0x0000, 4) );
    }
    
    public RegisterFactory getRecFactory( ) {
        return (RegisterFactory)getRegisterFactory();
    }
    
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r  = getRegisterFactory().findRegister(obisCode);
        return new RegisterInfo( r.getName() ); 
    }    
    
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        AbstractRegister r  = getRegisterFactory().findRegister(obisCode);
        String key          = r.getName();

        try {
            return r.registerValue(key);
        } catch (ModbusException e) {
            getLogger().warning("Failed to read register " + obisCode.toString() + " - " + e.getMessage());
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
        }
    }
    
    /** Transformation coefficient V */
    BigDecimal getKU( ) throws IOException {
        if (ku == null) {
            ku = readValue(0x0006, Type.REAL_NUMBER);
        }
        return ku;
    }
    
    /** Transformation coefficient I */
    BigDecimal getKI( ) throws IOException {
        if (ki == null) {
            ki = readValue(0x000a, Type.REAL_NUMBER);
        }
        return ki;
    }
    
    /** Transformation coefficient P */
    BigDecimal getKP( ) throws IOException {
        if (kp == null) {
            kp = readValue(0x000e, Type.REAL_NUMBER);
        }
        return kp;
    }
    
    BigDecimal getCtRatio( ) throws IOException {
        if( ctRatio == null ) {
            ctRatio = readValue(0x19fa, Type.REAL_NUMBER); 
        }
        return ctRatio;
    }
   
    BigDecimal getPtRatio( ) throws IOException {
        if( ptRatio == null ) {
            ptRatio = readValue(0x19fe, Type.REAL_NUMBER);
        }
        return ptRatio;        
    }
    
    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }    
 
}
