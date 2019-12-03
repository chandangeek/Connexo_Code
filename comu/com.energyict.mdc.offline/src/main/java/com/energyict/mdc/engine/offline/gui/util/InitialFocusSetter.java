/*
 * InitialFocusSetter.java
 *
 * Created on 6 januari 2006, 10:38
 */

package com.energyict.mdc.engine.offline.gui.util;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class InitialFocusSetter {

    public static void setInitialFocus(Window w, Component c) {
        w.addWindowListener(new FocusSetter(c));
    }

    public static class FocusSetter extends WindowAdapter {

        Component initComp;

        FocusSetter(Component c) {
            initComp = c;
        }

        public void windowOpened(WindowEvent e) {
            initComp.requestFocus();

            // Since this listener is no longer needed, remove it
            e.getWindow().removeWindowListener(this);
        }
    }
}
