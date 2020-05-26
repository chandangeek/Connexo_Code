package com.elster.jupiter.bootstrap.logging.impl;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class CustomOsgiLogHandler extends FileHandler {
    public CustomOsgiLogHandler() throws IOException {
        super();
        setFormatter(new SimpleFormatter());
    }
}
