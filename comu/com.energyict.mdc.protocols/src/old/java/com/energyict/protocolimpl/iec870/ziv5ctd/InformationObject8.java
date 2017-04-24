/*
 * InformationObject8.java
 *
 * Created on 7 april 2006, 9:49
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.common.interval.IntervalStateBits;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;

import com.energyict.cbo.Quantity;

import java.util.Date;

/** @author fbo */

public class InformationObject8 extends InformationObject {

    private Date date;

    private Quantity quanitity1;
    private int status1;
    private Quantity quanitity2;
    private int status2;
    private Quantity quanitity3;
    private int status3;
    private Quantity quanitity4;
    private int status4;
    private Quantity quanitity5;
    private int status5;
    private Quantity quanitity6;
    private int status6;

    /** Creates a new instance of InformationObject8 */
    InformationObject8() { }

    ChannelInfo toChannelInfo( ){
        return null;
    }

    IntervalData toIntervalData( ){
        IntervalData id = new IntervalData(date);
        id.addValue( quanitity1, status1, toEistatus(status1));
        id.addValue( quanitity2, status2, toEistatus(status2));
        id.addValue( quanitity3, status3, toEistatus(status3));
        id.addValue( quanitity4, status4, toEistatus(status4));
        id.addValue( quanitity5, status5, toEistatus(status5));
        id.addValue( quanitity6, status6, toEistatus(status6));
        return id;
    }

    int toEistatus( int ziv ){
        int result = 0;
        if( (ziv & 0x40) > 0 )
            result |= IntervalStateBits.SHORTLONG;
        if( (ziv & 0x20) > 0 )
            result |= IntervalStateBits.OVERFLOW;
        if( (ziv & 0x10) > 0 )
            result |= IntervalStateBits.OTHER;
        if( (ziv & 0x8) > 0 )
            result |= IntervalStateBits.CONFIGURATIONCHANGE;
        if( (ziv & 0x4) > 0 )
            result |= IntervalStateBits.OTHER;
        if( (ziv & 0x2) > 0 )
            result |= IntervalStateBits.POWERDOWN;
        return result;
    }

    Date getDate() {
        return date;
    }

    void setDate(Date date) {
        this.date = date;
    }

    Quantity getQuanitity1() {
        return quanitity1;
    }

    void setQuanitity1(Quantity quanitity1) {
        this.quanitity1 = quanitity1;
    }

    Quantity getQuanitity2() {
        return quanitity2;
    }

    void setQuanitity2(Quantity quanitity2) {
        this.quanitity2 = quanitity2;
    }

    Quantity getQuanitity3() {
        return quanitity3;
    }

    void setQuanitity3(Quantity quanitity3) {
        this.quanitity3 = quanitity3;
    }

    Quantity getQuanitity4() {
        return quanitity4;
    }

    void setQuanitity4(Quantity quanitity4) {
        this.quanitity4 = quanitity4;
    }

    Quantity getQuanitity5() {
        return quanitity5;
    }

    void setQuanitity5(Quantity quanitity5) {
        this.quanitity5 = quanitity5;
    }

    Quantity getQuanitity6() {
        return quanitity6;
    }

    void setQuanitity6(Quantity quanitity6) {
        this.quanitity6 = quanitity6;
    }

    int getStatus1() {
        return status1;
    }

    void setStatus1(int status1) {
        this.status1 = status1;
    }

    int getStatus2() {
        return status2;
    }

    void setStatus2(int status2) {
        this.status2 = status2;
    }

    int getStatus3() {
        return status3;
    }

    void setStatus3(int status3) {
        this.status3 = status3;
    }

    int getStatus4() {
        return status4;
    }

    void setStatus4(int status4) {
        this.status4 = status4;
    }

    int getStatus5() {
        return status5;
    }

    void setStatus5(int status5) {
        this.status5 = status5;
    }

    int getStatus6() {
        return status6;
    }

    void setStatus6(int status6) {
        this.status6 = status6;
    }

}
