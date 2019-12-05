/*
 * FileSaveTasksAction.java
 *
 * Created on 30 september 2003, 10:38
 */

package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Koen
 */
public class FileSaveTasksAction extends AbstractAction {
    private static final Log logger = LogFactory.getLog(FileSaveTasksAction.class);

    /**
     * Creates a new instance of ExitAction
     */
    public FileSaveTasksAction(OfflineFrame mainFrame) {
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("saveTasksFile"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S)); // Internationalization?
        putValue(ActionKeys.MAIN_FRAME, (Object) mainFrame);
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent event) {
        logger.debug("KV_DEBUG>save tasks file, not yet implemented...");
    }
}
