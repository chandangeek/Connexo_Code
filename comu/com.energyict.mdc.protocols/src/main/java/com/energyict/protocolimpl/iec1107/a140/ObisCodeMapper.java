/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 11 juni 2004, 13:55
 */

package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.iec1107.ProtocolLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;

public class ObisCodeMapper {

    private int dbg = 0;

    private ProtocolLink pLink = null;
    private RegisterFactory rFactory = null;

    /** Collection for sorting the keys */
    private TreeSet keys = new TreeSet();
    /** HashMap with the ValueFactories per ObisCode  */
    private HashMap oMap = new HashMap();

    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper(ProtocolLink pLink, RegisterFactory registerFactory) throws IOException {
        this.pLink = pLink;
        this.rFactory=registerFactory;
        init();
    }

    /** @return a RegisterInfo for the obiscode */
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
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
    public void put( ObisCode o, ValueFactory f ) {
        ObisCodeWrapper ocw = new ObisCodeWrapper(o);
        keys.add( ocw );
        oMap.put( ocw, f );
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
            //result.append( o + " " + getRegisterInfo( o ) + "\n" );
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

        ObisCode o = null;

        // create obiscodes for cummulative registers
        o = ObisCode.fromString("1.1.1.8.0.255");
        put( o, new ValueFactory( o ){
            Quantity getQuantity() throws IOException {
                return rFactory.getCumulative().getImportRegister();
            }
        });

        o = ObisCode.fromString("1.1.2.8.0.255");
        put( o, new ValueFactory( o ){
            Quantity getQuantity() throws IOException {
                return rFactory.getCumulative().getExportRegister();
            }
        });

        // create obiscodes for rate registers
        for( int i = 1; i < 5; i ++ ) {

            final int rateIndex = i-1;
            final int touImport = TouSourceRegister.IMPORT;
            final int touExport = TouSourceRegister.EXPORT;
            final TouSourceRegister tSources = rFactory.getTouSource();

            o = ObisCode.fromString( "1.1.1.8." + i + ".255" );
            put( o, new ValueFactory( o ){
                Quantity getQuantity() throws IOException {
                    if( tSources.getSource( rateIndex ) == touImport )
                        return rFactory.getTou().get(rateIndex);
                    return null;
                }
            });

            o = ObisCode.fromString( "1.1.2.8." + i + ".255" );
            put( o, new ValueFactory( o ){
                Quantity getQuantity() throws IOException {
                    if( tSources.getSource( rateIndex ) == touExport )
                        return rFactory.getTou().get(rateIndex);
                    return null;
                }
            });
        }

        // create obiscodes for time register
        o = ObisCode.fromString("1.1.0.1.2.255" );
        put( o, new ValueFactory( o ){
            Quantity getQuantity() { return null; }
            RegisterValue getRegisterValue( ObisCode obisCode )throws IOException{
                return new RegisterValue( obisCode, rFactory.getTimeAndDate().getTime() );
            }
        });

        // create obiscodes for historical register
        final int [] billingPoint = { 0, 1, 2, 3, 4 };
        for( int i = 0; i < billingPoint.length; i ++ ) {

            final HistoricalRegisterSet hrs = rFactory.getHistoricalSet();
            final int bp = i;

            o = ObisCode.fromString("1.1.1.8.0." + billingPoint[i] );
            put( o, new ValueFactory( o ){

                Date getToTime() throws IOException {
                    return hrs.get(bp).getTime();
                }

                Quantity getQuantity() throws IOException {
                    if( hrs.get(bp).getTime() != null )
                        return hrs.get(bp).getImportRegister();
                    return null;
                }
            });

            o = ObisCode.fromString("1.1.2.8.0." + billingPoint[i] );
            put(  o, new ValueFactory( o ){

                Date getToTime() throws IOException {
                    return hrs.get(bp).getTime();
                }

                Quantity getQuantity() throws IOException {
                    if( hrs.get(bp).getTime() != null )
                        return hrs.get(bp).getExportRegister();
                    return null;
                }
            });

            // create obiscodes for rate registers
            for( int ii = 1; ii < 5; ii ++ ) {

                final int rateIndex = ii-1;
                final int touImport = TouSourceRegister.IMPORT;
                final int touExport = TouSourceRegister.EXPORT;

                o = ObisCode.fromString( "1.1.1.8." + ii + "." + billingPoint[i] );
                put( o, new ValueFactory( o ){

                    Date getToTime() throws IOException {
                        return hrs.get(bp).getTime();
                    }

                    Quantity getQuantity() throws IOException {
                        int src = hrs.get(bp).getTariffSources().getSource( rateIndex );
                        if( hrs.get(bp).getTime() != null && src == touImport )
                            return hrs.get(bp).getTou(rateIndex);
                        return null;
                    }
                });

                o = ObisCode.fromString( "1.1.2.8." + ii + "." + billingPoint[i] );
                put( o, new ValueFactory( o ){

                    Date getToTime() throws IOException {
                        return hrs.get(bp).getTime();
                    }

                    Quantity getQuantity() throws IOException {
                        int src = hrs.get(bp).getTariffSources().getSource( rateIndex );
                        if( hrs.get(bp).getTime() != null && src == touExport  )
                            return hrs.get(bp).getTou(rateIndex);
                        return null;
                    }
                });

            }

            // time register
            o = ObisCode.fromString("1.1.0.1.2." + billingPoint[i] );
            put(  o, new ValueFactory( o ){

                Date getFromTime() throws IOException   { return null; }
                Date getToTime() throws IOException     { return hrs.get(bp).getTime(); }

                Quantity getQuantity() throws IOException {
                    Unit secondsUnit = Unit.get( BaseUnit.SECOND );
                    if(getEventTime() == null )
                        throwException( obisCode );
                    Long sl = new Long( getEventTime().getTime() / 1000 );
                    return new Quantity( sl, secondsUnit );
                }

            });

        }

        if( dbg > 0 )
            pLink.getLogger().log( Level.INFO, this.toString() );

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
    abstract class ValueFactory {
        ObisCode obisCode = null;
        public ValueFactory( ObisCode o ){
            obisCode = o;
        }
        Quantity getQuantity( ) throws IOException  { return null;       };
        // since the eventTime is always the same as the toTime ... shortcut
        Date getEventTime( ) throws IOException     { return getToTime(); };
        Date getFromTime( ) throws IOException      { return null; };
        Date getToTime( ) throws IOException        { return new Date(); };
        ObisCode getObisCode( ) throws IOException  { return obisCode;   };

        RegisterValue getRegisterValue( ObisCode obisCode ) throws IOException  {
            Quantity q = getQuantity();
            if( q == null ) throwException( obisCode );
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
            return "ObisCode: "  + obisCode;
        }

        public int compareTo(Object o) {
            ObisCodeWrapper other = (ObisCodeWrapper)o;
            return reversedOs.compareTo(other.reversedOs);
        }

    }



}
