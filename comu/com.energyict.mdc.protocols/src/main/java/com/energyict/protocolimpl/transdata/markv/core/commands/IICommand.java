/*
 * IICommand.java
 *
 * Created on 12 augustus 2005, 16:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 *
 * @author koen
 */
public class IICommand extends AbstractCommand {

    private static final CommandIdentification commandIdentification = new CommandIdentification("II");

    private String id;

    /** Creates a new instance of IDCommand */
    public IICommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    protected void parse(String strData) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(strData));
        setId(br.readLine());
    }

    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


} // public class IDCommand extends AbstractCommand