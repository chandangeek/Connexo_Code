/*
 * IDCommand.java
 *
 * Created on 9 augustus 2005, 13:31
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
public class IDCommand extends AbstractCommand {

    private static final CommandIdentification commandIdentification = new CommandIdentification("ID");

    private String idCode1;

    private String idCode2;

    private String serialNr;

    /** Creates a new instance of IDCommand */
    public IDCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    protected void parse(String strData) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(strData));
        setIdCode1(br.readLine());
        setIdCode2(br.readLine());
        setSerialNr(br.readLine().trim());
    }

    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public String getIdCode1() {
        return idCode1;
    }

    public void setIdCode1(String idCode1) {
        this.idCode1 = idCode1;
    }

    public String getIdCode2() {
        return idCode2;
    }

    public void setIdCode2(String idCode2) {
        this.idCode2 = idCode2;
    }

    public String getSerialNr() {
        return serialNr;
    }

    public void setSerialNr(String serialNr) {
        this.serialNr = serialNr;
    }

} // public class IDCommand extends AbstractCommand
