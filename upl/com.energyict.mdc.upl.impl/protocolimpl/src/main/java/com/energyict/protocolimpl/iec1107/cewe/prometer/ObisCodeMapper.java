package com.energyict.protocolimpl.iec1107.cewe.prometer;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ObisCodeExtensions;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/** @author fbo */

class ObisCodeMapper {

    static final int MaximumDemandE [] = {
        128,    /* rate group 1 */
        129,    /* rate group 2 */
        130     /* rate group 3 */
    };

    static final int C_SUMMATION_1 = 128;
    static final int C_SUMMATION_2 = 129;

    static final int C_EXTERNAL_1 = 130;
    static final int C_EXTERNAL_2 = 131;
    static final int C_EXTERNAL_3 = 132;

    static final int d_md_1 = 6;
    static final int d_md_2 = ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK;
    static final int d_md_3 = ObisCodeExtensions.OBISCODE_D_HIGHESTPEAK + 1;

    final private Prometer prometer;

    /** Collection for sorting the keys */
    private LinkedHashMap keys = new LinkedHashMap();

    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper() {
        this.prometer = null;
        init();
    }

    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper(Prometer prometer) {
        this.prometer = prometer;
        init();
    }

    /** @return a RegisterInfo for the obiscode */
    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ValueFactory vFactory = (ValueFactory)get( obisCode );
        if( vFactory == null )
            return new RegisterInfo( "not supported" );
        return new RegisterInfo( vFactory.getDescription() );
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
        return (ValueFactory)keys.get( new ObisCodeWrapper( o ) );
    }

    /** Add objects to the ObisCodeMap */
    public void put( ObisCode o, ValueFactory f ) {
        ObisCodeWrapper ocw = new ObisCodeWrapper(o);
        f.setObisCode(o);

        if(keys.containsKey(ocw))
            throw new ApplicationException("obiscode " + o + " already mapped");

        keys.put( ocw, f );
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
        Iterator i = keys.keySet().iterator();
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
    private void init( ) {

        ObisCode o;

        /* Date and time */
        put( toObis("1.1.0.8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                Unit secondsUnit = Unit.get( BaseUnit.SECOND );
                Long sl = new Long( prometer.getTime().getTime() / 1000 );
                return new Quantity( sl, secondsUnit );
            }
        });

        /* Total energy registers */

        /* Total active import */
        put( toObis("1.1.1.8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rEenergy[0].asQuantity();
            }
        });

        /* Total active export */
        put( toObis("1.1.2.8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rEenergy[1].asQuantity();
            }
        });

        /* Total reactive import */
        put( toObis("1.1.3.8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rEenergy[2].asQuantity();
            }
        });

        /* Total reactive export */
        put( toObis("1.1.4.8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rEenergy[3].asQuantity();
            }
        });

        /* Total apparent import */
        put( toObis("1.1.9.8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rEenergy[4].asQuantity();
            }
        });

        /* Total apparent export */
        put( toObis("1.1.10.8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rEenergy[5].asQuantity();
            }
        });

        /* Total external 1 */
        put( toObis("1.1." + C_EXTERNAL_1 + ".8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rExternal[0].asQuantity();
            }
        });

        /* Total external 2 */
        put( toObis("1.1." + C_EXTERNAL_2 + ".8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rExternal[1].asQuantity();
            }
        });

        /* Total external 3 */
        put( toObis("1.1." + C_EXTERNAL_3 + ".8.0.255"), new ValueFactory( ){
            Quantity getQuantity() throws IOException {
                return prometer.rExternal[2].asQuantity();
            }
        });

        /* Maximum demand mappings */

        for( int i = 0; i < 3; i ++ ) {

            final int e = MaximumDemandE[i];

            /* active import */

            /* maximum demand active import (highest) */
            o = toObis("1.1.1.6." + e + ".255");
            put( o, new MDValueFactory(Prometer.ACTIVE_IMPORT) );

            /* maximum demand active import (second highest) */
            o = toObis("1.1.1." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.ACTIVE_IMPORT) );

            /* maximum demand active import (third highest) */
            o = toObis("1.1.1." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.ACTIVE_IMPORT) );


            /* active export */

            /* maximum demand active export (highest) */
            o = toObis("1.1.2.6." + e + ".255");
            put( o, new MDValueFactory(Prometer.ACTIVE_EXPORT) );

            /* maximum demand active export (second highest) */
            o = toObis("1.1.2." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.ACTIVE_EXPORT) );

            /* maximum demand active export (third highest) */
            o = toObis("1.1.2." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.ACTIVE_EXPORT) );


            /* reactive import */

            /* maximum demand reactive import */
            o = toObis("1.1.3.6." + e + ".255");
            put( o, new MDValueFactory(Prometer.REACTIVE_IMPORT) );

            /* maximum demand reactive import */
            o = toObis("1.1.3." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.REACTIVE_IMPORT) );

            /* maximum demand reactive import */
            o = toObis("1.1.3." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.REACTIVE_IMPORT) );


            /* reactive export*/

            /* maximum demand reactive export */
            o = toObis("1.1.4.6." + e + ".255");
            put( o, new MDValueFactory(Prometer.REACTIVE_EXPORT) );

            o = toObis("1.1.4." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.REACTIVE_EXPORT) );

            o = toObis("1.1.4." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.REACTIVE_EXPORT) );


            /* apparent import */

            /* maximum demand apparent import */
            o = toObis("1.1.9.6." + e + ".255");
            put( o, new MDValueFactory(Prometer.APPARENT_IMPORT) );

            o = toObis("1.1.9." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.APPARENT_IMPORT) );

            o = toObis("1.1.9." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.APPARENT_IMPORT) );


            /* apparent export */

            /* maximum demand apparent export */
            o = toObis("1.1.10.6." + e + ".255");
            put( o, new MDValueFactory(Prometer.APPARENT_EXPORT) );

            o = toObis("1.1.10." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.APPARENT_EXPORT) );

            o = toObis("1.1.10." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.APPARENT_EXPORT) );


            /* summation 1 */

            /* maximum demand summation 1 */
            o = toObis("1.1." + C_SUMMATION_1 + ".6." + e + ".255");
            put( o, new MDValueFactory(Prometer.SUMMATION_1) );

            o = toObis("1.1." + C_SUMMATION_1 + "." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.SUMMATION_1) );

            o = toObis("1.1." + C_SUMMATION_1 + "." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.SUMMATION_1) );


            /* summation 2 */

            /* maximum demand summation 2 */
            o = toObis("1.1." + C_SUMMATION_2 + ".6." + e + ".255");
            put( o, new MDValueFactory(Prometer.SUMMATION_2) );

            o = toObis("1.1." + C_SUMMATION_2 + "." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.SUMMATION_2) );

            o = toObis("1.1." + C_SUMMATION_2 + "." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.SUMMATION_2) );


            /* external 1 */

            /* maximum demand external 1 */
            o = toObis("1.1." + C_EXTERNAL_1 + ".6." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_1) );

            o = toObis("1.1." + C_EXTERNAL_1 + "." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_1) );

            o = toObis("1.1." + C_EXTERNAL_1 + "." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_1) );


            /* external 2 */

            /* maximum demand external 2 */
            o = toObis("1.1." + C_EXTERNAL_2 + ".6." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_2) );

            o = toObis("1.1." + C_EXTERNAL_2 + "." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_2) );

            o = toObis("1.1." + C_EXTERNAL_2 + "." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_2) );


            /* external 3 */

            /* maximum demand external 3 */
            o = toObis("1.1." + C_EXTERNAL_3 + ".6." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_3) );

            o = toObis("1.1." + C_EXTERNAL_3 + "." + d_md_2 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_3) );

            o = toObis("1.1." + C_EXTERNAL_3 + "." + d_md_3 + "." + e + ".255");
            put( o, new MDValueFactory(Prometer.EXTERNAL_INPUT_3) );

        }


        /* historical billing registers */

        String vz[] = { "VZ", "VZ-1" };
        for( int i = 0; i < vz.length; i ++ ) {

            final int bp = i;

            /* Date and time */
            o = toObis("1.1.0.8.0." + vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    Unit secondsUnit = Unit.get( BaseUnit.SECOND );
                    Date from = prometer.rBilling[getIndex()].asShortDate(1);
                    Long sl = new Long( from.getTime() / 1000 );
                    return new Quantity( sl, secondsUnit );
                }
            });

            /* historical total registers */
            o = toObis("1.1.1.8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_ACTIVE_IMPORT);
                }
            });

            o = toObis("1.1.2.8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_ACTIVE_EXPORT);
                }
            });

            o = toObis("1.1.3.8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_REACTIVE_IMPORT);
                }
            });

            o = toObis("1.1.4.8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_REACTIVE_EXPORT);
                }
            });

            o = toObis("1.1.9.8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_APPARENT_IMPORT);
                }
            });

            o = toObis("1.1.10.8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_APPARENT_EXPORT);
                }
            });

            o = toObis("1.1." + C_SUMMATION_1 + ".8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_SUM_1);
                }
            });

            o = toObis("1.1." + C_SUMMATION_2 + ".8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_SUM_2);
                }
            });

            o = toObis("1.1." + C_EXTERNAL_1 + ".8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_INPUT_1);
                }
            });

            o = toObis("1.1." + C_EXTERNAL_2 + ".8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_INPUT_2);
                }
            });

            o = toObis("1.1." + C_EXTERNAL_3 + ".8.0."+vz[i]);
            put( o, new HistoricValueFactory( ){
                Quantity getQuantity() throws IOException {
                    return searchHistoricTotal(bp, Prometer.REG_TYPE_INPUT_3);
                }
            });


            /* historical rate registers */
            for( int r = 0; r < 8; r ++ ){

                final int rate = r+1;

                /* historical rate registers active import */
                put( toObis("1.1.1.8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_ACTIVE_IMPORT);
                        }
                    });

                /* historical rate registers active export */
                put( toObis("1.1.2.8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_ACTIVE_EXPORT);
                        }
                    });

                /* historical rate registers reactive import */
                put( toObis("1.1.3.8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_REACTIVE_IMPORT);
                        }
                    });

                /* historical rate registers reactive export */
                put( toObis("1.1.4.8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_REACTIVE_EXPORT);
                        }
                    });

                /* historical rate registers apparent import */
                put( toObis("1.1.9.8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_APPARENT_IMPORT);
                        }
                    });

                /* historical rate registers apparent export */
                put( toObis("1.1.10.8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_APPARENT_EXPORT);
                        }
                    });

                /* historical rate registers sum 1 */
                put( toObis("1.1." + C_SUMMATION_1 + ".8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_SUM_1);
                        }
                    });

                /* historical rate registers sum 2 */
                put( toObis("1.1." + C_SUMMATION_2 + ".8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_SUM_2);
                        }
                    });

                /* historical rate registers external 1 */
                put( toObis("1.1." + C_EXTERNAL_1 + ".8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_INPUT_1);
                        }
                    });

                /* historical rate registers external 2 */
                put( toObis("1.1." + C_EXTERNAL_2 + ".8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_INPUT_2);
                        }
                    });

                /* historical rate registers external 3 */
                put( toObis("1.1." + C_EXTERNAL_3 + ".8." + rate + "." + vz[i]),
                    new HistoricValueFactory(){
                        Quantity getQuantity() throws IOException {
                            return searchHistoricRate(bp, rate, Prometer.REG_TYPE_INPUT_3);
                        }
                    });

            }


            /* Maximum demand mappings */
            for( int md = 0; md < 3; md++ ) {

                final int e = MaximumDemandE[md];

                /* active import */

                /* maximum demand active import (highest) */
                o = toObis("1.1.1.6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* active export */

                /* maximum demand active export (highest) */
                o = toObis("1.1.2.6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );


                /* reactive import */

                /* maximum demand reactive import */
                o = toObis("1.1.3.6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* reactive export*/

                /* maximum demand reactive export */
                o = toObis("1.1.4.6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* apparent import */

                /* maximum demand apparent import */
                o = toObis("1.1.9.6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* apparent export */

                /* maximum demand apparent export */
                o = toObis("1.1.10.6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* summation 1 */

                /* maximum demand summation 1 */
                o = toObis("1.1." + C_SUMMATION_1 + ".6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* summation 2 */

                /* maximum demand summation 2 */
                o = toObis("1.1." + C_SUMMATION_2 + ".6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* external 1 */

                /* maximum demand external 1 */
                o = toObis("1.1." + C_EXTERNAL_1 + ".6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );

                /* external 2 */

                /* maximum demand external 2 */
                o = toObis("1.1." + C_EXTERNAL_2 + ".6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );


                /* external 3 */

                /* maximum demand external 3 */
                o = toObis("1.1." + C_EXTERNAL_3 + ".6." + e + "." + vz[i]);
                put( o, new HistoricMDValueFactory() );


            }


        }

    }

    private ObisCode toObis(String code){
        return ObisCode.fromString(code);
    }

    private Quantity searchHistoricTotal(final int bp, String searched)
        throws IOException, NoSuchRegisterException {

        for(int i = 0; i < prometer.rBillingTotal.length; i++ ){
            String regType = prometer.rBillingTotal[i][bp].asString(0);
            if( searched.equals( regType ) )
                return prometer.rBillingTotal[i][bp].asQuantity(2);
        }
        throw new NoSuchRegisterException();

    }

    private Quantity searchHistoricRate(final int bp, final int rate, String rType)
        throws IOException, NoSuchRegisterException {

        for(int i = 0; i < prometer.rBillingRegister.length; i++ ){

            ProRegister register = prometer.rBillingRegister[i][bp];

            if( register.isEmpty() ) continue;

            String regType = register.asString(0);
            int rRate = register.asInt(1);

            if( rType.equals( regType ) && rRate == rate )
                return register.asQuantity(2);

        }
        throw new NoSuchRegisterException();

    }

    /** @return list of all ObisCodes supported by the currently connected
     * meter.  By trial and error. */
    private List getMeterSupportedObisCodes( ) throws IOException {
        ArrayList validObisCodes = new ArrayList( );
        Iterator i = keys.keySet().iterator();
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
            } catch(ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                System.out.println( oc );
            }
        }
        return validObisCodes;
    }

    /** Shorthand notation for creating NoSuchRegisterException
     * @throws NoSuchRegisterException  */
    private NoSuchRegisterException createNoSuchRegisterException(ObisCode obisCode)
        throws NoSuchRegisterException {

        String ob = obisCode != null ? obisCode.toString() : "unknown";
        String msg = "ObisCode " + ob +" is not supported!";
        return new NoSuchRegisterException(msg);

    }

    /** the java version of a closure ( aka a nice function pointer ) */
    abstract class ValueFactory {
        ObisCode obisCode = null;
        public ValueFactory(  ){ }
        Quantity getQuantity( ) throws IOException  { return null;       };
        // since the eventTime is always the same as the toTime ... shortcut
        Date getEventTime( ) throws IOException     { return null; };
        Date getFromTime( ) throws IOException      { return null; };
        Date getToTime( ) throws IOException        { return null; };
        ObisCode getObisCode( ) throws IOException  { return obisCode;   };
        void setObisCode(ObisCode oCode)            { this.obisCode = oCode; };

        RegisterValue getRegisterValue( ObisCode obisCode ) throws IOException  {
            Quantity q = getQuantity();
            if( q == null ) throw createNoSuchRegisterException(obisCode);
            Date e = getEventTime();
            Date f = getFromTime();
            Date t = getToTime();
            return new RegisterValue( obisCode, q, e, f, t );
        }

        public String toString(){
            return obisCode.toString();
        }

        String getDescription( ){
            return getCDescription() + getDDescription() +
                   getEDescription() + getFDescription();
        }

        String getCDescription( ) {
            switch(obisCode.getC()) {
                case 0:             return "time";
                case 1:             return "active energy imp.";
                case 2:             return "active energy exp.";
                case 3:             return "reactive energy imp.";
                case 4:             return "reactive energy exp.";
                case 8:             return "reactive energy QIV";
                case 9:             return "apparent energy imp.";
                case 10:            return "apparent energy exp";
                case C_SUMMATION_1: return "summation 1";
                case C_SUMMATION_2: return "summation 2";
                case C_EXTERNAL_1:  return "external register 1";
                case C_EXTERNAL_2:  return "external register 2";
                case C_EXTERNAL_3:  return "external register 3";
            }
            String msg = "No description found for " + this.obisCode;
            throw new ApplicationException(msg);
        }

        String getDDescription( ){
            switch(obisCode.getD()){
                case d_md_1:        return ", maximum demand (highest)";
                case d_md_2:        return ", maximum demand (second highest)";
                case d_md_3:        return ", maximum demand (third highest)";
                default:            return "";
            }
        }

        String getEDescription( ){
            switch(obisCode.getE()) {
                case 0:             return "";
                default:            return ", rate " + obisCode.getE();
            }
        }

        String getFDescription( ){
            switch(obisCode.getF()) {
                case 255:           return "";
                case 0:             return ", billing point VZ";
                case -1:            return ", billing point VZ-1";
            }
            String msg = "No description found for " + this.obisCode;
            throw new ApplicationException(msg);
        }

    }

    /** Specialized subclass of ValueFactory for retrieving Max Demands */
    class HistoricValueFactory extends ValueFactory {

        int getIndex( ) throws IOException{
            if( getObisCode().getF() == 0 )
                return 0;
            return 1;
        }

        Date getFromTime() throws IOException {
            return prometer.rBilling[getIndex()].asShortDate(0);
        }

        Date getToTime() throws IOException {
            return prometer.rBilling[getIndex()].asShortDate(1);
        }

    }

    /** Specialized subclass of ValueFactory for retrieving Max Demands */
    class MDValueFactory extends ValueFactory {

        private int phenomenon;
        private int mdIndex [];

        MDValueFactory(int phenomenon){
            this.phenomenon = phenomenon;
        }

        private int getMDIndex( ) throws IOException {
            if( mdIndex == null){

                int d = obisCode.getD();
                int idx = prometer.findMDRegister(phenomenon);

                if( d != 6 && idx != 0 )
                    throw createNoSuchRegisterException(getObisCode());

                if( d == 6 )
                    mdIndex = new int [] { idx };

                if( d == d_md_2 )
                    mdIndex = new int [] { 6 };

                if( d == d_md_3 )
                    mdIndex = new int [] { 7 };

            }

            return mdIndex[0];
        }

        private int getE() throws NoSuchRegisterException{
            int e = obisCode.getE();

            if( e == MaximumDemandE[0] )
                return 0;

            if( e == MaximumDemandE[1] )
                return 1;

            if( e == MaximumDemandE[2] )
                return 2;

            throw createNoSuchRegisterException(obisCode);

        }

        Quantity getQuantity() throws IOException {
            return prometer.rMaximumDemand[getMDIndex()][getE()].asQuantity();
        }

        Date getEventTime() throws IOException {
            try {

                String ds = null;
                ds = prometer.rMaximumDemandDate[getMDIndex()][getE()].asString(0);
                ds += prometer.rMaximumDemandDate[getMDIndex()][getE()].asString(1);

                if( ds.length() == 0 )
                    throw createNoSuchRegisterException(getObisCode());

                return prometer.getShortDateFormat().parse(ds);

            } catch (ParseException pex) {
                throw new NestedIOException(pex);
            }
        }

    }

    /** Specialized subclass of ValueFactory for retrieving Max Demands */
    class HistoricMDValueFactory extends ValueFactory {

        private ProRegister register;

        private String regType;
        private int [] bp;
        private int [] group;

        HistoricMDValueFactory(){ }

        int getBp( ) throws IOException {
            if( bp == null ) {
                if( getObisCode().getF() == 0 )     bp = new int[] { 0 };
                if( getObisCode().getF() == -1 )    bp = new int[] { 1 };
            }

            if( bp == null )
                throw createNoSuchRegisterException(obisCode);

            return bp[0];
        }

        int getGroup( ) throws IOException {
            if( group == null ) {

                if( getObisCode().getE() == MaximumDemandE[0] )
                    group = new int[] { 1 };

                if( getObisCode().getE() == MaximumDemandE[1] )
                    group = new int[] { 2 };

                if( getObisCode().getE() == MaximumDemandE[2] )
                    group = new int[] { 3 };

            }

            if( group == null )
                throw createNoSuchRegisterException(obisCode);

            return group[0];
        }

        String getRegType( ) throws IOException {

            if( regType == null ) {

                if( getObisCode().getC() == 1 )
                    regType = Prometer.REG_TYPE_ACTIVE_IMPORT;

                if( getObisCode().getC() == 2 )
                    regType = Prometer.REG_TYPE_ACTIVE_EXPORT;

                if( getObisCode().getC() == 3 )
                    regType = Prometer.REG_TYPE_REACTIVE_IMPORT;

                if( getObisCode().getC() == 4 )
                    regType = Prometer.REG_TYPE_REACTIVE_EXPORT;

                if( getObisCode().getC() == 9 )
                    regType = Prometer.REG_TYPE_APPARENT_IMPORT;

                if( getObisCode().getC() == 10 )
                    regType = Prometer.REG_TYPE_APPARENT_EXPORT;

                if( getObisCode().getC() == 128 )
                    regType = Prometer.REG_TYPE_SUM_1;

                if( getObisCode().getC() == 129 )
                    regType = Prometer.REG_TYPE_SUM_2;

                if( getObisCode().getC() == 130 )
                    regType = Prometer.REG_TYPE_INPUT_1;

                if( getObisCode().getC() == 131 )
                    regType = Prometer.REG_TYPE_INPUT_2;

                if( getObisCode().getC() == 132 )
                    regType = Prometer.REG_TYPE_INPUT_3;

                if( regType == null )
                    throw createNoSuchRegisterException(obisCode);
            }

            return regType;

        }

        ProRegister getRegister( ) throws IOException {

            if( register == null ) {

                for( int i = 0; i < prometer.rBillingMD.length; i++){
                    String registerRegType = prometer.rBillingMD[i][getBp()].asString(0);
                    if( getRegType().equals( registerRegType ) ) {
                        int group = prometer.rBillingMD[i][getBp()].asInt(1);
                        if( group == getGroup() )
                            register = prometer.rBillingMD[i][getBp()];
                    }
                }

            }

            if( register == null )
                throw createNoSuchRegisterException(obisCode);

            return register;

        }

        Quantity getQuantity() throws IOException {
            return getRegister().asQuantity(3);
        }

        Date getEventTime() throws IOException {
            return getRegister().asShortDate(4);
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
