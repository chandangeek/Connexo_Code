package com.energyict.mdc.engine.offline.gui;

import com.energyict.mdc.engine.offline.gui.windows.EisInternalFrame;

import javax.swing.*;

public abstract class EisMenuBarBuilder implements MenuBarBuilder {

    protected JMenuBar menuBar;

    public EisMenuBarBuilder() {
        menuBar = newMenuBar();
    }

    public abstract void initMenuBar(EisInternalFrame frame);

    // to be overriden by specialized classes
    public void refreshMenuBar() {
        // Do nothing
    }

    public void addMenu(JMenu menu, int index) {
        menuBar.add(menu, index);
    }

    public void addMenu(JMenu menu) {
        menuBar.add(menu);
    }

    private JMenuBar newMenuBar() {
        return new JMenuBar();
    }

}
