/*
 * ObisCodeMapper.java
 *
 * Created on 21 December 2005, 14:22
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/** @author  fbo */

class ObisCodeMapper {

    static ObisCodeMapper instance = new ObisCodeMapper();
    Ion ion;

    /** Collection for sorting the keys */
    private ArrayList keys = new ArrayList();
    /** HashMap with the ValueFactories per ObisCode  */
    private HashMap oMap = new HashMap();

    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper() {
        init();
    }

    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper(Ion ion) throws IOException {
        this.ion = ion;
        init();
    }

    /** @return a RegisterInfo for the obiscode */
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ValueFactory vFactory = (ValueFactory)instance.get( obisCode );
        if( vFactory == null )
            throwException( obisCode );
        return new RegisterInfo( vFactory.toString() );

    }

    /** @return a RegisterValue for the obiscode */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        ValueFactory vFactory = (ValueFactory)get( obisCode );
        if( vFactory == null )
            throwException( obisCode );
        return vFactory.getRegisterValue(obisCode);
    }

    /** Retrieves objects from the ObisCodeMap */
    public ValueFactory get( ObisCode o ) {
        return (ValueFactory)oMap.get( new ObisCodeWrapper( o ) );
    }

    /** Add objects to the ObisCodeMap */
    public void put( ObisCode o, ValueFactory f ) {
        ObisCodeWrapper ocw = new ObisCodeWrapper(o);
        keys.add( ocw );
        oMap.put( ocw, f );
    }

    /** Add objects to the ObisCodeMap */
    public void put( ObisCode o, IonHandle ionHandle, String description ) {
        ObisCodeWrapper ocw = new ObisCodeWrapper(o);
        keys.add( ocw );
        oMap.put( ocw, new ValueFactory( o, ionHandle, description ) );
    }

    /** @return construct extended logging */
    public String getExtendedLogging( ) throws IOException {
        StringBuffer result = new StringBuffer();
        List obisList = getMeterSupportedObisCodes();
        Iterator i = obisList.iterator();
        while( i.hasNext() ){
            ObisCode obc = (ObisCode) i.next();
            result.append( obc.toString() + " " + getRegisterInfo(obc) + "\n" );
        }
        return result.toString();
    }

    /** @return get Values for all available obiscodes */
    public String getDebugLogging( ) throws IOException {
        StringBuffer result = new StringBuffer();
        Iterator i = getMeterSupportedObisCodes().iterator();
        while( i.hasNext() ) {
            ObisCode o = (ObisCode)i.next();
            ValueFactory vf = (ValueFactory)oMap.get(new ObisCodeWrapper(o));
            result.append( o + " " + vf + "\n" );
            result.append( getRegisterValue( o) + "\n" );
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
            ValueFactory vf = (ValueFactory)oMap.get(key);
            result.append( key + " " + vf.toString() + "\n" );
        }
        return result.toString();
    }

    /** This is the init for the actual values, this method does not
     * read any register configuration information, since that requires
     * communication.
     *
     * @throws IOException
     */
    private void init( ) {

        int c = 1;
        for (Iterator iter = IonHandle.getMax().iterator(); iter.hasNext(); c++) {
            IonHandle handle = (IonHandle) iter.next();
            String d = "Maximum demand module #" + c;
            put( ObisCode.fromString("255.0." + c + ".6.0.255"), handle, d );
        }

        c = 1;
        for (Iterator iter = IonHandle.getMin().iterator(); iter.hasNext(); c++) {
            IonHandle handle = (IonHandle) iter.next();
            String d = "Minium demand module #" + c;
            put( ObisCode.fromString("255.0." + c + ".3.0.255"), handle, d );
        }

        c = 1;
        for (Iterator iter = IonHandle.getInt().iterator(); iter.hasNext(); c++) {
            IonHandle handle = (IonHandle) iter.next();
            String d = "Integrator module #" + c;
            put( ObisCode.fromString("255.0." + c + ".8.0.255"), handle, d );
        }

    }

    /** @return list of all ObisCodes supported by the currently connected
     * meter.  Does this by trial and error. */
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
    static private void throwException( ObisCode obisCode ) throws NoSuchRegisterException {
        String ob = obisCode != null ? obisCode.toString() : "unknown";
        String msg = "ObisCode " + ob +" is not supported!";
        throw new NoSuchRegisterException(msg);
    }

    /** the java version of a closure ( aka a nice function pointer ) */
    class ValueFactory {
        ObisCode obisCode = null;
        IonHandle ionHandle = null;
        String description = null;

        public ValueFactory( ObisCode o, IonHandle handle, String description ){
            this.obisCode = o;
            this.ionHandle = handle;
            this.description = description;
        }

        void setDescription( String aDescription ) {
            this.description = aDescription;
        }

        ObisCode getObisCode( ) throws IOException  { return obisCode;   };

        RegisterValue getRegisterValue( ObisCode obisCode ) throws IOException {

            // event time is only used for energy registers, not for max
            // demand

            boolean isEnergy = IonHandle.getInt().contains( ionHandle );
            Date e = null;
            Date f = null;
            Date t = null;

            Command cValue = new Command( ionHandle, IonMethod.READ_REGISTER_VALUE );
            Command cTime = new Command( ionHandle, IonMethod.READ_REGISTER_TIME );

            ArrayList list = new ArrayList( );
            list.add( cValue );

            if( !isEnergy )
                list.add( cTime );

            ion.getApplicationLayer().read( list );

            Number number = (Number)((IonObject)cValue.getResponse()).getValue();
            if( number == null )
                throwException( obisCode );

            Quantity q = new Quantity( number, Unit.getUndefined() );

            if( !isEnergy )
                e = (Date)((IonObject)cTime.getResponse()).getValue();

            return new RegisterValue( obisCode, q, e, f, t );

        }

        public String toString(){
            if( description == null )
                return obisCode.getDescription();
            else
                return description;
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
            return "ObisCode: "  + obisCode;
        }

        public int compareTo(Object o) {
            ObisCodeWrapper other = (ObisCodeWrapper)o;
            return reversedOs.compareTo(other.reversedOs);
        }

    }

}