/*
 * TemplateCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import java.io.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;

/**
 *
 * @author Koen
 */
public class TemplateCommand extends AbstractCommand {
    
    /** Creates a new instance of ForceStatusCommand */
    public TemplateCommand(CommandFactory cm) {
        super(cm);
    }
    
    // =0x"+Integer.toHexString(
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TemplateCommand(null)));
    }          
    
    protected void parse(byte[] data) throws IOException {
    }
    
    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('?');
    }
    
//    protected byte[] prepareData() throws IOException {
//        return null;
//    }
    
}
