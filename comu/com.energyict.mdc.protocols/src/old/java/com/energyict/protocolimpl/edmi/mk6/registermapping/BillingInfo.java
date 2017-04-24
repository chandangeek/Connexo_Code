/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * BillingInfo.java
 *
 * Created on 27 maart 2006, 10:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.registermapping;

import com.energyict.protocolimpl.edmi.mk6.command.CommandFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;



/**
 *
 * @author koen
 */
public class BillingInfo implements Serializable
{

	/** Generated SerialVersionUID */
	private static final long serialVersionUID = -9204872710461066424L;

	private CommandFactory commandFactory;

    private int nrOfBillingResets;
    private Date toDate;
    private Date fromDate;
    /** Creates a new instance of BillingInfo */
    public BillingInfo(CommandFactory commandFactory) throws IOException {
        this.setCommandFactory(commandFactory);
        init();
    }

    public String toString() {
        return "BillingInfo: nrOfBillingResets="+nrOfBillingResets+", toDate="+toDate+", fromDate="+fromDate;
    }


    private void init() throws IOException {
        setNrOfBillingResets(getCommandFactory().getReadCommand(0xF032).getRegister().getBigDecimal().intValue());
        setToDate(getCommandFactory().getReadCommand(0xFC00).getRegister().getDate());
        setFromDate(getNrOfBillingResets()>=2?getCommandFactory().getReadCommand(0xFC01).getRegister().getDate():null);
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

    public Date getFromDate() {
        return fromDate;
    }

    private void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }







}
