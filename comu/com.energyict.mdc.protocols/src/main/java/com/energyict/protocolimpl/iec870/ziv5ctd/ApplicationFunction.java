/*
 * ApplicationFunction.java
 *
 * Created on 27 March 2006, 08:38
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;

import com.energyict.cbo.Unit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fbo */

public class ApplicationFunction {

    private final LinkLayer linkLayer;
    private final FrameFactory frameFactory;

    private boolean cumulative;
    private final ArrayList channelInfo;

    /** Creates a new instance of ApplicationFunction */
    public ApplicationFunction(Ziv5Ctd ziv) {
        this.linkLayer = ziv.linkLayer;
        this.frameFactory = ziv.frameFactory;
        this.cumulative = ziv.pCumulativeProfile;

        this.channelInfo =  new ArrayList( ){
            {
                ChannelInfo c1 = new ChannelInfo( 1, "Import Kw", Unit.get("kWh") );
                ChannelInfo c2 = new ChannelInfo( 2, "Export Kw", Unit.get("kWh") );
                ChannelInfo c3 = new ChannelInfo( 3, "Reactive Q1", Unit.get("kvarh") );
                ChannelInfo c4 = new ChannelInfo( 4, "Reactive Q2", Unit.get("kvarh") );
                ChannelInfo c5 = new ChannelInfo( 5, "Reactive Q3", Unit.get("kvarh") );
                ChannelInfo c6 = new ChannelInfo( 6, "Reactive Q4", Unit.get("kvarh") );

                if( cumulative ) {
                    c1.setCumulativeWrapValue( Ziv5Ctd.MAX_PROFILE_VALUE );
                    c2.setCumulativeWrapValue( Ziv5Ctd.MAX_PROFILE_VALUE );
                    c3.setCumulativeWrapValue( Ziv5Ctd.MAX_PROFILE_VALUE );
                    c4.setCumulativeWrapValue( Ziv5Ctd.MAX_PROFILE_VALUE );
                    c5.setCumulativeWrapValue( Ziv5Ctd.MAX_PROFILE_VALUE );
                    c6.setCumulativeWrapValue( Ziv5Ctd.MAX_PROFILE_VALUE );
                }

                add( c1 );
                add( c2 );
                add( c3 );
                add( c4 );
                add( c5 );
                add( c6 );
            }
        };
    }

    Object read( Asdu asdu ) throws IOException, ParseException {

        ArrayList result = new ArrayList();

        if( asdu.getTypeIdentification().isReadProfileCmd() ){
            return readProfile(asdu);
        }
        if( asdu.getTypeIdentification().isReadHistoricalTarification() ) {
            return readHistoricalTarification(asdu);
        }
        if( asdu.getTypeIdentification().isReadTarification() ) {
            return readTarification(asdu);
        }
        if( asdu.getTypeIdentification().isReadEvents() ) {
            return readEvents(asdu);
        }

        readSingle(result, asdu);
        return result;

    }

    private Object readProfile(final Asdu asdu) throws IOException, ParseException {

        ProfileData pData = new ProfileData();

        pData.setChannelInfos(channelInfo);

        VariableFrame vFrame = (VariableFrame) linkLayer.requestRespond(
                (VariableFrame) frameFactory.createVariable(
                FunctionCode.PRIMARY[0x3], asdu ));

        Frame resp = frameFactory.createFixed( FunctionCode.PRIMARY[0xb] );

        CauseOfTransmission fCot = vFrame.getCauseOfTransmission();
        while( fCot != CauseOfTransmission.ACTIVATION_TERMINATION ) {
            vFrame = (VariableFrame)linkLayer.requestRespond(resp );
            List al = (List)vFrame.getAsdu().getInformationObjects();
            if( al.size() > 0) {
                InformationObject8 i8 = (InformationObject8)al.get(0);
                pData.addInterval( i8.toIntervalData() );
            }
            fCot = vFrame.getCauseOfTransmission();
        }

        return pData;
    }

    private Object readTarification( final Asdu asdu ) throws IOException {

        InformationObject87 io87 = new InformationObject87();

        VariableFrame vFrame =
                (VariableFrame) linkLayer.requestRespond(
                    (VariableFrame) frameFactory.createVariable( FunctionCode.PRIMARY[0x3], asdu ));

        Frame resp = frameFactory.createFixed( FunctionCode.PRIMARY[0xb] );

        CauseOfTransmission fCot = vFrame.getCauseOfTransmission();
        while( fCot != CauseOfTransmission.ACTIVATION_TERMINATION ) {
            vFrame = (VariableFrame)linkLayer.requestRespond(resp );
            Asdu rAsdu = vFrame.getAsdu();
            List al = (List)rAsdu.getInformationObjects();
            if( al.size() > 0) {
                io87.add( (InformationObject87Period) al.get(0));
            }
            fCot = vFrame.getCauseOfTransmission();
        }

        return io87;
    }

    private Object readHistoricalTarification( final Asdu asdu ) throws IOException {

        InformationObject88 io88 = new InformationObject88();

        VariableFrame vFrame =
                (VariableFrame) linkLayer.requestRespond(
                    (VariableFrame) frameFactory.createVariable( FunctionCode.PRIMARY[0x3], asdu ));

        if( vFrame.getCauseOfTransmission() != CauseOfTransmission.ACTIVATION_CONFIRMATION )
            return io88;

        Frame resp = frameFactory.createFixed( FunctionCode.PRIMARY[0xb] );

        CauseOfTransmission fCot = vFrame.getCauseOfTransmission();
        while( fCot != CauseOfTransmission.ACTIVATION_TERMINATION ) {
            vFrame = (VariableFrame)linkLayer.requestRespond(resp);
            Asdu rAsdu = vFrame.getAsdu();
            List al = (List)rAsdu.getInformationObjects();
            if( al.size() > 0) {
                io88.add( (InformationObject88Period) al.get(0));
            }
            fCot = vFrame.getCauseOfTransmission();
        }

        return io88;
    }

    private List readEvents(final Asdu asdu) throws IOException {

        ArrayList result = new ArrayList();
        VariableFrame vFrame =
            (VariableFrame) linkLayer.requestRespond(
            (VariableFrame) frameFactory.createVariable( FunctionCode.PRIMARY[0x3], asdu ));

        if( vFrame.getCauseOfTransmission() != CauseOfTransmission.ACTIVATION_CONFIRMATION )
            return result;

        Frame resp = frameFactory.createFixed( FunctionCode.PRIMARY[0xb] );

        CauseOfTransmission fCot = vFrame.getCauseOfTransmission();
        while( fCot != CauseOfTransmission.ACTIVATION_TERMINATION ) {
            vFrame = (VariableFrame)linkLayer.requestRespond(resp);
            Asdu rAsdu = vFrame.getAsdu();
            List al = (List)rAsdu.getInformationObjects();
            if( al.size() > 0) {
                InformationObject1 io1 = (InformationObject1)al.get(0);
                result.addAll( io1.getMeterEvents() );
            }
            fCot = vFrame.getCauseOfTransmission();
        }

        return result;
    }

    private void readSingle(final ArrayList result, final Asdu asdu) throws IOException, ParseException {

        VariableFrame vFrame = (VariableFrame)
        linkLayer.requestRespond( (VariableFrame)
        frameFactory.createVariable(
                FunctionCode.PRIMARY[0x3], asdu ));
        result.addAll( vFrame.getAsdu().getInformationObjects() );
    }

}
