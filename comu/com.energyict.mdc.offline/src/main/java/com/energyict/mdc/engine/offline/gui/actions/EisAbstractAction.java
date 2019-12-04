/*
 * EisAbstractAction.java
 *
 * Created on 16 januari 2004, 11:04
 */

package com.energyict.mdc.engine.offline.gui.actions;

import com.energyict.mdc.engine.offline.core.TranslatorProvider;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.util.Refresher;
import com.energyict.mdc.engine.offline.gui.windows.EisInternalFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geert
 */
public class EisAbstractAction extends AbstractAction {

    List objects = null;
    boolean resetAtEnd = true;
    private JFrame mainWindow = null;

    /**
     * Creates a new instance of EisAction
     */
    public EisAbstractAction() {
    }

    public EisAbstractAction(EisInternalFrame frame) {
        setFrame(frame);
    }

    public void setFrame(EisInternalFrame frame) {
        putValue(ActionKeys.EISINTERNAL_FRAME, frame);
    }

    public void setName(String name) {
        putValue(Action.NAME, name);
    }

    public String getName() {
        return (String) getValue(Action.NAME);
    }

    public void setMnemonic(Integer key) {
        putValue(Action.MNEMONIC_KEY, key);
    }

    public void setAccelerator(KeyStroke key) {
        putValue(Action.ACCELERATOR_KEY, key);
    }

    public void setTooltip(String tooltip) {
        putValue(Action.SHORT_DESCRIPTION, tooltip);
    }

    public void setObject(Object object) {
        objects = new ArrayList();
        objects.add(object);
    }

    public void setObjects(List objects) {
        this.objects = new ArrayList(objects);
    }

    public void setRefresher(Refresher refresher) {
        putValue(ActionKeys.REFRESHER, refresher);
    }

    public void setResetAtEnd(boolean resetAtEnd) {
        this.resetAtEnd = resetAtEnd;
    }

    public Object getObject() {
        if (objects == null || objects.isEmpty()) {
            return null;
        }
        return objects.get(0);
    }

    public List getObjects() {
        return objects;
    }

    public EisInternalFrame getFrame() {
        return (EisInternalFrame) getValue(ActionKeys.EISINTERNAL_FRAME);
    }

    public Refresher getRefresher() {
        return (Refresher) getValue(ActionKeys.REFRESHER);
    }

    public void reset() {
//        objects = null;
//        setRefresher(null);
    }

    // Can be overwritten if you won't want a wait cursor at this moment

    public void actionPerformed(ActionEvent event) {
        EisInternalFrame theFrame = getFrame();
        if (theFrame != null) {
            theFrame.startWaitCursor();
        }
        try {
            doAction(event);
        } finally {
            if (theFrame != null) {
                theFrame.stopWaitCursor();
            }
            if (resetAtEnd) {
                reset();
            }
        }
    }

    // to be implemented by the derived class of course

    public void doAction(ActionEvent event) {
    }

    public JFrame getMainWindow() {
        if (mainWindow == null) {
            mainWindow = getFrame() != null ?
                    getFrame().getMainWindow() : UiHelper.getMainWindow();
        }
        return mainWindow;
    }

    public String translate(String translationKey) {
        return TranslatorProvider.instance.get().getTranslator().getTranslation(translationKey);
    }
}