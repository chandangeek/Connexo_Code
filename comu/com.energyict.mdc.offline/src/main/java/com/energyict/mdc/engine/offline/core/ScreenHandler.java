/*
 * ScreenHandler.java
 *
 * Created on 2 januari 2003, 16:01
 */

package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.engine.offline.OfflineExecuter;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Koen
 */
public class ScreenHandler extends Handler {

    OfflineExecuter publisher;

    public ScreenHandler() {
        super();
        this.publisher = null;
    }

    public ScreenHandler(OfflineExecuter publisher) {
        this();
        this.publisher = publisher;
    }

    public void setPublisher(OfflineExecuter publisher) {
        this.publisher = publisher;
    }

    /**
     * Close the <tt>Handler</tt> and free all associated resources.
     * <p/>
     * The close method will perform a <tt>flush</tt> and then close the
     * <tt>Handler</tt>.   After close has been called this <tt>Handler</tt>
     * should no longer be used.  Method calls may either be silently
     * ignored or may throw runtime exceptions.
     *
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have <tt>LoggingPermission("control")</tt>.
     */
    public void close() throws SecurityException {

    }

    /**
     * Flush any buffered output.
     */
    public void flush() {
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p/>
     * The logging request was made initially to a <tt>Logger</tt> object,
     * which initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p/>
     * The <tt>Handler</tt>  is responsible for formatting the message, when and
     * if necessary.  The formatting should include localization.
     *
     * @param record description of the log event
     */
    public void publish(LogRecord record) {

        ScreenFormatter formatter = (ScreenFormatter) getFormatter();
        String str;

        if (formatter != null) {
            str = formatter.format(record);
        } else {
            str = record.getMessage();
        }

        //TODO write logging in some file... original impl is the line below
        //if (publisher != null) publisher.writeMessage(formatter.getID(), str);

    } // public void publish(LogRecord record)
} // public class ScreenHandler extends Handler
