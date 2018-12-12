/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.edmi.mk6.profiles;

import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.ReadCommand;
import com.energyict.protocolimpl.edmi.mk6.registermapping.MK6RegisterInformation;

import java.io.IOException;

/**
 * @author koen
 */
public class ExtensionFactory {

    private Extension[] extensions;
    private int nrOfLoadedExtensions;
    CommandFactory commandFactory;

    public ExtensionFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
        init();
    }

    private void init() {
        nrOfLoadedExtensions = commandFactory.getReadCommand(MK6RegisterInformation.NUMBER_OF_LOADED_EXTENSIONS).getRegister().getBigDecimal().intValue();
        initializeExtensions(nrOfLoadedExtensions);
    }

    public void initializeExtensions(int nrOfLoadedExtensions) {
        this.extensions = new Extension[nrOfLoadedExtensions];
    }

    public int getNrOfLoadedExtensions() {
        return nrOfLoadedExtensions;
    }

    /**
     * Getter for the array of Extensions <br/>
     * Remark: note that this list is lazy filled up, or in other words:
     * extensions are loaded in up to the point the needed extension is found,
     * skipping the read of other extensions.
     * @return
     */
    private Extension[] getExtensions() {
        return extensions;
    }

    private Extension getExtension(int nr) {
        if (getExtensions()[nr] == null) {
            String name = getReadCommand(MK6RegisterInformation.EXTENSION_NAME, nr).getRegister().getString();
            int registerId = getReadCommand(MK6RegisterInformation.REGISTER_ID_OF_EXTENSION, nr).getRegister().getBigDecimal().intValue();
            getExtensions()[nr] = new Extension(registerId, name);
        }
        return getExtensions()[nr];
    }

    private ReadCommand getReadCommand(MK6RegisterInformation registerIdOfExtension, int extensionNr) {
        return commandFactory.getReadCommand(registerIdOfExtension.getRegisterId() + extensionNr, registerIdOfExtension.getDataType());
    }


    public LoadSurvey findLoadSurvey(String name) throws IOException {
        for (int extension = 0; extension < getNrOfLoadedExtensions(); extension++) {
            if (getExtension(extension).getName().toLowerCase().replaceAll(" ", "").contains(name.toLowerCase().replaceAll(" ", ""))) { // Compare lower case without spaces
                return new LoadSurvey(commandFactory, getExtensions()[extension].getRegisterId());
            }
        }
        throw new IOException("ExtensionFactory, findLoadSurvey, load survey with name '" + name + "' not found! Existing extensions are: " + getExtensionNames());

    }

    private String getExtensionNames() {
        StringBuilder strBuff = new StringBuilder();
        for (int extension = 0; extension < getNrOfLoadedExtensions(); extension++) {
            if (extension > 0) {
                strBuff.append(", ");
            }
            strBuff.append(getExtensions()[extension].getName());   // When reaching this point, we are sure all extensions are loaded
        }
        return strBuff.toString();
    }
}
