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

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TemplateCommand extends AbstractCommand {

    /** Creates a new instance of ForceStatusCommand */
    public TemplateCommand(CommandFactory cm) {
        super(cm);
    }

    protected void parse(byte[] data) throws IOException {
    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('?');
    }

}
