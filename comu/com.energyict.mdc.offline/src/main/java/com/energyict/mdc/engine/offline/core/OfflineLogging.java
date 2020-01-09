package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.engine.offline.OfflineExecuter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p/>
 * <B>Description :</B><BR>
 * Wrapper class for the logging mechanisms used by CommServer.<BR>
 * CommServerLog.txt.0 and CommServerLog.txt.1 are the global logfiles with each the max size
 * as given in the properties COMMSERVER_LOGFILESIZE in bytes.<BR>
 * PR_x_y where x=ProtocolReaderID and y=time/date of creation in ms. These are separate logfiles for each communicationsession of a ProtocolReader. When
 * COMMSERVER_PROTOCOLLOG=0, these logfiles will not be created.
 * <BR>
 * <B>Changes :</B><BR>
 * KV 15052002 Initial version
 * KV 17022004 Solved bug no logfile string return when protocol loglevel = Level.OFF
 *
 * @author Koenraad Vanderschaeve
 * @version 1.0
 */
public class OfflineLogging {

    private static final Log logger = LogFactory.getLog(OfflineLogging.class);

    /**
     * The reference to the global Logger (if we don't declare it, it might get garbage collected)
     */
    private Logger globalLogger;

    OfflineExecuter publisher;
    String strLoggerName = null;

    /**
     * Class constructor.
     */
    public OfflineLogging(OfflineExecuter publisher, String strLoggerName) {
        this.publisher = publisher;
        this.strLoggerName = strLoggerName;

    }

    public void log(Level level, String str) {
        getLogger().log(level, str);
    }

    public void createIndividualLogDirectories(String dir1) {
        // Check directory and create if not exist
        File directory;
        directory = new File(dir1);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void createGlobalLogDirectory(String dir1) {
        // Check directory and create if not exist
        File directory;
        directory = new File(dir1);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Method to create the globallogging file.
     *
     * @param logLevel     Log level as given with the property COMMSERVER_LOGLEVEL.
     * @param iLogFileSize log file size as given with the property COMMSERVER_LOGFILESIZE.
     */
    public void initGlobalLogging(Level logLevel, int iLogFileSize, String strFileName) {
        // Remove the Comsolehandler from the parentlogger.
        if (getLogger().getParent().getHandlers().length >= 1) {
            getLogger().getParent().removeHandler(getLogger().getParent().getHandlers()[0]);
        }

        // Logging to a txt file, using simpleformatter.
        try {
            Handler fhSimple;
            Handler hSimple;
            if (iLogFileSize == 0) {
                fhSimple = new FileHandler(strFileName, true);
            } else {
                fhSimple = new FileHandler(strFileName, iLogFileSize, 2, true); // 2 files
            }
            hSimple = new ScreenHandler(publisher);

            getLogger().addHandler(fhSimple);
            getLogger().addHandler(hSimple);

            getLogger().setLevel(logLevel);

            fhSimple.setFormatter(new FileFormatter());
            fhSimple.setLevel(logLevel);
            hSimple.setFormatter(new ScreenFormatter(strLoggerName));
            hSimple.setLevel(logLevel);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Method to close the global log handle.
     */
    public void closeGlobalLogging() {
        int nrOfHandlers = getLogger().getHandlers().length;
        for (int i = 0; i < nrOfHandlers; i++) {
            Handler handler = getLogger().getHandlers()[0];
            getLogger().removeHandler(getLogger().getHandlers()[0]);
            handler.close();
        }
    }

    /**
     * Get the global logger.
     *
     * @return Logger
     */
    public Logger getLogger() {
        if (this.globalLogger == null) {
            this.globalLogger = Logger.getLogger(strLoggerName);
        }
        return this.globalLogger;
    }
}