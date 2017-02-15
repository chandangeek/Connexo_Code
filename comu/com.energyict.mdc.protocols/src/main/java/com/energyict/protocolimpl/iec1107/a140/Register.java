/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ABBA1700Register.java
 *
 * Created on 24 april 2003, 17:29
 */

package com.energyict.protocolimpl.iec1107.a140;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/** Base class for all the registers.
 * <pre>
 * A register is responsible for
 *  - fetching it's data from the meter.
 *  - parsing the data
 *
 * There are 2 types to use a register:
 *  - as top level register accessible from the RegisterFactory.
 *    such a registers knows the address and will go to the meter
 *  - as part of another register.  In this case no data will
 *    be fetched only parsed.
 *
 * (this is old fashiond oo-design with a fragile base class, but since all
 * off the children are known I was hoping this was not so bad )
 * </pre>
 * @author fbo */

public abstract class Register {

    /** No options */
    public final static int N   = 0;

    /** Read access */
    public final static int R   = 1 << 0; // 1
    /** Write access */
    public final static int W   = 1 << 1; // 2
    /** Read/Write access */
    public final static int RW  = 1 << 2; // 4

    /** Cacheable */
    public final static int C   = 1 << 5; // 32
    /** Not Cacheable */
    public final static int NC  = 1 << 6; // 64

    protected A140 a140 = null;
    private final String id;
    protected final int length;
    protected final int sets;
    byte [] rawdata = null;

    private final boolean cacheable;
    private final int access;

    public Register( A140 a140 ){
        this( a140, null, 0, 0, N );
    }

    public Register( A140 a140, String id, int length, int sets ) {
        this( a140, id, length, sets, N );
    }

    public Register( A140 a140, String id, int length, int sets, int options ) {
        this.a140 = a140;
        this.id = id;
        this.length = length;
        this.sets = sets;

        cacheable = !( (options & NC) != 0 );
        access = (options & R) | (options & W) | (options & RW);
    }

    /** Read the register: fetch data from meter and parse.
     * @throws IOException
     */
    public void read( ) throws IOException {
        if( cacheable ) {
            if( rawdata == null ) {
                rawdata = readDataIdentity();
                parse( rawdata );
            }
            return;
        }
        parse( readDataIdentity() ); // if not cached
        return;
    }

    public void read( byte [] input ) throws IOException {
        rawdata = input;
        parse( input );
    }

    public void write( ) throws IOException {
        if( ( access & W ) > 0 || ( access & RW ) > 0 ){
            writeDataIdentity();
            rawdata = null; // throw cache away
        } else {
            String msg = "Register " + id + " is read only.";
            throw new IOException( msg );
        }
    }

    public boolean isCached( ){
        return cacheable && ( rawdata != null );
    }

    /*
     * After 4,5 min, do authentication before continue! otherwise we can
     * receive ERR5, password timeout!
     */
    private static final int AUTHENTICATE_REARM = 270000;

    private byte [] readDataIdentity( )
        throws FlagIEC1107ConnectionException, IOException {

        long logInTimeout = System.currentTimeMillis() + AUTHENTICATE_REARM;
        FlagIEC1107Connection con = a140.getFlagIEC1107Connection();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int [] sets = getSets();

        for( int pI = 1; pI < sets.length + 1; pI ++ ) {

            byte [] cmd = ( id + toHex( pI, 3 ) + "(" + toHex( sets[pI-1], 2 ) + ")" ).getBytes();
            con.sendRawCommandFrame( FlagIEC1107Connection.READ1, cmd );
            byte[] ba = con.receiveData();
            String str = new String(ba);
            if (str.indexOf("ERR") != -1) {
                int i = str.indexOf("ERR");
                String exceptionId = str.substring(i, i + 4);
                throw new FlagIEC1107ConnectionException(
                        (String) A140.exception.get(exceptionId));
            }
            bos.write(ba);

            if (((long) (System.currentTimeMillis() - logInTimeout)) > 0) {
                logInTimeout = System.currentTimeMillis() + AUTHENTICATE_REARM;
                a140.getFlagIEC1107Connection().authenticate();
            }

        }
        return ProtocolUtils.convert2ascii(bos.toByteArray());

    }

    private void writeDataIdentity( ) throws IOException {
        byte [] d = ( id + "001("+ new String( construct() ) + ")" ).getBytes();
        FlagIEC1107Connection con = a140.getFlagIEC1107Connection();
        String retVal =
            con.sendRawCommandFrameAndReturn(FlagIEC1107Connection.WRITE1,d);

        if ((retVal != null) && (retVal.indexOf("ERR") != -1)) {
            String msg =    retVal + " received! Write "  +
                            "command failed! Possibly wrong password level!";
            throw new IOException(msg);
        }
    }

    public abstract void parse( byte [] ba ) throws IOException;

    public byte[] construct( ){
        return null;
    }

    private String toHex( int nr, int length ) {
        return fill( Integer.toHexString( nr ).toUpperCase(), length);
    }

    private String fill( String base, int length ){
        String result = base;
        for( int i = base.length(); i < length; i ++ ) result = "0" + result;
        return result;
    }

    /** @return an array of set lengths
     * @throws IOException */
    public int [] getSets( ) throws IOException {
        int [] result = new int[sets];
        for( int i = 0; i < result.length; i ++ )
            result[i] = length / result.length;
        return result;
    }

    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append( "Register " );
        rslt.append( "id=" + id + ", " );
        try {
            rslt.append( "length=" + length + ", " );
            rslt.append( "sets=" + getSets() + ", " );
            rslt.append( "cacheable=" + cacheable + ", " );
        } catch( IOException ioe ) {
            throw new ApplicationException( ioe );
        }
        return rslt.toString();
    }

}
