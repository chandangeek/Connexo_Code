/*
 * RefreshOwner.java
 *
 * Created on 24 november 2004, 10:13
 */

package com.energyict.mdc.engine.offline.gui.util;

import com.energyict.mdc.engine.offline.gui.windows.EisInternalFrame;

import javax.swing.*;


/**
 * @author Geert
 */
public interface RefreshOwner {

    void refresh();

    EisInternalFrame getParentFrame();

    JTree getTree();

}
