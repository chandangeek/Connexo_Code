/*
 * InputStreamMonitor.java
 *
 * Created on 6 oktober 2002, 13:12
 */

package com.energyict.protocols.util;

import com.energyict.mdc.protocol.api.dialer.core.InputStreamObserver;
import com.energyict.mdc.protocol.api.dialer.core.OutputStreamObserver;

import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Karel
 */
public class InputStreamMonitor implements InputStreamObserver, OutputStreamObserver {

    private static final Logger LOGGER = Logger.getLogger(InputStreamMonitor.class.getName());

    /**
     * Creates a new instance of InputStreamMonitor
     */
    public InputStreamMonitor() {
    }

    public void read(byte[] b) {
        LOGGER.info("Read " + b.length + " bytes");
        for (int i = 0; i < b.length; i++) {
            LOGGER.info(Integer.toHexString(b[i]));
        }
        LOGGER.info(new String(b));
    }

    public void wrote(byte[] b) {
        LOGGER.info("Wrote " + b.length + " bytes");
        for (int i = 0; i < b.length; i++) {
            LOGGER.info(Integer.toHexString(b[i]));
        }
        LOGGER.info(new String(b));
    }

    public void threw(Throwable ex) {
    }

    public static void main(String[] args) {
        try {
            File file = new File("eiserver.properties");
            FileInputStream stream = new FileInputStream(file);
            MonitoredInputStream monitoredStream =
                    new MonitoredInputStream(stream, new InputStreamMonitor());
            while (monitoredStream.read() != -1) {
                // System.out.println(line);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
