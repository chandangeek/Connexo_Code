/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InformationObject1.java
 *
 * Created on 14 april 2006, 15:46
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Date;

/** @author fbo */

public class InformationObject1Event extends InformationObject {

    /** Single-point address */
    private int spa;
    /** Single-point qualifier */
    private int spq;
    private int spi;

    private Date date;

    /** Creates a new instance of InformationObject1 */
    public InformationObject1Event( int spa, int spq, int spi, Date date ) {
        this.spa = spa;
        this.spq = spq;
        this.spi = spi;
        this.date = date;
    }

    public void setSpa(int spa) {
        this.spa = spa;
    }

    public void setSpq(int spq) {
        this.spq = spq;
    }

    public void setSpi(int spi) {
        this.spi = spi;
    }

    public Date getDate() {
        return date;
    }

    MeterEvent toMeterEvent( ){
        String d = null;

        if(spa == 1 && spq == 1) {
            d = "starting, restart of system (with loss of the data)";
            return new MeterEvent( getDate(), MeterEvent.POWERUP, d );
        }
        if(spa == 1 && spq == 2) {
            d = "starting after powerfailure (data, parameters and " +
                "hour not lost)";
            return new MeterEvent( getDate(), MeterEvent.POWERUP, d );
        }
        if(spa == 3 && spq == 0) {
            d = "power failure within integration period.";
            return new MeterEvent( getDate(), MeterEvent.POWERDOWN, d );
        }
        if(spa==7 && spq == 9) {
            d = "time change, DST hour retard";
            return new MeterEvent( getDate(), MeterEvent.SETCLOCK, d );
        }
        if(spa==7 && spq==11) {
            d = "time change, DST hour advance";
            return new MeterEvent( getDate(), MeterEvent.SETCLOCK, d );
        }
        if(spa==7 && spq==2) {
            d= "desincronizacion manufacturer documentation spa=7, spq=2";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==15 && spq==0) {
            d="configuration change";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==16 && spq==0) {
            d="change of the private key";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==18 && spq==1) {
            d="intrusion";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==18 && spq==2){
            d="establishing connection with CM";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==18 && spq==3) {
            d="establishing connection with TPL";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==18 && spq==4) {
            d="communication with gps";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==19 ) {
            d="internal error " + spq;
            return new MeterEvent( getDate(), MeterEvent.FATAL_ERROR, d );
        }
        if(spa==15 && spq==21) {
            d="configuration change contract 1";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==22) {
            d="configuration change contract 2";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==23) {
            d="configuration change contract 3";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==7 && spq==21) {
            d="billing period reset for contract 1";
            return new MeterEvent( getDate(), MeterEvent.BILLING_ACTION, d );
        }
        if(spa==7 && spq==22) {
            d="biling period reset for contract 2";
            return new MeterEvent( getDate(), MeterEvent.BILLING_ACTION, d );
        }
        if(spa==7 && spq==23) {
            d="billing period reset for contract 3";
            return new MeterEvent( getDate(), MeterEvent.BILLING_ACTION, d );
        }
        if(spa==18 && spq==21){
            d="communication for contract 1";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==18 && spq==22){
            d="communication for contract 2";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==18 && spq==23){
            d="communication for contract 3";
            return new MeterEvent( getDate(), MeterEvent.OTHER, d );
        }
        if(spa==15 && spq==1){
            d="configuration change, communication settings";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==24) {
            d="configuration change, contract 1 power spa=15 spq=24";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==25) {
            d="configuration change, contract 2 power spa=15 spq=25";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==26) {
            d="configuration change, contract 3 power spa=15 spq=26";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==27) {
            d="configuration change, contract 1 holidays spa=15 spq=27";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==28) {
            d="configuration change, contract 2 holidays spa=15 spq=28";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==15 && spq==29) {
            d="configuration change, contract 3 holidays spa=15 spq=29";
            return new MeterEvent( getDate(), MeterEvent.CONFIGURATIONCHANGE, d );
        }
        if(spa==3 && spq==1) {
            d="Phase failure, phase 1";
            return new MeterEvent( getDate(), MeterEvent.PHASE_FAILURE, d );
        }
        if(spa==3 && spq==2) {
            d="Phase failure, phase 2";
            return new MeterEvent( getDate(), MeterEvent.PHASE_FAILURE, d );
        }
        if(spa==3 && spq==3) {
            d="Phase failure, phase 3";
            return new MeterEvent( getDate(), MeterEvent.PHASE_FAILURE, d );
        }

        return null;
    }

    public String toString(){
        if( toMeterEvent() != null )
            return toMeterEvent().toString() + " " + getDate();
        else
            return "unknown event spa" + spa + " , spq " + spq + " ,spi " + spi;
    }

}
