package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.firmware.FirmwareVersion;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/05/2016 - 11:53
 */
public class FirmwareCache {

    private static final String TEMP_DIR = "java.io.tmpdir";

    /**
     * We lock the critical section where we write the firmware file, making sure that we don't corrupt it.
     */
    private static final Lock firmwareFileLock = new ReentrantLock();


    public static File findOrCreateTempFile(final FirmwareVersion firmwareVersion) {

        firmwareFileLock.lock();
        try {

            final File tempFile = new File(System.getProperty(TEMP_DIR), "firmware-" + firmwareVersion.getId() + "-" + firmwareVersion.getModTime().getEpochSecond() + ".tmp");

            if (!tempFile.exists()) {

                final boolean created = tempFile.createNewFile();
                if (!created) {
                    throw new IllegalStateException("Could not create temporary file [" + tempFile + "] : create() returns false !");
                }

                tempFile.deleteOnExit();    //Make sure the temp file is removed if this JVM is shut down

                try (OutputStream outStream = new FileOutputStream(tempFile)) {
                    InputStream inputStream = firmwareVersion.getFirmwareFileAsStream();
                    final byte[] buffer = new byte[2048];
                    try {
                        int bytesRead = inputStream.read(buffer);

                        while (bytesRead != -1) {
                            outStream.write(buffer, 0, bytesRead);
                            bytesRead = inputStream.read(buffer);
                        }

                        outStream.flush();
                    } catch (IOException e) {
                        throw new IllegalStateException("IO error while writing temporary file : [" + e.getMessage() + "]", e);
                    }
                }
            }

            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Error while writing temporary file : [" + e.getMessage() + "]", e);
        } finally {
            firmwareFileLock.unlock();
        }
    }
}