package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ObisCodeExtensions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author fbo */

public class ObisCodeMapper {
    
    static final int d_md_1 = 6;
    static final int d_md_2 = ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK;
    static final int d_md_3 = ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK + 1;
    
    final private CewePrometer prometer;
    
    /** Collection for sorting the keys */
    private LinkedHashSet keys = new LinkedHashSet();
    /** HashMap with the ValueFactories per ObisCode  */
    private HashMap oMap = new HashMap();
    
    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper(CewePrometer cewePrometer) throws IOException {
        this.prometer = cewePrometer;
        init();
    }
    
    /** @return a RegisterInfo for the obiscode */
    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        AbstractValueFactory vFactory = (AbstractValueFactory)get( obisCode );
        if( vFactory == null )
            return new RegisterInfo( "not supported" );        
        return new RegisterInfo( vFactory.getDescription() );
    }
    
    /** @return a RegisterValue for the obiscode */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        AbstractValueFactory vFactory = (AbstractValueFactory)get( obisCode );
        if( vFactory == null )
            throw new NoSuchRegisterException(); 
        return vFactory.getRegisterValue(obisCode);
    }
    
    /** Retrieves objects from the ObisCodeMap */
    public AbstractValueFactory get( ObisCode o ) {
        return (AbstractValueFactory)oMap.get( new ObisCodeWrapper( o ) );
    }
    
    /** Add objects to the ObisCodeMap */
    private void putStd( String ocs, ProRegister[] rArray, int fieldIdx) {
        
        ObisCode oc = ObisCode.fromString(ocs);
        EnergyValueFactory f = new EnergyValueFactory(oc,rArray, fieldIdx);
        ObisCodeWrapper ocw = new ObisCodeWrapper(f.obisCode);

        putFactory((AbstractValueFactory)f, ocw);
        
    }

    private void putFactory(AbstractValueFactory f, ObisCodeWrapper ocw) {
        if( keys.contains(ocw) )
            throw new ApplicationException("obiscode already exists " + ocw);
            
        keys.add( ocw );
        oMap.put( ocw, f );
    }    
    
    private void putMD( String oc, int  mdId ) {
        
        ObisCode o = ObisCode.fromString(oc);
        
        MaximumDemandValueFactory f = new MaximumDemandValueFactory(o, mdId);
        
        f.obisCode = ObisCode.fromString(oc);
        ObisCodeWrapper ocw = new ObisCodeWrapper(f.obisCode);

        putFactory((AbstractValueFactory)f, ocw);
        
    }

    private void putTOU( String oc, int touPhenomenon ) {
        
        ObisCode o = ObisCode.fromString(oc);
        
        for( int e = 1; e < 9; e ++ ) {
            
            String f = "" + o.getF();
            if( o.getF() == 0 ) f = "VZ";
            if( o.getF() < 0  ) f = "VZ" + o.getF(); 

            String oString = ""; 
            oString += o.getA() + ".";
            oString += o.getB() + "."; 
            oString += o.getC() + ".";
            oString += o.getD() + ".";
            oString += e + ".";
            oString += f;
            
            ObisCode eo = ObisCode.fromString(oString);
            
            TOUValueFactory fct = new TOUValueFactory(eo, touPhenomenon);
            ObisCodeWrapper ocw = new ObisCodeWrapper(eo);
    
            putFactory((AbstractValueFactory)fct, ocw);
            
        }
        
    }
    
    /** Add objects to the ObisCodeMap */
    private void putTime( String oc ) {

        ObisCode o = ObisCode.fromString(oc);
        TimeValueFactory f = new TimeValueFactory(o);
        
        f.obisCode = ObisCode.fromString(oc);
        ObisCodeWrapper ocw = new ObisCodeWrapper(f.obisCode);

        putFactory((AbstractValueFactory)f, ocw);

    }
    
    /** @return construct extended logging */
    public String getExtendedLogging() throws IOException {
        StringBuffer result = new StringBuffer();
        List obisList = getMeterSupportedObisCodes();
        Iterator i = obisList.iterator();
        while( i.hasNext() ){
            ObisCode obc = (ObisCode) i.next();
            result.append( obc.toString() + " " + getRegisterInfo(obc) + "\n" );
            result.append( getRegisterValue(obc).toString() + "\n" );
        }
        return result.toString();
    }
        
    /** @return short desciption of ALL the possibly available obiscodes */
    public String toString( ){
        
        StringBuffer result = new StringBuffer();
        result.append( "All possibly supported ObisCodes \n" );
        Iterator i = keys.iterator();
        while( i.hasNext() ){
            ObisCodeWrapper key = (ObisCodeWrapper)i.next();
            try {
                result.append( key + " " + getRegisterInfo(key.obisCode) + "\n" );
            } catch (IOException e) {
                result.append( key + " exception for info " );
                e.printStackTrace();
            }
        }
        return result.toString();
    }
    
    /** This is the init for the actual values, this method does not
     * read any register configuration information, since that requires
     * communication. 
     * 
     * @throws IOException
     */
    private void init( ) throws IOException {
        
        String[] f = new String [] { 
            "255", "VZ", "VZ-1", "VZ-2", "VZ-3", "VZ-4", "VZ-5", "VZ-6", "VZ-7", 
            "VZ-8", "VZ-9", "VZ-10", "VZ-11", "VZ-12", "VZ-13"
        };
            
        for( int bpI = 0; bpI < f.length; bpI ++ ){
        
        /* Energy registers */
        putStd("1.1.1.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 0 );
        putStd("1.1.2.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 1 );
        putStd("1.1.5.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 2 );
        putStd("1.1.6.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 3 );
        putStd("1.1.7.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 4 );
        putStd("1.1.8.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 5 );
        putStd("1.1.9.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 6 );
        putStd("1.1.10.8.0."  + f[bpI], prometer.getRegisters().getrEenergy(), 7 );
        putStd("1.1.3.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 8 );
        putStd("1.1.4.8.0."   + f[bpI], prometer.getRegisters().getrEenergy(), 9 );
        putStd("1.1.128.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 10 );
        putStd("1.1.129.8.0." + f[bpI], prometer.getRegisters().getrEenergy(), 11 );
        
        /* External Registers */
        putStd("1.1.131.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 0 );
        putStd("1.1.132.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 1 );
        putStd("1.1.133.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 2 );
        putStd("1.1.134.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 3 );
        putStd("1.1.135.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 4 );
        putStd("1.1.136.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 5 );
        putStd("1.1.137.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 6 );
        putStd("1.1.138.8.0." + f[bpI], prometer.getRegisters().getrExternal(), 7 );
        
        putTOU("1.1.1.8.1." + f[bpI],   CewePrometer.TOU_ACTIVE_ENERGY_IMP);
        putTOU("1.1.2.8.1." + f[bpI],   CewePrometer.TOU_ACTIVE_ENERGY_EXP);
        putTOU("1.1.3.8.1." + f[bpI],   CewePrometer.TOU_REACTIVE_ENERGY_IMP);
        putTOU("1.1.4.8.1." + f[bpI],   CewePrometer.TOU_REACTIVE_ENERGY_EXP);
        putTOU("1.1.128.8.1." + f[bpI], CewePrometer.TOU_REACTIVE_ENERGY_IND);
        putTOU("1.1.129.8.1." + f[bpI], CewePrometer.TOU_REACTIVE_ENERGY_CAP);
        putTOU("1.1.5.8.1." + f[bpI],   CewePrometer.TOU_REACTIVE_ENERGY_QI);
        putTOU("1.1.6.8.1." + f[bpI],   CewePrometer.TOU_REACTIVE_ENERGY_QII);
        putTOU("1.1.7.8.1." + f[bpI],   CewePrometer.TOU_REACTIVE_ENERGY_QIII);
        putTOU("1.1.8.8.1." + f[bpI],   CewePrometer.TOU_REACTIVE_ENERGY_QIV);
        putTOU("1.1.9.8.1." + f[bpI],   CewePrometer.TOU_APPARENT_ENERGY_IMP);
        putTOU("1.1.10.8.1." + f[bpI],  CewePrometer.TOU_APPARENT_ENERGY_EXP);

        putTOU("1.1.131.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_1);
        putTOU("1.1.132.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_2);
        putTOU("1.1.133.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_3);
        putTOU("1.1.134.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_4);
        putTOU("1.1.135.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_5);
        putTOU("1.1.136.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_6);
        putTOU("1.1.137.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_7);
        putTOU("1.1.138.8.1." + f[bpI], CewePrometer.TOU_EXTERNAL_REG_8);
         
        
        /* Maximum demand */
        int mdIdx = CewePrometer.MD_ACTIVE_POWER_IMP;
        putMD( "1.1.1." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.1." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.1." + d_md_3 + ".0." + f[bpI], mdIdx);        

        mdIdx = CewePrometer.MD_ACTIVE_POWER_EXP;
        putMD( "1.1.2." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.2." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.2." + d_md_3 + ".0." + f[bpI], mdIdx);        

        mdIdx = CewePrometer.MD_REACTIVE_POWER_IMP;
        putMD( "1.1.3." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.3." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.3." + d_md_3 + ".0." + f[bpI], mdIdx);        

        mdIdx = CewePrometer.MD_REACTIVE_POWER_EXP;
        putMD( "1.1.4." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.4." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.4." + d_md_3 + ".0." + f[bpI], mdIdx);
        
        mdIdx = CewePrometer.MD_REACTIVE_POWER_IND;
        putMD( "1.1.128." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.128." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.128." + d_md_3 + ".0." + f[bpI], mdIdx);
        
        mdIdx = CewePrometer.MD_REACTIVE_POWER_CAP;
        putMD( "1.1.129." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.129." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.129." + d_md_3 + ".0." + f[bpI], mdIdx);

        mdIdx = CewePrometer.MD_REACTIVE_POWER_QI;
        putMD( "1.1.5." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.5." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.5." + d_md_3 + ".0." + f[bpI], mdIdx);        

        mdIdx = CewePrometer.MD_REACTIVE_POWER_QII;
        putMD( "1.1.6." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.6." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.6." + d_md_3 + ".0." + f[bpI], mdIdx);        

        mdIdx = CewePrometer.MD_REACTIVE_POWER_QIII;
        putMD( "1.1.7." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.7." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.7." + d_md_3 + ".0." + f[bpI], mdIdx);

        mdIdx = CewePrometer.MD_REACTIVE_POWER_QIV;
        putMD( "1.1.8." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.8." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.8." + d_md_3 + ".0." + f[bpI], mdIdx);

        mdIdx = CewePrometer.MD_APPARENT_POWER_IMP;
        putMD( "1.1.9." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.9." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.9." + d_md_3 + ".0." + f[bpI], mdIdx);

        mdIdx = CewePrometer.MD_APPARENT_POWER_EXP;
        putMD( "1.1.10." + d_md_1 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.10." + d_md_2 + ".0." + f[bpI], mdIdx);        
        putMD( "1.1.10." + d_md_3 + ".0." + f[bpI], mdIdx);
        
        /* create obiscodes for time register */
        putTime("1.1.0.1.0." + f[bpI]);
        
        }
        
        putStd("1.1.21.8.0.255", prometer.getRegisters().getrEenergy(), 12);
        putStd("1.1.41.8.0.255", prometer.getRegisters().getrEenergy(), 13);
        putStd("1.1.61.8.0.255", prometer.getRegisters().getrEenergy(), 14);
        putStd("1.1.22.8.0.255", prometer.getRegisters().getrEenergy(), 15);
        putStd("1.1.42.8.0.255", prometer.getRegisters().getrEenergy(), 16);
        putStd("1.1.62.8.0.255", prometer.getRegisters().getrEenergy(), 17);
        
        
    }
    
    /** @return list of all ObisCodes supported by the currently connected
     * meter.  By trial and error. */
    private List getMeterSupportedObisCodes( ) throws IOException {
        ArrayList validObisCodes = new ArrayList( );
        Iterator i = keys.iterator();
        while( i.hasNext() ){
            ObisCodeWrapper key = (ObisCodeWrapper)i.next();
            ObisCode oc = key.obisCode;
            // if no exception is thrown, the ObisCode is supported
            try {
                getRegisterValue( oc );
                validObisCodes.add( oc );
            } catch( NoSuchRegisterException nre ) {
                // if an exception is thrown, the ObisCode is not available.
                //nre.printStackTrace();
            }
        }
        return validObisCodes;
    }
    
    /** Shorthand notation for throwing NoSuchRegisterException
     * @throws NoSuchRegisterException  */
    private void throwException( ObisCode obisCode ) throws NoSuchRegisterException {
        String ob = obisCode != null ? obisCode.toString() : "unknown";
        String msg = "ObisCode " + ob +" is not supported!";
        throw new NoSuchRegisterException(msg);
    }
    
    /** the java version of a closure ( aka a nice function pointer ) */
    abstract class AbstractValueFactory {
        
        ObisCode obisCode;
        public AbstractValueFactory( ){ }
        Quantity getQuantity( ) throws IOException  { return null;       };
        // since the eventTime is always the same as the toTime ... shortcut 
        Date getEventTime( ) throws IOException     { return null; };
        Date getFromTime( ) throws IOException      { return null; };        
        Date getToTime( ) throws IOException        { return null; };
        ObisCode getObisCode( ) throws IOException  { return obisCode;   };
        
        RegisterValue getRegisterValue( ObisCode obisCode ) throws IOException  {
            Quantity q = getQuantity();
            if( q == null ) throwException( obisCode );
            Date e = getEventTime();
            Date f = getFromTime();
            Date t = getToTime();
            return new RegisterValue( obisCode, q, e, f, t );
        }
        
        abstract String getDescription();

        String getCDescription( ) {
            String d = "";
            switch(obisCode.getC()) {
                case 0:     d = "time";                     break;
                case 1:     d = "active energy imp.";       break;
                case 2:     d = "active energy exp.";       break;
                case 3:     d = "reactive energy imp.";     break;
                case 4:     d = "reactive energy exp.";     break;
                case 5:     d = "reactive energy QI";       break;
                case 6:     d = "reactive energy QII";      break;
                case 7:     d = "reactive energy QIII";     break;
                case 8:     d = "reactive energy QIV";      break;
                case 9:     d = "apparent energy imp.";     break;
                case 10:    d = "apparent energy exp";      break;
                case 128:   d = "reactive energy ind.";     break;
                case 129:   d = "reactive energy cap.";     break;
                case 21:    d = "active energy imp. L1";    break;
                case 41:    d = "active energy imp. L2";    break;
                case 61:    d = "active energy imp. L3";    break;
                case 22:    d = "active energy exp. L1";    break;
                case 42:    d = "active energy exp. L2";    break;
                case 62:    d = "active energy exp. L3";    break;
                case 131:   d = "External register 1";      break;
                case 132:   d = "External register 2";      break;
                case 133:   d = "External register 3";      break;
                case 134:   d = "External register 4";      break;
                case 135:   d = "External register 5";      break;
                case 136:   d = "External register 6";      break;
                case 137:   d = "External register 7";      break;
                case 138:   d = "External register 8";      break;
            }
            return d;
        }
        
        String getFDescription( ) {
            if( obisCode.getF() < 0 )
                return "VZ" + obisCode.getF();
            if( obisCode.getF() == 0 )
                return "VZ";
            return "";
        }

        Unit getUnit( ) {
            Unit u = null;
            switch(obisCode.getC()) {

                case 1:   u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 2:   u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 3:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 4:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 5:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 6:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 7:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 8:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 9:   u = Unit.get(BaseUnit.VOLTAMPEREHOUR);         break;
                case 10:  u = Unit.get(BaseUnit.VOLTAMPEREHOUR);         break;
                case 128: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 129: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR); break;
                case 21:  u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 41:  u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 61:  u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 22:  u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 42:  u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 62:  u = Unit.get(BaseUnit.WATTHOUR);               break;
                case 131: u = Unit.getUndefined();                       break;
                case 132: u = Unit.getUndefined();                       break;
                case 133: u = Unit.getUndefined();                       break;
                case 134: u = Unit.getUndefined();                       break;
                case 135: u = Unit.getUndefined();                       break;
                case 136: u = Unit.getUndefined();                       break;
                case 137: u = Unit.getUndefined();                       break;
                case 138: u = Unit.getUndefined();                       break;
            }
            
            return u;
        }
        
        int bp( ){
            return obisCode.getF();
        }
        
        public String toString(){
            return obisCode.toString();
        }
        
    }
    
    class EnergyValueFactory extends AbstractValueFactory {
        
        ProRegister [] registerArray;
        int fieldIdx;
        
        public EnergyValueFactory(ObisCode obisCode, ProRegister[] registerArray, int fieldIdx){
            this.obisCode = obisCode;
            this.registerArray = registerArray;
            this.fieldIdx = fieldIdx;
        }
        
        Quantity getQuantity( ) throws IOException  {
            int row = prometer.getRow(bp());
            if( row == -1 ) {
                String msg = "No historical data for billing point: " + bp();
                throw new NoSuchRegisterException( msg );
            }
            return new Quantity(registerArray[row].asDouble(fieldIdx), getUnit());
        }
        
        Date getToTime( ) throws IOException {
            if( bp() == 255 ) return null;
            int row = prometer.getRow(bp());
            return prometer.getRegisters().getrTimestamp()[row].asDate();
        }

        String getDescription() {
            return getCDescription() + ", " + getFDescription();
        }
        
    }

    class TOUValueFactory extends AbstractValueFactory {

        int touPhenomenon;
        
        public TOUValueFactory(ObisCode obisCode, int touPhenomenon){
            this.obisCode = obisCode;
            this.touPhenomenon = touPhenomenon;
        }
        
        Quantity getQuantity() throws IOException {
            
            int phenomenon = prometer.getTouIndex(touPhenomenon);
            if(phenomenon == -1) 
                throw new NoSuchRegisterException();
            
            int row = prometer.getRow(bp());
            if( row == -1 ) {
                String msg = "No historical data for billing point: " + bp();
                throw new NoSuchRegisterException( msg );
            }

            return new Quantity( prometer.getRegisters().getrTou()[row][phenomenon].asDouble(getRate()),getUnit());
        }

        Date getToTime( ) throws IOException {
            if( bp() == 255 ) return null;
            int row = prometer.getRow(bp());
            return prometer.getRegisters().getrTimestamp()[row].asDate();
        }

        int getRate( ){
            return obisCode.getE()-1;
        }
        
        /* meter rate is 0 based ... */
        String getEDescription( ){
            return "rate " + (getRate()+1);
        }
        
        String getDescription() {
            return getCDescription() + ", " + getEDescription() + ", " + getFDescription();
        }
        
    }
    
    /* Maximum demands 
     * 
     * (quantity,yympddhhmm,highest,yymmddhhmm,second,yymmddhhmm,third)
     *     0         1         2        3        4        5        6
     */
    class MaximumDemandValueFactory extends AbstractValueFactory {

        ProRegister maximum = null;
        int maximumPhenomenon;
        
        public MaximumDemandValueFactory(ObisCode obisCode, int maxPhenomenon){
            this.obisCode = obisCode;
            this.maximumPhenomenon = maxPhenomenon; 
        }
        
        ProRegister getMaximum( ) throws IOException{
            if( maximum == null ) {
                
                int row = prometer.getRow(bp());
                if( row == -1 ) {
                    String msg = "No historical data for billing point: " + bp();
                    throw new NoSuchRegisterException( msg );
                }
                
                for(int i = 0; i < prometer.getRegisters().getrMaximumDemand()[row].length; i++){
                    if( prometer.getRegisters().getrMaximumDemand()[row][i].asInt(0) == maximumPhenomenon )
                        maximum = prometer.getRegisters().getrMaximumDemand()[row][i];
                }
                
            }
            
            if( maximum == null )
                throw new NoSuchRegisterException( obisCode.toString() );
          
            return maximum;
        }
        
        Quantity getQuantity() throws IOException {
            return new Quantity(getMaximum().asDouble(getQuantityFieldIndex()),getUnit());
        }
        
        Date getEventTime( ) throws IOException {
            
            int idx = getDateFieldIndex();
            
            if( "7001010000".equals(getMaximum( ).asString(idx) ) )
                    throw new NoSuchRegisterException( obisCode.toString() );
            
            return getMaximum( ).asShortDate(idx);
            
        }
        
        Date getToTime( ) throws IOException {
            if( bp() == 255 ) return null;
            int row = prometer.getRow(bp());
            return prometer.getRegisters().getrTimestamp()[row].asDate();
        }
        
        int getRank() {
            switch( obisCode.getD() ) {
                case d_md_1: return 0;
                case d_md_2: return 1;
                case d_md_3: return 2;
            }
            throw new ApplicationException("No such Max demand");
        }
        
        int getDateFieldIndex(){
            switch( obisCode.getD() ) {
                case d_md_1: return 1;
                case d_md_2: return 3;
                case d_md_3: return 5;
            }
            throw new ApplicationException("No such Max demand");
        }
        
        int getQuantityFieldIndex(){
            switch( obisCode.getD() ) {
                case d_md_1: return 2;
                case d_md_2: return 4;
                case d_md_3: return 6;
            }
            throw new ApplicationException("No such Max demand"); 
        }
        
        String getDescription( ){
            String d = getCDescription();
            switch( obisCode.getD() ){
                case d_md_1: d = getCDescription() + ",";           break;
                case d_md_2: d = getCDescription() + ", second";    break;
                case d_md_3: d = getCDescription() + ", third";     break;    
            }
            return d + " highest, " + getFDescription();
        }
     
        Unit getUnit( ) {
            
            Unit u = null;
            switch(obisCode.getC()) {
                case 1:   u = Unit.get(BaseUnit.WATT);               break;
                case 2:   u = Unit.get(BaseUnit.WATT);               break;
                case 3:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 4:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 5:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 6:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 7:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 8:   u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 9:   u = Unit.get(BaseUnit.VOLTAMPERE);         break;
                case 10:  u = Unit.get(BaseUnit.VOLTAMPERE);         break;
                case 128: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 129: u = Unit.get(BaseUnit.VOLTAMPEREREACTIVE); break;
                case 21:  u = Unit.get(BaseUnit.WATT);               break;
                case 41:  u = Unit.get(BaseUnit.WATT);               break;
                case 61:  u = Unit.get(BaseUnit.WATT);               break;
                case 22:  u = Unit.get(BaseUnit.WATT);               break;
                case 42:  u = Unit.get(BaseUnit.WATT);               break;
                case 62:  u = Unit.get(BaseUnit.WATT);               break;
                case 131: u = Unit.getUndefined();                   break;
                case 132: u = Unit.getUndefined();                   break;
                case 133: u = Unit.getUndefined();                   break;
                case 134: u = Unit.getUndefined();                   break;
                case 135: u = Unit.getUndefined();                   break;
                case 136: u = Unit.getUndefined();                   break;
                case 137: u = Unit.getUndefined();                   break;
                case 138: u = Unit.getUndefined();                   break;
            }
            return u;
        }
        
    }
    
    class TimeValueFactory extends AbstractValueFactory {
        
        public TimeValueFactory(ObisCode obisCode){
            this.obisCode = obisCode;
        }
        
        Quantity getQuantity( ) throws IOException  {
            int row = prometer.getRow(bp());
            if( row == -1 ) {
                String msg = "No historical data for billing point: " + bp();
                throw new NoSuchRegisterException( msg );
            }
            Unit ms = Unit.get(BaseUnit.SECOND, -3);
            ProRegister r = prometer.getRegisters().getrTimestamp()[row].readAndFreeze();
            BigDecimal v = new BigDecimal( r.asDate().getTime() );
            return new Quantity(v,ms );
        }
        
        Date getToTime( ) throws IOException {
            if( bp() == 255 ) return null;
            int row = prometer.getRow(bp());
            return prometer.getRegisters().getrTimestamp()[row].asDate();
        }

        String getDescription() {
            return getCDescription() + ", " + getFDescription();
        }
        
    }
    
    /** The ObisCodeMapper works with a Map that links the available obis 
     * codes to ValueFactories that can retrieve data from the RegisterFactory.
     * 
     * The keys of the Map are actuall ObisCodes.  But the equal method of 
     * obis codes makes a distinction between relative period (VZ) and 
     * absolute periods.  This is not the behaviour that is needed here.
     * ObisCodeWrapper will provide the ObisCodes with an equals and hash
     * method that does not make a distinction between relative and absolute
     * periods.
     */
    static class ObisCodeWrapper implements Comparable  {
        
        private ObisCode obisCode;
        
        private String os;
        private String reversedOs;
        
        ObisCodeWrapper( ObisCode oc ){
            obisCode = oc;
            
            os = obisCode.getA() + "." + obisCode.getB() + "." + 
                 obisCode.getC() + "." + obisCode.getD() + "." + 
                 obisCode.getE() + "." + Math.abs( obisCode.getF() );
            
            reversedOs = new StringBuffer( os ).reverse().toString();
        }
        
        public boolean equals( Object o ){
            if(!(o instanceof ObisCodeWrapper))
                return false;
            
            ObisCodeWrapper other = (ObisCodeWrapper)o;
            return  os.equals( other.os );
        }
        
        public int hashCode( ){
            return os.hashCode();
        }
        
        public String toString(){
            return obisCode.toString();
        }
        
        public int compareTo(Object o) {
            ObisCodeWrapper other = (ObisCodeWrapper)o;
            return reversedOs.compareTo(other.reversedOs);
        }
    
    }
    
}
