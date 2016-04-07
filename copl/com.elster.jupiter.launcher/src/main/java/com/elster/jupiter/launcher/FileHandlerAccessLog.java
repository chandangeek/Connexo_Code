package com.elster.jupiter.launcher;

import java.io.IOException;
import java.util.logging.FileHandler;


public class FileHandlerAccessLog extends FileHandler {

    public FileHandlerAccessLog() throws IOException, SecurityException {
        super();
        if(this.getFormatter() != null){
            this.setFormatter(new SingleLineFormatter());
        }
    }
}
