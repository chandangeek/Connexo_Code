package com.energyict.mdc.engine.offline.gui.dialogs;

import com.energyict.mdc.engine.offline.OfflineExecuter;
import com.energyict.mdc.engine.offline.core.Utils;

/**
 * Extends the ExceptionDialog that serves as a dialog to show every unhandled exception.
 * This adds functionality to log the exception message & stacktrace to the log file before showing the exception window.
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 1/10/2014 - 11:40
 */
public class LoggingExceptionDialog extends ExceptionDialog {

    private OfflineExecuter offlineExecuter = null;

    public void setOfflineExecuter(OfflineExecuter offlineExecuter) {
        this.offlineExecuter = offlineExecuter;
    }

    /**
     * Log the stacktrace in the global log file, then show it in the UI
     */
    @Override
    public void handle(Throwable throwable) {
        if (offlineExecuter != null && offlineExecuter.getLogging() != null) {
            offlineExecuter.getLogging().getLogger().severe(throwable.toString() + ", " + Utils.stack2string(throwable));
        }
        super.handle(throwable);
    }
}
