/*
 * BillingInfo.java
 *
 * Created on 27 maart 2006, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.common.registermapping;

import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.core.DataType;
import com.energyict.protocolimpl.edmi.mk10.registermapping.MK10RegisterInformation;
import com.energyict.protocolimpl.edmi.mk6.registermapping.MK6RegisterInformation;

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
        setNrOfBillingResets(getCommandFactory().getReadCommand(getNumberOfBillingResetsRegisterId(), DataType.L_LONG).getRegister().getBigDecimal().intValue());
        setToDate(getCommandFactory().getReadCommand(getLastBillingResetDateRegisterId(), DataType.T_TIME_DATE_SINCE__1_97).getRegister().getDate());
        setFromDate(getCommandFactory().getReadCommand(getSecondLastBillingResetDateRegisterId(), DataType.T_TIME_DATE_SINCE__1_97).getRegister().getDate());
    }

    private int getNumberOfBillingResetsRegisterId() {
        return getCommandFactory().getProtocol().isMK10()
                ? MK10RegisterInformation.NUMBER_OF_BILLING_RESETS.getRegisterId()
                : MK6RegisterInformation.NUMBER_OF_BILLING_RESETS.getRegisterId();
    }

    private int getLastBillingResetDateRegisterId() {
        return getCommandFactory().getProtocol().isMK10()
                ? MK10RegisterInformation.LAST_BILLING_RESET_DATE.getRegisterId()
                : MK6RegisterInformation.LAST_BILLING_RESET_DATE.getRegisterId();
    }

    private int getSecondLastBillingResetDateRegisterId() {
        return getCommandFactory().getProtocol().isMK10()
                ? MK10RegisterInformation.SECOND_LAST_BILLING_RESET_DATE.getRegisterId()
                : MK6RegisterInformation.SECOND_LAST_BILLING_RESET_DATE.getRegisterId();
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