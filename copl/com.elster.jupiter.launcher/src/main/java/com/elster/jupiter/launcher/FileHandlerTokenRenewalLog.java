package com.elster.jupiter.launcher;

import java.io.IOException;
import java.util.logging.FileHandler;


public class FileHandlerTokenRenewalLog extends FileHandler {

    public FileHandlerTokenRenewalLog() throws IOException, SecurityException {
        super();
        if(this.getFormatter() != null){
            this.setFormatter(new SingleLineFormatter());
        }
    }
}
