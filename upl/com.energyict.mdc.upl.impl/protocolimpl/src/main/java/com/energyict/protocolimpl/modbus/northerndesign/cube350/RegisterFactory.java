/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.northerndesign.cube350;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.energyict.cbo.Unit;
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
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {
        // options
        setZeroBased(false); // this means that reg2read = reg-1

        /* scaling factor */
        getRegisters().add( new HoldingRegister(513,1,"escale").setParser("value0") ); 
        
        /* Amps scale */
        getRegisters().add( new HoldingRegister(2837,1,"ki").setParser("value0") );
        /* Phase volts scale */
        getRegisters().add( new HoldingRegister(2838,1,"kvp").setParser("value0") );
        /* Line volts scale */
        getRegisters().add( new HoldingRegister(2839,1,"kvi").setParser("value0") );
        /* Power scale */
        getRegisters().add( new HoldingRegister(2840,1,"kp").setParser("value0") );
        
        
        /* energy */
        getRegisters().add(new HoldingRegister(514,2,toObis("1.1.1.8.0.255"),Unit.get("kWh"))); 
        
        /* active power */
        getRegisters().add(new HoldingRegister(2816,1,toObis("1.1.1.7.0.255"),Unit.get("W"))); 
        /* reactive power */
        getRegisters().add(new HoldingRegister(2818,1,toObis("1.1.3.7.0.255"),Unit.get("var"))); 
        /* apparent power */
        getRegisters().add(new HoldingRegister(2817,1,toObis("1.1.9.7.0.255"),Unit.get("VA")));
        
        /* power factor */
        getRegisters().add(new HoldingRegister(2819,1,toObis("1.1.13.7.0.255"))); 
        
        
        /* power phase A */ 
        getRegisters().add(new HoldingRegister(2823,1,toObis("1.1.21.7.0.255"),Unit.get("W")));
        /* power phase B */ 
        getRegisters().add(new HoldingRegister(2826,1,toObis("1.1.41.7.0.255"),Unit.get("W")));
        /* power phase C */ 
        getRegisters().add(new HoldingRegister(2829,1,toObis("1.1.61.7.0.255"),Unit.get("W"))); 

        
        /* power factor phase A */
        getRegisters().add(new HoldingRegister(2830,1,toObis("1.1.33.7.0.255"))); 
        /* power factor phase B */
        getRegisters().add(new HoldingRegister(2831,1,toObis("1.1.53.7.0.255"))); 
        /* power factor phase C */
        getRegisters().add(new HoldingRegister(2832,1,toObis("1.1.73.7.0.255")));
        
        
        /* voltage, phase A-B */
        getRegisters().add(new HoldingRegister(2833,1,toObis("1.1.32.7.0.255"),Unit.get("V"))); 
        /* voltage, phase B-C */
        getRegisters().add(new HoldingRegister(2834,1,toObis("1.1.52.7.0.255"),Unit.get("V"))); 
        /* voltage, phase A-C */
        getRegisters().add(new HoldingRegister(2835,1,toObis("1.1.72.7.0.255"),Unit.get("V"))); 
        
        /* voltage, phase A-N */
        getRegisters().add(new HoldingRegister(2821,1,toObis("1.1.132.7.0.255"),Unit.get("V"))); 
        /* voltage, phase B-N */
        getRegisters().add(new HoldingRegister(2824,1,toObis("1.1.152.7.0.255"),Unit.get("V"))); 
        /* voltage, phase C-N */
        getRegisters().add(new HoldingRegister(2827,1,toObis("1.1.172.7.0.255"),Unit.get("V")));
        
        /* Current, phase A */
        getRegisters().add(new HoldingRegister(2822,1,toObis("1.1.31.7.0.255"),Unit.get("A"))); 
        /* Current, phase B */
        getRegisters().add(new HoldingRegister(2825,1,toObis("1.1.51.7.0.255"),Unit.get("A"))); 
        /* Current, phase C */
        getRegisters().add(new HoldingRegister(2828,1,toObis("1.1.71.7.0.255"),Unit.get("A"))); 
        
        
        getRegisters().add(new HoldingRegister(3592, 1, "firmwareVersion"));
        getRegisters().add(new HoldingRegister(3590, 1, "MeterModel"));
        
        /* V1 Harmonics registers */
        getRegisters().add(new HoldingRegister(7936, 1, toObis("1.1.32.7.2.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7937, 1, toObis("1.1.32.7.3.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7938, 1, toObis("1.1.32.7.4.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7939, 1, toObis("1.1.32.7.5.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7940, 1, toObis("1.1.32.7.6.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7941, 1, toObis("1.1.32.7.7.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7942, 1, toObis("1.1.32.7.8.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7943, 1, toObis("1.1.32.7.9.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7944, 1, toObis("1.1.32.7.10.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7945, 1, toObis("1.1.32.7.11.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7946, 1, toObis("1.1.32.7.12.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7947, 1, toObis("1.1.32.7.13.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7948, 1, toObis("1.1.32.7.14.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(7949, 1, toObis("1.1.32.7.15.255"), Unit.get("%")));

        /* V2 Harmonics registers */
        getRegisters().add(new HoldingRegister(8192, 1, toObis("1.1.52.7.2.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8193, 1, toObis("1.1.52.7.3.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8194, 1, toObis("1.1.52.7.4.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8195, 1, toObis("1.1.52.7.5.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8196, 1, toObis("1.1.52.7.6.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8197, 1, toObis("1.1.52.7.7.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8198, 1, toObis("1.1.52.7.8.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8199, 1, toObis("1.1.52.7.9.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8200, 1, toObis("1.1.52.7.10.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8201, 1, toObis("1.1.52.7.11.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8202, 1, toObis("1.1.52.7.12.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8203, 1, toObis("1.1.52.7.13.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8204, 1, toObis("1.1.52.7.14.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8205, 1, toObis("1.1.52.7.15.255"), Unit.get("%")));

        /* V3 Harmonics registers */
        getRegisters().add(new HoldingRegister(8448, 1, toObis("1.1.72.7.2.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8449, 1, toObis("1.1.72.7.3.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8450, 1, toObis("1.1.72.7.4.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8451, 1, toObis("1.1.72.7.5.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8452, 1, toObis("1.1.72.7.6.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8453, 1, toObis("1.1.72.7.7.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8454, 1, toObis("1.1.72.7.8.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8455, 1, toObis("1.1.72.7.9.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8456, 1, toObis("1.1.72.7.10.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8457, 1, toObis("1.1.72.7.11.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8458, 1, toObis("1.1.72.7.12.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8459, 1, toObis("1.1.72.7.13.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8460, 1, toObis("1.1.72.7.14.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8461, 1, toObis("1.1.72.7.15.255"), Unit.get("%")));

        /* I1 Harmonics registers */
        getRegisters().add(new HoldingRegister(8704, 1, toObis("1.1.31.7.2.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8705, 1, toObis("1.1.31.7.3.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8706, 1, toObis("1.1.31.7.4.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8707, 1, toObis("1.1.31.7.5.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8708, 1, toObis("1.1.31.7.6.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8709, 1, toObis("1.1.31.7.7.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8710, 1, toObis("1.1.31.7.8.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8711, 1, toObis("1.1.31.7.9.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8712, 1, toObis("1.1.31.7.10.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8713, 1, toObis("1.1.31.7.11.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8714, 1, toObis("1.1.31.7.12.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8715, 1, toObis("1.1.31.7.13.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8716, 1, toObis("1.1.31.7.14.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8717, 1, toObis("1.1.31.7.15.255"), Unit.get("%")));

        /* I2 Harmonics registers */
        getRegisters().add(new HoldingRegister(8960, 1, toObis("1.1.51.7.2.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8961, 1, toObis("1.1.51.7.3.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8962, 1, toObis("1.1.51.7.4.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8963, 1, toObis("1.1.51.7.5.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8964, 1, toObis("1.1.51.7.6.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8965, 1, toObis("1.1.51.7.7.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8966, 1, toObis("1.1.51.7.8.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8967, 1, toObis("1.1.51.7.9.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8968, 1, toObis("1.1.51.7.10.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8969, 1, toObis("1.1.51.7.11.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8970, 1, toObis("1.1.51.7.12.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8971, 1, toObis("1.1.51.7.13.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8972, 1, toObis("1.1.51.7.14.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(8973, 1, toObis("1.1.51.7.15.255"), Unit.get("%")));

        /* I3 Harmonics registers */
        getRegisters().add(new HoldingRegister(9216, 1, toObis("1.1.71.7.2.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9217, 1, toObis("1.1.71.7.3.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9218, 1, toObis("1.1.71.7.4.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9219, 1, toObis("1.1.71.7.5.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9220, 1, toObis("1.1.71.7.6.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9221, 1, toObis("1.1.71.7.7.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9222, 1, toObis("1.1.71.7.8.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9223, 1, toObis("1.1.71.7.9.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9224, 1, toObis("1.1.71.7.10.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9225, 1, toObis("1.1.71.7.11.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9226, 1, toObis("1.1.71.7.12.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9227, 1, toObis("1.1.71.7.13.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9228, 1, toObis("1.1.71.7.14.255"), Unit.get("%")));
        getRegisters().add(new HoldingRegister(9229, 1, toObis("1.1.71.7.15.255"), Unit.get("%")));

    }
    
    protected void initParsers() {
        // BigDecimal parser
        getParserFactory().addBigDecimalParser(new Parser() {
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
        });
        
        getParserFactory().addParser("value0", new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                return new BigDecimal( values[0] );
            }
        });
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

            // Harmonics
            scaleMap.put( toObis( "1.1.31.7.2.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.3.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.4.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.5.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.6.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.7.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.8.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.9.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.10.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.11.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.12.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.13.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.14.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.31.7.15.255" ),"1000=100%" );

            scaleMap.put( toObis( "1.1.32.7.2.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.3.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.4.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.5.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.6.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.7.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.8.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.9.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.10.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.11.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.12.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.13.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.14.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.32.7.15.255" ),"1000=100%" );

            scaleMap.put( toObis( "1.1.51.7.2.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.3.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.4.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.5.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.6.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.7.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.8.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.9.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.10.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.11.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.12.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.13.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.14.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.51.7.15.255" ),"1000=100%" );

            scaleMap.put( toObis( "1.1.52.7.2.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.3.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.4.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.5.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.6.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.7.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.8.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.9.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.10.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.11.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.12.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.13.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.14.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.52.7.15.255" ),"1000=100%" );

            scaleMap.put( toObis( "1.1.71.7.2.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.3.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.4.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.5.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.6.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.7.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.8.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.9.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.10.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.11.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.12.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.13.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.14.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.71.7.15.255" ),"1000=100%" );

            scaleMap.put( toObis( "1.1.72.7.2.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.3.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.4.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.5.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.6.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.7.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.8.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.9.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.10.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.11.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.12.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.13.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.14.255" ),"1000=100%" );
            scaleMap.put( toObis( "1.1.72.7.15.255" ),"1000=100%" );

            
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

        if( "1000=100%".equals(scaleName) )
            return -1;

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
    
} 
