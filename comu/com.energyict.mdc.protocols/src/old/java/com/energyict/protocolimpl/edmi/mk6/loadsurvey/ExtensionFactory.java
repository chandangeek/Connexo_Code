/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ExtensionFactory.java
 *
 * Created on 31 maart 2006, 13:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.loadsurvey;

import com.energyict.protocolimpl.edmi.mk6.command.CommandFactory;

import java.io.IOException;
import java.io.Serializable;
/**
 *
 * @author koen
 */
public class ExtensionFactory implements Serializable{

	/** Generated SerialVersionUID */
	private static final long serialVersionUID = -5199838318963041028L;
	private Extension[] extensions;
    private int nrOfLoadedExtensions;
    CommandFactory commandFactory;


    /** Creates a new instance of ExtensionFactory */
    public ExtensionFactory(CommandFactory commandFactory) throws IOException {
        this.commandFactory=commandFactory;
        init();
    }

    private void init() throws IOException {
        nrOfLoadedExtensions = commandFactory.getReadCommand(0x0002F001).getRegister().getBigDecimal().intValue();
        setExtensions(new Extension[nrOfLoadedExtensions]);
        for (int extension = 0;extension < getNrOfLoadedExtensions(); extension++) {
            String name = commandFactory.getReadCommand(0x00020000+extension).getRegister().getString();
            int registerId = commandFactory.getReadCommand(0x00021000+extension).getRegister().getBigDecimal().intValue();
            getExtensions()[extension] = new Extension(registerId,name);
        }
    }

    public int getNrOfLoadedExtensions() {
        return nrOfLoadedExtensions;
    }

    public Extension[] getExtensions() {
        return extensions;
    }

    private void setExtensions(Extension[] extensions) {
        this.extensions = extensions;
    }


    public LoadSurvey findLoadSurvey(String name) throws IOException {
        for (int extension = 0;extension < getNrOfLoadedExtensions(); extension++) {
            if (getExtensions()[extension].getName().indexOf(name)>=0) {
                return new LoadSurvey(commandFactory, getExtensions()[extension].getRegisterId());
            }
        }
        throw new IOException("ExtensionFactory, findLoadSurvey, load survey with name '"+name+"' not found! Existing extensions are: "+getExtensionNames());

    }

    private String getExtensionNames() {
        StringBuffer strBuff = new StringBuffer();
        for (int extension = 0;extension < getNrOfLoadedExtensions(); extension++) {
            if (extension>0) {
				strBuff.append(", ");
			}
            strBuff.append(getExtensions()[extension].getName());
        }
        return strBuff.toString();
    }
}
