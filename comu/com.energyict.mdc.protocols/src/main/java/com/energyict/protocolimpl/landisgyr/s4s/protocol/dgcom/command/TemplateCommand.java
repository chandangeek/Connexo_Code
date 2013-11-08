/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import java.io.*;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;


/**
 *
 * @author Koen
 */
public class TemplateCommand extends AbstractCommand {
    
    /** Creates a new instance of TemplateCommand */
    public TemplateCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }
    
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TemplateCommand(null)));
    } 
    
    protected byte[] prepareBuild() throws IOException {
        return new byte[]{(byte)0x8F,0,0,0,0,0,0,0,0};
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
    }
}
