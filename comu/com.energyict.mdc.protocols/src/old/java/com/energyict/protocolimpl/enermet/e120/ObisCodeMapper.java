package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

public class ObisCodeMapper {

    private E120 e120;

    /** Collection for sorting the keys */
    private LinkedHashSet keys = new LinkedHashSet();
    /** HashMap with the ValueFactories per ObisCode  */
    private HashMap oMap = new HashMap();

    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper(E120 e120) throws IOException {
        this.e120 = e120;
        init();
    }

    /** @return a RegisterInfo for the obiscode */
    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo( obisCode.getDescription() );
    }

    /** @return a RegisterValue for the obiscode */
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        ValueFactory vFactory = (ValueFactory)get( obisCode );
        if( vFactory == null )
            throw new NoSuchRegisterException();
        return vFactory.getRegisterValue(obisCode);
    }

    /** Retrieves objects from the ObisCodeMap */
    public ValueFactory get( ObisCode o ) {
        return (ValueFactory)oMap.get( new ObisCodeWrapper( o ) );
    }

    /** Add objects to the ObisCodeMap */
    public void put( String oc, ValueFactory f ) {
        f.obisCode = ObisCode.fromString(oc);
        ObisCodeWrapper ocw = new ObisCodeWrapper(f.obisCode);

        keys.add( ocw );
        oMap.put( ocw, f );
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

        // current
        put( "1.1.1.8.0.255", new ValueFactory(15, 1, "Active import"){
            Date getToTime( ) throws IOException{ return null; };
        });
        // previous
        for( int i = 0; i < 255; i++ ) {
            put( "1.1.1.8.0."+i, new ValueFactory(15, i+1, "Active import"));
        }

        // current
        put( "1.1.200.8.0.255", new ValueFactory(11, 1, "Tarif register 11"){
            Date getToTime( ) throws IOException{ return null; };
        });
        // previous
        for( int i = 0; i < 255; i++ ) {
            put( "1.1.200.8.0."+i, new ValueFactory(11, i+1, "Tarif register 11"));
        }

        // current
        put( "1.1.201.8.0.255", new ValueFactory(12, 1, "Tarif register 12"){
            Date getToTime( ) throws IOException{ return null; };
        });
        // previous
        for( int i = 0; i < 255; i++ ) {
            put( "1.1.201.8.0."+i, new ValueFactory(12, i+1, "Tarif register 12"));
        }

        // current
        put( "1.1.202.8.0.255", new ValueFactory(13, 1, "Tarif register 13"){
            Date getToTime( ) throws IOException{ return null; };
        });
        // previous
        for( int i = 0; i < 255; i++ ) {
            put( "1.1.202.8.0."+i, new ValueFactory(13, i+1, "Tarif register 13"));
        }

        // current
        put( "1.1.203.8.0.255", new ValueFactory(14, 1, "Tarif register 14"){
            Date getToTime( ) throws IOException{ return null; };
        });
        // previous
        for( int i = 0; i < 255; i++ ) {
            put( "1.1.203.8.0."+i, new ValueFactory(14, i+1, "Tarif register 14"));
        }

        // current
        put( "1.1.204.8.0.255", new ValueFactory(5, 0, "Power downs"){
            Date getToTime( ) throws IOException{ return null; };
        });

        // current
        put( "1.1.205.8.0.255", new ValueFactory(6, 0, "Power down time"){
            Date getToTime( ) throws IOException{ return null; };
        });

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
    private void throwException( ObisCode obisCode ) throws NoSuchRegisterException {
        String ob = obisCode != null ? obisCode.toString() : "unknown";
        String msg = "ObisCode " + ob +" is not supported!";
        throw new NoSuchRegisterException(msg);
    }

    /** the java version of a closure ( aka a nice function pointer ) */
    class ValueFactory {
        boolean read = false;
        int address;
        int historic;

        ObisCode obisCode;
        String description;
        E120RegisterValue register;
        public ValueFactory( String description ){
            this.description = description;
        }
        public ValueFactory(int address, int historic, String description ){
            this.address = address;
            this.historic = historic;
            this.description = description;
        }
        Quantity getQuantity( ) throws IOException  { return null;       };
        // since the eventTime is always the same as the toTime ... shortcut
        Date getEventTime( ) throws IOException     { return null; };
        Date getFromTime( ) throws IOException      { return null; };

        ObisCode getObisCode( ) throws IOException  { return obisCode;   };

        Date getToTime( ) throws IOException  {
            Calendar c = Calendar.getInstance(e120.getTimeZone());
            c.setTime(register.getTime());
            c.add(Calendar.DAY_OF_YEAR, 1);
            return c.getTime();
        };

        E120RegisterValue getRegister() throws IOException {
            if(read==false){
                read = true;
                Message msg = e120.getConnection().registerValue(address, historic);
                DefaultResponse response = (DefaultResponse)msg.getBody();
                if( !response.isOk() ) return null;
                register = ((E120RegisterValue)response.getValue());
            }
            return register;
        }

        RegisterValue getRegisterValue( ObisCode obisCode ) throws IOException  {
            if( getRegister() == null ) throwException( obisCode );
            Quantity q = getRegister().toQuantity();
            Date e = getEventTime();
            Date f = getFromTime();
            Date t = getToTime();
            return new RegisterValue( obisCode, q, e, f, t );
        }

        public String toString(){
            return obisCode.toString();
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
