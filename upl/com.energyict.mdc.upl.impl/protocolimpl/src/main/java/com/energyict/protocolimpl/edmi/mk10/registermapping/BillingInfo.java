/*
 * BillingInfo.java
 *
 * Created on 27 maart 2006, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.protocolimpl.edmi.common.command.CommandFactory;

import java.util.Date;

/**
 * @author koen
 */
public class BillingInfo {


    private CommandFactory commandFactory;

    private int nrOfBillingResets;
    private Date toDate;
    private Date fromDate;

    /**
     * Creates a new instance of BillingInfo
     */
    public BillingInfo(CommandFactory commandFactory) {
        this.setCommandFactory(commandFactory);
        init();
    }

    private void init() {
        setNrOfBillingResets(getCommandFactory().getReadCommand(MK10Register.NUMBER_OF_BILLING_RESETS).getRegister().getBigDecimal().intValue());
        setToDate(getCommandFactory().getReadCommand(MK10Register.BILLING_RESET_TO_DATE).getRegister().getDate());
        setFromDate(getCommandFactory().getReadCommand(MK10Register.BILLING_RESET_FROM_DATE).getRegister().getDate());
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

    public int getNrOfBillingResets() {
        return nrOfBillingResets;
    }

    private void setNrOfBillingResets(int nrOfBillingResets) {
        this.nrOfBillingResets = nrOfBillingResets;
    }

    public Date getToDate() {
        return toDate;
    }

    private void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getFromDate() {
        return fromDate;
    }
}