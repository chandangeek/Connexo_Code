/*
 * ExitAction.java
 *
 * Created on 2 juli 2003, 16:10
 */

package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.OfflineEngine;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * @author Geert
 */
public class ExitAction extends AbstractAction {

    /**
     * Creates a new instance of ExitAction
     */
    public ExitAction(OfflineFrame mainFrame) {
        putValue(Action.NAME, TranslatorProvider.instance.get().getTranslator().getTranslation("exitComServerOffline"));
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E)); // Internationalization?
        putValue(ActionKeys.MAIN_FRAME, mainFrame);
    }

    public void actionPerformed(ActionEvent event) {
        OfflineFrame mainFrame = ((OfflineFrame) getValue(ActionKeys.MAIN_FRAME));
        OfflineEngine.exitSystem(0);
    }
}
