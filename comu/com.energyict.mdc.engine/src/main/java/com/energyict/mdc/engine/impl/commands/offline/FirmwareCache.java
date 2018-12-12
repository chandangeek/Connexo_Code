/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.engine.config.HostName;
import com.energyict.mdc.firmware.FirmwareVersion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FirmwareCache {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * We lock the critical section where we write the firmware file, making sure that we don't corrupt it.
     */
    private static final Lock firmwareFileLock = new ReentrantLock();

    /**
     * Find or create the proper temp file for the given FirmwareVersion, in the sub-folder with the host name of the ComServer.
     */
    public static File findOrCreateTempFile(final FirmwareVersion firmwareVersion) {

        firmwareFileLock.lock();
        try {

            String path = TEMP_DIR + FILE_SEPARATOR + HostName.getCurrent() + FILE_SEPARATOR;

            //First find or create the sub folder with the name of this ComServer
            File folder = new File(path);
            if (!folder.exists() || !folder.isDirectory()) {
                boolean mkdir = folder.mkdir();
                if (!mkdir) {
                    throw new IllegalStateException("Could not create folder [" + folder + "] : mkdir() returns false !");
                }
            }

            //Now find or create the temp file representing the given FirmwareVersion
            final File tempFile = new File(path, "firmware-" + firmwareVersion.getId() + "-" + firmwareVersion.getModTime().getEpochSecond() + ".tmp");
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