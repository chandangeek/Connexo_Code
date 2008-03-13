package com.energyict.protocolimpl.iec1107.ppm;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ppm.register.HistoricalData;
import com.energyict.protocolimpl.iec1107.ppm.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppm.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppm.register.RegisterInformation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <pre>
 * ObisCodes
 *
 * Value group A - Electricity related objects
 * Value group B - Channel number (1-255)
 *
 * Value group C - Abstract of physical data items related to information
 * source
 * == 1  Active Power+
 * == 2  Active Power-
 * == 3  Reactive Power+
 * == 4  Reactive Power-
 * == 9  Apparent Power
 *
 * Value group D, Defines types
 * == 2  cumulative maximum
 * == 6  maximum 1 MD
 * == 8  time integral 1 TOTAL and RATE
 *
 * Value group E
 * Defines the Rate register.
 * == 0, no Rate register (TOU) so just the TOTAL
 * == 1-8, Rate register 1-8
 *
 * Value group F
 * - defines storage of Data
 * - value 0-255, (255 if not used)
 * - specifies the allocation of billing periods (historical values)
 * == 255 Not used
 * == 0 becomes 1
 * == 1 becomes 2
 * == ...
 *
 *
 * Power in AC circuits:
 *
 * - Apparent power
 * The product of voltage and current.  Expressed in Volt-Amps (VA).  Apparent
 * power is the easist to measure and is the vector sum of real power and
 * reactive power.
 * - Real power
 * The time avergage of the instantaneous product of voltage and current.
 * Expressed in Watts (W).  Real power can only be consumed in the resistive
 * part of the load, where the current is in phase with the voltage.
 * - Reactive power
 * The time avergage of the instantaneous product of the voltage and current,
 * with current phasshifted by 90°.  Expressed in Volt-Amps reactive (VAr).
 * Reactive power, while still drawing a current from the supply, is not
 * actually consuming any power at all - one way of describing reactive power
 * is to use the term  &quot;wattless watts&quot; since it can draw 1A of
 * current at 1V but not produce 1W of heat.
 *
 * </pre>
 *
 * 16/03/2005 added optimisation: if obiscode x.x.x.x.x.VZ is used, then use
 * register 541 (last billing) instead of 540.
 *
 * @author fbo
 */

public class ObisCodeMapper {
    
    private RegisterFactory rFactory;
    RegisterInformation ri = new RegisterInformation();
    
    /**
     * Dangerous stuff (using instance variables with a static method:
     * getRegisterInfo). Make sure only 2 methods are public!
     */
    
    private Date billingDate = null;
    private MetaRegister sourceRegister = null;
    private MetaRegister derivedRegister = null;
    // private int registerIndex; // KV 22072005 unused
    private int billingPoint;
    
    /** Manufacturer specific codes */
    static public final int CODE_E_REGISTER_1=128;
    static public final int CODE_E_REGISTER_2=129;
    static public final int CODE_E_REGISTER_3=130;
    static public final int CODE_E_REGISTER_4=131;
    
    public ObisCodeMapper(RegisterInformation ri) throws IOException {
        this.ri = ri;
    }
    
    public ObisCodeMapper(RegisterFactory registerFactory) throws IOException {
        this.rFactory = registerFactory;
        this.ri = registerFactory.getRegisterInformation();
    }
    
    private ObisCodeMapper(ObisCode obisCode) throws NoSuchRegisterException {
        this.getMapRegister(obisCode);
        this.getBillingPoint(obisCode);
    }
    
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(obisCode);
        String desc = ocm.toString( obisCode );
        return new RegisterInfo(desc);
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        
        this.getBillingPoint(obisCode);
        
        if( isBillingDate(obisCode ) ) {
            HistoricalData hd = null;
            if( billingPoint == 0 ) {
                hd = rFactory.getLastBilling();
            } else {
                hd = rFactory.getHistoricalData().get(billingPoint);
            }
            Date date = hd.getDate();
            Unit secondsUnit = Unit.get( BaseUnit.SECOND );
            Long sl = new Long( date.getTime() / 1000 );
            Quantity seconds = new Quantity( sl, secondsUnit );
            return new RegisterValue( obisCode, seconds,  date );
        }
        
        this.getMapRegister(obisCode);
        String key = getRegisterFactoryKey(obisCode);
        
