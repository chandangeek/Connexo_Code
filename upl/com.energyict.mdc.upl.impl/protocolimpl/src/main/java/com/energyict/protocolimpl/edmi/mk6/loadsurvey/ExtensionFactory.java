package com.energyict.protocolimpl.edmi.mk6.loadsurvey;

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
        setExtensions(new Extension[nrOfLoadedExtensions]);
        for (int extension = 0; extension < getNrOfLoadedExtensions(); extension++) {
            String name = getReadCommand(MK6RegisterInformation.EXTENSION_NAME, extension).getRegister().getString();
            int registerId = getReadCommand(MK6RegisterInformation.REGISTER_ID_OF_EXTENSION, extension).getRegister().getBigDecimal().intValue();
            getExtensions()[extension] = new Extension(registerId, name);
        }
    }

    private ReadCommand getReadCommand(MK6RegisterInformation registerIdOfExtension, int extensionNr) {
        return commandFactory.getReadCommand(registerIdOfExtension.getRegisterId() + extensionNr, registerIdOfExtension.getDataType());
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
        for (int extension = 0; extension < getNrOfLoadedExtensions(); extension++) {
            if (getExtensions()[extension].getName().toLowerCase().replaceAll(" ", "").contains(name.toLowerCase().replaceAll(" ", ""))) { // Compare lower case without spaces
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
            strBuff.append(getExtensions()[extension].getName());
        }
        return strBuff.toString();
    }
}
