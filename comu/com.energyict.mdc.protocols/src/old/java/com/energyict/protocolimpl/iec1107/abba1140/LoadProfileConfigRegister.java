/*
 * LoadProfileConfigRegister.java
 *
 * Created on 21 February 2006, 14:06
 *
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import com.energyict.cbo.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/** 29 may 2006 Bugfix
 * The load profile was being read with energy units.  The meter was actually
 * reporting power units.
 * The fix with the least impact on the code was to use functionality of
 * the Unit class to tranform to a flow unit.
 *
 * @author fbo */

public class LoadProfileConfigRegister {

    private ABBA1140RegisterFactory rFactory;
    private byte [] channelMask = null;

    private ArrayList register = new ArrayList();
    private ArrayList channelInfo = new ArrayList();

    private boolean importWh = false;
    private boolean exportWh = false;
    private boolean q1Varh = false;
    private boolean q2Varh = false;
    private boolean q3Varh = false;
    private boolean q4Varh = false;
    private boolean vah1 = false;
    private boolean vah2 = false;
    private boolean customerDefined1 = false;
    private boolean customerDefined2 = false;

    /** Creates new LoadProfileConfigRegister */
    LoadProfileConfigRegister(ABBA1140RegisterFactory rFactory, byte[] data) throws IOException {

        this.rFactory = rFactory;
        this.channelMask = data;

        init();

    }

    LoadProfileConfigRegister(ABBA1140RegisterFactory rFactory, int data ) throws IOException {

        this.rFactory = rFactory;
        byte b1 = (byte)((data&0xFF00)>>8);
        byte b2 = (byte)(data&0x00FF);
        byte [] ba = { b1, b2 };
        this.channelMask = ba;

        init();

    }

    private void init( ) throws IOException {

        int i = 0;

        if( ( channelMask[1] & 0x01 ) > 0 ) {
            importWh = true;
            ABBA1140Register r = rFactory.getCummMainImport();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[1] & 0x02 ) > 0 ) {
            exportWh = true;
            ABBA1140Register r = rFactory.getCummMainExport();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[1] & 0x04 ) > 0 ) {
            q1Varh = true;
            ABBA1140Register r =  rFactory.getCummMainQ1();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[1] & 0x08 ) > 0 ) {
            q2Varh = true;
            ABBA1140Register r = rFactory.getCummMainQ2();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[1] & 0x10 ) > 0 ) {
            q3Varh = true;
            ABBA1140Register r = rFactory.getCummMainQ3();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[1] & 0x20 ) > 0 ) {
            q4Varh = true;
            ABBA1140Register r = rFactory.getCummMainQ4();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[1] & 0x40 ) > 0 ) {
            vah1 = true;
            ABBA1140Register r = rFactory.getCummMainVAImport();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[1] & 0x80 ) > 0 ) {
            vah2 = true;
            ABBA1140Register r = rFactory.getCummMainVAExport();
            register.add( r );
            Unit u = r.getUnit().getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, u ) );
            i = i + 1;
        }

        if( ( channelMask[0] & 0x40 ) > 0 ) {
            customerDefined1 = true;
            ABBA1140Register r = rFactory.getCummMainCustDef1();
            register.add( r );
            CustDefRegConfig cd = (CustDefRegConfig)rFactory.getRegister(rFactory.getCustDefRegConfig());
            Unit unit = EnergyTypeCode.getUnitFromRegSource(cd.getRegSource(0),true).getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, unit ) );
            i = i + 1;
        }

        if( ( channelMask[0] & 0x80 ) > 0 ) {
            customerDefined2 = true;
            ABBA1140Register r = rFactory.getCummMainCustDef2();
            register.add( r );
            CustDefRegConfig cd = (CustDefRegConfig)rFactory.getRegister(rFactory.getCustDefRegConfig());
            Unit unit = EnergyTypeCode.getUnitFromRegSource(cd.getRegSource(1),true).getFlowUnit();
            channelInfo.add( new ChannelInfo( i, "ELSTERA1140_channel_"+ i, unit ) );
            i = i + 1;
        }

    }

    int getNumberRegisters() {
        return register.size();
    }

    public Collection getRegisters(){
        return register;
    }

    boolean isImportWh() {
        return importWh;
    }

    boolean isExportWh() {
        return exportWh;
    }

    boolean isQ1Varh() {
        return q1Varh;
    }

    boolean isQ2Varh() {
        return q2Varh;
    }

    boolean isQ3Varh() {
        return q3Varh;
    }

    boolean isQ4Varh() {
        return q4Varh;
    }

    boolean isVah1() {
        return vah1;
    }

    boolean isVah2() {
        return vah2;
    }

    boolean isCustomerDefined1() {
        return customerDefined1;
    }

    boolean isCustomerDefined2() {
        return customerDefined2;
    }


    int getChannelMask(){
        int x = (0x00FF00&(channelMask[0]<<8))|channelMask[1];
        return x;
    }

    Collection toChannelInfo() throws IOException {
        return channelInfo;
    }

    public String toShortString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( " [" );
        Iterator i = register.iterator();
        while( i.hasNext() ) {
            ABBA1140Register r = (ABBA1140Register)i.next();
            rslt.append( r.getName() + " " );
        }
        rslt.append( "] " );
        return rslt.toString();
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "LoadProfileConfigRegister [" );
        rslt.append( " chn msk: " + getChannelMask() );
        try {
            Iterator i = this.toChannelInfo().iterator();
            while( i.hasNext() ){
                ChannelInfo ci = (ChannelInfo)i.next();
                rslt.append( "ChannelInfo [" + ci.getId() + " " + ci.getName() + " " + ci.getChannelId() + " ]\n" );
            }
        } catch( IOException ioe ){
            rslt.append( ioe );
        }
        rslt.append( "]" );
        return rslt.toString();
    }

}
