package com.energyict.mdc.engine.offline.gui.table.renderer;

import com.elster.jupiter.properties.PropertySpec;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/**
 * User: gde
 * Date: 26/11/12
 */
public class PropertySpecListCellRenderer extends DefaultListCellRenderer {

    private Set<String> requiredPropertyNames;


    public PropertySpecListCellRenderer() {
        this(new HashSet<String>());
    }

    public PropertySpecListCellRenderer(Set<String> requiredPropertyNames) {
        this.requiredPropertyNames = requiredPropertyNames;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if ( value == null || !(value instanceof PropertySpec) ) {
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
        return super.getListCellRendererComponent(list, getDisplayText((PropertySpec)value), index, isSelected, cellHasFocus);
    }

    private String getDisplayText(PropertySpec propertySpec) {
        return " " + propertySpec.getName() + (requiredPropertyNames.contains(propertySpec.getName()) ? " *" : "");
    }
}
