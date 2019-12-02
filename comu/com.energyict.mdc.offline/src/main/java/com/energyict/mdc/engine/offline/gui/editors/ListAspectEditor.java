package com.energyict.mdc.engine.offline.gui.editors;


import com.energyict.mdc.engine.offline.gui.table.renderer.ValuesCellRenderer;

import javax.swing.*;
import java.util.List;

public class ListAspectEditor extends JComboBoxAspectEditor {

    public ListAspectEditor(List<?> list){
        super(new DefaultComboBoxModel(list.toArray()));
        getValueComponent().setRenderer(new ValuesCellRenderer() );
    }
}