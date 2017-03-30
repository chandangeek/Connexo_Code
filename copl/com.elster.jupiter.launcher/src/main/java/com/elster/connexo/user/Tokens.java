/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.connexo.user;

import com.elster.jupiter.launcher.SingleLineFormatter;

import java.io.IOException;
import java.util.logging.FileHandler;

/**
 * Created by albertv on 5/17/2016.
 */
public class Tokens extends FileHandler {

    public Tokens() throws IOException, SecurityException {
        super();
        if(this.getFormatter() != null){
            this.setFormatter(new SingleLineFormatter());
        }
    }
}
