package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.discover.DiscoverResult;
import com.energyict.protocol.discover.DiscoverTools;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCodeFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/** 
 * RecDigit Cct meter is a pulse counter. 
 */

public class RecDigitCdtPr extends RecDigitCdt {
	
    private boolean debug = false;

    final static Unit kWh = Unit.get(BaseUnit.WATT);
    
    private final int ACTIVE        = 0;
    private final int READ_BYTES 	= 4;
    
    public int searchPointer;
    private int flagState = 0;
	private int interval;
    private int READ_STEP = 4;
    private int pointer = -1;
    private int profileInterval = -1;
    private int tempPointer = 0;
	private int addState = 0;
	private int tempPermission = 0;
    
    private Calendar currentTime = null;
    private Calendar calendar = null;

	private ByteArray previousFourAct = new ByteArray();
    private ByteArray	parseDate = new ByteArray();

	private Date firstDate;
	private Date date = new Date();
    private boolean GO = true;

    private IntervalData temp[] = { null, null, null, null, null };
    
    private VirtualMemory memChannel[] = new VirtualMemory[] { new VirtualMemory(this), new VirtualMemory(this) };
	
	private RegisterFactoryCdtPr rFactory;
	
    protected void initRegisterFactory() {
        setRegisterFactory(new RegisterFactoryCdtPr(this));
    }
    
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) 
    throws IOException, UnsupportedException {
	
		this.interval   = this.getProfileInterval();
		this.rFactory   = this.getRecFactory();
	    
	    ProfileData profileData = new ProfileData();
	    profileData.setChannelInfos(newChannelInfo());
	    
		searchPointer = getPointer();
	   
    	memChannel[ACTIVE].initMemory(ACTIVE);		
    	
    	previousFourAct = new ByteArray( new byte[]{ 0, 0, 0, 0 } );
        
        ByteArray activeArray = new ByteArray();
        IntervalData currentId[] = null;
    
        calendar = Calendar.getInstance( gettimeZone() );
    	currentTime = Calendar.getInstance( gettimeZone() );
        
        if( to != null )
            calendar.setTime( round( to ) );
        else
            calendar.setTime( round( new Date() ) );
        
    	while( currentTime.getTime().after(from) & GO ){
            
            activeArray = memChannel[ACTIVE].read(searchPointer, READ_STEP); 

            currentId = parse(activeArray);
            
            if ( !currentTime.getTime().after(from) ) 
            	addState = 1;
            
            searchPointer = searchPointer - READ_STEP;   
            if ( searchPointer == -4 ){
            	searchPointer = searchPointer + (memChannel[ACTIVE].MEMORY_SIZE) ;
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
  
    public void setTime() throws IOException {
    	
    	if (debug) System.out.println( "TESTING THE setTime!" ); 
    		
        	Calendar instTime = Calendar.getInstance( gettimeZone() );
        	byte[] currentInstantTime = new byte[6];

        	currentInstantTime[0] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.MONTH) + 1) ,16)) ;
        	currentInstantTime[1] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.YEAR) - 2000) ,16));
        	currentInstantTime[2] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.HOUR_OF_DAY)) ,16));
        	currentInstantTime[3] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.DATE)) ,16));
        	currentInstantTime[4] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.SECOND)) ,16));
        	currentInstantTime[5] = (byte)(Integer.parseInt( Integer.toString(instTime.get(Calendar.MINUTE)) ,16));
        	
        	FunctionCodeFactory fcf = new FunctionCodeFactory(this); 

        	fcf.getWriteMultipleRegisters(0x19DC, 4, new byte[] { 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55 });
        	fcf.getWriteMultipleRegisters(0x0000, 3, currentInstantTime);
        	fcf.getWriteMultipleRegisters(0x19DC, 4, new byte[] { 0x63, 0x65, 0x69, 0x72, 0x75, 0x74, 0x65, 0x72 });
        	
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
    
    IntervalData[] parse(ByteArray active) throws UnsupportedException, IOException{
    	
		ByteArray fourByteAct = active.sub( 0, READ_BYTES );
		BigDecimal actualAct = null, currentPercent, previousPercent;
		long intervalTime, timeDifference;
	
		intervalTime = 60*10*1000;
		
		if ( debug ) {
			System.out.println("The active value:" + fourByteAct.toHexaString(true));  
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
			}
			else {
				tempPermission = 0;
				jumpBack(currentTime);			
			}
		}
		
		if ( tempPermission  == 1 ){	
			
			timeChecks(intervalTime);
			actualAct = BigDecimal.valueOf( (long) 0 );
			
			timeDifference = ( currentTime.getTimeInMillis() - ( currentTime.getTimeInMillis()%60000 ) ) 
				- ( calendar.getTimeInMillis() - ( calendar.getTimeInMillis()%60000 ) );

			if ( timeDifference >= intervalTime ){
				
				if ( tempPointer > 0 ){
					
    				timeDifference = timeDifference - intervalTime;
    				tempPointer--;
    				calendar.add( Calendar.SECOND, getProfileInterval() );
				
					flagState = flagState + temp[tempPointer].getEiStatus(tempPointer);
    				actualAct = (BigDecimal)temp[tempPointer].get(0);
    				
				}
		
			}
			
			currentPercent = BigDecimal.valueOf(timeDifference).divide(BigDecimal.valueOf(intervalTime),5,BigDecimal.ROUND_HALF_UP);
			previousPercent = BigDecimal.valueOf( (long) 1 ).subtract(currentPercent);

			actualAct = actualAct.add( toBigDecimal( fourByteAct ).multiply( currentPercent )
					.add( toBigDecimal( previousFourAct ).multiply( previousPercent ) ) );;
			
			previousFourAct = fourByteAct;
			
			if ( ( addState == 0 ) & ( currentTime.getTimeInMillis() != firstDate.getTime() ) ){
				
				temp[tempPointer] = new IntervalData( round( currentTime.getTime() ) );
				temp[tempPointer].addValue( actualAct, 0, flagState );
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
    	
    	for(int i = 0; i<4; i++){
    		currentIntData[i] = currentIntData[i+1];
    	}
    	temp = currentIntData;
    	tempPointer--;
    	return currentIntData;
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
	
    private void timeChecks(long intervalTime) throws UnsupportedException, IOException{
		while (currentTime.getTime().before(calendar.getTime())){
        	calendar.add( Calendar.SECOND, -getProfileInterval() ); 
		}
    }
    
    private BigDecimal toBigDecimal(ByteArray byteArray) throws IOException {
        BigDecimal bd = rFactory.toBigDecimal(Type.LONG_WORD, byteArray);
        bd = bd.divide(getKP(), 3, BigDecimal.ROUND_HALF_UP);
        bd = bd.multiply(getCtRatio());
        bd = bd.multiply(getPtRatio());System.out.println();
        return bd;
    }
    
    private Date round( Date date ) throws UnsupportedException, IOException {
        long msRest = date.getTime() % (getProfileInterval() * 1000);
        return new Date(date.getTime() - msRest);
    }
    
    private int getPointer() throws UnsupportedException, IOException {
        if( pointer == -1 ) {
            pointer = readValue(0x03FC, Type.WORD).intValue();
        }
        pointer = ( ( pointer - 1 )* 4 );
        return pointer;
    }
    
    public int getProfileInterval() throws UnsupportedException, IOException {
        if( profileInterval == -1 ) {
            profileInterval = readValue( 0x1B86, Type.WORD).intValue();
        }
        return profileInterval;
    }
    
    public Date getTime() throws IOException {
        return (Date)getRecFactory().toDate( readRawValue(0x0000, 4) );
    }
    
    private List newChannelInfo( ){
        ArrayList result = new ArrayList();
        result.add( new ChannelInfo(0, "0.1.128.0.0.255", kWh) );		//Active Power Stream
        return result;
    }
    
    public RegisterFactoryCdtPr getRecFactory( ) {
        return (RegisterFactoryCdtPr)getRegisterFactory();
    }

    public String getProtocolVersion() {
        return "$Date$";
    }

    public DiscoverResult discover(DiscoverTools discoverTools) {
        return null;
    }    
    
}
