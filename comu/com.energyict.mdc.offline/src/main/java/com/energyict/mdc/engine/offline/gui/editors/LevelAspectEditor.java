package com.energyict.mdc.engine.offline.gui.editors;

import javax.swing.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Level;

public class LevelAspectEditor extends JComboBoxAspectEditor<Level> {

    public LevelAspectEditor(){
        super(new DefaultComboBoxModel<>(new Vector<>(Arrays.asList(Level.OFF, Level.SEVERE, Level.WARNING, Level.INFO, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST, Level.ALL))));
        getValueComponent().setRenderer(new LevelListCellRenderer() );
    }

    public void removeLevel(Level level) {
        getValueComponent().removeItem(level);
    }

    public void addLevel(Level level, String name) {
        getValueComponent().addItem(level);
    }
}