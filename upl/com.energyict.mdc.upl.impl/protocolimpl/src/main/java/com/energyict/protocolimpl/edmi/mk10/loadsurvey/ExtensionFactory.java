/*
 * ExtensionFactory.java
 *
 * Created on 31 maart 2006, 13:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.loadsurvey;

import java.io.*;

import com.energyict.protocolimpl.edmi.mk10.command.*;
/**
 *
 * @author koen
 */
public class ExtensionFactory {
    
    private Extension[] extensions;
    private int nrOfLoadedExtensions;
    CommandFactory commandFactory;
    
    
    /** Creates a new instance of ExtensionFactory */
    public ExtensionFactory(CommandFactory commandFactory) throws IOException {
        this.commandFactory=commandFactory;
        init();
    }
    
    private void init() throws IOException {

		// There are 7 profiles available
    	// 	* SystemLog
    	//	* AccesLog
    	//  * TamperLog
    	//  * DiagnosticsLog
    	//	* SagSwell
    	//	* LoadSurvey1
    	//	* LoadSurvey2

    	nrOfLoadedExtensions = 7;
		setExtensions(new Extension[nrOfLoadedExtensions]);

		//TODO Aanpassen
		getExtensions()[0] = new Extension(0x0000,"SystemLog");
		getExtensions()[1] = new Extension(0x0000,"AccesLog");
		getExtensions()[2] = new Extension(0x0000,"TamperLog");
		getExtensions()[3] = new Extension(0x0000,"DiagnosticsLog");
		getExtensions()[4] = new Extension(0x0000,"SagSwell");
		getExtensions()[5] = new Extension(0xD800,"LoadSurvey1");
		getExtensions()[6] = new Extension(0xD801,"LoadSurvey2");

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
            if (extension>0) strBuff.append(", ");
            strBuff.append(getExtensions()[extension].getName());
        }
        return strBuff.toString();
    }
}
