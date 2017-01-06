/*
 * InputStreamMonitor.java
 *
 * Created on 6 oktober 2002, 13:12
 */

package com.energyict.protocol.tools;

import java.util.logging.Logger;

/**
 * @author Karel
 */
public class InputStreamMonitor implements InputStreamObserver, OutputStreamObserver {

    private static final Logger logger = Logger.getLogger(InputStreamMonitor.class.getName());

    @Override
    public void read(byte[] b) {
        logger.info("Read " + b.length + " bytes");
        for (int i = 0; i < b.length; i++) {
            logger.info(Integer.toHexString(b[i]));
        }
        logger.info(new String(b));
    }

    @Override
    public void wrote(byte[] b) {
        logger.info("Wrote " + b.length + " bytes");
        for (int i = 0; i < b.length; i++) {
            logger.info(Integer.toHexString(b[i]));
        }
        logger.info(new String(b));
    }

    @Override
    public void threw(Throwable ex) {
    }

}