        if (key == null)
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
            + " is not supported!");
        
        Object o = null;
        Date toDate = null;
        Date eventDate = null;
        
        if (rFactory != null) {
            
            if( obisCode.getD() == 2 )
                return getCMD(obisCode);
            if( obisCode.getD() == 6 )
                return getMD(obisCode);
            
            if (billingPoint == -1) {
                o = rFactory.getRegister(key);
                toDate = new Date();
            } else {
                HistoricalData hd = null;
                
                if( billingPoint == 0 ) {
                    hd = rFactory.getLastBilling();
                } else {
                    hd = rFactory.getHistoricalData().get(billingPoint);
                }
                
                if( hd == null )
                    return null;
                
                o = hd.get(key);
                toDate = hd.getDate();
                eventDate = hd.getDate();

            }
            
        }
        
        if (o instanceof MainRegister)
            return ((MainRegister) o).toRegisterValue(obisCode, eventDate, toDate);
        if (o instanceof MaximumDemand) {
            return ((MaximumDemand) o).toRegisterValue(obisCode, toDate);
        }
        
        return new RegisterValue(obisCode);
        
    }
    
    /* Return the RegisterFactoryKey that identifies the register.  If it
     * concerns a simple (cumulative|primary) register, just return
     * that RegisterFactoryKey, if it is a derived register, return that key.
     */
    private String getRegisterFactoryKey( ObisCode obisCode ) {
        if (derivedRegister == null) {
            return sourceRegister.registerFactoryKey;
        } else {
            
            if (derivedRegister.sourceRegister != null) {
                
                if (ri.isCMDRegister(derivedRegister) && derivedRegister.sourceRegister.equals(sourceRegister)) {
                    return derivedRegister.registerFactoryKey;
                }
                
                if (ri.isMDRegister(derivedRegister) && derivedRegister.sourceRegister.equals(sourceRegister))
                    return derivedRegister.registerFactoryKey;
                
                if (ri.isTouRegister(derivedRegister) && derivedRegister.sourceRegister.equals(sourceRegister))
                    return derivedRegister.registerFactoryKey;
            }
            
        }
        
        return null;
    }
    
    /* This is a big chunk of mapping/translation */
    
    /* In the case of energy: map the obiscode to the MetaRegister */
    private HashMap energyMap = new HashMap() {
        {
            put(new Integer(ObisCode.CODE_C_ACTIVE_IMPORT), ri.importWh);
            put(new Integer(ObisCode.CODE_C_ACTIVE_EXPORT), ri.exportWh);
            put(new Integer(ObisCode.CODE_C_REACTIVE_IMPORT), ri.importVarh);
            put(new Integer(ObisCode.CODE_C_REACTIVE_EXPORT), ri.exportVarh);
            put(new Integer(ObisCode.CODE_C_APPARENT), ri.vAh);
        }
    };
    
    private MetaRegister getEnergy(ObisCode o) {
        return (MetaRegister) energyMap.get(new Integer(o.getC()));
    }
    
    /* In the case of power: map the obiscode to the MetaRegister */
    private HashMap powerMap = new HashMap() {
        {
            put(new Integer(ObisCode.CODE_C_ACTIVE_IMPORT), ri.importW);
            put(new Integer(ObisCode.CODE_C_ACTIVE_EXPORT), ri.exportW);
            put(new Integer(ObisCode.CODE_C_REACTIVE_IMPORT), ri.importVar);
            put(new Integer(ObisCode.CODE_C_REACTIVE_EXPORT), ri.exportVar);
            put(new Integer(ObisCode.CODE_C_APPARENT), ri.vA);
        }
    };
    
    private MetaRegister getPower(ObisCode o) {
        return (MetaRegister) powerMap.get(new Integer(o.getC()));
    }
    
    /* sourceRegister and derivedRegister are mapped */
    private void getMapRegister(ObisCode obisCode)
    throws NoSuchRegisterException {
        
        if( isBillingDate( obisCode ) ) return ;
        
        //int e = registerIndex = obisCode.getE(); // KV 22072005 unused
        int e = obisCode.getE();
        
        // Step 3 :: What does code D say ?
        switch (obisCode.getD()) {
            
            case ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND : // C_MX_DMD (o=2)
                sourceRegister = getPower(obisCode);
                if( e == 0 ) {
                    derivedRegister = ri.findCMDRegisterFor(sourceRegister);
                } else if ( e == CODE_E_REGISTER_1 ) {
                    derivedRegister = ri.cmdRegister[ 0 ];
                } else if ( e == CODE_E_REGISTER_2 ) {
                    derivedRegister = ri.cmdRegister[ 1 ];
                } else if ( e == CODE_E_REGISTER_3 ) {
                    derivedRegister = ri.cmdRegister[ 2 ];
                } else if ( e == CODE_E_REGISTER_4 ) {
                    derivedRegister = ri.cmdRegister[ 3 ];
                }
                
                //derivedRegister = (e == 0 | e > ri.cmdRegister.length ) ? null : ri.cmdRegister[e - 1];
                break;
                
            case ObisCode.CODE_D_MAXIMUM_DEMAND : // MX_DMD (o=6)
                sourceRegister = getPower(obisCode);
                if( e == 0 ) {
                    derivedRegister = ri.findMDRegisterFor(sourceRegister);
                } else if ( e == CODE_E_REGISTER_1 ) {
                    derivedRegister = ri.mdRegister[ 0 ];
                } else if ( e == CODE_E_REGISTER_2 ) {
                    derivedRegister = ri.mdRegister[ 1 ];
                } else if ( e == CODE_E_REGISTER_3 ) {
                    derivedRegister = ri.mdRegister[ 2 ];
                } else if ( e == CODE_E_REGISTER_4 ) {
                    derivedRegister = ri.mdRegister[ 3 ];
                }
                
                //derivedRegister = (e == 0 | e > ri.mdRegister.length ) ? null : ri.mdRegister[e - 1];
                break;
                
            case ObisCode.CODE_D_TIME_INTEGRAL : // TOU (o=8)
                sourceRegister = getEnergy(obisCode);
                derivedRegister = (e == 0 | e > ri.touRegister.length) ? null : ri.touRegister[e - 1];
                break;
                
                
            default :
                throw new NoSuchRegisterException("ObisCode "
                + obisCode.toString() + " is not supported!");
        }
        
    }
    
    void getBillingPoint(ObisCode obisCode) throws NoSuchRegisterException {
        
        if (obisCode.getF() == 255)
            billingPoint = -1;
        else if (obisCode.getF() >= 0 && obisCode.getF() <= 3)
            billingPoint = obisCode.getF();
        else if (obisCode.getF() < 0 && obisCode.getF() >= -3)
            billingPoint = obisCode.getF() * -1;
        else
            throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
            + " is not supported!");
    }
    
    boolean isBillingDate( ObisCode obisCode ){
        return ( obisCode.getC() == 0 ) && ( obisCode.getE() == 2 );
    }
    
    /*
     * For making a nice descriptive toString(), the MetaRegisters are mapped
     * onto a special string, that makes sense in an Obiscode-context.
     */
    public String toString( ObisCode obisCode ) {
        String result = "";
        
        if ( billingDate != null )
            result += "billing date ";
        
        if (sourceRegister != null)
            result += metaRegToString.get(sourceRegister).toString();
        
        if (derivedRegister != null) {
            result += ", " + metaRegToString.get(derivedRegister).toString();
        } else {
            if( obisCode.getE() == 0 && obisCode.getD() == 2 )
                result += ", " + metaRegToString.get(ri.cmdTou1).toString();
            if( obisCode.getE() == 0 && obisCode.getD() == 6 )
                result += ", " + metaRegToString.get(ri.mdTou1).toString();
        }
        
        if (billingPoint != -1)
            result += ", billingpoint " + (billingPoint);
        else
            result += ", current value";
        
        return result;
    }
    
    /* String representation of the registers */
    HashMap metaRegToString = new HashMap() {
        {
            put(ri.importWh, "Energy, Active import");
            put(ri.exportWh, "Energy, Active export");
            put(ri.importVarh, "Energy, Reactive import");
            put(ri.exportVarh, "Energy, Reactive export");
            put(ri.vAh, "Energy, Apparent import");
            
            put(ri.importW, "Power, Active import");
            put(ri.exportW, "Power, Active export");
            put(ri.importVar, "Power, Reactive import");
            put(ri.exportVar, "Power, Reactive export");
            put(ri.vA, "Power, Apparent import");
            
            put(ri.tou1, "Tarif reg. 1");
            put(ri.tou2, "Tarif reg. 2");
            put(ri.tou3, "Tarif reg. 3");
            put(ri.tou4, "Tarif reg. 4");
            put(ri.tou5, "Tarif reg. 5");
            put(ri.tou6, "Tarif reg. 6");
            put(ri.tou7, "Tarif reg. 7");
            put(ri.tou8, "Tarif reg. 8");
            
            put(ri.mdTou1, "Maximum Demand");
            put(ri.mdTou2, "Maximum Demand");
            put(ri.mdTou3, "Maximum Demand");
            put(ri.mdTou4, "Maximum Demand");
            
            put(ri.cmdTou1, "Cumulative Maximum Demand");
            put(ri.cmdTou2, "Cumulative Maximum Demand");
            put(ri.cmdTou3, "Cumulative Maximum Demand");
            put(ri.cmdTou4, "Cumulative Maximum Demand");
            
        }
    };
    
    /** Getting Maximum Demands.
     *
     */
    private RegisterValue getMD( ObisCode obisCode ) throws IOException {
        
        if( obisCode.getE() == 0 ) {
            
            List l = ri.findAllMDRegistersFor( sourceRegister );
            
            if( billingPoint == -1 ) {
                
                Date d = new Date();
                ArrayList r = new ArrayList();
                
                if( l.contains( ri.mdTou1 )  )
                    r.add( rFactory.getMaximumDemand1().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou2 ) )
                    r.add( rFactory.getMaximumDemand2().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou3 ) )
                    r.add( rFactory.getMaximumDemand3().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou4 ) )
                    r.add( rFactory.getMaximumDemand4().toRegisterValue( obisCode, d ) );
                
                return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );
                
                
            } else if( billingPoint == 0 ) {
                
                HistoricalData hd = rFactory.getLastBilling();
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                ArrayList r = new ArrayList();
                
                if( l.contains( ri.mdTou1 )  )
                    r.add( hd.getMaxDemand1().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou2 ) )
                    r.add( hd.getMaxDemand2().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou3 ) )
                    r.add( hd.getMaxDemand3().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou4 ) )
                    r.add( hd.getMaxDemand4().toRegisterValue( obisCode, d ) );
                
                return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );
                
                
            } else if(billingPoint > 0 && billingPoint < 4) {
                
                HistoricalData hd = rFactory.getHistoricalData().get(billingPoint);
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                ArrayList r = new ArrayList();
                
                if( l.contains( ri.mdTou1 )  )
                    r.add( hd.getMaxDemand1().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou2 ) )
                    r.add( hd.getMaxDemand2().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou3 ) )
                    r.add( hd.getMaxDemand3().toRegisterValue( obisCode, d ) );
                if( l.contains( ri.mdTou4 ) )
                    r.add( hd.getMaxDemand4().toRegisterValue( obisCode, d ) );
                
                return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );
                
                
            }

            
        } else {
            
            if( billingPoint == -1 ) {
                
                Date d = new Date();
                //ArrayList r = new ArrayList(); // KV 22072005
                
                if( obisCode.getE() == CODE_E_REGISTER_1 )
                    return rFactory.getMaximumDemand1().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_2 )
                    return rFactory.getMaximumDemand2().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_3 )
                    return rFactory.getMaximumDemand3().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_4 )
                    return rFactory.getMaximumDemand4().toRegisterValue( obisCode, d );
                
            } else if( billingPoint == 0 ) {
                
                HistoricalData hd = rFactory.getLastBilling();
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                // ArrayList r = new ArrayList(); // KV 22072005
                
                if( obisCode.getE() == CODE_E_REGISTER_1 )
                    return hd.getMaxDemand1().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_2 )
                    return hd.getMaxDemand2().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_3 )
                    return hd.getMaxDemand3().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_4 )
                    return hd.getMaxDemand4().toRegisterValue( obisCode, d );
                
            } else if(billingPoint > 0 && billingPoint < 4) {
                
                HistoricalData hd = rFactory.getHistoricalData().get(billingPoint);
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                // ArrayList r = new ArrayList() // KV 22072005
                
                if( obisCode.getE() == CODE_E_REGISTER_1 )
                    return hd.getMaxDemand1().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_2 )
                    return hd.getMaxDemand2().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_3 )
                    return hd.getMaxDemand3().toRegisterValue( obisCode, d );
                if( obisCode.getE() == CODE_E_REGISTER_4 )
                    return hd.getMaxDemand4().toRegisterValue( obisCode, d );
                
            }
            
        }
        
        throw new
        NoSuchRegisterException( "ObisCode " + obisCode.toString()
        + " is not supported!");
        
    }
    
    
    private RegisterValue getCMD( ObisCode obisCode ) throws IOException {
        
        if( obisCode.getE() == 0 ) {
            
            List l = ri.findAllCMDRegistersFor( sourceRegister );
            
            if( billingPoint == -1 ) {
                
                Date d = new Date();
                ArrayList r = new ArrayList();
                
                if( l.contains( ri.cmdTou1 )  )
                    r.add( rFactory.getCumulativeMaximumDemand1().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou2 ) )
                    r.add( rFactory.getCumulativeMaximumDemand2().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou3 ) )
                    r.add( rFactory.getCumulativeMaximumDemand3().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou4 ) )
                    r.add( rFactory.getCumulativeMaximumDemand4().toRegisterValue( obisCode, d, d ) );
                
                return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );
                
                
            } else if( billingPoint == 0 ) {
                
                HistoricalData hd = rFactory.getLastBilling();
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                ArrayList r = new ArrayList();
                
                if( l.contains( ri.cmdTou1 )  )
                    r.add( hd.getCumulativeMaxDemand1().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou2 ) )
                    r.add( hd.getCumulativeMaxDemand2().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou3 ) )
                    r.add( hd.getCumulativeMaxDemand3().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou4 ) )
                    r.add( hd.getCumulativeMaxDemand4().toRegisterValue( obisCode, d, d ) );
                
                return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );
                
                
            } else if(billingPoint > 0 && billingPoint < 4) {
                
                HistoricalData hd = rFactory.getHistoricalData().get(billingPoint);
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                ArrayList r = new ArrayList();
                
                if( l.contains( ri.cmdTou1 )  )
                    r.add( hd.getCumulativeMaxDemand1().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou2 ) )
                    r.add( hd.getCumulativeMaxDemand2().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou3 ) )
                    r.add( hd.getCumulativeMaxDemand3().toRegisterValue( obisCode, d, d ) );
                if( l.contains( ri.cmdTou4 ) )
                    r.add( hd.getCumulativeMaxDemand4().toRegisterValue( obisCode, d, d ) );
                
                return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );
                   
            }
            
        } else {
            
            if( billingPoint == -1 ) {
                
                Date d = new Date();
                //ArrayList r = new ArrayList(); // KV 22072005
                
                if( obisCode.getE() == CODE_E_REGISTER_1 )
                    return rFactory.getCumulativeMaximumDemand1().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_2 )
                    return rFactory.getCumulativeMaximumDemand2().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_3 )
                    return rFactory.getCumulativeMaximumDemand3().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_4 )
                    return rFactory.getCumulativeMaximumDemand4().toRegisterValue( obisCode, d, d );
                
            } else if( billingPoint == 0 ) {
                
                HistoricalData hd = rFactory.getLastBilling();
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                // ArrayList r = new ArrayList(); // KV 22072005
                
                if( obisCode.getE() == CODE_E_REGISTER_1 )
                    return hd.getCumulativeMaxDemand1().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_2 )
                    return hd.getCumulativeMaxDemand2().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_3 )
                    return hd.getCumulativeMaxDemand3().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_4 )
                    return hd.getCumulativeMaxDemand4().toRegisterValue( obisCode, d, d );
                
            } else if(billingPoint > 0 && billingPoint < 4) {
                
                HistoricalData hd = rFactory.getHistoricalData().get(billingPoint);
                
                if( hd == null ) return null;
                
                Date d = hd.getDate();
                // ArrayList r = new ArrayList(); // KV 22072005
                
                if( obisCode.getE() == CODE_E_REGISTER_1 )
                    return hd.getCumulativeMaxDemand1().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_2 )
                    return hd.getCumulativeMaxDemand2().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_3 )
                    return hd.getCumulativeMaxDemand3().toRegisterValue( obisCode, d, d );
                if( obisCode.getE() == CODE_E_REGISTER_4 )
                    return hd.getCumulativeMaxDemand4().toRegisterValue( obisCode, d, d );
                
            }
            
        }
        
        throw new
        NoSuchRegisterException( "ObisCode " + obisCode.toString()
        + " is not supported!");
        
    }
    
    /* Return the biggest RegisterValue out of an array */
    private RegisterValue getMax( RegisterValue[] values ){
        RegisterValue max = values[0];
        for( int i = 1; i < values.length; i ++ )
            if( values[i].getQuantity().compareTo( max.getQuantity() ) > 0 )
                max = values[i];
        return max;
    }

    public Date getBillingDate() {
        return billingDate;
    }

}
