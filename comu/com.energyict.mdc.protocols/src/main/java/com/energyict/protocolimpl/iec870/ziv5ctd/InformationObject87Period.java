/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InformationObject19.java
 *
 * Created on 11 april 2006, 16:15
 *
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import com.energyict.mdc.common.Quantity;

import java.util.Date;

/**A Period is a TOU within a Contract
 *
 * @author fbo */

public class InformationObject87Period extends InformationObject {

    private int infoAddress;

    private Quantity absoluteA;
    private Quantity incrementalA;
    //
    private Quantity absoluteRi;
    private Quantity incrementalRi;
    //
    private Quantity absoluteRc;
    private Quantity incrementalRc;
    //
    private Quantity reserva7;
    //
    private Quantity reserva8;
    //
    private Quantity maxPotentia;
    private Date maxPotentiaDate;
    //
    private Date startPeriod;
    private Date endPeriod;

    public InformationObject87Period() { }

    int getInfoAddress() {
        return infoAddress;
    }

    void setInfoAddress(int infoAddress) {
        this.infoAddress = infoAddress;
    }

    Quantity getAbsoluteA() {
        return absoluteA;
    }

    void setAbsoluteA(Quantity absoluteA) {
        this.absoluteA = absoluteA;
    }

    Quantity getIncrementalA() {
        return incrementalA;
    }

    void setIncrementalA(Quantity incrementalA) {
        this.incrementalA = incrementalA;
    }

    Quantity getAbsoluteRi() {
        return absoluteRi;
    }

    void setAbsoluteRi(Quantity absoluteRi) {
        this.absoluteRi = absoluteRi;
    }

    Quantity getIncrementalRi() {
        return incrementalRi;
    }

    void setIncrementalRi(Quantity incrementalRi) {
        this.incrementalRi = incrementalRi;
    }

    Quantity getAbsoluteRc() {
        return absoluteRc;
    }

    void setAbsoluteRc(Quantity absoluteRc) {
        this.absoluteRc = absoluteRc;
    }

    Quantity getIncrementalRc() {
        return incrementalRc;
    }

    void setIncrementalRc(Quantity incrementalRc) {
        this.incrementalRc = incrementalRc;
    }

    Quantity getReserva7() {
        return reserva7;
    }

    void setReserva7(Quantity reserva7) {
        this.reserva7 = reserva7;
    }

    Quantity getReserva8() {
        return reserva8;
    }

    void setReserva8(Quantity reserva8) {
        this.reserva8 = reserva8;
    }

    Quantity getMaxPotentia() {
        return maxPotentia;
    }

    void setMaxPotentia(Quantity maxPotentia) {
        this.maxPotentia = maxPotentia;
    }

    Date getMaxPotentiaDate() {
        return maxPotentiaDate;
    }

    void setMaxPotentiaDate(Date maxPotentiaDate) {
        this.maxPotentiaDate = maxPotentiaDate;
    }

    Date getStartPeriod() {
        return startPeriod;
    }

    void setStartPeriod(Date startPeriod) {
        this.startPeriod = startPeriod;
    }

    Date getEndPeriod() {
        return endPeriod;
    }

    void setEndPeriod(Date endPeriod) {
        this.endPeriod = endPeriod;
    }

    public String toString( ){
        return "InformationObject19["
                + " infoAddress " + getInfoAddress() + ", \n"
                + " absoluteA " + getAbsoluteA() + ", \n"
                + " incrementalA " + getIncrementalA() + ", \n"
                + " absoluteRi " + getAbsoluteRi() + ", \n"
                + " incrementalRi " + getIncrementalRi() + ", \n"
                + " absoluteRc " + getAbsoluteRc() + ", \n"
                + " incrementalRc " + getIncrementalRc() + ", \n"
                + " reserva7 " + getReserva7() + ", \n"
                + " reserva8 " + getReserva8() + ", \n"
                + " maxPotentia " + getMaxPotentia() + ", \n"
                + " maxPotentiaDate " + getMaxPotentiaDate() + ", \n"
                + " startPeriod " + getStartPeriod() + ", \n"
                + " endPeriod " + getEndPeriod()
                + "]";
    }

}
