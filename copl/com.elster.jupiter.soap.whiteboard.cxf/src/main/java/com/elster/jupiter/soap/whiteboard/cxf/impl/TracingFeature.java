package com.elster.jupiter.soap.whiteboard.cxf.impl;

import org.apache.cxf.feature.LoggingFeature;

import java.io.File;
import java.net.URI;

/**
 * Created by dvy on 5/07/2016.
 */
public class TracingFeature extends LoggingFeature {
    public TracingFeature(String directory, String file) {
        super(new File(directory + file).toURI().toString(), new File(directory + file).toURI().toString());
    }
}
