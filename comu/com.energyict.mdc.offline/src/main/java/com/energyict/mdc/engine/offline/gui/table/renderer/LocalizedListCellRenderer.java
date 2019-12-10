/*
 * LocalizedListCellRenderer.java
 *
 * Created on 7 oktober 2003, 10:36
 */

package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.energyict.mdc.engine.offline.UserEnvironment;
import com.energyict.mdc.engine.offline.core.TranslatorProvider;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author Geert
 */
public class LocalizedListCellRenderer extends DefaultListCellRenderer {

    private boolean errorIndication;
    private String languageBundle;

    /**
     * Creates a new instance of LocalizedListCellRenderer
     */
    public LocalizedListCellRenderer() {
        this(null, true);
    }

    public LocalizedListCellRenderer(boolean errorIndication) {
        this(null, errorIndication);
    }

    public LocalizedListCellRenderer(String languageBundle, boolean errorIndication) {
        this.languageBundle = languageBundle;
        this.errorIndication = errorIndication;
        this.setBorder(new EmptyBorder(0, 3, 0, 3));
    }

    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        if (value == null) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        String displayString = (languageBundle == null) ?
                TranslatorProvider.instance.get().getTranslator().getTranslation(value.toString(), errorIndication) :
                UserEnvironment.getDefault().getMsg(languageBundle, value.toString(), errorIndication);
        return super.getListCellRendererComponent(
                list, displayString, index, isSelected, cellHasFocus);
    }
}
