package com.elster.connexo.user;

import com.elster.jupiter.launcher.SingleLineFormatter;

import java.io.IOException;
import java.util.logging.FileHandler;

/**
 * Created by albertv on 5/17/2016.
 */
public class Access extends FileHandler {

    public Access() throws IOException, SecurityException {
        super();
        if(this.getFormatter() != null){
            this.setFormatter(new SingleLineFormatter());
        }
    }
}
