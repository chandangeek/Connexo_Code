/*
 * DialogLocalizer.java
 *
 * Created on 13 december 2002, 12:02
 */

package com.energyict.mdc.engine.offline.gui.windows;

import java.awt.*;

/**
 * @author Koen
 */
public class DialogLocalizer {

    /**
     * Creates a new instance of DialogLocalizer
     */
    public DialogLocalizer() {

    }

    static public void place(java.awt.Frame parent, java.awt.Dialog child) {
        if (parent != null) {
            Dimension parentSize = parent.getSize();
            Point P = parent.getLocation();
            child.setLocation((int) (P.x + Math.abs((parentSize.getWidth() / 2) - (child.getWidth() / 2))),
                    (int) (P.y + Math.abs((parentSize.getHeight() / 2) - (child.getHeight() / 2))));
        }
    }
}
