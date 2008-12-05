/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.modbus.core.*;

/**
 *
 * @author Koen
 */
public class RegisterFactory extends AbstractRegisterFactory {
    
    private BigDecimal eScale;
    private BigDecimal ampsScale;
    private BigDecimal phaseVoltsScale;
    private BigDecimal lineVoltsScale;
    private BigDecimal powerScale;
    
    private Map scaleMap;
    
    public static final String TIME 	= "Time";
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {
        // options
        setZeroBased(false); // this means that reg2read = reg-1
        
        getRegisters().add(new HoldingRegister(3592, 1, TIME));
        //getRegisters().add(new HoldingRegister(3592, 1, "firmwareVersion"));
        //getRegisters().add(new HoldingRegister(3590, 1, "MeterModel"));
        
    }
    
    private int getScaleForObis( ObisCode obis ) throws IOException {
        
        if( scaleMap == null ) {
            
            scaleMap = new HashMap( );
            
            scaleMap.put( toObis( "1.1.1.8.0.255" ), "escale" ); 
            
            scaleMap.put( toObis( "1.1.1.7.0.255" ), "kp" );
            scaleMap.put( toObis( "1.1.3.7.0.255" ), "kp"  );
            scaleMap.put( toObis( "1.1.9.7.0.255" ), "kp"  );
            
            scaleMap.put( toObis( "1.1.13.7.0.255" ),"pf" );
            
            scaleMap.put( toObis( "1.1.21.7.0.255" ),"kp" );
            scaleMap.put( toObis( "1.1.41.7.0.255" ),"kp" );
            scaleMap.put( toObis( "1.1.61.7.0.255" ),"kp" );

            
            scaleMap.put( toObis( "1.1.33.7.0.255" ),"pf" );
            scaleMap.put( toObis( "1.1.53.7.0.255" ),"pf" );
            scaleMap.put( toObis( "1.1.73.7.0.255" ),"pf" );
            
            scaleMap.put( toObis( "1.1.32.7.0.255" ),"kvi" );
            scaleMap.put( toObis( "1.1.52.7.0.255" ),"kvi" );
            scaleMap.put( toObis( "1.1.72.7.0.255" ),"kvi" );
            
            scaleMap.put( toObis( "1.1.132.7.0.255" ),"kvp" );
            scaleMap.put( toObis( "1.1.152.7.0.255" ),"kvp" );
            scaleMap.put( toObis( "1.1.172.7.0.255" ),"kvp" );
            
            scaleMap.put( toObis( "1.1.31.7.0.255" ),"ki" );
            scaleMap.put( toObis( "1.1.51.7.0.255" ),"ki" );
            scaleMap.put( toObis( "1.1.71.7.0.255" ),"ki" );

        }
        
        String scaleName = (String) scaleMap.get(obis);

        if( "escale".equals( scaleName ) )
            return getEScale().intValue() - 6;
        
        if( "ki".equals(scaleName) )
            return getAmpsScale().intValue() - 3;
        
        if( "kvp".equals( scaleName ) )
            return getPhaseVoltsScale().intValue() - 3;
        
        if( "kvi".equals(scaleName) )
            return getLineVoltsScale().intValue() - 3;
        
        if( "kp".equals(scaleName) )
            return getPowerScale().intValue() - 3;
        
        if( "pf".equals(scaleName) )
            return -3;
        
        String msg = "scaleName " + scaleName + " is not supported";
        throw new RuntimeException( msg );

    }
    
    
    private BigDecimal getEScale( ) throws IOException {
        if( eScale == null ) {
            AbstractRegister r = findRegister("escale");
            eScale = (BigDecimal)r.objectValueWithParser( "value0" );
        }
        return eScale;
    }
    
    private BigDecimal getAmpsScale( ) throws IOException {
        if( ampsScale == null ) {
            AbstractRegister r = findRegister("ki");
            ampsScale = (BigDecimal)r.objectValueWithParser( "value0" );
        }
        return ampsScale;
    }
    
    private BigDecimal getPhaseVoltsScale( ) throws IOException {
        if( phaseVoltsScale == null ) {
            AbstractRegister r = findRegister("kvp");
            phaseVoltsScale = (BigDecimal)r.objectValueWithParser( "value0" );
        }
        return phaseVoltsScale;
    }
    
    private BigDecimal getLineVoltsScale( ) throws IOException {
        if( lineVoltsScale == null ) {
            AbstractRegister r = findRegister("kvi");
            lineVoltsScale = (BigDecimal)r.objectValueWithParser( "value0" );
        }
        return lineVoltsScale;
    }

    private BigDecimal getPowerScale( ) throws IOException {
        if( powerScale == null ) {
            AbstractRegister r = findRegister("kp");
            powerScale = (BigDecimal)r.objectValueWithParser( "value0" );
        }
        return powerScale;
    }

    
    private ObisCode toObis(String obis) {
        return ObisCode.fromString( obis );
    }
    
    //------------------------------------------------------------------------------------------------------------
    // Parser classes
    //------------------------------------------------------------------------------------------------------------
    
    protected void initParsers() {

    	getParserFactory().addBigDecimalParser(new BigDecimalParser());
        getParserFactory().addDateParser(new TimeParser());
        getParserFactory().addParser("value0", new Value0Parser());

    } 

    class BigDecimalParser implements Parser {
        public Object val(int[] values, AbstractRegister register) throws IOException {
            BigDecimal bd = null;
            if( values.length == 1 ) {
                bd = new BigDecimal( values[0] );
            } else {
                bd = new BigDecimal( (values[0]<<16)+values[1] );
            }
            
            bd = bd.movePointRight( getScaleForObis( register.getObisCode() ) );
            bd = bd.setScale(6, BigDecimal.ROUND_HALF_UP);
            return bd;
        }
    }
    
    class TimeParser implements Parser {
		public Object val(int[] values, AbstractRegister register) throws IOException {

			return new Date();
		}
    }
    
    class Value0Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            return new BigDecimal( values[0] );
        }
    }

} 

